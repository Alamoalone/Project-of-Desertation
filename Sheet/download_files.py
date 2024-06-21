import requests
import json
import time
import re
import os
import shutil
from requests.exceptions import HTTPError

def get_link_url(headers, rel):
    """anaysly link """
    links = headers.get('Link', "").split(', ')
    for link in links:
        if 'rel="%s"' % rel in link:
            return re.search(r'<(.+?)>', link).group(1)
    return None

def get_commits(url, headers):
    """request GitHub API and process the limitaiton"""
    while True:
        try:
            response = requests.get(url, headers=headers)
            response.raise_for_status()
            return response
        except HTTPError as http_err:
            if 'X-RateLimit-Remaining' in response.headers and response.headers['X-RateLimit-Remaining'] == '0':
                # when it reach the limitation and use 'X-RateLimit-Reset'
                reset_time = int(response.headers['X-RateLimit-Reset'])
                sleep_time = reset_time - int(time.time()) + 1  # add 1 second for buffer
                print(f"Rate limit reached. Retrying after {sleep_time} seconds.")
                time.sleep(sleep_time)
            else:
                print(f'HTTP error occurred: {http_err}')
                time.sleep(10)
        except Exception as err:
            print(f'Other error occurred: {err}')
            time.sleep(10)

def save_state(state, state_file_path):
    if not state:
        print("Warning: Attempted to save an empty state. This is not expected unless it's the end of the pages.")
        return
    os.makedirs(os.path.dirname(state_file_path), exist_ok=True)
    with open(state_file_path, 'w') as file:
        file.write(state)
        print(f"Saved state: {state}")

def load_state(state_file_path):
    try:
        with open(state_file_path, 'r') as file:
            return file.read().strip()
    except FileNotFoundError:
        return None

def download_file(url, folder, filename):
    simple_filename = os.path.basename(filename)
    file_path = os.path.join(folder, simple_filename)
    
    try:
        response = requests.get(url, timeout=10)  
        response.raise_for_status()  
        with open(file_path, 'wb') as file:
            file.write(response.content)
        return True  
    except requests.HTTPError as http_err:
        print(f"HTTP error occurred while downloading {url}: {http_err}")  
    except requests.Timeout as timeout_err:
        print(f"Timeout error occurred while downloading {url}: {timeout_err}")  
    except requests.RequestException as err:
        print(f"An error occurred while downloading {url}: {err}")  
    except Exception as e:
        print(f"An error occurred while saving the file {filename}: {e}")  
    return False  

def process_commit(commit_data, local_file_path, repository_name):
    if len(commit_data['parents']) == 1:
        parent_sha = commit_data['parents'][0]['sha']
        repository_folder = os.path.join(local_file_path, repository_name)
        os.makedirs(repository_folder, exist_ok=True)
        current_sha = commit_data['sha']
        current_commit_folder = os.path.join(repository_folder, current_sha)
        os.makedirs(current_commit_folder, exist_ok=True)

        json_file_path = os.path.join(current_commit_folder, f"{repository_name}_{current_sha}.json")
        
        for file in commit_data['files']:
            if file['filename'].endswith('.java'):
                commit_sha = file['sha']
                commit_folder = os.path.join(current_commit_folder, f"commit_{commit_sha}")
                os.makedirs(commit_folder, exist_ok=True)

                after_commit_folder = os.path.join(commit_folder, 'after_commit')
                before_commit_folder = os.path.join(commit_folder, 'before_commit')
                os.makedirs(after_commit_folder, exist_ok=True)
                os.makedirs(before_commit_folder, exist_ok=True)

                raw_url = file.get('raw_url', '')
                filename = file.get('filename', '')
                before_url = raw_url.replace(commit_data['sha'], parent_sha)
                if not download_file(before_url, before_commit_folder, filename):
                    print(f"Failed to download before commit file: {filename}")
                    shutil.rmtree(after_commit_folder)
                    shutil.rmtree(before_commit_folder)
                    
                else:    
                    if not download_file(raw_url, after_commit_folder, filename):
                        print(f"Failed to download after commit file: {filename}")
                        shutil.rmtree(after_commit_folder)
                        shutil.rmtree(before_commit_folder)
                    # Write patch data to a text file
                    if 'patch' in file:
                        patch_data = file['patch']
                        patch_file_path = os.path.join(commit_folder, f"patch_{filename.replace('/', '_')}.txt")
                        with open(patch_file_path, 'w', encoding='utf-8') as patch_file:
                            patch_file.write(patch_data)
                
                if not os.listdir(commit_folder):
                    shutil.rmtree(commit_folder)
                
        if not os.listdir(current_commit_folder):
            shutil.rmtree(current_commit_folder)
        else:
            with open(json_file_path, 'w', encoding='utf-8') as file:
                    json.dump(commit_data, file, ensure_ascii=False, indent=4)
                
        if not os.listdir(repository_folder):  
            shutil.rmtree(repository_folder)
            

def main():
    # local path
    local_file_base_path = os.getcwd()
    local_file_path = os.path.join(local_file_base_path, 'GitHubCommits')
    os.makedirs(local_file_path, exist_ok=True)
    state_file_path = os.path.join(local_file_base_path, 'github_api_state.txt')

    # launch last state
    state = load_state(state_file_path)
    print(f"Loaded state: {state}")
    if state is None:
        base_url = "https://api.github.com/search/commits?q=null+pointer+exception&language=java"
        save_state(base_url, state_file_path)
        state = base_url

    headers = {'Accept': 'application/vnd.github.cloak-preview'}
    next_page_url = state

    while next_page_url:
        response = get_commits(next_page_url, headers)
        if response:
            data = response.json()
            for item in data['items']:
                # check state of 'parents'
                commit_url = item['url']
                repository_name = item['repository']['name']
                commit_response = get_commits(commit_url, headers)
                commit_data = commit_response.json()
                process_commit(commit_data, local_file_path, repository_name)

            next_page_url = get_link_url(response.headers, 'next')
            save_state(next_page_url, state_file_path)  # save state
            print(f"Next page URL saved as state: {next_page_url}")
            time.sleep(1)  # avoid limitation
        else:
            break

if __name__ == "__main__":
    main()

import ollama
import pandas as pd
import os
import json
import argparse


def read_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        return file.read()

def read_json(directory):
    for file_name in os.listdir(directory):
        if file_name.endswith('.json'):
            json_file_path = os.path.join(directory, file_name)
            with open(json_file_path, 'r') as file:
                data = json.load(file)
                return data
    print(f"No JSON file found in {directory}")
    return None
    
def prediction(file_content, methods):
    # Construct the inquiry
    #prompt = f"Is there a NullPointerException in the given Java code:\n\n{file_content}\n\n? Think step by step but end your answer with the word yes, no, or unclear.Limit your answer to 1 word"
    prompt = f"Examine only the extracted methods:\n\n{methods}\n\n in the following Java code:\n\n{file_content}\n\n. Analyze step by step to determine if there is a potential for a NullPointerException.But Limit your answer with one word: yes, no, or unclear."
    response = ollama.chat(
        model='llama3', 
        messages=[
            {
                'role': 'user',
                'content': prompt,
            }
        ]
    )
    print(response['message']['content'])
    return response['message']['content']

def process_all(base_dir, output_filename):
    if not os.path.exists(output_filename):
        build_new_sheet(output_filename)

    df = pd.read_excel(output_filename) if os.path.exists(output_filename) else pd.DataFrame()

    for commit_dir in os.listdir(base_dir):
        repository_folder = os.path.join(base_dir, commit_dir)
        if os.path.isdir(repository_folder):
            for sub_commit_dir in os.listdir(repository_folder):
                commit_sha_folder = os.path.join(repository_folder, sub_commit_dir)
                json_data = read_json(commit_sha_folder)
                if json_data is None:
                    continue
                current_sha = json_data["sha"]
                commit_message = json_data["commit"]["message"]
                first_parent_sha = json_data["parents"][0]["sha"]
                files_info = json_data["files"]
                if os.path.isdir(commit_sha_folder):
                    for file_dir in os.listdir(commit_sha_folder):
                        clean_commit_str = file_dir.replace("commit_", "")
                        commit_url = ''
                        for file_info in files_info:
                            if file_info["sha"] == clean_commit_str:
                                commit_url = file_info["raw_url"]
                        commit_path = os.path.join(commit_sha_folder, file_dir)
                        if os.path.isdir(commit_path):
                            before_path = os.path.join(commit_path, 'before_commit')
                            after_path = os.path.join(commit_path, 'after_commit')
                            txt_path = os.path.join(commit_path, 'changed_methods.txt')
                            if os.path.exists(txt_path):
                                methods = read_file(txt_path)
                                if os.path.exists(before_path): 
                                    for file_name in os.listdir(before_path):
                                        file_before = os.path.join(before_path, file_name)
                                        base_file_name = os.path.splitext(os.path.basename(file_before))[0]
                                        file_content_before = read_file(file_before)
                                        before_url = commit_url.replace(current_sha, first_parent_sha)
                                        answer_string = prediction(file_content_before,methods).lower()
                                        contains_yes = 'yes' in answer_string
                                        contains_no = 'no' in answer_string
                                        exist_str = 'Y' if contains_yes else 'N' if contains_no else 'UN'
                                        new_row = {"Repo name": commit_dir, "commit hash": clean_commit_str, "before/after file": f"{base_file_name}_before","has NPE":exist_str,"commit url":before_url,"message":commit_message}
                                        df.loc[len(df)] = new_row
                                        
                                if os.path.exists(after_path):
                                    for file_name in os.listdir(after_path):
                                        file_after = os.path.join(after_path, file_name)
                                        base_file_name = os.path.splitext(os.path.basename(file_after))[0]
                                        file_content_after = read_file(file_after)
                                        answer_string = prediction(file_content_after,methods).lower()
                                        contains_yes = 'yes' in answer_string
                                        contains_no = 'no' in answer_string
                                        exist_str = 'Y' if contains_yes else 'N' if contains_no else 'UN' 
                                        new_row = {"Repo name": commit_dir, "commit hash": clean_commit_str, "before/after file": f"{base_file_name}_after","has NPE":exist_str,"commit url":commit_url,"message":commit_message}
                                        df.loc[len(df)] = new_row 
                            else:
                                continue
                                                         
    df.to_excel(output_filename, index=False, engine='openpyxl')
    print(f"Updated spreadsheet '{output_filename}' with new data.") 

def build_new_sheet(output_filename):
    columns = ["Repo name","commit hash", "before/after file", "has NPE", "commit url", "message"]
    df = pd.DataFrame(columns=columns)
    df.to_excel(output_filename, index=False, engine='openpyxl')
    print(f"Spreadsheet '{output_filename}' has been created because it did not exist.")  

def main():
    parser = argparse.ArgumentParser(description="Process some integers.")
    parser.add_argument('github_commits_dir', type=str, help='Directory containing the commit data')
    parser.add_argument('output_filename', type=str, help='Output file name for the Excel sheet')
    args = parser.parse_args()
    process_all(args.github_commits_dir, args.output_filename)

if __name__ == "__main__":
    main()
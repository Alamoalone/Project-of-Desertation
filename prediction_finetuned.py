import json
import requests
import pandas as pd

# Function to generate a response from the model
def generate_response(prompt):
    response = requests.post(
        "http://127.0.0.1:8000/predict",
        json={"prompt": prompt}
    )
    print(response.json()["output"])
    return response.json()["output"]

def read_json(json_file):
    # Load JSON data
    with open(json_file, 'r') as f:
        json_data = json.load(f)
    return json_data

def main():
    df = pd.read_excel('valid_data.xlsx') 
    json_data = read_json('my_valid.json')
    # Write responses to Excel
    for item in json_data:
        instruction = item['instruction']
        response = generate_response(instruction.strip())
        
        repo_name = item['meta']['Repo name']
        commit_hash = item['meta']['commit hash']
        file_name = item['meta']['file']
        
        # Find the matching row in the DataFrame
        match = df[(df['Repo name'] == repo_name) & (df['commit hash'] == commit_hash) & (df['before/after file'] == file_name)]
        if not match.empty:
            row_index = match.index[0]
            contains_yes = 'yes' in response
            contains_no = 'no' in response
            exist_str = 'Y' if contains_yes else 'N' if contains_no else 'UN'
            df.loc[row_index, 'has NPE'] = exist_str                
            break
    df.to_excel('valid_data.xlsx', index=False, engine='openpyxl')
    print(f"Updated spreadsheet 'alid_data.xlsx' with new data.")

if __name__ == "__main__":
    main()

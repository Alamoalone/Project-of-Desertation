import os
import argparse
import pandas as pd
import json
import random

def read_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        return file.read()

def sanitize_java_code(code):
    # Escape backticks to avoid breaking the markdown format in JSON
    return code.replace('`', '\\`')

def shuffle_and_split(data, ratio=0.5):
    # Randomly shuffle the data
    random.shuffle(data)
    # Split the data according to the specified ratio
    split_point = int(len(data) * ratio)
    return data[:split_point], data[split_point:]

def load_and_process_files(base_dir):
    data = []
    for commit_dir in os.listdir(base_dir):
        repository_folder = os.path.join(base_dir, commit_dir)
        if os.path.isdir(repository_folder):
            for sub_commit_dir in os.listdir(repository_folder):
                clean_commit_str = sub_commit_dir.replace("commit_", "")
                commit_sha_folder = os.path.join(repository_folder, sub_commit_dir)
                if os.path.isdir(commit_sha_folder):
                    if len(os.listdir(commit_sha_folder)) != 2:
                            continue                        
                    for file in os.listdir(commit_sha_folder):
                        file_path = os.path.join(commit_sha_folder, file)
                        file_content = read_file(file_path)
                        base_file_name = os.path.splitext(os.path.basename(file_path))[0] 
                        sanitized_code = sanitize_java_code(file_content)
                        instruction = f"Is there a NullPointerException in the given Java code:\n\n```java\n{sanitized_code}\n```\n\n? Limit your answer to 1 word."
                        output = 'no'
                        if file.endswith('_before.txt'):
                            output = 'yes'  # Assuming the default answer is 'yes', you can adjust logic here.
                        data.append({
                            "instruction": instruction,
                            "input": "",
                            "output": output,
                            "meta": {"Repo name": commit_dir, "commit hash": clean_commit_str, "file": base_file_name}
                        })
    return data

def save_to_json(data, output_filename, is_train):
    # Remove the 'meta' key from each data entry before saving
    if is_train:
        for entry in data:
            if 'meta' in entry:
                del entry['meta']

    with open(output_filename, 'w', encoding='utf-8') as outfile:
        json.dump(data, outfile, indent=4)
    print(f"Generated JSON file with {len(data)} entries.")

def update_excel_sheet(valid_data, output_filename):
    if not os.path.exists(output_filename):
        columns = ["Repo name","commit hash", "before/after file","has NPE"]
        df = pd.DataFrame(columns=columns)
    else:
        df = pd.read_excel(output_filename)

    for item in valid_data:
        new_row = {"Repo name": item["meta"]["Repo name"], "commit hash": item["meta"]["commit hash"], "before/after file": f"{item['meta']['file']}"}
        df.loc[len(df)] = new_row

    df.to_excel(output_filename, index=False, engine='openpyxl')
    print(f"Updated spreadsheet '{output_filename}' with validation data.") 

def main():
    parser = argparse.ArgumentParser(description="Run NPE prediction on Java code files.")
    parser.add_argument('input_dir', type=str, help='Directory containing the code files')
    parser.add_argument('output_valid_filename', type=str, help='Output file name for the Excel sheet')
    parser.add_argument('output_train', type=str, help='Filename for the output training JSON file')
    parser.add_argument('output_valid', type=str, help='Filename for the output validation JSON file')
    args = parser.parse_args()
    
    data = load_and_process_files(args.input_dir)
    train_data, valid_data = shuffle_and_split(data)
    
    # Update Excel before removing meta data
    update_excel_sheet(valid_data, args.output_valid_filename)
    
    # Save JSON files after Excel update to ensure meta is used correctly
    save_to_json(train_data, args.output_train, True)
    save_to_json(valid_data, args.output_valid, False)

if __name__ == "__main__":
    main()

import ollama
import pandas as pd
import os
import argparse

def read_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        return file.read()
    
def prediction(file_content):
    # Construct the inquiry
    prompt = f"Is there a NullPointerException in the given Java code:\n\n{file_content}\n\n? Limit your answer to 1 word"
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


def load_file(base_dir, output_filename, isFunction):
    df = pd.read_excel(output_filename) if os.path.exists(output_filename) else pd.DataFrame()
    if 'has NPE' in df.columns:
        df['has NPE'] = df['has NPE'].astype(str)
        df['function contain NPE count'] = df['function contain NPE count'].astype(str)

    for commit_dir in os.listdir(base_dir):
        repository_folder = os.path.join(base_dir, commit_dir)
        if os.path.isdir(repository_folder):
            for sub_commit_dir in os.listdir(repository_folder):
                commit_sha_folder = os.path.join(repository_folder, sub_commit_dir)
                if os.path.isdir(commit_sha_folder):
                    for file in os.listdir(commit_sha_folder):
                        file_path = os.path.join(commit_sha_folder, file)
                        # Check if the file is the one we want to process
                        if file.endswith('.txt'):
                            file_name = file.replace(".txt", "")
                            print(f"Processing file: {file} in commit {commit_dir}")
                            if isFunction:
                                row_index = df.index[(df['before/after function'] == file_name)]
                            else :    
                                row_index = df.index[(df['before/after file'] == file_name)]
                            file_content = read_file(file_path)
                            answer_string = prediction(file_content).lower()
                            contains_yes = 'yes' in answer_string
                            contains_no = 'no' in answer_string
                            exist_str = 'Y' if contains_yes else 'N' if contains_no else 'UN'
                            if not row_index.empty:
                                df.loc[row_index, 'has NPE'] = exist_str
                            #    df.loc[row_index, 'function contain NPE count'] = answer_string                       
    df.to_excel(output_filename, index=False, engine='openpyxl')
    print(f"Updated spreadsheet '{output_filename}' with new data.")

def str2bool(v):
    if isinstance(v, bool):
       return v
    if v.lower() in ('yes', 'true', 't', 'y', '1'):
        return True
    elif v.lower() in ('no', 'false', 'f', 'n', '0'):
        return False
    else:
        raise argparse.ArgumentTypeError('Boolean value expected.')   

def main():
    parser = argparse.ArgumentParser(description="Run NPE prediction on Java code files.")
    parser.add_argument('input_dir', type=str, help='Directory containing the code files')
    parser.add_argument('output_filename', type=str, help='Filename for the output Excel sheet')
    parser.add_argument('isFunction', type=str2bool, help='Flag to indicate if the operation mode is based on functions')
    args = parser.parse_args()
    
    load_file(args.input_dir, args.output_filename, args.isFunction)

if __name__ == "__main__":
    main()

                      



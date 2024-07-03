import javalang
import pandas as pd
import json
import os
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
                #print(f"Loaded JSON file: {json_file_path}")
                return data
    print(f"No JSON file found in {directory}")
    return None

def parse_methods_improved(file_content, file_path):
    try:
        tree = javalang.parse.parse(file_content)
        methods = {}
        for _, node in tree.filter(javalang.tree.MethodDeclaration):
            start_line = node.position.line if node.position else None
            end_line = start_line
            method_code = ''

            # Improved logic to find the method's closing brace more accurately
            if node.body:
                method_code_lines = file_content.splitlines()
                open_braces, close_braces = 0, 0
                for i, line in enumerate(method_code_lines[start_line-1:], start=start_line):
                    open_braces += line.count('{')
                    close_braces += line.count('}')
                    # Check if we've found the matching closing brace for the method
                    if open_braces > 0 and open_braces == close_braces:
                        end_line = i
                        break

                method_code = '\n'.join(method_code_lines[start_line-1:end_line])

            method_signature = f"{node.name}({', '.join(f'{param.type.name} {param.name}' for param in node.parameters)})"
            methods[method_signature] = {
                'start_line': start_line,
                'end_line': end_line,
                'code': method_code
            }
            
        return methods
    except javalang.parser.JavaSyntaxError as e:
        print(f"Syntax error parsing {file_path}: {e}")
        return {}

def compare_files(df,isGenerateFunction,file_dir,file_path_before, file_path_after, output_dir, output_filename, commit_dir, clean_commit_str,current_sha,parent_sha, after_url, message):
    
    file_content_before = read_file(file_path_before)
    file_content_after = read_file(file_path_after)

    methods_before = parse_methods_improved(file_content_before, file_path_before)
    methods_after = parse_methods_improved(file_content_after, file_path_after)
    os.makedirs(output_dir, exist_ok=True)  # Ensure the directory exists
    before_url = after_url.replace(current_sha, parent_sha)

    if isGenerateFunction:
        files_created = False 
        changed_methods = []  # List to track all changed methods
        for method_signature in set(methods_before.keys()) | set(methods_after.keys()):
            before_method = methods_before.get(method_signature, {}).get('code', '')
            after_method = methods_after.get(method_signature, {}).get('code', '')
            
            if before_method != after_method:
                method_name = method_signature.split('(')[0].strip().replace(' ', '_')
                changed_methods.append(method_signature)  # Track the changed method

                output_path_before = os.path.join(output_dir, f"{method_name}_before.txt")
                with open(output_path_before, 'w', encoding='utf-8') as file_before:
                    # file_before.write(f"Changed method: {method_signature}\nBefore:\n")
                    file_before.write(before_method)
                    
                    new_row = {
                        "Repo name": commit_dir, 
                        "commit hash": clean_commit_str, 
                        "before/after function": f"{method_name}_before",
                        "commit url":before_url,
                        "message":message
                    }
                    df.loc[len(df)] = new_row
                
                output_path_after = os.path.join(output_dir, f"{method_name}_after.txt")
                with open(output_path_after, 'w', encoding='utf-8') as file_after:
                    # file_after.write(f"Changed method: {method_signature}\nAfter:\n")
                    file_after.write(after_method)
                    new_row = {
                        "Repo name": commit_dir, 
                        "commit hash": clean_commit_str, 
                        "before/after function": f"{method_name}_after",
                        "commit url":after_url,
                        "message":message
                    }
                    df.loc[len(df)] = new_row
                # Write all changed methods to a file
                if changed_methods:
                    with open(os.path.join(file_dir, "changed_methods.txt"), 'w', encoding='utf-8') as file:
                        file.write("\n".join(changed_methods))
                files_created = True 
        
        if not files_created:
            try:
                os.rmdir(output_dir)
            except OSError as e:
                print(f"Error: {e.strerror}")
    else :
        before_changes = []
        after_changes = []
        before_created = False
        after_created = False
        before_count = 0
        after_count = 0 
        base_file_name = os.path.splitext(os.path.basename(file_path_before))[0] 
        for method_signature in set(methods_before.keys()) | set(methods_after.keys()):
            before_method = methods_before.get(method_signature, {}).get('code', '')
            after_method = methods_after.get(method_signature, {}).get('code', '')
            
            if before_method != after_method:
                before_changes.append(before_method)
                before_changes.append("\n\n")
                before_count += 1
                after_changes.append(after_method)
                after_changes.append("\n\n")
                after_count += 1 
        
        if before_changes:
            output_path = os.path.join(output_dir, base_file_name + "_before.txt")
            with open(output_path, 'w', encoding='utf-8') as file:
                file.writelines(before_changes)
                new_row = {"Repo name": commit_dir, "commit hash": clean_commit_str, "before/after file": f"{base_file_name}_before","function changed count": before_count,"commit url":before_url,"message":message}
                df.loc[len(df)] = new_row
            before_created = True
        
        if after_changes:
            output_path = os.path.join(output_dir, base_file_name + "_after.txt")
            with open(output_path, 'w', encoding='utf-8') as file:
                file.writelines(after_changes)
                new_row = {"Repo name": commit_dir, "commit hash": clean_commit_str, "before/after file": f"{base_file_name}_after","function changed count": after_count,"commit url":after_url,"message":message}
                df.loc[len(df)] = new_row
            after_created = True
        
        if not before_created and not after_created:
            try:
                os.rmdir(output_dir)
            except OSError as e:
                print(f"Error: {e.strerror}")            

def traverse_and_compare(base_dir, output_filename, output_folder,isGenerateFunction):
    dataset_dir = os.path.join(os.path.dirname(base_dir), output_folder)
    if not os.path.exists(output_filename):
        build_new_sheet(output_filename,isGenerateFunction)

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
                            output_file_dir = os.path.join(dataset_dir, commit_dir, file_dir)  # Modified to use the dataset directory
                
                            if os.path.exists(before_path) and os.path.exists(after_path):
                                for file_name in os.listdir(before_path):
                                    file_before = os.path.join(before_path, file_name)
                                    file_after = os.path.join(after_path, file_name)
                                    if os.path.isfile(file_after):
                                        compare_files(df,isGenerateFunction,commit_path,file_before, file_after, output_file_dir, output_filename, commit_dir, clean_commit_str,current_sha,first_parent_sha, commit_url, commit_message)
    df.to_excel(output_filename, index=False, engine='openpyxl')
    print(f"Updated spreadsheet '{output_filename}' with new data.") 
  

def build_new_sheet(output_filename, isGenerateFunction):
    columns = []
    if isGenerateFunction:
        columns = ["Repo name","commit hash", "before/after function", "has NPE", "commit url", "message"]
    else:
        columns = ["Repo name","commit hash", "before/after file", "function changed count" , "has NPE", "function contain NPE count","commit url", "message"]
    
    df = pd.DataFrame(columns=columns)
    df.to_excel(output_filename, index=False, engine='openpyxl')
    print(f"Spreadsheet '{output_filename}' has been created because it did not exist.")

def remove_empty_folders(folder):
    for root, dirs, files in os.walk(folder, topdown=False):
        for name in dirs:
            current_dir = os.path.join(root, name)
            if not os.listdir(current_dir):
                print(f"Deleting empty folder: {current_dir}")
                os.rmdir(current_dir)
    
    for root, dirs, files in os.walk(folder):
        for name in dirs:
            current_dir = os.path.join(root, name)
            if not os.listdir(current_dir):
                print(f"Deleting empty folder: {current_dir}")
                os.rmdir(current_dir)

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
    
    parser = argparse.ArgumentParser(description="Process some integers.")
    parser.add_argument('github_commits_dir', type=str, help='Directory containing the commit data')
    parser.add_argument('output_filename', type=str, help='Output file name for the Excel sheet')
    parser.add_argument('output_folder', type=str, help='Output folder for the dataset')
    parser.add_argument('isGenerateFunction', type=str2bool, help='Flag to determine method of file generation')
    args = parser.parse_args()
    traverse_and_compare(args.github_commits_dir, args.output_filename, args.output_folder, args.isGenerateFunction)
    remove_empty_folders(args.output_folder)

if __name__ == "__main__":
    main()


import pandas as pd
import argparse

def process_npe_data_with_files(method_data, file_data):
    # Group by commit hash
    grouped = method_data.groupby('commit hash')
    result_rows = []
    unprocessed_data = []
    
    for commit, group in grouped:
        processed = False
        for suffix in ['_before', '_after']:
            sub_group = group[group['before/after function'].str.endswith(suffix)]
            if sub_group.empty:
                continue
            
            processed = True
            
            # Determine if any of the entries in the group has 'Y' for 'has NPE'
            if 'Y' in sub_group['has NPE'].values:
                has_npe = 'Y'
            else:
                has_npe = 'N'

            # Get corresponding before/after file content
            matching_files = file_data[(file_data['commit hash'] == commit) &
                                       (file_data['before/after file'].str.endswith(suffix))]
            
            # Combine the before and after file contents
            file_contents = matching_files['before/after file'].tolist()
            file_content_combined = '; '.join(file_contents)

            # Handle case when there's only one function group
            repo_name = sub_group['Repo name'].values[0]
            # commit_url = sub_group['commit url'].values[0]
            # message = sub_group['message'].values[0]

            # Append the result with additional required columns
            result_rows.append({
                'Repo name': repo_name,
                'commit hash': commit,
                'before/after file': file_content_combined,
                'has NPE': has_npe,
                # 'commit url': commit_url,
                # 'message': message
            })
        
        if not processed:
            unprocessed_data.append(group)
    
    result_df = pd.DataFrame(result_rows)
    
    if unprocessed_data:
        unprocessed_df = pd.concat(unprocessed_data)
        print("Unprocessed Data:")
        print(unprocessed_df)
    
    return result_df

def main():

    parser = argparse.ArgumentParser(description='Process NPE data and merge before/after files.')
    parser.add_argument('methods_sheet_path', type=str,  help='Path to the methods sheet Excel file.')
    parser.add_argument('file_sheet_path', type=str,  help='Path to the file sheet Excel file.')
    parser.add_argument('output_file_path_with_files', type=str, help='Path to save the processed Excel file.')

    args = parser.parse_args()

    method_data = pd.read_excel(args.methods_sheet_path)
    file_data = pd.read_excel(args.file_sheet_path)

    # Process the data
    processed_data_with_files = process_npe_data_with_files(method_data, file_data)

    # Save the processed data to a new Excel file
    processed_data_with_files.to_excel(args.output_file_path_with_files, index=False)

    # Display the location of the saved file
    print(f'The processed data has been saved to: {args.output_file_path_with_files}')

if __name__ == "__main__":
    main()

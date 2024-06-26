import pandas as pd

# Read data for each model
codellama_methods_df = pd.read_excel('Sheet/codellama_method_sheet.xlsx')
codellama_file_df = pd.read_excel('Sheet/codellama_file_sheet.xlsx')
codellama_code_df = pd.read_excel('Sheet/codellama_code_sheet.xlsx')

llama2_methods_df = pd.read_excel('Sheet/llama2_method_sheet.xlsx')
llama2_file_df = pd.read_excel('Sheet/llama2_file_sheet.xlsx')
llama2_code_df = pd.read_excel('Sheet/llama2_code_sheet.xlsx')

llama3_methods_df = pd.read_excel('Sheet/llama3_method_sheet.xlsx')
llama3_file_df = pd.read_excel('Sheet/llama3_file_sheet.xlsx')
llama3_code_df = pd.read_excel('Sheet/llama3_code_sheet.xlsx')

llama3_70B_methods_df = pd.read_excel('Sheet/llama3_70b_method_sheet.xlsx')
llama3_70B_file_df = pd.read_excel('Sheet/llama3_70b_file_sheet.xlsx')
llama3_70B_code_df = pd.read_excel('Sheet/llama3_70b_code_sheet.xlsx')

gemma_methods_df = pd.read_excel('Sheet/gemma_method_sheet.xlsx')
gemma_file_df = pd.read_excel('Sheet/gemma_file_sheet.xlsx')
gemma_code_df = pd.read_excel('Sheet/gemma_code_sheet.xlsx')

phi2_methods_df = pd.read_excel('Sheet/phi_method_sheet.xlsx')
phi2_file_df = pd.read_excel('Sheet/phi_file_sheet.xlsx')
phi2_code_df = pd.read_excel('Sheet/phi_code_sheet.xlsx')

# Function to merge results from different models
def merge_results(codellama_df, llama2_df, llama3_df, llama3_70B_df,gemma_df,phi2_df, key_columns):
    merged_df = codellama_df.copy()
    merged_df = merged_df.merge(llama2_df, on=key_columns, suffixes=('_codellama', '_llama2'))
    merged_df = merged_df.merge(llama3_df, on=key_columns, suffixes=('', '_llama3'))
    merged_df = merged_df.merge(llama3_70B_df, on=key_columns, suffixes=('', '_llama3_70B'))
    merged_df = merged_df.merge(gemma_df, on=key_columns, suffixes=('', '_gemma'))
    merged_df = merged_df.merge(phi2_df, on=key_columns, suffixes=('', '_phi2'))
    return merged_df

# Merge method, file, and code results
key_columns_method = ['Repo name', 'commit hash', 'before/after file', 'commit url', 'message']
key_columns_file = ['Repo name', 'commit hash', 'before/after file', 'function changed count','commit url', 'message']
key_columns_code = ['Repo name', 'commit hash', 'before/after file','commit url', 'message']

merged_methods_df = merge_results(codellama_methods_df, llama2_methods_df, llama3_methods_df, llama3_70B_methods_df,gemma_methods_df,phi2_methods_df, key_columns_method)
merged_file_df = merge_results(codellama_file_df, llama2_file_df, llama3_file_df, llama3_70B_file_df,gemma_file_df,phi2_file_df, key_columns_file)
merged_code_df = merge_results(codellama_code_df, llama2_code_df, llama3_code_df, llama3_70B_code_df,gemma_code_df,phi2_code_df, key_columns_code)

# Function to compute agreement on each commit point
def compute_agreement(df, prediction_columns):
    df['agreement'] = df[prediction_columns].apply(lambda row: row.nunique() == 1, axis=1)
    return df

# Compute agreement for each merged DataFrame
prediction_columns_method = ['has NPE_codellama', 'has NPE_llama2', 'has NPE', 'has NPE_llama3_70B','has NPE_gemma','has NPE_phi2']
prediction_columns_file = ['has NPE_codellama', 'has NPE_llama2', 'has NPE', 'has NPE_llama3_70B','has NPE_gemma','has NPE_phi2']
prediction_columns_code = ['has NPE_codellama', 'has NPE_llama2', 'has NPE', 'has NPE_llama3_70B','has NPE_gemma','has NPE_phi2']

merged_method_df = compute_agreement(merged_methods_df, prediction_columns_method)
merged_file_df = compute_agreement(merged_file_df, prediction_columns_file)
merged_code_df = compute_agreement(merged_code_df, prediction_columns_code)

# Save results to a new Excel file
with pd.ExcelWriter('Sheet/merged_model_results.xlsx') as writer:
    merged_method_df.to_excel(writer, sheet_name='Method', index=False)
    merged_file_df.to_excel(writer, sheet_name='File', index=False)
    merged_code_df.to_excel(writer, sheet_name='Code', index=False)

print("Merged results saved to 'Sheet/merged_model_results.xlsx'")

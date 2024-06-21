import pandas as pd
import argparse

def main():
    parser = argparse.ArgumentParser(description="Run NPE prediction on Java code files.")
    parser.add_argument('files_sheet', type=str, help='Path to the Excel sheet containing the code files')
    parser.add_argument('methods_sheet', type=str, help='Path to the Excel sheet containing the methods information')
    args = parser.parse_args()
    
    files_sheet = pd.read_excel(args.files_sheet)
    methods_sheet = pd.read_excel(args.methods_sheet)

    # Extract suffix from methods_sheet
    methods_suffix = methods_sheet['before/after function'].str.extract(r'_(before|after)$')[0]
    methods_commit_suffix = (methods_sheet['commit hash'] + '_' + methods_suffix).str.strip().str.lower()

    # Extract suffix from files_sheet
    files_suffix = files_sheet['before/after file'].str.extract(r'_(before|after)$')[0]
    files_commit_suffix = (files_sheet['commit hash'] + '_' + files_suffix).str.strip().str.lower()

    # Get unique commit_suffix sets and find intersection
    methods_suffix_set = set(methods_commit_suffix.unique())
    files_suffix_set = set(files_commit_suffix.unique())
    intersection = methods_suffix_set.intersection(files_suffix_set)
    print("Number of matching commit_suffix:", len(intersection))

    # Calculate NPE counts
    npe_counts_before = methods_sheet[(methods_sheet['has NPE'] == 'Y') & (methods_sheet['before/after function'].str.contains('_before'))].groupby(methods_commit_suffix).size()
    npe_counts_after = methods_sheet[(methods_sheet['has NPE'] == 'Y') & (methods_sheet['before/after function'].str.contains('_after'))].groupby(methods_commit_suffix).size()

    # Apply function to files_sheet without adding extra columns
    def get_npe_count(commit_suffix):
        if '_before' in commit_suffix:
            return npe_counts_before.get(commit_suffix, 0)
        else:
            return npe_counts_after.get(commit_suffix, 0)

    files_sheet['function contain NPE count'] = files_commit_suffix.apply(lambda x: get_npe_count(x) if x in intersection else 0)

    files_sheet.to_excel(args.files_sheet, index=False)

if __name__ == "__main__":
    main()

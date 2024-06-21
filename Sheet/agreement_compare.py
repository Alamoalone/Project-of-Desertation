import pandas as pd
import matplotlib.pyplot as plt

# Function to count agreements and disagreements
def count_agreements(df):
    total = len(df)
    agreements = df['agreement'].sum()
    disagreements = total - agreements
    return total, agreements, disagreements

# Load the merged results
merged_methods_df = pd.read_excel('merged_model_results.xlsx', sheet_name='Methods')
merged_file_df = pd.read_excel('merged_model_results.xlsx', sheet_name='Files')
merged_code_df = pd.read_excel('merged_model_results.xlsx', sheet_name='Code')

# Count agreements and disagreements for each dataset
methods_total, methods_agreements, methods_disagreements = count_agreements(merged_methods_df)
file_total, file_agreements, file_disagreements = count_agreements(merged_file_df)
code_total, code_agreements, code_disagreements = count_agreements(merged_code_df)

# Create a summary DataFrame
summary_df = pd.DataFrame({
    'Dataset': ['Methods', 'Files', 'Code'],
    'Total': [methods_total, file_total, code_total],
    'Agreements': [methods_agreements, file_agreements, code_agreements],
    'Disagreements': [methods_disagreements, file_disagreements, code_disagreements]
})

# Display the summary
print(summary_df)

# Plotting the agreements and disagreements
fig, ax = plt.subplots(figsize=(10, 6))

bar_width = 0.35
index = summary_df['Dataset']

bar1 = plt.bar(index, summary_df['Agreements'], bar_width, label='Agreements')
bar2 = plt.bar(index, summary_df['Disagreements'], bar_width, bottom=summary_df['Agreements'], label='Disagreements')

plt.xlabel('Dataset')
plt.ylabel('Count')
plt.title('Agreements and Disagreements across Datasets')
plt.legend()

# Adding data labels
for bar in bar1 + bar2:
    yval = bar.get_height()
    plt.text(bar.get_x() + bar.get_width() / 2, bar.get_y() + yval / 2, int(yval), ha='center', va='center', color='white', fontweight='bold')

plt.show()

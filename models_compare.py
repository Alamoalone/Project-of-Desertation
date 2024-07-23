import pandas as pd
import matplotlib.pyplot as plt
from sklearn.metrics import precision_score, recall_score, f1_score, accuracy_score, confusion_matrix, roc_curve, roc_auc_score

# Function to compute metrics
def compute_metrics(df, model_prefix, column):
    y_pred = df[f'has NPE_{model_prefix}'].map({'Y': 1, 'N': 0, 'UN': 0}).values
    y_true = df[column].apply(lambda x: 0 if 'after' in x else (1 if 'before' in x else 0)).values
    # y_true = df[column].apply(lambda x: 1 if 'before' in x else 0).values
    # y_pred = df[f'has NPE_{model_prefix}'].map({'Y': 1, 'N': 0, 'UN': 0}).values

    # Filter out UN tags
    valid_indices = df[f'has NPE_{model_prefix}'].map({'Y': True, 'N': True, 'UN': False}).values
    y_true = y_true[valid_indices]
    y_pred = y_pred[valid_indices]
    
    precision = precision_score(y_true, y_pred, zero_division=1)
    recall = recall_score(y_true, y_pred, zero_division=1)
    f1 = f1_score(y_true, y_pred, zero_division=1)
    accuracy = accuracy_score(y_true, y_pred)
    
    cm = confusion_matrix(y_true, y_pred)
    fp = cm[0][1]
    tn = cm[0][0]
    false_positive_rate = fp / (fp + tn)

    # Calculate ROC and AUC
    y_scores = df[f'has NPE_{model_prefix}'].map({'Y': 1, 'N': 0, 'UN': 0}).values[valid_indices]
    fpr, tpr, thresholds = roc_curve(y_true, y_scores)
    auc = roc_auc_score(y_true, y_scores)
    
    return precision, recall, f1, accuracy, false_positive_rate, fpr, tpr, auc

# Function to process each model's data
def process_model_data(method_d, file_df, code_df, model_prefix):
    method_metrics = compute_metrics(method_d, model_prefix, 'before/after file')
    file_metrics = compute_metrics(file_df, model_prefix, 'before/after file')
    code_metrics = compute_metrics(code_df, model_prefix, 'before/after file')
    
    return {
        f'{model_prefix}_Method': method_metrics,
        f'{model_prefix}_File': file_metrics,
        f'{model_prefix}_Code': code_metrics
    }

def main():
    # Load the merged results
    merged_method_df = pd.read_excel('Sheet/merged_model_results.xlsx', sheet_name='Method')
    merged_file_df = pd.read_excel('Sheet/merged_model_results.xlsx', sheet_name='File')
    merged_code_df = pd.read_excel('Sheet/merged_model_results.xlsx', sheet_name='Code')

    # Process data for each model
    llama2_metrics = process_model_data(merged_method_df, merged_file_df, merged_code_df, 'llama2')
    llama3_metrics = process_model_data(merged_method_df, merged_file_df, merged_code_df, 'llama3')
    codellama_metrics = process_model_data(merged_method_df, merged_file_df, merged_code_df, 'codellama')
    llama3_70B_metrics = process_model_data(merged_method_df, merged_file_df, merged_code_df, 'llama3_70B')
    gemma_metrics = process_model_data(merged_method_df, merged_file_df, merged_code_df, 'gemma')
    phi2_metrics = process_model_data(merged_method_df, merged_file_df, merged_code_df, 'phi2')

    # Combine metrics into a single DataFrame
    combined_metrics = {**llama2_metrics, **llama3_metrics, **codellama_metrics, **llama3_70B_metrics, **gemma_metrics, **phi2_metrics}
    # metrics_df = pd.DataFrame(combined_metrics, index=['Precision', 'Recall', 'F1 Score', 'Accuracy', 'False Positive Rate'])
    metrics_df = pd.DataFrame(combined_metrics, index=['Precision', 'Recall', 'F1 Score', 'Accuracy', 'False Positive Rate', 'FPR', 'TPR', 'AUC'])

    # Plotting each metric separately
    for metric in ['Precision', 'Recall', 'F1 Score', 'Accuracy', 'False Positive Rate']:
        fig, ax = plt.subplots(figsize=(12, 8))
        metrics_df.loc[metric].plot(kind='bar', ax=ax, rot=45)
        ax.set_title(f'{metric} for Various Models')
        ax.set_xlabel('Model and Granularity')
        ax.set_ylabel('Score')
        ax.grid(True)
        ax.set_xticklabels(ax.get_xticklabels(), rotation=45, ha='right')
        ax.legend(loc='best')

        # Adding data labels
        for p in ax.patches:
            ax.annotate(f'{p.get_height():.2f}', 
                        (p.get_x() + p.get_width() / 2., p.get_height()), 
                        ha='center', va='center', xytext=(0, 10), 
                        textcoords='offset points')

        plt.tight_layout()
        plt.show()
    
    # Plotting ROC curves
    for model_prefix in ['llama2', 'llama3', 'codellama', 'llama3_70B', 'gemma', 'phi2']:
        fig, ax = plt.subplots(figsize=(12, 8))
        for granularity in ['Method', 'File', 'Code']:
            fpr = metrics_df.loc['FPR', f'{model_prefix}_{granularity}']
            tpr = metrics_df.loc['TPR', f'{model_prefix}_{granularity}']
            auc = metrics_df.loc['AUC', f'{model_prefix}_{granularity}']
            ax.plot(fpr, tpr, label=f'{model_prefix}_{granularity} (AUC = {auc:.2f})')

        ax.plot([0, 1], [0, 1], 'k--', label='Random guess')
        ax.set_xlabel('False Positive Rate')
        ax.set_ylabel('True Positive Rate')
        ax.set_title(f'ROC Curve for {model_prefix}')
        ax.legend(loc='best')
        ax.grid(True)
        
        plt.tight_layout()
        plt.show()
    
if __name__ == "__main__":
    main()



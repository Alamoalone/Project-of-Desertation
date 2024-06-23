import pandas as pd
import matplotlib.pyplot as plt
from sklearn.metrics import precision_score, recall_score, f1_score, accuracy_score, confusion_matrix

# Function to compute metrics
def compute_metrics(df, column):
    # y_true = df[f'has NPE_{model_prefix}'].map({'Y': 1, 'N': 0, 'UN': 0}).values
    # y_pred = df[column].apply(lambda x: 1 if 'after' in x else 0).values
    y_true = df[column].apply(lambda x: 1 if 'before' in x else 0).values
    y_pred = df[f'has NPE'].map({'Y': 1, 'N': 0, 'UN': 0}).values
    
    precision = precision_score(y_true, y_pred, zero_division=1)
    recall = recall_score(y_true, y_pred, zero_division=1)
    f1 = f1_score(y_true, y_pred, zero_division=1)
    accuracy = accuracy_score(y_true, y_pred)
    
    cm = confusion_matrix(y_true, y_pred)
    fp = cm[0][1]
    tn = cm[0][0]
    false_positive_rate = fp / (fp + tn)
    
    return precision, recall, f1, accuracy, false_positive_rate

# Function to plot metrics
def plot_metrics(metrics_fine_tuned, metrics_non_fine_tuned, model_name):
    labels = ['Precision', 'Recall', 'F1 Score', 'Accuracy', 'False Positive Rate']
    
    values_fine_tuned = [metrics_fine_tuned['precision'], metrics_fine_tuned['recall'], metrics_fine_tuned['f1'], metrics_fine_tuned['accuracy'], metrics_fine_tuned['false_positive_rate']]
    values_non_fine_tuned = [metrics_non_fine_tuned['precision'], metrics_non_fine_tuned['recall'], metrics_non_fine_tuned['f1'], metrics_non_fine_tuned['accuracy'], metrics_non_fine_tuned['false_positive_rate']]
    
    x = range(len(labels))
    
    plt.figure(figsize=(12, 6))
    bars_fine_tuned = plt.bar(x, values_fine_tuned, width=0.4, label='Fine-tuned', align='center', color='blue')
    bars_non_fine_tuned = plt.bar(x, values_non_fine_tuned, width=0.4, label='Non Fine-tuned', align='edge', color='orange')
    
    # Annotate bars with values
    for bar in bars_fine_tuned:
        height = bar.get_height()
        plt.annotate(f'{height:.2f}', 
                     xy=(bar.get_x() + bar.get_width() / 2, height),
                     xytext=(0, 3),  # 3 points vertical offset
                     textcoords="offset points",
                     ha='center', va='bottom')
    
    for bar in bars_non_fine_tuned:
        height = bar.get_height()
        plt.annotate(f'{height:.2f}', 
                     xy=(bar.get_x() + bar.get_width() / 2, height),
                     xytext=(0, 3),  # 3 points vertical offset
                     textcoords="offset points",
                     ha='center', va='bottom')
    
    plt.xticks(x, labels)
    plt.ylim(0, 1)
    plt.ylabel('Score')
    plt.title(f'Performance Metrics Comparison for {model_name}')
    plt.legend()
    plt.show()

def main():
    # Load data
    df_fine_tuned = pd.read_excel('Sheet/valid_data.xlsx', engine='openpyxl')
    df_non_fine_tuned = pd.read_excel('Sheet/phi_file_sheet.xlsx', engine='openpyxl')
    
    # List of models to evaluate
    model = 'Phi2'
    column = 'before/after file'  # Replace with the correct column name if different

    # Compute metrics for fine-tuned model
    metrics_fine_tuned = compute_metrics(df_fine_tuned, column)
    metrics_dict_fine_tuned = {
        'precision': metrics_fine_tuned[0],
        'recall': metrics_fine_tuned[1],
        'f1': metrics_fine_tuned[2],
        'accuracy': metrics_fine_tuned[3],
        'false_positive_rate': metrics_fine_tuned[4]
    }
        
    # Compute metrics for non-fine-tuned model
    metrics_non_fine_tuned = compute_metrics(df_non_fine_tuned, column)
    metrics_dict_non_fine_tuned = {
        'precision': metrics_non_fine_tuned[0],
        'recall': metrics_non_fine_tuned[1],
        'f1': metrics_non_fine_tuned[2],
        'accuracy': metrics_non_fine_tuned[3],
        'false_positive_rate': metrics_non_fine_tuned[4]
    }
        
    print(f"Metrics for {model} (Fine-tuned): {metrics_dict_fine_tuned}")
    print(f"Metrics for {model} (Non Fine-tuned): {metrics_dict_non_fine_tuned}")
        
    # Plot comparison
    plot_metrics(metrics_dict_fine_tuned, metrics_dict_non_fine_tuned, model)
        

if __name__ == "__main__":
    main()
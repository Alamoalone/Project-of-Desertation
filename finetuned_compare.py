import pandas as pd
import matplotlib.pyplot as plt
from sklearn.metrics import precision_score, recall_score, f1_score, accuracy_score, confusion_matrix, roc_curve, roc_auc_score

# Function to compute metrics
def compute_metrics(df, column):
    y_pred = df['has NPE'].map({'Y': 1, 'N': 0, 'UN': 0}).values
    y_true = df[column].apply(lambda x: 0 if 'after' in x else (1 if 'before' in x else 0)).values
    # y_true = df[column].apply(lambda x: 1 if 'before' in x else 0).values
    # y_pred = df['has NPE'].map({'Y': 1, 'N': 0, 'UN': 0}).values

    # Filter out UN tags
    valid_indices = df['has NPE'].map({'Y': True, 'N': True, 'UN': False}).values
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
    fpr, tpr, thresholds = roc_curve(y_true, y_pred)
    auc = roc_auc_score(y_true, y_pred)

    return precision, recall, f1, accuracy, false_positive_rate, fpr, tpr, auc

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

# Function to plot ROC curves
def plot_roc_curve(fpr_fine_tuned, tpr_fine_tuned, auc_fine_tuned, fpr_non_fine_tuned, tpr_non_fine_tuned, auc_non_fine_tuned, model_name):
    plt.figure(figsize=(12, 6))
    plt.plot(fpr_fine_tuned, tpr_fine_tuned, color='blue', lw=2, label=f'Fine-tuned (AUC = {auc_fine_tuned:.2f})')
    plt.plot(fpr_non_fine_tuned, tpr_non_fine_tuned, color='orange', lw=2, label=f'Non Fine-tuned (AUC = {auc_non_fine_tuned:.2f})')
    plt.plot([0, 1], [0, 1], color='gray', lw=2, linestyle='--', label='Random guess')
    
    plt.xlim([0.0, 1.0])
    plt.ylim([0.0, 1.05])
    plt.xlabel('False Positive Rate')
    plt.ylabel('True Positive Rate')
    plt.title(f'ROC Curve Comparison for {model_name}')
    plt.legend(loc="lower right")
    plt.grid()
    plt.show()

def main():
    # Load data
    df_fine_tuned = pd.read_excel('Sheet/file_valid.xlsx', engine='openpyxl')
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
        'false_positive_rate': metrics_fine_tuned[4],
        'fpr': metrics_fine_tuned[5],
        'tpr': metrics_fine_tuned[6],
        'auc': metrics_fine_tuned[7]
    }
        
    # Compute metrics for non-fine-tuned model
    metrics_non_fine_tuned = compute_metrics(df_non_fine_tuned, column)
    metrics_dict_non_fine_tuned = {
        'precision': metrics_non_fine_tuned[0],
        'recall': metrics_non_fine_tuned[1],
        'f1': metrics_non_fine_tuned[2],
        'accuracy': metrics_non_fine_tuned[3],
        'false_positive_rate': metrics_non_fine_tuned[4],
        'fpr': metrics_non_fine_tuned[5],
        'tpr': metrics_non_fine_tuned[6],
        'auc': metrics_non_fine_tuned[7]
    }
        
    print(f"Metrics for {model} (Fine-tuned): {metrics_dict_fine_tuned}")
    print(f"Metrics for {model} (Non Fine-tuned): {metrics_dict_non_fine_tuned}")
        
    # Plot performance metrics comparison
    plot_metrics(metrics_dict_fine_tuned, metrics_dict_non_fine_tuned, model)
    
    # Plot ROC curve comparison
    plot_roc_curve(metrics_dict_fine_tuned['fpr'], metrics_dict_fine_tuned['tpr'], metrics_dict_fine_tuned['auc'],
                   metrics_dict_non_fine_tuned['fpr'], metrics_dict_non_fine_tuned['tpr'], metrics_dict_non_fine_tuned['auc'],
                   model)    

if __name__ == "__main__":
    main()
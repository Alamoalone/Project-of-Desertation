# Project-of-Desertation

# Null Pointer Exception (NPE) Detection with Large Language Models (LLM)

## Project Overview
This project aims to detect null pointer exceptions (NPE) in code using various large language models (LLM). We used multiple models including LLAMA2, LLAMA3 (70B), CodeLlama, Phi2, and Gemma, and conducted experiments and fine-tuning on a set of Java code examples.

## Workflow

### Overall Plan

This project focuses on detecting Null Pointer Exceptions (NPEs) using Large Language Models (LLMs) through two main directions:

1. **Performance Comparison of Non-Finetuned LLMs:** Compare the performance of unfine-tuned LLMs (LLAMA2, LLAMA3, LLAMA3 (70B), CodeLlama, Phi2, Gemma) in NPE detection using various evaluation metrics.

2. **LoRA Fine-Tuning and Performance Comparison:** Select and fine-tune one LLM model using the Low Rank Adaptation (LoRA) technique, comparing its performance before and after fine-tuning.

### Data Collection

**Data Source:** GitHub is used to collect real-world Java code data for training and testing LLMs.

**Dataset Creation:** Three distinct test sets are created to evaluate model generalization across different repositories, ensuring robustness of test results.

## Experimental Methods

### Data Collection

GitHub serves as the primary source, providing diverse and representative datasets crucial for model training and evaluation.

### Data Selection

Supervised learning approach selects data where NPEs exist pre-repair and are resolved post-repair, ensuring effective model training.

### Datasets Creation

Data undergoes parsing and cleaning with javalang, ensuring only valid code fragments are used. The dataset is categorized into MethodDataset and FileDataset for method and file-level analysis.

### Obtaining Non-Finetuned LLM Results with the Ollama LLM Library

**Ollama LLM Library:** Utilized to access pre-trained LLM models (LLAMA2, LLAMA3, etc.) for performance evaluation without local deployment complexities.

### Phi2 Model Fine-Tuning on Google Colab using the Litgpt Library

**Litgpt Library:** Used on Google Colab for Phi2 model fine-tuning, employing LoRA technology to optimize model performance efficiently.


## Installation Guide
Follow these steps to set up your development environment:

1. Clone this repository:
   ```bash
   git clone https://github.com/alamoalone/Project-of-Desertation.git
   ```

2. Install dependencies:
   ```bash
   cd Project-of-Desertation
   pip install -r requirements.txt
   ```


## Contribution Guidelines
We welcome any form of contribution! Here are some ways you can contribute:

1. **Submit a Pull Request**:
   If you have improved the code or fixed a bug, please submit a Pull Request.

2. **Report an Issue**:
   If you find a bug or have a suggestion for improvement, please report an Issue.

3. **Use and Improve Provided Data**:
   We provide the data used in our experiments, and you are welcome to use it for further research and improvement. The data can be obtained from the provided sheet. Please follow the relevant guidelines for data usage.

## Contact
If you have any questions, please contact:
- Email: wangx33@tcd.ie
- GitHub: [alamoalone](https://github.com/alamoalone)

# Project-of-Desertation

# Null Pointer Exception (NPE) Detection with Large Language Models (LLM)

## Project Overview
This project aims to detect null pointer exceptions (NPE) in code using various large language models (LLM). We used multiple models including LLAMA2, LLAMA3 (70B), CodeLlama, Phi2, and Gemma, and conducted experiments and fine-tuning on a set of Java code examples.

## Experimental Setup
### Output Table
Our experimental output includes the following information:
- Commit hash for each commit
- Function name
- Whether there is an NPE before and after the URL
- Changes before and after the commit URL

### Experimental Steps
1. **Function Level Detection**:
   For each function changed in a commit, ask the LLM if it contains an NPE.
   
2. **Method Level Detection**:
   For each commit, give all changed methods to the LLM and ask if any contains an NPE.

3. **File Level Detection**:
   For each changed Java file, give the file to the LLM and ask if any method in the file could produce an NPE.

### Experimental Run
We conducted experiments on 1000 Java code examples and followed these steps:
- Used multiple LLM models (LLAMA2, LLAMA3, LLAMA3 (70B), CodeLlama, Phi2, Gemma).
- Fine-tuning: Used litgpt to fine-tune the Phi2 model at the method level. We used 500 examples for fine-tuning and then ran the remaining 500 examples.

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

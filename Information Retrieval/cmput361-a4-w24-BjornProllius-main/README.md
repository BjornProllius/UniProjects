

# Compilation/execution instructions

**NOTE**: TAs and instructors **do not** have superuser privileges on the CS lab machines. Provide instructions that a regular user can follow to run your programs. **Make sure to test your instructions.**

```
1: open a terminal window in the root folder of the directory.
2: To run the build_index program type: 'python3 ./code/build_index.py collection'
    For example:
            'python3 ./code/build_index.py CISI_simplified'
3: To run the query.py type: 'python3 code/query.py collection scheme normalization k keywords'
    For example:
            'python3 ./code/query.py CISI_simplified ltc l 10 science'
4: To run the evaluation type: 'python3 ./code/evaluation.py collection scheme normalization k n metric'
    For Example:
            'python3 ./code/evaluation.py CISI_simplified ltn l 5 10 mrr'
5: to run the mass_evaluation script type: 'python3 ./code/mass_evaluation.py'
    - this does not take any arguments
    - it is currently set up to run 384 evaluations using multiprocessing
    - it is highly recommended that you modify the paramaters before execution for a faster runtime
    - if you don't want to utilize multiprocessing, replace the current 'with open' segment with the commented out code at the bottom of the file.
    - this will create a 'output.txt' file containing the output from the evaluations.
...
```

# Students

|Student name| CCID |
|------------|------|
|Bjorn Prollius   |---      |


# Sources consulted

https://docs.python.org/3/, Lecture Slides, Assignment 1, Assignment 2, Assignment 3, Copilot
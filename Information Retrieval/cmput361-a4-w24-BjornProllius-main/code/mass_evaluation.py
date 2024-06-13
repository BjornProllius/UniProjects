import concurrent.futures
import subprocess
import sys

# Define the possible values for each parameter
collection = 'CISI_simplified'
schemes = ['nnc','ntc','ntn','lnn','ltn','ltc','lnc','nnn']
stem_or_lems = ['s', 'l']
ks = [1, 5, 10]
ns = [5,10, 25, 50]
metrics = ['mrr', 'map']


#Runs the evaluation script with the given parameters
def run_evaluation(scheme, stem_or_lem, k, n, metric):
    print(f"Running evaluation for parameters {collection} {scheme} {stem_or_lem} {k} {n} {metric}.")
    # Run the evaluation script with the current combination of parameters
    process = subprocess.run(['python3', './code/evaluation.py', collection, scheme, stem_or_lem, str(k), str(n), metric], capture_output=True, text=True)

    # Check if the process completed successfully
    if process.returncode == 0:
        return process.stdout
    else:
        print(f"Error: Evaluation script failed for parameters {collection} {scheme} {stem_or_lem} {k} {n} {metric}.", file=sys.stderr)
        return None


# Open the output file
with open('output.txt', 'w') as file:
    # Create a process pool
    with concurrent.futures.ProcessPoolExecutor() as executor:
        # Iterate over all possible combinations of parameters
        futures = []
        for scheme in schemes:
            for stem_or_lem in stem_or_lems:
                for k in ks:
                    for n in ns:
                        for metric in metrics:
                            # Submit the function to the process pool
                            futures.append(executor.submit(run_evaluation, scheme, stem_or_lem, k, n, metric))

        # Write the results to the file as they become available
        for future in concurrent.futures.as_completed(futures):
            result = future.result()
            if result is not None:
                file.write(result)



# slower version that does not use multiprocessing
#
# # Open the output file
# with open('output.txt', 'w') as file:
#     # Iterate over all possible combinations of parameters
#     for scheme in schemes:
#         for stem_or_lem in stem_or_lems:
#             for k in ks:
#                 for n in ns:
#                     for metric in metrics:
#                         # Run the evaluation
#                         result = run_evaluation(scheme, stem_or_lem, k, n, metric)
#                         # Write the result to the file
#                         if result is not None:
#                             file.write(result)
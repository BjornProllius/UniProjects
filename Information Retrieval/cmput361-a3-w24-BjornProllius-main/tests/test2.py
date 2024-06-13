import os
import sys
import subprocess


program = "./code/query.py"
build_program = "./code/build_index.py"
collection= "good"
k='10'

queries_file = './collections/' + collection + '.QRY'
rel_file = './collections/' + collection + '.REL'

scoring_schemes=['nnn','nnc','ntc','ntn','lnn','ltn','ltc','lnc','nnn','ntc']
queries={}
results={}
pass_test=True


print("Testing if query.py matches expected output on good collection")
print("expected to PASS")


#build proccessed file if it does not exist
if os.path.isfile('./processed/'+collection+'.index.pkl'):
    pass
else:
    subprocess.run(["python3", build_program, collection], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

# Read the queries
with open(queries_file) as file:
    current_key = None
    current_value = ''
    ignore_lines = False
    for line in file:
        line = line.strip()
        if line.startswith('.I'):
            if current_key is not None:
                queries[current_key] = current_value.strip()
            current_key = line.split(' ')[1]
            current_value = ''
            ignore_lines = False
        elif line.startswith('.X'):
            ignore_lines = True
        elif not line.startswith('.') and not ignore_lines:
            current_value += ' ' + line

    if current_key is not None:
        queries[current_key] = current_value.strip()

# Read the expected results
with open(rel_file) as file:
    for line in file:
        values = line.split()
        if values[0] in results:
            results[values[0]].append(values[1])
        else:
            results[values[0]] = [values[1]]


# Run the queries and compare the results
for (key, value), scheme in zip(queries.items(), scoring_schemes):
    result = subprocess.run(["python3", program, collection, scheme, k, value], capture_output=True, text=True)
    if not result.stdout.strip():
        print(f'No scores returned for query {key}')
        pass_test = False
    else:
        output_lines = result.stdout.split('\n')
        for line in output_lines:
            if line:  # ignore empty lines
                pairs = line.split('\t')
                for pair in pairs:
                    docID, score = pair.split(':')
                    if docID not in results[key]:
                        print(f'Mismatch found for query {key} and docID {docID}')
                        pass_test = False

if pass_test:
    print('PASS')
else:
    print('FAIL')




exit(0)
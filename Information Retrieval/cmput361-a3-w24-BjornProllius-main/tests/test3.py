import os
import sys
import subprocess

pass_test=True
build_program = "./code/build_index.py"
collection= "good"

print("Testing if query.py returns errors on bad queries")
print("expected to PASS")

#build proccessed file if it does not exist
if os.path.isfile('./processed/'+collection+'.index.pkl'):
    pass
else:
    subprocess.run(["python3", build_program, collection], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

bad_queries = ["python3 ./code/query.py good ltn 10 pineapple goober", #OOV term
               "python3 ./code/query.py ltn 10 canadian",              #missing collection
               "python3 ./code/query.py mystery ltn 10 police",        #nonexistent collection
               "python3 ./code/query.py good lzn 10 snake",            #invalid scoring scheme
               "python3 ./code/query.py good lltn 10 fridge"]          #invalid scoring scheme

# Run the queries and check for errors
for query in bad_queries:
    args = query.split()
    result = subprocess.run(args,capture_output=True, text=True)
    if result.returncode == 0:
        print(f"FAIL: Query '{query}' did not return an error code.")
        pass_test = False

if pass_test:
    print('PASS')
else:
    print('FAIL')

exit(0)
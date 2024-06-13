
import os
import sys
from preprocessing import tokenize
from preprocessing import normalize
import random
import subprocess


def find_relevant(rel_file, selected_queries):
    '''
    Read the REL file and extract the relevant documents for the selected queries.
    '''
    relevant_docs = {}
    with open(rel_file) as file:
        for line in file:
            values = line.split()
            if values[0] in selected_queries:
                # if the queryID is already in the dictionary, append the relevant document to the list
                if values[0] in relevant_docs:
                    relevant_docs[values[0]].append(values[1])

                # if the queryID is not in the dictionary, create a new list with the relevant document
                else:
                    relevant_docs[values[0]] = [values[1]]

    return relevant_docs


def read_queries(queries_file, n):
    '''
    Read the queries from the QRY file and return n random queries as a dictionary.
    '''
    queries={}
    with open(queries_file) as file:
        current_key = None
        current_value = ''
        ignore_lines = False
        for line in file:
            line = line.strip()
            #if the line starts with '.I', it is a new query
            if line.startswith('.I'):
                if current_key is not None:
                    queries[current_key] = current_value.strip()
                current_key = line.split(' ')[1]
                current_value = ''
                ignore_lines = False

            #if the line starts with '.X', ignore the lines until the next '.I'    
            elif line.startswith('.X'):
                ignore_lines = True

            #if the line does not start with '.', add it to the current value
            elif not line.startswith('.') and not ignore_lines:
                current_value += ' ' + line

        # Add the last query
        if current_key is not None:
            queries[current_key] = current_value.strip()

    # Return n random queries as a dictionary
    return {k: queries[k] for k in random.sample(list(queries.keys()), n)}

    
def compute_MRR(results, relevant_docs):
    '''
    Compute the Mean Reciprocal Rank of the results.
    '''
    reciprocal_ranks = []

    for query_id, output in results.items():
        # Parse the output to get the list of document IDs and scores
        doc_scores = output.split('\t')
        doc_ids = [doc_score.split(':')[0] for doc_score in doc_scores]

        # Find the rank of the first relevant document
        for i, doc_id in enumerate(doc_ids, start=1):
            if doc_id in relevant_docs.get(query_id, []):
                reciprocal_ranks.append(1 / i)
                break

    # Compute the mean of the reciprocal ranks
    if reciprocal_ranks:
        mrr = sum(reciprocal_ranks) / len(reciprocal_ranks)
    else:
        mrr = 0
    return mrr

def compute_MAP_at_k(results, relevant_docs, k):
    '''
    Compute the Mean Average Precision at k of the results.
    '''
    average_precisions = []

    for query_id, output in results.items():
        # Parse the output to get the list of document IDs and scores
        doc_scores = output.split('\t')
        doc_ids = [doc_score.split(':')[0] for doc_score in doc_scores]

        # Compute the average precision for the query
        relevant_docs_query = relevant_docs.get(query_id, [])
        precision = 0
        num_relevant = 0
        for i, doc_id in enumerate(doc_ids[:k], start=1):  # Only consider top k results
            if doc_id in relevant_docs_query:
                num_relevant += 1
                precision += num_relevant / i

        # Compute the average precision for the query
        if relevant_docs_query:
            average_precision = precision / min(len(relevant_docs_query), k)
        else:
            average_precision = 0

        # Append the average precision to the list    
        average_precisions.append(average_precision)

    # Compute the mean of the average precisions
    if average_precisions:
        map_at_k = sum(average_precisions) / len(average_precisions)
    else:
        map_at_k = 0

    return map_at_k

if __name__ == "__main__":
    '''
    "main()" function goes here
    '''
    # Check if enough arguments are provided
    if len(sys.argv) < 7:
        print("Error: Not enough arguments provided, minimum 4 expected: collection, scheme, k, keywords.", file=sys.stderr)
        sys.exit(1)

    #set the collection
    collection = sys.argv[1]

    #set the scheme
    scheme = sys.argv[2]

    # Check if the scheme is exactly 3 letters long
    if len(scheme) != 3:
        print("Error: Scheme should be exactly 3 letters long.", file=sys.stderr)
        sys.exit(1)

    #decompose the scheme into tf_scheme, df_scheme, normalization
    tf_scheme = scheme[0]
    df_scheme = scheme[1]
    normalization = scheme[2]

    #set the stem_or_lem
    stem_or_lem = sys.argv[3]

    #set the value of k
    k = int(sys.argv[4])


    # Check if the stemming collection exists 
    if stem_or_lem == 's':
        if not os.path.exists('./processed/'+collection+'.stemming.index.json'):
            print(f"Error: Collection '{collection}' does not exist.", file=sys.stderr)
            sys.exit(1)

    # Check if the lemmatization collection exists
    elif stem_or_lem == 'l':
        if not os.path.exists('./processed/'+collection+'.lemmatization.index.json'):
            print(f"Error: Collection '{collection}' does not exist.", file=sys.stderr)
            sys.exit(1)

    else:
        print("Error: Invalid scheme. Expected 's' or 'l'.", file=sys.stderr)
        sys.exit(1)
    
    #set the number of queries
    n = int(sys.argv[5])

    queries={}
    results = {}
    relevant_docs = {}

    queries = read_queries('./collections/'+collection+'.QRY', n)


    #run the random queries on the collection
    for query_id, query in queries.items():
        # Run the query program for each query       
        process = subprocess.run(['python3', './code/query.py', collection, scheme, stem_or_lem, str(k), query], capture_output=True, text=True)

        # Check if the process completed successfully
        if process.returncode != 0:
            print(f"Error: Query program failed for query {query_id}.", file=sys.stderr)
            continue
        # Parse the output of the query program
        output = process.stdout
        results[query_id] = output

    # Find the relevant documents for the queries
    relevant_docs = find_relevant('./collections/'+collection+'.REL', queries.keys())
   
    evaluation=0
    metric = sys.argv[6]

    # Compute the evaluation metric
    if metric == 'mrr':
        evaluation = compute_MRR(results, relevant_docs)
    
    elif metric == 'map':
        evaluation = compute_MAP_at_k(results, relevant_docs, k)
    else:
        print('Error: Invalid metric. Expected MRR or MAP@K.', file=sys.stderr)
        sys.exit(1)

    # Print the parameters and the evaluation metric in a single line
    print(f"{collection} {scheme} {stem_or_lem} {k} {n} {metric} {evaluation:.4f}")

    exit(0)
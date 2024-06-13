

'''

Reads all documents in the collection file into memory and writes
an inverted index to the processed folder.

The program will be run from the root of the repository.

'''

import sys
from preprocessing import tokenize
from preprocessing import normalize
import os
import json



def read_documents(collection):
    '''
    Reads the documents in the collection (inside the 'collections' folder).
    '''

    extension = '.ALL'
    assert type(collection) == str
    corpus_file = './collections/' + collection + '.ALL'

    documents = {}

    #taken from a1 and modified
    with open(corpus_file) as file:
        current_key = None
        current_value = ''
        ignore_lines = False
        for line in file:
            line = line.strip()
            if line.startswith('.I'):
                parts = line.split(' ')
                #check if the line is formatted correctly
                if len(parts) != 2 or not parts[1].isdigit():
                    print(f"Error: File '{corpus_file}' is not formatted correctly.", file=sys.stderr)
                    sys.exit(1)
                if current_key is not None:
                    documents[current_key] = current_value.strip()
                current_key = parts[1]
                current_value = ''
                ignore_lines = False
            elif line.startswith('.X'):
                ignore_lines = True
            elif not line.startswith('.') and not ignore_lines:
                current_value += ' ' + line

        # Add the last document
        if current_key is not None:
            documents[current_key] = current_value.strip()


    
    return documents

def build_index(documents, method):
    '''
    Builds inverted index.
    '''

    assert type(documents) == dict

    index = {}
    tokenized = {}
    postings = []
    
    # Tokenize and normalize the document with stemming or lemmatization
    for docID in documents:
        original_text = documents[docID]
        tokenized[docID] = normalize(tokenize(original_text), method)

    # Create postings list
    for docID, tokens in tokenized.items():
        for position, term in enumerate(tokens):
            postings.append((term, docID, position))

    # Sort the postings list
    postings = sorted(postings, key=lambda posting: (posting[0], posting[1], posting[2]))

    # Build the inverted index
    for term, docID, position in postings:
        if term in index:
            if docID == index[term][-1]['docID']:  # if the document ID is the same as the last node for term
                index[term][-1]['positions'].append(position)  
                index[term][-1]['term_frequency'] += 1  
            else:
                index[term].append({'docID': docID, 'term_frequency': 1, 'positions': [position]})  # create a new node with the term frequency and position
        else:
            index[term] = [{'docID': docID, 'term_frequency': 1, 'positions': [position]}]  # initialize the list with the document with term frequency and position

    return index

def write_index(collection, index, method):
    '''
    Writes the data structure to the processed folder
    '''

    assert type(index) == dict
    # Write the index to a file and name it based on method
    extension = f'.{method}.index.json' 
    index_file = './processed/' + collection + extension
    
    try:
        with open(index_file, 'w') as file:
            json.dump(index, file)

    #check if written successfully
    except Exception as e:
        print(f"Error: Failed to write index: {e}", file=sys.stderr)
        os.remove('./processed/'+collection+extension)
        sys.exit(1)

if __name__ == "__main__":
    '''
    main() function
    '''
    extension = '.ALL'
    documents = {}
    index = {}

    #check if the number of arguments is correct
    if len(sys.argv) != 2:
        raise Exception("ArgumentError", "Invalid number of arguments!")

    #check if the file already exists 
    collection = sys.argv[1]
    if os.path.isfile('./processed/'+collection+'.stemming.index.json') or os.path.isfile('./processed/'+collection+'.lemmatization.index.json'):
        print(f"Error: Index files for {collection} already exist.", file=sys.stderr)
        sys.exit(1)

    #check if collection exists
    try:
        open('./collections/' + collection + extension)
    except FileNotFoundError:
        print(f"Error: ./collections/{collection}.all not found.", file=sys.stderr)
        sys.exit(1)
    else:
        documents = read_documents(collection)
        #build index for stemming
        index = build_index(documents, 'stemming')
        write_index(collection, index, 'stemming')
        #build index for lemmatization
        index = build_index(documents, 'lemmatization')
        write_index(collection, index, 'lemmatization')
        print('SUCCESS')

    exit(0)
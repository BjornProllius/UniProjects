

'''

Reads all documents in the collection file into memory and writes
an inverted index to the processed folder.

The program will be run from the root of the repository.

'''

import sys
from preprocessing import tokenize
from preprocessing import normalize
import os
import pickle




class Node:
    def __init__(self, docID=None, term_frequency=None, positions=None, next=None):
        self.docID = docID
        self.term_frequency = term_frequency
        self.positions = positions
        self.next = next

    def __str__(self):
        return f'({self.docID}, {self.term_frequency}, {self.positions})'

class LinkedList:
    def __init__(self, head=None):
        self.head = head
        self.tail = head
        self.df = 1 if head else 0

    def __str__(self):
        node = self.head
        nodes = []
        while node is not None:
            nodes.append(str(node))
            node = node.next
        return f'{self.df},[' + ', '.join(nodes) + ']'

#===============================================================================
    
# example of how the index can be printed  

# for term, postings in index.items():
#     print(f'{term}: {postings}')

#===============================================================================


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


    print(f'{len(documents)} documents read in total')
    return documents

def build_index(documents):
    '''
    Builds inverted index.
    '''

    assert type(documents) == dict

    index = {}
    tokenized = {}
    postings = []
    
    for docID in documents:
        original_text = documents[docID]
        tokenized[docID] = normalize(tokenize(original_text))

    # Create postings list
    for docID, tokens in tokenized.items():
        for position, term in enumerate(tokens):
            postings.append((term, docID, position))

    # Sort the postings list
    postings = sorted(postings, key=lambda posting: (posting[0], posting[1], posting[2]))

    # Build the inverted index
    for term, docID, position in postings:
        if term in index:
            if docID == index[term].tail.docID:  # if the document ID is the same as the last node for term
                index[term].tail.positions.append(position)  
                index[term].tail.term_frequency += 1  
            else:
                index[term].df += 1  # increment the document frequency if the document ID is different from
                new_node = Node(docID, 1, [position])  # create a new node with the term frequency and position
                index[term].tail.next = new_node  
                index[term].tail = new_node  
        else:
            index[term] = LinkedList(Node(docID, 1, [position]))  # initialize the linked list with the document with term frequency and position

    return index


def write_index(collection, index):
    '''
    Writes the data structure to the processed folder
    '''

    assert type(index) == dict
    extension = '.index.pkl' 
    index_file = './processed/' + collection + extension
    

    try:
        with open(index_file, 'wb') as file:
            pickle.dump(index, file)

    #check if pickled successfully
    except Exception as e:
        print(f"Error: Failed to pickle index: {e}", file=sys.stderr)
        os.remove('./processed/'+collection+'.index.pkl')
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
    if os.path.isfile('./processed/'+collection+'.index.pkl'):
        print(f"Error: ./processed/{collection}.index.pkl already exists.", file=sys.stderr)
        sys.exit(1)

    #check if collection exists
    try:
        open('./collections/' + collection + extension)
    except FileNotFoundError:
        print(f"Error: ./collections/{collection}.all not found.", file=sys.stderr)
        sys.exit(1)
    else:
        documents =read_documents(collection)
        index = build_index(documents)
        write_index(collection, index)
        print('SUCCESS')


    exit(0)
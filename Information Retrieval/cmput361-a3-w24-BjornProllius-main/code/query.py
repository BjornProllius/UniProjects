'''

Reads in a collection name, a scoring scheme, k, and a keyword query 
and prints to STDOUT the IDs of the k documents in the collection 
with highest score, sorted by decreasing score

The program will be run from the root of the repository.


'''


import os
import sys
from preprocessing import tokenize
from preprocessing import normalize
import pickle
import heapq
import math

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


document_vectors = {} # dictionary to keep track of document vectors


def read_index(collection):
    '''
    Reads an inverted index (inside the 'processed' folder).
    '''
    global document_vectors
    extention = '.index.pkl'
    index_file = './processed/' + collection + extention

    #load the index from file
    with open(index_file, 'rb') as file:
        index = pickle.load(file)
    
    # Build document_vectors dictionary
    for term, linked_list in index.items():
        node = linked_list.head
        while node is not None:
            docID = node.docID
            term_frequency = node.term_frequency
            if docID in document_vectors:
                document_vectors[docID][term] = term_frequency
            else:
                document_vectors[docID] = {term: term_frequency}
            node = node.next

    return index


def build_query_vector(keyword_query):
    '''
    Takes a query, tokenizes and normalizes it, builds a query vector
    using the 'nnn' weighting scheme.
    '''

    terms = normalize(tokenize(keyword_query))
    query_vector = {}

    # Build query vector
    for term in terms:
        if term in index:
            if term in query_vector:
                query_vector[term] += 1
            else:
                query_vector[term] = 1
        else:
            #check if term is in vocabulary
            print(f"Term '{term}' OOV.", file=sys.stderr)
            sys.exit(1)

    return query_vector

def tokenize_and_answer(keyword_query, tf_scheme, df_scheme, normalization, k):
    '''
    Takes a query, tokenizes and normalizes it, builds a query vector, 
    and scores the documents using the dot product algorithm discussed in class,
    returns the k highest ranked documents in order.
    '''
    assert type(keyword_query) == str

    query_vector = build_query_vector(keyword_query)
    min_heap = []
    scores = {} # dictionary to keep track of scores
    document_lengths = {} # dictionary to keep track of document lengths


    for term in query_vector:
        linked_list = index.get(term)
        if linked_list is not None:
            document_frequency = linked_list.df

            #handle df_scheme
            if df_scheme == 'n':
                document_frequency = 1
            elif df_scheme == 't':
                document_frequency = math.log(len(index)/document_frequency)
            else:
                #invalid df scheme
                print("Invalid df_scheme", file=sys.stderr)
                sys.exit(1)

            node = linked_list.head
            while node is not None:
                docID = node.docID
                term_frequency = node.term_frequency

                #handle tf_scheme
                if tf_scheme == 'n':
                    term_frequency = term_frequency
                elif tf_scheme == 'l':
                    term_frequency = 1 + math.log(term_frequency)
                else:
                    #invalid tf scheme
                    print("Invalid tf_scheme", file=sys.stderr)
                    sys.exit(1)

                weight = term_frequency * document_frequency
                scores[docID] = scores.get(docID, 0) + weight * query_vector[term]

                node = node.next

    # Calculate document lengths
    for docID, doc_vector in document_vectors.items():
        document_lengths[docID] = math.sqrt(sum(value**2 for value in doc_vector.values()))

    # handle score normalization
    if normalization == 'c':
        for docID, score in scores.items():
            scores[docID] = score / document_lengths[docID]
    elif normalization == 'n':
        pass
    else:
        #invalid normalization
        print("Invalid normalization", file=sys.stderr)
        sys.exit(1)

    # Push items onto heap
    for docID, score in scores.items():
        heapq.heappush(min_heap, (-score, docID))

    # Pop items off of heap until only top k remain
    while len(min_heap) > k:
        heapq.heappop(min_heap)

    # Convert heap to a list of tuples and return
    answer = [(-score, docID) for score, docID in min_heap]
    answer.sort(key=lambda x: (-x[0], x[1]))
    return answer

index = {} 


if __name__ == "__main__":
    '''
    "main()" function goes here
    '''
    # Check if enough arguments are provided
    if len(sys.argv) < 5:
        print("Error: Not enough arguments provided, minimum 4 expected: collection, scheme, k, keywords.", file=sys.stderr)
        sys.exit(1)

    # Check if the collection exists
    collection = sys.argv[1]
    if not os.path.exists('./processed/'+collection+'.index.pkl'):
        print(f"Error: Collection '{collection}' does not exist.", file=sys.stderr)
        sys.exit(1)

    scheme = sys.argv[2]
    # Check if the scheme is exactly 3 letters long
    if len(scheme) != 3:
        print("Error: Scheme should be exactly 3 letters long.", file=sys.stderr)
        sys.exit(1)

    tf_scheme = scheme[0]
    df_scheme = scheme[1]
    normalization = scheme[2]

    k = int(sys.argv[3])


    keywords = ' '.join(sys.argv[4:])
    index = read_index(collection)

    answer = tokenize_and_answer(keywords, tf_scheme,df_scheme,normalization, k)
    
    print('\t'.join(f'{docID}:{round(score, 3)}' for score, docID in answer))
    exit(0)
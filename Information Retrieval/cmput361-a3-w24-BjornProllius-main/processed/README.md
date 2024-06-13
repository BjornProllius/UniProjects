# Important

# Representation of inverted index


The inverted index is a dictionary where each key is a term from the corpus and each value is a linked list object.
The linked list has a head and tail along with a document frequency for its term.

The index is stored by using the pickle module because the postings are a linked list.

Each node in the linked list represents a document that contains the term and has the following information:
    -the documentID
    -the frequency of the term in the document
    -the positions of the term in the document
    -a pointer to the next node

an example of how this index looks in human-readable form is:
    
    index=  ["alberta: 1,[(2, 1, [4])]",  
             "are: 2,[(3, 1, [1]), (4, 1, [10])]",  
             "around: 1,[(4, 1, [18])]"]

an example of how to print the index can be found in a block comment within `query.py`, `build_index.py`, and `test1.py`

The index resides in memory while `query.py` runs.


# Naming convention 

- You can use as many files as you want to represent the index.
- Each file should have the same name (prior to extension) as the corresponding test collection. 
- The extensions of those files cannot be the same as in the `./collections/` folder, to avoid confusion/errors.

import os
import sys
import subprocess
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




index_hardcode = [
    "alberta: 1,[(2, 1, [4])]",
    "are: 2,[(3, 1, [1]), (4, 1, [10])]",
    "around: 1,[(4, 1, [18])]",
    "at: 1,[(3, 1, [8])]",
    "balloon: 2,[(1, 1, [1]), (3, 1, [17])]",
    "becaus: 1,[(2, 1, [12])]",
    "been: 1,[(1, 1, [3])]",
    "big: 1,[(4, 1, [12])]",
    "buy: 1,[(5, 1, [6])]",
    "canadian: 5,[(1, 1, [11]), (2, 1, [6]), (3, 1, [16]), (4, 1, [5]), (5, 1, [15])]",
    "cheap: 1,[(5, 1, [14])]",
    "citi: 2,[(1, 1, [12]), (5, 1, [3])]",
    "club: 1,[(5, 1, [1])]",
    "coffe: 2,[(1, 1, [8]), (5, 2, [0, 8])]",
    "commiss: 1,[(2, 1, [11])]",
    "doe: 2,[(3, 1, [5]), (4, 1, [1])]",
    "fanci: 1,[(5, 1, [7])]",
    "for: 2,[(2, 1, [1]), (3, 1, [3])]",
    "fridg: 1,[(4, 1, [6])]",
    "from: 1,[(1, 1, [5])]",
    "hall: 1,[(2, 1, [7])]",
    "have: 1,[(1, 1, [2])]",
    "he: 1,[(3, 1, [11])]",
    "heat: 1,[(4, 2, [13, 17])]",
    "hi: 1,[(3, 1, [15])]",
    "in: 1,[(1, 1, [10])]",
    "is: 2,[(2, 1, [8]), (4, 1, [7])]",
    "jane: 1,[(4, 1, [0])]",
    "john: 1,[(3, 1, [4])]",
    "just: 2,[(4, 2, [11, 15]), (5, 1, [9])]",
    "last: 1,[(3, 1, [6])]",
    "less: 1,[(5, 1, [17])]",
    "like: 1,[(5, 1, [12])]",
    "local: 1,[(1, 1, [7])]",
    "look: 1,[(3, 1, [2])]",
    "make: 1,[(5, 1, [10])]",
    "move: 1,[(4, 1, [16])]",
    "need: 1,[(3, 1, [12])]",
    "notic: 1,[(2, 1, [0])]",
    "of: 2,[(2, 3, [3, 10, 13]), (5, 1, [2])]",
    "out: 2,[(2, 1, [9]), (5, 1, [5])]",
    "pay: 1,[(3, 1, [14])]",
    "permit: 1,[(3, 1, [18])]",
    "polic: 1,[(3, 1, [0])]",
    "problem: 1,[(4, 1, [3])]",
    "pump: 1,[(4, 1, [14])]",
    "seen: 1,[(3, 1, [7])]",
    "seventeen: 1,[(1, 1, [0])]",
    "shop: 1,[(1, 1, [9])]",
    "snake: 1,[(2, 1, [14])]",
    "stolen: 1,[(1, 1, [4])]",
    "stuff: 1,[(5, 1, [16])]",
    "that: 1,[(4, 1, [8])]",
    "the: 5,[(1, 1, [6]), (2, 1, [5]), (3, 1, [9]), (4, 1, [2]), (5, 1, [13])]",
    "they: 1,[(4, 1, [9])]",
    "to: 1,[(3, 1, [13])]",
    "turn: 1,[(5, 1, [4])]",
    "univers: 2,[(2, 1, [2]), (3, 1, [10])]",
    "with: 1,[(4, 1, [4])]",
    "you: 1,[(5, 1, [11])]"
]



program = "./code/build_index.py"
collection= "good"
extention = '.index.pkl'
index_file = './processed/' + collection + extention
pass_test = True

print("Testing if build_index.py matches expected output on good collection")
print("expected to PASS")


if os.path.isfile('./processed/'+collection+'.index.pkl'): #delete the proccessed file if it already exists
    os.remove('./processed/'+collection+'.index.pkl')

subprocess.run(["python3", program, collection], stdout=subprocess.DEVNULL, stderr=subprocess.STDOUT)

#unpickle index
with open(index_file, 'rb') as file:
    index = pickle.load(file)


#compare index to hardcoded index
for i, (term, postings) in enumerate(index.items()):
    line = f'{term}: {postings}'
    if line != index_hardcode[i]:
        pass_test = False

if pass_test:
    print("PASS")
else:
    print("FAIL")
    
exit(0)


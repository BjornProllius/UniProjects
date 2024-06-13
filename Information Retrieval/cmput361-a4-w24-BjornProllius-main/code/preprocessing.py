'''

This file contains the preprocessing that must be used
for this assignment. 

'''

import nltk
nltk.download('wordnet', quiet=True)
from nltk.tokenize import word_tokenize
from nltk.stem import PorterStemmer
from nltk.stem import WordNetLemmatizer
from nltk.corpus import wordnet
nltk.download('punkt', quiet=True)
stemmer = PorterStemmer()
lemmatizer = WordNetLemmatizer()

import string

def tokenize(text):
    '''
    Tokenizes text in a document or query. 
    Removes punctuation and returns a list of tokens.
    '''
    assert type(text) == str

    # remove punctuation
    new_text = text.translate(str.maketrans('', '', string.punctuation))
    tokens = word_tokenize(new_text)

    return tokens

def normalize(tokens, method='stemming'):
    '''
    Normalize a list of tokens by lowercasing and applying 
    stemming or lemmatization.
    '''
    assert type(tokens) == list

    l_cased = [token.lower() for token in tokens]

    # Apply stemming
    if method == 'stemming':
        normalized = [stemmer.stem(token) for token in l_cased]

    # Apply lemmatization
    elif method == 'lemmatization':
        normalized = [lemmatizer.lemmatize(token, pos=wordnet.NOUN) for token in l_cased]
    
    # Invalid method
    else:
        raise ValueError("Invalid method. Expected 'stemming' or 'lemmatization'.")

    return normalized

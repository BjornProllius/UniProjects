/*
Bjorn Prollius
1646013
CMPUT 325 Lec B1
Assignment 3
*/

:- use_module(library(lists)).



/* ---------------------------------------------------------

Question 1

setDifference(+S1,+S2,-S3)
Where:
S1 and S2 are lists of atoms,
S3 is a list of atoms that are in S1 but not in S2.

test cases:

setDifference([a,b,c,d,e,g],[b,c,e,f,q],S).
S=[a,d,g].

--------------------------------------------------------- */

%returns a list of atoms that are in S1 but not in S2
%base case
setDifference([], _, []).

%if head of list is in S2, then skip it
setDifference([H|T1], S2, S3) :-
    member(H, S2), !,
    setDifference(T1, S2, S3).

%if head of list is not in S2, then add it to the result
setDifference([H|T1], S2, [H|S3]) :-
    setDifference(T1, S2, S3).



/* ---------------------------------------------------------

Question 2

swap(+L,-R)
Where:
L is a list of atoms,
R is a list of atoms that is the same as L, except that every pair of consecutive elements in L is swapped in R.

test cases:

swap([a,1,b,2], W).
W = [1,a,2,b].

swap([a,1,b], W).
W = [1,a,b]

--------------------------------------------------------- */

%swaps consecutive elements in a list
%base case
swap([], []).

%if there is only one element in the list, then return the list
swap([X], [X]).

%if there are two elements in the list, then swap them and recurse
swap([X, Y|T], [Y, X|R]) :-
    swap(T, R).

/* ---------------------------------------------------------

Question 3

filter(+L,+OP,+N,-L1)
Where:
L is a list of atoms and numbers,
OP is one of the words 'equal', 'greaterThan', or 'lessThan', 
N is a number,
L1 is a list of atoms and numbers that is the same as L, except that all elements that do not satisfy the condition specified by OP and N are removed.

test cases:

?- filter([3,4,[5,2],[1,7,3]],greaterThan,3,W).
W= [4,5,7]

?- filter([3,4,[5,2],[1,7,3]],equal,3,W).
W= [3,3]

--------------------------------------------------------- */


%flatten the list then filter the xflattened list
filter(L, OP, N, L1) :-
    xflatten(L, LFlat),
    filterFlat(LFlat, OP, N, L1).

%base case
filterFlat([], _, _, []).

%if head of list satisfies the condition, then add it to the result
filterFlat([H|T], OP, N, [H|T1]) :-
    compareValue(OP, H, N), !,
    filterFlat(T, OP, N, T1).

%if head of list does not satisfy the condition, then skip it
filterFlat([_|T], OP, N, T1) :-
    filterFlat(T, OP, N, T1).


compareValue('equal', N, N). 
compareValue('greaterThan', H, N) :- H > N.
compareValue('lessThan', H, N) :- H < N.



%xflatten is taken from eclass and flattens a list.
xflatten([],[]).
xflatten([A|L],[A|L1]) :-
     xatom(A), !, xflatten(L,L1).
xflatten([A|L],R) :-
     xflatten(A,A1), xflatten(L,L1), append(A1,L1,R).

xatom(A) :- atom(A).
xatom(A) :- number(A).



/* ---------------------------------------------------------

Question 4

countAll(+L,-N)
Where:
L is a list of atoms,
N is a list of pairs where each list is of the form [a, n], where a is an atom that occurs in L, and n is the number of times that a occurs in L.

The list N is sorted in ascending order of the atoms and numbers.


test cases:

countAll([a,b,e,c,c,b],N).
N = [[a,1],[e,1],[b,2],[c 2]] 

--------------------------------------------------------- */

countAll(L, Sorted) :-
    %sort the input list
    sort(L, L1),
    %find all the atoms in the list and count them, collecting the pairs into R
    findall([A, N], (member(A, L1), count(L, A, N)), R),
    %sort the list of pairs
    merge_sort(R, Sorted).

%base case
count([], _, 0).

%if head of list is the thing we are counting, then add 1 to the count
count([H|T], H, N) :-
    count(T, H, N1),
    N is N1 + 1.

%if head of list is not the thing we are counting, then skip it
count([H|T], X, N) :-
    X \= H,
    count(T, X, N).


%mergesort taken from http://kti.ms.mff.cuni.cz/~bartak/prolog/sorting.html
%divide and merge have both been slighly modified to work with lists of pairs

merge_sort([],[]).
merge_sort([A],[A]).
merge_sort(List,Sorted):-
    List=[_,_|_],divide(List,L1,L2),
    merge_sort(L1,Sorted1),
    merge_sort(L2,Sorted2),
    merge(Sorted1,Sorted2,Sorted).

divide([],[],[]).
divide([A],[A],[]).
divide([A,B|T],[A|Ta],[B|Tb]):-divide(T,Ta,Tb).

merge([],B,B).
merge(A,[],A).
merge([[A1,A]|T1],[[B1,B]|T2],[[A1,A]|T]):-A=<B,merge(T1,[[B1,B]|T2],T).
merge([[A1,A]|T1],[[B1,B]|T2],[[B1,B]|T]):-A>B,merge([[A1,A]|T1],T2,T).



/* ---------------------------------------------------------

Question 5

sub(+L,+S,-L1)
Where:
L is a possibly nested list of atoms, 
S is a list of pairs, [[x1,e1],...,[xn,en]]
L1 is the same as L except that any occurrence of xi is replaced by ei



test cases:

sub([a,[a,d],[e,a]],[[a,2]],L).
L= [2,[2,d],[e,2]].

--------------------------------------------------------- */


%base case
sub([], _, []).

%if head of list is an atom and is in S, then replace it and sub the tail
sub([H|T], S, [E|NewTail]) :-
    atom(H),
    member([H, E], S), !,
    sub(T, S, NewTail).

%if head of list is an atom and is not in S, then skip it
sub([H|T], S, [H|NewTail]) :-
    atom(H),
    \+ member([H, _], S),
    sub(T, S, NewTail).

%if head of list is a list, then call sub on both the head and the tail of the list
sub([H|T], S, [NewHead|NewTail]) :-
    is_list(H),
    sub(H, S, NewHead),
    sub(T, S, NewTail).



/* ---------------------------------------------------------

Question 6

clique(?L)
Where:
L is a list of atoms as input, or L is a list of cliques as output.

Clique works by first finding all the subsets of nodes, 
and then checking if each subset is a clique.

Helper function subsetx is used to generate all subsets of a list.

test cases:

? clique(L).
L = [a, b, c]; L = [a, c], ... L = []

?- clique([a, b]).
true ;


?- clique([f]).
false.

--------------------------------------------------------- */


%if L is a variable, then find all subsets of nodes and check if each subset is a clique
clique(L) :-
    var(L), !,
    find_subsets_of_nodes(Subsets),
    member(L, Subsets),
    connected_subset(L).

%if L is a list, then check if L is a clique
clique(L) :-
    is_list(L),
    find_subsets_of_nodes(Subsets),
    member(L, Subsets),
    connected_subset(L).


%finds all nodes
find_nodes(Nodes) :-
    findall(Node, node(Node), Nodes).


%base case
subsetx([], []).

%include the head of the list in the subset, then call subset on the tail of the list
subsetx([H|Tail], [H|NewTail]):-
    subsetx(Tail, NewTail).

%skip the head of the list, then call subset on the tail of the list
subsetx([_|Tail], NewTail):-
    subsetx(Tail, NewTail).


%finds all subsets of a list and collects them
find_subsets(List, Subsets) :-
    findall(Subset, subsetx(List, Subset), Subsets).


%finds all subsets of nodes using find_nodes and find_subsets
find_subsets_of_nodes(Subsets) :-
    find_nodes(Nodes),
    find_subsets(Nodes, Subsets).


%checks if a subset is a clique
connected_subset(Subset) :-
    forall(
        %first argument of forall finds all pairs of distinct nodes in the subset
        (member(Node, Subset), member(OtherNode, Subset), Node \= OtherNode),
        %second argument of forall checks if all pairs of distinct nodes in the subset are connected
        (edge(Node, OtherNode) ; edge(OtherNode, Node))). 




/* ---------------------------------------------------------

Question 7

convert(+Term,-Result)
Where:
Term is a possibly empty list of atoms,
Result is the same as Term except that:
    every occurrence of the atom e is removed,
    every other atom is replaced by w,
    and all atoms between q's are left the same.

convert works by finding the first occurence of q in the list and modifying the atoms up until that point.
If it finds another q, then it keeps the atoms between the two q's the same and recursively calls convert on the rest of the list.
If it does not find another q, then it removes all the e's from the rest of the list and replaces all the atoms that are not q with w.

test cases:

?- convert([e,e,a,e,b,e],R)
R = [w,w] ?;


?- convert([e,q,a,e,b,q,e,a,e],R).
R = [q,a,e,b,q,w]? ;


?- convert([q,e,q,b,q,e,l,q,a,e],R).
R = [q,e,q,w,q,e,l,q,w] ? ;


--------------------------------------------------------- */


%case in which there are at least 2 q's
convert(Term, Result) :-
    %split term into Preq and Postq at first q
    append(Preq, [q|Postq], Term),
    %split Postq into Mid and Rest at the next q
    append(Mid, [q|Rest], Postq), !, %this fails if there is no second q and prolog backtracks
    % delete e from Preq (the part of the list before the first q)
    delete(Preq, e, Preq2),
    %replace all atoms that are not q with W in preq after e has been removed
    replace(Preq2, PreqResult),
    %combine the modified preq with the middle part (between the q's) in tempresult
    append([PreqResult, [q|Mid], [q]], TempResult),
    %recursively call convert on the rest of the list after double q
    convert(Rest, RestResult),
    %combine the tempresult with the restresult
    append(TempResult, RestResult, Result).

%case in which there is less than 2 q's
convert(Term, Result) :-
    %delete all e's from the list
    delete(Term, e, NewTerm),
    %replace all atoms that are not q with W
    replace(NewTerm, Result).


%base case
replace([], []).
%if head of list is q, then keep it and recursively call replace on the tail
replace([q|T], [q|Result]) :- replace(T, Result), !.
%if head of list is not q, then replace it with w and recursively call replace on the tail
replace([_|T], [w|Result]) :- replace(T, Result).


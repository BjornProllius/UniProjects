# Prollius Lab 4 Report

## Introduction
The document scoring scheme is in the form 'nnn' where each character specifies the weighting scheme used for computing the answer to a query.

The first character corresponds to term frequency and can either be 'n' for raw term frequency, or 'l' for logarithmic smoothing of the term frequency.

The second character corresponds to document frequency and can either be 'n' for raw document frequency or 't' for logarithmic smoothing of the document frequency.

The third character corresponds to normalization and can either be 'n' for no normalization or 'c' for cosine normalization. Cosine normalization helps to reduce bias towards document length when ranking.

'l' or 's' in the field following the scheme denotes either 'lemmatization' or 'stemming' for the document tokens.

The last two numbers before the metric, k and n, specify the number of documents returned per query and the number of randomly selected queries respectively.

## Metrics

The two metrics used were Mean Reciprocal Rank (MRR) and Mean Average Precision (MAP).

MRR is a metric that looks at the rank of the first relevant document for a query and then averages these rankings
over all queries.

MAP is a metric that measures the average precision for the top k documents returned for a query, and then averages
these prescisions over all queries to determine if relevant documents are ranked higher in the query results.

## Methodology

To try and find the best configuration, I made a program 'mass_evaluation.py' that tests every combination of scoring
scheme, normalization, MRR, and MAP on 3 values for k along with 4 values for n. These results are printed
to output.txt and from there I moved them into an Excel sheet in order to evaluate the data. I have included in my
results some tables showing the highest MRR and MAP values along with some averages I calculated from the overall
dataset.


## Results

Since k=1 always gave 0 for MRR and MAP, it was excluded when calculating averages.

**K Averages:**
| k  | MRR         |         MAP |
| -- | ----------- | ----------- |
| 1  | 0.0000      | 0.0000      |
| 5  | 0.4277      | 0.0105      |
| 10 | 0.2706      | 0.0059      |




**Scheme Averages**
| Scheme| MRR    | MAP    |
| ----- | ------ | ------ |
| lnc   | 0.2571 | 0.0047 |
| lnn   | 0.3551 | 0.0076 |
| ltc   | 0.3096 | 0.0033 |
| ltn   | 0.4324 | 0.0091 |
| nnc   | 0.5054 | 0.0078 |
| nnn   | 0.3949 | 0.0092 |
| ntc   | 0.2764 | 0.0062 |
| ntn   | 0.2613 | 0.0178 |




**Lemma vs Stem**
| Metric     | l      | s      |
| ---------- | ------ | ------ |
| Average MRR| 0.3461 | 0.3522 |
| Average MAP| 0.0088 | 0.0076 |




**Top Ten by MRR**
| Dataset           | Scheme| l/s | k   | n   | Metric | Value |
| ----------------- | ----- | --- | --- | --- | ------ | ----- |
| CISI_simplified   | ltn   | l   | 5   | 5   | mrr    | 1     |
| CISI_simplified   | nnc   | l   | 10  | 5   | mrr    | 1     |
| CISI_simplified   | nnc   | l   | 5   | 10  | mrr    | 1     |
| CISI_simplified   | ltn   | l   | 5   | 10  | mrr    | 1     |
| CISI_simplified   | nnc   | l   | 10  | 10  | mrr    | 1     |
| CISI_simplified   | ntc   | s   | 5   | 10  | mrr    | 1     |
| CISI_simplified   | nnc   | s   | 5   | 10  | mrr    | 1     |
| CISI_simplified   | lnc   | s   | 5   | 25  | mrr    | 1     |
| CISI_simplified   | lnn   | s   | 5   | 25  | mrr    | 1     |
| CISI_simplified   | ntc   | l   | 5   | 50  | mrr    | 1     |





**Top Ten by MAP**
| Dataset           | Scheme| l/s | k   | n  | Metric | Value |
| ----------------- | ----- | --- | --- | --- | ------ | ----- |
| CISI_simplified   | ntn   | s   | 5   | 5   | map    | 0.06  |
| CISI_simplified   | ltn   | l   | 5   | 5   | map    | 0.05  |
| CISI_simplified   | ntn   | s   | 5   | 10  | map    | 0.04  |
| CISI_simplified   | ntn   | l   | 5   | 5   | map    | 0.04  |
| CISI_simplified   | ntn   | l   | 10  | 5   | map    | 0.038 |
| CISI_simplified   | ltn   | l   | 5   | 10  | map    | 0.0367|
| CISI_simplified   | nnn   | s   | 5   | 5   | map    | 0.0267|
| CISI_simplified   | nnn   | s   | 10  | 5   | map    | 0.0257|
| CISI_simplified   | nnc   | l   | 5   | 10  | map    | 0.0233|
| CISI_simplified   | nnc   | s   | 5   | 10  | map    | 0.02  |




**Top Five Lemma by MRR**
| Dataset           | scheme| l/s | k   | n  | Metric | Value  |
| ----------------- | ----- | --- | --- | --- | ------ | ------ |
| CISI_simplified   | ntc   | l   | 5   | 50  | mrr    | 1      |
| CISI_simplified   | nnc   | l   | 5   | 10  | mrr    | 1      |
| CISI_simplified   | nnc   | l   | 10  | 10  | mrr    | 1      |
| CISI_simplified   | ltn   | l   | 5   | 10  | mrr    | 1      |
| CISI_simplified   | nnc   | l   | 10  | 5   | mrr    | 1      |




**Top Five Lemma by MAP**
| Dataset           | Scheme| l/s | k   | n   | Metric | Value |
| ----------------- | ----- | --- | --- | --- | ------ | ----- |
| CISI_simplified   | ltn   | l   | 5   | 5   | map    | 0.05  |
| CISI_simplified   | ntn   | l   | 5   | 5   | map    | 0.04  |
| CISI_simplified   | ntn   | l   | 10  | 5   | map    | 0.038 |
| CISI_simplified   | ltn   | l   | 5   | 10  | map    | 0.0367|
| CISI_simplified   | nnc   | l   | 5   | 10  | map    | 0.0233|





**Top Five Stem by MRR**
| Dataset           | Scheme| l/s | k   | n   | Metric | Value  |
| ----------------- | ----- | --- | --- | --- | ------ | ------ |
| CISI_simplified   | ntc   | s   | 5   | 10  | mrr    | 1      |
| CISI_simplified   | nnc   | s   | 5   | 10  | mrr    | 1      |
| CISI_simplified   | lnc   | s   | 5   | 25  | mrr    | 1      |
| CISI_simplified   | lnn   | s   | 5   | 25  | mrr    | 1      |
| CISI_simplified   | nnc   | s   | 5   | 25  | mrr    | 0.7778 |




**Top Five Stem by MAP**
| Dataset           | Scheme| l/s | k   | n   | Metric | Value  |
| ----------------- | ----- | --- | --- | --- | ------ | ------ |
| CISI_simplified   | ntn   | s   | 5   | 5   | map    | 0.06   |
| CISI_simplified   | ntn   | s   | 5   | 10  | map    | 0.04   |
| CISI_simplified   | nnn   | s   | 5   | 5   | map    | 0.0267 |
| CISI_simplified   | nnn   | s   | 10  | 5   | map    | 0.0257 |
| CISI_simplified   | nnc   | s   | 5   | 10  | map    | 0.02   |




## Conclusion

From this experiment, it seems that both stemming and lemmatization yield similar values for both MRR and MAP.
This can be seen in rows 1 and 2 of 'Lemma vs Stem', and in the fact that the top ten results by MRR and MAP 
have a roughly equal number of 'l' and 's' flags.

Additionally, it seems that the evaluation metric performs the best with k=5. This is evidenced by the data in 'K Averages',
and the high amount of k=5 in the top scoring evaluations.

For MRR, the best scoring scheme seems to be 'nnc'. It has the highest MRR average as calculated in row 5 of the
'Scheme Averages' table, and it appears 4 times in the top 10 documents by MRR. One noteworthy trend in the data
is that the evaluations with a high MRR tend to also use cosine normalization, with 7 of the 'Top Ten by MRR'
utilizing cosine normalization.

For MAP, the best scoring scheme seems to be 'ntn'. It has the highest MAP average as calculated in row 8 of the
'Scheme Averages' table, and it appears in 4 of the top 10 documents by MAP. An interesting thing to note is that
cosine normalization tends to lower the overall MAP score; of the 'Top Ten by MAP', only the two in rows
9 and 10 use cosine normalization.

The weighting scheme that produces the best results might be 'ltn' as it gives the second highest average MRR
and the third highest average MAP, as exemplified in row 4 of the 'Scheme Averages' table. However, seeing as 
'nnc' gives the highest average MRR and 4th highest average MAP in row 5 of the 'Scheme Averages' table, and is
the most frequently occurring scheme in the 'Top Ten by MRR' and 'Top Ten by MAP' tables, it is also a strong contender
for the best scheme overall.
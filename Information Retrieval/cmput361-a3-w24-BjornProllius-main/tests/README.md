# Important

# test1.py
**Purpose:** test if `build_index.py` on `good` collection produces the correct index by comparing it against a hard-coded index.

**Expected output:** PASS

**Example run**:
```bash
%python3 ./tests/test1.py
Testing if build_index.py matches expected output on good collection
expected to PASS
PASS
```



# test2.py
**Purpose:** test if `query.py` on `good` collection produces the correct results by comparing to expected output.

**Expected output:** PASS

**Example run**:
```bash
%python3 ./tests/test2.py
Testing if query.py matches expected output on good collection
expected to PASS
PASS
```


# test3.py
**Purpose:** test if `query.py` on `good` collection correctly fails when passed bad queries.

**Expected output:** PASS

**Example run**:
```bash
%python3 ./tests/test3.py
Testing if query.py returns errors on bad queries
expected to PASS
PASS
```


# test4.py
**Purpose:** test if `query.py` on `CISI_simplified` collection correctly returns at least 1 exptected result.

**Current status:** Does not work as the index for `CISI_simplified` has too many linked list objectes to pickle properly.

**Expected output:** PASS

**Example run**:
```bash
%python3 ./tests/test4.py
Testing if query.py matches expected output on CISI_simplified collection
expected to PASS
[nltk_data] Downloading package punkt to /home/lurcafe/nltk_data...
[nltk_data]   Package punkt is already up-to-date!
1460 documents read in total
Error: Failed to pickle index: maximum recursion depth exceeded while pickling an object
Error running query 62
Error running query 17
Error running query 90
Error running query 57
Error running query 58
Error running query 65
Error running query 81
Error running query 16
Error running query 84
Error running query 111
FAIL
```
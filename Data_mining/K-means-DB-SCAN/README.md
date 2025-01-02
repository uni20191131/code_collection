# K-means++ & DB-SCAN
CSE304 assginment2 group11

## Code Description

### K-means++ algorithm

- compile
```bash
$ javac A2_G11_t1.java
```

- run
```bash
$ java A2_G11_t1 <file_path>
$ java A2_G11_t1 <file_path> <k>
```
 The input parameter may include a value for k. If not specified, k must be estimated.

### DB-SCAN algorithm
- compile
```bash
$ javac A2_G11_t2.java
```

- run
```bash
$ java A2_G11_t2 <filePath> <mu> <eps>
$ java A2_G11_t2 <filePath> <mu/eps>
```
 The input parameter may include values for mu and epsilon. If the input is an integer, it represents mu; if a floating number, it represents epsilon.

## Dataset
- K-means++
1. artset1.csv : given sample data
2. artd-31.csv : givne sample data with includes two different density datasets
3. D31.csv
4. S2.csv
5. R15.csv

- DB-SCAN
1. artset1.csv : given sample data
2. artd-31.csv : givne sample data with includes two different density datasets
3. artd-31_1.csv : Datasets that separate data with different densities fro 'artd-31.csv'
4. artd-31_2.csv : Datasets that separate data with different densities fro 'artd-31.csv'

## Visualization tool
 - By using python, created a tool that can show clustering results in 2D and 3D graphs.
   1. /visualization tool/2D.ipynb
   2. /visualization tool/3D.ipynb

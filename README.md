dami
====

Scalable algorithms in data mining.

Our goal is to make algorithms that can handle hundreds of millions of data with a limited memory PC 

Currently we have 

classification: 
    SGD for logistic regressions
	
recommendation:
    SlopeOne, SVD, RSVD, neighborhood-SVD
	
significant test:
    swap randomization

Next release:

graph:
    Pagerank etc.

====
To achieve computation efficiency and memory utilization, two ways we have just adopted.
 
1: Using "id" as index of array for fetching data.

2: Only maintaining model in memory and saving data to converted bytes for IO

So it's highly recommemded you use continuous id for computation :)
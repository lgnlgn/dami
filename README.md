
**dami**
========

Scalable algorithms in **da**ta **mi**ning.

dami is writen in Java. Our goal is to make algorithms that can handle hundreds of millions of data with a limited memory PC 


Currently we have : 

- **utility**: Asynchronous vector buffer, High performance  and simple text parser. *More tests needs*

- **classification**: 
SGD for logistic regressions
	
- **recommendation**:
    SlopeOne, SVD, RSVD, itemneighborhood-SVD
	
- **significant test**:
 swap randomization

- **graph**:
    Pagerank.

Future:


- **similarity**:
    simhash 


---------

>*2012/7/20 Release Notes:*

> - Asynchronous vector buffer for dataset IO 

> - High performance and simple text parser(only for digital related chars)

> - small refactoring.

>*2012/7/12 Release Notes:*

> - code refactoring for recommendation and IO

> - To run RMSE for recommendation, you first need to see *`movielens_convert.py`* for converting and/or splitting movielens data, and see *`CFDataConverter`* and *`TestSVD`*

----------
To achieve computation efficiency and memory utilization, two ways we have just adopted.
 
*1: Using "id" as index of array for fetching data.*

*2: Only maintaining model in memory and saving data to converted bytes for IO*

So it's highly recommemded you use continuous ids for the algorithms :)

My Chinese blog : [http://blog.csdn.net/lgnlgn](http://blog.csdn.net/lgnlgn)      
E-mail : gnliang10 [at] 126.com
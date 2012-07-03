
**dami**
========

Scalable algorithms in **da**ta **mi**ning.

Our goal is to make algorithms that can handle hundreds of millions of data with a limited memory PC 

Currently we have : 

- **classification**: 
SGD for logistic regressions
	
- **recommendation**:
    SlopeOne, SVD, RSVD, neighborhood-SVD
	
- **significant test**:
 swap randomization

- **graph**:
    Pagerank etc.

Next release:

- code refactor for recommendation

- **similarity**:
    simhash 



----------
To achieve computation efficiency and memory utilization, two ways we have just adopted.
 
> 1: Using "id" as index of array for fetching data.
> 
> 2: Only maintaining model in memory and saving data to converted bytes for IO

So it's highly recommemded you use continuous ids for computation :)

My Chinese blog : [http://blog.csdn.net/lgnlgn](http://blog.csdn.net/lgnlgn)      
E-mail : gnliang10 [at] 126.com
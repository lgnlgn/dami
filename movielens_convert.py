import sys
#-------------------------------------------------------------------------------
# Name:
# Purpose:     convert movielens' ids to continues ids
#              download movielens 10M dataset
#               from  http://www.grouplens.org/sites/www.grouplens.org/external_files/data/ml-10m.zip
#               call simple_convert(movie_file, rating_file, output) to convert movie ids
#               call split(rating_input, howmany = 10)  to split dataset into training & test parts
# Author:      lgn
#
# Created:     10/07/2012
# Copyright:   (c) lgn 2012
# Licence:     <your licence>
#-------------------------------------------------------------------------------
#!/usr/bin/env python

def simple_convert(movie_file, rating_file, output):
    movieMap = [-1] * 100000
    movieId = 1
    for line in open(movie_file):
        movieMap[int(line.split("::")[0])] = movieId
        movieId += 1
    print "start~"
    fw = open(output, 'w')
    cc = 0
    for line in open(rating_file):
        info = line.split("::")
        fw.write("%s\t%d\t%s\n"% (info[0], movieMap[int(info[1])], info[2]))
        cc += 1
        if cc % 100000 == 0:
            print "1",
    fw.close()


def split(rating_input, howmany = 10):
    ftrain = open(rating_input + ".train", 'w')
    ftest = open(rating_input + ".test", 'w')
    i = 1
    for line in open(rating_input):
        if (i % howmany == 0):
            ftest.write(line)
        else:
            ftrain.write(line)
        i+= 1
    ftest.close()
    ftrain.close()

if __name__ == '__main__':
    movie_file = r"E:\data\ml-10M100K\movies.dat"
    rating_file = r"E:\data\ml-10M100K\ratings.dat"
    output = r"E:\data\ml-10M100K\movielens.txt"
##    movie_file = sys.argv[1]
##    rating_file = sys.argv[2]
##    output = sys.argv[3]
    simple_convert(movie_file, rating_file, output)
    split(output)
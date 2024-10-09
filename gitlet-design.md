# Gitlet Design Document

**Name**: Jorge Emanuel Nunez

## Classes and Data Structures


###Main
- creates Repository class by calling repository class
- Calls all the commands using the recently initialized repository

###Repository Class
- creates : staging area, commits, and blobs.
- create .gitlet folder and a necessary sub-folders/ files.
    - has all the commands.
      - add
        - a hash map
      - commit
      - log
      - checkout
      - etc.
- stored head commit in files. store initial commit, and manually creates initial commit. reference this as pointer (using a Tree Map). every new commit,
  - assign as class attribute, and get hash get files, and take that ones class and assign to the new commits parents, take the parent commits and copy it in.
###Commit Class
- data structure: SHA-1. 
    - commits all have 
      - time and day 
      - files that are tracked 
      - its commit message 
      - its own hashcode
      - and a parent to its parent commit.
    - first commit has null pointer
    - hash map stores all files staged in commit.
    - static method in commit to get memory, pass in hash.
- class instance variables:
    - date : time at which a commit was created. 
    - this.parent: the parent commit of a commit object.
    - this.message: contains the message of the commit.
    - this.Tracker(hashmap): the files pertaining to the commit object.
  
    

###Utils Class(given)
- will use this class to implement Persistence
- other uses given once I have a higher understanding of my own implementation.


## Algorithms


###Main
- init(): the constructor of the repository.


### Repository Class
- Repository: calls and initiates the Staging area, the initial commits area, and the blobs area. Also setups persistence.
- Will implement all the other functions in this class
    - Add
    - commit
    - log
    - checkout
    - etc.
    - will use a TreeMap to represent the branching.







## Persistence

- to keep the persistence or the state of the given git. I will implement Serializable in the Commit.
- I will also setup the persistance within my Repository class. this one one of my options.
- currently stuck on this.
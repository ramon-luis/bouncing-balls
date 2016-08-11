# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

* Quick summary
* Version
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### Branh Summary ###

There are 3 branches that represent 3 possible solutions:
* single-thread
* thread-for-checks
* thread-per-ball

Each branch has comments in the FXMLController that summarize the results of the solution. These comments are replicated below for easy review. The master branch represents my opinion of the optimal solution: it is the single-thread solution and matches the single-thread branch.


### single-thread branch ###

All calculations are handled by the main GUI thread in a JavaFX timeline. This implementation performs very well with no noticeable lag, even with many balls added.  Balls show a high level of respect for boundaries and "bounce" well off walls.  There is some visible overlap between balls before two colliding balls "bounce" off each other: this is common if one of the balls has a particularly high velocity.

A simple ArrayList can be used to manage outstanding balls since there is no concurrency.

All updates (boundary checking, collision detection, friction, movement) are called on an individual ball object.

### thread-for-checks branch ###

Multi-threaded solution where each animation frame spawns a single thread to check boundaries, check collisions, and apply friction. The ball movement occurs in the main JavaFX thread. This is a sub-optimal solution.  Delegating boundary checking and collision checking to background tasks results in time slicing: balls can be moved outside of boundaries and overlap with other balls more prominently than with a single-thread solution.  A better solution is to do a single iteration over the ball list where each ball is moved and then checked for boundary and collisions.

A CopyOnWriteArrayList is used to manage outstanding balls since the list is accessed by multiple threads.  This data structure is often recommended when concurrency is used for data that is often read and not often updated (i.e. add/delete).  This fits well with the list of active balls since the ball object data is read for detection and updating the attributes of each ball, but the list of ball objects is not often updated with addition/deletion.

Note that the location of a given ball object is only changed in the move() method which is only called from the main JavaFX thread.  This is intentional in order to comply with recommendations from java tutorial on concurrency with javaFX.

### thread-per-ball branch ###

This is a multi-threaded solution where each ball is moved in the main javafx thread and then background tasks are delegated to individual threads via tasks.  Each ball's boundary check, collision check, and friction application are all delegated to individual threads.  A task is called for each of these tasks, for each ball, during each animation keyframe.  This performs worse then having all processing done in a single thread and worse than having a single background thread to check for collisions, etc.  Animation lag occurs visibly when there are ~100+ balls with increased animation speed.  Additionally, this solution suffers from the same time-slicing errors as the other multi-threaded solution.  Despite using a thread executor to manage threads, this solution also can use up too many threads.  When the program is stressed with a high number of balls and fast animation, an error commonly occurs:

```
#!java

*Exception in thread "JavaFX Application Thread" java.lang.OutOfMemoryError: unable to create new native thread*
```

This is identified as an error from calling too many unnecessary threads: [http://stackoverflow.com/questions/16789288/java-lang-outofmemoryerror-unable-to-create-new-native-thread](Link URL)

Similar to the other multi-threaded solution, a CopyOnWriteArrayList data structure is used to manage the ball object list and each ball object's position is only updated by calls from the main javaFX thread.

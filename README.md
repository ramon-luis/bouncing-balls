# Bouncing Balls  

Bouncing Balls is an application that uses a container of bouncing ball objects to explore threading concepts with Java.  An empty visual container is createdat the start of the program. A user can add balls by either:
  1. clicking anywhere inside the container to add a single ball or 
  2. clicking the "+10 balls" button that will add 10 random balls to the container.  

Every ball that is added to the container consists of a random radius, color, opacity, and velocity.  Users can control the active friction of the environment and the animations speed via sliders.  The container / active window can be resized and balls will adjust accordingly to the new container boundaries.

![Bouncing Balls Screenshot 1](https://github.com/ramon-luis/bouncing-balls/raw/master/demo/bouncing-balls-screenshot-1.png "Bouncing Balls Screenshot 1")

## Application Highlights
  * a custom Timeline is used for animation  
  * ball movement checks:
    1. if a ball is currently out-of-bounds and moves the ball to be in-bounds
    2. the distance to current boundaries and adjusts the applied velocity for that specific animation frame in order to prevent balls from going past boundaries  
  * ball velocity is only negated if a ball is at a wall and moving towards that wall: using both of these conditions prevents the ball from "bouncing" along a wall  
  * collision detection checks each possible pair of ball objects one time in O(n<sup>2</sup>) time  
  * a Pane is used instead of an AnchorPane: using an AnchorPane resulted in balls going beyond visual boundaries  
    * Testing appeared to show that the bounds of an AnchorPane can adjust based on the nodes attached to it whereas a Pane will not adjust its bounds based on child nodes.  

## Implementation Details

There are 3 branches that represent 3 possible implementations:

1. single-thread
2. thread-for-checks
3. thread-per-ball

Each branch has comments in the FXMLController that summarize the results of the solution. These comments are replicated below for easy review. The master branch represents my opinion of the optimal solution: it is the single-thread solution using a JavaFX animation timeline and matches the single-thread branch.


#### single-thread branch

All calculations are handled by the main GUI thread in a JavaFX timeline. This implementation performs very well with no noticeable lag, even with many balls added.  Balls show a high level of respect for boundaries and "bounce" well off walls and each other.  There is some visible overlap between balls before two colliding balls "bounce" off each other: this is common if one of the balls has a particularly high velocity.  However, in general, ball collision performs fairly well and visibly better than the multi-threaded solutions outlined below.

A simple `ArrayList` can be used to manage outstanding balls since there is no concurrency.

All updates (boundary checking, collision detection, friction, movement) are called on an individual ball object via a single loop through the current ball object list.

#### thread-for-checks branch

Multi-threaded solution where each animation frame spawns a single thread to check boundaries, check collisions, and apply friction. The ball movement occurs in the main JavaFX thread. This is a sub-optimal solution.  Delegating boundary checking and collision checking to background tasks results in time slicing: balls can overlap with other balls more prominently than with a single-thread solution.  A better solution is to do a single iteration over the ball list where each ball is moved and then checked for boundary and collisions.

A `CopyOnWriteArrayList` is used to manage outstanding balls since the list is accessed by multiple threads.  This data structure is often recommended when concurrency is used for data that is often read and not often updated (i.e. add/delete).  This fits well with the list of active balls since the ball object data is read for detection and updating the attributes of each ball, but the list of ball objects is not often updated with addition/deletion.

Note that the location of a given ball object is only changed in the `move()` method which is only called from the main JavaFX thread.  This is intentional in order to comply with recommendations from java tutorials on concurrency with javaFX.

#### thread-per-ball branch

This is a multi-threaded solution where each ball is moved in the main javafx thread and then background tasks are delegated to individual threads via tasks.  Each ball's boundary check, collision check, and friction application are all delegated to individual threads.  A task is called for each of these tasks, for each ball, during each animation keyframe.  This performs worse then having all processing done in a single thread and worse than having a single background thread to check for collisions, etc.  Animation lag occurs visibly when the application is stressed via ball count and/or animation speed.  Additionally, this solution suffers from the same time-slicing error with collision detection as the other multi-threaded solution.  Despite using a thread executor to manage threads, this solution also can use too many threads.  When the program is stressed with a high number of balls and fast animation, an error commonly occurs:

```
#!java

Exception in thread "JavaFX Application Thread" java.lang.OutOfMemoryError: unable to create new native thread
```

This is identified as an error from calling too many unnecessary threads: [http://stackoverflow.com/questions/16789288/java-lang-outofmemoryerror-unable-to-create-new-native-thread](Link URL)

Similar to the other multi-threaded solution, a `CopyOnWriteArrayList` data structure is used to manage the ball object list and each ball object's position is only updated by calls from the main javaFX thread.

#### Possible Improvements

* Collision detection does not currently account for ball velocity.  It would be ideal to "pre-check" for ball collision and update the velocity for a frame in order to prevent balls from overlapping.  This concept is already applied to boundary checking.  
* Collision detection also does not account for ball proximity. The algorithm could be further optimized by assigning some form of location identity to balls (i.e. quadrants) and only checking ball combinations that are near one another.

## Getting Started / Installing / Deployment

Compile the src directory and call Main.  The main method is located in mvc/controller/Game.java.

```
$ javac src/main/java/proThreaded/MainApp.java
$ java src/main/java/proThreaded/MainApp
```

Or open in your favorite IDE (Eclipse, IntelliJ) and run Main in MainApp.java.


## Built With

* [Java](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
* [Maven](https://maven.apache.org/) - Dependency Management 

## Author

* [**Ramon-Luis**](https://github.com/ramon-luis)

## Acknowledgments

* Thank you to Adam Gerber at the University of Chicago


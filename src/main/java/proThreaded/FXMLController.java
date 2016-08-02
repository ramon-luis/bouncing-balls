package proThreaded;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

/*
* http://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm:
* If you implement a background worker by creating a Runnable object and a new thread, at some
* point, you must communicate with the JavaFX Application thread, either with a result or with the
* progress of the background task, which is error prone. Instead, use the JavaFX APIs provided by
* the javafx.concurrent package, which takes care of multithreaded code that interacts with the
* UI and ensures that this interaction happens on the correct thread.
* */

public class FXMLController implements Initializable {

    private static final int INITIAL_BALL_LIST_SIZE = 200;
    private static final Random sRandom = new Random();

    private List<Ball> mBalls;
    private ExecutorService mThreadExecutor;

    @FXML
    private AnchorPane mAnchorPane;

    @FXML
    private Slider sldSpeed;

    @FXML
    private ToggleButton tgbPause;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        mBalls = new ArrayList<>(INITIAL_BALL_LIST_SIZE);
        mThreadExecutor = Executors.newCachedThreadPool(); // this or threadpool?
        mAnchorPane.addEventFilter(MouseEvent.MOUSE_CLICKED, this::addNewBall);

        //Timeline animation = new Timeline(new KeyFrame(Duration.millis(50), e -> Platform.runLater(this::moveBalls)));
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            checkWalls();
            checkCollisions();
            moveBalls();
        }));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();

        sldSpeed.valueProperty().addListener((observable, oldValue, newValue) -> {
            animation.setRate( (double) newValue);
        });


        tgbPause.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                animation.pause();
            } else {
                animation.play();
            }
        });
    }


    private void addNewBall(MouseEvent mouseEvent) {
        double dRadius = getRandomRadius();
        // only add a new ball if the radius doesn't extend beyond borders -> prevents "bouncing" along axis
        if (mouseEvent.getX() > dRadius && mouseEvent.getY() > dRadius) {
            Ball ball = new Ball(mouseEvent.getX(), mouseEvent.getY(), dRadius);
            mBalls.add(ball);
            mAnchorPane.getChildren().add(ball);
        }
        //mThreadExecutor.execute(ball);
    }


    // http://gamedev.stackexchange.com/questions/20516/ball-collisions-sticking-together
    private void checkCollisions(){
        double xDist, yDist;

        // cycle through all balls
        for(int i = 0; i < mBalls.size(); i++){
            Ball ball = mBalls.get(i);

            // check for collision against other balls
            for(int j = i+1; j < mBalls.size(); j++){
                Ball otherBall =  mBalls.get(j);

                xDist = ball.getCenterX() - otherBall.getCenterX();
                yDist = ball.getCenterY() - otherBall.getCenterY();
                double distSquared = xDist*xDist + yDist*yDist;

                //Check the squared distances instead of the the distances, same result, but avoids a square root.
                if(distSquared <= (ball.getRadius() + otherBall.getRadius())*(ball.getRadius() + otherBall.getRadius())){
                    double xVelocity = otherBall.getVelocityX() - ball.getVelocityX();
                    double yVelocity = otherBall.getVelocityY() - ball.getVelocityY();
                    double dotProduct = xDist*xVelocity + yDist*yVelocity;

                    //Neat vector maths, used for checking if the objects moves towards one another.
                    if(dotProduct > 0){
                        double collisionScale = dotProduct / distSquared;
                        double xCollision = xDist * collisionScale;
                        double yCollision = yDist * collisionScale;

                        //The Collision vector is the speed difference projected on the Dist vector,
                        //thus it is the component of the speed difference needed for the collision.
                        double combinedMass = ball.getMass() + otherBall.getMass();
                        double collisionWeightA = 2 * otherBall.getMass() / combinedMass;
                        double collisionWeightB = 2 * ball.getMass() / combinedMass;
                        ball.setVelocityX(ball.getVelocityX() + collisionWeightA * xCollision);
                        ball.setVelocityY(ball.getVelocityY() + collisionWeightA * yCollision);
                        otherBall.setVelocityX(otherBall.getVelocityX() - collisionWeightB * xCollision);
                        otherBall.setVelocityY(otherBall.getVelocityY() - collisionWeightB * yCollision);
                    }
                }
            }
        }
    }

    private void checkWalls() {
        for (Ball ball : mBalls) {
            double dCurrentX = ball.getCenterX();
            double dCurrentY = ball.getCenterY();

            // check for wall at left/right of frame
            if (dCurrentX < ball.getRadius() || dCurrentX > mAnchorPane.getWidth() - ball.getRadius()) {
                ball.setVelocityX(ball.getVelocityX() * -0.95);  // ball slows slightly for wall bounce
            }

            // check for wall at top/bottom of frame
            if (dCurrentY < ball.getRadius() || dCurrentY > mAnchorPane.getHeight() - ball.getRadius()) {
                ball.setVelocityY(ball.getVelocityY() * -0.95);  // ball slows slightly for wall bounce
            }
        }
    }

    private void moveBalls() {
        mBalls.forEach(Ball::move);
    }

    private static double getRandomRadius() {
        return sRandom.nextDouble() * 20 + 15;  // between 15.0 and 35.0

    }



}

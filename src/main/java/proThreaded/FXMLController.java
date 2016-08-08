package proThreaded;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

/**
 * Friction and boundary checking are both applied by individual threads per ball.  Loop through the ball list
 * and start a thread for each ball that checks boundaries and applies friction.
 * This performs worse then having boundary checking & friction each done by a single thread for all balls.
* */

public class FXMLController implements Initializable {

    private static final double MAX_RADIUS = 30;
    private static final double MIN_RADIUS = 5;
    private static final Random sRandom = new Random();

    private CopyOnWriteArrayList<Ball> mBalls;
    private ExecutorService mThreadExecutor;
    private double mFrictionFactor = 1;

    @FXML
    private AnchorPane mAnchorPane;

    @FXML
    private ToggleButton tgbPause;

    @FXML
    private Button btnAdd10;

    @FXML
    private Slider sldSpeed;

    @FXML
    Slider sldFriction;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mBalls = new CopyOnWriteArrayList<>();
        mThreadExecutor = Executors.newCachedThreadPool();

        // create new timeline for animation, set cycle count, and play the animation
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            double dXBoundary = mAnchorPane.getWidth();
            double dYBoundary = mAnchorPane.getHeight();
            moveBalls();
            for (Ball ball : mBalls) {
                ball.move();
                ball.applyFriction(mFrictionFactor);
                ball.checkBoundaries(dXBoundary, dYBoundary);
            }
            checkCollisions();
        }));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();

        // play-pause toggle button
        tgbPause.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                animation.pause();
            } else {
                animation.play();
            }
        });

        // anchor pane mouse click -> add single ball
        mAnchorPane.addEventFilter(MouseEvent.MOUSE_CLICKED, this::addSingleBall);

        // button to add 10 balls on single click
        btnAdd10.setOnAction(e -> addTenBalls());

        sldFriction.valueProperty().addListener((observable, oldValue, newValue) -> {
           mFrictionFactor = 1 - (double) newValue / 10000.0;
        });

        // slider for animation speed
        sldSpeed.valueProperty().addListener((observable, oldValue, newValue) -> {
            animation.setRate( (double) newValue);
        });
    }  // end initialize

    // move the balls -> sets each ball to new x,y based on ball velocity
    private void moveBalls() {  // perform this in main animation thread
        mBalls.forEach(Ball::move);
    }

    // add a single ball
    private void addSingleBall(MouseEvent mouseEvent) {
        createAndAddBall(mouseEvent.getX(), mouseEvent.getY());
    }

    // add 10 balls
    private void addTenBalls() {
        double dMinXorY = MAX_RADIUS;
        double dMaxX = mAnchorPane.getWidth() - MAX_RADIUS;
        double dMaxY = mAnchorPane.getHeight() - MAX_RADIUS;
        for (int i = 0; i < 10; i++) {
            double dCenterX = getRandomInRange(dMinXorY, dMaxX);
            double dCenterY = getRandomInRange(dMinXorY, dMaxY);
            createAndAddBall(dCenterX, dCenterY);
        }
    }

    // create a ball and add to the anchorpane
    private void createAndAddBall(double dCenterX, double dCenterY) {
        double dRadius = getRandomInRange(MIN_RADIUS, MAX_RADIUS);
        Ball ball = new Ball(dCenterX, dCenterY, dRadius);
        mBalls.add(ball);
        mAnchorPane.getChildren().add(ball);
    }

    // http://gamedev.stackexchange.com/questions/20516/ball-collisions-sticking-together
    private void checkCollisions() {
            // variables to store x and y distance
            double xDist, yDist;

            // cycle through all balls
            for (int i = 0, len = mBalls.size(); i < len; i++){
                // access each ball in list
                Ball ball = mBalls.get(i);

                // check for collision against other balls "to right" in list -> each pair is only checked once
                for (int j = i + 1; j < len; j++){
                    // access "other" ball in list
                    Ball otherBall =  mBalls.get(j);

                    // calculate distance parameters
                    xDist = ball.getCenterX() - otherBall.getCenterX();
                    yDist = ball.getCenterY() - otherBall.getCenterY();
                    double distSquared = xDist * xDist + yDist * yDist;

                    // check if balls are colliding: squared distances avoids a square root
                    if (distSquared <= (ball.getRadius() + otherBall.getRadius()) * (ball.getRadius() + otherBall.getRadius())){
                        double xVelocity = otherBall.getVelocityX() - ball.getVelocityX();
                        double yVelocity = otherBall.getVelocityY() - ball.getVelocityY();
                        double dotProduct = xDist * xVelocity + yDist * yVelocity;

                        // check if the objects are moving towards one another
                        if (dotProduct > 0){
                            double collisionScale = dotProduct / distSquared;
                            double xCollision = xDist * collisionScale;
                            double yCollision = yDist * collisionScale;

                            // collision vector is the speed difference projected on the dist vector,
                            double combinedMass = ball.getMass() + otherBall.getMass();
                            double collisionWeightA = 2 * otherBall.getMass() / combinedMass;
                            double collisionWeightB = 2 * ball.getMass() / combinedMass;

                            // update velocity of each ball appropriately
                            ball.setVelocityX(ball.getVelocityX() + collisionWeightA * xCollision);
                            ball.setVelocityY(ball.getVelocityY() + collisionWeightA * yCollision);
                            otherBall.setVelocityX(otherBall.getVelocityX() - collisionWeightB * xCollision);
                            otherBall.setVelocityY(otherBall.getVelocityY() - collisionWeightB * yCollision);
                        }  // end if (dotProduct > 0)
                    }  // end if (distSquared...)
                } // end for (j = i + 1...)
        }  // end call
    }  // end collisionCheck task

    // helper method to get a random double in a certain range
    private static double getRandomInRange(double dMin, double dMax) {
        return sRandom.nextDouble() * (dMax - dMin) + dMin;
    }


}

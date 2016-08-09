package proThreaded;

import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * No threads: all calculations are handled by main GUI thread in JavaFX timeline.
 * This implementation performs very well with no noticeable lag, even with many balls added.
 * A simple ArrayList can be used since there is no concurrency.
* */

public class FXMLController implements Initializable {

    private static final double MAX_RADIUS = 30;
    private static final double MIN_RADIUS = 5;
    private static final Random sRandom = new Random();

    private ArrayList<Ball> mBalls;
    private double mFrictionFactor = 1;

    @FXML
    private VBox vbFrame;

    @FXML
    private Pane pane;

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
        mBalls = new ArrayList<>(5000);

        // create new timeline for animation, set cycle count, and play the animation
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            // get current bounds of pane
            double dXBoundary = pane.getWidth();
            double dYBoundary = pane.getHeight();

            // update each ball: loop through list one time each animation cycle
            for (Ball ball : mBalls) {
                ball.move(dXBoundary, dYBoundary);
                ball.checkBoundaries(dXBoundary, dYBoundary);
                ball.checkCollisions(mBalls);
                ball.applyFriction(mFrictionFactor);
            }
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

        // pane properties
        pane.addEventFilter(MouseEvent.MOUSE_CLICKED, this::addSingleBall);
        pane.prefWidthProperty().bind(vbFrame.widthProperty());
        pane.prefHeightProperty().bind(vbFrame.heightProperty());

        // button to add 10 balls on single click
        btnAdd10.setOnAction(e -> addTenBalls());

        // friction slider
        sldFriction.valueProperty().addListener((observable, oldValue, newValue) -> {
           mFrictionFactor = 1 - (double) newValue / 100.0;
        });

        // slider for animation speed
        sldSpeed.valueProperty().addListener((observable, oldValue, newValue) -> {
            animation.setRate( (double) newValue);
        });
    }  // end initialize

    // add a single ball
    private void addSingleBall(MouseEvent mouseEvent) {
        createAndAddBall(mouseEvent.getX(), mouseEvent.getY());
    }

    // add 10 balls
    private void addTenBalls() {
        double dMinXorY = MAX_RADIUS;
        double dMaxX = pane.getWidth() - MAX_RADIUS;
        double dMaxY = pane.getHeight() - MAX_RADIUS;
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
        pane.getChildren().add(ball);
    }

    // helper method to get a random double in a certain range
    private static double getRandomInRange(double dMin, double dMax) {
        return sRandom.nextDouble() * (dMax - dMin) + dMin;
    }


}

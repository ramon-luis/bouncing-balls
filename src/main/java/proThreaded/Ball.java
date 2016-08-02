package proThreaded;

import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.Random;

/* Filename:        Ball.java
 * Last Modified:   19 Mar 2014
 * Author:          Todd Parker
 * Email:           todd.i.parker@maine.edu
 * Course:          CIS314 - Advanced Java
 *
 * Ball.java implements Runnable so that each mCircle can possess its own thread.
 * Each Ball maintains attributes such as size, coordinates, color, velocity,
 * and boundary limits.  Accesssor and Mutator methods are available for each
 * respective attribute to allow BallPanel to maintain a Ball's creation and
 * positioning.
 */

public class Ball extends Circle implements Runnable, Movable  {

    private static final int SLEEP_DELAY = 20;
    private static final Random sRandom = new Random();

    private double mVelocityX;
    private double mVelocityY;

    // Constructor
    public Ball(double xPos, double yPos, double radius) {
        super(xPos, yPos, radius, getRandomColor());
        mVelocityX = getRandomVelocity();
        mVelocityY = getRandomVelocity();
    }


    public double getMass() {
        return getRadius() * getRadius() * getRadius() / 1000d;
    }

    // Update mCircle position
    public void run() {
        while(true) {
            move();
            try {
                Thread.sleep(SLEEP_DELAY);
            } catch(Exception e){}
        }
    }



    public double getVelocityX() {
        return mVelocityX;
    }

    public void setVelocityX(double velocityX) {
        mVelocityX = velocityX;
    }

    public double getVelocityY() {
        return mVelocityY;
    }

    public void setVelocityY(double velocityY) {
        mVelocityY = velocityY;
    }

    public void move() {
        double dCurrentX = super.getCenterX();
        double dCurrentY = super.getCenterY();

        this.setCenterX(dCurrentX + mVelocityX);
        this.setCenterY(dCurrentY + mVelocityY);
    }


    // helper methods
    private static Color getRandomColor() {
        double dR = sRandom.nextDouble();
        double dG = sRandom.nextDouble();
        double dB = sRandom.nextDouble();
        double dOpacity = Math.max(sRandom.nextDouble(), 0.75);  //  between 0.5 and 1.0

        return new Color(dR, dG, dB, dOpacity);
    }

    private static double getRandomVelocity() {
        return sRandom.nextDouble() * 10 - 5;  // between -5.0 and 5.0
    }

}


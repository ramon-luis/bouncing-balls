package proThreaded;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *  Ball Class extends circle: added x and y velocity and mass.  X and y velocity
 *  are used to move ball during animation.  ApplyFriction() reduces the velocity.
 */


class Ball extends Circle {

    private static final Random sRandom = new Random();

    private double mVelocityX;
    private double mVelocityY;

    // Constructor
    Ball(double xPos, double yPos, double radius) {
        super(xPos, yPos, radius, getRandomColor());
        mVelocityX = getRandomVelocity();
        mVelocityY = getRandomVelocity();
    }


    double getMass() {
        return getRadius() * getRadius() * getRadius() / 1000d;
    }

    double getVelocityX() {
        return mVelocityX;
    }

    void setVelocityX(double velocityX) {
        mVelocityX = velocityX;
    }

    double getVelocityY() {
        return mVelocityY;
    }

    void setVelocityY(double velocityY) {
        mVelocityY = velocityY;
    }

    // used to animate ball
    void move() {
        this.setCenterX(getCenterX() + mVelocityX);
        this.setCenterY(getCenterY() + mVelocityY);
    }

    // changes the velocity
    void applyFriction(double dFrictionFactor) {
        this.setVelocityX(mVelocityX * dFrictionFactor);
        this.setVelocityY(mVelocityY * dFrictionFactor);
    }

    void checkBoundaries(double dXBoundary, double dYBoundary) {
        // booleans for location
        boolean bAtLeftWall = getCenterX() <= getRadius();
        boolean bAtRightWall = getCenterX() >= (dXBoundary - getRadius());
        boolean bAtTopWall = getCenterY() <= getRadius();
        boolean bAtBottomWall = getCenterY() >= (dYBoundary - getRadius());

        // booleans for movement
        boolean bIsMovingLeft = mVelocityX < 0;
        boolean bIsMovingRight = mVelocityX > 0;
        boolean bIsMovingUp = mVelocityY < 0;
        boolean bIsMovingDown = mVelocityY > 0;

        // check left and right boundary - update velocity
        if ((bAtLeftWall && bIsMovingLeft) || (bAtRightWall&& bIsMovingRight)) {
            mVelocityX = -mVelocityX;
        }

        // check upper and lower boundary - update velocity
        if ((bAtTopWall && bIsMovingUp) || (bAtBottomWall && bIsMovingDown)) {
            mVelocityY = -mVelocityY;
        }

    }


    // helper method for random color
    private static Color getRandomColor() {
        double dR = sRandom.nextDouble();
        double dG = sRandom.nextDouble();
        double dB = sRandom.nextDouble();
        double dOpacity = Math.max(sRandom.nextDouble(), 0.75);  //  between 0.5 and 1.0

        return new Color(dR, dG, dB, dOpacity);
    }

    // helper method for random velocity
    private static double getRandomVelocity() {
        return sRandom.nextDouble() * 10 - 5;  // between -5.0 and 5.0
    }

}


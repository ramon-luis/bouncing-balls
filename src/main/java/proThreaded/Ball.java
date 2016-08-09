package proThreaded;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Random;

/**
 *  Ball Class extends circle: added x and y velocity and mass.  X and y velocity
 *  are used to move ball during animation.  ApplyFriction() reduces the velocity.
 */


class Ball extends Circle {

    private static final Random sRandom = new Random();

    private double mVelocityX;
    private double mVelocityY;
    private double mMass;

    // Constructor
    Ball(double xPos, double yPos, double radius) {
        super(xPos, yPos, radius, getRandomColor());
        mVelocityX = getRandomVelocity();
        mVelocityY = getRandomVelocity();
        mMass = radius * radius * radius / 1000d;
    }

    private double getMass() {
        return mMass;
    }

    private double getVelocityX() {
        return mVelocityX;
    }

    private void setVelocityX(double velocityX) {
        mVelocityX = velocityX;
    }

    private double getVelocityY() {
        return mVelocityY;
    }

    private void setVelocityY(double velocityY) {
        mVelocityY = velocityY;
    }

    // used to animate ball
    void move(double dXBoundary, double dYBoundary) {
        // initial values for movement
        double dXAdjust = mVelocityX;
        double dYAdjust = mVelocityY;

        // update if outside bounds already
        if (getCenterX() < 0) {
            setCenterX(getRadius());
        } else if (getCenterX() + getRadius() > dXBoundary) {
            setCenterX(dXBoundary - getRadius());
        }
        if (getCenterY() < 0) {
            setCenterY(getRadius());
        } else if (getCenterY() + getRadius() > dYBoundary) {
            setCenterY(dYBoundary - getRadius());
        }

        // update the xadjust if a move will take ball outside of x bounds
        if (isMovingLeft() && getCenterX() - mVelocityX < getRadius()) {
            dXAdjust = Math.max(getRadius() - getCenterX(), mVelocityX);
        } else if (isMovingRight() && getCenterX() + mVelocityX > dXBoundary - getRadius()) {
            dXAdjust = Math.min(dXBoundary - (getRadius() + getCenterX()), mVelocityX);
        }

        // update the yadjust if a move will take ball outside of y bounds
        if (isMovingUp() && getCenterY() - mVelocityY < getRadius()) {
            dYAdjust = Math.max(getRadius() - getCenterY(), mVelocityY);
        } else if (isMovingDown() && getCenterY() + mVelocityY > dYBoundary - getRadius()) {
            dYAdjust = Math.min(dYBoundary - (getRadius() + getCenterY()), mVelocityY);
        }

        // reset the center of the ball
        this.setCenterX(getCenterX() + dXAdjust);
        this.setCenterY(getCenterY() + dYAdjust);
    }

    // changes the velocity
    void applyFriction(double dFrictionFactor) {
        mVelocityX = mVelocityX * dFrictionFactor;
        mVelocityY = mVelocityY * dFrictionFactor;
    }

    void checkBoundaries(double dXBoundary, double dYBoundary) {
        // booleans for location
        boolean bAtLeftWall = getCenterX() <= getRadius();
        boolean bAtRightWall = getCenterX() >= (dXBoundary - getRadius());
        boolean bAtTopWall = getCenterY() <= getRadius();
        boolean bAtBottomWall = getCenterY() >= (dYBoundary - getRadius());

        // check left and right boundary - update velocity
        if ((bAtLeftWall && isMovingLeft()) || (bAtRightWall&& isMovingRight())) {
            mVelocityX = -mVelocityX;
        }

        // check upper and lower boundary - update velocity
        if ((bAtTopWall && isMovingUp()) || (bAtBottomWall && isMovingDown())) {
            mVelocityY = -mVelocityY;
        }
    }

    // http://gamedev.stackexchange.com/questions/20516/ball-collisions-sticking-together
    void checkCollisions(ArrayList<Ball> allBalls) {
        // variables to store x and y distance
        double xDist, yDist;

        int i = allBalls.indexOf(this);
        int len = allBalls.size();

            // check for collision against other balls "to right" in list -> each pair is only checked once
            for (int j = i + 1; j < len; j++){
                // access "other" ball in list
                Ball otherBall =  allBalls.get(j);

                // calculate distance parameters
                xDist = getCenterX() - otherBall.getCenterX();
                yDist = getCenterY() - otherBall.getCenterY();
                double distSquared = xDist * xDist + yDist * yDist;

                // check if balls are colliding: squared distances avoids a square root
                if (distSquared <= (getRadius() + otherBall.getRadius()) * (getRadius() + otherBall.getRadius())){
                    double xVelocity = otherBall.getVelocityX() - getVelocityX();
                    double yVelocity = otherBall.getVelocityY() - getVelocityY();
                    double dotProduct = xDist * xVelocity + yDist * yVelocity;

                    // check if the objects are moving towards one another
                    if (dotProduct > 0){
                        double collisionScale = dotProduct / distSquared;
                        double xCollision = xDist * collisionScale;
                        double yCollision = yDist * collisionScale;

                        // collision vector is the speed difference projected on the dist vector,
                        double combinedMass = getMass() + otherBall.getMass();
                        double collisionWeightA = 2 * otherBall.getMass() / combinedMass;
                        double collisionWeightB = 2 * getMass() / combinedMass;

                        // update velocity of each ball appropriately
                        mVelocityX = (mVelocityX + collisionWeightA * xCollision);
                        mVelocityY = (mVelocityY + collisionWeightA * yCollision);
                        otherBall.setVelocityX(otherBall.getVelocityX() - collisionWeightB * xCollision);
                        otherBall.setVelocityY(otherBall.getVelocityY() - collisionWeightB * yCollision);
                    }  // end if (dotProduct > 0)
                }  // end if (distSquared...)
            } // end for (j = i + 1...)
        }  // end call



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


    // booleans for movement direction
    private boolean isMovingLeft() {
        return mVelocityX < 0;
    }

    private boolean isMovingRight() {
        return mVelocityX > 0;
    }

    private boolean isMovingUp() {
        return mVelocityY < 0;
    }

    private boolean isMovingDown() {
        return mVelocityY > 0;
    }

}



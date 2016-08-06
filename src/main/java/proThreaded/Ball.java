package proThreaded;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.Random;

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

    void move() {
        this.setCenterX(getCenterX() + mVelocityX);
        this.setCenterY(getCenterY() + mVelocityY);
    }

    void applyFriction(double dFrictionFactor) {
        this.setVelocityX(mVelocityX * dFrictionFactor);
        this.setVelocityY(mVelocityY * dFrictionFactor);
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


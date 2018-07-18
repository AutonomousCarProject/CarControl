package com.apw.blobtrack;

import com.apw.blobdetect.Blob;
import com.apw.oldglobal.Constant;
import com.apw.oldimage.IPixel;

public class MovingBlob extends Blob
{

    // The X and Y components of the MovingBlob's velocity.
    public float velocityX, velocityY;

    public float velocityChangeX, velocityChangeY;

    // The predicted X and Y center coordinates of the MovingBlob in the next frame.
    public float predictedX, predictedY;

    // The time, in frames, that the MovingBlob has been on-screen.
    public int age;

    // The time, in frames, that the MovingBlob has been off-screen.
    public int ageOffScreen;
    
    private int score = 0;

    /**
     * Creates a MovingBlob from a Blob. This is used when a previously unseen
     * Blob comes on-screen.
     * 
     * @param b
     *            The Blob that the MovingBlob will be created from.
     */
    public MovingBlob(Blob b)
    {
        super(b.width, b.height, b.x, b.y, b.color);
        this.velocityX = 0;
        this.velocityY = 0;
        this.age = 0;
        this.ageOffScreen = 0;
        updatePredictedPosition();
    }

    /**
     * Default constructor for MovingBlob. This primarily exists to make the
     * creation of UnifiedBlobs more simple.
     */
    public MovingBlob()
    {
        super(0, 0, 0, 0, null);
    }

    public MovingBlob(int width, int height, int x, int y, IPixel color, float velocityX, float velocityY)
    {
        super(width, height, x, y, color);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.age = 0;
        this.ageOffScreen = 0;
        updatePredictedPosition();
    }
    //for testing only
    public MovingBlob(int width, int height, int x, int y, float velocityX, float velocityY,int age)
    {
        super(width, height, x, y,null);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.age = age;
        this.ageOffScreen = 0;
        updatePredictedPosition();
    }

    /**
     * This method is used to scale the X velocity of the MovingBlob. This is
     * used to reduce the effect of a pedestrian being far away from the camera
     * and seeming to move more slowly because of the distance.
     * 
     * @return The X velocity of the MovingBlob scaled based on the MovingBlob's
     *         width and height
     */
    public float getScaledVelocityX()
    {
        return 100* this.velocityX / this.height;//(this.width + this.height);
    }

    /**
     * This method is used to scale the Y velocity of the MovingBlob. This is
     * used to reduce the effect of a pedestrian being far away from the camera
     * and seeming to move more slowly because of the distance.
     * 
     * @return The Y velocity of the MovingBlob scaled based on the MovingBlob's
     *         width and height
     */
    public float getScaledVelocityY()
    {
        return this.velocityY / this.height;//(this.width + this.height);
    }

    /**
     * Simple method to get the magnitude of the MovingBlob's velocity.
     * 
     * @return The magnitude of the scaled velocity of the MovingBlob
     */
    public float getVelocityMagnitude()
    {
        float scaledX = this.getScaledVelocityX();
        float scaledY = this.getScaledVelocityY();
        return (float) Math.sqrt(scaledX * scaledX + scaledY * scaledY);
    }

    /**
     * Updates the predicted position of the MovingBlob based on its center and
     * velocity.
     */
    public void updatePredictedPosition()
    {
        predictedX = velocityX/67*Constant.TIME_DIFFERENCE + x+width/2;
        predictedY = velocityY/67*Constant.TIME_DIFFERENCE + y+height/2;
    }

    @Override
    public String toString()
    {
        return "Moving blob: Color " + /* color.getColor() + */ " X: " + x + " Y: " + y + " vX: " + velocityX +
        		" vY: " + velocityY + " pX: " + predictedX + " pY: " + predictedY + " w: " + width + " h: " + height
        		+ " age: " + age + " ageoff: " + ageOffScreen;
    }
    public void incrementScore()
    {
        score++;
    }
    public void setScore(int score)
    {
        this.score = score;
    }
    public int getScore()
    {
        return score;
    }
    
    public float getDensity(){return -1;}
}
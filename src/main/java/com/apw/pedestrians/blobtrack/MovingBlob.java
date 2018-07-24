package com.apw.pedestrians.blobtrack;

import com.apw.pedestrians.Constant;
import com.apw.pedestrians.blobdetect.Blob;

public class MovingBlob extends Blob {

  // The X and Y components of the MovingBlob's velocity.
  public float velocityX, velocityY;
  public float velocityChangeX, velocityChangeY;
  // The predicted X and Y center coordinates of the MovingBlob in the next frame.
  public float predictedX, predictedY;
  // The time, in frames, that the MovingBlob has been on-screen.
  public int age;
  // The time, in frames, that the MovingBlob has been off-screen.
  public int ageOffScreen;

  /**
   * Creates a MovingBlob from a Blob. This is used when a previously unseen
   * Blob comes on-screen.
   *
   * @param b The Blob that the MovingBlob will be created from.
   */
  public MovingBlob(Blob b) {
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
  public MovingBlob() {
    super(0, 0, 0, 0, null);
  }

  /**
   * This method is used to scale the X velocity of the MovingBlob. This is
   * used to reduce the effect of a pedestrian being far away from the camera
   * and seeming to move more slowly because of the distance.
   *
   * @return The X velocity of the MovingBlob scaled based on the MovingBlob's
   * width and height
   */
  public float getScaledVelocityX() {
    return 100 * this.velocityX / this.height;//(this.width + this.height);
  }

  /**
   * This method is used to scale the Y velocity of the MovingBlob. This is
   * used to reduce the effect of a pedestrian being far away from the camera
   * and seeming to move more slowly because of the distance.
   *
   * @return The Y velocity of the MovingBlob scaled based on the MovingBlob's
   * width and height
   */
  public float getScaledVelocityY() {
    return this.velocityY / this.height;//(this.width + this.height);
  }

  /**
   * Updates the predicted position of the MovingBlob based on its center and
   * velocity.
   */
  public void updatePredictedPosition() {
    predictedX = velocityX / 67 * Constant.TIME_DIFFERENCE + x + width / 2;
    predictedY = velocityY / 67 * Constant.TIME_DIFFERENCE + y + height / 2;
  }

  @Override
  public String toString() {
    return "Moving blob: Color " + /* color.getColor() + */ " X: " + x + " Y: " + y + " vX: "
        + velocityX +
        " vY: " + velocityY + " pX: " + predictedX + " pY: " + predictedY + " w: " + width + " h: "
        + height
        + " age: " + age + " ageoff: " + ageOffScreen;
  }

  public float getDensity() {
    return -1;
  }
}
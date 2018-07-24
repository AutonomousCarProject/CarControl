package com.apw.pedestrians.blobtrack;

import java.util.Set;

public class UnifiedBlob extends MovingBlob {

  private float density;

  public UnifiedBlob(Set<MovingBlob> movingBlobs) {
    super();
    int numBlobs = movingBlobs.size();
    int minAgeOffScreen = 100000;
    float totalVelocityX = 0;
    float totalVelocityY = 0;
    float top = 1000000;
    float bottom = 0;
    float left = 1000000;
    float right = 0;

    float totalArea = 0;

    //find averages and other values
    for (MovingBlob movingBlob : movingBlobs) {
      minAgeOffScreen = Math.min(minAgeOffScreen, movingBlob.ageOffScreen);

      totalVelocityX += movingBlob.velocityX;
      totalVelocityY += movingBlob.velocityY;

      float blobRightSide = movingBlob.x + movingBlob.width;
      if (blobRightSide > right) {
        right = blobRightSide;
      }

      float blobLeftSide = movingBlob.x;
      if (blobLeftSide < left) {
        left = blobLeftSide;
      }

      float blobTop = movingBlob.y;
      if (blobTop < top) {
        top = blobTop;
      }

      float blobBottom = movingBlob.y + movingBlob.height;
      if (blobBottom > bottom) {
        bottom = blobBottom;
      }

      totalArea += movingBlob.width * movingBlob.height;
    }
    this.age = 0;
    //this.age = maxAge;
    this.ageOffScreen = minAgeOffScreen;

    this.velocityX = totalVelocityX / numBlobs;
    this.velocityY = totalVelocityY / numBlobs;
    this.x = (int) (left);
    this.y = (int) (top);

    this.width = (int) (right - left);
    this.height = (int) (bottom - top);

    this.density = 10 * totalArea / (this.width * this.height);

    this.updatePredictedPosition();
  }

  @Override
  public float getDensity() {
    return this.density;
  }

}

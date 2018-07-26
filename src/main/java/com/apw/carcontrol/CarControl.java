package com.apw.carcontrol;

/**
 * Wraps control of the car through either TrakSim or real-world car control.
 */
public interface CarControl {
    /**
     * Gets the current image, from either {@link com.apw.fly2cam.FlyCamera FlyCamera} or {@link com.apw.apw3.SimCamera SimCamera}.
     * @return The image as a Bayer-8 encoded byte array.
     */
    byte[] readCameraImage();

    /**
     * Gets the most recent camera image as loaded from {@link #readCameraImage() readCameraImage}.
     * @return The most recent camera image as a Bayer-8 encoded byte array.
     */
    byte[] getRecentCameraImage();

    /**
     * Gets a processed image. Either this is equivalent to the original from {@link #readCameraImage()} readCameraImage} or will be processed and set through {@link #setProcessedImage(byte[]) setCameraImage}.
     * @return The processed image in the form of a Bayer-8 byte array.
     */
    byte[] getProcessedImage();

    /**
     * Sets the processed image.
     * @param image The processed image in the form of a Bayer-8 byte array.
     */
    void setProcessedImage(byte[] image);

    /**
     * Closes open resources and exits.
     * @param why Error code.
     */
    void exit(int why);
    /**
     * Accelerates the car.
     * @param absolute If true, sets the velocity to the parameter given, else increments it by the parameter given.
     * @param velocity The velocity to set the car's velocity to or increment it by.
     */
    void accelerate(boolean absolute, int velocity);

    /**
     * Steers the car.
     * @param absolute If true, sets the angle to the parameter given, else increments it by the parameter given.
     * @param angle The angle to set the car's angle to or increment it by.
     */
    void steer(boolean absolute, int angle);

}

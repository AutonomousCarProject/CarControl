package com.apw.carcontrol;

import com.apw.fakefirm.Arduino;

import java.awt.*;

/**
 * Wraps control of the car through either TrakSim or real-world car control.
 */
public interface CarControl {
    /**
     * Gets the current image, from either {@link com.apw.fly2cam.FlyCamera FlyCamera} or {@link com.apw.apw3.SimCamera SimCamera}.
     * Also invalidates the current version of the processed/rendering images.
     * @return The image as a Bayer-8 encoded byte array.
     */
    byte[] readCameraImage();

    /**
     * Gets the most recent camera image as loaded from {@link #readCameraImage() readCameraImage}.
     * @return The most recent camera image as a Bayer-8 encoded byte array.
     */
    byte[] getRecentCameraImage();

    /**
     * Gets a processed image. This should be processed and set through {@link #setProcessedImage(byte[]) setCameraImage}. If not, an exception may be thrown.
     * @return The processed image in the form of a simple color raster array.
     */
    byte[] getProcessedImage();

    int[] getRGBImage();

    /**
     * Sets the processed image.
     * @param image The processed image in the form of a simple color raster array.
     */
    void setProcessedImage(byte[] image);

    /**
     * Sets the image to be rendered on the screen.
     * Note that this will do nothing if there is no screen/window to draw on ({@link #willPaint() willPaint} returns false).
     * @param renderedImage The image to render on the screen as an RGB integer array.
     */
    void setRenderedImage(int[] renderedImage);

    /**
     * Checks if this car control will paint (if there is a screen/window to draw on).
     * @return Whether or not the car control will paint.
     */
    boolean willPaint();

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

    // TODO figure this out, write javadocs, maybe modify interface
    int getGas();

    int getSteering();

    int getManualSpeed();

    Insets getEdges();

    // this is a traksim method
    void rectFill(int colo, int rx, int cx, int rz, int c);

    Arduino getServos();
}

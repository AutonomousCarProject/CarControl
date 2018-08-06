package com.apw.carcontrol;

import java.awt.*;
import java.util.ArrayList;

/**
 * Wraps control of the car through either TrakSim or real-world car control.
 */
public interface CarControl {

    /**
     * Gets the current image, from either {@link com.apw.fly2cam.FlyCamera FlyCamera} or {@link com.apw.apw3.SimCamera SimCamera}.
     * Also invalidates the current version of the processed/rendering images.
     *
     * @return The image as a Bayer-8 encoded byte array.
     */
    byte[] readCameraImage();

    /**
     * Gets the most recent camera image as loaded from {@link #readCameraImage() readCameraImage}.
     *
     * @return The most recent camera image as a Bayer-8 encoded byte array.
     */
    byte[] getRecentCameraImage();

    /**
     * Gets a processed image. This should be processed and set through {@link #setProcessedImage(byte[]) setCameraImage}. If not, an exception may be thrown.
     *
     * @return The processed image in the form of a simple color raster array.
     */
    byte[] getProcessedImage();

    /**
     * Sets the processed image.
     *
     * @param image The processed image in the form of a simple color raster array.
     */
    void setProcessedImage(byte[] image);

    /**
     * Gets the rgb image.
     *
     * @return The rgb image in the form of an rgb raster array.
     */
    int[] getRGBImage();

    /**
     * Gets the rendered image;
     * @return The rendered image in the form of an rgb raster array.
     */
    int[] getRenderedImage();

    /**
     * Sets the rgb image.
     *
     * @param image The rgb image in the form of a rgb raster array.
     */
    void setRGBImage(int[] image);

    /**
     * Gets the width of the image.
     * @return The image width as an int.
     */
    int getImageWidth();

    /**
     * Gets the height of the image.
     * @return The image height as an int.
     */
    int getImageHeight();

    /**
     * Sets the image to be rendered on the screen.
     * Note that this will do nothing if there is no screen/window to draw on ({@link #willPaint() willPaint} returns false).
     *
     * @param renderedImage The image to render on the screen as an RGB integer array.
     */
    void setRenderedImage(int[] renderedImage);

    /**
     * Checks if this car control will paint (if there is a screen/window to draw on).
     *
     * @return Whether or not the car control will paint.
     */
    boolean willPaint();

    /**
     * Closes open resources and exits.
     *
     * @param why Error code.
     */
    void exit(int why);

    /**
     * Accelerates the car.
     *
     * @param absolute If true, sets the velocity to the parameter given, else increments it by the parameter given.
     * @param velocity The velocity to set the car's velocity to or increment it by.
     */
    void accelerate(boolean absolute, int velocity);

    /**
     * Steers the car.
     *
     * @param absolute If true, sets the angle to the parameter given, else increments it by the parameter given.
     * @param angle    The angle to set the car's angle to or increment it by.
     */
    void steer(boolean absolute, int angle);

    /**
     * Manually controls the speed of the car.
     *
     * @param absolute If true, sets the manual speed to the parameter given, else increments it by the parameter given.
     * @param manualSpeed The manual speed to set the car's velocity to or increment it by.
     */
    void manualSpeedControl(boolean absolute, int manualSpeed);

    /**
     * Gets the current gas (velocity) of the car.
     * @return The curremt gas/velocity value.
     * @see #accelerate(boolean, int)
     */
    int getVelocity();

    /**
     * Gets the current steering angle of the car
     * @return The current steering angle.
     * @see #steer(boolean, int)
     */
    int getSteering();

    /**
     * Gets the manual speed control of the car.
     * @return The current manual speed control value.
     * @see #manualSpeedControl(boolean, int)
     */
    int getManualSpeed();

    /**
     * Gets the edges of the screen (if one exists).
     * @return The edges of the rendering window as a Java AWT Insets object.
     */
    Insets getEdges();


    /**
     * Set the edges of the screen. If one does not exist, does nothing.
     * @param edges The edges of the rendering window as a Java AWT Insets object.
     */
    void setEdges(Insets edges);

    /**
     * Draws a rectangle on the screen (if one exists).
     *
     * @param colo The pixel color to fill that rectangle, = 0x00RRGGBB
     * @param rx   The pixel row in one corner
     * @param cx   The pixel column in the same corner
     * @param rz   The pixel row in the other corner (inclusive)
     * @param c   The pixel column there
     *
     * @see com.apw.apw3.TrakSim#RectFill(int, int, int, int, int)
     */
    void rectFill(int colo, int rx, int cx, int rz, int c);
    
    /**
     * Draws a line that you could totally do on your own if you actually used traksim
     * @param color
     * @param rx
     * @param cx
     * @param rz
     * @param cz
     */
    void drawLine(int color, int rx, int cx, int rz, int cz);

    /**
     * Registers a key event.
     * @param keyCode The key that should be pressed to trigger the action.
     * @param action The action to trigger when the key is pressed.
     */
    void addKeyEvent(int keyCode, Runnable action);

    /**
     * Gets the tiling type of the camera.
     * @return The camera's tiling type.
     */
    byte getTile();

    /**
     * Updates the window width and height to the provided values, should only be called from
     * MrModule.
     * @param width The new window width.
     * @param height The new window height.
     */
    void updateWindowDims(int width, int height);

    /**
     * Returns the height of the JFrame created in MrModule.
     * @return Window height.
     */
    int getWindowHeight();

    /**
     * Returns the width of the JFrame created in MrModule
     * @return window width.
     */
    int getWindowWidth();

    /**
     * Gets the list of <Code>ColoredLine</Code>s rendered in MrModule.
     * @return List of <Code>ColoredLine</Code>s.
     */
    ArrayList<ColoredLine> getLines();

    /**
     * Gets the list of <Code>ColoredRect</Code>s rendered in MrModule.
     * @return List of <Code>ColoredRect</Code>s.
     */
    ArrayList<ColoredRect> getRects();

    /**
     * Clears the list of <Code>ColoredLine</Code>s rendered in MrModule.
     */
    void clearLines();

    /**
     * Clears the rect of <Code>ColoredRect</Code>s rendered in MrModule.
     */
    void clearRects();
}

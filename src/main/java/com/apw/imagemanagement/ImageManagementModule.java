package com.apw.imagemanagement;

import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 *  Controls how the image is processed after acquisition by CamControl.
 * @author
 * @author
 * @author
 * @see ImageManipulator
 */
public class ImageManagementModule implements Module {

	//adjustable variables
    private int viewType = 1;
    private int blackWhiteRasterVersion = 1;
    private double luminanceMultiplier = 1.6;

    //internal variables
    private int width, height;
    private int[] imagePixels;
    private byte tile;
    boolean removeNoise = false;
    boolean dilate = true;

    /**
     * Main constructor for imageManagement
     * @param width the width of the camera image
     * @param height the height of the camera image
     * @param newtile the tiling of the camera: See ImageManipulator.getPos()
     */
    public ImageManagementModule(int width, int height, byte newtile) {
        //Set passed values
        this.width = width;
        this.height = height;
        tile = newtile;

        //Tells ImageManipulator what the Black/White threshold is
        ImageManipulator.setLuminanceMultiplier(luminanceMultiplier);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Serves monochrome raster of given image
     * Formatted in 1D array of bytes
     *
     * Uses only the Green Value for image
     *
     * @param pixels 1D byte array for an image
     * @return monochrome image
     */
    public byte[] getMonochromeRaster(byte[] pixels) {
    	byte[] mono = new byte[width * height];
        ImageManipulator.convertToMonochromeRaster(pixels, mono, height, width, tile);
        return mono;

    }

    /**
     * Serves monochrome raster of given image
     * Formatted in 1D array of bytes
     *
     * Averages the RGB values of image
     *
     * @param pixels 1D byte array for an image
     * @return monochrome image
     */
    public byte[] getMonochrome2Raster(byte[] pixels) {
    	byte[] mono = new byte[width * height];
        ImageManipulator.convertToMonochrome2Raster(pixels, mono, height, width, tile);
        return mono;
    }

    /**
     * Serves black and white raster of given image
     * Formatted in 1D array of bytes
     *
     * Compares luminance of pixels to those around them
     *
     * @param pixels 1D byte array for an image
     * @return black and white image
     */
    public int[] getBlackWhiteRaster(byte[] pixels) {
    	int[] output = new int[width * height];
        if(blackWhiteRasterVersion == 2) {
            //ImageManipulator.convertToBlackWhite2Raster(pixels, output, height, width, tile);
        }
        else {
            ImageManipulator.convertToBlackWhiteRaster(pixels, output, height, width, tile);
        }
        if(removeNoise) {
            output = ImageManipulator.removeNoise(output, height, width);
        }
        if(dilate) {
            output = ImageManipulator.dilate(output, height, width);
        }
        return output;
    }

    /**
     * Serves simple raster of given image
     * Formatted in 1D array of bytes
     *
     * Serves color raster encoded in 1D of values 0-6 with
     *      0 = RED
     *      1 = GREEN
     *      2 = BLUE
     *      3 = WHITE
     *      4 = GREY
     *      5 = BLACK
     *      6 = YELLOW
     *
     * @param pixels 1D byte array for an image
     * @return simple image
     */
    public byte[] getSimpleColorRaster(byte[] pixels) {
    	byte[] simple = new byte[width * height];
        ImageManipulator.convertToSimpleColorRaster(pixels, simple, height, width, tile);
        return simple;


    }

    /**
     * Serves RGB raster of given image
     * Formatted in 1D array of ints
     *
     * Formatted in 0xRRGGBB
     *
     * @param pixels 1D byte array for an image
     * @return RGB image
     */
    public int[] getRGBRaster(byte[] pixels) {
    	int[] rgb = new int[width*height];
        ImageManipulator.convertToRGBRaster(pixels, rgb, height, width, tile);
        return rgb;

    }

    /**
     * Converts into monochrome supported by window
     * Formatted in 1D array of ints
     *
     * Uses only the Green Value for image
     * Formatted in 0xRRGGBB
     *
     * @param pixels 1D byte array for an image
     * @return monochrome image
     */
    public int[] getMonoRGBRaster(byte[] pixels) {
    	int[] rgb = new int[width*height];
    	byte[] mono = getMonochromeRaster(pixels);
        ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
        return rgb;

    }

    /**
     * Converts into simple colors supported by window
     * Formatted in 1D array of ints
     *
     * Serves color raster encoded in 1D of values 0-6 with
     *      0 = RED
     *      1 = GREEN
     *      2 = BLUE
     *      3 = WHITE
     *      4 = GREY
     *      5 = BLACK
     *      6 = YELLOW
     * Formatted in 0xRRGGBB
     *
     * @param pixels 1D byte array for an image
     * @return simple image
     */
    public int[] getSimpleRGBRaster(byte[] pixels) {
    	int[] rgb = new int[width*height];
    	byte[] simple = getSimpleColorRaster(pixels);
        ImageManipulator.convertSimpleToRGB(simple, rgb, simple.length);
        return rgb;

    }

    /**
     * Converts into black and white supported by window
     * Formatted in 1D array of ints
     *
     * Compares luminance of pixels to those around them
     * Formatted in 0xRRGGBB
     *
     * @param pixels 1D byte array for an image
     * @return black and white image
     */
    public int[] getBWRGBRaster(byte[] pixels) {
        int[] output = getBlackWhiteRaster(pixels);
        int[] rgb = new int[width*height];
        ImageManipulator.convertBWToRGB(output, rgb, output.length);
        return rgb;

    }
    
    public int[] getRobertsCross(byte[] pixels) {
    	byte[] mono = new byte[width * height];
    	int[] rgb = new int[width * height];
    	int[] output = new int[width * height];
    	ImageManipulator.convertToMonochrome2Raster(pixels, mono, height, width, tile);
    	ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
    	ImageManipulator.convertToRobertsCrossRaster(rgb, output, height, width);
    	return output;
    }

    /**
     * Converts into road supported by window
     * Formatted in 1D array of ints
     *
     * Compares luminance of pixels to those around them
     * Then makes the road pink by drawing from the bottom of the screen to the first white pixel
     * Formatted in 0xRRGGBB
     *
     * @param pixels 1D byte array for an image
     * @return road finding image
     */
    public int[] getRoad(byte[] pixels){
        int[] output = getBlackWhiteRaster(pixels);

        int[] rgb = new int[width*height];
        ImageManipulator.findRoad(output, rgb, height, width);
        return rgb;
    }

    @Override
    public void initialize(CarControl control) {
    	control.addKeyEvent(KeyEvent.VK_SPACE, () -> changeFilter());
    }
    
    public void changeFilter() {
    	viewType = (viewType) % 6 + 1; 
    	System.out.println(viewType);
    }

    @Override
    public void update(CarControl control) {
        imagePixels = null;
        switch (viewType) {
            case 1:
                imagePixels = getRGBRaster(control.getRecentCameraImage());
                break;
            case 2:
                imagePixels = getMonoRGBRaster(control.getRecentCameraImage());
                break;
            case 3:
                imagePixels = getSimpleRGBRaster(control.getRecentCameraImage());
                break;
            case 4:
                imagePixels = getBlackWhiteRaster(control.getRecentCameraImage());
                break;
            case 5:
                imagePixels = getRoad(control.getRecentCameraImage());
                break;
            case 6:
            	imagePixels = getRobertsCross(control.getRecentCameraImage());
            	break;
            default:
                throw new IllegalStateException("No image management viewType: " + viewType);
        }

        control.setRenderedImage(imagePixels);
        control.setRGBImage(getBlackWhiteRaster(control.getRecentCameraImage()));
        control.setProcessedImage(getSimpleColorRaster(control.getRecentCameraImage()));
    }

    @Override
    public void paint(CarControl control, Graphics g) {
    }
}

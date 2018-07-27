package com.apw.ImageManagement;

import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;

import java.awt.*;

public class ImageManagementModule implements Module {

    private int viewType = 5;
    private int width, height;
    private int[] imagePixels;
    private byte mono[];
    private byte simple[];
    private int rgb[];
    private byte tile;

    public ImageManagementModule(int width, int height, byte newtile) {
        this.width = width;
        this.height = height;
        mono = new byte[width * height];
        simple = new byte[width * height];
        rgb = new int[width * height];
        tile = newtile;
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

    /*Serves monochrome raster of camera feed
     * Formatted in 1D array of bytes*/
    public byte[] getMonochromeRaster(byte[] pixels) {
        ImageManipulator.convertToMonochromeRaster(pixels, mono, height, width, tile);
        return mono;

    }

    public byte[] getMonochrome2Raster(byte[] pixels) {
        ImageManipulator.convertToMonochrome2Raster(pixels, mono, height, width, tile);
        return mono;
    }

    public byte[] getBlackWhiteRaster(byte[] pixels) {

        ImageManipulator.convertToBlackWhiteRaster(pixels, mono, height, width, tile);
        return mono;

    }

    /*Serves color raster encoded in 1D of values 0-5 with
     * 0 = REDnew ImageIcon(displayImage)
     * 1 = GREEN
     * 2 = BLUE
     * 3 = WHITE
     * 4 = GREY
     * 5 = BLACK
     */
    public byte[] getSimpleColorRaster(byte[] pixels) {

        ImageManipulator.convertToSimpleColorRaster(pixels, simple, height, width, tile);
        return simple;


    }

    public int[] getRGBRaster(byte[] pixels) {

        ImageManipulator.convertToRGBRaster(pixels, rgb, height, width, tile);
        return rgb;

    }
    
    public int[] getMonoRGBRaster(byte[] pixels) {

        ImageManipulator.convertToMonochromeRaster(pixels, mono, height, width, tile);
        ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
        return rgb;

    }

    public int[] getSimpleRGBRaster(byte[] pixels) {

        ImageManipulator.convertToSimpleColorRaster(pixels, simple, height, width, tile);
        ImageManipulator.convertSimpleToRGB(simple, rgb, simple.length);
        return rgb;

    }

    public int[] getBWRGBRaster(byte[] pixels) {

        ImageManipulator.convertToBlackWhiteRaster(pixels, mono, height, width, tile);
        ImageManipulator.convertBWToRGB(mono, rgb, mono.length);
        return rgb;

    }
    
    public int[] getRoad(byte[] pixels){
        ImageManipulator.findRoad(pixels, rgb, height, width, tile);
        return rgb;
    }

    @Override
    public void initialize(CarControl control) {

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
                imagePixels = getBWRGBRaster(control.getRecentCameraImage());
                break;
            case 5:
                imagePixels = getRoad(control.getRecentCameraImage());
                break;
            default:
                throw new IllegalStateException("No image management viewType: " + viewType);
        }

        control.setRenderedImage(imagePixels);
        control.setProcessedImage(getSimpleColorRaster(control.getRecentCameraImage()));
    }

    @Override
    public void paint(CarControl control, Graphics g) {
    }
}

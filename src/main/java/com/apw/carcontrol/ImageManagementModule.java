package com.apw.carcontrol;

import com.aparapi.Range;
import com.apw.ImageManagement.ImageManipulator;
import com.apw.ImageManagement.ImagePicker;
import com.apw.fly2cam.FlyCamera;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.invoke.LambdaMetafactory;
import java.util.Arrays;

public class ImageManagementModule implements Module {

    private int viewType = 1;
    private int ncols, nrows;
    private int[] imagePixels;
    private byte mono[];
    private byte simple[];
    private int rgb[];

    public ImageManagementModule(int nrows, int ncols) {
        this.nrows = nrows;
        this.ncols = ncols;
        mono = new byte[nrows * ncols];
        simple = new byte[nrows * ncols];
        rgb = new int[nrows * ncols];
    }

    public int getNrows() {
        return nrows;
    }

    public void setNrows(int nrows) {
        this.nrows = nrows;
    }

    public int getNcols() {
        return ncols;
    }

    public void setNcols(int ncols) {
        this.ncols = ncols;
    }

    /*Serves monochrome raster of camera feed
     * Formatted in 1D array of bytes*/
    public byte[] getMonochromeRaster(byte[] pixels) {
        ImageManipulator.convertToMonochromeRaster(pixels, mono, nrows, ncols);
        return mono;

    }
    public byte[] getMonochrome2Raster(byte[] pixels){
        ImageManipulator.convertToMonochrome2Raster(pixels, mono, nrows, ncols);
        return mono;
    }

    public byte[] getBlackWhiteRaster(byte[] pixels) {

        ImageManipulator.convertToBlackWhiteRaster(pixels, mono, nrows, ncols);
        return mono;

    }

    /*Serves color raster encoded in 1D of values 0-5 with
     * 0 = RED
     * 1 = GREEN
     * 2 = BLUE
     * 3 = WHITE
     * 4 = GREY
     * 5 = BLACK
     */
    public byte[] getSimpleColorRaster(byte[] pixels) {

        ImageManipulator.convertToSimpleColorRaster(pixels, simple, nrows, ncols);
        return simple;


    }

    public int[] getRGBRaster(byte[] pixels) {

        ImageManipulator.convertToRGBRaster(pixels, rgb, nrows, ncols);
        return rgb;

    }

    public int[] getSimpleRGBRaster(byte[] pixels) {

        ImageManipulator.convertToSimpleColorRaster(pixels, simple, nrows, ncols);
        ImageManipulator.convertSimpleToRGB(simple, rgb, simple.length);
        return rgb;

    }

    public int[] getBWRGBRaster(byte[] pixels) {

        ImageManipulator.convertToBlackWhiteRaster(pixels, mono, nrows, ncols);
        ImageManipulator.convertBWToRGB(mono, rgb, mono.length);
        return rgb;

    }

    public int[] getMonoRGBRaster(byte[] pixels) {

        ImageManipulator.convertToMonochromeRaster(pixels, mono, nrows, ncols);
        ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
        return rgb;

    }
    public int[] getMonoRGB2Raster(byte[] pixels) {
        ImageManipulator.convertToMonochromeRaster(pixels, mono, nrows, ncols);
        ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
        return rgb;
    }

    @Override
    public void initializeConstants() {

    }

    @Override
    public void update(CarControl control) {
        imagePixels = null;
        switch(viewType) {
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
                imagePixels = getMonoRGB2Raster(control.getRecentCameraImage());
                break;
            default:
                throw new IllegalStateException("No image management viewType: " + viewType);
        }
    }

    @Override
    public void paint(CarControl control, BufferedImage image) {
        int[] displayPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(imagePixels, 0, displayPixels, 0, displayPixels.length);
    }
}

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
    private int width, height;
    private int ncols, nrows;
    private int[] imagePixels;
    private ImagePicker picker;
    private byte mono[];
    private byte simple[];
    private int rgb[];


    public ImageManagementModule(FlyCamera trakcam) {
        picker = new ImagePicker(trakcam, 30);
        nrows = picker.getNrows();
        ncols = picker.getNcols();
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
    public byte[] getMonochromeRaster() {
        ImageManipulator.convertToMonochromeRaster(picker.getPixels(), mono, nrows, ncols);
        return mono;

    }
    public byte[] getMonochrome2Raster(){
        ImageManipulator.convertToMonochrome2Raster(picker.getPixels(), mono, nrows, ncols);
        return mono;
    }

    public byte[] getBlackWhiteRaster() {

        ImageManipulator.convertToBlackWhiteRaster(picker.getPixels(), mono, nrows, ncols);
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
    public byte[] getSimpleColorRaster() {

        ImageManipulator.convertToSimpleColorRaster(picker.getPixels(), simple, nrows, ncols);
        return simple;


    }

    public int[] getRGBRaster() {

        ImageManipulator.convertToRGBRaster(picker.getPixels(), rgb, nrows, ncols);
        return rgb;

    }

    public int[] getSimpleRGBRaster() {

        ImageManipulator.convertToSimpleColorRaster(picker.getPixels(), simple, nrows, ncols);
        ImageManipulator.convertSimpleToRGB(simple, rgb, simple.length);
        return rgb;

    }

    public int[] getBWRGBRaster() {

        ImageManipulator.convertToBlackWhiteRaster(picker.getPixels(), mono, nrows, ncols);
        ImageManipulator.convertBWToRGB(mono, rgb, mono.length);
        return rgb;

    }

    public int[] getMonoRGBRaster() {

        ImageManipulator.convertToMonochromeRaster(picker.getPixels(), mono, nrows, ncols);
        ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
        return rgb;

    }
    public int[] getMonoRGB2Raster() {
        ImageManipulator.convertToMonochromeRaster(picker.getPixels(), mono, nrows, ncols);
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
                imagePixels = getRGBRaster();
                break;
            case 2:
                imagePixels = getMonoRGBRaster();
                break;
            case 3:
                imagePixels = getSimpleRGBRaster();
                break;
            case 4:
                imagePixels = getBWRGBRaster();
                break;
            case 5:
                imagePixels = getMonoRGB2Raster();
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

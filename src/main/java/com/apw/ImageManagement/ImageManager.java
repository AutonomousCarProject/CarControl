/*ImageManager: Retrieves and preprocesses images from the camera and displays feed onscreen*/

package com.apw.ImageManagement;

import com.apw.fly2cam.FlyCamera;

public class ImageManager {

    int nrows, ncols;
    private ImagePicker picker;
    private byte mono[];
    private byte simple[];
    private int rgb[];

    /*Main*/
    public ImageManager(FlyCamera trakcam) {
        picker = new ImagePicker(trakcam, 30);
        nrows = picker.getNrows();
        ncols = picker.getNcols();
        mono = new byte[nrows*ncols];
        simple = new byte[nrows * ncols];
        rgb = new int[nrows*ncols];
    }

    public int getNrows(){
        return nrows;
    }

    public int getNcols(){
        return ncols;
    }

    /*Serves monochrome raster of camera feed
     * Formatted in 1D array of bytes*/
    public byte[] getMonochromeRaster() {
        ImageManipulator.convertToMonochromeRaster(picker.getPixels(), mono, nrows, ncols);
        return mono;
    }
    public byte[] getBlackWhiteRaster(){
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

    public int[] getRGBRaster(){
        ImageManipulator.convertToRGBRaster(picker.getPixels(), rgb, nrows, ncols);
        return rgb;
    }

    public int[] getSimpleRGBRaster(){
        ImageManipulator.convertToSimpleColorRaster(picker.getPixels(), simple, nrows, ncols);
        ImageManipulator.convertSimpleToRGB(simple, rgb, simple.length);
        return rgb;
    }

    public int[] getBWRGBRaster(){
        ImageManipulator.convertToBlackWhiteRaster(picker.getPixels(), mono, nrows, ncols);
        ImageManipulator.convertBWToRGB(mono, rgb, mono.length);
        return rgb;
    }

    public int[] getMonoRGBRaster(){
        ImageManipulator.convertToMonochromeRaster(picker.getPixels(), mono, nrows, ncols);
        ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
        return rgb;
    }
}

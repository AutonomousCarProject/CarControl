/*ImageManager: Retrieves and preprocesses images from the camera and displays feed onscreen*/

package com.apw.ImageManagement;

//import com.aparapi.Range;
import com.apw.apw3.DriverCons;
import com.apw.fly2cam.FlyCamera;


import java.lang.invoke.LambdaMetafactory;
import java.util.Arrays;

public class ImageManager {

    private static final int cameraWidth = DriverCons.D_ImWi ,cameraHeight = DriverCons.D_ImHi;
    
    int nrows, ncols;
    private ImagePicker picker;
    private byte mono[];
    //private byte simple[];
    private int rgb[];
    private int cameraInt[];
    private byte cameraByte[];

    public ImageManager(FlyCamera trakcam) {
        picker = new ImagePicker(trakcam, 30);
        nrows = picker.getNrows();
        ncols = picker.getNcols();
        mono = new byte[nrows * ncols];
        //simple = new byte[nrows * ncols];
        rgb = new int[nrows * ncols];
        cameraInt = new int[cameraWidth*cameraHeight];
        cameraByte = new byte[cameraWidth*cameraHeight];
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
        ImageManipulator.limitTo(cameraByte,mono,ncols,nrows,cameraWidth,cameraHeight);
        return cameraByte;

    }
    public byte[] getMonochrome2Raster(){
        ImageManipulator.convertToMonochrome2Raster(picker.getPixels(), mono, nrows, ncols);
        ImageManipulator.limitTo(cameraByte,mono,ncols,nrows,cameraWidth,cameraHeight);
        return cameraByte;
    }

    public byte[] getBlackWhiteRaster() {

        ImageManipulator.convertToBlackWhiteRaster(picker.getPixels(), mono, nrows, ncols);
        ImageManipulator.limitTo(cameraByte,mono,ncols,nrows,cameraWidth,cameraHeight);
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
        
        ImageManipulator.convertToSimpleColorRaster(picker.getPixels(), mono, nrows, ncols);
        ImageManipulator.limitTo(cameraByte,mono,ncols,nrows,cameraWidth,cameraHeight);
        return mono;
    }

    public int[] getRGBRaster() {

        ImageManipulator.convertToRGBRaster(picker.getPixels(), rgb, nrows, ncols);
        ImageManipulator.limitTo(cameraInt,rgb,ncols,nrows,cameraWidth,cameraHeight);
        return rgb;
    }
    public int[] getRGBRasterFull(){
        
        ImageManipulator.convertToRGBRaster(picker.getPixels(), rgb, nrows, ncols);
        return rgb;
    }
    public byte[] getMonochromeRasterFull(){

        ImageManipulator.convertToMonochromeRaster(picker.getPixels(),mono,nrows,ncols);
        return mono;
    }
    public byte[] getSimpleColorRasterFull(){

        ImageManipulator.convertToSimpleColorRaster(picker.getPixels(),mono,nrows,ncols);
        return mono;
    }
    public byte[] getBlackWhiteRasterFull(){
        
        ImageManipulator.convertToBlackWhiteRaster(picker.getPixels(),mono,nrows,ncols);
        return mono;
    }
    public byte[] convertToMonochrome2RasterFull(){
        
        ImageManipulator.convertToMonochrome2Raster(picker.getPixels(),mono,nrows,ncols);
        return mono;
    }



    public int[] getSimpleRGBRaster() {

        getSimpleColorRasterFull();
        ImageManipulator.convertSimpleToRGB(mono, rgb, mono.length);
        return rgb;

    }

    public int[] getBWRGBRaster() {

        getBlackWhiteRasterFull();
        ImageManipulator.convertBWToRGB(mono, rgb, mono.length);
        return rgb;

    }

    public int[] getMonoRGBRaster() {

        getMonochromeRasterFull();
        ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
        return rgb;

    }
    public int[] getMonoRGB2Raster(){
        
        convertToMonochrome2RasterFull();
        ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
        return rgb;
    }


}

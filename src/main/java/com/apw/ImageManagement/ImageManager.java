/*ImageManager: Retrieves and preprocesses images from the camera and displays feed onscreen*/

package com.apw.ImageManagement;

//import com.aparapi.Range;
import com.apw.apw3.DriverCons;
import com.apw.fly2cam.FlyCamera;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.invoke.LambdaMetafactory;
import java.util.Arrays;

public class ImageManager {

    private static final int cameraWidth = DriverCons.D_ImWi ,cameraHeight = DriverCons.D_ImHi;
    
    int nrows, ncols;
    private ImagePicker picker;
    private byte tile;
    //private byte mono[];
    //private boolean isRunning;
    //private int[] rgbIn
    //private int[] rgbOut,simpleOut, monoOut, BWOut, mono2Out , cameraOut;
    //private int[] rgb;
    //private int[] cameraInt;
    //private byte[] cameraByte;

    public ImageManager(FlyCamera trakcam) {
        picker = new ImagePicker(trakcam, 30);
        nrows = picker.getNrows();
        ncols = picker.getNcols();
        byte tile =  (byte) (trakcam.PixTile()-1);
        //mono = new byte[nrows * ncols];
        //rgb = new int[nrows*ncols];
        //cameraInt = new int[cameraWidth*cameraHeight];
        //cameraByte = new byte[cameraWidth*cameraHeight];
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
        byte[] mono = new byte[nrows * ncols];
        //int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        ImageManipulator.convertToMonochromeRaster(picker.getPixels(), mono, nrows, ncols, tile);
        ImageManipulator.limitTo(cameraByte,mono,ncols,nrows,cameraWidth,cameraHeight);
        return cameraByte;

    }
    public byte[] getMonochrome2Raster(){
        byte[] mono = new byte[nrows * ncols];
        //int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        ImageManipulator.convertToMonochrome2Raster(picker.getPixels(), mono, nrows, ncols, tile);
        ImageManipulator.limitTo(cameraByte,mono,ncols,nrows,cameraWidth,cameraHeight);
        return cameraByte;
    }

    public byte[] getBlackWhiteRaster() {
        byte[] mono = new byte[nrows * ncols];
        //int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        ImageManipulator.convertToBlackWhiteRaster(picker.getPixels(), mono, nrows, ncols, tile);
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
        byte[] mono = new byte[nrows * ncols];
        //int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        ImageManipulator.convertToSimpleColorRaster(picker.getPixels(), mono, nrows, ncols, tile);
        ImageManipulator.limitTo(cameraByte,mono,ncols,nrows,cameraWidth,cameraHeight);
        return mono;
    }

    public int[] getRGBRaster() {
        //byte[] mono = new byte[nrows * ncols];
        int[] rgb = new int[nrows*ncols];
        int[] cameraInt = new int[cameraWidth*cameraHeight];
        //byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        ImageManipulator.convertToRGBRaster(picker.getPixels(), rgb, nrows, ncols, tile);
        ImageManipulator.limitTo(cameraInt,rgb,ncols,nrows,cameraWidth,cameraHeight);
        return rgb;
    }
    public int[] getRGBRasterFull(){
        //byte[] mono = new byte[nrows * ncols];
        int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        //byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        ImageManipulator.convertToRGBRaster(picker.getPixels(), rgb, nrows, ncols, tile);
        return rgb;
    }
    public byte[] getMonochromeRasterFull(){
        byte[] mono = new byte[nrows * ncols];
        //int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        //byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        ImageManipulator.convertToMonochromeRaster(picker.getPixels(),mono,nrows,ncols, tile);
        return mono;
    }
    public byte[] getSimpleColorRasterFull(){
        byte[] mono = new byte[nrows * ncols];
        //int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        //byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        ImageManipulator.convertToSimpleColorRaster(picker.getPixels(),mono,nrows,ncols, tile);
        return mono;
    }
    public byte[] getBlackWhiteRasterFull(){
        byte[] mono = new byte[nrows * ncols];
        //int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        //byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        ImageManipulator.convertToBlackWhiteRaster(picker.getPixels(),mono,nrows,ncols, tile);
        return mono;
    }
    public byte[] convertToMonochrome2RasterFull(){
        byte[] mono = new byte[nrows * ncols];
        //int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        //byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        ImageManipulator.convertToMonochrome2Raster(picker.getPixels(),mono,nrows,ncols, tile);
        return mono;
    }



    public int[] getSimpleRGBRaster() {
        byte[] mono;
        int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        //byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        mono = getSimpleColorRasterFull();
        ImageManipulator.convertSimpleToRGB(mono,rgb , mono.length);
        return rgb;

    }

    public int[] getBWRGBRaster() {
        byte[] mono;
        int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        //byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        mono = getBlackWhiteRasterFull();
        ImageManipulator.convertBWToRGB(mono, rgb, mono.length);
        return rgb;

    }

    public int[] getMonoRGBRaster() {
        byte[] mono;
        int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        //byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        mono = getMonochromeRasterFull();
        ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
        return rgb;

    }
    public int[] getMonoRGB2Raster(){
        byte[] mono;
        int[] rgb = new int[nrows*ncols];
        //int[] cameraInt = new int[cameraWidth*cameraHeight];
        //byte[] cameraByte = new byte[cameraWidth*cameraHeight];
        mono = convertToMonochrome2RasterFull();
        ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
        return rgb;
    }

    public int[] getRoad(){
        int road[] = new int[nrows*ncols];
        ImageManipulator.findRoad(picker.getPixels(), road, nrows, ncols, tile);
        return road;
    }

    public int[] getCameraRaw() {
        byte[] mono = new byte[nrows * ncols];
        int[] rgb = new int[nrows*ncols];
        int[] cameraInt = new int[cameraWidth*cameraHeight];
        //byte[] cameraByte = new byte[cameraWidth*cameraHeight];

        //ImageManipulator.convertToRGBRaster(picker.getPixels(), rgb, nrows, ncols);

    	ImageManipulator.convertToBlackWhiteRaster(picker.getPixels(), mono, nrows, ncols, tile);
    	ImageManipulator.convertBWToRGB(mono, rgb, mono.length);
    	
    	ImageManipulator.limitTo(cameraInt , rgb, ncols, nrows, cameraWidth, cameraHeight);

        return cameraInt;
    }

    /**
     * Converts a RGB pixel array to BufferedImage for painting. Adapted from
     * example code found on StackExchange.
     *
     * @param pixels The pixel array
     *
     * @param width  Its width
     *
     * @param height Its height
     *
     * @return The BufferedImage result
     */
    public BufferedImage Int2BufImg(int[] pixels, int width, int height) // (in DrDemo)
            throws IllegalArgumentException {
    	System.out.println(pixels);
        int lxx = 0;
        int[] theData = null; // Raster raz = null; DataBufferInt DBI = nell;
        BufferedImage bufIm = null;
        if (pixels != null)
            lxx = pixels.length;
        if (lxx == 0)
            return null;
        if (bufIm == null) // (should be never)
            bufIm = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        theData = ((DataBufferInt) bufIm.getRaster().getDataBuffer()).getData();
        System.arraycopy(pixels, 0, theData, 0, lxx);
        return bufIm;
    } // ~Int2BufImg

    public BufferedImage getRoadLines() {
        BufferedImage roadLines = Int2BufImg(getCameraRaw(), 640, 480);
        Color fill = new Color (0, 0, 0);
        for (int i = 0; i < 640; i++) {
            for (int j = 0; j < 480; j++) {
                if(j < 240 || j > 450) {
                    roadLines.setRGB(i, j, fill.getRGB());
                }
            }
        }
        return roadLines;
    }
    
    public int[] getRoadLinesArray() {
    	int[] theData = ((DataBufferInt) getRoadLines().getRaster().getDataBuffer()).getData();
    	return theData;
    }


}

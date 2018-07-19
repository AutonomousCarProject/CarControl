/*ImageManager: Retrieves and preprocesses images from the camera and displays feed onscreen*/

package com.apw.ImageManagement;

import com.aparapi.Range;
import com.apw.fly2cam.FlyCamera;
import com.apw.gpu.MonochromeRasterKernel;
import com.apw.gpu.RGBRasterKernel;
import com.apw.gpu.SimpleColorRasterKernel;

public class ImageManager {

    int nrows, ncols;
    private ImagePicker picker;
    private byte mono[];
    private byte simple[];
    private int rgb[];
    private boolean runOnGpu = true;
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
        if (runOnGpu) {
            MonochromeRasterKernel kernel = new MonochromeRasterKernel(picker.getPixels(), mono, nrows, ncols);
            kernel.execute(Range.create(nrows * ncols));
            kernel.dispose();
            return kernel.getMono();
        } else {
            ImageManipulator.convertToMonochromeRaster(picker.getPixels(), mono, nrows, ncols);
            return mono;
        }
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
        if (runOnGpu) {
            SimpleColorRasterKernel kernel = new SimpleColorRasterKernel(picker.getPixels(), simple, nrows, ncols);
            kernel.execute(Range.create(nrows * ncols));
            kernel.dispose();
            return kernel.getSimple();
        } else {
            ImageManipulator.convertToSimpleColorRaster(picker.getPixels(), simple, nrows, ncols);
            return simple;
        }

    }

    public int[] getRGBRaster() {
        if (runOnGpu) {
            RGBRasterKernel kernel = new RGBRasterKernel(picker.getPixels(), rgb, nrows, ncols);
            kernel.execute(Range.create(nrows * ncols));
            kernel.dispose();
            return kernel.getRgb();
        } else {
            ImageManipulator.convertToRGBRaster(picker.getPixels(), rgb, nrows, ncols);
            return rgb;
       }
    }

    public static void convertSimpleToRGB(byte[] simpleByte, int[]simpleRGB, int length){
        for(int i = 0; i < length; i++){
            switch(simpleByte[i]){
                case 0:
                    simpleRGB[i] = 0xFF0000;
                    break;
                case 1:
                    simpleRGB[i] = 0x00FF00;
                    break;
                case 2:
                    simpleRGB[i] = 0x0000FF;
                    break;
                case 3:
                    simpleRGB[i] = 0xFFFFFF;
                    break;
                case 4:
                    simpleRGB[i] = 0x808080;
                    break;
                case 5:
                    simpleRGB[i] = 0x000000;
                    break;
            }
        }
    }
}

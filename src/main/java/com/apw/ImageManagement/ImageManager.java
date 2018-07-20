/*ImageManager: Retrieves and preprocesses images from the camera and displays feed onscreen*/

package com.apw.ImageManagement;

import com.aparapi.Range;
import com.apw.fly2cam.FlyCamera;
import com.apw.gpu.MonochromeRasterKernel;
import com.apw.gpu.RGBRasterKernel;
import com.apw.gpu.SimpleColorRasterKernel;

import java.util.Arrays;

public class ImageManager {

    enum Processsor {
        CPU,
        GPU
    }

    int nrows, ncols;
    private ImagePicker picker;
    private byte mono[];
    private byte simple[];
    private int rgb[];
    private Processsor processsor;
    MonochromeRasterKernel  monoRasterKernel;
    SimpleColorRasterKernel simpleRasterKernel;
    RGBRasterKernel         rgbRasterKernel;
    /*Main*/
    public ImageManager(FlyCamera trakcam) {
        picker = new ImagePicker(trakcam, 30);
        nrows = picker.getNrows();
        ncols = picker.getNcols();
        mono = new byte[nrows*ncols];
        simple = new byte[nrows * ncols];
        rgb = new int[nrows*ncols];
        monoRasterKernel   = new MonochromeRasterKernel (picker.getPixels(), mono,   nrows, ncols);
        simpleRasterKernel = new SimpleColorRasterKernel(picker.getPixels(), simple, nrows, ncols);
        rgbRasterKernel    = new RGBRasterKernel        (picker.getPixels(), rgb,    nrows, ncols);
    }

    public int getNrows(){
        return nrows;
    }

    public int getNcols(){
        return ncols;
    }

    public void runOnGpu(boolean value)
    {
        if (value)
            processsor = Processsor.GPU;
        else
            processsor = Processsor.CPU;
    }

    /*Serves monochrome raster of camera feed
     * Formatted in 1D array of bytes*/
    public byte[] getMonochromeRaster() {
        if (processsor == Processsor.GPU) {
            monoRasterKernel.setValues(picker.getPixels(), mono, nrows, ncols);
            monoRasterKernel.execute(Range.create(nrows));
            monoRasterKernel.dispose();
            return monoRasterKernel.getMono();
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
        if (processsor == Processsor.GPU) {
            //simpleRasterKernel.setValues(picker.getPixels(), simple, nrows, ncols);
            simpleRasterKernel.setExplicit(true);
            simpleRasterKernel.put(picker.getPixels()).put(simple);
            System.out.println(Arrays.toString(simple).substring(0, 30));
            simpleRasterKernel.execute(Range.create(nrows)).get(simpleRasterKernel.getSimple());
            simpleRasterKernel.dispose();
            System.out.println(Arrays.toString(simple).substring(0, 30));
            return simpleRasterKernel.getSimple();
        } else {
            ImageManipulator.convertToSimpleColorRaster(picker.getPixels(), simple, nrows, ncols);
            return simple;
        }

    }

    public int[] getRGBRaster() {
        if (processsor == Processsor.GPU) {
            rgbRasterKernel.setValues(picker.getPixels(), rgb, nrows, ncols);
            rgbRasterKernel.execute(Range.create(nrows));
            rgbRasterKernel.dispose();
            return rgbRasterKernel.getRgb();
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

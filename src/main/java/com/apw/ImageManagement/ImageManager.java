/*ImageManager: Retrieves and preprocesses images from the camera and displays feed onscreen*/

package com.apw.ImageManagement;

import com.aparapi.Range;
import com.apw.fly2cam.FlyCamera;
import com.apw.gpu.*;

import java.lang.invoke.LambdaMetafactory;
import java.util.Arrays;

public class ImageManager {

    int nrows, ncols;
    private ImagePicker picker;
    private byte mono[];
    private byte simple[];
    private int rgb[];
    private Processsor processsor;
    private MonochromeRasterKernel monoRasterKernel;
    private SimpleColorRasterKernel simpleColorRasterKernel;
    private RGBRasterKernel rgbRasterKernel;
    private BlackWhiteRasterKernel bwRasterKernel;
    private MonoToRGBKernel monoToRgbKernel;
    private SimpleToRGBKernel simpleToRgbKernel;
    private BWToRGBKernel bwToRgbKernel;
    /*Main*/
    public ImageManager(FlyCamera trakcam) {
        picker = new ImagePicker(trakcam, 30);
        nrows = picker.getNrows();
        ncols = picker.getNcols();
        mono = new byte[nrows * ncols];
        simple = new byte[nrows * ncols];
        rgb = new int[nrows * ncols];
        monoRasterKernel = new MonochromeRasterKernel(picker.getPixels(), mono, nrows, ncols);
        simpleColorRasterKernel = new SimpleColorRasterKernel(picker.getPixels(), simple, nrows, ncols);
        rgbRasterKernel = new RGBRasterKernel(picker.getPixels(), rgb, nrows, ncols);
        bwRasterKernel = new BlackWhiteRasterKernel(picker.getPixels(), mono, nrows, ncols);
        monoToRgbKernel = new MonoToRGBKernel(mono, rgb, mono.length);
        simpleToRgbKernel = new SimpleToRGBKernel(simple, rgb, simple.length);
        bwToRgbKernel = new BWToRGBKernel(mono, rgb, mono.length);
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

    public void runOnGpu(boolean value) {
        if (value)
            processsor = Processsor.GPU;
        else
            processsor = Processsor.CPU;
    }

    /*Serves monochrome raster of camera feed
     * Formatted in 1D array of bytes*/
    public byte[] getMonochromeRaster() {
        if (processsor == Processsor.GPU) {
            monoRasterKernel.execute(Range.create2D(nrows, ncols));
            return monoRasterKernel.getMono();
        } else {
            if (monoRasterKernel.isExecuting())
                monoRasterKernel.dispose();
            ImageManipulator.convertToMonochromeRaster(picker.getPixels(), mono, nrows, ncols);
            return mono;
        }
    }

    public byte[] getBlackWhiteRaster() {
        if (processsor == Processsor.GPU) {
            bwRasterKernel.execute(Range.create2D(nrows, ncols));
            return bwRasterKernel.getMono();
        } else {
            if (bwRasterKernel.isExecuting())
                bwRasterKernel.dispose();
            ImageManipulator.convertToBlackWhiteRaster(picker.getPixels(), mono, nrows, ncols);
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
            simpleColorRasterKernel.execute(Range.create2D(nrows, ncols));
            return simpleColorRasterKernel.getSimple();
        } else {
            if (simpleColorRasterKernel.isExecuting())
                simpleColorRasterKernel.dispose();
            ImageManipulator.convertToSimpleColorRaster(picker.getPixels(), simple, nrows, ncols);
            return simple;
        }

    }

    public int[] getRGBRaster() {
        if (processsor == Processsor.GPU) {
            rgbRasterKernel.execute(Range.create2D(nrows, ncols));
            return rgbRasterKernel.getRgb();
        } else {
            if (rgbRasterKernel.isExecuting())
                rgbRasterKernel.dispose();
            ImageManipulator.convertToRGBRaster(picker.getPixels(), rgb, nrows, ncols);
            return rgb;
        }
    }

    public int[] getSimpleRGBRaster() {
        if (processsor == Processsor.GPU) {
            simpleColorRasterKernel.execute(Range.create2D(nrows, ncols));
            simpleToRgbKernel.execute(Range.create(simple.length));
            return simpleToRgbKernel.getSimpleRGB();
        } else {
            if (simpleColorRasterKernel.isExecuting())
                simpleColorRasterKernel.dispose();
            if (simpleToRgbKernel.isExecuting())
                simpleColorRasterKernel.dispose();
            ImageManipulator.convertToSimpleColorRaster(picker.getPixels(), simple, nrows, ncols);
            ImageManipulator.convertSimpleToRGB(simple, rgb, simple.length);
            return rgb;
        }
    }

    public int[] getBWRGBRaster() {
        if (processsor == Processsor.GPU) {
            bwRasterKernel.execute(Range.create2D(nrows, ncols));
            bwToRgbKernel.execute(Range.create(mono.length));
            return bwToRgbKernel.getMono();
        } else {
            if (bwRasterKernel.isExecuting())
                bwRasterKernel.dispose();
            if (bwToRgbKernel.isExecuting())
                bwToRgbKernel.dispose();
            ImageManipulator.convertToBlackWhiteRaster(picker.getPixels(), mono, nrows, ncols);
            ImageManipulator.convertBWToRGB(mono, rgb, mono.length);
            return rgb;
        }
    }

    public int[] getMonoRGBRaster() {
        if (processsor == Processsor.GPU) {
            monoRasterKernel.execute(Range.create(mono.length));
            monoToRgbKernel.execute(Range.create2D(nrows, ncols));
            return monoToRgbKernel.getRGB();
        } else {
            if (monoRasterKernel.isExecuting())
                monoRasterKernel.dispose();
            if (monoToRgbKernel.isExecuting())
                monoToRgbKernel.dispose();
            ImageManipulator.convertToMonochromeRaster(picker.getPixels(), mono, nrows, ncols);
            ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
            return rgb;
        }
    }

    public void disposeAllKernals() {
        monoRasterKernel.dispose();
        simpleColorRasterKernel.dispose();
        rgbRasterKernel.dispose();
        bwRasterKernel.dispose();
        monoToRgbKernel.dispose();
        simpleToRgbKernel.dispose();
        bwToRgbKernel.dispose();
    }

    enum Processsor {
        CPU,
        GPU
    }
}

package com.apw.gpu;

import com.aparapi.Range;
import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;
import com.apw.imagemanagement.ImageManipulator;

import java.awt.*;
import java.awt.event.KeyEvent;

public class GPUImageModule implements Module {

    boolean removeNoise = false;
    boolean dilate = true;
    //adjustable variables
    private int viewType = 6;
    private int blackWhiteRasterVersion = 1;
    private double luminanceMultiplier = 1.6;
    private int width, height;
    private int[] imagePixels;
    private byte tile;
    private int frameWidth = 640;
    private SimpleColorRasterKernel simpleColorRasterKernel;
    private BlackWhiteRasterKernel blackWhiteRasterKernel;
    private BlackWhiteRaster2Kernel blackWhiteRaster2Kernel;
    private RGBRasterKernel rgbRasterKernel;
    private MonochromeRasterKernel monochromeRasterKernel;
    private Monochrome2RasterKernel monochrome2RasterKernel;
    private MonoToRGBKernel monoToRGBKernel;
    private SimpleToRGBKernel simpleToRGBKernel;
    private RemoveNoiseKernel removeNoiseKernel;
    private DilateKernel dilateKernel;
    private BWToRGBKernel bwToRGBKernel;
    private RobertsCrossRasterKernel robertsCrossRasterKernel;

    public GPUImageModule(int width, int height, byte newtile) {
        this.width = width;
        this.height = height;
        tile = newtile;
        ImageManipulator.setLuminanceMultiplier(luminanceMultiplier);

        simpleColorRasterKernel = new SimpleColorRasterKernel();
        blackWhiteRaster2Kernel = new BlackWhiteRaster2Kernel();
        blackWhiteRasterKernel = new BlackWhiteRasterKernel();
        rgbRasterKernel = new RGBRasterKernel();
        monochromeRasterKernel = new MonochromeRasterKernel();
        monochrome2RasterKernel = new Monochrome2RasterKernel();
        monoToRGBKernel = new MonoToRGBKernel();
        simpleToRGBKernel = new SimpleToRGBKernel();
        removeNoiseKernel = new RemoveNoiseKernel();
        dilateKernel = new DilateKernel();
        bwToRGBKernel = new BWToRGBKernel();
        robertsCrossRasterKernel = new RobertsCrossRasterKernel();
    }

    @Override
    public void initialize(CarControl control) {
        control.addKeyEvent(KeyEvent.VK_SPACE, this::changeFilter);
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
        byte[] mono = new byte[width * height];
        monochromeRasterKernel.setValues(pixels, mono, height, width, tile);
        monochromeRasterKernel.execute(Range.create2D(height, width));
        return monochromeRasterKernel.getMono();

    }

    public byte[] getMonochrome2Raster(byte[] pixels) {
        byte[] mono = new byte[width * height];
        monochrome2RasterKernel.setValues(pixels, mono, height, width, tile);
        monochrome2RasterKernel.execute(Range.create2D(height, width));
        return monochrome2RasterKernel.getMono();
    }

    public byte[] getBlackWhiteRaster(byte[] pixels) {
        byte[] mono = new byte[width * height];
        blackWhiteRasterKernel.setValues(pixels, mono, height, width, tile);
        blackWhiteRasterKernel.execute(Range.create2D(height, width));
        return blackWhiteRasterKernel.getMono();
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
        byte[] simple = new byte[width * height];
        simpleColorRasterKernel.setValues(pixels, simple, height, width, tile);
        simpleColorRasterKernel.execute(Range.create2D(height, width));
        return simpleColorRasterKernel.getSimple();
    }

    public int[] getRGBRaster(byte[] pixels) {
        int[] rgb = new int[width * height];
        rgbRasterKernel.setValues(pixels, rgb, height, width, tile);
        rgbRasterKernel.execute(Range.create2D(height, width));
        return rgbRasterKernel.getRgb();
    }

    public int[] getMonoRGBRaster(byte[] pixels) {
        int[] rgb = new int[width * height];
        byte[] mono = new byte[width * height];
        monochromeRasterKernel.setValues(pixels, mono, height, width, tile);
        monoToRGBKernel.setValues(mono, rgb, monochromeRasterKernel.getMono().length);
        monochromeRasterKernel.execute(Range.create2D(height, width));
        monoToRGBKernel.execute(Range.create(monochromeRasterKernel.getMono().length));
        return monoToRGBKernel.getRGB();
    }

    public int[] getSimpleRGBRaster(byte[] pixels) {
        int[] rgb = new int[width * height];
        byte[] simple = new byte[width * height];
        simpleColorRasterKernel.setValues(pixels, simple, height, width, tile);
        simpleToRGBKernel.setValues(simple, rgb, simpleColorRasterKernel.getSimple().length);
        simpleColorRasterKernel.execute(Range.create2D(height, width));
        simpleToRGBKernel.execute(Range.create(simpleColorRasterKernel.getSimple().length));
        return rgb;
    }

    public int[] getBWRGBRaster(byte[] pixels) {
        int[] output = new int[width * height];
        int[] rgb = new int[width * height];
        if (blackWhiteRasterVersion == 2) {
            //ImageManipulator.convertToBlackWhite2Raster(pixels, output, height, width, tile);
            //blackWhiteRaster2Kernel.setValues(pixels, output, height, width, tile);
            //blackWhiteRaster2Kernel.execute(Range.create2D(height, width));
            //output = blackWhiteRaster2Kernel.getMono();
        } else {
            //blackWhiteRasterKernel.setValues(pixels, output, height, width, tile);
            //blackWhiteRasterKernel.execute(Range.create2D(height, width));
            //ImageManipulator.convertToBlackWhiteRaster(pixels, rgb, height, width, frameWidth, tile);
            //output = blackWhiteRasterKernel.getMono();
        }
        if (removeNoise) {
            removeNoiseKernel.setValues(output, height, width);
            removeNoiseKernel.execute(Range.create2D(height, width));
            output = removeNoiseKernel.getEroded();
        }
        if (dilate) {
            dilateKernel.setValues(output, height, width);
            dilateKernel.execute(Range.create2D(height, width));
            output = dilateKernel.getDilated();
        }
        //bwToRGBKernel.setValues(output, rgb, output.length);
        //bwToRGBKernel.execute(Range.create(output.length));
        //return bwToRGBKernel.getRgb();
        return rgb;
    }

    public int[] getRobertsCross(byte[] pixels) {
        byte[] mono = new byte[width * height];
        int[] rgb = new int[width * height];
        int[] output = new int[width * height];
        //ImageManipulator.convertToMonochrome2Raster(pixels, mono, height, width, tile);
        monochrome2RasterKernel.setValues(pixels, mono, height, width, tile);
        monochrome2RasterKernel.execute(Range.create2D(height, width));
        //ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
        monoToRGBKernel.setValues(mono, rgb, mono.length);
        monoToRGBKernel.execute(Range.create(mono.length));
        //ImageManipulator.convertToRobertsCrossRaster(rgb, output, height, width);
        robertsCrossRasterKernel.setValues(rgb, output, height, width);
        robertsCrossRasterKernel.execute(Range.create2D(height, width));
        return output;
    }

    public int[] getRoad(byte[] pixels) {
        int[] output = new int[width * height];
        int[] rgb = new int[width * height];
        //ImageManipulator.convertToBlackWhiteRaster(pixels, output, height, width, frameWidth, tile);
        //blackWhiteRasterKernel.setValues(pixels, output, height, width, tile);
        //blackWhiteRasterKernel.execute(Range.create2D(height, width));
        if (removeNoise) {
            //ImageManipulator.removeNoise(output, height, width);
            removeNoiseKernel.setValues(output, height, width);
            removeNoiseKernel.execute(Range.create2D(height, width));
        }
        if (dilate) {
            //ImageManipulator.dilate(output, height, width);
            dilateKernel.setValues(output, height, width);
            dilateKernel.execute(Range.create2D(height, width));
        }
        ImageManipulator.findRoad(output, rgb, height, width);
        return rgb;
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
                imagePixels = getBWRGBRaster(control.getRecentCameraImage());
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
        control.setRGBImage(getBWRGBRaster(control.getRecentCameraImage()));
        control.setProcessedImage(getSimpleColorRaster(control.getRecentCameraImage()));
    }

    @Override
    public void paint(CarControl control, Graphics g) {
    }
}

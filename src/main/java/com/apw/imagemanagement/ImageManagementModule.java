package com.apw.imagemanagement;

import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 *  Controls how the image is processed after acquisition by CamControl.
 * @author Riley J
 * @author Joshua B
 * @author Nathan P
 * @see ImageManipulator
 */
public class ImageManagementModule implements Module {

	//adjustable variables
    private int viewType = 4;
    private int blackWhiteRasterVersion = 1;
    private double luminanceMultiplier = 1.5;


    //internal variables
    private int width, height;
    //public int[] imagePixels;
    public int[] displayPixels;
    public int[] BWPixels;
    public byte[] simplePixels;
    private byte tile;
    private int frameWidth = 640;
    boolean removeNoise = true;
    boolean dilate = false;
    
    ImageThread displayThread = new ImageThread(this, 1,true);
    ImageThread BWThread = new ImageThread(this, 1,false);
    ImageThread simpleThread = new ImageThread(this, 2,false);

    //Image Variables
    byte[] R;
    byte[] G;
    byte[] B;

    /**
     * Main constructor for imageManagement
     * @param width the width of the camera image
     * @param height the height of the camera image
     * @param newtile the tiling of the camera: See ImageManipulator.getPos()
     */
    public ImageManagementModule(int width, int height, byte newtile) {
        //Set passed values
        this.width = width;
        this.height = height;
        tile = newtile;

        //Create image arrays
        R = new byte[width*height];
        G = new byte[width*height];
        B = new byte[width*height];

        //Initialize camera arrays
        displayPixels = new int[width*height];
        BWPixels = new int[width*height];
        simplePixels = new byte[width*height];


        //Tells ImageManipulator what the Black/White threshold is
        ImageManipulator.setLuminanceMultiplier(luminanceMultiplier);
        
        displayThread.start();
        BWThread.start();
        simpleThread.start();

    }

    /**
     * Takes in a Bayer8 image and fills an array with the red values, another with the green
     * values and a third with the blue values
     * @param pixels Bayer8 image
     */

    public void setupArrays(byte[] pixels){
        int nrows = height;
        int ncols = width;
        int bit0 = ImageManipulator.getBit(tile,0);
        int bit1 = ImageManipulator.getBit(tile,1);
        int bit2 = ImageManipulator.getBit(tile,2);
        int offset = 2*(1-bit2)*ncols*bit1+bit0+bit1*bit2;
        int rowV = (4-bit2)*ncols;
        int halfRowV = (4-bit2)*ncols/2;
        int halfRow = 2*ncols;
        int halfRow1 = halfRow+1;
        int colV = 2+bit2;
        int max = rowV*nrows;
        int sum = 0;
        if (tile == 0){
            for(int r = 0;r<max;r+=rowV){
                for(int c = 0;c<halfRowV;c+=colV){
                    R[sum] = pixels[r+c];
                    G[sum] = pixels[r+c+1];
                    B[sum] = pixels[r+c+halfRow1];
                    sum++;
                }
            }
        }else if (tile == 1){
            for(int r = 0;r<max;r+=rowV){
                for(int c = 0;c<halfRowV;c+=colV){
                    R[sum] = pixels[r+c+1];
                    G[sum] = pixels[r+c];
                    B[sum] = pixels[r+c+halfRow];
                    sum++;
                }
            }
        }else if (tile == 2){
            for(int r = 0;r<max;r+=rowV){
                for(int c = 0;c<halfRowV;c+=colV){
                    R[sum] = pixels[r+c+halfRow];
                    G[sum] = pixels[r+c];
                    B[sum] = pixels[r+c+1];
                    sum++;
                }
            }
        }else if (tile == 3){
            for(int r = 0;r<max;r+=rowV){
                for(int c = 0;c<halfRowV;c+=colV){
                    R[sum] = pixels[r+c+halfRow1];
                    G[sum] = pixels[r+c+1];
                    B[sum] = pixels[r+c];
                    sum++;
                }
            }
        }else if (tile == 4){
            for(int r = 0;r<max;r+=rowV){
                for(int c = 0;c<rowV;c+=colV){
                    R[sum] = pixels[r+c];
                    G[sum] = pixels[r+c+1];
                    B[sum] = pixels[r+c+2];
                    sum++;
                }
            }
        }
    }
    public void removeShadows(){
        double Y, Y1, Y2, U, U1, U2, V, V1, V2;
        int dif = 11;
        for(int r = height-2;r>0;r--){
            for(int c = frameWidth-2; c>0;c--){
                Y = (R[r*width+c]&0xFF) *  .299000 + (G[r*width+c]&0xFF) *  .587000 + (B[r*width+c]&0xFF) *  .114000;
                Y1 = (R[r*width+c+1]&0xFF) *  .299000 + (G[r*width+c+1]&0xFF) *  .587000 + (B[r*width+c+1]&0xFF) *  .114000;
                Y2 = (R[(r+1)*width+c]&0xFF) *  .299000 + (G[(r+1)*width+c]&0xFF) *  .587000 + (B[(r+1)*width+c]&0xFF) *  .114000;
                U = (R[r*width+c]&0xFF) * -.168736 + (G[r*width+c]&0xFF) * -.331264 + (B[r*width+c]&0xFF) *  .500000 + 128;
                U1 = (R[r*width+c+1]&0xFF) * -.168736 + (G[r*width+c+1]&0xFF) * -.331264 + (B[r*width+c+1]&0xFF) *  .500000 + 128;
                U2 = (R[(r+1)*width+c]&0xFF) * -.168736 + (G[(r+1)*width+c]&0xFF) * -.331264 + (B[(r+1)*width+c]&0xFF) *  .500000 + 128;
                V = (R[r*width+c]&0xFF) *  .500000 + (G[r*width+c]&0xFF) * -.418688 + (B[r*width+c]&0xFF) * -.081312 + 128;
                V1 = (R[r*width+c+1]&0xFF) *  .500000 + (G[r*width+c+1]&0xFF) * -.418688 + (B[r*width+c+1]&0xFF) * -.081312 + 128;
                V2 = (R[(r+1)*width+c]&0xFF) *  .500000 + (G[(r+1)*width+c]&0xFF) * -.418688 + (B[(r+1)*width+c]&0xFF) * -.081312 + 128;
                if(Math.abs(U1-U)<dif&&Math.abs(V1-V)<dif){
                    Y=Y1;
                    U=U1;
                    V=V1;
                }else if(Math.abs(U2-U)<dif&&Math.abs(V2-V)<dif){
                    Y=Y2;
                    U=U2;
                    V=V2;
                }
                R[r*width+c] = (byte) (Y + 1.4075 * (V - 128));
                G[r*width+c] = (byte) (Y - 0.3455 * (U - 128) - (0.7169 * (V - 128)));
                B[r*width+c] = (byte) (Y + 1.7790 * (U - 128));

            }
        }

    }




    /**
     * Serves monochrome raster of given image
     * Formatted in 1D array of bytes
     * Uses only the Green Value for image
     * @return monochrome image
     */
    public byte[] getMonochromeRaster() {
    	byte[] mono = new byte[width * height];
        ImageManipulator.convertToMonochromeRaster(R,G,B, mono, height, width);
        return mono;

    }

    /**
     * Serves monochrome raster of given image
     * Formatted in 1D array of bytes
     * Averages the RGB values of image
     * @return monochrome image
     */
    public byte[] getMonochrome2Raster() {
    	byte[] mono = new byte[width * height];
        ImageManipulator.convertToMonochrome2Raster(R,G,B, mono, height, width);
        return mono;
    }

    /**
     * Serves black and white raster of given image
     * Formatted in 1D array of integers 0xRRGGBB
     * Compares luminance of pixels to those around them
     *
     * @return black and white image
     */
    public int[] getBlackWhiteRaster() {
    	int[] output = new int[width * height];
        if(blackWhiteRasterVersion == 2) {
            //ImageManipulator.convertToBlackWhite2Raster(pixels, output, height, width, tile);
        }
        else {
            ImageManipulator.convertToBlackWhiteRaster(R,G,B, output, height, width, frameWidth);
        }
        if(removeNoise) {
            output = ImageManipulator.removeNoise(output, height, width);
        }
        if(dilate) {
            output = ImageManipulator.dilate(output, height, width);
        }
        return output;
    }

    public int[] getEdgeBlackWhiteRaster(){
        int[] output = new int[width*height];
        //ImageManipulator.convertToFirstEdgeBlackWhiteRaster(R,G,B,output,height,width,tile);
        return output;
    }

    /**
     * Serves simple raster of given image
     * Formatted in 1D array of bytes
     *
     * Serves color raster encoded in 1D of values 0-6 with
     *      0 = RED
     *      1 = GREEN
     *      2 = BLUE
     *      3 = WHITE
     *      4 = GREY
     *      5 = BLACK
     *      6 = YELLOW
     *
     * @return simple image
     */
    public byte[] getSimpleColorRaster() {
    	byte[] simple = new byte[width * height];
        ImageManipulator.convertToSimpleColorRaster(R,G,B, simple, height, width, frameWidth);
        return simple;


    }

    /**
     * Serves RGB raster of given image
     * Formatted in 1D array of ints
     *
     * Formatted in 0xRRGGBB
     *
     * @return RGB image
     */
    public int[] getRGBRaster() {
    	int[] rgb = new int[width*height];
        ImageManipulator.convertToRGBRaster(R,G,B, rgb, height, width);
        return rgb;

    }

    /**
     * Converts into monochrome supported by window
     * Formatted in 1D array of ints
     *
     * Uses only the Green Value for image
     * Formatted in 0xRRGGBB
     *
     * @return monochrome image
     */
    public int[] getMonoRGBRaster() {
    	int[] rgb = new int[width*height];
    	byte[] mono = getMonochromeRaster();
        ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
        return rgb;

    }

    /**
     * Converts into simple colors supported by window
     * Formatted in 1D array of ints
     *
     * Serves color raster encoded in 1D of values 0-6 with
     *      0 = RED
     *      1 = GREEN
     *      2 = BLUE
     *      3 = WHITE
     *      4 = GREY
     *      5 = BLACK
     *      6 = YELLOW
     * Formatted in 0xRRGGBB
     *
     * @return simple image
     */
    public int[] getSimpleRGBRaster() {
    	int[] rgb = new int[width*height];
    	byte[] simple = getSimpleColorRaster();
        ImageManipulator.convertSimpleToRGB(simple, rgb, simple.length);
        return rgb;

    }


    
    public int[] getRobertsCross() {
    	byte[] mono = new byte[width * height];
    	int[] rgb = new int[width * height];
    	int[] output = new int[width * height];
    	ImageManipulator.convertToMonochrome2Raster(R,G,B, mono, height, width);
    	ImageManipulator.convertMonotoRGB(mono, rgb, mono.length);
    	ImageManipulator.convertToRobertsCrossRaster(rgb, output, height, width);
    	return output;
    }

    /**
     * Converts into road supported by window
     * Formatted in 1D array of ints
     *
     * Compares luminance of pixels to those around them
     * Then makes the road pink by drawing from the bottom of the screen to the first white pixel
     * Formatted in 0xRRGGBB
     *
     * @return road finding image
     */
    public int[] getRoad(){
        int[] output = getBlackWhiteRaster();

        int[] rgb = new int[width*height];
        ImageManipulator.findRoad(output, rgb, height, width);
        return rgb;
    }



    @Override
    public void initialize(CarControl control) {
    	control.addKeyEvent(KeyEvent.VK_SPACE, () -> changeFilter());
    }
    
    public void changeFilter() {
    	viewType = (viewType) % 6 + 1;
        displayThread.updateRaster(viewType);
    	System.out.println("view changed to " + viewType);
    }

    @Override
    public void update(CarControl control) {
        setupArrays(control.getRecentCameraImage());
        removeShadows();
        synchronized(displayThread){
            displayThread.notifyAll();
        }
        synchronized(BWThread) {
            BWThread.notifyAll();
        }
        synchronized(simpleThread) {
            simpleThread.notifyAll();
        }
        //notifyAll();
//
//  switch (viewType) {
//            case 1:
//                imagePixels = getRGBRaster();
//                break;
//            case 2:
//                imagePixels = getMonoRGBRaster();
//                break;
//            case 3:
//                imagePixels = getSimpleRGBRaster();
//                break;
//            case 4:
//                imagePixels = getBlackWhiteRaster();
//                break;
//            case 5:
//                imagePixels = getRoad();
//                break;
//            case 6:
//            	imagePixels = getRobertsCross();
//            	break;
//            default:
//                throw new IllegalStateException("No image management viewType: " + viewType);
//        }

        control.setRenderedImage(displayPixels);
    	control.setRGBImage(BWPixels);
    	control.setProcessedImage(simplePixels);
    }

    @Override
    public void paint(CarControl control, Graphics g) {
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

}

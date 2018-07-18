package com.apw.oldimage;

//import group1.fly0cam.FlyCamera;

import com.apw.fly2cam.OldFlyCamera;
import com.apw.oldglobal.Constant;

//Defines image as an 2d array of pixels
public class Image implements IImage {
    private final int frameRate = 3;
    //parameters for the coloratiion of the image
    private final float greyRatio = Constant.GREY_RATIO;
    private final int blackRange = Constant.BLACK_RANGE;
    private final int whiteRange = Constant.WHITE_RANGE;
    private final int lightDark = Constant.LIGHT_DARK_THRESHOLD;
    public int height;
    public int width;
    public OldFlyCamera flyCam = new OldFlyCamera();
    private int tile;
    private int autoCount = 0;
    private int autoFreq = 15;

    // 307200
    // private byte[] camBytes = new byte[2457636];
    private short[] camBytes;
    private byte[] tempBytes;
    private IPixel[][] image;
    private int frameNo = 0;

    //default values for image
    public Image() {
        this(0, 0, 0);
    }

    //exposure, shutter, gain for image properties
    public Image(int exposure, int shutter, int gain) {
        flyCam.Connect(frameRate, exposure, shutter, gain);

        int res = flyCam.Dimz();
        height = res >> 16;
        width = res & 0x0000FFFF;

        camBytes = new short[height * width * 4];
        tempBytes = new byte[height * width * 4];
        image = new Pixel[height][width];
        tile = flyCam.PixTile();
        System.out.println("tile: " + tile + " width: " + width + " height: " + height);
    }

    @Override
    public void setAutoFreq(int autoFreq) {  //How many frames are loaded before the calibrate is called (-1 never calls it)
        this.autoFreq = autoFreq;
    }

    @Override
    public IPixel[][] getImage() {
        return image;
    }

    public void setImage(IPixel[][] i) {
        image = i;
    }

    // gets a single frame
    @Override
    public void readCam() {
        Constant.LAST_FRAME_MILLIS = Constant.CURRENT_FRAME_MILLIS;
        Constant.CURRENT_FRAME_MILLIS = System.currentTimeMillis();
        Constant.TIME_DIFFERENCE = Constant.CURRENT_FRAME_MILLIS - Constant.LAST_FRAME_MILLIS;
        autoCount++;
        //System.out.println("TILE: " + flyCam.PixTile());
        // System.out.println(flyCam.errn);
        flyCam.NextFrame(camBytes);
        // System.out.println(flyCam.errn);

        //downcast to bytes
        for (int i = 0; i < camBytes.length; i++) {
            tempBytes[i] = (byte) (camBytes[i] >> 4);
        }

        if (autoCount > autoFreq && autoFreq > -1) {
            autoConvertWeighted();
            autoCount = 0;
        } else {
            byteConvert();
        }
    }

    public void finish() {
        flyCam.Finish();
    }

    //converts image from bytes to arrays of pixels
    private void byteConvert() {

        int pos = 0;
        if (tile == 1) {
            for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {

                    image[i][j] = new Pixel((short) (camBytes[pos] & 255), (short) (camBytes[pos + 1] & 255),
                            (short) (camBytes[pos + 1 + width * 2] & 255));
                    pos += 2;

                }

                pos += width * 2;

            }
        } else if (tile == 3) {
            for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {

                    image[i][j] = new Pixel((short) (camBytes[pos + width * 2] & 255), (short) (camBytes[pos] & 255), (short) (camBytes[pos + 1] & 255));
                    pos += 2;

                }

                pos += width * 2;

            }
        }

    }

    //skip this method for now
    private void autoConvert() {
        int average = 0;    //0-255
        int average2;   //0-765
        int variation = 0;
        final int divisor = (width * height);

        int pos = 0;
        if (tile == 1) {
            for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {

                    image[i][j] = new Pixel((short) (camBytes[pos] & 255), (short) (camBytes[pos + 1] & 255),
                            (short) (camBytes[pos + 1 + width * 2] & 255));
                    pos += 2;

                    average += image[i][j].getRed() + image[i][j].getGreen() + image[i][j].getBlue();

                }

                pos += width * 2;

            }
        } else if (tile == 3) {
            for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {

                    image[i][j] = new Pixel((short) (camBytes[pos + width * 2] & 255), (short) (camBytes[pos] & 255), (short) (camBytes[pos + 1] & 255));
                    pos += 2;

                    average += image[i][j].getRed() + image[i][j].getGreen() + image[i][j].getBlue();

                }

                pos += width * 2;

            }
        }

        average2 = average / divisor;
        average = average2 / 3;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                IPixel temp = image[i][j];
                int rVar = temp.getRed() - average;
                if (rVar < 0) {
                    rVar = -rVar;
                }

                int gVar = temp.getGreen() - average;
                if (gVar < 0) {
                    gVar = -rVar;
                }

                int bVar = temp.getBlue() - average;
                if (bVar < 0) {
                    bVar = -bVar;
                }

                variation += rVar + gVar + bVar;
            }

        }

        variation = variation / divisor;
        Pixel.greyMargin = (int) (variation * greyRatio);
        Pixel.blackMargin = average2 - blackRange;
        Pixel.whiteMargin = average2 + whiteRange;
        System.out.println("Variation: " + variation + " greyRatio: " + greyRatio);
        System.out.println("greyMargin: " + Pixel.greyMargin + " blackMargin: " + Pixel.blackMargin + " whiteMargin: " + Pixel.whiteMargin);

    }

    /*
    private void autoConvertWeighted2() {

        int average;    //0-255
        int average2;   //0-765
        int greatVar = 0;
        int lessVar = 0;
        int greatCount = 0;
        int lessCount = 0;
        int threshVar = 50;
        final int divisor = (width * height);


        //autoThreshold variables
        int threshold = 381;
        int avg; //0-765
        int r, b, g;
        int lesserSum = 0;
        int greaterSum = 0;
        int lesserCount = 0;
        int greaterCount = 0;
        int lesserMean;
        int greaterMean;

        int pos = 0;
        if (tile == 1) {
            for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {

                    image[i][j] = new Pixel((short) (camBytes[pos] & 255), (short) (camBytes[pos + 1] & 255), (short) (camBytes[pos + 1 + width * 2] & 255));
                    pos += 2;

                    r = image[i][j].getRed();
                    b = image[i][j].getBlue();
                    g = image[i][j].getGreen();

                    avg = (r + b + g);

                    if (avg < threshold) {

                        lesserSum += avg;
                        lesserCount++;

                    } else {

                        greaterSum += avg;
                        greaterCount++;

                    }


                }

                pos += width << 1;

            }


        } else if (tile == 3) {
            for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {

                    image[i][j] = new Pixel((short) (camBytes[pos + width * 2] & 255), (short) (camBytes[pos] & 255), (short) (camBytes[pos + 1] & 255));
                    pos += 2;

                    r = image[i][j].getRed();
                    b = image[i][j].getBlue();
                    g = image[i][j].getGreen();

                    avg = (r + b + g);

                    if (avg < threshold) {

                        lesserSum += avg;
                        lesserCount++;

                    } else {

                        greaterSum += avg;
                        greaterCount++;

                    }

                }

                pos += width << 1;

            }


        }

        lesserMean = lesserSum / lesserCount;
        greaterMean = greaterSum / greaterCount;
        threshold = (lesserMean + greaterMean) >> 1;

        average2 = threshold;
        average = average2 / 3;


        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                IPixel temp = image[i][j];
                int rVar = temp.getRed() - average;
                if (rVar < 0) {
                    rVar = -rVar;
                }

                int gVar = temp.getGreen() - average;
                if (gVar < 0) {
                    gVar = -rVar;
                }

                int bVar = temp.getBlue() - average;
                if (bVar < 0) {
                    bVar = -bVar;
                }

                variation += rVar + gVar + bVar;
            }

        }

        variation = variation / divisor;
        Pixel.greyMargin = (int) (variation * greyRatio);
        Pixel.blackMargin = average2 - blackRange;
        Pixel.whiteMargin = average2 + whiteRange;

    }

    */

//gets the current frame number and increases by 1 whenever queried

    private void autoConvertWeighted() {

        int average;    //0-255
        int average2;   //0-765
        int variation = 0;
        final int divisor = (width * height);


        //autoThreshold variable
        int avg; //0-765
        int r, b, g;
        int lesserSum = 0;
        int greaterSum = 0;
        int lesserCount = 0;
        int greaterCount = 0;
        int lesserMean;
        int greaterMean;

        int pos = 0;
        if (tile == 1) {
            for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {

                    image[i][j] = new Pixel((short) (camBytes[pos] & 255), (short) (camBytes[pos + 1] & 255), (short) (camBytes[pos + 1 + width * 2] & 255));
                    pos += 2;

                    r = image[i][j].getRed();
                    b = image[i][j].getBlue();
                    g = image[i][j].getGreen();

                    avg = (r + b + g);

                    if (avg < lightDark) {

                        lesserSum += avg;
                        lesserCount++;

                    } else {

                        greaterSum += avg;
                        greaterCount++;

                    }


                }

                pos += width << 1;

            }


        } else if (tile == 3) {
            for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {

                    image[i][j] = new Pixel((short) (camBytes[pos + width * 2] & 255), (short) (camBytes[pos] & 255), (short) (camBytes[pos + 1] & 255));
                    pos += 2;

                    r = image[i][j].getRed();
                    b = image[i][j].getBlue();
                    g = image[i][j].getGreen();

                    avg = (r + b + g);

                    if (avg < lightDark) {

                        lesserSum += avg;
                        lesserCount++;

                    } else {

                        greaterSum += avg;
                        greaterCount++;

                    }

                }

                pos += width << 1;

            }


        }

        lesserMean = lesserSum / lesserCount;
        greaterMean = greaterSum / greaterCount;

        average2 = (lesserMean + greaterMean) >> 1;
        average = average2 / 3;


        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                IPixel temp = image[i][j];
                int rVar = temp.getRed() - average;
                if (rVar < 0) {
                    rVar = -rVar;
                }

                int gVar = temp.getGreen() - average;
                if (gVar < 0) {
                    gVar = -rVar;
                }

                int bVar = temp.getBlue() - average;
                if (bVar < 0) {
                    bVar = -bVar;
                }

                variation += rVar + gVar + bVar;
            }

        }

        variation = variation / divisor;
        Pixel.greyMargin = (int) (variation * greyRatio);
        Pixel.blackMargin = average2 - blackRange;
        Pixel.whiteMargin = average2 + whiteRange;

    }

    @Override
    public int getFrameNo() {
        return frameNo++;
    }

}

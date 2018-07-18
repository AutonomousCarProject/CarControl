package com.apw.oldimage;

import com.apw.fly2cam.OldFlyCamera;
import com.apw.oldglobal.Constant;

import java.util.Arrays;

import static com.apw.oldimage.Pixel.*;

/**
 * Image class for HDR images, handles the sequence of four images sent by the camera using HDR
 */

public class HDRImage implements IImage {
    //hehe
    private static final int windowSize = 3;
    private static final int WHITE_BAL_ITR_COUNT = 6;
    private static final int PIX_INC = 10;
    private static int AUTO_FREQ = 15;
    private final float greyRatio = Constant.GREY_RATIO;
    private final int blackRange = Constant.BLACK_RANGE * 3;
    private final int whiteRange = Constant.WHITE_RANGE * 3;
    public int height;
    public int width;
    public int frameRate = 4;
    private OldFlyCamera flyCam = new OldFlyCamera();
    private short[] camBytes;
    private int[][][] images;
    private IPixel[][] out;
    private int tile;
    private int autoCount = 0;
    private int[][][] justOnce;
    private int[][] set = new int[3][windowSize * windowSize];

    public HDRImage(int exposure, int shutter, int gain) {
        flyCam.Connect(frameRate, exposure, shutter, gain);

        int res = flyCam.Dimz();
        height = res >> 16;
        width = res & 0x0000FFFF;

        camBytes = new short[height * width * 4];
        images = new int[height][width][3];
        out = new IPixel[height][width];
        justOnce = new int[images.length][images[0].length][3];
        for (int i = 0; i < out.length; i++) for (int j = 0; j < out[0].length; j++) out[i][j] = new Pixel(0);
        tile = flyCam.PixTile();
        System.out.println("tile: " + tile + " width: " + width + " height: " + height);
        tile = 1;
        //auto white balance such that our greys are maximized at at stared
        //autoWhiteBalance();
    }

    private static int cheapSaturationTotal(short[] camBytes, int width, int height, int pixInc) {
        int ret = 0;
        for (int i = 0; i < height; i += pixInc) {
            for (int j = 0; j < width; j += pixInc) {
                final int pos = j * 2 + i * width * 4;
                final int r = (camBytes[pos] & 0xffff) >> 4;
                final int g = (camBytes[pos + 1] & 0xffff) >> 4;
                final int b = (camBytes[pos + 1 + width * 2] & 0xffff) >> 4;
                ret += Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b));
            }
        }
        return ret;
    }

    @Override
    public void setAutoFreq(int autoFreq) {
    }

    //this is where the HDR happens
    @Override
    public void readCam() {
        //step 1: all the HDR images
        flyCam.NextFrame(camBytes);
        //if(++autoCount < AUTO_FREQ) byteConvert();
        //else {
        //autoCount = 0;
        //autoConvert();
        //autoConvertWeighted();
        byteConvert();
        //}

        medianFilter();

        //attempt optimized conversion
        for (int i = 0; i < images.length; i++) {
            for (int j = 0; j < images[0].length; j++) {
                //final int pos = j * 2 + i * width * 4;
                //final int r = (camBytes[pos] & 0xffff) >> 4;
                //final int g = (camBytes[pos + 1] & 0xffff) >> 4;
                //final int b = (camBytes[pos + 1 + width * 2] & 0xffff) >> 4;
                final int ave = images[i][j][0] + images[i][j][1] + images[i][j][2];
                final int r3 = images[i][j][0] * 3;
                final int g3 = images[i][j][1] * 3;
                final int b3 = images[i][j][2] * 3;

                final int rdiff = Math.abs(r3 - ave);
                final int gdiff = Math.abs(g3 - ave);
                final int bdiff = Math.abs(b3 - ave);

                if (rdiff < greyMargin * 3 && gdiff < greyMargin * 3 && bdiff < greyMargin * 3) { // if its not a distinct color
                    if (r3 < blackMargin * 3 && g3 < blackMargin * 3 && b3 < blackMargin * 3)
                        out[i][j].setColor(4); // black
                    else if (r3 > whiteMargin * 3 && g3 > whiteMargin * 3 && b3 > whiteMargin * 3)
                        out[i][j].setColor(5); // white
                    else
                        out[i][j].setColor(3);
                } else if (r3 > g3 && r3 > b3)
                    out[i][j].setColor(0);
                else if (g3 > r3 && g3 > b3)
                    out[i][j].setColor(1);
                else if (b3 > r3 && b3 > g3)
                    out[i][j].setColor(2);
                    //uhhhh... red?
                else out[i][j].setColor(0);


                //out[i][j] = new Pixel((short)(images[i][j][0] >> 4), (short)(images[i][j][1] >> 4), (short)(images[i][j][2] >> 4));

            }

        }


        Constant.LAST_FRAME_MILLIS = Constant.CURRENT_FRAME_MILLIS;
        Constant.CURRENT_FRAME_MILLIS = System.currentTimeMillis();
        Constant.TIME_DIFFERENCE = Constant.CURRENT_FRAME_MILLIS - Constant.LAST_FRAME_MILLIS;
    }

    @Override
    public IPixel[][] getImage() {
        return out;
    }

    @Override
    public void finish() {
        flyCam.Finish();
    }

    private void byteConvert() {

        int pos = 0;
        if (tile == 1) {
            for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {
                    images[i][j][0] = (camBytes[pos] & 0xffff) >> 4;
                    images[i][j][1] = (camBytes[pos + 1] & 0xffff) >> 4;
                    images[i][j][2] = (camBytes[pos + 1 + width * 2] & 0xffff) >> 4;
                    //out[i][j] = new Pixel((short)(r >> 4), (short)(g >> 4), (short)(b >> 4));
//                    out[i][j] = new Pixel((short)(r), (short)(g), (short)(b));
                    pos += 2;

                }

                pos += width * 2;

            }
        } else if (tile == 3) { // should be unreachable
            for (int i = 0; i < height; i++) {
                System.out.println("hai");

                for (int j = 0; j < width; j++) {

                    images[i][j][0] = (camBytes[pos + width * 2] & 0xffff) >> 4;
                    images[i][j][1] = (camBytes[pos] & 0xffff) >> 4;
                    images[i][j][2] = (camBytes[pos + 1] & 0xffff) >> 4;
                    //out[i][j] = new Pixel((short)(r >> 4), (short)(g >> 4), (short)(b >> 4));
                    pos += 2;

                }

                pos += width * 2;

            }
        }
    }

    private void autoConvertWeighted() {

        int average; // 0-255
        int average2; // 0-765
        int variation = 0;
        final int divisor = (width * height);

        // autoThreshold variables
        int threshold = 381;
        int avg; // 0-765
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

                    images[i][j][0] = (camBytes[pos] & 0xffff) >> 4;
                    images[i][j][1] = (camBytes[pos + 1] & 0xffff) >> 4;
                    images[i][j][2] = (camBytes[pos + 1 + width * 2] & 0xffff) >> 4;
                    pos += 2;

                    final int r = images[i][j][0];
                    final int g = images[i][j][1];
                    final int b = images[i][j][2];

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

                    images[i][j][0] = (camBytes[pos] & 0xffff) >> 4;
                    images[i][j][1] = (camBytes[pos + 1] & 0xffff) >> 4;
                    images[i][j][2] = (camBytes[pos + 1 + width * 2] & 0xffff) >> 4;
                    pos += 2;

                    final int r = images[i][j][0];
                    final int g = images[i][j][1];
                    final int b = images[i][j][2];


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

                int rVar = images[i][j][0] - average;
                if (rVar < 0) {
                    rVar = -rVar;
                }

                int gVar = images[i][j][1] - average;
                if (gVar < 0) {
                    gVar = -gVar;
                }

                int bVar = images[i][j][2] - average;
                if (bVar < 0) {
                    bVar = -bVar;
                }

                variation += rVar + gVar + bVar;
            }

        }

        variation = variation / divisor;
        greyMargin = (int) (variation * greyRatio);
        blackMargin = average2 - blackRange;
        Pixel.whiteMargin = average2 + whiteRange;

    }

    private void autoConvert() {
        int average = 0; // 0-255
        int average2; // 0-765
        int variation = 0;
        final int divisor = (width * height);

        int pos = 0;
        if (tile == 1) {
            for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {

                    images[i][j][0] = (camBytes[pos] & 0xffff) >> 4;
                    images[i][j][1] = (camBytes[pos + 1] & 0xffff) >> 4;
                    images[i][j][2] = (camBytes[pos + 1 + width * 2] & 0xffff) >> 4;
                    pos += 2;

                    average += images[i][j][0] + images[i][j][1] + images[i][j][2];

                }

                pos += width * 2;

            }
        } else if (tile == 3) {
            for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {

                    images[i][j][0] = (camBytes[pos] & 0xffff) >> 4;
                    images[i][j][1] = (camBytes[pos + 1] & 0xffff) >> 4;
                    images[i][j][2] = (camBytes[pos + 1 + width * 2] & 0xffff) >> 4;
                    pos += 2;

                    average += images[i][j][0] + images[i][j][1] + images[i][j][2];

                }

                pos += width * 2;

            }
        }

        average2 = average / divisor;
        average = average2 / 3;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rVar = images[i][j][0] - average;
                if (rVar < 0) {
                    rVar = -rVar;
                }

                int gVar = images[i][j][1] - average;
                if (gVar < 0) {
                    gVar = -rVar;
                }

                int bVar = images[i][j][2] - average;
                if (bVar < 0) {
                    bVar = -bVar;
                }

                variation += rVar + gVar + bVar;
            }

        }

        variation = variation / divisor;
        greyMargin = (int) (variation * greyRatio);
        blackMargin = average2 - blackRange;
        Pixel.whiteMargin = average2 + whiteRange;
        System.out.println("Variation: " + variation + " greyRatio: " + greyRatio);
        System.out.println("greyMargin: " + greyMargin + " blackMargin: " + blackMargin + " whiteMargin: "
                + Pixel.whiteMargin);

    }

    private void autoWhiteBalance() {
        int redBal = 512;
        int blueBal = 512;
        int inc = 256;
        for (int p = 0; p < WHITE_BAL_ITR_COUNT; p++) {
            int totalSats[] = new int[2];
            //set white balance to manual, and enable it, first with first inc
            flyCam.SafeWriteRegister(0x80C, (1 << 25) | redBal - inc | (blueBal << 12), "white balance write failed");
            //get the image, and averaging saturation
            flyCam.NextFrame(camBytes);
            //iterate through most pixels cheaply
            totalSats[0] = cheapSaturationTotal(camBytes, width, height, PIX_INC);
            //shift the balance a sizable amount, then get saturation again
            flyCam.SafeWriteRegister(0x80C, (1 << 25) | redBal + inc | (blueBal << 12), "white balance write failed");
            flyCam.NextFrame(camBytes);
            //iterate through most pixels cheaply
            totalSats[1] = cheapSaturationTotal(camBytes, width, height, PIX_INC);
            //whichever one is lower, move the white balance there
            if (totalSats[0] < totalSats[1]) redBal -= inc;
            else if (totalSats[0] > totalSats[1]) redBal += inc;
            //or just keep it the same
            //and halve the increment
            inc >>= 1;
        }
        //and do it again for blueshift
        inc = 256;
        for (int p = 0; p < WHITE_BAL_ITR_COUNT; p++) {
            int totalSats[] = new int[2];
            //set white balance to manual, and enable it, first with first inc
            flyCam.SafeWriteRegister(0x80C, (1 << 25) | redBal | (blueBal - inc << 12), "white balance write failed");
            //get the image, and averaging saturation
            flyCam.NextFrame(camBytes);
            //iterate through most pixels cheaply
            totalSats[0] = cheapSaturationTotal(camBytes, width, height, PIX_INC);
            //shift the balance a sizable amount, then get saturation again
            flyCam.SafeWriteRegister(0x80C, (1 << 25) | redBal | (blueBal + inc << 12), "white balance write failed");
            flyCam.NextFrame(camBytes);
            //iterate through most pixels cheaply
            totalSats[1] = cheapSaturationTotal(camBytes, width, height, PIX_INC);
            //whichever one is lower, move the white balance there
            if (totalSats[0] < totalSats[1]) blueBal -= inc;
            else if (totalSats[0] > totalSats[1]) blueBal += inc;
            //or just keep it the same
            //and halve the increment
            inc >>= 1;
        }
        //do the thing
        flyCam.SafeWriteRegister(0x80C, (1 << 25) | redBal | (blueBal << 12), "white balance write failed");
    }

	/*
	private static int getFancyS(short r, short g, short b){
		return Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b));
	}

	private static float getFancyL(short r, short g, short b){
		return 0.212f * r + 0.7152f * g + 0.0722f * b;
	}

	private void fuseImages() {
		//find saturation, then weighted average
		//for every pixel
		for(int i = 0; i < images[0].length; i++){
			for(int j = 0; j < images[0][0].length; j++){
				//take weighted average with saturation and value
				double sumHA = 0;
				double sumHB = 0;
				double sumV = 0;
				boolean sumCheck = false;

				for(int m = 0; m < images.length; m++) {
					final float[] HSV = Color.RGBtoHSB(images[m][i][j][0], images[m][i][j][1], images[m][i][j][2], null);
					//if the hue is saturated, take the average of it
					if (HSV[1] >= SAT_MIN && HSV[2] >= 0.1f) {
						sumHA += Math.sin(HSV[0] * 2 * Math.PI) * HSV[1];
						sumHB += Math.cos(HSV[0] * 2 * Math.PI) * HSV[1];
						sumCheck = true;
					}
					//else try the average of the value
					else if(!sumCheck) {
						sumV += HSV[2];
					}
				}

				//if we got any hue average value, make that the color
				if(sumCheck){
					double hue = Math.atan2(sumHA, sumHB) / (2 * Math.PI);
					if(hue < 0) hue += 1;

					tempHue[i][j] = hue;
				}
				//else use the average value as the color (e.g. greyscale)
				else {
					if(sumV < LUM_BLACK * images.length) tempHue[i][j] = -4;
					else if(sumV > LUM_WHITE * images.length) tempHue[i][j] = -5;
					else tempHue[i][j] = -3;
				}

			}
		}
	}

	private int frameNo = 0;
	@Override
	public int getFrameNo()
	{
		return frameNo++;
	}

	private static double getWeight(double color, double exposureNum, double numExposures) {
		//final double c = Math.pow(exposureNum / numExposures, 0.3);
		return Math.exp(-Math.pow((color), 2) / K);
	}*/

    public void medianFilter() {
        for (int i = 0; i < images.length; i++) {
            for (int j = 0; j < images[0].length; j++) {
                if (i > images.length - windowSize || j > images[0].length - windowSize) {
                    //tempHue[i][j] = new Pixel((short)0, (short)0, (short)0);
                } else {
                    for (int w = 0; w < windowSize; w++) {
                        for (int q = 0; q < windowSize; q++) {
                            set[0][w * windowSize + q] = images[i + w][j + q][0];
                            set[1][w * windowSize + q] = images[i + w][j + q][1];
                            set[2][w * windowSize + q] = images[i + w][j + q][2];
                        }
                    }

                    Arrays.sort(set[0]);
                    Arrays.sort(set[1]);
                    Arrays.sort(set[2]);

                    final int half = (windowSize * windowSize) / 2;

                    justOnce[i][j][0] = set[0][half];
                    justOnce[i][j][1] = set[1][half];
                    justOnce[i][j][2] = set[2][half];
                }
            }
        }
        images = justOnce;
    }

    public void meanFilter() {

    }

	/*
	public void meanFilter(){
		final int windowSize = 3;

		double[][] justOnce = new double[tempHue.length][tempHue[0].length];

		for(int i=0; i<tempHue.length; i++){
			for(int j=0; j<tempHue[0].length; j++){
				if(i>tempHue.length-windowSize || j>tempHue[0].length-windowSize){
					//tempHue[i][j] = new Pixel((short)0, (short)0, (short)0);
				}
				else{
					double sum = 0;

					for(int w=0; w<windowSize; w++){
						for(int q=0; q<windowSize; q++){
							sum += tempHue[i+w][j+q];
						}
					}

					justOnce[i][j] = sum / 9.0;
				}
			}
		}
		tempHue = justOnce;
	}
	*/


}

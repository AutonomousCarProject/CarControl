
/**
 * This class contains functions to apply filters and convert images
 * between bayer8 and rgb formats. Functions in this class are only
 * accessed by ImageManager in the NWAPW Autonomous Car Project
 *
 * @author Vikram K
 * @author Joshua B
 * @author Riley J
 * @author Nathan P
 *
 * @see com.apw.ImageManagement.ImageManager
 */

package com.apw.imagemanagement;

public class ImageManipulator {
	
	private static double luminanceMultiplier = 1;

	/** Converts a bayer8 image to a monochrome image, uses the green value of the bayer8
	 *
	 * @param R red values of bayer8 image
	 * @param G green values of bayer8 image
	 * @param B blue values of bayer8 image
	 * @param mono	monochrome output
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number of columns of pixels in the image
	 */
    public static void convertToMonochromeRaster(byte[] R, byte[] G, byte[] B, byte[] mono, int nrows, int ncols) {

        for (int r = 0; r < nrows; r++) {
            for (int c = 0; c < ncols; c++) {
                mono[r * ncols + c] = (byte) ((((int) G[(r * ncols + c)]) & 0xFF));            //Use only top right
            }
        }
    }

	/** Converts a bayer8 image to a monochrome image, averages the values of the bayer8
	 *
	 * @param R red values of bayer8 image
	 * @param G green values of bayer8 image
	 * @param B blue values of bayer8 image
	 * @param mono	monochrome output
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number of columns of pixels in the image
	 */

	public static void convertToMonochrome2Raster(byte[] R, byte[] G, byte[] B, byte[] mono, int nrows, int ncols) {

        for (int r = 0; r < nrows; r++) {
            for (int c = 0; c < ncols; c++) {

				int red = R[ncols*r+c]&0xFF;
				int green = G[ncols*r+c]&0xFF;
				int blue = B[ncols*r+c]&0xFF;
				//double Y = R *  .299000 + G *  .587000 + B *  .114000;

				double Y = (red+green+blue)/3;
				mono[r * ncols + c] = (byte) Y;
			}
		}
	}
	
	/** Converts a bayer8 image to a black and white image based on average luminance of each row
	 *
	 * @param R red values of bayer8 image
	 * @param G green values of bayer8 image
	 * @param B blue values of bayer8 image
	 * @param blackWhite	black and white output
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number of columns of pixels in the image
	 * @param frameWidth width of image to apply filter to (usually 640)
	 */

	public static void convertToBlackWhiteRaster(byte[] R, byte[] G, byte[] B, int[] blackWhite, int nrows, int ncols, int frameWidth) {
		int pixelsAveraged = 3; //number of pixels averaged to see if the middle pixel counts as white.
		// Increasing this number reduces noise but severely increases processing time
		for (int r = nrows >> 1; r < nrows; r++) {
    		int averageLuminance = 0;
        	for(int c = 0; c < frameWidth; c++) {
				int red = R[ncols*r+c]&0xFF;
				int green = G[ncols*r+c]&0xFF;
				int blue = B[ncols*r+c]&0xFF;
				averageLuminance += (red + green + blue);
			}


        	int borderWidth = pixelsAveraged >> 1; //int division
            for (int c = borderWidth; c < frameWidth-borderWidth; c++) {
            	int pix = 0;
            	for(int i = 0; i < pixelsAveraged; i++) {
					pix+= R[ncols*r+c-borderWidth + i]&0xFF;
					pix+= G[ncols*r+c-borderWidth + i]&0xFF;
					pix+= B[ncols*r+c-borderWidth + i]&0xFF;
            	}
				if (pix * frameWidth > luminanceMultiplier * averageLuminance * pixelsAveraged) {
					blackWhite[r * ncols + c] = 0xFFFFFF;
				} else {
					blackWhite[r * ncols + c] = 0;
				}
			}
		}
	}

	public static void convertToBlackWhiteRaster(int[] rgb, int[] blackWhite, int nrows, int ncols, int frameWidth) {
		int pixelsAveraged = 3; //number of pixels averaged to see if the middle pixel counts as white.
		// Increasing this number reduces noise but severely increases processing time
		for (int r = nrows >> 1; r < nrows; r++) {
			int averageLuminance = 0;
			for(int c = 0; c < frameWidth; c++) {
				int red = rgb[ncols*r+c]/0xFFFF;
				int green = (rgb[ncols*r+c]%0xFFFF)/0xFF;
				int blue = rgb[ncols*r+c]%0xFF;
				averageLuminance += (red + green + blue);
			}


			int borderWidth = pixelsAveraged >> 1; //int division
			for (int c = borderWidth; c < frameWidth-borderWidth; c++) {
				int pix = 0;
				for(int i = 0; i < pixelsAveraged; i++) {
					pix+= rgb[ncols*r+c-borderWidth + i]/0xFFFF;
					pix+= (rgb[ncols*r+c-borderWidth + i]%0xFFFF)/0xFF;
					pix+= rgb[ncols*r+c-borderWidth + i]%0xFF;
				}
				if (pix * frameWidth > luminanceMultiplier * averageLuminance * pixelsAveraged) {
					blackWhite[r * ncols + c] = 0xFFFFFF;
				} else {
					blackWhite[r * ncols + c] = 0;
				}
			}
		}
	}

	/** Converts a bayer8 image to a black and white image based on how much each pixel increases
	 *  the average luminance of each row
	 * @param blackWhite	black and white output
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number of columns of pixels in the image
	 */
	public static void convertToBlackWhite2Raster(byte[] R, byte[] G, byte[] B, int[] blackWhite, int nrows, int ncols, int frameWidth) {
		int pixelsAveraged = 3; //number of pixels averaged to see if the middle pixel counts as white.
		// Increasing this number reduces noise but severely increases processing time
		for (int r = nrows >> 1; r < nrows; r++) {
			int averageLuminance = 0;
			double stddev = 0;
			for(int c = 0; c < frameWidth; c++) {
				int red = R[ncols*r+c]&0xFF;
				int green = G[ncols*r+c]&0xFF;
				int blue = B[ncols*r+c]&0xFF;
				averageLuminance += (red + green + blue);
			}
			averageLuminance /= frameWidth;
			for(int c = 0; c < frameWidth; c++){
				stddev = (R[ncols*r+c] +G[ncols*r+c] + B[ncols*r+c] - averageLuminance)*(R[ncols*r+c] + G[ncols*r+c] + B[ncols*r+c] - averageLuminance);
			}
			stddev /= frameWidth;
			stddev = Math.sqrt(stddev);
			System.out.println(stddev);
			for (int c = 0; c < frameWidth; c++) {
				//System.out.println(averageLuminance);
				if ((R[ncols*r+c]&0xFF) + (G[ncols*r+c]&0xFF) + (B[ncols*r+c]&0xFF) - averageLuminance > (stddev * luminanceMultiplier) ) {
					blackWhite[r * ncols + c] = 0xFFFFFF;
				} else {
					blackWhite[r * ncols + c] = 0;
				}
			}
		}
	}

	/**
	 *
	 * @param input
	 * @param output
	 * @param nrows
	 * @param ncols
	 */
	public static void convertToRobertsCrossRaster(int[] input, int[] output, int nrows, int ncols) {
		for (int r = 0; r < nrows - 1; r++) {
			for(int c = 0; c < ncols - 1; c++) {
				output[r * ncols + c] = Math.abs(input[r * ncols + c] - input[(r+1) * ncols + (c+1)])
						+ Math.abs(input[r * ncols + (c+1)] - input[(r+1) * ncols + c]);
				
			}
		}
	}
	
	


	/** Converts a bayer8 image to a simple color image, the simple colors are red, green, blue, yellow, white, grey and black
	 *
	 * @param R red values of bayer8 image
	 * @param G green values of bayer8 image
	 * @param B blue values of bayer8 image
	 * @param simple simple color output
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number og columns of pixels in the image
	 * @param frameWidth width of the image to apply the filter to (usually 640)
	 */
	public static void convertToSimpleColorRaster(byte[] R, byte[] G, byte[] B, byte[] simple, int nrows, int ncols, int frameWidth) {
		/*
			*
			*Serves color raster encoded in 1D of values 0-5 with
	 		* 0 = RED
	 		* 1 = GREEN
	 		* 2 = BLUE
	 		* 3 = WHITE
	 		* 4 = GREY
	 		* 5 = BLACK
	 		* 6 = YELLOW
		*/
		for(int r = 0; r < nrows; r++){
			for(int c = 0; c < frameWidth; c++){
				int red = R[ncols*r+c]&0xFF;
				int green = G[ncols*r+c]&0xFF;
				int blue = B[ncols*r+c]&0xFF;
				//int B = (((int)bayer[(r*ncols*2 + c)*2 + 1+2*ncols-ncols*2*getBit(tile,1)-getBit(tile,0)])&0xFF);			//Bottom right (blue)
                double Y = red *  .299000 + green *  .587000 + blue *  .114000;
				double U  = red * -.168736 + green * -.331264 + blue *  .500000 + 128;
				double V = red *  .500000 + green * -.418688 + blue * -.081312 + 128;
				red =(int)(  1.4075 * (V - 128));
				green = (int)(0- 0.3455 * (U - 128) - (0.7169 * (V - 128)));
				blue = (int)(1.7790 * (U - 128));
				//If one of the colors has a value 50 greater than both other colors
				//it assigns that pixel to that color
				if(red > green+SimpleThresholds.redGreen && red > blue+SimpleThresholds.redBlue){
					simple[r*ncols+c] = 0;
				} else if(green > red+SimpleThresholds.greenRed && green > blue+SimpleThresholds.greenBlue){
					simple[r*ncols+c] = 1;
				} else if(blue > red+SimpleThresholds.blueRed && blue > green+SimpleThresholds.blueGreen){
					simple[r*ncols+c] = 2;
				}else if(red<green+SimpleThresholds.yellowDiff&&green<red+SimpleThresholds.yellowDiff&&(red>blue+SimpleThresholds.yellowBlue)){
					simple[r*ncols+c] = 6;
				}
				//Otherwise it sees if one of the colors has a value above 170 for white
				// if not, 85 for grey and below 85 for black
				else if(Y>SimpleThresholds.whitePoint){
					simple[r*ncols+c] = 3;
				} else if(Y>SimpleThresholds.greyPoint){
					simple[r*ncols+c] = 4; //0x808080
				} else {
					simple[r * ncols + c] = 5;
				}
			}
		}
	}

	/** Converts a bayer8 image to a rgb image
	 *
	 * @param R red values of bayer8 image
	 * @param G green values of bayer8 image
	 * @param B blue values of bayer8 image
	 * @param rgb	rgb output
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number og columns of pixels in the image
	 */
	public static void convertToRGBRaster(byte[] R, byte[] G, byte[] B, int[] rgb, int nrows, int ncols) {
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				int red = R[ncols*r+c]&0xFF;
				int green = G[ncols*r+c]&0xFF;
				int blue = B[ncols*r+c]&0xFF;
				int pix =(red<<16)+(green<<8)+blue;
				rgb[r*ncols + c] = pix;
			}
			
		}
	}

	/** Converts the simpleColor byte array to an rgb int array
	 *
	 * @param simpleByte simple color byte array(input)
	 * @param simpleRGB simple color int array(output)
	 * @param length length of the array
	 */
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
				case 6:
					simpleRGB[i] = 0xDDDD00;
					break;
			}
		}
	}

	/** Converts the black and white byte array to an rgb int array
	 *
	 * @param blackWhiteByte black and white byte array(input)
	 * @param blackWhite black and white int array(output)
	 * @param length length of the array
	 */
    public static void convertBWToRGB(int[] blackWhiteByte, int[] blackWhite, int length) {
        for (int i = 0; i < length; i++) {
            switch (blackWhiteByte[i]) {
                case 0:
                    blackWhite[i] = 0x000000;
                    break;
                case 1:
                    blackWhite[i] = 0xFFFFFF;
                    break;
            }
        }
    }

	/**Converts the monochrome byte array to a int array
	 *
	 * @param mono monochrome byte array (input)
	 * @param rgb monochrome int array (output)
	 * @param length length of the array
	 */

    public static void convertMonotoRGB(byte[] mono, int[] rgb, int length) {
        for (int i = 0; i < length; i++) {
            rgb[i] = (mono[i] << 16) + (mono[i] << 8) + mono[i];
        }
    }

    /** Converts a black and white image to a black white image with a colored road
	 *
	 * @param bw black and white
	 * @param output image output in int[]
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number of columns of pixels in the image
	 */
    public static void findRoad(int[] bw, int[] output, int nrows, int ncols){
    	int rightEnd = 640, leftEnd = 0;
    	for(int i = ncols/2; i < ncols; i++){
			if(bw[454*ncols + i] == 1){
				rightEnd = i;
			}
		}

		for(int i = ncols/2; i > 0; i--){
			if(bw[454*ncols + i] == 1){
				leftEnd = i;
			}
		}
    	for(int col = 0; col < ncols; col++){
    		boolean endFound = false;

    		for(int row = nrows-1; row > 0; row--){
				if(col > 638 || row < 240 || row > 455){
					output[row*ncols+col] = 0;
				} else if(bw[row*ncols+col] == 0xFFFFFF){
					endFound = true;
					output[row*ncols+col] = 0xFFFFFF;
				}else if(!endFound && col > leftEnd && col < rightEnd){
					output[row*ncols + col] = 0xF63FFC;
				}else{
					output[row*ncols + col] = 0x000000;
				}
			}
		}
	}


	/**Crops an array down
	 *
	 * @param output cropped array
	 * @param input array to crop
	 * @param ncols number of columns of pixels in the image
	 * @param nrows number of rows of pixels in the image
	 * @param width width of the array to crop to
	 * @param height height of the array to crop to
	 */
	public static void limitTo(byte[] output, byte[] input, int ncols, int nrows, int width, int height) {
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				//System.out.println(r*width+c);
				output[width*r+c]=input[r*ncols+c];
			}
		}
	}

	/** erosion filter used on pixels in a int[]
	 *
	 * @param pixels image to be eroded
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number of columns of pixels in the image
	 * @return byte[] of eroded image
	 */
	public static int[] removeNoise(int[] pixels, int nrows, int ncols) {
		int[] output = new int[nrows * ncols];
		for (int r = nrows/2; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				if(pixels[r * ncols + c] == 0xFFFFFF) {
					int whiteNeighbors = 0;
					//top left
					if((r - 1) > 0 && (c - 1) > 0 && pixels[(r - 1) * ncols + (c - 1)] == 0xFFFFFF) {
						whiteNeighbors++;
					}
					//top
					if((r - 1) > 0 && pixels[(r - 1) * ncols + (c)] == 0xFFFFFF) {
						whiteNeighbors++;
					}
					//top right
					if((r - 1) > 0 && (c + 1) < ncols && pixels[(r - 1) * ncols + (c + 1)] == 0xFFFFFF) {
						whiteNeighbors++;
					}
					//left
					if((c - 1) > 0 && pixels[(r) * ncols + (c - 1)] == 0xFFFFFF) {
						whiteNeighbors++;
					}
					//right
					if((c + 1) < ncols && pixels[(r) * ncols + (c + 1)] == 0xFFFFFF) {
						whiteNeighbors++;
					}
					//bot left
					if((r + 1) < nrows && (c - 1) > 0 && pixels[(r + 1) * ncols + (c - 1)] == 0xFFFFFF) {
						whiteNeighbors++;
					}
					//bot
					if((r + 1) < nrows && pixels[(r + 1) * ncols + (c)] == 0xFFFFFF) {
						whiteNeighbors++;
					}
					//bot right
					if((r + 1) < nrows && (c + 1) < ncols && pixels[(r + 1) * ncols + (c + 1)] == 0xFFFFFF) {
						whiteNeighbors++;
					}
					if(whiteNeighbors > 7) {
						output[r * ncols + c] = 0xFFFFFF;
					}
					else {
						output[r * ncols + c] = 0;
					}
				}
			}
		}
		return output;
	}

	/** dilation filter used on pixels in a int[]
	 *
	 * @param pixels image to be dilated
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number of columns of pixels in the image
	 * @return byte[] of dilated image
	 */
	public static int[] dilate(int[] pixels, int nrows, int ncols) {
		int[] output = new int[nrows * ncols];
		for (int r = nrows/2; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				if(pixels[r * ncols + c] == 0) {
					//top left
					if((r - 1) > 0 && (c - 1) > 0 && pixels[(r - 1) * ncols + (c - 1)] == 0xFFFFFF) {
						output[r * ncols + c] = 0xFFFFFF;
					}
					//top
					else if((r - 1) > 0 && pixels[(r - 1) * ncols + (c)] == 0xFFFFFF) {
						output[r * ncols + c] = 0xFFFFFF;
					}
					//top right
					else if((r - 1) > 0 && (c + 1) < ncols && pixels[(r - 1) * ncols + (c + 1)] == 0xFFFFFF) {
						output[r * ncols + c] = 0xFFFFFF;
					}
					//left
					else if((c - 1) > 0 && pixels[(r) * ncols + (c - 1)] == 0xFFFFFF) {
						output[r * ncols + c] = 0xFFFFFF;
					}
					//right
					else if((c + 1) < ncols && pixels[(r) * ncols + (c + 1)] == 0xFFFFFF) {
						output[r * ncols + c] = 0xFFFFFF;
					}
					//bot left
					else if((r + 1) < nrows && (c - 1) > 0 && pixels[(r + 1) * ncols + (c - 1)] == 0xFFFFFF) {
						output[r * ncols + c] = 0xFFFFFF;
					}
					//bot
					else if((r + 1) < nrows && pixels[(r + 1) * ncols + (c)] == 0xFFFFFF) {
						output[r * ncols + c] = 0xFFFFFF;
					}
					//bot right
					else if((r + 1) < nrows && (c + 1) < ncols && pixels[(r + 1) * ncols + (c + 1)] == 0xFFFFFF) {
						output[r * ncols + c] = 0xFFFFFF;
					}
				}
				else {
					output[r * ncols + c] = 0xFFFFFF;
				}
			}
		}
		return output;
	}

	/**
	 *
	 * @param x
	 * @param y
	 * @param tile				0 = X0 X0	1 = 0X 0X	2 = 00 00	3 = 00 00	4 = X00	  5 = 0X0	6 = 0X0	  7 = 00X
	 * 								00 00		00 0X		X0 X0		0X 00		X00		  0X0		0X0		  00X
	 * @param ncols
	 * @param nrows
	 * @return
	 */
	public static int getPos(int x, int y, byte tile, int ncols, int nrows){
		//return (y*ncols*(3*boolBit(bayer)+1)+2*x+getBit(tile,1)*(boolBit(bayer)+1)*ncols+getBit(tile,0))%((3*boolBit(bayer)+1)*ncols*nrows);
		return (y*ncols*(4-getBit(tile,2))+(2+getBit(tile,2))*x+getBit(tile,1)*(2*ncols-(2*ncols-1)*getBit(tile,2))+getBit(tile,0));
	}

	public static void setLuminanceMultiplier(double multiplier) {
		luminanceMultiplier = multiplier;
	}

	public static int getBit(byte tile, int pos){
		return (tile >> pos) & 1;
	}

	public static byte combineTile(byte tile1, byte tile2){
		return (byte)(((int)tile1)^((int)tile2));
	}

}


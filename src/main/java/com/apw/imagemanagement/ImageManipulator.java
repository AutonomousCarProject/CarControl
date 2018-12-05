package com.apw.imagemanagement;

import java.util.ArrayList;
import java.util.List;

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
 * @see com.apw.imagemanagement
 */
public class ImageManipulator {

	private static double luminanceMultiplier = 1;

	/** Converts a bayer8 image to a monochrome image, uses the green value of the bayer8
	 *
	 * @param bayer bayer8 image
	 * @param mono	monochrome output
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number of columns of pixels in the image
	 * @param tile tiling pattern of the bayer8 image
	 */
	public static void convertToMonochromeRaster(byte[] bayer, byte[] mono, int nrows, int ncols, byte tile) {

		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				mono[r * ncols + c] = (byte) ((((int) bayer[(r * ncols * 2 + c) * 2 + 1]) & 0xFF));            //Use only top right
			}
		}
	}

	public static void convertToMonochrome2Raster(byte[] bayer, byte[] mono, int nrows, int ncols, byte tile) {

		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {

				int R = (bayer[getPos(c,r,combineTile((byte)0,tile),ncols,nrows)]&0xFF);
				int G = (bayer[getPos(c,r,combineTile((byte)1,tile),ncols,nrows)]&0xFF);
				int B = (bayer[getPos(c,r,combineTile((byte)3,tile),ncols,nrows)]&0xFF);
				//double Y = R *  .299000 + G *  .587000 + B *  .114000;

				double Y = (R+G+B)/3;
				mono[r * ncols + c] = (byte) Y;
			}
		}
	}


	private static int getRGBPixel(byte[] bayer, int column, int row, byte tile, int nCols, int nRows) {
		int R = (bayer[getPos(column, row, combineTile((byte) 0, tile), nCols, nRows)] & 0xFF);
		int G = (bayer[getPos(column, row, combineTile((byte) 1, tile), nCols, nRows)] & 0xFF);
		int B = (bayer[getPos(column, row, combineTile((byte) 3, tile), nCols, nRows)] & 0xFF);
		return (R<<16)+(G<<8)+B;
	}

	public static int[] convertToBlackWhiteRaster(byte[] bayer, int numRows, int numColumns, int frameWidth, byte tile) {
		int[] pixels = new int[numColumns * numRows];
		if (bayer != null) {
			int white = 0xffffff;
			int black = 0;
			ArrayList<Integer> previousColors = new ArrayList<>();
			double averageColor;
			int numPastFrames = 10;
			double multiplier = 0.47;

			for (int i = 1; i <= numPastFrames; i++) {
				previousColors.add(getRGBPixel(bayer, frameWidth - i, numRows - 1, tile, numColumns, numRows));
			}

			double averageRightColor = 0;
			//FIXME Make 250 a percentage
			for (int row = 250; row < numRows; row++) {
				averageRightColor += getRGBPixel(bayer, frameWidth - 1, row, tile, numColumns, numRows);
			}
			averageRightColor = averageRightColor / (numRows - 250);

			for (int pixelIdx = pixels.length - 1; pixelIdx > 0; pixelIdx--) {
				//FIXME Make 250 a percentage
				if (pixelIdx < 251 * numColumns) {
					pixels[pixelIdx] = black;
					continue;
				} else if (pixelIdx % numColumns >= frameWidth) {
					pixels[pixelIdx] = black;
					continue;
				}
				int currentRow = getRowFromIndex(pixelIdx, numColumns);
				int currentColumn = getColumnFromIndex(pixelIdx, numColumns);
				int currentPixel = getRGBPixel(bayer, currentColumn, currentRow, tile, numColumns, numRows);
				averageColor = averageArray(previousColors);

				//FIXME Same a above.
				if (Math.abs(currentPixel - averageColor) > multiplier * averageColor) {
					pixels[pixelIdx] = white;
				} else {
					pixels[pixelIdx] = black;
				}

				if (pixelIdx % numColumns == frameWidth - 1) {
					previousColors.clear();
					for (int i = 0; i < numPastFrames; i++) {
						previousColors.add(getRGBPixel(bayer, getColumnFromIndex(pixelIdx - i, numColumns),
								getRowFromIndex(pixelIdx - i, numColumns), tile, numColumns, numRows));
					}
					if (averageArray(previousColors) > (1 + multiplier) * averageRightColor) {
						previousColors.clear();
						for (int i = 0; i < numPastFrames; i++) {
							previousColors.add((int) averageRightColor);
						}
					}
				}
				if (pixels[pixelIdx] == black || (currentPixel != white && frameWidth != numColumns)) {
					previousColors.add(currentPixel);
					previousColors.remove(0);
				}
			}
		}

		if (numColumns == frameWidth) {
			pixels = removeNoise2(pixels);
		}

		return pixels;
	}

	private static double averageArray(List<Integer> list) {
		int sum = 0;
		for (int num : list) {
			sum += num;
		}
		return sum / list.size();
	}

	private static int[] removeNoise2(int pixels[]) {
		int whiteWidth = 0;
		for (int idx = 0; idx < pixels.length; idx++) {

			while (pixels[idx] == 0xffffff && idx < pixels.length) {
				whiteWidth++;
				idx++;
			}
			if (5 >= whiteWidth || whiteWidth >= 40 ) {
				for (int idxToRemove = idx; idxToRemove >= idx - whiteWidth; idxToRemove--) {
					pixels[idxToRemove] = 0;
				}
			}
			whiteWidth = 0;
		}
		return pixels;
	}

    private static int getRowFromIndex(int index, int numCols) {
		return index / numCols;
	}

	private static int getColumnFromIndex(int index, int numCols) {
		return index % numCols;
	}

    private static int getNumberFromCord(int x, int y, int frameWidth) {
        return (y * frameWidth) + x;
    }

	public static void convertToBlackWhiteRaster(byte[] bayer, int[] mono, int nrows, int ncols, int frameWidth, byte tile) {
		if (bayer == null) {
			return;
		}
		int pixelsAveraged = 3;
		for (int r = 240; r < nrows; r++) {
			int averageLuminance = 0;
			for(int c = 0; c < frameWidth; c++) {
				int R = bayer[getPos(c, r, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF;
				int G = bayer[getPos(c, r, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF;
				int B = bayer[getPos(c, r, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF;
				averageLuminance += (R + G + B);
			}


			int borderWidth = pixelsAveraged >> 1; //int division
			for (int c = borderWidth; c < frameWidth-borderWidth; c++) {
				int pix = 0;
				for(int i = 0; i < pixelsAveraged; i++) {
					pix += (bayer[getPos(c-borderWidth + i,r,combineTile((byte)0,tile),ncols,nrows)]&0xFF);
					pix += (bayer[getPos(c-borderWidth + i,r,combineTile((byte)1,tile),ncols,nrows)]&0xFF);
					pix += (bayer[getPos(c-borderWidth + i,r,combineTile((byte)3,tile),ncols,nrows)]&0xFF);
				}
				if (pix * frameWidth > luminanceMultiplier * averageLuminance * pixelsAveraged) {
					mono[r * ncols + c] = 0xFFFFFF;
				} else {
					mono[r * ncols + c] = 0;
				}
			}
		}
	}

	public static void convertToBlackWhite2Raster(byte[] bayer, byte[] mono, int nrows, int ncols, byte tile) {
		for (int r = nrows/2; r < nrows; r++) {
			int averageLuminance = 0;
			for(int c = 0; c < ncols; c++) {
				int R = (bayer[getPos(c,r,combineTile((byte)0,tile),ncols,nrows)]&0xFF);
				int G = (bayer[getPos(c,r,combineTile((byte)1,tile),ncols,nrows)]&0xFF);
				int B = (bayer[getPos(c,r,combineTile((byte)3,tile),ncols,nrows)]&0xFF);
				if(c == 0){
					averageLuminance = (R+G+B)/3;
				}
				if(!(c >= 640 || r < 240 || r > 455)) {
					if ((averageLuminance + (R+G+B)/3)/2 > averageLuminance * 1.5) {
						mono[r * ncols + c] = 1;
					} else {
						mono[r * ncols + c] = 0;
					}
				} else {
					mono[r * ncols + c] = 0;
				}
				averageLuminance = (averageLuminance + (R+G+B)/3)/2;
			}
		}
	}

	public static void convertToRobertsCrossRaster(int[] input, int[] output, int nrows, int ncols) {
		for (int r = 0; r < nrows - 1; r++) {
			for(int c = 0; c < ncols - 1; c++) {
				output[r * ncols + c] = Math.abs(input[r * ncols + c] - input[(r+1) * ncols + (c+1)])
						+ Math.abs(input[r * ncols + (c+1)] - input[(r+1) * ncols + c]);

			}
		}
	}


	/** erosion filter used on pixels in a byte[]
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

	/** dilation filter used on pixels in a byte[]
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

	/** Converts a bayer8 image to a simple color image, the simple colors are red, green, blue, yellow, white, grey and black
	 *
	 * @param bayer bayer8 image
	 * @param simple simple color output
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number og columns of pixels in the image
	 * @param tile tiling pattern of the bayer8 image
	 */
	public static void convertToSimpleColorRaster(byte[] bayer, byte[] simple, int nrows, int ncols, int frameWidth, byte tile) {
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

		if (bayer == null) {
			return;
		}

		for(int r = 0; r < nrows; r++){
			for(int c = 0; c < frameWidth; c++){
				int R = (bayer[getPos(c,r,combineTile((byte)0,tile),ncols,nrows)]&0xFF);
				int G = (bayer[getPos(c,r,combineTile((byte)1,tile),ncols,nrows)]&0xFF);
				int B = (bayer[getPos(c,r,combineTile((byte)3,tile),ncols,nrows)]&0xFF);
				//int B = (((int)bayer[(r*ncols*2 + c)*2 + 1+2*ncols-ncols*2*getBit(tile,1)-getBit(tile,0)])&0xFF);			//Bottom right (blue)
				double Y = R *  .299000 + G *  .587000 + B *  .114000;
				double U  = R * -.168736 + G * -.331264 + B *  .500000 + 128;
				double V = R *  .500000 + G * -.418688 + B * -.081312 + 128;
				R =(int)(  1.4075 * (V - 128));
				G = (int)(0- 0.3455 * (U - 128) - (0.7169 * (V - 128)));
				B = (int)(1.7790 * (U - 128));
				//If one of the colors has a value 50 greater than both other colors
				//it assigns that pixel to that color
				if(R > G+SimpleThresholds.redGreen && R > B+SimpleThresholds.redBlue){
					simple[r*ncols+c] = 0;
				} else if(G > R+SimpleThresholds.greenRed && G > B+SimpleThresholds.greenBlue){
					simple[r*ncols+c] = 1;
				} else if(B > R+SimpleThresholds.blueRed && B > G+SimpleThresholds.blueGreen){
					simple[r*ncols+c] = 2;
				}else if(R<G+SimpleThresholds.yellowDiff&&G<R+SimpleThresholds.yellowDiff&&(R>B+SimpleThresholds.yellowBlue)){
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
	 * @param bayer bayer8 image
	 * @param rgb	rgb output
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number og columns of pixels in the image
	 * @param tile tiling pattern of the bayer8 image
	 */
	public static void convertToRGBRaster(byte[] bayer, int[] rgb, int nrows, int ncols, byte tile) {
	    if (bayer == null) {
	    	return;
		}
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				int pix = getRGBPixel(bayer, c, r, tile, ncols, nrows);
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
	 * @param simpleByte black and white byte array(input)
	 * @param mono black and white int array(output)
	 * @param length length of the array
	 */
	public static void convertBWToRGB(int[] simpleByte, int[] mono, int length) {
		for (int i = 0; i < length; i++) {
			switch (simpleByte[i]) {
				case 0:
					mono[i] = 0x000000;
					break;
				case 1:
					mono[i] = 0xFFFFFF;
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
	 * @param ncols number og columns of pixels in the image
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

	public static void byteRGB(byte[] bayer, byte[] rgb, int ncols, int nrows, byte tile){
		//System.out.println(ncols+", "+nrows);

		for(int r = 0; r<nrows;r++){
			for(int c = 0; c<ncols;c++){
				(rgb[getPos(c,r,combineTile((byte)0,(byte)4),ncols,nrows)])=(bayer[getPos(c,r,combineTile((byte)0,tile),ncols,nrows)]);
				(rgb[getPos(c,r,combineTile((byte)1,(byte)4),ncols,nrows)])=(bayer[getPos(c,r,combineTile((byte)1,tile),ncols,nrows)]);
				(rgb[getPos(c,r,combineTile((byte)3,(byte)4),ncols,nrows)])=(bayer[getPos(c,r,combineTile((byte)3,tile),ncols,nrows)]);
				//System.out.println(getPos(c,r,combineTile((byte)0,(byte)4),ncols,nrows)+", "+getPos(c,r,combineTile((byte)1,(byte)4),ncols,nrows)+", "+getPos(c,r,combineTile((byte)3,(byte)4),ncols,nrows));
				//System.out.println(getPos(c,r,combineTile((byte)0,tile),ncols,nrows)+", "+getPos(c,r,combineTile((byte)1,tile),ncols,nrows)+", "+getPos(c,r,combineTile((byte)3,tile),ncols,nrows));
				//System.out.println();
			}
			//System.out.println(getPos(ncols-1,r-1,combineTile((byte)0,tile),ncols,nrows)+", "+getPos(ncols-1,r-1,combineTile((byte)1,tile),ncols,nrows)+", "+getPos(ncols-1,r-1,combineTile((byte)3,tile),ncols,nrows));
			//System.out.println(getPos(0,r,combineTile((byte)0,tile),ncols,nrows)+", "+getPos(0,r,combineTile((byte)1,tile),ncols,nrows)+", "+getPos(0,r,combineTile((byte)3,tile),ncols,nrows));

			//System.out.println(getPos(0,r,combineTile((byte)0,(byte)4),ncols,nrows)+", "+getPos(0,r,combineTile((byte)1,(byte)4),ncols,nrows)+", "+getPos(0,r,combineTile((byte)3,(byte)4),ncols,nrows));
		}
	}


	public static void limitTo(byte[] output, byte[] input, int ncols, int nrows, int width, int height) {
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				System.out.println(r*width+c);
				output[width*r+c]=input[r*ncols+c];
			}
		}
	}

	public static void limitTo(byte[] output, byte[] input, int ncols, int nrows, int width, int height, boolean bayer) {
		//if(bayer){
		//	width*=2;
		//	height*=2;
		//}
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				//System.out.println(r*width+c);
				output[width * r + c] = input[r * ncols + c];
			}
		}

	}

	public static void setLuminanceMultiplier(double multiplier) {
		luminanceMultiplier = multiplier;
	}

	public static int getBit(byte tile, int pos){
		return (tile >> pos) & 1;
	}
	public static int boolBit(boolean check){
		if(check) return 1;
		return 0;
	}

	public static void smooth(byte[] input,byte[] output, int ncols, int nrows, byte tile){
		for(int r = 0; r<nrows;r++){
			output[getPos(0,r,combineTile((byte)0,tile),ncols,nrows)] = input[getPos(0,r,combineTile((byte)0,tile),ncols,nrows)];
			int R2 = (input[getPos(0,r,combineTile((byte)0,tile),ncols,nrows)]&0xFF);
			int G2 = (input[getPos(0,r,combineTile((byte)1,tile),ncols,nrows)]&0xFF);
			int B2 = (input[getPos(0,r,combineTile((byte)3,tile),ncols,nrows)]&0xFF);
			double Y2 = R2 *  .299000 + G2 *  .587000 + B2 *  .114000;
			double Y = Y2;
			//output[getPos(ncols-1,r,combineTile((byte)0,tile),ncols,nrows,true)] = input[getPos(ncols-1,r,combineTile((byte)0,tile),ncols,nrows,true)];
			for(int c = 1; c<ncols;c++){
				//int R1 = (input[getPos(c-1,r,combineTile((byte)0,tile),ncols,nrows,true)]&0xFF);
				//int G1 = (input[getPos(c-1,r,combineTile((byte)1,tile),ncols,nrows,true)]&0xFF);
				//int B1 = (input[getPos(c-1,r,combineTile((byte)3,tile),ncols,nrows,true)]&0xFF);
				R2 = (input[getPos(c,r,combineTile((byte)0,tile),ncols,nrows)]&0xFF);
				G2 = (input[getPos(c,r,combineTile((byte)1,tile),ncols,nrows)]&0xFF);
				B2 = (input[getPos(c,r,combineTile((byte)3,tile),ncols,nrows)]&0xFF);
				//int R3 = (input[getPos(c+1,r,combineTile((byte)0,tile),ncols,nrows,true)]&0xFF);
				//int G3 = (input[getPos(c+1,r,combineTile((byte)1,tile),ncols,nrows,true)]&0xFF);
				//int B3 = (input[getPos(c+1,r,combineTile((byte)3,tile),ncols,nrows,true)]&0xFF);
				//double Y1 = R1 *  .299000 + G1 *  .587000 + B1 *  .114000;
				Y2 = R2 *  .299000 + G2 *  .587000 + B2 *  .114000;
				//double Y3 = R3 *  .299000 + G3 *  .587000 + B3 *  .114000;
				//double Y = (Y1+Y2*2+Y3)/4;
				Y += Y2;
				Y/=2;
				double U  = R2 * -.168736 + G2 * -.331264 + B2 *  .500000 + 128;
				double V = R2 *  .500000 + G2 * -.418688 + B2 * -.081312 + 128;
				double R = Y + 1.4075 * (V - 128);
				double G = Y - 0.3455 * (U - 128) - (0.7169 * (V - 128));
				double B = Y + 1.7790 * (U - 128);
				output[getPos(c,r,combineTile((byte)0,tile),ncols,nrows)] = (byte)R;
				output[getPos(c,r,combineTile((byte)1,tile),ncols,nrows)] = (byte)G;
				output[getPos(c,r,combineTile((byte)2,tile),ncols,nrows)] = (byte)G;
				output[getPos(c,r,combineTile((byte)3,tile),ncols,nrows)] = (byte)B;
			}
		}
	}

	/**
	 *
	 * @param x
	 * @param y
	 * @param tile				0 = X0	1 = 0X	2 = 00	3 = 00
	 * 								00		00		X0		0X
	 * @param ncols
	 * @param nrows
	 * @return
	 */
	public static int getPos(int x, int y, byte tile, int ncols, int nrows){
		//return (y*ncols*(3*boolBit(bayer)+1)+2*x+getBit(tile,1)*(boolBit(bayer)+1)*ncols+getBit(tile,0))%((3*boolBit(bayer)+1)*ncols*nrows);
		return (y * ncols * (4 - getBit(tile, 2)) + (2 + getBit(tile, 2)) * x + getBit(tile, 1) *
                (2 * ncols - (2 * ncols - 1) * getBit(tile, 2)) + getBit(tile, 0)) %
                ((4 - getBit(tile, 2)) * ncols * nrows);
	}
	public static byte combineTile(byte tile1, byte tile2){
		return (byte)(((int)tile1)^((int)tile2));
	}

}


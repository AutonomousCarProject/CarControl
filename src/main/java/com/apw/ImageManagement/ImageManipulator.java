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

package com.apw.ImageManagement;

public class ImageManipulator {

	/** Converts a bayer8 image to a monochrome image, uses the green value of the bayer8
	 *
	 * @param bayer bayer8 image
	 * @param mono	monochrome output
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number og columns of pixels in the image
	 * @param tile tiling pattern of the bayer8 image
	 */
    public static void convertToMonochromeRaster(byte[] bayer, byte[] mono, int nrows, int ncols, byte tile) {

        for (int r = 0; r < nrows; r++) {
            for (int c = 0; c < ncols; c++) {
                mono[r * ncols + c] = (byte) ((((int) bayer[(r * ncols * 2 + c) * 2 + 1]) & 0xFF));            //Use only top right
            }
        }
    }


	/** Converts a bayer8 image to a monochrome image, uses the the average rgb value of the bayer8
	 *
	 * @param bayer bayer8 image
	 * @param mono	monochrome output
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number og columns of pixels in the image
	 * @param tile tiling pattern of the bayer8 image
	 */
	public static void convertToMonochrome2Raster(byte[] bayer, byte[] mono, int nrows, int ncols, byte tile) {

		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {

				/*
				//Averaging all colors
				int total = bayer[r*ncols*2 + c*2] 		//Top left (
						+ bayer[r*ncols*2 + c*2+1] 		//Top right
						+ bayer[(r+1)*ncols*2 + c*2]*2;	//Bottom left
				mono[r*ncols + c] = (byte) (total >> 2);
				*/
				int R = (bayer[getPos(c,r,combineTile((byte)0,tile),ncols,nrows,true)]&0xFF);
				int G = (bayer[getPos(c,r,combineTile((byte)1,tile),ncols,nrows,true)]&0xFF);
				int B = (bayer[getPos(c,r,combineTile((byte)3,tile),ncols,nrows,true)]&0xFF);
				//double Y = R *  .299000 + G *  .587000 + B *  .114000;

				double Y = (R+G+B)/3;
				mono[r * ncols + c] = (byte) Y;
			}
		}
	}



	public static void convertToBlackWhiteRaster(byte[] bayer, byte[] mono, int nrows, int ncols, byte tile) {
    	for (int r = 0; r < nrows; r++) {
    		int averageLuminance = 0;
        	for(int c = 0; c < ncols; c++) {
				int R = (bayer[getPos(c,r,combineTile((byte)0,tile),ncols,nrows,true)]&0xFF);
				int G = (bayer[getPos(c,r,combineTile((byte)1,tile),ncols,nrows,true)]&0xFF);
				int B = (bayer[getPos(c,r,combineTile((byte)3,tile),ncols,nrows,true)]&0xFF);
				averageLuminance += R+G+B/3;

			}
			averageLuminance /= ncols;

            for (int c = 0; c < ncols; c++) {

				int R = (bayer[getPos(c,r,combineTile((byte)0,tile),ncols,nrows,true)]&0xFF);
				int G = (bayer[getPos(c,r,combineTile((byte)1,tile),ncols,nrows,true)]&0xFF);
				int B = (bayer[getPos(c,r,combineTile((byte)3,tile),ncols,nrows,true)]&0xFF);

				int pix =(R+G+B)/3;
				if(!(c >= 640 || r < 240 || r > 455)) {
					if (pix >  1.8*averageLuminance) {
						mono[r * ncols + c] = 1;
					} else {
						mono[r * ncols + c] = 0;

					}
				} else {
					mono[r * ncols + c] = 0;
				}
			}
		}
	}


	/** Converts a bayer8 image to a simple color image, the simple colors are red, green, blue, yellow, white, grey and black
	 *
	 * @param bayer bayer8 image
	 * @param simple simple color output
	 * @param nrows	number of rows of pixels in the image
	 * @param ncols number og columns of pixels in the image
	 * @param tile tiling pattern of the bayer8 image
	 */
	public static void convertToSimpleColorRaster(byte[] bayer, byte[] simple, int nrows, int ncols, byte tile) {
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
			for(int c = 0; c < ncols; c++){
				int R = (bayer[getPos(c,r,combineTile((byte)0,tile),ncols,nrows,true)]&0xFF);
				int G = (bayer[getPos(c,r,combineTile((byte)1,tile),ncols,nrows,true)]&0xFF);
				int B = (bayer[getPos(c,r,combineTile((byte)3,tile),ncols,nrows,true)]&0xFF);
				//int B = (((int)bayer[(r*ncols*2 + c)*2 + 1+2*ncols-ncols*2*getBit(tile,1)-getBit(tile,0)])&0xFF);			//Bottom right (blue)
                double Y = R *  .299000 + G *  .587000 + B *  .114000;
				double U  = R * -.168736 + G * -.331264 + B *  .500000 + 128;
				double V = R *  .500000 + G * -.418688 + B * -.081312 + 128;
				R =(int)(  1.4075 * (V - 128));
				G = (int)(0- 0.3455 * (U - 128) - (0.7169 * (V - 128)));
				B = (int)(1.7790 * (U - 128));
				//If one of the colors has a value 50 greater than both other colors
				//it assigns that pixel to that color
				if(R > G+55 && R > B+55){
					simple[r*ncols+c] = 0;
				} else if(G > R+45 && G > B+45){
					simple[r*ncols+c] = 1;
				} else if(B > R+45 && B > G+45){
					simple[r*ncols+c] = 2;
				}else if(R<G+20&&G<R+20&&(R>B+45)){
					simple[r*ncols+c] = 6;
				}
				//Otherwise it sees if one of the colors has a value above 170 for white
				// if not, 85 for grey and below 85 for black
				else if(Y>170){
					simple[r*ncols+c] = 3;
				} else if(Y>50){
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
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				int R = (bayer[getPos(c,r,combineTile((byte)0,tile),ncols,nrows,true)]&0xFF);
				int G = (bayer[getPos(c,r,combineTile((byte)1,tile),ncols,nrows,true)]&0xFF);
				int B = (bayer[getPos(c,r,combineTile((byte)3,tile),ncols,nrows,true)]&0xFF);
				int pix =(R<<16)+(G<<8)+B;
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
    public static void convertBWToRGB(byte[] simpleByte, int[] mono, int length) {
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

    public static void findRoad(byte[] bayer, int[] output, int nrows, int ncols, byte tile){
    	for(int col = 0; col < ncols; col++){
			int averageLuminance = 0;
			for(int row = nrows-1; row > 0; row--) {
				int R = ((((int)bayer[(row*ncols*2 + col)*2+getBit(tile,0)+ncols*2*getBit(tile,1)]) & 0xFF));				//Top left (red)
				int G = ((((int)bayer[(row*ncols*2 + col)*2 +1-getBit(tile,0)])&0xFF)); 			//Top right (green)
				int B = (((int)bayer[(row*ncols*2 + col)*2 + 1+2*ncols-ncols*2*getBit(tile,1)-getBit(tile,0)])&0xFF);			//Bottom right (blue)
				averageLuminance += R+G+B/3;
			}
			averageLuminance /= ncols;
    		boolean endFound = false;

    		for(int row = nrows-1; row > 0; row--){
				int R = ((((int)bayer[(row*ncols*2 + col)*2+getBit(tile,0)+ncols*2*getBit(tile,1)]) & 0xFF));				//Top left (red)
				int G = ((((int)bayer[(row*ncols*2 + col)*2 +1-getBit(tile,0)])&0xFF)); 			//Top right (green)
				int B = (((int)bayer[(row*ncols*2 + col)*2 + 1+2*ncols-ncols*2*getBit(tile,1)-getBit(tile,0)])&0xFF);			//Bottom right (blue)
				int pix =(R+G+B)/3;
				if(col >= 640 || row < 240 || row > 455){
					output[row*ncols+col] = 0;
				} else if(pix>averageLuminance){
					endFound = true;
					output[row*ncols+col] = 0xFFFFFF;
				}else if(!endFound){
					output[row*ncols + col] = 0xF63FFC;
				}else{
					output[row*ncols + col] = 0x000000;
				}
			}
		}
	}


	public static void limitTo(int[] output, int[] input, int ncols, int nrows, int width, int height) {
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				//System.out.println(r*width+c);
				output[width*r+c]=input[r*ncols+c];
			}

		}

	}
	public static void limitTo(byte[] output, byte[] input, int ncols, int nrows, int width, int height) {
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				//System.out.println(r*width+c);
				output[width*r+c]=input[r*ncols+c];
			}

		}

	}
	public static int getBit(byte tile, int pos){
        return (tile >> pos) & 1;
    }
    public static int boolBit(boolean check){
    	if(true) return 1;
    	return 0;
	}

    public static void smooth(byte[] input,byte[] output, int ncols, int nrows, byte tile){
    	for(int r = 0; r<nrows;r++){
			output[getPos(0,r,combineTile((byte)0,tile),ncols,nrows,true)] = input[getPos(0,r,combineTile((byte)0,tile),ncols,nrows,true)];
			output[getPos(ncols-1,r,combineTile((byte)0,tile),ncols,nrows,true)] = input[getPos(ncols-1,r,combineTile((byte)0,tile),ncols,nrows,true)];
			for(int c = 1; c<ncols-1;c++){
				int R1 = (input[getPos(c-1,r,combineTile((byte)0,tile),ncols,nrows,true)]&0xFF);
				int G1 = (input[getPos(c-1,r,combineTile((byte)1,tile),ncols,nrows,true)]&0xFF);
				int B1 = (input[getPos(c-1,r,combineTile((byte)3,tile),ncols,nrows,true)]&0xFF);
				int R2 = (input[getPos(c,r,combineTile((byte)0,tile),ncols,nrows,true)]&0xFF);
				int G2 = (input[getPos(c,r,combineTile((byte)1,tile),ncols,nrows,true)]&0xFF);
				int B2 = (input[getPos(c,r,combineTile((byte)3,tile),ncols,nrows,true)]&0xFF);
				int R3 = (input[getPos(c+1,r,combineTile((byte)0,tile),ncols,nrows,true)]&0xFF);
				int G3 = (input[getPos(c+1,r,combineTile((byte)1,tile),ncols,nrows,true)]&0xFF);
				int B3 = (input[getPos(c+1,r,combineTile((byte)3,tile),ncols,nrows,true)]&0xFF);
				double Y1 = R1 *  .299000 + G1 *  .587000 + B1 *  .114000;
				double Y2 = R2 *  .299000 + G2 *  .587000 + B2 *  .114000;
				double Y3 = R3 *  .299000 + G3 *  .587000 + B3 *  .114000;
				double Y = (Y1+Y2*2+Y3)/4;
				double U  = R2 * -.168736 + G2 * -.331264 + B2 *  .500000 + 128;
				double V = R2 *  .500000 + G2 * -.418688 + B2 * -.081312 + 128;
				double R = Y + 1.4075 * (V - 128);
				double G = Y - 0.3455 * (U - 128) - (0.7169 * (V - 128));
				double B = Y + 1.7790 * (U - 128);
				output[getPos(c,r,combineTile((byte)0,tile),ncols,nrows,true)] = (byte)R;
				output[getPos(c,r,combineTile((byte)1,tile),ncols,nrows,true)] = (byte)G;
				output[getPos(c,r,combineTile((byte)2,tile),ncols,nrows,true)] = (byte)G;
				output[getPos(c,r,combineTile((byte)3,tile),ncols,nrows,true)] = (byte)B;
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
	public static int getPos(int x, int y, byte tile, int ncols, int nrows, boolean bayer){
		return (y*ncols*(3*boolBit(bayer)+1)+2*x+getBit(tile,1)*(boolBit(bayer)+1)*ncols+getBit(tile,0))%((3*boolBit(bayer)+1)*ncols*nrows);
	}
	public static byte combineTile(byte tile1, byte tile2){
		return (byte)(((int)tile1)^((int)tile2));
	}


}


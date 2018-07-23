package com.apw.ImageManagement;

public class ImageManipulator {

	public static void convertToMonochromeRaster(byte[] bayer, byte[] mono, int nrows, int ncols) {

		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				
				/*
				//Averaging all colors
				int total = bayer[r*ncols*2 + c*2] 		//Top left (
						+ bayer[r*ncols*2 + c*2+1] 		//Top right
						+ bayer[(r+1)*ncols*2 + c*2]*2;	//Bottom left
				mono[r*ncols + c] = (byte) (total >> 2);
				*/
				mono[r*ncols + c] = (byte)((((int)bayer[(r*ncols*2 + c)*2 +1])&0xFF)); 			//Use only top right (green)
			}
		}
	}
	public static void convertToBlackWhiteRaster(byte[] bayer, byte[] mono, int nrows, int ncols){
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {

				/*
				//Averaging all colors
				int total = bayer[r*ncols*2 + c*2] 		//Top left (
						+ bayer[r*ncols*2 + c*2+1] 		//Top right
						+ bayer[(r+1)*ncols*2 + c*2]*2;	//Bottom left
				mono[r*ncols + c] = (byte) (total >> 2);
				*/
				int R = ((((int)bayer[(r*ncols*2 + c)*2]) & 0xFF));				//Top left (red)
				int G = ((((int)bayer[(r*ncols*2 + c)*2 +1])&0xFF)); 			//Top right (green)
				int B = (((int)bayer[(r*ncols*2 + c)*2 + 1+2*ncols])&0xFF);			//Bottom right (blue)
				int pix =R+G+B;
				if(pix>700){
					mono[r*ncols + c] = 1;
				}else{
					mono[r*ncols + c] = 0;
				}
			}
		}
	}
	
	public static void convertToSimpleColorRaster(byte[] bayer, byte[] simple, int nrows, int ncols) {
		/*
			*Built for RG/GB Bayer Configuration
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
				int R = ((((int)bayer[(r*ncols*2 + c)*2]) & 0xFF));				//Top left (red)
				int G = ((((int)bayer[(r*ncols*2 + c)*2 +1])&0xFF)); 			//Top right (green)
				int B = (((int)bayer[(r*ncols*2 + c)*2 + 1+2*ncols])&0xFF);			//Bottom right (blue)
				//If one of the colors has a value 50 greater than both other colors
				//it assigns that pixel to that color
				if(R > G+51 && R > B+51){
					simple[r*ncols+c] = 0;
				} else if(G > R+50 && G > B+50){
					simple[r*ncols+c] = 1;
				} else if(B > R+50 && B > G+50){
					simple[r*ncols+c] = 2;
				}else if(R<G+20&&G<R+20&&(R>B+50)){
					simple[r*ncols+c] = 6;
				}
				//Otherwise it sees if one of the colors has a value above 170 for white
				// if not, 85 for grey and below 85 for black
				else if(R > 170 || G > 170 || B > 170){
					simple[r*ncols+c] = 3;
				} else if(R > 85 || G > 85 || B > 85){
					simple[r*ncols+c] = 4; //0x808080
				} else if(R < 85 || G < 85 || B < 85) {
					simple[r * ncols + c] = 5;
				}
			}
		}
	}
	
	public static void convertToRGBRaster(byte[] bayer, int[] rgb, int nrows, int ncols) {
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				int R = ((((int)bayer[(r*ncols*2 + c)*2]) & 0xFF));				//Top left (red)
				int G = ((((int)bayer[(r*ncols*2 + c)*2 +1])&0xFF)); 			//Top right (green)
				int B = (((int)bayer[(r*ncols*2 + c)*2 + 1+2*ncols])&0xFF);			//Bottom right (blue)
				int pix =(R<<16)+(G<<8)+B;
				rgb[r*ncols + c] = pix;
			}
			
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
				case 6:
					simpleRGB[i] = 0xDDDD00;
					break;
			}
		}
	}

	public static void convertBWToRGB(byte[] simpleByte, int[] mono, int length){
		for(int i = 0; i < length; i++){
			switch(simpleByte[i]){
				case 0:
					mono[i] = 0x000000;
					break;
				case 1:
					mono[i] = 0xFFFFFF;
					break;
			}
		}
	}

	public static void convertMonotoRGB(byte[] mono, int[] rgb, int length){
		for(int i = 0; i < length; i++){
			rgb[i] =(mono[i]<<16)+(mono[i]<<8)+mono[i];
		}
	}

	public static void limitTo(int[] output, int[] input, int ncols, int nrows, int width, int height, boolean bayer) {
		//if(bayer){
		//	width*=2;
		//	height*=2;
		//}
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				//System.out.println(r*width+c);
				output[width*r+c]=input[r*ncols+c];
			}

		}

	}



}

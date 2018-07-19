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
				}
				//Otherwise it sees if one of the colors has a value above 170 for white
				// if not, 85 for grey and below 85 for black
				else if(R > 170 || G > 170 || B > 170){
					simple[r*ncols+c] = 3;
				} else if(R > 85 || G > 85 || B > 85){
					simple[r*ncols+c] = 4; //0x808080
				} else if(R < 85 || G < 85 || B < 85){
					simple[r*ncols+c] = 5;
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
}

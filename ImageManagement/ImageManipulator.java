package ImageManagement;

public class ImageManipulator {

	public static void convertToMonochromeRaster(byte[] bayer, byte[] mono, int nrows, int ncols) {

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
		for(int i = 0; i < nrows; i++){
			for(int j = 0; j < ncols; j++){
				int r = bayer[2*i*nrows+j*2];
				int g = bayer[2*i*nrows+j*2+1];
				int b = bayer[(2*i*nrows)+1+j+1];
				//If one of the colors has a value 50 greater than both other colors
				//it assigns that pixel to that color
				if(r > g+50 && r > b+50){
					simple[i*nrows+j] = 0;
				} else if(g > r+50 && g > b+50){
					simple[i*nrows+j] = 1;
				} else if(b > r+50 && b > g+50){
					simple[i*nrows+j] = 2;
				}
				//Otherwise it sees if one of the colors has a value above 170 for white
				// if not, 85 for grey and below 85 for black
				else if(r > 170 || g > 170 || b > 170){
					simple[i*nrows+j] = 3;
				} else if(r > 85 || g > 85 || b > 85){
					simple[i*nrows+j] = 4;
				} else if(r < 85 || g < 85 || b < 85){
					simple[i*nrows+j] = 5;
				}
			}
		}
	}
}

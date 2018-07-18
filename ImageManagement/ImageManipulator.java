package imagemanagement;

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
				mono[r*ncols + c] = bayer[r*ncols*2 + c*2 + 1];	//Use only top right (green)
			}
		}
	}
	
	public static void convertToSimpleColorRaster(byte[] bayer, byte[] simple, int nrows, int ncols) {
		
	}
	
	public static void convertToRGBRaster(byte[] bayer, int[] rgb, int nrows, int ncols) {
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				int pix = bayer[(r*ncols + c)*2] << 16				//Top left (red)
						+ bayer[(r*ncols + c)*2 +1] << 8 			//Top right (green)
						+ bayer[((r+1)*ncols + c)*2 + 1];			//Bottom right (blue)
				rgb[r*ncols + c] = pix;
			}
		}
	}
}

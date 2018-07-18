/*ImageManager: Retrieves and preprocesses images from the camera and displays feed onscreen*/

package ImageManagement;

import fly2cam.FlyCamera;

public class ImageManager {
	
	private ImagePicker picker;
<<<<<<< HEAD
	private byte[] mono;
	private int[] rgb;
	int nrows, ncols;
	
	/*Main*/
	public ImageManager(FlyCamera cam, int fps) {
		picker = new ImagePicker(cam, fps);
		this.nrows = picker.getRows();
		this.ncols = picker.getCols();
		mono = new byte[nrows * ncols];
		rgb = new int[nrows * ncols];
=======
	int nrows, ncols;
	private byte simple[];
	
	/*Main*/
	public ImageManager(SimCamera trakcam) {
		picker = new ImagePicker(trakcam, 30);
		nrows = picker.getNrows();
		ncols = picker.getNcols();
		simple = new byte[nrows*ncols];
>>>>>>> 1b875f393ed0b51b09a4e6facc110eb840a60fb3
	}
	
	/*Get number of rows and cols*/
	public int getRows() { return nrows; }
	public int getCols() { return ncols; }

	/*Serves monochrome raster of camera feed
	 * Formatted in 1D array of bytes*/
	public byte[] getMonochromeRaster() {
		ImageManipulator.convertToMonochromeRaster(picker.getPixels(), mono, nrows, ncols);
		return mono;
	}
	
	/*Serves color raster encoded in 1D of values 0-5 with
	 * 0 = RED
	 * 1 = GREEN
	 * 2 = BLUE
	 * 3 = WHITE
	 * 4 = GREY
	 * 5 = BLACK
	 */
	public byte[] getSimpleColorRaster() {
		ImageManipulator.convertToSimpleColorRaster(picker.getPixels(), simple, nrows, ncols);
		return simple;
	}
	
	/*Serves unchanged image in 1D array of ARGB ints*/
	public int[] getRGBRaster() {
		ImageManipulator.convertToRGBRaster(picker.getPixels(), rgb, nrows, ncols);
		return rgb;
	}
}

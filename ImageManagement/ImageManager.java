/*ImageManager: Retrieves and preprocesses images from the camera and displays feed onscreen*/

package imagemanagement;

import fly2cam.FlyCamera;

public class ImageManager {
	
	private ImagePicker picker;
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
	 * 4 = BLACK
	 * 6 = GREY
	 */
	public byte[] getSimpleColorRaster() {
		return null;
	}
	
	/*Serves unchanged image in 1D array of ARGB ints*/
	public int[] getRGBRaster() {
		ImageManipulator.convertToRGBRaster(picker.getPixels(), rgb, nrows, ncols);
		return rgb;
	}
}

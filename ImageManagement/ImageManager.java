/*ImageManager: Retrieves and preprocesses images from the camera and displays feed onscreen*/

package ImageManagement;

import apw3.SimCamera;

public class ImageManager {
	
	private ImagePicker picker;
	int nrows, ncols;
	private byte simple[];
	
	/*Main*/
	public ImageManager(SimCamera trakcam) {
		picker = new ImagePicker(trakcam, 30);
		nrows = picker.getNrows();
		ncols = picker.getNcols();
		simple = new byte[nrows*ncols];
	}

	/*Serves monochrome raster of camera feed
	 * Formatted in 1D array of bytes*/
	public byte[] getMonochromeRaster() {
		return null;
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
}

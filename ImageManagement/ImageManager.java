/*ImageManager: Retrieves and preprocesses images from the camera and displays feed onscreen*/

package ImageManagement;

import apw3.SimCamera;

public class ImageManager {
	
	/*Main*/
	public ImageManager(SimCamera trakcam) {
		
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
	 * 4 = BLACK
	 * 6 = GREY
	 */
	public byte getSimpleColorRaster() {
		return 0;
		
	}
}

package ImageManagement;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import apw3.SimCamera;

/*ImagePicker: Periodically retrieves an image from the camera (traksim feed or onboard cam)
*/

public class ImagePicker extends TimerTask {
	
	private SimCamera cam;
	private int fps;
	private byte[] pixels;
	private int numpixels;
	
	public ImagePicker(SimCamera cam, int fps) {
		//Keep camera ref and fps
		this.cam = cam;
		this.fps = fps;
		
		//Get number of pixels
		numpixels = cam.Dimz() >> 16 * (cam.Dimz() << 16) >> 16;
		pixels = new byte[numpixels];
		
		//Schedule task
		Timer pickerTaskTimer = new Timer();
		pickerTaskTimer.scheduleAtFixedRate(this, new Date(), (long) (1000/(float)fps));
		
	}

	@Override
	public void run() {
		cam.NextFrame(pixels);
	}
}
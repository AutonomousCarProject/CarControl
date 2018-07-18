package ImageManagement;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import apw3.SimCamera;
import fly2cam.FlyCamera;

/*ImagePicker: Periodically retrieves an image from the camera (traksim feed or onboard cam)
*/

public class ImagePicker extends TimerTask {
	
	private FlyCamera cam;
	private int fps;
	private byte[] pixels;
	private int nrows, ncols;
	
	public ImagePicker(FlyCamera cam, int fps) {
		//Keep camera ref and fps
		this.cam = cam;
		this.fps = fps;
		
		//Get number of pixels
		nrows = cam.Dimz() >> 16;
		ncols = cam.Dimz() << 16 >> 16;
		pixels = new byte[nrows * ncols * 4];
		
		//Schedule task
		Timer pickerTaskTimer = new Timer();
		pickerTaskTimer.scheduleAtFixedRate(this, new Date(), (long) (1000/this.fps));
	}
	
	byte[] getPixels() {
		return pixels;
	}
	
	int getRows() {
		return nrows;
	}
	
	int getCols() {
		return ncols;
	}
	public int getNrows(){
		return nrows;
	}

	public int getNcols(){
		return ncols;
	}

	public byte[] getPixels() {
		return pixels;
	}

	@Override
	public void run() {
		try {
			cam.NextFrame(pixels);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
}
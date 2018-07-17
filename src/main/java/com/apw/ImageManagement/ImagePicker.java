package com.apw.ImageManagement;

import apw3.SimCamera;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/*ImagePicker: Periodically retrieves an image from the camera (traksim feed or onboard cam)
*/

public class ImagePicker extends TimerTask {
	
	private SimCamera cam;
	private int fps;
	private byte[] pixels;
	private int nrows, ncols;
	
	public ImagePicker(SimCamera cam, int fps) {
		//Keep camera ref and fps
		this.cam = cam;
		this.fps = fps;
		
		//Get number of pixels
		nrows = cam.Dimz() >> 16;
		ncols = cam.Dimz() << 16 >> 16;
		pixels = new byte[nrows * ncols];
		
		//Schedule task
		Timer pickerTaskTimer = new Timer();
		pickerTaskTimer.scheduleAtFixedRate(this, new Date(), (long) (1000/(float)this.fps));
		
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
		cam.NextFrame(pixels);
	}
}
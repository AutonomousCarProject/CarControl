package com.apw.carcontrol;

import com.apw.fly2cam.FlyCamera;
import com.apw.sbcio.PWMController;

public class CamControl extends CarControlBase {

    public CamControl(PWMController driveSys) {
        super(new FlyCamera(), driveSys);

        //while (cam.Dimz() == 0);

        setWindowHeight(cam.Dimz() >> 16);
        setWindowWidth(cam.Dimz() << 16 >> 16);
    }

    @Override
    public byte[] readCameraImage() {
        try {
            return super.readCameraImage();
        } catch (IllegalStateException ex) {
            System.err.println("An error occurred while reading the camera image from FlyCamera.");
            return getRecentCameraImage();
        }
    }

    @Override
	public int getImageWidth() {
		return getWindowWidth();
	}

	@Override
	public int getImageHeight() {
		return getWindowHeight();
	}

	@Override
	public void drawLine(int color, int rx, int cx, int rz, int cz) {
    	// Not implemented
	}

	@Override
	public void updateWindowDims(int width, int height) {
    	// Not implemented
	}
}

package com.apw.carcontrol;

import com.apw.apw3.SimCamera;
import com.apw.sbcio.PWMController;

public class TrakSimControl extends CarControlBase {

    public TrakSimControl(PWMController drivesys) {
        super(new SimCamera(), drivesys);
        cam.Connect(4); // 30 FPS
    }

    @Override
    public byte[] readCameraImage() {
        try {
            return super.readCameraImage();
        } catch (IllegalStateException ex) {
            System.err.println("An error occurred in TrakSimControl while reading the camera image from SimCamera.");
            return getRecentCameraImage();
        }
    }
}

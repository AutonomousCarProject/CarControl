package com.apw.carcontrol;

import com.apw.apw3.SimCamera;
import com.apw.fly2cam.FlyCamera;
import com.apw.sbcio.PWMController;

public class CamControl extends CarControlBase {

    public CamControl(PWMController drivesys) {
        super(new FlyCamera(), drivesys);
        cam.Connect(4); // 30 FPS
    }

    @Override
    public byte[] readCameraImage() {
        try {
            return super.readCameraImage();
        } catch (IllegalStateException ex) {
            System.err.println("An error occurred in CamControl while reading the camera image from FlyCamera.");
            return getRecentCameraImage();
        }
    }
}

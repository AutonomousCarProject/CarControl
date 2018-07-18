package com.apw.fly2cam;

import com.apw.oldglobal.Log;
import com.apw.oldimage.IImage;
import com.apw.oldimage.IPixel;
import com.apw.oldimage.Image;

public class ToggleExposure implements IAutoExposure {
    private boolean shouldBoost = false;
    private OldFlyCamera flyCam;

    public ToggleExposure(IImage image) {
        if (image instanceof Image) {
            flyCam = ((Image) image).flyCam;
        } else {
            flyCam = null;
        }

    }

    @Override
    public void autoAdjust(IPixel[][] pixels) {
        if (flyCam == null) return;

        if (shouldBoost) {
            flyCam.SetShutter(75);
            sleep(5);
        } else {
            flyCam.SetShutter(25);
            sleep(5);
        }

        shouldBoost = !shouldBoost;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.e("ToggleExposure", "Thread interrupted while sleeping.");
        }
    }
}

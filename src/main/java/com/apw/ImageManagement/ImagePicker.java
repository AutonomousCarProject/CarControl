package com.apw.ImageManagement;


import com.apw.fly2cam.FlyCamera;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/*ImagePicker: Periodically retrieves an image from the camera (traksim feed or onboard cam)
 */

public class ImagePicker extends TimerTask {

    private FlyCamera cam;
    private int fps;
    private byte[] pixels;
    private int nrows, ncols;
    private int tile;

    ImagePicker(FlyCamera cam, int fps) {
        //Keep camera ref and fps
        this.cam = cam;
        this.tile = cam.PixTile();
        this.fps = fps;
        //Get number of pixels
        nrows = cam.Dimz() >> 16;
        ncols = cam.Dimz() << 16 >> 16;
        pixels = new byte[nrows * ncols * 4];
        Timer pickerTaskTimer = new Timer();
        pickerTaskTimer.scheduleAtFixedRate(this, new Date(), (long) (1000 / (float) this.fps));

    }

    public int getNrows() {
        return nrows;
    }

    public int getNcols() {
        return ncols;
    }

    public byte[] getPixels() {
        //cam.NextFrame(pixels);
        return pixels;
    }

    @Override
    public void run() {
        cam.NextFrame(pixels);
    }
}
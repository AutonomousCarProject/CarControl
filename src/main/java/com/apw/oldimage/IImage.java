package com.apw.oldimage;

public interface IImage {
    IPixel[][] getImage();

    default void setImage(IPixel[][] image) {
    }

    void readCam();

    void finish();

    default int getFrameNo() {
        return 0;
    }

    void setAutoFreq(int autoFreq);
}

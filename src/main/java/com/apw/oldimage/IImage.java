package com.apw.oldimage;

public interface IImage {
    IPixel[][] getImage();

    void readCam();

    void finish();

    default void setImage(IPixel[][] image) {
    }

    default int getFrameNo() {
        return 0;
    }

    void setAutoFreq(int autoFreq);
}

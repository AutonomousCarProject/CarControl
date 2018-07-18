package com.apw.blobdetect;

import com.apw.oldimage.IPixel;

public class TestPixel implements IPixel {
    private short color;

    public TestPixel(int color) {
        this.color = (short) color;

        if (color != 0 && color != 1) {
            throw new IllegalArgumentException("TestPixel does not work with color code " + color);
        }
    }

    @Override
    public short getLuma() {
        return 255;
    }

    @Override
    public short getSaturation() {
        return 255;
    }

    @Override
    public short getColor() {
        return color;
    }

    public void setColor(int c) {
    }

    @Override
    public short getRed() {
        return (short) (color == 0 ? 255 : 0);
    }

    @Override
    public short getGreen() {
        return (short) (color == 1 ? 255 : 0);
    }

    @Override
    public short getBlue() {
        return 0;
    }

    @Override
    public void setRGB(short r, short b, short g) {
    }

}
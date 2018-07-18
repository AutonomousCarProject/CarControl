package com.apw.oldimage;

//Defines basic implementation for pixel
public class Pixel implements IPixel { // 0-765
    public static int greyMargin = 60;
    public static int blackMargin = 300;
    public static int whiteMargin = 700; // 0-765
    // white
    // YUV
    private short luma;
    private short saturation;
    private short color; // 0: red, 1: green, 2: blue, 3: grey, 4: black, 5:
    // RGB Values 0-255
    private short red;
    private short green;
    private short blue;

    public Pixel(int color) {
        this.setColor(color);
    }

    public Pixel(short red, short green, short blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        simpleConvert();
    }

    public void setRGB(short red, short green, short blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        simpleConvert();
    }

    @Override
    public short getLuma() {
        return luma;
    }

    @Override
    public short getSaturation() {
        return saturation;
    }

    @Override
    public short getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = (short) color;
        switch (this.color) {
            case 0:
                this.red = 255;
                this.green = 0;
                this.blue = 0;
                break;
            case 1:
                this.red = 0;
                this.green = 255;
                this.blue = 0;
                break;
            case 2:
                this.red = 0;
                this.green = 0;
                this.blue = 255;
                break;
            case 3:
                this.red = 128;
                this.green = 128;
                this.blue = 128;
                break;
            case 4:
                this.red = 0;
                this.green = 0;
                this.blue = 0;
                break;
            case 5:
                this.red = 255;
                this.green = 255;
                this.blue = 255;
                break;
        }
    }

    @Override
    public short getRed() {
        return red;
    }

    @Override
    public short getGreen() {
        return green;
    }

    @Override
    public short getBlue() {
        return blue;
    }

    public void simpleConvert() {
        short ave = (short) (red + green + blue);
        int r = red * 3;
        int g = green * 3;
        int b = blue * 3;

        int rdiff = r - ave;
        if (rdiff < 0) {
            rdiff = -rdiff;
        }

        int gdiff = g - ave;
        if (gdiff < 0) {
            gdiff = -gdiff;
        }

        int bdiff = b - ave;
        if (bdiff < 0) {
            bdiff = -bdiff;
        }

        if (rdiff < greyMargin && gdiff < greyMargin && bdiff < greyMargin) { // if its not a distinct color
            if (r < blackMargin && g < blackMargin && b < blackMargin)
                color = 4; // black
            else if (r > whiteMargin && g > whiteMargin && b > whiteMargin)
                color = 5; // white
            else
                color = 3;
        } else if (r > g && r > b)
            color = 0;
        else if (g > r && g > b)
            color = 1;
        else if (b > r && b > g) color = 2;
    }


    /*
     * public void convert(){ //luminance = sum of rgb luma = (short)(red +
     * green + blue);
     *
     * //saturation = max - min of rgb saturation =
     * (short)((Math.max(Math.max(red, green), blue) - Math.min(Math.min(red,
     * green), blue))*3);
     *
     * //hue short ave = (short)(red + green + blue); int r = red*3; int g =
     * green*3; int b = blue*3; int rdiff = r - ave; int gdiff = g - ave; int
     * bdiff = b - ave; if(rdiff < greyMargin && gdiff < greyMargin && bdiff <
     * greyMargin){ //if its not a distinct color if(r < blackMargin && g <
     * blackMargin && b < blackMargin) color = 4; //black else if(r >
     * whiteMargin && g > whiteMargin && b > whiteMargin) color = 5; //white
     * else color = 3; } else if(rdiff > gdiff && rdiff > bdiff) color = 0; else
     * if(gdiff > rdiff && gdiff > bdiff) color = 1; else if(bdiff > rdiff &&
     * bdiff > gdiff) color = 2; }
     */

    public String toString() {
        return this.red + " " + this.green + " " + this.blue;
    }
}
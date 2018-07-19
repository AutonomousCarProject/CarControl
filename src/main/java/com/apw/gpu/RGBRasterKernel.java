package com.apw.gpu;

import com.aparapi.Kernel;

public class RGBRasterKernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer;
    private int[] rgb;

    public RGBRasterKernel(int nrows, int ncols, byte[] bayer, int[] rgb) {
        this.nrows = nrows;
        this.ncols = ncols;
        this.bayer = bayer;
        this.rgb = rgb;
    }

    public int[] getRgb() {
        return rgb;
    }

    @Override
    public void run() {

        // these might not be accurate
        int rows = getGlobalId(1);
        int cols = getGlobalId(0);

        int R = ((((int) bayer[(rows * ncols * 2 + cols) * 2                ]) & 0xFF));                //Top left (red)
        int G = ((((int) bayer[(rows * ncols * 2 + cols) * 2 + 1            ]) & 0xFF));                //Top right (green)
        int B = (( (int) bayer[(rows * ncols * 2 + cols) * 2 + 1 + 2 * ncols]) & 0xFF);                 //Bottom right (blue)
        int pix = (R << 16) + (G << 8) + B;

        rgb[rows * ncols + cols] = pix;
    }

}

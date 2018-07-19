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

        int pix = bayer[(rows * ncols + cols      ) * 2    ] << 16                //Top left (red)
                + bayer[(rows * ncols + cols      ) * 2 + 1] << 8                 //Top right (green)
                + bayer[((rows + 1) * ncols + cols) * 2 + 1];                     //Bottom right (blue)

        rgb[rows * ncols + cols] = pix;
    }

}

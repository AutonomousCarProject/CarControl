package com.apw.gpu;

import com.aparapi.Kernel;

public class RGBRasterKernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer, rgb;

    public RGBRasterKernel(int nrows, int ncols, byte[] bayer, byte[] rgb)
    {
        this.nrows  = nrows;
        this.ncols  = ncols;
        this.bayer  = bayer;
        this.rgb    = rgb;
    }

    @Override
    public void run() {

    }

}

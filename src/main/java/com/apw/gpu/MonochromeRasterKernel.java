package com.apw.gpu;

import com.aparapi.Kernel;

public class MonochromeRasterKernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer, mono;

    public MonochromeRasterKernel(int nrows, int ncols, byte[] bayer, byte[] mono)
    {
        this.nrows = nrows;
        this.ncols = ncols;
        this.bayer = bayer;
        this.mono = mono;
    }

    @Override
    public void run() {

        int r = getGlobalId()/nrows;
        int c = getGlobalId()/ncols;

        mono[r*ncols + c] = bayer[r*ncols*2 + c*2 + 1];
    }
}
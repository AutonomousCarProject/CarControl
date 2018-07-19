package com.apw.gpu;

import com.aparapi.Kernel;

public class MonochromeRasterKernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer, mono;

    public MonochromeRasterKernel(int nrows, int ncols, byte[] bayer, byte[] mono) {
        this.nrows = nrows;
        this.ncols = ncols;
        this.bayer = bayer;
        this.mono = mono;
    }

    public byte[] getMono() {
        return mono;
    }

    @Override
    public void run() {

        // these might not be accurate
        int rows = getGlobalId(1);
        int cols = getGlobalId(0);

        mono[rows * ncols + cols] = bayer[rows * ncols * 2 + cols * 2 + 1];
    }
}
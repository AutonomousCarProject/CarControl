package com.apw.gpu;

import com.aparapi.Kernel;

public class MonochromeRasterKernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer, mono;

    @Override
    public void run() {

        int r = getGlobalId()/nrows;
        int c = getGlobalId()/ncols;

        mono[r*ncols + c] = bayer[r*ncols*2 + c*2 + 1];
    }
}
package com.apw.gpu;

import com.aparapi.Kernel;

public class MonochromeRasterKernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer, mono;

    @Override
    public void run() {

        var r = getGlobalId()/nrows;
        var c = getGlobalId()/ncols;

        mono[r*ncols + c] = bayer[r*ncols*2 + c*2 + 1];
    }
}
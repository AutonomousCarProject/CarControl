package com.apw.gpu;

import com.aparapi.Kernel;

public class SimpleColorRasterKernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer, simple;

    public SimpleColorRasterKernel(int nrows, int ncols, byte[] bayer, byte[] simple)
    {
        this.nrows  = nrows;
        this.ncols  = ncols;
        this.bayer  = bayer;
        this.simple = simple;
    }

    @Override
    public void run() {

    }
}

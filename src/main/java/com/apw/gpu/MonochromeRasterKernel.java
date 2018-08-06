package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>MonochromeRasterKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that converts a bayer rgb byte array into a monochromatic bayer byte array.
 */
public class MonochromeRasterKernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer, mono;

    private byte tile;

    /**
     * Constructs an <code>MonochromeRasterKernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
     *
     * @param bayer Array of bayer arranged rgb colors
     * @param mono  Monochrome copy of the bayer array
     * @param nrows Number of rows to filter
     * @param ncols Number of columns to filter
     */
    public MonochromeRasterKernel(byte[] bayer, byte[] mono, int nrows, int ncols, byte tile) {
        this.bayer = bayer;
        this.mono = mono;
        this.nrows = nrows;
        this.ncols = ncols;
        this.tile = tile;
    }

    public MonochromeRasterKernel() {}

    /**
     * Sets all member variables of <code>MonochromeRasterKernel</code>.
     *
     * @param bayer Array of bayer arranged rgb colors
     * @param mono  Monochrome copy of the bayer array
     * @param nrows Number of rows to filter
     * @param ncols Number of columns to filter
     */
    public void setValues(byte[] bayer, byte[] mono, int nrows, int ncols, byte tile) {
        this.bayer = bayer;
        this.mono = mono;
        this.nrows = nrows;
        this.ncols = ncols;
        this.tile = tile;
    }

    /**
     * Returns a monochrome bayer byte array,
     * Should be called to retrieve result after kernel is executed.
     *
     * @return Monochrome bayer byte array
     */
    public byte[] getMono() {
        return mono;
    }

    @Override
    public void run() {

        int row = getGlobalId(0);
        int col = getGlobalId(1);

        mono[row * ncols + col] = (byte) ((((int) bayer[(row * ncols * 2 + col) * 2 + 1]) & 0xFF));
    }
}
package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>MonochromeRasterKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that converts a bayer rgb byte array into a monochromatic bayer byte array.
 */
public class Monochrome2RasterKernel extends Kernel {

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
    public Monochrome2RasterKernel(byte[] bayer, byte[] mono, int nrows, int ncols, byte tile) {
        this.bayer = bayer;
        this.mono = mono;
        this.nrows = nrows;
        this.ncols = ncols;
        this.tile = tile;
    }

    public Monochrome2RasterKernel() {
    }

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

        int R = (bayer[getPos(col, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
        int G = (bayer[getPos(col, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
        int B = (bayer[getPos(col, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);
        //float Y = R *  .299000 + G *  .587000 + B *  .114000;

        float Y = (R + G + B) / 3.0f;
        mono[row * ncols + col] = (byte) Y;
    }

    private int getBit(byte tile, int pos) {
        return (tile >> pos) & 1;
    }

    private int boolBit(boolean check) {
        if (check) return 1;
        return 0;
    }

    private int getPos(int x, int y, byte tile, int ncols, int nrows) {
        return (y * ncols * (4 - getBit(tile, 2)) + (2 + getBit(tile, 2)) * x + getBit(tile, 1) * (2 * ncols - (2 * ncols - 1) * getBit(tile, 2)) + getBit(tile, 0)) % ((4 - getBit(tile, 2)) * ncols * nrows);
    }

    private byte combineTile(byte tile1, byte tile2) {
        return (byte) (((int) tile1) ^ ((int) tile2));
    }
}
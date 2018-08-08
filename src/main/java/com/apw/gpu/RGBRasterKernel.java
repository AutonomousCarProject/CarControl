package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>RGBRasterKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that converts a bayer rgb byte array into an rgb raster of the same array.
 */
public class RGBRasterKernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer;

    private int[] rgb;

    private byte tile;

    /**
     * Constructs an <code>RGBRasterKernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
     *
     * @param bayer Array of bayer arranged rgb colors
     * @param nrows Number of rows to filter
     * @param ncols Number of columns to filter
     * @param rgb   rgb raster of the bayer array
     */
    public RGBRasterKernel(byte[] bayer, int[] rgb, int nrows, int ncols, byte tile) {
        this.bayer = bayer;
        this.rgb = rgb;
        this.nrows = nrows;
        this.ncols = ncols;
        this.tile = tile;
    }

    public RGBRasterKernel() {
    }

    /**
     * Sets all member variables of <code>RGBRasterKernel</code>.
     *
     * @param bayer Array of bayer arranged rgb colors
     * @param nrows Number of rows to filter
     * @param ncols Number of columns to filter
     * @param rgb   rgb raster of the bayer array
     */
    public void setValues(byte[] bayer, int[] rgb, int nrows, int ncols, byte tile) {
        this.bayer = bayer;
        this.rgb = rgb;
        this.nrows = nrows;
        this.ncols = ncols;
        this.tile = tile;
    }

    /**
     * Returns an rgb raster of a bayer byte array,
     * Should be called to retrieve result after kernel is executed.
     *
     * @return Bayer rgb raster int array
     */
    public int[] getRgb() {
        return rgb;
    }

    @Override
    public void run() {

        int row = getGlobalId(0);
        int col = getGlobalId(1);

        int R = (bayer[getPos(col, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
        int G = (bayer[getPos(col, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
        int B = (bayer[getPos(col, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);

        int pix = (R << 16) + (G << 8) + B;

        rgb[row * ncols + col] = pix;
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

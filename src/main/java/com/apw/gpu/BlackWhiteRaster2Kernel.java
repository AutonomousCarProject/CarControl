package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>BlackWhiteRaster2Kernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that converts a bayer rgb byte array into a black and white bayer byte array.
 */
public class BlackWhiteRaster2Kernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer, mono;

    private byte tile;

    private int averageLuminance;

    /**
     * Constructs an <code>BlackWhiteRaster2Kernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
     *
     * @param bayer Array of bayer arranged rgb colors
     * @param mono  Monochrome copy of the bayer array
     * @param nrows Number of rows to filter
     * @param ncols Number of columns to filter
     */
    public BlackWhiteRaster2Kernel(byte[] bayer, byte[] mono, int nrows, int ncols, byte tile) {
        this.bayer = bayer;
        this.mono = mono;
        this.nrows = nrows;
        this.ncols = ncols;
        this.tile = tile;
    }

    public BlackWhiteRaster2Kernel() {}

    /**
     * Sets all member variables of <code>BlackWhiteRaster2Kernel</code>.
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
        System.out.println("BlackWhite2RasterKernel.run");
        int row = getGlobalId(0);
        int col = getGlobalId(1);

        if (col == 0)
            averageLuminance = 0;
        int R = (bayer[getPos(col, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
        int G = (bayer[getPos(col, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
        int B = (bayer[getPos(col, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);

        if (col == 0)
            averageLuminance = (R + G + B) / 3;

        if (!(col >= 640 || row < 240 || row > 455)) {
            if ((averageLuminance + (R + G + B) / 3) / 2 > averageLuminance * 1.5) {
                mono[row * ncols + col] = 1;
            } else {
                mono[row * ncols + col] = 0;
            }
        } else {
            mono[row * ncols + col] = 0;
        }
        averageLuminance = (averageLuminance + (R + G + B) / 3) / 2;
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
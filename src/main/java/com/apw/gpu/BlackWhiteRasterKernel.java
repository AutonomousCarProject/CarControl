package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>BlackWhiteRasterKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that converts a bayer rgb byte array into a black and white bayer byte array.
 */
public class BlackWhiteRasterKernel extends Kernel {

    private final int luminanceMultiplier = 1;
    private int nrows, ncols;
    private byte[] bayer, mono;
    private byte tile;
    @Local
    private int averageLuminance;

    /**
     * Constructs an <code>BlackWhiteRasterKernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
     *
     * @param bayer Array of bayer arranged rgb colors
     * @param mono  Monochrome copy of the bayer array
     * @param nrows Number of rows to filter
     * @param ncols Number of columns to filter
     */
    public BlackWhiteRasterKernel(byte[] bayer, byte[] mono, int nrows, int ncols, byte tile) {
        this.bayer = bayer;
        this.mono = mono;
        this.nrows = nrows;
        this.ncols = ncols;
        this.tile = tile;
    }

    public BlackWhiteRasterKernel() {
    }

    /**
     * Sets all member variables of <code>BlackWhiteRasterKernel</code>.
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

        if (col == 0)
            averageLuminance = 0;
        int R = (bayer[getPos(col, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
        int G = (bayer[getPos(col, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
        int B = (bayer[getPos(col, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);

        averageLuminance += (R + G + B) / 3;

        if (col == 0)
            averageLuminance /= ncols;

        if (col < ncols) {
            int R1 = (bayer[getPos(col, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
            int G1 = (bayer[getPos(col, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
            int B1 = (bayer[getPos(col, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);
            int R2 = (bayer[getPos(col + 1, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
            int G2 = (bayer[getPos(col + 1, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
            int B2 = (bayer[getPos(col + 1, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);
            int R3 = (bayer[getPos(col + 2, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
            int G3 = (bayer[getPos(col + 2, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
            int B3 = (bayer[getPos(col + 2, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);

            int pix = (R1 + R2 + R3 + B1 + B2 + B3 + G1 + G2 + G3) / 9;
            // int pix = (R2 + G2 + B2)/3;
            if (!(col + 1 >= 640 || row < 240 || row > 455)) {
                if (pix > luminanceMultiplier * averageLuminance) {
                    mono[row * ncols + col + 1] = 1;
                } else {
                    mono[row * ncols + col + 1] = 0;
                }
            } else {
                mono[row * ncols + col + 1] = 0;
            }
        }
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
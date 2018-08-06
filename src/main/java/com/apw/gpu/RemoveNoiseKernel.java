package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>BlackWhiteRaster2Kernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that converts a bayer rgb byte array into a black and white bayer byte array.
 */
public class RemoveNoiseKernel extends Kernel {

    private int nrows, ncols;

    private byte[] pixels, output;
    /**
     * Constructs an <code>BlackWhiteRaster2Kernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
     *
     * @param pixels image to be eroded
     * @param nrows	number of rows of pixels in the image
     * @param ncols number of columns of pixels in the image
     */
    public RemoveNoiseKernel(byte[] pixels, int nrows, int ncols) {
        this.pixels = pixels;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    public RemoveNoiseKernel() {
    }

    /**
     * Sets all member variables of <code>BlackWhiteRaster2Kernel</code>.
     *
     * @param pixels image to be eroded
     * @param nrows	number of rows of pixels in the image
     * @param ncols number of columns of pixels in the image
     */
    public void setValues(byte[] pixels, int nrows, int ncols) {
        this.pixels = pixels;
        this.nrows = nrows;
        this.ncols = ncols;
        output = new byte[nrows * ncols];
    }

    /**
     * Returns a monochrome bayer byte array,
     * Should be called to retrieve result after kernel is executed.
     *
     * @return Monochrome bayer byte array
     */
    public byte[] getEroded() {
        return output;
    }

    @Override
    public void run() {
        int row = getGlobalId(0);
        int col = getGlobalId(1);
        if(pixels[row * ncols + col] == 1) {
            int whiteNeighbors = 0;
            //top left
            if((row - 1) > 0 && (col - 1) > 0 && pixels[(row - 1) * ncols + (col - 1)] == 1) {
                whiteNeighbors++;
            }
            //top
            if((row - 1) > 0 && pixels[(row - 1) * ncols + (col)] == 1) {
                whiteNeighbors++;
            }
            //top right
            if((row - 1) > 0 && (col + 1) < ncols && pixels[(row - 1) * ncols + (col + 1)] == 1) {
                whiteNeighbors++;
            }
            //left
            if((col - 1) > 0 && pixels[(row) * ncols + (col - 1)] == 1) {
                whiteNeighbors++;
            }
            //right
            if((col + 1) < ncols && pixels[(row) * ncols + (col + 1)] == 1) {
                whiteNeighbors++;
            }
            //bot left
            if((row + 1) < nrows && (col - 1) > 0 && pixels[(row + 1) * ncols + (col - 1)] == 1) {
                whiteNeighbors++;
            }
            //bot
            if((row + 1) < nrows && pixels[(row + 1) * ncols + (col)] == 1) {
                whiteNeighbors++;
            }
            //bot right
            if((row + 1) < nrows && (col + 1) < ncols && pixels[(row + 1) * ncols + (col + 1)] == 1) {
                whiteNeighbors++;
            }
            if(whiteNeighbors > 7) {
                output[row * ncols + col] = 1;
            }
            else {
                output[row * ncols + col] = 0;
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
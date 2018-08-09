package com.apw.gpu;

import com.aparapi.Kernel;
import com.apw.imagemanagement.SimpleThresholds;

/**
 * The <code>MonochromeRasterKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that creates a simple color raster from a bayer rgb byte array.
 * <p>
 * The raster is comprised of <code>RED</code>, <code>GREEN</code>, <code>BLUE</code>, <code>WHITE</code>, and <code>GREY</code>
 * color codes ranging from 0 to 5.
 * </p>
 */
public class SimpleColorRasterKernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer, simple;

    private byte tile;

    public SimpleColorRasterKernel() {

    }

    /**
     * Constructs an <code>SimpleColorRasterKernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
     *
     * @param bayer  Array of bayer arranged rgb colors
     * @param simple Simple color raster of the bayer array
     * @param nrows  Number of rows to filter
     * @param ncols  Number of columns to filter
     */
    public SimpleColorRasterKernel(byte[] bayer, byte[] simple, int nrows, int ncols, byte tile) {
        this.bayer = bayer;
        this.simple = simple;
        this.nrows = nrows;
        this.ncols = ncols;
        this.tile = tile;
    }

    /**
     * Sets all member variables of <code>SimpleColorRasterKernel</code>.
     *
     * @param bayer  Array of bayer arranged rgb colors
     * @param simple Simple color raster of the bayer array
     * @param nrows  Number of rows to filter
     * @param ncols  Number of columns to filter
     */
    public void setValues(byte[] bayer, byte[] simple, int nrows, int ncols, byte tile) {
        this.bayer = bayer;
        this.simple = simple;
        this.nrows = nrows;
        this.ncols = ncols;
        this.tile = tile;
    }

    /**
     * Returns an simple color raster of a bayer byte array,
     * Should be called to retrieve result after kernel is executed.
     *
     * @return Simple color raster byte array
     */
    public byte[] getSimple() {
        return simple;
    }

    @Override
    public void run() {
        /*
         *Built for RG/GB Bayer Configuration
         *Serves color raster encoded in 1D of values 0-5 with
         * 0 = RED
         * 1 = GREEN
         * 2 = BLUE
         * 3 = WHITE
         * 4 = GREY
         * 5 = BLACK
         */

        int row = getGlobalId(0);
        int col = getGlobalId(1);

        int R = (bayer[getPos(col, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
        int G = (bayer[getPos(col, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
        int B = (bayer[getPos(col, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);
        //int B = (((int)bayer[(r*ncols*2 + c)*2 + 1+2*ncols-ncols*2*getBit(tile,1)-getBit(tile,0)])&0xFF);			//Bottom right (blue)
        float Y = R * .299000f + G * .587000f + B * .114000f;
        float U = R * -.168736f + G * -.331264f + B * .500000f + 128;
        float V = R * .500000f + G * -.418688f + B * -.081312f + 128;
        R = (int) (1.4075f * (V - 128));
        G = (int) (0 - 0.3455f * (U - 128) - (0.7169f * (V - 128)));
        B = (int) (1.7790f * (U - 128));
        //If one of the colors has a value 50 greater than both other colors
        //it assigns that pixel to that color
        if (R > G + SimpleThresholds.redGreen && R > B + SimpleThresholds.redBlue) {
            simple[row * ncols + col] = 0;
        } else if (G > R + SimpleThresholds.greenRed && G > B + SimpleThresholds.greenBlue) {
            simple[row * ncols + col] = 1;
        } else if (B > R + SimpleThresholds.blueRed && B > G + SimpleThresholds.blueGreen) {
            simple[row * ncols + col] = 2;
        } else if (R < G + SimpleThresholds.yellowDiff && G < R + SimpleThresholds.yellowDiff && (R > B + SimpleThresholds.yellowBlue)) {
            simple[row * ncols + col] = 6;
        }
        //Otherwise it sees if one of the colors has a value above 170 for white
        // if not, 85 for grey and below 85 for black
        else if (Y > SimpleThresholds.whitePoint) {
            simple[row * ncols + col] = 3;
        } else if (Y > SimpleThresholds.greyPoint) {
            simple[row * ncols + col] = 4; //0x808080
        } else {
            simple[row * ncols + col] = 5;
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

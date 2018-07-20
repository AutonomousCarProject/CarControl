package com.apw.gpu;

import com.aparapi.Kernel;

import java.util.Arrays;

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

    /**
     * Constructs an <code>SimpleColorRasterKernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
     *
     * @param bayer  Array of bayer arranged rgb colors
     * @param simple Simple color raster of the bayer array
     * @param nrows  Number of rows to filter
     * @param ncols  Number of columns to filter
     */
    public SimpleColorRasterKernel(byte[] bayer, byte[] simple, int nrows, int ncols) {
        this.bayer = bayer;
        this.simple = simple;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    /**
     * Sets all member variables of <code>SimpleColorRasterKernel</code>.
     *
     * @param bayer  Array of bayer arranged rgb colors
     * @param simple Simple color raster of the bayer array
     * @param nrows  Number of rows to filter
     * @param ncols  Number of columns to filter
     */
    public void setValues(byte[] bayer, byte[] simple, int nrows, int ncols) {
        this.bayer = bayer;
        this.simple = simple;
        this.nrows = nrows;
        this.ncols = ncols;
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

        int rows = getGlobalId(0);
        int cols = getGlobalId(1);

        int R = ((((int) bayer[(rows * ncols * 2 + cols) * 2                ]) & 0xFF)); // Top left (red)
        int G = ((((int) bayer[(rows * ncols * 2 + cols) * 2 + 1            ]) & 0xFF)); // Top right (green)
        int B = (((int ) bayer[(rows * ncols * 2 + cols) * 2 + 1 + 2 * ncols]) & 0xFF);  // Bottom right (blue)

        // If one of the colors has a value 50 greater than both other colors
        // it assigns that pixel to that color
             if (R > G + 51 && R > B + 51) simple[rows * ncols + cols] = 0;
        else if (G > R + 50 && G > B + 50) simple[rows * ncols + cols] = 1;
        else if (B > R + 50 && B > G + 50) simple[rows * ncols + cols] = 2;
        // Otherwise it sees if one of the colors has a value above 170 for white
        // if not, 85 for grey and below 85 for black
        else if (R > 170 || G > 170 || B > 170) simple[rows * ncols + cols] = 3;
        else if (R > 85  || G > 85  || B > 85 ) simple[rows * ncols + cols] = 4; //0x808080
        else if (R < 85  || G < 85  || B < 85 ) simple[rows * ncols + cols] = 5;
    }
}

package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>MonochromeRasterKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that creates a simple color raster from a bayer rgb byte array.
 * <p>
 *     The raster is comprised of <code>RED</code>, <code>GREEN</code>, <code>BLUE</code>, <code>WHITE</code>, and <code>GREY</code>
 *     color codes ranging from 0 to 5.
 * </p>
 */
public class SimpleColorRasterKernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer, simple;

    /**
     * Constructs an <code>SimpleColorRasterKernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
     * @param nrows Number of rows to filter
     * @param ncols Number of columns to filter
     * @param bayer Array of bayer arranged rgb colors
     * @param simple Simple color raster of the bayer array
     */
    public SimpleColorRasterKernel(int nrows, int ncols, byte[] bayer, byte[] simple) {
        this.nrows = nrows;
        this.ncols = ncols;
        this.bayer = bayer;
        this.simple = simple;
    }

    /**
     * Returns an simple color raster of a bayer byte array,
     * Should be called to retrieve result after kernel is executed.
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

        // these might not be accurate
        int rows = getGlobalId(1);
        int cols = getGlobalId(0);

        int r = bayer[2 * rows * nrows + cols * 2      ];
        int g = bayer[2 * rows * nrows + cols * 2 + 1  ];
        int b = bayer[(2 * rows * nrows) + 1 + cols + 1];

        // if one of the colors has a value 50 greater than both other colors
        // it assigns that pixel to that color
        if (r > g + 50 && r > b + 50) {
            simple[rows * nrows + cols] = 0;
        } else if (g > r + 50 && g > b + 50) {
            simple[rows * nrows + cols] = 1;
        } else if (b > r + 50 && b > g + 50) {
            simple[rows * nrows + cols] = 2;
        }

        // Otherwise it sees if one of the colors has a value above 170 for white
        // if not, 85 for grey and below 85 for black
        else if (r > 170 || g > 170 || b > 170) {
            simple[rows * nrows + cols] = 3;
        } else if (r > 85 || g > 85 || b > 85) {
            simple[rows * nrows + cols] = 4;
        } else if (r < 85 || g < 85 || b < 85) {
            simple[rows * nrows + cols] = 5;
        }
    }
}

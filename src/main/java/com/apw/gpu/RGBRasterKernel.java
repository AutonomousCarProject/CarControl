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

    /**
     * Constructs an <code>RGBRasterKernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
     *
     * @param bayer Array of bayer arranged rgb colors
     * @param nrows Number of rows to filter
     * @param ncols Number of columns to filter
     * @param rgb   rgb raster of the bayer array
     */
    public RGBRasterKernel(byte[] bayer, int[] rgb, int nrows, int ncols) {
        this.bayer = bayer;
        this.rgb = rgb;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    /**
     * Sets all member variables of <code>RGBRasterKernel</code>.
     *
     * @param bayer Array of bayer arranged rgb colors
     * @param nrows Number of rows to filter
     * @param ncols Number of columns to filter
     * @param rgb   rgb raster of the bayer array
     */
    public void setValues(byte[] bayer, int[] rgb, int nrows, int ncols) {
        this.bayer = bayer;
        this.rgb = rgb;
        this.nrows = nrows;
        this.ncols = ncols;
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

        int rows = getGlobalId(0);
        int cols = getGlobalId(1);

        int R = ((((int) bayer[(rows * ncols * 2 + cols) * 2                ]) & 0xFF));  //Top left (red)
        int G = ((((int) bayer[(rows * ncols * 2 + cols) * 2 + 1            ]) & 0xFF));  //Top right (green)
        int B = (((int ) bayer[(rows * ncols * 2 + cols) * 2 + 1 + 2 * ncols]) & 0xFF );  //Bottom right (blue)

        int pix = (R << 16) + (G << 8) + B;

        rgb[rows * ncols + cols] = pix;
    }
}

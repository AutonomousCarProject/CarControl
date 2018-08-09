package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>RobertsCrossRasterKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that converts a bayer rgb byte array into a monochromatic bayer byte array.
 */
public class RobertsCrossRasterKernel extends Kernel {

    private int nrows, ncols;

    private int[] input, output;


    public RobertsCrossRasterKernel(int[] input, int[] output, int nrows, int ncols) {
        this.input = input;
        this.output = output;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    public RobertsCrossRasterKernel() {
    }

    public void setValues(int[] input, int[] output, int nrows, int ncols) {
        this.input = input;
        this.output = output;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    /**
     * Returns a monochrome bayer byte array,
     * Should be called to retrieve result after kernel is executed.
     *
     * @return Monochrome bayer byte array
     */
    public int[] getOutput() {
        return output;
    }

    @Override
    public void run() {

        int row = getGlobalId(0);
        int col = getGlobalId(1);

        if (row < nrows - 1 && col < ncols - 1) {
            output[row * ncols + col] = Math.abs(input[row * ncols + col] - input[(row + 1) * ncols + (col + 1)])
                    + Math.abs(input[row * ncols + (col + 1)] - input[(row + 1) * ncols + col]);
        }
    }
}
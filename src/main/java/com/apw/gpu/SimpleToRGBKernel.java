package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>SimpleToRGBKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that creates a simple 5 color raw rgb pixel array from a simple 5 color raster of bayer rgb array
 * The raster is comprised of <code>RED</code>, <code>GREEN</code>, <code>BLUE</code>, <code>WHITE</code>, and <code>GREY</code>
 * color codes ranging from 0 to 5.
 * </p>
 */
public class SimpleToRGBKernel extends Kernel {

    private byte[] simpleByte;

    private int[] simpleRGB;

    private int length;

    /**
     * Constructs an <code>SimpleToRGBKernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
     *
     * @param simpleByte Simple 5 color raster of bayer arranged rgb colors
     * @param simpleRGB  Simple 5 color raw rgb pixel array
     * @param length     Length of <code>simpleByte</code> and <code>simpleRGB</code> arrays
     */
    public SimpleToRGBKernel(byte[] simpleByte, int[] simpleRGB, int length) {
        this.simpleByte = simpleByte;
        this.simpleRGB = simpleRGB;
        this.length = length;
    }

    /**
     * Sets all member variables of <code>SimpleToRGBKernel</code>.
     *
     * @param simpleByte Simple 5 color raster of bayer arranged rgb colors
     * @param simpleRGB  Simple 5 color raw rgb pixel array
     * @param length     Length of <code>simpleByte</code> and <code>simpleRGB</code> arrays
     */
    public void setValues(byte[] simpleByte, int[] simpleRGB, int length) {
        this.simpleByte = simpleByte;
        this.simpleRGB = simpleRGB;
        this.length = length;
    }

    /**
     * Returns a simple 5 color raw rgb pixel array,
     * Should be called to retrieve result after kernel is executed.
     *
     * @return Simple 5 color raw rgb pixel array
     */
    public int[] getSimpleRGB() {
        return simpleRGB;
    }

    @Override
    public void run() {
        int i = getGlobalId();

        if (simpleByte[i] == 0) simpleRGB[i] = 0xFF0000;
        else if (simpleByte[i] == 1) simpleRGB[i] = 0x00FF00;
        else if (simpleByte[i] == 2) simpleRGB[i] = 0x0000FF;
        else if (simpleByte[i] == 3) simpleRGB[i] = 0xFFFFFF;
        else if (simpleByte[i] == 4) simpleRGB[i] = 0x808080;
        else if (simpleByte[i] == 5) simpleRGB[i] = 0x000000;
    }
}

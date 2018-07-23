package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>MonoToRGBKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that creates a raw rgb pixel array from a monochromatic bayer rgb byte array
 * </p>
 */
public class MonoToRGBKernel extends Kernel {

    private byte[] mono;

    private int[] rgb;

    private int length;

    /**
     * Constructs an <code>MonoToRGBKernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
     *
     * @param mono   Monochromatic bayer rgb byte array
     * @param rgb    Raw rgb pixel array
     * @param length Length of <code>mono</code> and <code>rgb</code> arrays
     */
    public MonoToRGBKernel(byte[] mono, int[] rgb, int length) {
        this.mono = mono;
        this.rgb = rgb;
        this.length = length;
    }

    /**
     * Sets all member variables of <code>MonoToRGBKernel</code>.
     *
     * @param mono   Monochromatic bayer rgb byte array
     * @param rgb    Raw rgb pixel array
     * @param length Length of <code>mono</code> and <code>rgb</code> arrays
     */
    public void setValues(byte[] mono, int[] rgb, int length) {
        this.mono = mono;
        this.rgb = rgb;
        this.length = length;
    }

    /**
     * Returns raw rgb pixel array,
     * Should be called to retrieve result after kernel is executed.
     *
     * @return raw rgb pixel array
     */
    public int[] getRGB() {
        return rgb;
    }

    @Override
    public void run() {
        int i = getGlobalId();

        rgb[i] = (mono[i] << 16) + (mono[i] << 8) + mono[i];
    }
}

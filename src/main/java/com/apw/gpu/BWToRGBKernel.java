package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>BWToRGBKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that creates a monochromatic raw rgb pixel array from a black and white bayer rgb array
 * </p>
 */
public class BWToRGBKernel extends Kernel {

  private byte[] simpleByte;

  private int[] mono;

  private int length;

  /**
   * Constructs an <code>BWToRGBKernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL}
   * kernel.
   *
   * @param simpleByte Simple 5 color raster of bayer arranged rgb colors
   * @param mono Monochromatic raw rgb pixel array
   * @param length Length of <code>simpleByte</code> and <code>mono</code> arrays
   */
  public BWToRGBKernel(byte[] simpleByte, int[] mono, int length) {
    this.simpleByte = simpleByte;
    this.mono = mono;
    this.length = length;
  }

  /**
   * Sets all member variables of <code>BWToRGBKernel</code>.
   *
   * @param simpleByte Simple 5 color raster of bayer arranged rgb colors
   * @param mono Monochromatic raw rgb pixel array
   * @param length Length of <code>simpleByte</code> and <code>simpleRGB</code> arrays
   */
  public void setValues(byte[] simpleByte, int[] mono, int length) {
    this.simpleByte = simpleByte;
    this.mono = mono;
    this.length = length;
  }

  /**
   * Returns a monochromatic raw rgb pixel array,
   * Should be called to retrieve result after kernel is executed.
   *
   * @return Simple color raster byte array
   */
  public int[] getMono() {
    return mono;
  }

  @Override
  public void run() {
    int i = getGlobalId();

    if (simpleByte[i] == 0) {
      mono[i] = 0xFF0000;
    } else if (simpleByte[i] == 1) {
      mono[i] = 0xFFFFFF;
    }
  }
}

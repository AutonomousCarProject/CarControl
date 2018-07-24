package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>MonochromeRasterKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that converts a bayer rgb byte array into a black and white bayer byte array.
 */
public class BlackWhiteRasterKernel extends Kernel {

  private int nrows, ncols;

  private byte[] bayer, mono;

  /**
   * Constructs an <code>BlackWhiteRasterKernel</code> Aparapi
   * {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
   *
   * @param bayer Array of bayer arranged rgb colors
   * @param mono Monochrome copy of the bayer array
   * @param nrows Number of rows to filter
   * @param ncols Number of columns to filter
   */
  public BlackWhiteRasterKernel(byte[] bayer, byte[] mono, int nrows, int ncols) {
    this.bayer = bayer;
    this.mono = mono;
    this.nrows = nrows;
    this.ncols = ncols;
  }

  /**
   * Sets all member variables of <code>BlackWhiteRasterKernel</code>.
   *
   * @param bayer Array of bayer arranged rgb colors
   * @param mono Monochrome copy of the bayer array
   * @param nrows Number of rows to filter
   * @param ncols Number of columns to filter
   */
  public void setValues(byte[] bayer, byte[] mono, int nrows, int ncols) {
    this.bayer = bayer;
    this.mono = mono;
    this.nrows = nrows;
    this.ncols = ncols;
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

    int rows = getGlobalId(0);
    int cols = getGlobalId(1);

    int R = ((((int) bayer[(rows * ncols * 2 + cols) * 2]) & 0xFF));            //Top left (red)
    int G = ((((int) bayer[(rows * ncols * 2 + cols) * 2 + 1])
        & 0xFF));            //Top right (green)
    int B = (((int) bayer[(rows * ncols * 2 + cols) * 2 + 1 + 2 * ncols])
        & 0xFF);            //Bottom right (blue)
    int pix = R + G + B;
    if (pix > 700) {
      mono[rows * ncols + cols] = 1;
    } else {
      mono[rows * ncols + cols] = 0;
    }
  }
}
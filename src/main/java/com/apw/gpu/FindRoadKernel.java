package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>RobertsCrossRasterKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that converts a bayer rgb byte array into a monochromatic bayer byte array.
 */
public class FindRoadKernel extends Kernel {

    private int nrows, ncols;

    private int[] output;
    private byte[] bw;

    public FindRoadKernel(byte[] bw, int[] output, int nrows, int ncols) {
        this.bw = bw;
        this.output = output;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    public FindRoadKernel() {
    }

    public void setValues(byte[] bw, int[] output, int nrows, int ncols) {
        this.bw = bw;
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

        int krow = getGlobalId(0);
        int kcol = getGlobalId(1);

        int rightEnd = 640, leftEnd = 0;
        if (kcol + ncols/2 < ncols) {
            if(bw[454*ncols + (kcol + ncols/2)] == 1){
                rightEnd = (kcol + ncols/2);
            }
        }

        for(int i = ncols/2; i > 0; i--){
            if(bw[454*ncols + i] == 1){
                leftEnd = i;
            }
        }
        for(int col = 0; col < ncols; col++){
            boolean endFound = false;

            for(int row = nrows-1; row > 0; row--){
                if(col > 638 || row < 240 || row > 455){
                    output[row*ncols+col] = 0;
                } else if(bw[row*ncols+col] == 1){
                    endFound = true;
                    output[row*ncols+col] = 0xFFFFFF;
                }else if(!endFound && col > leftEnd && col < rightEnd){
                    output[row*ncols + col] = 0xF63FFC;
                }else{
                    output[row*ncols + col] = 0x000000;
                }
            }
        }
    }
}
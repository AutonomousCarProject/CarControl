package com.apw.gpu;

import com.aparapi.Range;

import java.util.Arrays;

public class ImageProcessing {
    public static void main(String[] args) {

        byte[] bayer = { 1, 2, 3, 4 };
        byte[] simple = { 4, 3, 2, 1 };
        int nrows = 4;
        int ncols = 4;

        Range range = Range.create(nrows, ncols);

        SimpleColorRasterKernel kernel = new SimpleColorRasterKernel(nrows, ncols, bayer, simple);
        kernel.execute(range);

        System.out.println(Arrays.toString(kernel.getSimple()));


    }
}

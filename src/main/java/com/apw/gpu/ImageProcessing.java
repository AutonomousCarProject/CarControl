package com.apw.gpu;

import com.aparapi.Range;
import com.apw.ImageManagement.ImageManipulator;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

public class ImageProcessing {
    public static void main(String[] args) {

        byte[] bayer = {1, 2, 3, 4};
        byte[] simple = {4, 3, 2, 1};
        int nrows = 4;
        int ncols = 4;

        Range range = Range.create(nrows * ncols);

        SimpleColorRasterKernel kernel = new SimpleColorRasterKernel(bayer, simple, nrows, ncols);

        kernel.execute(range);
        kernel.dispose();

        System.out.printf("%.2fms%n", kernel.getAccumulatedExecutionTime());
        System.out.println(Arrays.toString(kernel.getSimple()));

        Instant start = Instant.now();

        ImageManipulator.convertToSimpleColorRaster(bayer, simple, nrows, ncols);

        System.out.println(Duration.between(start, Instant.now()).toMillis());

    }
}

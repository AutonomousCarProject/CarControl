package com.apw.gpu;

import com.aparapi.Range;

import com.apw.imagemanagement.ImageManipulator;
import java.time.Duration;
import java.time.Instant;

public class ImageProcessing {
    public static void main(String[] args) {

        byte[] bayer = {1, 2, 3, 4};
        byte[] simple = {4, 3, 2, 1};
        int nrows = 4;
        int ncols = 4;

        Range range = Range.create2D(nrows, ncols);

        SimpleColorRasterKernel kernel = new SimpleColorRasterKernel(bayer, simple, nrows, ncols);

        for (int i = 0; i < 50; ++i) {
            kernel.execute(range);
            kernel.dispose();
            System.out.printf("%.2fms%n", kernel.getAccumulatedExecutionTime());
        }

        System.out.printf("%.2fms%n", kernel.getAccumulatedExecutionTime());
        //System.out.println(Arrays.toString(kernel.getSimple()));

        Instant start = Instant.now();

        ImageManipulator.convertToSimpleColorRaster(bayer, simple, nrows, ncols,640, (byte) 1);

        System.out.println(Duration.between(start, Instant.now()).toMillis());

    }
}

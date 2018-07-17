package com.apw.gpu;

import com.aparapi.Kernel;
import com.aparapi.Range;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class ImageProcessing {
    public static void main(String[] args) {

        final var image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        final int[] imageRgb = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        var width = image.getWidth();
        var height = image.getHeight();

        var range = Range.create(width, height);

        var kernel = new ImageKernel(width, height, imageRgb);

        kernel.execute(range);

        System.out.printf("Execution time = %sms%n", (int) kernel.getAccumulatedExecutionTime());
    }

    public static class ImageKernel extends Kernel {

        /** RGB buffer used to store the cameras image. This buffer holds (width * height) RGB values. */
        private int[] rgb;

        /** cameras image width. */
        private int width;

        /** cameras image height. */
        private int height;

        /**
         * Initialize the Kernel.
         *
         * @param width camera image width
         * @param height camera image height
         * @param rgb camera image RGB buffer
         */
        ImageKernel(int width, int height, int[] rgb) {
            this.width  = width;
            this.height = height;
            this.rgb    = rgb;
        }

        public void resetImage(int width, int height, int[] rgb) {
            this.width  = width;
            this.height = height;
            this.rgb    = rgb;
        }

        @Override public void run() {

            // Determine which RGB value is going to be processed (0 - rgb.length)
            final int gid = getGlobalId();

            // TODO implement convertToSimpleColorRaster method

            rgb[gid] = rgb[gid];
        }

        public int[] getRgbs() {
            return rgb;
        }
    }
}

package com.apw.pedestrians.blobdetect;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.apw.pedestrians.image.Color;
import com.apw.pedestrians.image.Pixel;

import static com.apw.pedestrians.blobdetect.PrimitiveBlob.*;

public class PrimitiveBlobDetection {
    private static final int MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN = 75;

    private int[] blobs;
    private int[] blobList;

    public int[] getBlobs(Pixel[][] pixels) {
        if (pixels.length == 0) {
            return null;
        }

        final int width = pixels.length;
        final int height = pixels[0].length;

        int[] colors = new int[width * height];

        for (int i = 0; i < colors.length; i++) {
            colors[i] = pixels[i / width][i % width].getColor().ordinal();
        }

        int blobSize = width * height * BLOB_NUM_INT_FIELDS;
        if (blobs == null || blobs.length != blobSize) {
            blobs = new int[blobSize];
        } else {
            for (int i = 0; i < blobs.length; i += BLOB_NUM_INT_FIELDS) {
                blobs[i + BLOB_TYPE] = BLOB_TYPE_NULL;
            }
        }

        Range rowRange = Range.create(pixels.length);

        // check the pixel to the right
        Kernel rightCheckKernel = new Kernel() {
            @Override
            public void run() {
                int row = getGlobalId();
                for (int col = 0; col < pixels[0].length - 1; col++) {
                    int color1 = colors[(row * width) + col];
                    int color2 = colors[(row * width) + col + 1];

                    int blob1 = ((row * width) + col) * BLOB_NUM_INT_FIELDS;
                    int blob2 = ((row * width) + col + 1) * BLOB_NUM_INT_FIELDS;

                    if (color1 != color2) {
                        //either adds to the blob if there is an existing one or creates a new one if there isn't
                        if (blobs[blob1 + BLOB_TYPE] == BLOB_TYPE_NULL) {
                            blobs[blob1 + BLOB_TYPE] = BLOB_TYPE_VALUE;
                            blobs[blob1 + BLOB_TOP] = row;
                            blobs[blob1 + BLOB_LEFT] = col;
                            blobs[blob1 + BLOB_BOTTOM] = row;
                            blobs[blob1 + BLOB_RIGHT] = col + 1;
                            blobs[blob1 + BLOB_COLOR] = color1;
                            blobs[blob1 + BLOB_MATCHED] = BLOB_IS_UNMATCHED;
                        } else if (blobs[blob1 + BLOB_TYPE] == BLOB_TYPE_VALUE) {
                            blobs[blob1 + BLOB_RIGHT] = max(blobs[blob1 + BLOB_RIGHT], col + 1);
                        } else if (blobs[blob1 + BLOB_TYPE] == BLOB_TYPE_REFERENCE) {
                            // get pointer to actual blob value from top/left fields
                            int tlBlob = blob1;
                            while (blobs[tlBlob + BLOB_TYPE] == BLOB_TYPE_REFERENCE) {
                                tlBlob = ((blobs[tlBlob + BLOB_TOP] * width) + blobs[tlBlob + BLOB_LEFT]) * BLOB_NUM_INT_FIELDS;
                            }
                            blobs[tlBlob + BLOB_RIGHT] = max(blobs[tlBlob + BLOB_RIGHT], col + 1);
                        }
                        blobs[blob2 + BLOB_TYPE] = BLOB_TYPE_REFERENCE;
                        blobs[blob2 + BLOB_TOP] = blobs[blob1 + BLOB_TOP];
                        blobs[blob2 + BLOB_LEFT] = blobs[blob1 + BLOB_LEFT];
                    }
                }
            }
        };
        rightCheckKernel.execute(rowRange);
        rightCheckKernel.dispose();

        Kernel bottomCheckKernel = new Kernel() {
            @Override
            public void run() {
                int row = getGlobalId();
                for (int col = 0; col < pixels[0].length; col++) {
                    int color1 = colors[(row * width) + col];
                    int color2 = colors[(row * width) + col + 1];

                    int blob1 = ((row * width) + col) * BLOB_NUM_INT_FIELDS;
                    int blob2 = (((row + 1) * width) + col + 1) * BLOB_NUM_INT_FIELDS;

                    int tlBlob1 = blob1;
                    while (blobs[tlBlob1 + BLOB_TYPE] == BLOB_TYPE_REFERENCE) {
                        tlBlob1 = ((blobs[tlBlob1 + BLOB_TOP] * width) + blobs[tlBlob1 + BLOB_LEFT]) * BLOB_NUM_INT_FIELDS;
                    }

                    int tlBlob2 = blob2;
                    while (blobs[tlBlob2 + BLOB_TYPE] == BLOB_TYPE_REFERENCE) {
                        tlBlob2 = ((blobs[tlBlob2 + BLOB_TOP] * width) + blobs[tlBlob2 + BLOB_LEFT]) * BLOB_NUM_INT_FIELDS;
                    }

                    int blob1Width = blobs[tlBlob1 + BLOB_RIGHT] - blobs[tlBlob1 + BLOB_LEFT] + 1;
                    int blob2Width = blobs[tlBlob2 + BLOB_RIGHT] - blobs[tlBlob2 + BLOB_LEFT] + 1;

                    //merges pixels that are vertically nearby and of same color
                    if (color1 == color2) {
                        if (blobs[blob1 + BLOB_TYPE] != BLOB_TYPE_NULL
                                && blobs[blob2 + BLOB_TYPE] != BLOB_TYPE_NULL
                                && abs(blob2Width - blob1Width) <= MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN) {

                            blobs[tlBlob1 + BLOB_LEFT] = min(blobs[tlBlob1 + BLOB_LEFT], blobs[tlBlob2 + BLOB_LEFT]);
                            blobs[tlBlob1 + BLOB_RIGHT] = max(blobs[tlBlob1 + BLOB_RIGHT], blobs[tlBlob2 + BLOB_RIGHT]);
                            blobs[tlBlob1 + BLOB_TOP] = min(blobs[tlBlob1 + BLOB_TOP], blobs[tlBlob2 + BLOB_TOP]);
                            blobs[tlBlob1 + BLOB_BOTTOM] = max(blobs[tlBlob1 + BLOB_BOTTOM], blobs[tlBlob2 + BLOB_BOTTOM]);

                            blobs[tlBlob2 + BLOB_TYPE] = BLOB_TYPE_REFERENCE;
                            blobs[tlBlob2 + BLOB_TOP] = blobs[tlBlob1 + BLOB_TOP];
                            blobs[tlBlob2 + BLOB_LEFT] = blobs[tlBlob1 + BLOB_LEFT];
                        }
                        // Potentially re-add these later, but according to the original algorithm, these will not be called
                        // Also consider adding a case where they are both null
                        /*
                        else if(blobs[blob1 + BLOB_TYPE] != BLOB_TYPE_NULL
                                && blobs[blob2 + BLOB_TYPE] == BLOB_TYPE_NULL
                                && blob1Width < MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN) {

                            blobs[tlBlob1 + BLOB_BOTTOM] = blobs[tlBlob1 + BLOB_BOTTOM] + 1;

                            blobs[tlBlob2 + BLOB_TYPE] = BLOB_TYPE_REFERENCE;
                            blobs[tlBlob2 + BLOB_TOP] = blobs[tlBlob1 + BLOB_TOP];
                            blobs[tlBlob2 + BLOB_LEFT] = blobs[tlBlob1 + BLOB_LEFT];
                        }
                        else if(blobs[blob1 + BLOB_TYPE] == BLOB_TYPE_NULL
                                && blobs[blob2 + BLOB_TYPE] != BLOB_TYPE_NULL
                                && blob2Width < MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN) {

                            blobs[tlBlob2 + BLOB_TOP] = blobs[tlBlob2 + BLOB_TOP] + 1;

                            blobs[tlBlob1 + BLOB_TYPE] = BLOB_TYPE_REFERENCE;
                            blobs[tlBlob1 + BLOB_TOP] = blobs[tlBlob2 + BLOB_TOP];
                            blobs[tlBlob1 + BLOB_LEFT] = blobs[tlBlob2 + BLOB_LEFT];
                        }
                        */
                    }
                }
            }
        };
        bottomCheckKernel.execute(rowRange);
        bottomCheckKernel.dispose();

        // eliminates blobs that are too large or too small or grey
        // also brings the blobs as left as possible within the array
        // this makes it so that there is no need to allocate a new array and reduces amount of iteration needed later

        int eliminated = 0;

        for (int i = 0; i < blobs.length; i += BLOB_NUM_INT_FIELDS) {
            if (blobs[i + BLOB_TYPE] == BLOB_TYPE_VALUE) {
                int blobWidth = blobs[i + BLOB_RIGHT] - blobs[i + BLOB_LEFT] + 1;
                int blobHeight = blobs[i + BLOB_BOTTOM] - blobs[i + BLOB_TOP] + 1;

                if (blobWidth >= 4 && blobHeight >= 4 && blobWidth < (width / 2) && blobHeight < (height / 2) && blobs[i + BLOB_COLOR] != Color.GREY.ordinal()) {
                    // do not eliminate
                    if(eliminated != 0) {
                        // needs to be shifted left
                        int newIndex = i - (eliminated * BLOB_NUM_INT_FIELDS);
                        System.arraycopy(blobs, i, blobs, newIndex, BLOB_NUM_INT_FIELDS);
                    }
                } else {
                    eliminated++;
                }
            } else {
                eliminated++;
            }

            if (eliminated != 0) {
                blobs[i + BLOB_TYPE] = BLOB_TYPE_NULL;
            }
        }
        return blobs;
    }
}

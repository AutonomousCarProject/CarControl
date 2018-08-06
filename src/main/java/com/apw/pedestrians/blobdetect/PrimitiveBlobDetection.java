package com.apw.pedestrians.blobdetect;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.apw.pedestrians.image.Color;
import com.apw.pedestrians.image.Pixel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class PrimitiveBlobDetection {
    private static final int MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN = 75;
    private static final int BLOB_TYPE = 0, BLOB_TOP = 1, BLOB_LEFT = 2, BLOB_BOTTOM = 3, BLOB_RIGHT = 4, BLOB_COLOR = 5, BLOB_NUMFIELDS = 6;
    private static final int BLOB_TYPE_NULL = 0, BLOB_TYPE_VALUE = 1, BLOB_TYPE_REFERENCE = 2;

    private int[] blobs;
    private List<Blob> blobObjects = new LinkedList<>();
    private Deque<Blob> unusedBlobs = new ArrayDeque<>();

    public List<Blob> getBlobs(Pixel[][] pixels) {
        if (pixels.length == 0) {
            return null;
        }

        final int width = pixels.length;
        final int height = pixels[0].length;

        int[] colors = new int[width * height];

        for (int i = 0; i < colors.length; i++) {
            colors[i] = pixels[i / width][i % width].getColor().ordinal();
        }

        int bipSize = width * height * BLOB_NUMFIELDS;
        if (blobs == null || blobs.length != bipSize) {
            blobs = new int[bipSize];
        } else {
            for (int i = 0; i < blobs.length; i += BLOB_NUMFIELDS) {
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

                    int bip1 = ((row * width) + col) * BLOB_NUMFIELDS;
                    int bip2 = ((row * width) + col + 1) * BLOB_NUMFIELDS;

                    if (color1 != color2) {
                        //either adds to the bip if there is an existing one or creates a new one if there isn't
                        if (blobs[bip1 + BLOB_TYPE] == BLOB_TYPE_NULL) {
                            blobs[bip1 + BLOB_TYPE] = BLOB_TYPE_VALUE;
                            blobs[bip1 + BLOB_TOP] = row;
                            blobs[bip1 + BLOB_LEFT] = col;
                            blobs[bip1 + BLOB_BOTTOM] = row;
                            blobs[bip1 + BLOB_RIGHT] = col + 1;
                            blobs[bip1 + BLOB_COLOR] = color1;
                        } else if (blobs[bip1 + BLOB_TYPE] == BLOB_TYPE_VALUE) {
                            blobs[bip1 + BLOB_RIGHT] = max(blobs[bip1 + BLOB_RIGHT], col + 1);
                        } else if (blobs[bip1 + BLOB_TYPE] == BLOB_TYPE_REFERENCE) {
                            // get pointer to actual bip value from top/left fields
                            int tlBip = bip1;
                            while (blobs[tlBip + BLOB_TYPE] == BLOB_TYPE_REFERENCE) {
                                tlBip = ((blobs[tlBip + BLOB_TOP] * width) + blobs[tlBip + BLOB_LEFT]) * BLOB_NUMFIELDS;
                            }
                            blobs[tlBip + BLOB_RIGHT] = max(blobs[tlBip + BLOB_RIGHT], col + 1);
                        }
                        blobs[bip2 + BLOB_TYPE] = BLOB_TYPE_REFERENCE;
                        blobs[bip2 + BLOB_TOP] = blobs[bip1 + BLOB_TOP];
                        blobs[bip2 + BLOB_LEFT] = blobs[bip1 + BLOB_LEFT];
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

                    int bip1 = ((row * width) + col) * BLOB_NUMFIELDS;
                    int bip2 = (((row + 1) * width) + col + 1) * BLOB_NUMFIELDS;

                    int tlBip1 = bip1;
                    while (blobs[tlBip1 + BLOB_TYPE] == BLOB_TYPE_REFERENCE) {
                        tlBip1 = ((blobs[tlBip1 + BLOB_TOP] * width) + blobs[tlBip1 + BLOB_LEFT]) * BLOB_NUMFIELDS;
                    }

                    int tlBip2 = bip2;
                    while (blobs[tlBip2 + BLOB_TYPE] == BLOB_TYPE_REFERENCE) {
                        tlBip2 = ((blobs[tlBip2 + BLOB_TOP] * width) + blobs[tlBip2 + BLOB_LEFT]) * BLOB_NUMFIELDS;
                    }

                    int bip1Width = blobs[tlBip1 + BLOB_RIGHT] - blobs[tlBip1 + BLOB_LEFT] + 1;
                    int bip2Width = blobs[tlBip2 + BLOB_RIGHT] - blobs[tlBip2 + BLOB_LEFT] + 1;

                    //merges pixels that are vertically nearby and of same color
                    if (color1 == color2) {
                        if (blobs[bip1 + BLOB_TYPE] != BLOB_TYPE_NULL
                                && blobs[bip2 + BLOB_TYPE] != BLOB_TYPE_NULL
                                && abs(bip2Width - bip1Width) <= MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN) {

                            blobs[tlBip1 + BLOB_LEFT] = min(blobs[tlBip1 + BLOB_LEFT], blobs[tlBip2 + BLOB_LEFT]);
                            blobs[tlBip1 + BLOB_RIGHT] = max(blobs[tlBip1 + BLOB_RIGHT], blobs[tlBip2 + BLOB_RIGHT]);
                            blobs[tlBip1 + BLOB_TOP] = min(blobs[tlBip1 + BLOB_TOP], blobs[tlBip2 + BLOB_TOP]);
                            blobs[tlBip1 + BLOB_BOTTOM] = max(blobs[tlBip1 + BLOB_BOTTOM], blobs[tlBip2 + BLOB_BOTTOM]);

                            blobs[tlBip2 + BLOB_TYPE] = BLOB_TYPE_REFERENCE;
                            blobs[tlBip2 + BLOB_TOP] = blobs[tlBip1 + BLOB_TOP];
                            blobs[tlBip2 + BLOB_LEFT] = blobs[tlBip1 + BLOB_LEFT];
                        }
                        // Potentially re-add these later, but according to the original algorithm, these will not be called
                        // Also consider adding a case where they are both null
                        /*
                        else if(blobs[bip1 + BLOB_TYPE] != BLOB_TYPE_NULL
                                && blobs[bip2 + BLOB_TYPE] == BLOB_TYPE_NULL
                                && bip1Width < MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN) {

                            blobs[tlBip1 + BLOB_BOTTOM] = blobs[tlBip1 + BLOB_BOTTOM] + 1;

                            blobs[tlBip2 + BLOB_TYPE] = BLOB_TYPE_REFERENCE;
                            blobs[tlBip2 + BLOB_TOP] = blobs[tlBip1 + BLOB_TOP];
                            blobs[tlBip2 + BLOB_LEFT] = blobs[tlBip1 + BLOB_LEFT];
                        }
                        else if(blobs[bip1 + BLOB_TYPE] == BLOB_TYPE_NULL
                                && blobs[bip2 + BLOB_TYPE] != BLOB_TYPE_NULL
                                && bip2Width < MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN) {

                            blobs[tlBip2 + BLOB_TOP] = blobs[tlBip2 + BLOB_TOP] + 1;

                            blobs[tlBip1 + BLOB_TYPE] = BLOB_TYPE_REFERENCE;
                            blobs[tlBip1 + BLOB_TOP] = blobs[tlBip2 + BLOB_TOP];
                            blobs[tlBip1 + BLOB_LEFT] = blobs[tlBip2 + BLOB_LEFT];
                        }
                        */
                    }
                }
            }
        };
        bottomCheckKernel.execute(rowRange);
        bottomCheckKernel.dispose();

        //eliminates blobObjects that are too large or too small or grey
        for (int i = 0; i < blobs.length; i += BLOB_NUMFIELDS) {
            if (blobs[i + BLOB_TYPE] == BLOB_TYPE_VALUE) {
                int bipWidth = blobs[i + BLOB_RIGHT] - blobs[i + BLOB_LEFT] + 1;
                int bipHeight = blobs[i + BLOB_BOTTOM] - blobs[i + BLOB_TOP] + 1;

                if (bipWidth >= 4 && bipHeight >= 4
                        && bipWidth < (width / 2)
                        && bipHeight < (height / 2)
                        && blobs[i + BLOB_COLOR] != Color.GREY.ordinal()) {
                    blobObjects.add(getBlob(i));
                }
            }
        }

        return blobObjects;
    }

    // reduce, reuse, recycle for blobObjects in progress
    // reduces the need for object allocation at runtime
    private Blob getBlob(int bip) {
        int bipWidth = blobs[bip + BLOB_RIGHT] - blobs[bip + BLOB_LEFT] + 1;
        int bipHeight = blobs[bip + BLOB_BOTTOM] - blobs[bip + BLOB_TOP] + 1;

        Color bipColor = Color.values()[blobs[bip + BLOB_COLOR]];
        if (unusedBlobs.isEmpty()) {
            return new Blob(bipWidth, bipHeight, blobs[bip + BLOB_LEFT], blobs[bip + BLOB_TOP], new Pixel(bipColor));
        } else {
            Blob blob = unusedBlobs.pop();
            blob.width = bipWidth;
            blob.height = bipHeight;
            blob.x = blobs[bip + BLOB_LEFT];
            blob.y = blobs[bip + BLOB_TOP];
            blob.color = new Pixel(bipColor);
            return blob;
        }
    }
}

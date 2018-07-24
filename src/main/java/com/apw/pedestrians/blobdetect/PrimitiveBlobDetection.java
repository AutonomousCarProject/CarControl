package com.apw.pedestrians.blobdetect;

//import com.aparapi.Kernel;
//import com.aparapi.Range;
import com.apw.pedestrians.image.Color;
import com.apw.pedestrians.image.IImage;
import com.apw.pedestrians.image.IPixel;
import com.apw.pedestrians.image.Pixel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class PrimitiveBlobDetection implements IBlobDetection {
    public static final int MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN = 75;
    private static final int BIP_TYPE = 0, BIP_TOP = 1, BIP_LEFT = 2, BIP_BOTTOM = 3, BIP_RIGHT = 4, BIP_COLOR = 5, BIP_NUMFIELDS = 6;
    private static final int BIP_TYPE_NULL = 0, BIP_TYPE_VALUE = 1, BIP_TYPE_REFERENCE = 2;
    //creates data structures to organize different stages of blobs
    private int[] bips;
    private List<Blob> blobs = new LinkedList<>();
    private Deque<Blob> unusedBlobs = new ArrayDeque<>();

    @Override
    public List<Blob> getBlobs(IImage image) {
        IPixel[][] pixels = image.getImage();

        if (pixels.length == 0) {
            return null;
        }

        final int width = pixels.length;
        final int height = pixels[0].length;

        int[] colors = new int[width * height];

        for (int i = 0; i < colors.length; i++) {
            colors[i] = pixels[i / width][i % width].getColor().ordinal();
        }

        int bipSize = width * height * BIP_NUMFIELDS;
        if (bips == null || bips.length != bipSize) {
            bips = new int[bipSize];
        } else {
            for (int i = 0; i < bips.length; i += BIP_NUMFIELDS) {
                bips[i + BIP_TYPE] = BIP_TYPE_NULL;
            }
        }

       // Range rowRange = Range.create(pixels.length);

        // check the pixel to the right
        /*
        Kernel rightCheckKernel = new Kernel() {
            @Override
            public void run() {
                int row = getGlobalId();
                for (int col = 0; col < pixels[0].length - 1; col++) {
                    int color1 = colors[(row * width) + col];
                    int color2 = colors[(row * width) + col + 1];

                    int bip1 = ((row * width) + col) * BIP_NUMFIELDS;
                    int bip2 = ((row * width) + col + 1) * BIP_NUMFIELDS;

                    if (color1 != color2) {
                        //either adds to the bip if there is an existing one or creates a new one if there isn't
                        if (bips[bip1 + BIP_TYPE] == BIP_TYPE_NULL) {
                            bips[bip1 + BIP_TYPE] = BIP_TYPE_VALUE;
                            bips[bip1 + BIP_TOP] = row;
                            bips[bip1 + BIP_LEFT] = col;
                            bips[bip1 + BIP_BOTTOM] = row;
                            bips[bip1 + BIP_RIGHT] = col + 1;
                            bips[bip1 + BIP_COLOR] = color1;
                        } else if (bips[bip1 + BIP_TYPE] == BIP_TYPE_VALUE) {
                            bips[bip1 + BIP_RIGHT] = max(bips[bip1 + BIP_RIGHT], col + 1);
                        } else if (bips[bip1 + BIP_TYPE] == BIP_TYPE_REFERENCE) {
                            // get pointer to actual bip value from top/left fields
                            int tlBip = bip1;
                            while (bips[tlBip + BIP_TYPE] == BIP_TYPE_REFERENCE) {
                                tlBip = ((bips[tlBip + BIP_TOP] * width) + bips[tlBip + BIP_LEFT]) * BIP_NUMFIELDS;
                            }
                            bips[tlBip + BIP_RIGHT] = max(bips[tlBip + BIP_RIGHT], col + 1);
                        }
                        bips[bip2 + BIP_TYPE] = BIP_TYPE_REFERENCE;
                        bips[bip2 + BIP_TOP] = bips[bip1 + BIP_TOP];
                        bips[bip2 + BIP_LEFT] = bips[bip1 + BIP_LEFT];
                    }
                }
            }
        };
        rightCheckKernel.execute(rowRange);
        rightCheckKernel.dispose();
        */
        /*
        Kernel bottomCheckKernel = new Kernel() {
            @Override
            public void run() {
                int row = getGlobalId();
                for (int col = 0; col < pixels[0].length; col++) {
                    int color1 = colors[(row * width) + col];
                    int color2 = colors[(row * width) + col + 1];

                    int bip1 = ((row * width) + col) * BIP_NUMFIELDS;
                    int bip2 = (((row + 1) * width) + col + 1) * BIP_NUMFIELDS;

                    int tlBip1 = bip1;
                    while (bips[tlBip1 + BIP_TYPE] == BIP_TYPE_REFERENCE) {
                        tlBip1 = ((bips[tlBip1 + BIP_TOP] * width) + bips[tlBip1 + BIP_LEFT]) * BIP_NUMFIELDS;
                    }

                    int tlBip2 = bip2;
                    while (bips[tlBip2 + BIP_TYPE] == BIP_TYPE_REFERENCE) {
                        tlBip2 = ((bips[tlBip2 + BIP_TOP] * width) + bips[tlBip2 + BIP_LEFT]) * BIP_NUMFIELDS;
                    }

                    int bip1Width = bips[tlBip1 + BIP_RIGHT] - bips[tlBip1 + BIP_LEFT] + 1;
                    int bip2Width = bips[tlBip2 + BIP_RIGHT] - bips[tlBip2 + BIP_LEFT] + 1;

                    //merges pixels that are vertically nearby and of same color
                    if (color1 == color2) {
                        if (bips[bip1 + BIP_TYPE] != BIP_TYPE_NULL
                                && bips[bip2 + BIP_TYPE] != BIP_TYPE_NULL
                                && abs(bip2Width - bip1Width) <= MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN) {

                            bips[tlBip1 + BIP_LEFT] = min(bips[tlBip1 + BIP_LEFT], bips[tlBip2 + BIP_LEFT]);
                            bips[tlBip1 + BIP_RIGHT] = max(bips[tlBip1 + BIP_RIGHT], bips[tlBip2 + BIP_RIGHT]);
                            bips[tlBip1 + BIP_TOP] = min(bips[tlBip1 + BIP_TOP], bips[tlBip2 + BIP_TOP]);
                            bips[tlBip1 + BIP_BOTTOM] = max(bips[tlBip1 + BIP_BOTTOM], bips[tlBip2 + BIP_BOTTOM]);

                            bips[tlBip2 + BIP_TYPE] = BIP_TYPE_REFERENCE;
                            bips[tlBip2 + BIP_TOP] = bips[tlBip1 + BIP_TOP];
                            bips[tlBip2 + BIP_LEFT] = bips[tlBip1 + BIP_LEFT];
                        }
                        // Potentially re-add these later, but according to the original algorithm, these will not be called
                        // Also consider adding a case where they are both null
                        /*
                        else if(bips[bip1 + BIP_TYPE] != BIP_TYPE_NULL
                                && bips[bip2 + BIP_TYPE] == BIP_TYPE_NULL
                                && bip1Width < MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN) {

                            bips[tlBip1 + BIP_BOTTOM] = bips[tlBip1 + BIP_BOTTOM] + 1;

                            bips[tlBip2 + BIP_TYPE] = BIP_TYPE_REFERENCE;
                            bips[tlBip2 + BIP_TOP] = bips[tlBip1 + BIP_TOP];
                            bips[tlBip2 + BIP_LEFT] = bips[tlBip1 + BIP_LEFT];
                        }
                        else if(bips[bip1 + BIP_TYPE] == BIP_TYPE_NULL
                                && bips[bip2 + BIP_TYPE] != BIP_TYPE_NULL
                                && bip2Width < MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN) {

                            bips[tlBip2 + BIP_TOP] = bips[tlBip2 + BIP_TOP] + 1;

                            bips[tlBip1 + BIP_TYPE] = BIP_TYPE_REFERENCE;
                            bips[tlBip1 + BIP_TOP] = bips[tlBip2 + BIP_TOP];
                            bips[tlBip1 + BIP_LEFT] = bips[tlBip2 + BIP_LEFT];
                        }
                        *//*
                    }
                }
            }
        };
        */
        /*
        bottomCheckKernel.execute(rowRange);
        bottomCheckKernel.dispose();
        */
        //eliminates blobs that are too large or too small or grey
        for (int i = 0; i < bips.length; i += BIP_NUMFIELDS) {
            if (bips[i + BIP_TYPE] == BIP_TYPE_VALUE) {
                int bipWidth = bips[i + BIP_RIGHT] - bips[i + BIP_LEFT] + 1;
                int bipHeight = bips[i + BIP_BOTTOM] - bips[i + BIP_TOP] + 1;

                if (bipWidth >= 4 && bipHeight >= 4
                        && bipWidth < (width / 2)
                        && bipHeight < (height / 2)
                        && bips[i + BIP_COLOR] != Color.GREY.ordinal()) {
                    blobs.add(getBlob(i));
                }
            }
        }

        return blobs;
    }

    // reduce, reuse, recycle for blobs in progress
    // reduces the need for object allocation at runtime
    private Blob getBlob(int bip) {
        int bipWidth = bips[bip + BIP_RIGHT] - bips[bip + BIP_LEFT] + 1;
        int bipHeight = bips[bip + BIP_BOTTOM] - bips[bip + BIP_TOP] + 1;

        Color bipColor = Color.values()[bips[bip + BIP_COLOR]];
        if (unusedBlobs.isEmpty()) {
            return new Blob(bipWidth, bipHeight, bips[bip + BIP_LEFT], bips[bip + BIP_TOP], new Pixel(bipColor));
        } else {
            Blob blob = unusedBlobs.pop();
            blob.width = bipWidth;
            blob.height = bipHeight;
            blob.x = bips[bip + BIP_LEFT];
            blob.y = bips[bip + BIP_TOP];
            blob.color = new Pixel(bipColor);
            return blob;
        }

    }

}

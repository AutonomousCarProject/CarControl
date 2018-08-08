package com.apw.pedestrians.blobdetect;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.apw.pedestrians.image.Color;
import com.apw.pedestrians.image.Pixel;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static com.apw.pedestrians.blobdetect.PrimitiveBlob.*;

public class PrimitiveBlobDetection extends BlobDetection {
    private static final int MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN = 75;

    private int[] blobs;
    private Deque<Blob> unusedBlobs = new ArrayDeque<>();

    private List<Blob> blobList = new LinkedList<>();

    BottomCheckKernel bottomCheckKernel = new BottomCheckKernel();
    RightCheckKernel rightCheckKernel = new RightCheckKernel();

    @Override
    public List<Blob> getBlobs(Pixel[][] pixels) {
        if (pixels.length == 0) {
            return null;
        }

        unusedBlobs.addAll(blobList);
        blobList.clear();

        final int width = pixels[0].length;
        final int height = pixels.length;

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

        //noinspection SuspiciousNameCombination
        Range rowRange = Range.create(height);
        Range colRange1 = Range.create(width / 2);
        //Range colRange2 = Range.create2D(width / 2, 1);

        rightCheckKernel.setValues(colors, blobs, width);
        rightCheckKernel.execute(rowRange);

        bottomCheckKernel.setValues(colors, blobs, width, height);
        bottomCheckKernel.execute(colRange1);
        //bottomCheckKernel.execute(colRange2);

        // eliminates blobs that are too large or too small or grey
        // also brings the blobs as left as possible within the array
        // this makes it so that there is no need to allocate a new array and reduces amount of iteration needed later

        /*
        int eliminated = 0;

        for (int i = 0; i < blobs.length; i += BLOB_NUM_INT_FIELDS) {
            if (blobs[i + BLOB_TYPE] == BLOB_TYPE_VALUE) {
                int blobWidth = blobs[i + BLOB_RIGHT] - blobs[i + BLOB_LEFT] + 1;
                int blobHeight = blobs[i + BLOB_BOTTOM] - blobs[i + BLOB_TOP] + 1;

                if (blobWidth >= 4 && blobHeight >= 4 && blobWidth < (width / 2) && blobHeight < (height / 2) && blobs[i + BLOB_COLOR] != Color.GREY.ordinal()) {
                    // do not eliminate
                    if (eliminated != 0) {
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
        */

        for (int i = 0; i < blobs.length; i += BLOB_NUM_INT_FIELDS) {
            if (blobs[i + BLOB_TYPE] == BLOB_TYPE_VALUE) {
                blobList.add(getBlob(i));
            }
        }

        return blobList;
    }

    private static final Pixel[] colors = Arrays.stream(Color.values()).map(Pixel::new).toArray(Pixel[]::new);

    private Blob getBlob(int i) {
        int x = blobs[i + BLOB_LEFT];
        int y = blobs[i + BLOB_TOP];
        int width = blobs[i + BLOB_RIGHT] - blobs[i + BLOB_LEFT] + 1;
        int height = blobs[i + BLOB_BOTTOM] - blobs[i + BLOB_TOP] + 1;
        Pixel color = colors[blobs[i + BLOB_COLOR]];


        if (unusedBlobs.isEmpty()) {
            return new Blob(width, height, x, y, color);
        } else {
            Blob blob = unusedBlobs.pop();
            blob.set(width, height, x, y, color);
            return blob;
        }
    }
}
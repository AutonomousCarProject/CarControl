package com.apw.pedestrians.blobdetect;

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
        long time = System.currentTimeMillis();

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
        Range colRange = Range.create(width);

        rightCheckKernel.setValues(colors, blobs, width);
        rightCheckKernel.execute(rowRange);

        bottomCheckKernel.setValues(colors, blobs, width, height);
        bottomCheckKernel.execute(colRange);

        for (int i = 0; i < blobs.length; i += BLOB_NUM_INT_FIELDS) {
            if (blobs[i + BLOB_TYPE] == BLOB_TYPE_VALUE) {
                blobList.add(getBlob(i));
            }
        }

        System.out.println(System.currentTimeMillis() - time);

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
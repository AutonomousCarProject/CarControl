package com.apw.pedestrians.blobdetect;

import com.aparapi.Kernel;

import static com.apw.pedestrians.blobdetect.PrimitiveBlob.*;
import static com.apw.pedestrians.blobdetect.PrimitiveBlob.BLOB_REFERENCE_INDEX;

public class RightCheckKernel extends Kernel {

    int[] colors, blobs;

    int width;

    public void setValues(int[] colors, int[] blobs, int width) {
        this.colors = colors;
        this.blobs = blobs;
        this.width = width;
    }

    @Override
    public void run() {
        int row = getGlobalId();
        for (int col = 0; col < width - 1; col++) {
            int color1 = colors[(row * width) + col];
            int color2 = colors[(row * width) + col + 1];

            int blob1 = ((row * width) + col) * BLOB_NUM_INT_FIELDS;
            int blob2 = ((row * width) + col + 1) * BLOB_NUM_INT_FIELDS;

            if (color1 == color2) {
                //either adds to the blob if there is an existing one or creates a new one if there isn't
                if (blobs[blob1 + BLOB_TYPE] == BLOB_TYPE_NULL) {
                    blobs[blob1 + BLOB_TYPE] = BLOB_TYPE_VALUE;
                    blobs[blob1 + BLOB_TOP] = row;
                    blobs[blob1 + BLOB_LEFT] = col;
                    blobs[blob1 + BLOB_BOTTOM] = row;
                    blobs[blob1 + BLOB_RIGHT] = col + 1;
                    blobs[blob1 + BLOB_COLOR] = color1;
                    blobs[blob1 + BLOB_MATCHED] = BLOB_IS_UNMATCHED;

                    blobs[blob2 + BLOB_TYPE] = BLOB_TYPE_REFERENCE;
                    blobs[blob2 + BLOB_REFERENCE_INDEX] = blob1;
                } else if (blobs[blob1 + BLOB_TYPE] == BLOB_TYPE_VALUE) {
                    blobs[blob1 + BLOB_RIGHT] = max(blobs[blob1 + BLOB_RIGHT], col + 1);

                    blobs[blob2 + BLOB_TYPE] = BLOB_TYPE_REFERENCE;
                    blobs[blob2 + BLOB_REFERENCE_INDEX] = blob1;
                } else if (blobs[blob1 + BLOB_TYPE] == BLOB_TYPE_REFERENCE) {
                    // get pointer to actual blob value from top/left fields
                    int tlBlob = blob1;
                    while (blobs[tlBlob + BLOB_TYPE] == BLOB_TYPE_REFERENCE) {
                        tlBlob = blobs[tlBlob + BLOB_REFERENCE_INDEX];
                    }
                    blobs[tlBlob + BLOB_RIGHT] = max(blobs[tlBlob + BLOB_RIGHT], col + 1);

                    blobs[blob2 + BLOB_TYPE] = BLOB_TYPE_REFERENCE;
                    blobs[blob2 + BLOB_REFERENCE_INDEX] = tlBlob;
                }

            }
        }
    }
}

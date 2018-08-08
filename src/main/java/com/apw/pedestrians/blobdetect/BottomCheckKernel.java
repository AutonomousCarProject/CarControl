package com.apw.pedestrians.blobdetect;

import com.aparapi.Kernel;

import static com.apw.pedestrians.blobdetect.BlobDetection.MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN;
import static com.apw.pedestrians.blobdetect.PrimitiveBlob.*;

public class BottomCheckKernel extends Kernel {

    int[] colors, blobs;

    int width, height;

    public void setValues(int[] colors, int[] blobs, int width, int height) {
        this.colors = colors;
        this.blobs = blobs;
        this.width = width;
        this.height = height;
    }

    @Override
    public void run() {
//        int mode = getGlobalId(1);

  //      int col = getGlobalId(0) * 2 + mode;

        int col = getGlobalId(0) * 2;

        if(col >= width) {
            col = (col - width) + 1;
        }

        if (col < width) {
            for (int row = 0; row < height - 1; row++) {
                int color1 = colors[(row * width) + col];
                int color2 = colors[((row + 1) * width) + col];

                int blob1 = ((row * width) + col) * BLOB_NUM_INT_FIELDS;
                int blob2 = (((row + 1) * width) + col) * BLOB_NUM_INT_FIELDS;

                int tlBlob1 = blob1;
                while (blobs[tlBlob1 + BLOB_TYPE] == BLOB_TYPE_REFERENCE) {
                    tlBlob1 = blobs[tlBlob1 + BLOB_REFERENCE_INDEX];
                }

                int tlBlob2 = blob2;
                while (blobs[tlBlob2 + BLOB_TYPE] == BLOB_TYPE_REFERENCE) {
                    tlBlob2 = blobs[tlBlob2 + BLOB_REFERENCE_INDEX];
                }

                int blob1Width = blobs[tlBlob1 + BLOB_RIGHT] - blobs[tlBlob1 + BLOB_LEFT] + 1;
                int blob2Width = blobs[tlBlob2 + BLOB_RIGHT] - blobs[tlBlob2 + BLOB_LEFT] + 1;

                //merges pixels that are vertically nearby and of same color
                if (color1 == color2 && tlBlob1 != tlBlob2) {
                    if (blobs[blob1 + BLOB_TYPE] != BLOB_TYPE_NULL
                            && blobs[blob2 + BLOB_TYPE] != BLOB_TYPE_NULL
                            && Math.abs(blob2Width - blob1Width) <= MAXIMUM_DIFFERENCE_IN_WIDTH_BETWEEN_TWO_BLOBS_IN_ORDER_TO_JOIN) {

                        blobs[tlBlob1 + BLOB_LEFT] = Math.min(blobs[tlBlob1 + BLOB_LEFT], blobs[tlBlob2 + BLOB_LEFT]);
                        blobs[tlBlob1 + BLOB_RIGHT] = Math.max(blobs[tlBlob1 + BLOB_RIGHT], blobs[tlBlob2 + BLOB_RIGHT]);
                        blobs[tlBlob1 + BLOB_TOP] = Math.min(blobs[tlBlob1 + BLOB_TOP], blobs[tlBlob2 + BLOB_TOP]);
                        blobs[tlBlob1 + BLOB_BOTTOM] = Math.max(blobs[tlBlob1 + BLOB_BOTTOM], blobs[tlBlob2 + BLOB_BOTTOM]);

                        blobs[tlBlob2 + BLOB_TYPE] = BLOB_TYPE_REFERENCE;
                        blobs[tlBlob2 + BLOB_TOP] = tlBlob1;
                    }
                }
            }
        }
    }
}

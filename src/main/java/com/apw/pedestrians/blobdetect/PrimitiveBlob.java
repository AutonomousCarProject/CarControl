package com.apw.pedestrians.blobdetect;

public abstract class PrimitiveBlob {
    // Blob (int fields)
    public static final int BLOB_TYPE = 0, BLOB_TOP = 1, BLOB_LEFT = 2, BLOB_BOTTOM = 3, BLOB_RIGHT = 4, BLOB_COLOR = 5, BLOB_MATCHED = 6;

    // MovingBlob (float fields)
    public static final int MOVINGBLOB_VELOCITY_X = 0, MOVINGBLOB_VELOCITY_Y = 1, MOVINGBLOB_VELOCITY_CHANGE_X = 2, MOVINGBLOB_VELOCITY_CHANGE_Y = 3, MOVINGBLOB_PREDICTED_X = 4, MOVINGBLOB_PREDICTED_Y = 5, MOVINGBLOB_AGE = 6, MOVINGBLOB_AGE_OFF_SCREEN = 7;

    // Metadata
    public static final int BLOB_NUM_INT_FIELDS = 7;
    public static final int MOVINGBLOB_NUM_FLOAT_FIELDS = 8;

    // Enum values
    public static final int BLOB_TYPE_NULL = 0, BLOB_TYPE_VALUE = 1, BLOB_TYPE_REFERENCE = 2;
    public static final int BLOB_IS_UNMATCHED = 0, BLOB_IS_MATCHED = 1;
}

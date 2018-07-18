package com.apw.blobtrack;

import com.apw.blobdetect.Blob;

import java.util.List;

public interface IMovingBlobDetection {
    List<MovingBlob> getMovingBlobs(List<Blob> blobs);

    List<MovingBlob> getUnifiedBlobs(List<MovingBlob> movingBlobs);
}

package com.apw.pedestrians.blobtrack;

import com.apw.pedestrians.blobdetect.Blob;

import java.util.List;

public interface IMovingBlobDetection {
    List<MovingBlob> getMovingBlobs(List<Blob> blobs);

    List<MovingBlob> getUnifiedBlobs(List<MovingBlob> movingBlobs);

    List<MovingBlob> getFilteredUnifiedBlobs(List<MovingBlob> blobList);
}

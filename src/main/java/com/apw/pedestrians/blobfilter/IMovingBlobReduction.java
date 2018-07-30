package com.apw.pedestrians.blobfilter;

import com.apw.pedestrians.blobtrack.MovingBlob;

import java.util.List;

public interface IMovingBlobReduction {
    List<MovingBlob> filterMovingBlobs(List<MovingBlob> blobs);

    List<MovingBlob> filterUnifiedBlobs(List<MovingBlob> blobs);

    List<MovingBlob> filterFilteredUnifiedBlobs(List<MovingBlob> blobs);
}

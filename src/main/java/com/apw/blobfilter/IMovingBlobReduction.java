package com.apw.blobfilter;

import com.apw.blobtrack.MovingBlob;

import java.util.List;

public interface IMovingBlobReduction
{
    List<MovingBlob> filterMovingBlobs(List<MovingBlob> blobs);
}

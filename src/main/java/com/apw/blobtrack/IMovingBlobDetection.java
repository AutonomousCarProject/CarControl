package com.apw.blobtrack;

import java.util.List;

import com.apw.blobdetect.Blob;

public interface IMovingBlobDetection
{
    List<MovingBlob> getMovingBlobs(List<Blob> blobs);

	List<MovingBlob> getUnifiedBlobs(List<MovingBlob> movingBlobs);
}

package com.apw.pedestrians.blobdetect;

import com.apw.pedestrians.image.IImage;

import java.util.List;

// Interface Blob Detection
public interface IBlobDetection {
    List<Blob> getBlobs(IImage image);
}

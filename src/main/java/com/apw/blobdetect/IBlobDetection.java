package com.apw.blobdetect;

import java.util.List;

import com.apw.oldimage.IImage;

//Intereface Blob Detection
public interface IBlobDetection
{
    List<Blob> getBlobs(IImage image);
}

package com.apw.blobdetect;

import com.apw.oldimage.IImage;

import java.util.ArrayList;
import java.util.List;

public class TestBlobDetection implements IBlobDetection {
    @Override
    public List<Blob> getBlobs(IImage image) {
        List<Blob> blobs = new ArrayList<>();

        blobs.add(new Blob(120, 121, 60, 60, new TestPixel(0)));
        blobs.add(new Blob(120, 120, 180, 60, new TestPixel(1)));
        blobs.add(new Blob(120, 120, 300, 60, new TestPixel(0)));
        blobs.add(new Blob(120, 120, 420, 60, new TestPixel(1)));
        blobs.add(new Blob(160, 120, 560, 60, new TestPixel(0)));
        blobs.add(new Blob(120, 120, 60, 180, new TestPixel(1)));
        blobs.add(new Blob(120, 120, 180, 180, new TestPixel(0)));
        blobs.add(new Blob(120, 120, 300, 180, new TestPixel(1)));
        blobs.add(new Blob(120, 120, 420, 180, new TestPixel(0)));
        blobs.add(new Blob(160, 120, 560, 180, new TestPixel(1)));
        blobs.add(new Blob(120, 120, 60, 300, new TestPixel(0)));
        blobs.add(new Blob(120, 120, 180, 300, new TestPixel(1)));
        blobs.add(new Blob(120, 120, 300, 300, new TestPixel(0)));
        blobs.add(new Blob(120, 120, 420, 300, new TestPixel(1)));
        blobs.add(new Blob(160, 120, 560, 300, new TestPixel(0)));
        blobs.add(new Blob(120, 120, 60, 420, new TestPixel(1)));
        blobs.add(new Blob(120, 120, 180, 420, new TestPixel(0)));
        blobs.add(new Blob(120, 120, 300, 420, new TestPixel(1)));
        blobs.add(new Blob(120, 120, 420, 420, new TestPixel(0)));
        blobs.add(new Blob(160, 120, 560, 420, new TestPixel(1)));

        return blobs;
    }
}

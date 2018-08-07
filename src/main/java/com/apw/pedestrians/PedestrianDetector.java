package com.apw.pedestrians;

import com.apw.pedestrians.blobdetect.Blob;
import com.apw.pedestrians.blobdetect.BlobDetection;
import com.apw.pedestrians.blobdetect.PrimitiveBlobDetection;
import com.apw.pedestrians.blobfilter.BlobFilter;
import com.apw.pedestrians.blobtrack.MovingBlob;
import com.apw.pedestrians.blobtrack.MovingBlobDetection;
import com.apw.pedestrians.image.Color;
import com.apw.pedestrians.image.Pixel;

import java.util.Arrays;
import java.util.List;

public class PedestrianDetector {
    private static final Pixel[] pixels = Arrays.stream(Color.values()).map(Pixel::new).toArray(Pixel[]::new);

    private BlobDetection blobDetection;
    private MovingBlobDetection movingBlobDetection;
    private BlobFilter blobFilter;

    public PedestrianDetector() {
        this(new PrimitiveBlobDetection(), new MovingBlobDetection(), new BlobFilter());
    }

    public PedestrianDetector(BlobDetection blobDetection, MovingBlobDetection movingBlobDetection, BlobFilter blobFilter) {
        this.blobDetection = blobDetection;
        this.movingBlobDetection = movingBlobDetection;
        this.blobFilter = blobFilter;
    }

    public List<MovingBlob> getAllBlobs(byte[] colors, int width) {
        int height = colors.length / width;
        Pixel[][] image = new Pixel[height][width];
        for (int i = 0; i < colors.length; i++) {
            int row = i / width;
            int col = i % width;
            image[row][col] = pixels[colors[i]];
        }

        List<Blob> knownBlobs = blobDetection.getBlobs(image);
        return movingBlobDetection.getMovingBlobs(knownBlobs);
    }

    public List<MovingBlob> detect(byte[] colors, int width) {
        final List<MovingBlob> fmovingBlobs = blobFilter.filterMovingBlobs(getAllBlobs(colors, width));
        final List<MovingBlob> unifiedBlobs = movingBlobDetection.getUnifiedBlobs(fmovingBlobs);
        final List<MovingBlob> funifiedBlobs = blobFilter.filterUnifiedBlobs(unifiedBlobs);
        final List<MovingBlob> matchedUnifiedBlobs = movingBlobDetection.getFilteredUnifiedBlobs(funifiedBlobs);
        return blobFilter.filterFilteredUnifiedBlobs(matchedUnifiedBlobs);
    }

}

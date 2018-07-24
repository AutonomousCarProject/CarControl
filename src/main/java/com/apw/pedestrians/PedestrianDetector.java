package com.apw.pedestrians;

import com.apw.pedestrians.blobdetect.Blob;
import com.apw.pedestrians.blobdetect.BlobDetection;
import com.apw.pedestrians.blobdetect.IBlobDetection;
import com.apw.pedestrians.blobfilter.BlobFilter;
import com.apw.pedestrians.blobfilter.IMovingBlobReduction;
import com.apw.pedestrians.blobtrack.IMovingBlobDetection;
import com.apw.pedestrians.blobtrack.MovingBlob;
import com.apw.pedestrians.blobtrack.MovingBlobDetection;
import com.apw.pedestrians.image.Color;
import com.apw.pedestrians.image.IPixel;
import com.apw.pedestrians.image.Pixel;
import java.util.List;

public class PedestrianDetector {

  private IBlobDetection blobDetection;
  private IMovingBlobDetection movingBlobDetection;
  private IMovingBlobReduction blobFilter;

  public PedestrianDetector() {
    this(new BlobDetection(), new MovingBlobDetection(), new BlobFilter());
  }

  public PedestrianDetector(IBlobDetection blobDetection, IMovingBlobDetection movingBlobDetection,
      IMovingBlobReduction blobFilter) {
    this.blobDetection = blobDetection;
    this.movingBlobDetection = movingBlobDetection;
    this.blobFilter = blobFilter;
  }

  public List<MovingBlob> getAllBlobs(byte[] colors, int width) {
    int height = colors.length / width;
    IPixel[][] image = new IPixel[height][width];
    for (int i = 0; i < colors.length; i++) {
      int row = i / width;
      int col = i % width;
      image[row][col] = getPixel(colors[i]);
    }

    final List<Blob> knownBlobs = blobDetection.getBlobs(() -> image);
    return movingBlobDetection.getMovingBlobs(knownBlobs);
  }

  public List<MovingBlob> detect(byte[] colors, int width) {
    final List<MovingBlob> fmovingBlobs = blobFilter.filterMovingBlobs(getAllBlobs(colors, width));
    final List<MovingBlob> unifiedBlobs = movingBlobDetection.getUnifiedBlobs(fmovingBlobs);
    final List<MovingBlob> funifiedBlobs = blobFilter.filterUnifiedBlobs(unifiedBlobs);
    final List<MovingBlob> matchedUnifiedBlobs = movingBlobDetection
        .getFilteredUnifiedBlobs(funifiedBlobs);
    return blobFilter.filterFilteredUnifiedBlobs(matchedUnifiedBlobs);
  }

  public IPixel getPixel(byte b) {
    return new Pixel(Color.values()[b]);
  }
}

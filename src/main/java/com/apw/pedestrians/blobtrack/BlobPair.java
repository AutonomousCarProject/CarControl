package com.apw.pedestrians.blobtrack;

import com.apw.pedestrians.blobdetect.Blob;

public class BlobPair implements Comparable<BlobPair> {

  // The Blob in the current frame.
  Blob newBlob;
  // The MovingBlob from the previous frame.
  MovingBlob oldBlob;
  // The distance between the two Blobs in the pair.
  private float distance;

  /**
   * Creates a BlobPair from a Blob and MovingBlob in adjacent frames. Many BlobPairs
   * are created during the Blob matching process to match MovingBlobs with their Blob
   * counterparts in subsequent frames.
   *
   * @param distance The distance between the two Blobs
   * @param newBlob The Blob in the current frame
   * @param oldBlob The Blob from the previous frame
   */
  BlobPair(float distance, Blob newBlob, MovingBlob oldBlob) {
    this.distance = distance;
    this.newBlob = newBlob;
    this.oldBlob = oldBlob;
  }

  /**
   * Returns 1 if this BlobPair's distance attribute is greater than the other BlobPair's
   * distance attribute, -1 otherwise.
   *
   * @param o The BlobPair that will be compared.
   */
  public int compareTo(BlobPair o) {
    return (int) Math.signum(distance - o.distance);
  }
}

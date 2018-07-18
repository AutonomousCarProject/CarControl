package com.apw.blobtrack;

import com.apw.blobdetect.Blob;
import com.apw.blobdetect.TestBlobDetection;
import com.apw.blobfilter.BlobFilter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class MovingBlobDetectionTest implements IMovingBlobDetection {
	private List<MovingBlob> movingBlobs;

	public MovingBlobDetectionTest() {
		movingBlobs = new LinkedList<>();
	}

	public static void main(String[] args){
		TestBlobDetection test = new TestBlobDetection();
		MovingBlobDetection movingTest = new MovingBlobDetection();
		BlobFilter filter= new BlobFilter();

		final long startTime = System.currentTimeMillis();
		List<MovingBlob> list = new LinkedList<>();
		list.add(new MovingBlob(10, 10, 20, 20,  25, 27,15));
		list.add(new MovingBlob(11, 11, 200, 200, 0, 0,15));
		List<MovingBlob> unifiedBlobs = new LinkedList<>();
		
		for(MovingBlob blob: list){
			HashSet<MovingBlob> set = new HashSet<MovingBlob>();
			set.add(blob);
			unifiedBlobs.add(new UnifiedBlob(set));
		}
		System.out.println(list.get(1));
		System.out.println(unifiedBlobs.get(1));
		List<MovingBlob> filterList1 = filter.filterMovingBlobs(list);
		List<MovingBlob> filterList2 = filter.filterUnifiedBlobs(unifiedBlobs);
		System.out.println(filterList1.get(0));
		System.out.println(list.size());
		System.out.println(unifiedBlobs.size());
		System.out.println(filterList1.size());
		System.out.println(filterList2.size());

		final long endTime = System.currentTimeMillis();

		System.out.println("Total execution time: " + (endTime - startTime) );
	}

	// test data
	public List<MovingBlob> getMovingBlobs(List<Blob> blobList){
		for (Blob blob:blobList){
			this.movingBlobs.add(new MovingBlob(blob));
		}
		return movingBlobs;
	}

	public List<MovingBlob> getUnifiedBlobs(List<MovingBlob> blobs) {return blobs;}
}

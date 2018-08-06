package com.apw.pedestrians.blobfilter;

import com.apw.pedestrians.blobtrack.MovingBlob;
import com.aparapi.Kernel;
import com.aparapi.Range;
import com.apw.pedestrians.Constant;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PrimitiveBlobFilter {
	private static final int VELOCITY_X = 0, VELOCITY_Y = 1, VELOCITY_CHANGE_X = 2, VELOCITY_CHANGE_Y = 3,
			PREDICTED_X = 4, PREDICTED_Y = 5, AGE = 6, AGE_OFF_SCREEN = 7, WIDTH = 8, HEIGHT = 9, GET_SCALED_VELOCITY_X = 10, GET_SCALED_VELOCITY_Y = 11, DENSITY = 12, NUMFIELDS = 13;

	public List<MovingBlob> filterMovingBlobs(List<MovingBlob> blobObjects) {
		float[] blobs = new float[blobObjects.size() * NUMFIELDS];
		boolean[] shouldBeFiltered = new boolean[blobObjects.size()];

		for (int i = 0; i < blobObjects.size(); i++) {
			MovingBlob blob = blobObjects.get(i);
			blobs[(i * NUMFIELDS) + VELOCITY_X] = blob.velocityX;
			blobs[(i * NUMFIELDS) + VELOCITY_Y] = blob.velocityY;
			blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_X] = blob.velocityChangeX;
			blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_Y] = blob.velocityChangeY;
			blobs[(i * NUMFIELDS) + PREDICTED_X] = blob.predictedX;
			blobs[(i * NUMFIELDS) + PREDICTED_Y] = blob.predictedY;
			blobs[(i * NUMFIELDS) + AGE] = blob.age;
			blobs[(i * NUMFIELDS) + AGE_OFF_SCREEN] = blob.ageOffScreen;
			blobs[(i * NUMFIELDS) + WIDTH] = blob.width;
			blobs[(i * NUMFIELDS) + HEIGHT] = blob.height;
		}

		Kernel kernel = new Kernel() {
			@Override
			public void run() {
				int i = getGlobalId();
				if (blobs[(i * NUMFIELDS) + AGE] >= com.apw.pedestrians.Constant.AGE_MIN
						&& abs(blobs[(i * NUMFIELDS) + VELOCITY_Y]) < com.apw.pedestrians.Constant.VELOCITY_Y_MAX
						&& abs(blobs[(i * NUMFIELDS) + VELOCITY_X]) < com.apw.pedestrians.Constant.VELOCITY_X_MAX
						&& abs(blobs[(i * NUMFIELDS) + VELOCITY_Y]) > com.apw.pedestrians.Constant.VELOCITY_Y_MIN
						&& abs(blobs[(i * NUMFIELDS) + VELOCITY_X]) > com.apw.pedestrians.Constant.VELOCITY_X_MIN
						&& blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_X] < com.apw.pedestrians.Constant.MAX_VELOCITY_CHANGE_X
						&& blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_Y] < com.apw.pedestrians.Constant.MAX_VELOCITY_CHANGE_Y
						&& blobs[(i * NUMFIELDS) + WIDTH] / (float) blobs[(i * NUMFIELDS) + HEIGHT] < 1.3
						&& blobs[(i * NUMFIELDS) + WIDTH] * (float) blobs[(i * NUMFIELDS) + HEIGHT] > 40) {
					shouldBeFiltered[i] = false; // .add(blob);
				} 
				else {
					shouldBeFiltered[i] = true;
				}

			}
		};

		kernel.execute(Range.create(blobObjects.size()));
		kernel.dispose();

		//part 2
		ArrayList<MovingBlob> filtered = new ArrayList<MovingBlob>();
		//ArrayList<MovingBlob> unfiltered = new ArrayList<MovingBlob>();
		// convert back 
		for (int i = 0; i < blobObjects.size(); i++) {
			if (shouldBeFiltered[i] == false) {
				MovingBlob blob = new MovingBlob();
				blob.velocityX = blobs[(i * NUMFIELDS) + VELOCITY_X];
				blob.velocityY = blobs[(i * NUMFIELDS) + VELOCITY_Y];
				blob.velocityChangeX = blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_X];
				blob.velocityChangeY = blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_Y];
				blob.predictedX = blobs[(i * NUMFIELDS) + PREDICTED_X];
				blob.predictedY = blobs[(i * NUMFIELDS) + PREDICTED_Y];
				blob.age = (int) blobs[(i * NUMFIELDS) + AGE];
				blob.ageOffScreen = (int) blobs[(i * NUMFIELDS) + AGE_OFF_SCREEN];
				blob.width = (int) blobs[(i * NUMFIELDS) + WIDTH];
				blob.height = (int) blobs[(i * NUMFIELDS) + HEIGHT];
				filtered.add(blob);
			}
		}

		return null;
	}

	public List<MovingBlob> filterUnifiedBlobs(List<MovingBlob> blobObjects) {
		float[] blobs = new float[blobObjects.size() * NUMFIELDS];
		boolean[] shouldBeFiltered = new boolean[blobObjects.size()];

		for (int i = 0; i < blobObjects.size(); i++) {
			MovingBlob blob = blobObjects.get(i);
			blobs[(i * NUMFIELDS) + VELOCITY_X] = blob.velocityX;
			blobs[(i * NUMFIELDS) + VELOCITY_Y] = blob.velocityY;
			blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_X] = blob.velocityChangeX;
			blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_Y] = blob.velocityChangeY;
			blobs[(i * NUMFIELDS) + PREDICTED_X] = blob.predictedX;
			blobs[(i * NUMFIELDS) + PREDICTED_Y] = blob.predictedY;
			blobs[(i * NUMFIELDS) + AGE] = blob.age;
			blobs[(i * NUMFIELDS) + AGE_OFF_SCREEN] = blob.ageOffScreen;
			blobs[(i * NUMFIELDS) + WIDTH] = blob.width;
			blobs[(i * NUMFIELDS) + HEIGHT] = blob.height;
			blobs[(i * NUMFIELDS) + GET_SCALED_VELOCITY_X] = blob.getScaledVelocityX();
			blobs[(i * NUMFIELDS) + GET_SCALED_VELOCITY_Y] = blob.getScaledVelocityY();
			blobs[(i * NUMFIELDS) + DENSITY] = blob.getDensity();
		}
		
		for (int i = 0; i < blobObjects.size(); i++) {
			MovingBlob blob = blobObjects.get(i);
			blobs[(i * NUMFIELDS) + VELOCITY_X] = blob.velocityX;
			blobs[(i * NUMFIELDS) + VELOCITY_Y] = blob.velocityY;
			blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_X] = blob.velocityChangeX;
			blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_Y] = blob.velocityChangeY;
			blobs[(i * NUMFIELDS) + PREDICTED_X] = blob.predictedX;
			blobs[(i * NUMFIELDS) + PREDICTED_Y] = blob.predictedY;
			blobs[(i * NUMFIELDS) + AGE] = blob.age;
			blobs[(i * NUMFIELDS) + AGE_OFF_SCREEN] = blob.ageOffScreen;
			blobs[(i * NUMFIELDS) + WIDTH] = blob.width;
			blobs[(i * NUMFIELDS) + HEIGHT] = blob.height;
		}
	

		Kernel kernel = new Kernel() {
			@Override
			public void run() {
				int i = getGlobalId();
				if ((float) blobs[(i * NUMFIELDS) + WIDTH] / (float) blobs[(i * NUMFIELDS) + HEIGHT] < com.apw.pedestrians.Constant.MAX_WIDTH_HEIGHT_RATIO
						&& blobs[(i * NUMFIELDS) + WIDTH] < com.apw.pedestrians.Constant.MAX_WIDTH && blobs[(i * NUMFIELDS) + HEIGHT] < com.apw.pedestrians.Constant.MAX_HEIGHT
						&& Math.abs(blobs[(i * NUMFIELDS) + GET_SCALED_VELOCITY_X]) > com.apw.pedestrians.Constant.MIN_SCALED_VELOCITY_X
						&& Math.abs(blobs[(i * NUMFIELDS) + GET_SCALED_VELOCITY_Y]) > com.apw.pedestrians.Constant.MIN_SCALED_VELOCITY_Y && blobs[(i * NUMFIELDS) + DENSITY] > 2) {
					shouldBeFiltered[i] = false; // .add(blob);
				} 
				else {
					shouldBeFiltered[i] = true;
				}

			}
		};

		kernel.execute(Range.create(blobObjects.size()));
		kernel.dispose();

		ArrayList<MovingBlob> filtered = new ArrayList<MovingBlob>();
		//ArrayList<MovingBlob> unfiltered = new ArrayList<MovingBlob>();
		// convert back 
		for (int i = 0; i < blobObjects.size(); i++) {
			if (shouldBeFiltered[i] == false) {
				MovingBlob blob = new MovingBlob();
				blob.velocityX = blobs[(i * NUMFIELDS) + VELOCITY_X];
				blob.velocityY = blobs[(i * NUMFIELDS) + VELOCITY_Y];
				blob.velocityChangeX = blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_X];
				blob.velocityChangeY = blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_Y];
				blob.predictedX = blobs[(i * NUMFIELDS) + PREDICTED_X];
				blob.predictedY = blobs[(i * NUMFIELDS) + PREDICTED_Y];
				blob.age = (int) blobs[(i * NUMFIELDS) + AGE];
				blob.ageOffScreen = (int) blobs[(i * NUMFIELDS) + AGE_OFF_SCREEN];
				blob.width = (int) blobs[(i * NUMFIELDS) + WIDTH];
				blob.height = (int) blobs[(i * NUMFIELDS) + HEIGHT];
				filtered.add(blob);
			}
		}

		return null;
	}

	
//////////////////////
		public List<MovingBlob> filterUnifiedBlobsTwo(List<MovingBlob> blobObjects) {
			float[] blobs = new float[blobObjects.size() * NUMFIELDS];
			boolean[] shouldBeFiltered = new boolean[blobObjects.size()];

			for (int i = 0; i < blobObjects.size(); i++) {
				MovingBlob blob = blobObjects.get(i);
				blobs[(i * NUMFIELDS) + VELOCITY_X] = blob.velocityX;
				blobs[(i * NUMFIELDS) + VELOCITY_Y] = blob.velocityY;
				blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_X] = blob.velocityChangeX;
				blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_Y] = blob.velocityChangeY;
				blobs[(i * NUMFIELDS) + PREDICTED_X] = blob.predictedX;
				blobs[(i * NUMFIELDS) + PREDICTED_Y] = blob.predictedY;
				blobs[(i * NUMFIELDS) + AGE] = blob.age;
				blobs[(i * NUMFIELDS) + AGE_OFF_SCREEN] = blob.ageOffScreen;
				blobs[(i * NUMFIELDS) + WIDTH] = blob.width;
				blobs[(i * NUMFIELDS) + HEIGHT] = blob.height;
				blobs[(i * NUMFIELDS) + GET_SCALED_VELOCITY_X] = blob.getScaledVelocityX();
				blobs[(i * NUMFIELDS) + GET_SCALED_VELOCITY_Y] = blob.getScaledVelocityY();
				blobs[(i * NUMFIELDS) + DENSITY] = blob.getDensity();
			}
			
			for (int i = 0; i < blobObjects.size(); i++) {
				MovingBlob blob = blobObjects.get(i);
				blobs[(i * NUMFIELDS) + VELOCITY_X] = blob.velocityX;
				blobs[(i * NUMFIELDS) + VELOCITY_Y] = blob.velocityY;
				blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_X] = blob.velocityChangeX;
				blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_Y] = blob.velocityChangeY;
				blobs[(i * NUMFIELDS) + PREDICTED_X] = blob.predictedX;
				blobs[(i * NUMFIELDS) + PREDICTED_Y] = blob.predictedY;
				blobs[(i * NUMFIELDS) + AGE] = blob.age;
				blobs[(i * NUMFIELDS) + AGE_OFF_SCREEN] = blob.ageOffScreen;
				blobs[(i * NUMFIELDS) + WIDTH] = blob.width;
				blobs[(i * NUMFIELDS) + HEIGHT] = blob.height;
			}

			Kernel kernel = new Kernel() {
				@Override
				public void run() {
					int i = getGlobalId();
					if ((float) blobs[(i * NUMFIELDS) + WIDTH] / (float) blobs[(i * NUMFIELDS) + HEIGHT] < com.apw.pedestrians.Constant.MAX_WIDTH_HEIGHT_RATIO
							&& blobs[(i * NUMFIELDS) + WIDTH] < com.apw.pedestrians.Constant.MAX_WIDTH && blobs[(i * NUMFIELDS) + HEIGHT] < com.apw.pedestrians.Constant.MAX_HEIGHT
							&& Math.abs(blobs[(i * NUMFIELDS) + GET_SCALED_VELOCITY_X]) > com.apw.pedestrians.Constant.MIN_SCALED_VELOCITY_X
							&& Math.abs(blobs[(i * NUMFIELDS) + GET_SCALED_VELOCITY_Y]) > com.apw.pedestrians.Constant.MIN_SCALED_VELOCITY_Y && blobs[(i * NUMFIELDS) + DENSITY] > 2) {
						shouldBeFiltered[i] = false; // .add(blob);
					} 
					else {
						shouldBeFiltered[i] = true;
					}

				}
			};

			kernel.execute(Range.create(blobObjects.size()));
			kernel.dispose();

			ArrayList<MovingBlob> filtered = new ArrayList<MovingBlob>();
			//ArrayList<MovingBlob> unfiltered = new ArrayList<MovingBlob>();
			// convert back 
			for (int i = 0; i < blobObjects.size(); i++) {
				if (shouldBeFiltered[i] == false) {
					MovingBlob blob = new MovingBlob();
					blob.velocityX = blobs[(i * NUMFIELDS) + VELOCITY_X];
					blob.velocityY = blobs[(i * NUMFIELDS) + VELOCITY_Y];
					blob.velocityChangeX = blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_X];
					blob.velocityChangeY = blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_Y];
					blob.predictedX = blobs[(i * NUMFIELDS) + PREDICTED_X];
					blob.predictedY = blobs[(i * NUMFIELDS) + PREDICTED_Y];
					blob.age = (int) blobs[(i * NUMFIELDS) + AGE];
					blob.ageOffScreen = (int) blobs[(i * NUMFIELDS) + AGE_OFF_SCREEN];
					blob.width = (int) blobs[(i * NUMFIELDS) + WIDTH];
					blob.height = (int) blobs[(i * NUMFIELDS) + HEIGHT];
					filtered.add(blob);
				}
			}

			return null;
		}
	

//////final part
		public List<MovingBlob> filterUnifiedBlobsThree(List<MovingBlob> blobObjects) {
			float[] blobs = new float[blobObjects.size() * NUMFIELDS];
			boolean[] shouldBeFiltered = new boolean[blobObjects.size()];

			for (int i = 0; i < blobObjects.size(); i++) {
				MovingBlob blob = blobObjects.get(i);
				blobs[(i * NUMFIELDS) + VELOCITY_X] = blob.velocityX;
				blobs[(i * NUMFIELDS) + VELOCITY_Y] = blob.velocityY;
				blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_X] = blob.velocityChangeX;
				blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_Y] = blob.velocityChangeY;
				blobs[(i * NUMFIELDS) + PREDICTED_X] = blob.predictedX;
				blobs[(i * NUMFIELDS) + PREDICTED_Y] = blob.predictedY;
				blobs[(i * NUMFIELDS) + AGE] = blob.age;
				blobs[(i * NUMFIELDS) + AGE_OFF_SCREEN] = blob.ageOffScreen;
				blobs[(i * NUMFIELDS) + WIDTH] = blob.width;
				blobs[(i * NUMFIELDS) + HEIGHT] = blob.height;
				blobs[(i * NUMFIELDS) + GET_SCALED_VELOCITY_X] = blob.getScaledVelocityX();
				blobs[(i * NUMFIELDS) + GET_SCALED_VELOCITY_Y] = blob.getScaledVelocityY();
				blobs[(i * NUMFIELDS) + DENSITY] = blob.getDensity();
			}
			
			for (int i = 0; i < blobObjects.size(); i++) {
				MovingBlob blob = blobObjects.get(i);
				blobs[(i * NUMFIELDS) + VELOCITY_X] = blob.velocityX;
				blobs[(i * NUMFIELDS) + VELOCITY_Y] = blob.velocityY;
				blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_X] = blob.velocityChangeX;
				blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_Y] = blob.velocityChangeY;
				blobs[(i * NUMFIELDS) + PREDICTED_X] = blob.predictedX;
				blobs[(i * NUMFIELDS) + PREDICTED_Y] = blob.predictedY;
				blobs[(i * NUMFIELDS) + AGE] = blob.age;
				blobs[(i * NUMFIELDS) + AGE_OFF_SCREEN] = blob.ageOffScreen;
				blobs[(i * NUMFIELDS) + WIDTH] = blob.width;
				blobs[(i * NUMFIELDS) + HEIGHT] = blob.height;
			}

			Kernel kernel = new Kernel() {
				@Override
				public void run() {
					int i = getGlobalId();
					if (blobs[(i * NUMFIELDS) + AGE] > 2 && blobs[(i * NUMFIELDS) + AGE_OFF_SCREEN] < 1) {
						shouldBeFiltered[i] = false; // .add(blob);
					} 
					else {
						shouldBeFiltered[i] = true;
					}

				}
			};

			kernel.execute(Range.create(blobObjects.size()));
			kernel.dispose();

			ArrayList<MovingBlob> filtered = new ArrayList<MovingBlob>();
			//ArrayList<MovingBlob> unfiltered = new ArrayList<MovingBlob>();
			// convert back 
			for (int i = 0; i < blobObjects.size(); i++) {
				if (shouldBeFiltered[i] == false) {
					MovingBlob blob = new MovingBlob();
					blob.velocityX = blobs[(i * NUMFIELDS) + VELOCITY_X];
					blob.velocityY = blobs[(i * NUMFIELDS) + VELOCITY_Y];
					blob.velocityChangeX = blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_X];
					blob.velocityChangeY = blobs[(i * NUMFIELDS) + VELOCITY_CHANGE_Y];
					blob.predictedX = blobs[(i * NUMFIELDS) + PREDICTED_X];
					blob.predictedY = blobs[(i * NUMFIELDS) + PREDICTED_Y];
					blob.age = (int) blobs[(i * NUMFIELDS) + AGE];
					blob.ageOffScreen = (int) blobs[(i * NUMFIELDS) + AGE_OFF_SCREEN];
					blob.width = (int) blobs[(i * NUMFIELDS) + WIDTH];
					blob.height = (int) blobs[(i * NUMFIELDS) + HEIGHT];
					filtered.add(blob);
				}
			}

			return null;
		}
		
	    public List<MovingBlob> filterFilteredUnifiedBlobs(List<MovingBlob> blobs) {
	        List<MovingBlob> ret = new LinkedList<>();
	        for (MovingBlob blob : blobs) {
	            if (blob.age > 2 && blob.ageOffScreen < 1) 
	            	ret.add(blob);
	        }
	        return ret;
	    }
}
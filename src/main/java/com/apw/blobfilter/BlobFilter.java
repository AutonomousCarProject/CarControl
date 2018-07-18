package com.apw.blobfilter;

import com.apw.blobtrack.MovingBlob;
import com.apw.oldglobal.Constant;

import java.util.LinkedList;
import java.util.List;

public class BlobFilter implements IMovingBlobReduction
{

	/**
	/*
	 * Moving Blob filters
	 */
	//Minimum age 
	private static final short AGE_MIN = 5;
	//Maximum X velocity
	private static final short MAX_VELOCITY_X = 30;
	//Maximum Y velocity
	private static final short MAX_VELOCITY_Y = 10;
	
	/*
	 * Unified Blob filters
	 */
	//Maximum width to height ratio
	private static final float MAX_WIDTH_HEIGHT_RATIO = 1;
	//Maximum width
	private static final int MAX_WIDTH = 100;
	//Maximum height
	private static final int MAX_HEIGHT = 100;
	//Maximum scaled x velocity
	private static final float MAX_SCALED_VELOCITY_X = 30;
	//Maximum scaled x velocity
	private static final float MAX_SCALED_VELOCITY_Y = 10;


	/**
	 * Checks the list of potential pedestrian blobs to distinguish pedestrians from non-pedestrians.
	 * Non-pedestrians are removed from the list of blobs.
	 * 
	 * @param blobs 	the list of potential pedestrian blobs
	 * @return			the list of blobs determined to be pedestrians
	 */

	public List<MovingBlob> filterMovingBlobs(List<MovingBlob> blobs){
		List<MovingBlob> ret = new LinkedList<>();
		for(MovingBlob blob : blobs){
			if(filterMovingBlob(blob)) ret.add(blob);
		}
		return ret;
	}

	//returns false if blob should be filtered
	private boolean filterMovingBlob(MovingBlob blob){
		return blob.age >= Constant.AGE_MIN &&
				Math.abs(blob.velocityY) < Constant.VELOCITY_Y_MAX &&
				Math.abs(blob.velocityX) < Constant.VELOCITY_X_MAX &&
				Math.abs(blob.velocityY) > Constant.VELOCITY_Y_MIN &&
				Math.abs(blob.velocityX) > Constant.VELOCITY_X_MIN &&
				blob.velocityChangeX < Constant.MAX_VELOCITY_CHANGE_X &&
				blob.velocityChangeY < Constant.MAX_VELOCITY_CHANGE_Y &&
				(float)blob.width/(float)blob.height < 1.3 && (float)blob.width*(float)blob.height > 40;
				
	}
	
	public List<MovingBlob> filterUnifiedBlobs(List<MovingBlob> blobs){
		List<MovingBlob> ret = new LinkedList<>();
		for(MovingBlob blob : blobs){
			if(filterUnifiedBlob(blob)) ret.add(blob);
		}
		return ret;
	}
	
	private boolean filterUnifiedBlob(MovingBlob blob){
		return (float)blob.width / (float)blob.height < Constant.MAX_WIDTH_HEIGHT_RATIO &&
				blob.width < Constant.MAX_WIDTH &&
				blob.height < Constant.MAX_HEIGHT &&
				Math.abs(blob.getScaledVelocityX()) > Constant.MIN_SCALED_VELOCITY_X &&
				Math.abs(blob.getScaledVelocityY()) > Constant.MIN_SCALED_VELOCITY_Y &&
				blob.getDensity() > 2;
	}
	
	public List<MovingBlob> filterFilteredUnifiedBlobs(List<MovingBlob> blobs){
		List<MovingBlob> ret = new LinkedList<>();
		for(MovingBlob blob : blobs){
			if(filterFilteredUnifiedBlob(blob)) ret.add(blob);
		}
		return ret;
	}
	
	private boolean filterFilteredUnifiedBlob(MovingBlob blob){
		return blob.age > 2 && blob.ageOffScreen < 1;
	}
	
}
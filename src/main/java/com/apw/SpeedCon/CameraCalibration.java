package com.apw.SpeedCon;

import com.apw.ImageManagement.ImageManager;
import com.apw.pedestrians.PedestrianDetector;
import com.apw.pedestrians.blobtrack.MovingBlob;
import com.apw.pedestrians.blobtrack.MovingBlobDetection;
import com.apw.pedestrians.image.Color;
import java.util.List;

public class CameraCalibration {

	private PedestrianDetector pedDetect;
	private ImageManager imageManager;

	//Camera information
	private double cameraFocalLength;
	private double signWidth;           //The width of a standared stop sign in mm

	private MovingBlob testBlob;
	private double testBlobWidthHeight; //the width and height of a square used to calibrate the camera
	private double testBlobDistance;    //The distance the test blob is away from the camera.
	private double relativeWorldScale;  //The scale of the world (if 1/3 scale, set to 3)
	
	public CameraCalibration(){

		relativeWorldScale = 8;
		signWidth = 750/relativeWorldScale;

		List<MovingBlob> blobs = this.pedDetect.getAllBlobs(imageManager.getSimpleColorRaster(), 912);
		for(MovingBlob i : blobs)
		{
			if(i.color.getColor() == Color.BLUE)
			{
				testBlob = i;
				break;
			}
		}

		findFocalLength(testBlob);

	}

	
	void findFocalLength(MovingBlob blob)
	{
		cameraFocalLength = (blob.width * testBlobDistance) /testBlobWidthHeight;
	}
	
	public double distanceToObj(double knownWidth, double focalLength, double objPixelWidth)
	{
		return (knownWidth *focalLength) / objPixelWidth;
	}
}

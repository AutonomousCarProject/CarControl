package com.apw.SpeedCon;

import com.apw.ImageManagement.ImageManager;
import com.apw.pedestrians.PedestrianDetector;
import com.apw.pedestrians.blobtrack.MovingBlob;
import com.apw.pedestrians.blobtrack.MovingBlobDetection;
import com.apw.pedestrians.image.Color;
import java.util.List;

public class CameraCalibration {

	/*

	Camera Calibration is used to find the focal length and then distance of an object.
	To use, you need to create a square the size of testBlobWidthHeight at the distance testBlobDistance, then begine calibration which takes a picure
	and measures the the square thus filling needed variables. After calibration, you can find the distance of an object
	if you know its real world size along with picture information.

	*/



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

		//Searches for a blue blob
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

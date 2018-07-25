package com.apw.SpeedCon;

import com.apw.ImageManagement.ImageManager;
import com.apw.drivedemo.DrDemo;
import com.apw.drivedemo.DriveTest;
import com.apw.fly2cam.FlyCamera;
import com.apw.pedestrians.PedestrianDetector;
import com.apw.pedestrians.blobtrack.MovingBlob;
import com.apw.pedestrians.blobtrack.MovingBlobDetection;
import com.apw.pedestrians.image.Color;
import com.pi4j.util.Console;

import java.util.List;

public class CameraCalibration {

	/**
	 * Camera Calibration is used to find the focal length and then distance of an object.
	 * To use, you need to create a square the size of testBlobWidthHeight at the distance testBlobDistance, then begine calibration which takes a picure
	 * and measures the the square thus filling needed variables. After calibration, you can find the distance of an object
	 * if you know its real world size along with picture information.
	 * 
	 * -----
	 * How to calibrate camera to find object distance
	 * 
	 * 1) Draw a blue square the size of testBlobWidth. This needs to be a preset size and defined in CameraCalibration.
	 * 
	 * 2) Move the square to the distance testBlobDistance away from the camera. This also needs to be defined in the code.
	 * 
	 * 3) Run the method calibrateCamera, which will get the square that you wrote.
	 * 
	 * 4) With the square, other methods are ran and should fill out enough data to test a stop sign.
	 * -----
	 * 
	 * After Calibration is complete
	 * 
	 * To find the distance, run distanceToObj with the known real world width of the object (make sure to scale), 
	 * focal length (which should be set by calibrateCamera), and object pixel width (the width of the blob you want to get distance to)
	 */



	private PedestrianDetector pedDetect;
	private ImageManager imageManager;

	//Camera information
	private double cameraFocalLength;
	private double signWidth;           //The width of a standared stop sign in mm

	private MovingBlob testBlob;
	private double testBlobWidthHeight = 2 ; //the width and height of a square used to calibrate the camera 
	private double testBlobDistance = 10;    //The distance the test blob is away from the camera 
	private double relativeWorldScale;  //The scale of the world (if 1/3 scale, set to 3)
	
	//Used to set world scale, and width of known objects
	public CameraCalibration(){

		relativeWorldScale = 8;
		signWidth = 75/relativeWorldScale; //sign width in centi meters

		this.pedDetect = new PedestrianDetector();
		this.imageManager = DriveTest.imageManager;

	}

	
	//Finds focal length which can then be used for distance, read above for detail
	public void calibrateCamera()
	{
		//Searches for a blue blob
		List<MovingBlob> blobs = this.pedDetect.getAllBlobs(imageManager.getSimpleColorRaster(), 912);
		for(MovingBlob i : blobs)
		{
			if(i.color.getColor() == Color.BLUE)
			{
				testBlob = i;
				findFocalLength(testBlob);
				break;
			}
		}

		//Used to test distance to found test blob, should be same as testBlobDistance
		distanceToObj(testBlobWidthHeight, cameraFocalLength, testBlob.width);
	}


	//Formula that calculates focal length of the test blob
	void findFocalLength(MovingBlob blob)
	{
		cameraFocalLength = (blob.width * testBlobDistance) / testBlobWidthHeight;
		System.out.print("Focal Length = " + cameraFocalLength);
	}
	 

	//Calculates the distance to a blob if the real world size is known
	public double distanceToObj(double knownWidth, double focalLength, double objPixelWidth)
	{
		System.out.print("Distance to object = " + (knownWidth * focalLength) / objPixelWidth);
		return (knownWidth * focalLength) / objPixelWidth;
	}


	//Break Rate Math

	//The total distance it will take to stop
	double calcStopDist(double targetStopDist, double speed) {
		return Math.pow(speed, 2) / (Constants.FRICT * Constants.GRAV * 2);
	}

	//The amount of time that is needed to stop at the given speed.
	double getStopTime(double dist, double speed) {
		return dist / speed;
	}

	//The rate at which the speed must go down by, linear
	protected double calcStopRate(double speed, double time) {
		return (0 - speed) / time;
	}


	//Function used to get the rate to lower the speed by when a stop distance is given.
	double getStopRate(double targetDist, double currentSpeed) {
		return calcStopRate(currentSpeed, getStopTime(targetDist, currentSpeed));
	}

	// End Of Brake Rate Math

	
}

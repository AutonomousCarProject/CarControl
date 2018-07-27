package com.apw.SpeedCon;

import com.apw.ImageManagement.ImageManager;
import com.apw.drivedemo.DrDemo;
import com.apw.drivedemo.DriveTest;
import com.apw.fly2cam.FlyCamera;
import com.apw.pedestrians.PedestrianDetector;
import com.apw.pedestrians.blobtrack.MovingBlob;
import com.apw.pedestrians.blobtrack.MovingBlobDetection;
import com.apw.pedestrians.image.Color;

import java.util.List;

public class CameraCalibration {

	/**
	 * Camera Calibration is used to find the focal length and subsequently the distance of an object.
	 * In order to use it, you need to create a square the size of testBlobWidthHeight at the distance testBlobDistance,
	 * then begin calibration, taking a picture, measuring the square, and thus filling the requisite variables.
	 * After calibration you can find the distance to an object if you know its absolute size along with image information.
	 * 
	 * <p>How to calibrate the camera to find an object's distance:
	 * 1) Draw a blue square with the size of testBlobWidth. testBlobWidth is a constant defined in CameraCalibration.
	 * 2) Move the square to the distance from the camera specified by testBlobDistance. testBlobDistance is a constant defined in CameraCalibration.
	 * 3) Run the method calibrateCamera, getting information about the given square from the camera
	 * -- After Calibration is Complete --
	 * 4) After calibration, to find the distance run the method distanceToObj, imputing the absolute width of the object, focal length (given by calibrateCamera), and object pixel width
	 * 
	 * 4)To find the distance, run distanceToObj with the known real world width of the object (make sure to scale), 
	 * focal length (which should be set by calibrateCamera), and object pixel width (the width of the blob you want to get distance to)
	 * 
	 * @author Derek Schwartz (Wrote the code)
	 * @author Matthew Alexander (Added documentation)
	 * @see [another relevant class or method that you think people should look at]
	 * @see etc.
	 *
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

	/*
	 * 
	 * Documentation from Derek's Speed API:
	 * 
	 * Gives the needed distance to stop at given location at given speed. Can be used to do something when the car can't stop to avoid hitting what ever is identified.
	 * 
	 * Unit: Meters
	 * 
	 */
	
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

	/*
	 * 
	 * Documentation from Derek's Speed API:
	 * 
	 * Gives the rate the car needs to decelerate given a distance from stop point and the current speed. Gives in linear manner.
	 * 
	 * Unit: Meter/Second
	 * 
	 */
	
	//Function used to get the rate to lower the speed by when a stop distance is given.
	double getStopRate(double targetDist, double currentSpeed) {
		return calcStopRate(currentSpeed, getStopTime(targetDist, currentSpeed));
	}

	// End Of Brake Rate Math

	
}

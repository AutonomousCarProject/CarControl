package com.apw.speedcon;

import com.apw.carcontrol.CamControl;
import com.apw.carcontrol.CarControl;
import com.apw.pedestrians.PedestrianDetector;
import com.apw.pedestrians.blobtrack.MovingBlob;
import com.apw.pedestrians.image.Color;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


import java.util.List;

public class CameraCalibration {

	/**
	 * Camera Calibration is used to find the focal length and then distance of an object.
	 * To use, you need to create a square the size of testBlobWidthHeight at the distance testBlobDistance, then begine calibration which takes a picure
	 * and measures the the square thus filling needed variables. After calibration, you can find the tance of an object
	 * if you know its real world size along with picture information.
	 * <p>
	 * -----
	 * How to calibrate camera to find object distance
	 * <p>
	 * 1) Draw a blue square the size of testBlobWidth. This needs to be a preset size and defined in CameraCalibration.
	 * <p>
	 * 2) Move the square to the distance testBlobDistance away from the camera. This also needs to be defined in the code.
	 * <p>
	 * 3) Run the method calibrateCamera, which will get the square that you wrote.
	 * <p>
	 * 4) With the square, other methods are ran and should fill out enough data to test a stop sign.
	 * -----
	 * <p>
	 * After Calibration is complete
	 * <p>
	 * To find the distance, run distanceToObj with the known real world width of the object (make sure to scale),
	 * focal length (which should be set by calibrateCamera), and object pixel width (the width of the blob you want to get distance to)
	 */



	//Camera information
	private double cameraFocalLength;	//If used in sim, leave at 35, if testing IRL leave blank and use calibrateCamera

	private MovingBlob testBlob;
	private double testBlobWidthHeight;	//the width and height of a square used to calibrate the camera
	public double testBlobDistance;	//The distance the test blob is away from the camera
	public double relativeWorldScale;		//The scale of the world (if 1/3 scale, set to 3)
	private FileWriter fileWriter;
	private FileReader fileReader;

	//Used to set world scale, and width of known objects
	public CameraCalibration() {
		
		//cameraFocalLength = 35;
		relativeWorldScale = 8;

		testBlobWidthHeight = 9; //Set this to the width of the blob you will be testing for calibration
		testBlobDistance = 21;    //Set this to the distance the blob is away from the camera lens

		
		//Tries to find a file containing the focal length
		try{

		fileReader = new FileReader("calibrationData.txt");
		BufferedReader reader = new BufferedReader(fileReader);

		String line;
		if ((line = reader.readLine()) != null) {
			System.out.println("Found focal length " + line);
			cameraFocalLength = Double.parseDouble(line);
			System.out.println("Set Focal Length To " + cameraFocalLength);
		}
		reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		

	}

	
	//Finds focal length which can then be used for distance, read above for detail
	public void calibrateCamera(CarControl control, List<MovingBlob> currentBlobs) {
		//Searches for a blue blob
		int pixelWidth = 0;
		
		for (MovingBlob i : currentBlobs) {
			if (i.color.getColor() == Color.RED) {
			pixelWidth = Math.max(pixelWidth, i.width);
			}
		}
		
		if (control instanceof CamControl) {
			testBlobWidthHeight = 9; //Set this to the width of the blob you will be testing for calibration
			testBlobDistance = 18;    //Set this to the distance the blob is away from the camera lens
		}
		else {
			testBlobDistance = Math.sqrt(Math.pow(Math.abs(control.getPosition(true) - (2 * 29.5)), 2) + Math.pow(Math.abs(control.getPosition(false) - (2 * 30)), 2));
			testBlobWidthHeight = ((double) 29 / (double) 44); //Find this in the txt file, image index
		}
		
		findFocalLength(pixelWidth, control);
		
		System.out.println(distanceToObj(testBlobWidthHeight, pixelWidth, control));
		
		//Saves the camera calibration data, only needed once or when changing cameras
		try{
			fileWriter = new FileWriter("calibrationData.txt");
			fileWriter.write(Double.toString(cameraFocalLength));
			fileWriter.close();
		}catch(IOException e){
			e.printStackTrace();
		}

	}
	

	//Formula that calculates focal length of the test blob
	void findFocalLength(int pixelWidth, CarControl control) {
		if (control instanceof CamControl) {
			System.out.println("Calibration blob distance: " + testBlobDistance);
			System.out.println("Calibration blob pixel: " + pixelWidth);
			System.out.println("Cam sensor width: " + Constants.SENSOR_CAM_WIDTH);
			System.out.println("Calibration blob width: " + testBlobWidthHeight); // (dist * obj height pix * sensor height mm) / real height mm * image height pixels 
			System.out.println("Screen width: " + Constants.SCREEN_FILTERED_WIDTH);
			System.out.println("Focal Length = " + ((testBlobDistance * pixelWidth * Constants.SENSOR_CAM_WIDTH) / (testBlobWidthHeight * control.getImageWidth())));
			cameraFocalLength = ((testBlobDistance * pixelWidth * Constants.SENSOR_CAM_WIDTH) / (testBlobWidthHeight * control.getImageWidth())); //(pixelWidth * testBlobDistance) / testBlobWidthHeight;
		}
		else {
			System.out.println("Calibration blob distance: " + testBlobDistance);
			System.out.println("Calibration blob pixel: " + pixelWidth);
			System.out.println("Trak sensor width: " + Constants.SENSOR_TRAK_WIDTH);
			System.out.println("Calibration blob width: " + testBlobWidthHeight); // (dist * obj height pix * sensor height mm) / real height mm * image height pixels 
			System.out.println("Screen width: " + Constants.SCREEN_FILTERED_WIDTH);
			System.out.println("Focal Length = " + ((testBlobDistance * pixelWidth * Constants.SENSOR_TRAK_WIDTH) / (testBlobWidthHeight * Constants.SCREEN_FILTERED_WIDTH)));
			cameraFocalLength = ((testBlobDistance * pixelWidth * Constants.SENSOR_TRAK_WIDTH) / (testBlobWidthHeight * Constants.SCREEN_FILTERED_WIDTH));
		}
	}


	//Calculates the distance to a blob if the real world size is known
	public double distanceToObj(double knownWidth, double objPixelWidth, CarControl control) {
		if (control instanceof CamControl) {
			System.out.println("Distance to object = " + (cameraFocalLength * knownWidth * control.getImageWidth()) / (objPixelWidth * Constants.SENSOR_CAM_WIDTH));
			return (cameraFocalLength * knownWidth * control.getImageWidth()) / (objPixelWidth * Constants.SENSOR_CAM_WIDTH);	
		}
		else {
			System.out.println("Distance to object = " + (cameraFocalLength * knownWidth * Constants.SCREEN_FILTERED_WIDTH) / (objPixelWidth * Constants.SENSOR_TRAK_WIDTH));
			return (cameraFocalLength * knownWidth * Constants.SCREEN_FILTERED_WIDTH) / (objPixelWidth * Constants.SENSOR_TRAK_WIDTH);
		}
	}

	//Calculates the distance to a blob if the real world size is known, finds more accurate dist with height
	public double distanceToObj(double knownWidth, double objPixelWidth, double objectHeight, CarControl control) {
		//System.out.println("Distance to object = " + (knownWidth * cameraFocalLength) / objPixelWidth);
		System.out.println("special stop");
		System.out.println("Known width = " + knownWidth);
		double hyp =  distanceToObj(knownWidth, objPixelWidth, control);
		System.out.println("Hyp = " + hyp);
		double a = Math.pow(hyp, 2) - Math.pow(objectHeight, 2);
		System.out.println("special = " + Math.sqrt(a));
		return Math.sqrt(a);
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
		return speed / time;
	}


	//Function used to get the rate to lower the speed by when a stop distance is given.
	double getStopRate(double targetDist, double currentSpeed) {
		return calcStopRate(currentSpeed, getStopTime(targetDist, currentSpeed));
	}

	// End Of Brake Rate Math
}

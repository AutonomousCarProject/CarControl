package com.apw.SpeedCon;

import java.awt.Graphics;
import java.util.*;
import com.apw.pedestrians.blobtrack.*;
import com.apw.pedestrians.image.Color;
import com.apw.pedestrians.*;
import com.apw.ImageManagement.*;
import com.apw.apw3.TrakSim;
import com.apw.drivedemo.DriveTest;

public class SpeedController {

	private double currentEstimatedSpeed;
	private double desiredSpeed;
	private boolean stoppingAtSign;
	private boolean stoppedAtSign;
	private boolean stoppingAtLight;
	private boolean stoppedAtLight;
	private boolean readyToGo;
	private boolean emergencyStop;
	int color;
	private int cyclesToStopAtSign = Constants.DRIFT_TO_STOPSIGN_FRAMES;
	private int cyclesToGo;
	private int cyclesToStopAtLight = Constants.DRIFT_TO_STOPLIGHT_FRAMES;
	private int cyclesUntilCanDetectStopsign = Constants.WAIT_AFTER_STOPSIGN;

	private PedestrianDetector pedDetect;
	private CameraCalibration cameraCalibrator;

	private List<MovingBlob> currentBlobs;

	TrakSim trackSim = new TrakSim();

	public SpeedController(){
		this.pedDetect = new PedestrianDetector();
		this.currentBlobs = new ArrayList<MovingBlob>();
		this.cameraCalibrator = new CameraCalibration();
	}

	//A method to be called every frame. Calculates desired speed and actual speed
	//Also takes stopping into account
	public void onUpdate(int gasAmount, int steerDegs, int manualSpeed, Graphics graf, DriveTest dtest){
		if (cyclesUntilCanDetectStopsign > 0){
			cyclesUntilCanDetectStopsign--;
		}
		com.apw.pedestrians.Constant.LAST_FRAME_MILLIS = com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS;
		com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS = System.currentTimeMillis();
		com.apw.pedestrians.Constant.TIME_DIFFERENCE = com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS - com.apw.pedestrians.Constant.LAST_FRAME_MILLIS;
		this.calculateEstimatedSpeed(gasAmount);
		this.calculateDesiredSpeed(steerDegs, manualSpeed);

		//This part runs on-screen blobs thru a set of tests to figure out if they are
		//relevant, and then what to do with them
		ImageManager imageManager = DriveTest.imageManager;

		List<MovingBlob> blobs = this.pedDetect.getAllBlobs(imageManager.getSimpleColorRaster(), 912);

		for(MovingBlob i : blobs){
			/* Returns an int value corresponding to the color of the light we are looking at
			 * 0 - No light
			 * 1 - Red Light
			 * 2 - Yellow Light
			 * 3 - Green Light
			 * */
			int currLight = detectLight(i, blobs);

			if(currLight == 1){
				setStoppingAtLight();
			}
			else if (currLight == 2) {
			}
			else if (currLight == 3) {
				readyToGo();
			}
			else if(detectStopSign(i) && cyclesUntilCanDetectStopsign <= 0){
				cyclesUntilCanDetectStopsign = 100;
				setStoppingAtSign();
			}
			else{}
		}

		this.currentBlobs = blobs;
		
		if(emergencyStop == true)
		{
			emergenyStop();
		}
	}

	private boolean detectBlobOverlappingBlob(MovingBlob outsideBlob, MovingBlob insideBlob){
		if((insideBlob.x < outsideBlob.x+outsideBlob.width && insideBlob.width + insideBlob.x > outsideBlob.x)  ||  (insideBlob.y < outsideBlob.y+outsideBlob.height && insideBlob.height + insideBlob.y > outsideBlob.y)) {
			return true;
		}
		else {
			return false;
		}
	}

	//This figures out the speed that we want to be traveling at
	public void calculateDesiredSpeed(double wheelAngle, int manualSpeed){
		double curveSteepness = 0; // Steering.getCurveSteepness();
		int shouldStopSign = this.updateStopSign();
		int shouldStopLight = this.updateStopLight();

		//Logic for determining if we need to be slowing down due to a roadsign/light, and why
		if(shouldStopSign == 1 && shouldStopLight == 1){
			this.desiredSpeed = Math.min(Math.max((1 - Math.abs((double)(wheelAngle)/90.0))*Constants.MAX_SPEED + manualSpeed, Constants.MIN_SPEED), Constants.MAX_SPEED);
		}
		else if (shouldStopSign == -1){
			this.desiredSpeed = Constants.STOPSIGN_DRIFT_SPEED;
		}
		else if (shouldStopSign == 0){
			this.desiredSpeed = 0;
		}
		else if (shouldStopLight == -1){
			this.desiredSpeed = Constants.STOPLIGHT_DRIFT_SPEED;
		}
		else if (shouldStopLight == 0){
			this.desiredSpeed = 0;
		}
		if (this.emergencyStop){
			this.desiredSpeed = 0;
		}
	}

	public int getNextSpeed(){
		double distance = this.desiredSpeed - this.currentEstimatedSpeed;
		if(Math.abs(distance) < Constants.MIN_SPEED_INCREMENT){
			return (int)this.desiredSpeed;
		}
		else if (distance < 0){
			return (int)(this.currentEstimatedSpeed - Constants.MIN_SPEED_INCREMENT);
		}
		else{
			return (int)(this.currentEstimatedSpeed + Constants.MIN_SPEED_INCREMENT);
		}
	}

	//Returns the estimated speed IN METERS PER SECOND
	public double getEstimatedSpeed(){
		return currentEstimatedSpeed*Constants.PIN_TO_METER_PER_SECOND;
	}

	//Updates the estimated speed
	public void calculateEstimatedSpeed(int gasAmount){
		currentEstimatedSpeed = gasAmount;
	}

	//To be called every frame. Checks if we need to be stopping at a stopsign
	//By modifying constants in the Constants.java in SpeedCon, you can adjust how the stopping behaves
	//Can be triggered by pressing 'P'
	public int updateStopSign(){
		if(stoppingAtSign){
			if(cyclesToStopAtSign <= 0){
				cyclesToStopAtSign = Constants.DRIFT_TO_STOPSIGN_FRAMES;
				stoppedAtSign = true;
				stoppingAtSign = false;
				cyclesToGo = Constants.WAIT_AT_STOPSIGN_FRAMES;
			}
			else{
				cyclesToStopAtSign--;
				return -1;
			}
		}
		if(stoppedAtSign){
			if(cyclesToGo <= 0){
				cyclesToGo = Constants.WAIT_AT_STOPSIGN_FRAMES;
				stoppedAtSign = false;
			}
			else{
				cyclesToGo--;
				return 0;
			}
		}
		return 1;
	}

	//To be called every frame. Checks if we need to be stopping at a stoplight
	//By modifying constants in the Constants.java in SpeedCon, you can adjust how the stopping behaves
	//Can be triggered by pressing 'O', and released by pressing 'I'
	public int updateStopLight(){
		if(stoppingAtLight){
			if(cyclesToStopAtLight <= 0){
				cyclesToStopAtLight = Constants.DRIFT_TO_STOPLIGHT_FRAMES;
				stoppedAtLight = true;
				stoppingAtLight = false;
				readyToGo = false;
			}
			else{
				cyclesToStopAtLight--;
				return -1;
			}
		}
		if(stoppedAtLight){
			if(readyToGo){
				stoppedAtLight = false;
				readyToGo = false;
			}
			else{
				return 0;
			}
		}
		return 1;
	}

	//Triggered by pressing 'O', this tells us that we have a green light
	public void readyToGo(){
		readyToGo = true;
	}

	//Tells you if we are stopping at a sign currently
	public boolean getStoppingAtSign(){
		return stoppingAtSign;
	}

	//Tells you if we are stopping at a light currently
	public boolean getStoppingAtLight(){
		return stoppingAtLight;
	}

	//Tells us that we have detected a stopsign, and need to stop
	public void setStoppingAtSign(){
		stoppingAtSign = true;
		cyclesToStopAtSign = Constants.DRIFT_TO_STOPSIGN_FRAMES;
	}

	//Tells us that we have seen a red light, and need to stop
	public void setStoppingAtLight(){
		stoppingAtLight = true;
		cyclesToStopAtLight = Constants.DRIFT_TO_STOPLIGHT_FRAMES;
	}

	//Getting and setting our emergency stop boolean
	public boolean getEmergencyStop(){
		return emergencyStop;
	}

	public void setEmergencyStop(boolean emer){
		this.emergencyStop = emer;
	}





	public int getDesiredSpeed() {
		return (int)desiredSpeed;
	}

	// Checks a given blob for the properties of a stopsign (size, age, position, color)
	public boolean detectStopSign(MovingBlob blob) {
		if(blob.age > Constants.BLOB_AGE && blob.height > Constants.BLOB_HEIGHT && blob.width > Constants.BLOB_WIDTH && blob.x > Constants.STOPSIGN_MIN_X && blob.x < Constants.STOPSIGN_MAX_X && blob.y > Constants.STOPSIGN_MIN_Y && blob.y < Constants.STOPSIGN_MAX_Y && blob.color.getColor() == Color.RED) {
			return true;
		} else {
			return false;
		}
	}

	public List<MovingBlob> getBlobs() {
		return this.currentBlobs;
	}

	/* Returns an int value corresponding to the color of the light we are looking at
	 * 0 - No light
	 * 1 - Red Light
	 * 2 - Yellow Light
	 * 3 - Green Light
	 * */

	public int detectLight(MovingBlob blob, List<MovingBlob> bloblist){
		int lightColor = 0;
		boolean outputLight = false;
		//Figure out the color of our blob
		if (blob.age > Constants.BLOB_AGE && blob.height > Constants.BLOB_HEIGHT && blob.width > Constants.BLOB_WIDTH && blob.x > Constants.STOPLIGHT_MIN_X && blob.x < Constants.STOPLIGHT_MAX_X && blob.y > Constants.STOPLIGHT_MIN_Y && blob.y < Constants.STOPLIGHT_MAX_Y && blob.color.getColor() == Color.RED) {
			//Found a red light
			lightColor = 1;
		}
		else if (blob.age > Constants.BLOB_AGE && blob.height > Constants.BLOB_HEIGHT && blob.width > Constants.BLOB_WIDTH && blob.x > Constants.STOPLIGHT_MIN_X && blob.x < Constants.STOPLIGHT_MAX_X && blob.y > Constants.STOPLIGHT_MIN_Y && blob.y < Constants.STOPLIGHT_MAX_Y && blob.color.getColor() == Color.RED) {
			//Found a yellow light
			lightColor = 2;
		}
		else if (blob.age > Constants.BLOB_AGE && blob.height > Constants.BLOB_HEIGHT && blob.width > Constants.BLOB_WIDTH && blob.x > Constants.STOPLIGHT_MIN_X && blob.x < Constants.STOPLIGHT_MAX_X && blob.y > Constants.STOPLIGHT_MIN_Y && blob.y < Constants.STOPLIGHT_MAX_Y && blob.color.getColor() == Color.GREEN) {
			//Found a green light
			lightColor = 3;
		}
		else {
			//Didn't find a light
			return 0;
		}
		//If we made it here, we know that we have a light
		//Therefore, we need to check if that light is inside of a black blob, aka the lamp
		System.out.println("Found a light: " + lightColor);
		outputLight = true;
		int overlaps = 0;
		for(MovingBlob b : bloblist){
			if(blob.color.getColor() == Color.BLACK){
				if(detectBlobOverlappingBlob(blob, b)){
					overlaps++;
				}
			}
		}
		System.out.println("Overlaps: " + overlaps);
		if(outputLight) {
			return lightColor;
		}
		else {
			return 0;
		}
	}

	public CameraCalibration getCalibrator()
	{
		return cameraCalibrator;
	}

	public void emergenyStop()
	{
		cameraCalibrator.calcStopRate(getEstimatedSpeed(), 0.1);
	}
}

package com.apw.SpeedCon;

import java.awt.Graphics;
import java.util.*;
import com.apw.pedestrians.blobtrack.*;
import com.apw.pedestrians.image.Color;
import com.apw.pedestrians.*;
import com.apw.ImageManagement.*;
import com.apw.apw3.TrakSim;
import com.apw.drivedemo.DriveTest;

/**
 * This class handles the speed of our car, given info from image and steering.
 * 
 * @author William Adriance
 * @author Matthew Alexander
 * @author Derek Schwartz
 * @author Brett Zonick 
 * 
 * @see drivedemo/DrDemo.java
 * @see SpeedCon/Constants.java
 */

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
	
	private PedestrianDetector pedDetect;
	private CameraCalibration cameraCalibrator;

	private List<MovingBlob> currentBlobs;

	TrakSim trackSim = new TrakSim();

	
	/**
	 * A basic constructor for our SpeedController.
	 */
	public SpeedController(){
		this.pedDetect = new PedestrianDetector();
		this.currentBlobs = new ArrayList<MovingBlob>();
		this.cameraCalibrator = new CameraCalibration();
	}

	/**
	 * This is a method that is called on every frame update by DrDemo. It is responsible for calculating
	 * our speed at any given time, and setting it. It also calls methods to figure out if we need to be
	 * stopping due to a street obstacle, and it displays blob overlays on the screen.
	 * 
	 * @param gasAmount the pin position of your servos, e.g. how much gas we are giving the engine
	 * @param steerDegs the degree measure at which our wheels are facing
	 * @param manualSpeed an int that can be incremented by pressing the up and down arrow keys, and gets added on top of our desired speed
	 * @param graf a Graphics object fed to us so that we can display blob boxes and overlay lines
	 * @param dtest the DriveTest object that we feed to our blob detection, which gets displayed in a separate window
	 */
	public void onUpdate(int gasAmount, int steerDegs, int manualSpeed, Graphics graf, DriveTest dtest) {
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
				setStoppingAtLight();
			}
			else if (currLight == 3) {
				readyToGo();
			}
			else if (detectStopSign(i)) {
				System.out.println("Found a stopsign: " + i);
				setStoppingAtSign();
			}
		}

		this.currentBlobs = blobs;

		if(emergencyStop == true)
		{
			emergencyStop();
		}
	}
	
	/**
	 * This method detects if a given blob is overlapping another blob.
	 * 
	 * <p>This is useful for determining if an array of objects contains a stoplight,
	 * as a stoplight will contain overlapping black and red blobs.
	 * 
	 * @param b1 One of the two blobs that we are fed
	 * @param b2 One of the two blobs that we are fed
	 * @return a boolean that is true if the blobs are overlapping, and false if they are not
	 */
	public boolean detectBlobOverlappingBlob(MovingBlob b1, MovingBlob b2) {
		if((b2.x < b1.x+b1.width &&
			b2.width + b2.x > b1.x) ||
			(b2.y < b1.y+b1.height &&
			b2.height + b2.y > b1.y)) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * A method that determines what speed we need to be traveling at given our wheel angle, and how we have
	 * modified our speed by pressing the arrow keys.
	 * 
	 * <p>Also checks whether it has been told to stop at a stopsign or stoplight, and acts accordingly,
	 * slowing if it needs to slow, and stopping when it needs t stop.
	 * 
	 * @param wheelAngle our current wheel angle
	 * @param manualSpeed our modifier for speed based upon arrow key presses
	 */
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
	}

	/**
	 * Changes our speed from our current (estimated) speed to our desired speed in a smooth fashion.
	 * 
	 * @return the speed that we should currently be traveling at
	 */
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

	/**
	 * A method called every frame by onUpdate(). Checks if we need to be stopping at a stopsign.
	 * 
	 * <p> By modifying constants in SpeedCon/Constants.java, you can adjust how the stopping behaves.
	 * 
	 * <p>Can be triggered by pressing 'P'
	 * 
	 * @return a stopcode, which reads 1 if we are clear to keep driving, -1 if we are slowing down, and 0 if we need to be stopped
	 */
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

	/**
	 * A method called every frame by onUpdate(). Checks if we need to be stopping at a stoplight.
	 * 
	 * <p>By modifying constants in SpeedCon/Constants.java, you can adjust how the stopping behaves.
	 * 
	 * <p>Can be triggered by pressing 'O', and released by pressing 'I'
	 * 
	 * @return a stopcode, which reads 1 if we are clear to keep driving, -1 if we are slowing down, and 0 if we need to be stopped
	 */
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

	/**
	 * Checks a given blob for the properties of a stopsign (size, age, position, color)
	 * 
	 * <p>These properties are stored in the blob, and you probably will not need to worry about setting them.
	 * 
	 * @param blob the blob that we want to check
	 * @return true if the blob is recognized to be a stopsign, otherwise false
	 */
	public boolean detectStopSign(MovingBlob blob) {
		if(blob.age > Constants.BLOB_AGE && 
			blob.height > (3) * Constants.BLOB_MIN_HEIGHT && 
			blob.height < Constants.BLOB_MAX_HEIGHT && 
			blob.width > (3) * Constants.BLOB_MIN_WIDTH && 
			blob.width < Constants.BLOB_MAX_WIDTH && 
			blob.x > Constants.STOPSIGN_MIN_X && 
			blob.x < Constants.STOPSIGN_MAX_X && 
			blob.y > Constants.STOPSIGN_MIN_Y && 
			blob.y < Constants.STOPSIGN_MAX_Y && 
			blob.color.getColor() == Color.RED && 
			!blob.seen && 
			(((double) blob.height / (double) blob.width) < 1 + Constants.BLOB_RATIO_DIF && 
			((double) blob.height / (double) blob.width) > 1 - Constants.BLOB_RATIO_DIF)) {
			
			blob.seen = true;
			return true;
		} 
		return false;
	}

	//Returns all of the blobs on screen
	public List<MovingBlob> getBlobs() {
		return this.currentBlobs;
	}

	/** Returns an int value corresponding to the color of the light we are looking at
	 * <p>0 - No light
	 * <p>1 - Red Light
	 * <p>2 - Yellow Light
	 * <p>3 - Green Light
	 * 
	 * <p>It is crucial that this method be given a list of blobs to iterate through, as it recognizes
	 * a configuration of blobs to be a light by looking for black around and overlapping a color, rather than just one blob
	 * 
	 * @param blob our guess for a light, blob color is ALWAYS red, green, or yellow
	 * @param bloblist all of the blobs on screen
	 * @return returns a light code, see above for light codes
	 */
	public int detectLight(MovingBlob blob, List<MovingBlob> bloblist){
		int lightColor = 0;
		
		//Here we try to see if the blob is a red light
		if (blob.age >= Constants.BLOB_AGE && 
			blob.height >= (1) + Constants.BLOB_MIN_HEIGHT && 
			blob.height <= Constants.BLOB_MAX_HEIGHT && 
			blob.width >= Constants.BLOB_MIN_WIDTH && 
			blob.width <= Constants.BLOB_MAX_WIDTH && 
			blob.x >= Constants.STOPLIGHT_MIN_X && 
			blob.x <= Constants.STOPLIGHT_MAX_X && 
			blob.y >= Constants.STOPLIGHT_MIN_Y && 
			blob.y <= Constants.STOPLIGHT_MAX_Y && 
			blob.color.getColor() == Color.RED && 
			!blob.seen && 
			((double) blob.height / (double) blob.width) < 1 + Constants.BLOB_RATIO_DIF && 
			((double) blob.height / (double) blob.width) > 1 - Constants.BLOB_RATIO_DIF) {
			
			for(MovingBlob b : bloblist){
				if(b.color.getColor() == Color.BLACK){
					if(detectBlobOverlappingBlob(b, blob)){
						//Found a red light
						System.out.println("Found a redlight: " + blob);
						blob.seen = true;
						lightColor = 1;
					}
				}
			}
		}
		
		//Here we check if it is a yellow light
		else if (
			blob.age > Constants.BLOB_AGE && 
			blob.height > Constants.BLOB_MIN_HEIGHT && 
			blob.height < (1/2) * Constants.BLOB_MAX_HEIGHT && 
			blob.width > Constants.BLOB_MIN_WIDTH && 
			blob.width <  (1/2) * Constants.BLOB_MAX_WIDTH && 
			blob.x > Constants.STOPLIGHT_MIN_X && 
			blob.x < Constants.STOPLIGHT_MAX_X && 
			blob.y > Constants.STOPLIGHT_MIN_Y && 
			blob.y < Constants.STOPLIGHT_MAX_Y && 
			blob.color.getColor() == Color.YELLOW && 
			!blob.seen && 
			((double) blob.height / (double) blob.width) < 1 + Constants.BLOB_RATIO_DIF && 
			((double) blob.height / (double) blob.width) > 1 - Constants.BLOB_RATIO_DIF) {
			
			for(MovingBlob b : bloblist){
				if(b.color.getColor() == Color.BLACK){
					if(detectBlobOverlappingBlob(b, blob)){
						//Found a yellow light
						System.out.println("Found a yellowlight: " + blob);
						blob.seen = true;
						lightColor = 2;
					}
				}
			}
		}
		
		//And here we try to see if it is a green light
		else if (blob.age > Constants.BLOB_AGE && 
			blob.height > Constants.BLOB_MIN_HEIGHT && 
			blob.height < Constants.BLOB_MAX_HEIGHT && 
			blob.width > Constants.BLOB_MIN_WIDTH && 
			blob.width < Constants.BLOB_MAX_WIDTH && 
			blob.x > Constants.STOPLIGHT_MIN_X && 
			blob.x < Constants.STOPLIGHT_MAX_X && 
			blob.y > Constants.STOPLIGHT_MIN_Y && 
			blob.y < Constants.STOPLIGHT_MAX_Y && 
			blob.color.getColor() == Color.GREEN && 
			!blob.seen && 
			((double) blob.height / (double) blob.width) < 1 + Constants.BLOB_RATIO_DIF && 
			((double) blob.height / (double) blob.width) > 1 - Constants.BLOB_RATIO_DIF) {
			
			for(MovingBlob b : bloblist){
				if(b.color.getColor() == Color.BLACK){
					if(detectBlobOverlappingBlob(b, blob)){
						//Found a green light
						System.out.println("Found a greenlight: " + blob);
						blob.seen = true;
						lightColor = 3;
					}
				}
			}
		}
		//Otherwise, it's either an invalid color, or doesn't fit the standards for a light and is just background noise
		else {
			return 0;
		}
		return lightColor;
	}

	public CameraCalibration getCalibrator()
	{
		return cameraCalibrator;
	}

	public void emergencyStop()
	{
		this.desiredSpeed = cameraCalibrator.calcStopRate(getEstimatedSpeed(), 0.1);
	}
}

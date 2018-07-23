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
	private int color;
	private int cyclesToStopAtSign = Constants.DRIFT_TO_STOPSIGN_FRAMES;
	private int cyclesToGo;
	private int cyclesToStopAtLight = Constants.DRIFT_TO_STOPLIGHT_FRAMES;
	private int cyclesUntilCanDetectStopsign = Constants.WAIT_AFTER_STOPSIGN;
	

	
	
	
	private PedestrianDetector pedDetect;
	
	TrakSim trackSim = new TrakSim();
	
	public SpeedController(){
		this.pedDetect = new PedestrianDetector();
	}
	
	//A method to be called every frame. Calculates desired speed and actual speed
	//Also takes stopping into account
	public void onUpdate(int gasAmount, int steerDegs, int manualSpeed, Graphics graf, DriveTest dtest, boolean blobsOn, boolean overlayOn){
		if (cyclesUntilCanDetectStopsign > 0){
			cyclesUntilCanDetectStopsign--;
		}
		com.apw.pedestrians.Constant.LAST_FRAME_MILLIS = com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS;
		com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS = System.currentTimeMillis();
		com.apw.pedestrians.Constant.TIME_DIFFERENCE = com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS - com.apw.pedestrians.Constant.LAST_FRAME_MILLIS;
		dtest.run();
		//dtest.window.paint(graf);
		this.calculateEstimatedSpeed(gasAmount);
		this.calculateDesiredSpeed(steerDegs, manualSpeed);
		
		//This part runs on-screen blobs thru a set of tests to figure out if they are
		//relevant, and then what to do with them
		ImageManager imageManager = dtest.getImgManager();
		List<MovingBlob> blobs = this.pedDetect.getAllBlobs(imageManager.getSimpleColorRaster(), 912);
		for(MovingBlob i : blobs){
			if(blobsOn){
				if (i.color.getColor() == Color.BLACK) {
					graf.setColor(java.awt.Color.BLACK);	
					color = 0x000000;
				}
				else if (i.color.getColor() == Color.GREY) {
					graf.setColor(java.awt.Color.GRAY);
					color = 0xd3d3d3;
				}
				else if (i.color.getColor() == Color.WHITE) {
					graf.setColor(java.awt.Color.WHITE);
					color = 0xffffff;
				}
				else if (i.color.getColor() == Color.RED) {
					graf.setColor(java.awt.Color.RED);
					color = 0xff0000;
				}
				else if (i.color.getColor() == Color.GREEN) {
					graf.setColor(java.awt.Color.GREEN);
					color = 0x00ff00;
				}
				else if (i.color.getColor() == Color.BLUE) {
					graf.setColor(java.awt.Color.BLUE);
					color = 0x0000ff;
				}
				for(MovingBlob b:blobs){
					int velocity = (int)(100*Math.sqrt(b.velocityX*b.velocityX + b.velocityY*b.velocityY));
					int color = (velocity << 16) + (velocity << 8) + velocity;
					this.trackSim.DrawLine(color, b.y, b.x, b.y+b.height, b.x);
					this.trackSim.DrawLine(color, b.y, b.x, b.y, b.x+b.width);
					this.trackSim.DrawLine(color, b.y+b.height, b.x, b.y+b.height, b.x+b.width);
					this.trackSim.DrawLine(color, b.y, b.x+b.width, b.y+b.height, b.x+b.width);
				}
			}
			if(detectRedLight(i)){
				setStoppingAtLight();
			}
			else if (detectYellowLight(i)) {
			}
			else if (detectGreenLight(i)) {
				readyToGo();
			}
			else if(detectStopSign(i) && cyclesUntilCanDetectStopsign <= 0){
				cyclesUntilCanDetectStopsign = 100;
				setStoppingAtSign();
			}
			else {

			}
		}
		
		
	}
	
	//This figures out the speed that we want to be traveling at
	public void calculateDesiredSpeed(int wheelAngle, int manualSpeed){
		double curveSteepness = 0; // Steering.getCurveSteepness();
		
        int shouldStopSign = this.updateStopSign();
        int shouldStopLight = this.updateStopLight();
        
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
	
	//This returns our distance from an object. Currently non-functional
	public  double getDistance(double focalLength, double realObjHeight, double cameraFrameHeight, double objectPixelHeight, double sensorHeight) {
		
		return (focalLength * realObjHeight * cameraFrameHeight )
		/ ( objectPixelHeight * sensorHeight);
		
	}
	
	
	//Break Rate Math
	
	//The total distance it will take to stop
    double calcStopDist(double targetStopDist, double speed)
    {
        return Math.pow(speed, 2) / (Constants.FRICT * Constants.GRAV * 2);
    }

    //The amount of time that is needed to stop at the given speed.
    double getStopTime(double dist, double speed)
    {
        return dist / speed;
    }

    //The rate at which the speed must go down by, linear
    double calcStopRate(double speed, double time)
    {
        return (0 - speed) / time;
    }
    
    
    //Function used to get the rate to lower the speed by when a stop distance is given.
    double getStopRate(double targetDist, double currentSpeed)
    {
    	return calcStopRate(currentSpeed, getStopTime(targetDist, currentSpeed));
    }
	
    // End Of Brake Rate Math
    
	public int getDesiredSpeed(){
		return (int)desiredSpeed;
    }
    

    //Sets desired speed based on a meter per second
    public void setSpeed(double mPerSec){

        this.desiredSpeed = mPerSec * Constants.PIN_TO_METER_PER_SECOND;

    }

    //Returns speed in meters per second
    public double getMSec(){

        return getEstimatedSpeed() / 0.4;

    }
	
	// Checks a given blob for the properties of a stopsign (size, age, position, color)
	public boolean detectStopSign(MovingBlob blob) {
		if(blob.age > Constants.BLOB_AGE && blob.height > Constants.BLOB_HEIGHT && blob.width > Constants.BLOB_WIDTH && blob.x > Constants.STOPSIGN_MIN_X && blob.x < Constants.STOPSIGN_MAX_X && blob.y > Constants.STOPSIGN_MIN_Y && blob.y < Constants.STOPSIGN_MAX_Y && blob.color.getColor() == Color.RED) {
			return true;
		} else {
			return false;
		}
	}
	
	// Checks a given blob for the properties of a stoplight (size, age, position, color)
	public boolean detectRedLight(MovingBlob blob) {
		if (blob.age > Constants.BLOB_AGE && blob.height > Constants.BLOB_HEIGHT && blob.width > Constants.BLOB_WIDTH && blob.x > Constants.STOPLIGHT_MIN_X && blob.x < Constants.STOPLIGHT_MAX_X && blob.y > Constants.STOPLIGHT_MIN_Y && blob.y < Constants.STOPLIGHT_MAX_Y && blob.color.getColor() == Color.RED) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// Checks a given blob for the properties of a stoplight (size, age, position, color)
	public boolean detectYellowLight(MovingBlob blob) {
		if (blob.age > Constants.BLOB_AGE && blob.height > Constants.BLOB_HEIGHT && blob.width > Constants.BLOB_WIDTH && blob.x > Constants.STOPLIGHT_MIN_X && blob.x < Constants.STOPLIGHT_MAX_X && blob.y > Constants.STOPLIGHT_MIN_Y && blob.y < Constants.STOPLIGHT_MAX_Y && blob.color.getColor() == Color.RED) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// Checks a given blob for the properties of a stoplight (size, age, position, color)
	public boolean detectGreenLight(MovingBlob blob) {
		if (blob.age > Constants.BLOB_AGE && blob.height > Constants.BLOB_HEIGHT && blob.width > Constants.BLOB_WIDTH && blob.x > Constants.STOPLIGHT_MIN_X && blob.x < Constants.STOPLIGHT_MAX_X && blob.y > Constants.STOPLIGHT_MIN_Y && blob.y < Constants.STOPLIGHT_MAX_Y && blob.color.getColor() == Color.GREEN) {
			return true;
		}
		else {
			return false;
		}
    }
    

        //Will decrease speed to keep blob below a certain size
        public void followObjects(MovingBlob blob){


            if(blob.height > Constants.MAX_OBJECT_HEIGHT)
            {
                double heightDiff = Constants.MAX_OBJECT_HEIGHT - blob.height;
                setSpeed( (heightDiff * 0.5) / getMSec());
            }
            else if(blob.width > Constants.MAX_OBJECT_WIDTH)
            {
                double widthDiff = Constants.MAX_OBJECT_WIDTH - blob.width;
                setSpeed( getMSec() - (widthDiff * 0.5) / getMSec());
            }
    
        }
}

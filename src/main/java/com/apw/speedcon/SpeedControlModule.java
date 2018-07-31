package com.apw.speedcon;

import com.apw.imagemanagement.ImageManipulator;
import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;
import com.apw.pedestrians.PedestrianDetector;
import com.apw.pedestrians.blobtrack.MovingBlob;
import com.apw.pedestrians.image.Color;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class SpeedControlModule implements Module {
	private double currentEstimatedSpeed;
	private double desiredSpeed;
	private boolean emergencyStop;
	private boolean go = true; //A toggle to avoid the car from accelerating and decelerating at same time
	
	private PedestrianDetector pedDetect;
	private CameraCalibration cameraCalibrator;
	
	private List<MovingBlob> currentBlobs;
	private List<MovingBlob> currentPeds;
	private ArrayList<MovingBlob> stopObjects;
	
	private SizeConstants sizeCons;
	
	/**
	 * A basic constructor for our SpeedController.
	 */
	public SpeedControlModule() {
		this.pedDetect = new PedestrianDetector();
		this.currentBlobs = new ArrayList<>();
		this.cameraCalibrator = new CameraCalibration();
		this.stopObjects = new ArrayList<>();
		this.sizeCons = new SizeConstants();
	}
	
	/**
	 * This method is some gpu stuff to add key events, so that we can control stuff by
	 * manually hitting keys
	 */
	@Override
	public void initialize(CarControl control) {
		control.addKeyEvent(KeyEvent.VK_B, () -> Settings.blobsOn ^= true);
		control.addKeyEvent(KeyEvent.VK_V, () -> Settings.overlayOn ^= true);
		control.addKeyEvent(KeyEvent.VK_F, () -> cameraCalibrator.calibrateCamera(control));
		control.addKeyEvent(KeyEvent.VK_C, () -> Settings.writeBlobsToConsole ^= true);
		control.addKeyEvent(KeyEvent.VK_S, () -> Settings.writeSpeedToConsole ^= true);
		control.addKeyEvent(KeyEvent.VK_M, () -> Settings.colorMode++);
		control.addKeyEvent(KeyEvent.VK_UP, () -> control.manualSpeedControl(false, 1));
		control.addKeyEvent(KeyEvent.VK_UP, () -> control.manualSpeedControl(false, -1));
		//control.addKeyEvent(KeyEvent.VK_P, this::setStoppingAtSign);
		//control.addKeyEvent(KeyEvent.VK_O, this::setStoppingAtLight);
		//control.addKeyEvent(KeyEvent.VK_I, this::readyToGo);
	}
	
	/**
	 * A method that is called every frame to call the methods that need to get called every frame
	 * @param control this is our car controller
	 *
	 */
	@Override
	public void update(CarControl control) {
		onUpdate(control);
		control.accelerate(true, getNextSpeed());
		System.out.println("Speed: " + getNextSpeed());
	}
	
	@Override
	public void paint(CarControl control, Graphics g) {
		if (control.getProcessedImage() == null) {
			return;
		}
		
		PedestrianDetector pedDetect = new PedestrianDetector();
		// TODO get rid of hardcoded values
		final int width = 912;
		final int height = 480;
		int vEdit = (height - 480) / 2 - 25;
		byte[] limitArray = new byte[640 * 480];
		ImageManipulator.limitTo(limitArray, control.getProcessedImage(), width, height, 640, 480, false);
		List<MovingBlob> blobs = pedDetect.getAllBlobs(limitArray, 640);
		
		
		
		//We then:
		//A. Display those blobs on screen as empty rectangular boxes of the correct color
		//B. Test if those blobs are a useful roadsign/light
		//C. Do whatever we need to do if so
		//D. Write to the console what has happened
		//We need all of the if statements to display the colors,
		//as we need to convert from IPixel colors to Java.awt colors for display reasons
		if (Settings.blobsOn) {
			for (MovingBlob i : blobs) {
				if (i.color.getColor() == com.apw.pedestrians.image.Color.BLACK) {
					g.setColor(java.awt.Color.BLACK);
				} 
				else if (i.color.getColor() == com.apw.pedestrians.image.Color.GREY) {
					g.setColor(java.awt.Color.GRAY);
				} 
				else if (i.color.getColor() == com.apw.pedestrians.image.Color.WHITE) {
					g.setColor(java.awt.Color.WHITE);
				}
				else if (i.color.getColor() == com.apw.pedestrians.image.Color.RED) {
					g.setColor(java.awt.Color.RED);
				} 
				else if (i.color.getColor() == com.apw.pedestrians.image.Color.GREEN) {
					g.setColor(java.awt.Color.GREEN);
				} 
				else if (i.color.getColor() == com.apw.pedestrians.image.Color.BLUE) {
					g.setColor(java.awt.Color.BLUE);
				}
				g.drawRect(i.x + 8, i.y + 40 + vEdit, i.width, i.height);
			}
		}
	}
	
	//A method to be called every frame. Calculates desired speed and actual speed
	//Also takes stopping into account
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
	public void onUpdate(CarControl control) {
		int gasAmount = control.getGas();
		int steerDegs = control.getSteering();
		int manualSpeed = control.getManualSpeed();
		
		com.apw.pedestrians.Constant.LAST_FRAME_MILLIS = com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS;
		com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS = System.currentTimeMillis();
		com.apw.pedestrians.Constant.TIME_DIFFERENCE = com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS - com.apw.pedestrians.Constant.LAST_FRAME_MILLIS;
		this.calculateEstimatedSpeed(gasAmount);
		this.calculateDesiredSpeed(steerDegs, manualSpeed);
		
		List<MovingBlob> blobs = this.pedDetect.getAllBlobs(control.getProcessedImage(), 912);
		List<MovingBlob> peds = this.pedDetect.detect(control.getProcessedImage(), 912);
		this.currentBlobs = blobs;
		this.currentPeds = peds;

		
		for (MovingBlob i: currentBlobs) {
			if (detectStopSign(i)) {
				i.type = "Stop";
				i.seen = true;
				System.out.println("Found a stopsign: " + i);
				determineStop(i);
			}
			if (detectLight(i) > 0) {
				i.type = "StopLightWidth";
				i.seen = true;
				System.out.println("Found a " + i.color.getColor() + "light: " + i);
				if (detectLight(i) == 2 || detectLight(i) == 3) {
					determineStop(i);
				}
			}
		}
		
		if (emergencyStop) {
			System.out.println("EMERGENCY STOP");
			emergencyStop();
		}
		
		for(MovingBlob i : currentPeds) {
			System.out.println("FOUND PED "+i.id);
			determinePedStop(i);
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
	public boolean detectBlobOverlappingBlob(MovingBlob outsideBlob, MovingBlob insideBlob) {
		return (insideBlob.x < outsideBlob.x + outsideBlob.width && 
				insideBlob.width + insideBlob.x > outsideBlob.x) || 
				(insideBlob.y < outsideBlob.y + outsideBlob.height && 
				insideBlob.height + insideBlob.y > outsideBlob.y);
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
	public void calculateDesiredSpeed(double wheelAngle, int manualSpeed) {
		//Logic for determining if we need to be slowing down due to a roadsign/light, and why
		if (go == true) {
			this.desiredSpeed = Math.min(Math.max((1 - Math.abs((double) (wheelAngle) / 90.0)) * Constants.MAX_SPEED + manualSpeed, Constants.MIN_SPEED), Constants.MAX_SPEED);
		} 
	}
	
	/**
	 * Changes our speed from our current (estimated) speed to our desired speed in a smooth fashion.
	 * 
	 * @return the speed that we should currently be traveling at
	 */
	public int getNextSpeed() {
		double distance = this.desiredSpeed - this.currentEstimatedSpeed;
		if (Math.abs(distance) < Constants.MIN_SPEED_INCREMENT) {
			return (int) this.desiredSpeed;
		} 
		else if (distance < 0) {
			return (int) (this.currentEstimatedSpeed - Constants.MIN_SPEED_INCREMENT);
		} 
		else {
			return (int) (this.currentEstimatedSpeed + Constants.MIN_SPEED_INCREMENT);
		}
	}
	
	//Calculates when the car should start to stop, then reduces its speed.
	private void determineStop(MovingBlob closestBlob) {
		double blobRealSize = getStopReal(closestBlob); //Gets real size
		double distToBlob = cameraCalibrator.distanceToObj(blobRealSize/cameraCalibrator.relativeWorldScale, closestBlob.width); //Finds distance to closest blob based on real wrold size and pixel size
		if(cameraCalibrator.getStopTime(distToBlob - Constants.MIN_STOP_DISTANCE, getEstimatedSpeed()) <= Constants.MIN_STOP_TIME) { //If amount of time needed to stop is <= min stop time, starts to stop
			go = false;
			System.out.println("WEIRD STUFF HAPPENS HERE");
			this.desiredSpeed = desiredSpeed - cameraCalibrator.calcStopRate(getEstimatedSpeed(), cameraCalibrator.getStopTime(distToBlob, getEstimatedSpeed()));
		}
		else {
			go = true;
		}
	}
	
	//Returns the real size of the object to find distance to it
	private double getStopReal(MovingBlob stopBlob) {
		return sizeCons.SIGN_INFO.get(stopBlob.type).get(1);
	}
	
	//Returns the estimated speed IN METERS PER SECOND
	public double getEstimatedSpeed() {
		return currentEstimatedSpeed * Constants.PIN_TO_METER_PER_SECOND;
	}
	
	//Updates the estimated speed
	public void calculateEstimatedSpeed(int gasAmount) {
		currentEstimatedSpeed = gasAmount;
	}
	
	//Getting and setting our emergency stop boolean
	public boolean getEmergencyStop() {
		return emergencyStop;
	}
	
	public void setEmergencyStop(boolean emer) {
		this.emergencyStop = emer;
	}
	
	public int getDesiredSpeed() {
		return (int) desiredSpeed;
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
		if (blob.age > Constants.BLOB_AGE &&
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
			
			return true;
		}
		
		return false;
	}
	
	public boolean determinePedStop(MovingBlob ped) {
		System.out.println("Ped Width "+ped.width+" Ped X "+ped.x);
		if(ped.width >= Constants.PED_MIN_SIZE &&
			ped.x >= Constants.PED_MIN_X &&
			ped.x <= Constants.PED_MAX_X) {
			return true;
		}
		return false;
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
	public int detectLight(MovingBlob blob) {
		int lightColor = 0;
		
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
			
			for (MovingBlob b : currentBlobs) {
				if (b.color.getColor() == Color.BLACK) {
					if (detectBlobOverlappingBlob(b, blob)) {
						lightColor = 1;
					}
				}
			}
		} 
		
		else if (blob.age > Constants.BLOB_AGE &&
			blob.height > Constants.BLOB_MIN_HEIGHT &&
			blob.height < (1/2) * Constants.BLOB_MAX_HEIGHT &&
			blob.width > Constants.BLOB_MIN_WIDTH &&
			blob.width < (1/2) * Constants.BLOB_MAX_WIDTH &&
			blob.x > Constants.STOPLIGHT_MIN_X &&
			blob.x < Constants.STOPLIGHT_MAX_X &&
			blob.y > Constants.STOPLIGHT_MIN_Y &&
			blob.y < Constants.STOPLIGHT_MAX_Y &&
			blob.color.getColor() == Color.YELLOW &&
			!blob.seen &&
			((double) blob.height / (double) blob.width) < 1 + Constants.BLOB_RATIO_DIF &&
			((double) blob.height / (double) blob.width) > 1 - Constants.BLOB_RATIO_DIF) {
			
			for (MovingBlob b : currentBlobs) {
				if (b.color.getColor() == Color.BLACK) {
					if (detectBlobOverlappingBlob(b, blob)) {
						lightColor = 2;
					}
				}
			}
		} 
		
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
			
			for (MovingBlob b : currentBlobs) {
				if (b.color.getColor() == Color.BLACK) {
					if (detectBlobOverlappingBlob(b, blob)) {
						lightColor = 3;
						
					}
				}
			}
		}
		
	return lightColor;
	}
	
	public CameraCalibration getCalibrator() {
		return cameraCalibrator;
	}
	
	public void emergencyStop() {
		this.desiredSpeed = cameraCalibrator.calcStopRate(getEstimatedSpeed(), 0.1);
	}
}

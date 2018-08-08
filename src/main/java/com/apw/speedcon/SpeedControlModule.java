package com.apw.speedcon;

import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;
import com.apw.imagemanagement.ImageManipulator;
import com.apw.pedestrians.Constant;
import com.apw.pedestrians.PedestrianDetector;
import com.apw.pedestrians.blobtrack.MovingBlob;
import com.apw.pedestrians.image.Color;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpeedControlModule implements Module {

  
	private double currentEstimatedSpeed;
	private double desiredSpeed;
	private boolean emergencyStop;
	private boolean cycleStopping;
	private int stopType;
	private int frameWait;
	
	private double initDistToBlob;
	private double distToBlob;
	private double rpmSpeed;
	
	
	private PedestrianDetector pedDetect;
	private CameraCalibration cameraCalibrator;
	
	private List<MovingBlob> currentBlobs;
	//private List<MovingBlob> currentPeds;
	
	private SizeConstants sizeCons;
	
	/**
	 * A basic constructor for our SpeedController.
	 */
	public SpeedControlModule() {
		this.pedDetect = new PedestrianDetector();
		this.currentBlobs = new ArrayList<>();
		this.cameraCalibrator = new CameraCalibration();
		this.sizeCons = new SizeConstants();
	}
	
	/**
	 * This method is some gpu stuff to add key events, so that we can control stuff by
	 * manually hitting keys
	 */
	@Override
	public void initialize(CarControl control) {
		control.addKeyEvent(KeyEvent.VK_P, () -> stopType = 1);
		control.addKeyEvent(KeyEvent.VK_O, () -> stopType = 3);
		control.addKeyEvent(KeyEvent.VK_I, () -> stopType = 0);
		control.addKeyEvent(KeyEvent.VK_I, () -> cycleStopping = false);
		control.addKeyEvent(KeyEvent.VK_B, () -> Settings.blobsOn ^= true);
		control.addKeyEvent(KeyEvent.VK_V, () -> Settings.overlayOn ^= true);
		control.addKeyEvent(KeyEvent.VK_F, () -> cameraCalibrator.calibrateCamera(control, currentBlobs));
		control.addKeyEvent(KeyEvent.VK_C, () -> Settings.writeBlobsToConsole ^= true);
		control.addKeyEvent(KeyEvent.VK_S, () -> Settings.writeSpeedToConsole ^= true);
		control.addKeyEvent(KeyEvent.VK_M, () -> Settings.colorMode++);
		control.addKeyEvent(KeyEvent.VK_UP, () -> control.manualSpeedControl(true, 20));
		control.addKeyEvent(KeyEvent.VK_DOWN, () -> control.manualSpeedControl(true, 0));
	}


	/**
	 * A method that is called every frame to call the methods that need to get called every frame
	 * @param control this is our car controller
	 *
	 */
	@Override
	public void update(CarControl control) {
		List<MovingBlob> blobs = this.pedDetect.getAllBlobs(control.getProcessedImage(), 912);
		//List<MovingBlob> peds = this.pedDetect.detect(control.getProcessedImage(), 912, blobs);
		this.currentBlobs = blobs;
		//this.currentPeds = peds;
		
		onUpdate(control);
		control.accelerate(true, getNextSpeed());
		System.out.println("getNextSpeed(): " + getNextSpeed());
	}
	
	@Override
	public void paint(CarControl control, Graphics g) {
		if (control.getProcessedImage() == null) {
			return;
		}
		
		
		byte[] limitArray = new byte[Constants.SCREEN_FILTERED_WIDTH * Constants.SCREEN_HEIGHT];
		ImageManipulator.limitTo(limitArray, control.getProcessedImage(), Constants.SCREEN_FILTERED_WIDTH, Constants.SCREEN_HEIGHT, Constants.SCREEN_FILTERED_WIDTH, Constants.SCREEN_HEIGHT, false);
		
		if(Settings.overlayOn){
			//Draw our stoplight hitbox in constant designated color
			java.awt.Color color = java.awt.Color.decode(Constants.OVERLAY_STOPLIGHT_HITBOX_COLOR);
			g.setColor(color);
			g.drawRect(Constants.STOPLIGHT_MIN_Y, Constants.STOPLIGHT_MIN_X, Constants.STOPLIGHT_MIN_Y, Constants.STOPLIGHT_MAX_X);
				
			//Draw our stopsign hitbox in constant designated color
			color = java.awt.Color.decode(Constants.OVERLAY_STOPSIGN_HITBOX_COLOR);
			g.setColor(color);
			g.drawRect(Constants.STOPSIGN_MIN_Y, Constants.STOPSIGN_MIN_X, Constants.STOPSIGN_MIN_Y, Constants.STOPSIGN_MAX_X);
		}
		
		if (Settings.writeBlobsToConsole) {
			for (MovingBlob blob : currentBlobs) {
				if ((((double) blob.height / (double) blob.width) < 1 + Constants.BLOB_RATIO_DIF && ((double) blob.height / (double) blob.width) > 1 - Constants.BLOB_RATIO_DIF)) {
					System.out.println(blob);
					System.out.println(blob.color.getColor());
					System.out.println(blob.id);
				}
			}
	}
		
		if (Settings.colorMode >= Settings.numColorModes) {
			Settings.colorMode = 0;
		}
		if (Settings.blobsOn) {
			for (MovingBlob blob : currentBlobs){
				if ((((double) blob.height / (double) blob.width) < 1 + Constants.BLOB_RATIO_DIF && ((double) blob.height / (double) blob.width) > 1 - Constants.BLOB_RATIO_DIF)) {
					int velocity = (int)(100*Math.sqrt(blob.velocityX*blob.velocityX + blob.velocityY*blob.velocityY));
					java.awt.Color color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_AGE_5_COLOR);
					g.setColor(color);
					//If colormode is 0, set displayed blob color based upon age, so that older ones are darker
					if (Settings.colorMode == 0) {
						if (blob.age >= Constants.DISPLAY_AGE_MAX) {
							color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_AGE_5_COLOR);
							g.setColor(color);
						}
						else if (blob.age >= (4 * (Constants.DISPLAY_AGE_MAX - Constants.DISPLAY_AGE_MIN) / 5) + Constants.DISPLAY_AGE_MIN) {
							color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_AGE_4_COLOR);
							g.setColor(color);
						}
						else if (blob.age >= (3 * (Constants.DISPLAY_AGE_MAX - Constants.DISPLAY_AGE_MIN) / 5) + Constants.DISPLAY_AGE_MIN) {
							color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_AGE_3_COLOR);
							g.setColor(color);
						}
						else if (blob.age >= (2 * (Constants.DISPLAY_AGE_MAX - Constants.DISPLAY_AGE_MIN) / 5) + Constants.DISPLAY_AGE_MIN) {
							color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_AGE_2_COLOR);
							g.setColor(color);
						}
						else if (blob.age >= ((Constants.DISPLAY_AGE_MAX - Constants.DISPLAY_AGE_MIN) / 5) + Constants.DISPLAY_AGE_MIN) {
							color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_AGE_1_COLOR);
							g.setColor(color);
						}
						else if (blob.age <= Constants.DISPLAY_AGE_MIN) {
							color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_AGE_0_COLOR);
							g.setColor(color);
						}
					}
					//If colormode is 1, set displayed blob color to the color of the blob we are looking at
					//A conversion is needed here, as the stored colors in blobs are not hex values, and they need to be
					else if (Settings.colorMode == 1) {
						if (blob.color.getColor() == com.apw.pedestrians.image.Color.BLACK) {
							color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_COLOR_BLACK);
							g.setColor(color);
						} else if (blob.color.getColor() == com.apw.pedestrians.image.Color.GREY) {
							color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_COLOR_GRAY);
							g.setColor(color);
						} else if (blob.color.getColor() == com.apw.pedestrians.image.Color.WHITE) {
							color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_COLOR_WHITE);
							g.setColor(color);
						} else if (blob.color.getColor() == com.apw.pedestrians.image.Color.RED) {
							color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_COLOR_RED);
							g.setColor(color);
						} else if (blob.color.getColor() == com.apw.pedestrians.image.Color.GREEN) {
							color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_COLOR_GREEN);
							g.setColor(color);
						} else if (blob.color.getColor() == com.apw.pedestrians.image.Color.BLUE) {
								color = java.awt.Color.decode(Constants.BLOBVERLAY_COLORMODE_COLOR_BLUE);
								g.setColor(color);
						}
					}
					//If colormode is 2, set displayed blob color to be based upon velocity
					else if (Settings.colorMode == 2) {
						color = java.awt.Color.decode(Integer.toString((velocity << 16) + (velocity << 8) + velocity));
						g.setColor(color);
					}
					
					//Draw our current blob on screen
					g.drawRect(blob.x, blob.y + 16, blob.width, blob.height);
				}
			}
		}
		
		//We then:
		//A. Display those blobs on screen as empty rectangular boxes of the correct color
		//B. Test if those blobs are a useful roadsign/light
		//C. Do whatever we need to do if so
		//D. Write to the console what has happened
		//We need all of the if statements to display the colors,
		//as we need to convert from IPixel colors to Java.awt colors for display reasons

		//for (MovingBlob blob : currentPeds) {
		//	if (stopType == 4) {
		//		System.out.println("Found a pedestrian: " + blob);
		//		System.out.println("Id: " + blob.id);
		//		
		//		g.setColor(java.awt.Color.MAGENTA);
		//		g.drawRect(blob.x, blob.y + 16, blob.width, blob.height);	
		//}
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
		int gasAmount = control.getVelocity();
		int steerDegs = control.getSteering();
		int manualSpeed = control.getManualSpeed();
		
		com.apw.pedestrians.Constant.LAST_FRAME_MILLIS = com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS;
		com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS = System.currentTimeMillis();
		com.apw.pedestrians.Constant.TIME_DIFFERENCE = com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS - com.apw.pedestrians.Constant.LAST_FRAME_MILLIS;
		this.calculateEstimatedSpeed(gasAmount);
		this.calculateDesiredSpeed(steerDegs, manualSpeed);
		
		for (MovingBlob blob: currentBlobs) {
			if (detectStopSign(blob)) {
				stopType = 1;
				
				cycleStopping = true;
				
				blob.type = "Stop";
				System.out.println("Found a stopsign: " + "Color: " + blob.color.getColor() + blob);
				

				determineStop(blob, sizeCons.SIGN_INFO.get(blob.type).get(6), control);
				
				blob.seen = true;
			}
			if (detectLight(blob) == 1) {
				stopType = 2;	
				
				cycleStopping = true;
				
				blob.type = "StopLightWidth";
				System.out.println("Found a " + blob.color.getColor() + "light: " + "Color: " + blob.color.getColor() + blob);
				
				determineStop(blob, sizeCons.SIGN_INFO.get(blob.type).get(6), control);
				
				blob.seen = true;
			}
			else if (detectLight(blob) == 2) {
				stopType = 3;	
				
				cycleStopping = true;
				
				blob.type = "StopLightWidth";
				System.out.println("Found a " + blob.color.getColor() + "light: " + "Color: " + blob.color.getColor() + blob);
				
				determineStop(blob, sizeCons.SIGN_INFO.get(blob.type).get(6), control);

				
				blob.seen = true;
			}
			else if (detectLight(blob) == 3) {
				if (stopType == 2 || stopType == 3) {
					stopType = 0;
					cycleStopping = false;
				}
				
				blob.type = "StopLightWidth";
				System.out.println("Found a " + blob.color.getColor() + "light: " + "Color: " + blob.color.getColor() + blob);
				blob.seen = true;
			}
		}
		
		if (cycleStopping) {
			if (stopType != 0) {
				System.out.println("Stopped because stopping was true but it didn't find any signs or lights");	
			}
			
			determineStop();
		}
		
		//for(MovingBlob blob : currentPeds) {
		//	if (determinePedStop(blob)) {
				//stopType = 4;	
				
				//cycleStopping = true;
				
				//blob.type = "StopLightWidth";
				
				//determineStop(blob);
				
				//blob.seen = true;
		//	}
		//}
		
		if (emergencyStop) {
			stopType = 5;
			
			System.out.println("EMERGENCY STOP");
			
			determineStop();
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
		if (stopType == 0) {
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
		if (Math.abs(distance) < Constants.MAX_SPEED_INCREMENT) {
			return (int) this.desiredSpeed;
		} 
		else if (distance < 0) {
			return (int) (this.currentEstimatedSpeed - Constants.MAX_SPEED_INCREMENT);
		} 
		else {
			return (int) (this.currentEstimatedSpeed + Constants.MAX_SPEED_INCREMENT);
		}
	}
	
	//Calculates when the car should start to stop, then reduces its speed.
	private void determineStop(MovingBlob stoppingBlob, double objectHeight, CarControl control) {
		if (stopType != 0) {
			stopsignWaitFirst();
			
			double blobRealSize = getStopReal(stoppingBlob); //Gets real size
			distToBlob = cameraCalibrator.distanceToObj(blobRealSize, stoppingBlob.width, sizeCons.SIGN_INFO.get(stoppingBlob.type).get(6)); //Finds distance to closest blob based on real wrold size and pixel size
			
			System.out.println("frameWait: " + frameWait);
			System.out.println("stopType: " + stopType);
			System.out.println("desiredSpeed: " + desiredSpeed);
			System.out.println("getEstimatedSpeed: " + getEstimatedSpeed());
			System.out.println("distToBlob: " + distToBlob);
			System.out.println("blobRealSize: " + blobRealSize);
			System.out.println("stoppingBlob.width: " + stoppingBlob.width);
			System.out.println("sizeCons.SIGN_INFO.get(stoppingBlob.type).get(6): " + sizeCons.SIGN_INFO.get(stoppingBlob.type).get(6));
			System.out.println("Actual dist: " + Math.sqrt(Math.pow(Math.abs(control.getPosition(true) - (2 * 28.75)), 2) + Math.pow(Math.abs(control.getPosition(false) - (2 * 29.5)), 2)));
			System.out.println(cameraCalibrator.calcStopRate(getEstimatedSpeed(), cameraCalibrator.getStopTime(distToBlob, getEstimatedSpeed())));
			
			this.desiredSpeed = desiredSpeed - cameraCalibrator.calcStopRate(getEstimatedSpeed(), cameraCalibrator.getStopTime(distToBlob, getEstimatedSpeed()));
		
			if (desiredSpeed < 1) {
				desiredSpeed = 1;
			}
		}
	}
	
	//Calculates when the car should start to stop, then reduces its speed.
	private void determineStop() {
		if (stopType != 0) {
			stopsignWaitSubsequent();
			
			//distToBlob -= (rpmSpeed / Constants.WHEEL_GEARING) * Constants.WHEEL_CIRCUMFERENCE * Constant.TIME_DIFFERENCE;
			distToBlob -= getEstimatedSpeed() * (Constant.TIME_DIFFERENCE / 1000.0);
			
			//System.out.println("frameWait: " + frameWait);
			//System.out.println("stopType: " + stopType);
			//System.out.println("distToBlob: " + distToBlob);
			//System.out.println(getEstimatedSpeed());
			//System.out.println(Constant.TIME_DIFFERENCE);
			//System.out.println("desiredSpeed: " + desiredSpeed);
			//System.out.println("Change in desiredSpeed: " + cameraCalibrator.calcStopRate(getEstimatedSpeed(), cameraCalibrator.getStopTime(distToBlob, getEstimatedSpeed())));
			
			this.desiredSpeed = desiredSpeed - cameraCalibrator.calcStopRate(getEstimatedSpeed(), cameraCalibrator.getStopTime(distToBlob, getEstimatedSpeed()));
			
			if (desiredSpeed < 1) {
				desiredSpeed = 1;
			}
		}
	}
	
	private void stopsignWaitFirst() {
		if (stopType == 1) {
			frameWait = Constants.WAIT_AT_STOPSIGN_FRAMES + 1;
		}

		frameWait -= 1;
		
		if (frameWait == 0 && stopType == 1) {
			stopType = 0;
			cycleStopping = false;
		}
	}
	
	private void stopsignWaitSubsequent() {
		frameWait -= 1;
		
		if (frameWait == 0 && stopType == 1) {
			stopType = 0;
			cycleStopping = false;
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
		if (blob.color.getColor() == Color.RED &&
			blob.age > Constants.BLOB_AGE &&
			blob.height > (3) * Constants.BLOB_MIN_HEIGHT &&
			blob.height < Constants.BLOB_MAX_HEIGHT &&
			blob.width > (3) * Constants.BLOB_MIN_WIDTH &&
			blob.width < Constants.BLOB_MAX_WIDTH &&
			blob.x > Constants.STOPSIGN_MIN_X &&
			blob.x < Constants.STOPSIGN_MAX_X &&
			blob.y > Constants.STOPSIGN_MIN_Y &&
			blob.y < Constants.STOPSIGN_MAX_Y &&
			!blob.seen &&
			(((double) blob.height / (double) blob.width) < 1 + Constants.BLOB_RATIO_DIF &&
			((double) blob.height / (double) blob.width) > 1 - Constants.BLOB_RATIO_DIF)) {
			
			return true;
		}
		return false;
	}
	
	//public boolean determinePedStop(MovingBlob ped) {
	//	if(ped.width >= Constants.PED_MIN_SIZE &&
	//		ped.x >= Constants.PED_MIN_X &&
	//		ped.x <= Constants.PED_MAX_X) {
	//		
	//		//System.out.println("Ped Width "+ped.width+" Ped X "+ped.x+" Ped Y "+ped.y);
	//		//System.out.println(ped);
	//		return true;
	//	}
	//	return false;
	//}
	
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
	
	public int detectLight(MovingBlob blob) {
		int lightColor = 0;
		if (blob.color.getColor() == Color.RED &&
			blob.age >= Constants.BLOB_AGE &&
			blob.height >= (2) + Constants.BLOB_MIN_HEIGHT &&
			blob.height <= (2) + Constants.BLOB_MAX_HEIGHT &&
			blob.width >= Constants.BLOB_MIN_WIDTH &&
			blob.width <= Constants.BLOB_MAX_WIDTH &&
			blob.x >= Constants.STOPLIGHT_MIN_X &&
			blob.x <= Constants.STOPLIGHT_MAX_X &&
			blob.y >= Constants.STOPLIGHT_MIN_Y &&
			blob.y <= Constants.STOPLIGHT_MAX_Y &&
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
		else if (blob.color.getColor() == Color.YELLOW &&
			blob.age > Constants.BLOB_AGE &&
			blob.height > Constants.BLOB_MIN_HEIGHT &&
			blob.height < (1/2) * Constants.BLOB_MAX_HEIGHT &&
			blob.width > Constants.BLOB_MIN_WIDTH &&
			blob.width < (1/2) * Constants.BLOB_MAX_WIDTH &&
			blob.x > Constants.STOPLIGHT_MIN_X &&
			blob.x < Constants.STOPLIGHT_MAX_X &&
			blob.y > Constants.STOPLIGHT_MIN_Y &&
			blob.y < Constants.STOPLIGHT_MAX_Y &&
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
		else if (blob.color.getColor() == Color.GREEN &&
			blob.age > Constants.BLOB_AGE &&
			blob.height > Constants.BLOB_MIN_HEIGHT &&
			blob.height < Constants.BLOB_MAX_HEIGHT &&
			blob.width > Constants.BLOB_MIN_WIDTH &&
			blob.width < Constants.BLOB_MAX_WIDTH &&
			blob.x > Constants.STOPLIGHT_MIN_X &&
			blob.x < Constants.STOPLIGHT_MAX_X &&
			blob.y > Constants.STOPLIGHT_MIN_Y &&
			blob.y < Constants.STOPLIGHT_MAX_Y &&
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
	
	//Getting and setting our emergency stop boolean
	public boolean getEmergencyStop() {
		return emergencyStop;
	}
	
	public void setEmergencyStop(boolean emer) {
		this.emergencyStop = emer;
	}
	
	public void emergencyStop() {
		this.desiredSpeed = cameraCalibrator.calcStopRate(getEstimatedSpeed(), 0.1);
	}
}

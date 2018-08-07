package com.apw.speedcon;

import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;
import com.apw.imagemanagement.ImageManipulator;
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
	private int stopType;
	private int frameWait;
	
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
		control.addKeyEvent(KeyEvent.VK_DOWN, () -> control.manualSpeedControl(false, -1));
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
		//System.out.println("Speed: " + getNextSpeed());
	}
	/*
	@Override
	public void paint(CarControl control, Graphics g) {

		if (control.getProcessedImage() == null) {
			return;
		}
		
		PedestrianDetector pedDetect = new PedestrianDetector();
		
		byte[] limitArray = new byte[Constants.SCREEN_FILTERED_WIDTH * Constants.SCREEN_HEIGHT];
		ImageManipulator.limitTo(limitArray, control.getProcessedImage(), Constants.SCREEN_FILTERED_WIDTH, Constants.SCREEN_HEIGHT, Constants.SCREEN_FILTERED_WIDTH, Constants.SCREEN_HEIGHT, false);
		//List<MovingBlob> blobs = pedDetect.getAllBlobs(limitArray, Constants.SCREEN_FILTERED_WIDTH);
		//List<MovingBlob> peds = pedDetect.detect(limitArray, Constants.SCREEN_FILTERED_WIDTH);
		
		if(Settings.overlayOn){
			//Draw our stoplight hitbox in constant designated color
			int color = Constants.OVERLAY_STOPLIGHT_HITBOX_COLOR;
			control.drawLine(color, Constants.STOPLIGHT_MIN_Y, Constants.STOPLIGHT_MIN_X, Constants.STOPLIGHT_MIN_Y, Constants.STOPLIGHT_MAX_X);
			control.drawLine(color, Constants.STOPLIGHT_MIN_Y, Constants.STOPLIGHT_MAX_X, Constants.STOPLIGHT_MAX_Y, Constants.STOPLIGHT_MAX_X);
			control.drawLine(color, Constants.STOPLIGHT_MAX_Y, Constants.STOPLIGHT_MAX_X, Constants.STOPLIGHT_MAX_Y, Constants.STOPLIGHT_MIN_X);
			control.drawLine(color, Constants.STOPLIGHT_MAX_Y, Constants.STOPLIGHT_MIN_X, Constants.STOPLIGHT_MIN_Y, Constants.STOPLIGHT_MIN_X);
			
			//Draw our stopsign hitbox in constant designated color
			color = Constants.OVERLAY_STOPSIGN_HITBOX_COLOR;
			control.drawLine(color, Constants.STOPSIGN_MIN_Y, Constants.STOPSIGN_MIN_X, Constants.STOPSIGN_MIN_Y, Constants.STOPSIGN_MAX_X);
			control.drawLine(color, Constants.STOPSIGN_MIN_Y, Constants.STOPSIGN_MAX_X, Constants.STOPSIGN_MAX_Y, Constants.STOPSIGN_MAX_X);
			control.drawLine(color, Constants.STOPSIGN_MAX_Y, Constants.STOPSIGN_MAX_X, Constants.STOPSIGN_MAX_Y, Constants.STOPSIGN_MIN_X);
			control.drawLine(color, Constants.STOPSIGN_MAX_Y, Constants.STOPSIGN_MIN_X, Constants.STOPSIGN_MIN_Y, Constants.STOPSIGN_MIN_X);
		}
		
		/*if (Settings.blobsOn) {
			for(MovingBlob b:this.speedControl.getBlobs()){
				if ((((double) b.height / (double) b.width) < 1 + Constants.BLOB_RATIO_DIF && ((double) b.height / (double) b.width) > 1 - Constants.BLOB_RATIO_DIF)) {
					int velocity = (int)(100*Math.sqrt(b.velocityX*b.velocityX + b.velocityY*b.velocityY));
					int color = Constants.BLOBVERLAY_COLORMODE_AGE_5_COLOR;
					
					//If colormode is 0, set displayed blob color based upon age, so that older ones are darker
					if (Settings.colorMode == 0) {
						if (b.age >= Constants.DISPLAY_AGE_MAX) {
							color = Constants.BLOBVERLAY_COLORMODE_AGE_5_COLOR;
						}
						else if (b.age >= (4 * (Constants.DISPLAY_AGE_MAX - Constants.DISPLAY_AGE_MIN) / 5) + Constants.DISPLAY_AGE_MIN) {
							color = Constants.BLOBVERLAY_COLORMODE_AGE_4_COLOR;
						}
						else if (b.age >= (3 * (Constants.DISPLAY_AGE_MAX - Constants.DISPLAY_AGE_MIN) / 5) + Constants.DISPLAY_AGE_MIN) {
							color = Constants.BLOBVERLAY_COLORMODE_AGE_3_COLOR;
						}
						else if (b.age >= (2 * (Constants.DISPLAY_AGE_MAX - Constants.DISPLAY_AGE_MIN) / 5) + Constants.DISPLAY_AGE_MIN) {
							color = Constants.BLOBVERLAY_COLORMODE_AGE_2_COLOR;
						}
						else if (b.age >= ((Constants.DISPLAY_AGE_MAX - Constants.DISPLAY_AGE_MIN) / 5) + Constants.DISPLAY_AGE_MIN) {
							color = Constants.BLOBVERLAY_COLORMODE_AGE_1_COLOR;
						}
						else if (b.age <= Constants.DISPLAY_AGE_MIN) {
							color = Constants.BLOBVERLAY_COLORMODE_AGE_0_COLOR;
						}
					}
					//If colormode is 1, set displayed blob color to the color of the blob we are looking at
					//A conversion is needed here, as the stored colors in blobs are not hex values, and they need to be
					else if (Settings.colorMode == 1) {
						if (b.color.getColor() == com.apw.pedestrians.image.Color.BLACK) {
			                color = Constants.BLOBVERLAY_COLORMODE_COLOR_BLACK;
			            } else if (b.color.getColor() == com.apw.pedestrians.image.Color.GREY) {
			            	color = Constants.BLOBVERLAY_COLORMODE_COLOR_GRAY;
			            } else if (b.color.getColor() == com.apw.pedestrians.image.Color.WHITE) {
			            	color = Constants.BLOBVERLAY_COLORMODE_COLOR_WHITE;
			            } else if (b.color.getColor() == com.apw.pedestrians.image.Color.RED) {
			            	color = Constants.BLOBVERLAY_COLORMODE_COLOR_RED;
			            } else if (b.color.getColor() == com.apw.pedestrians.image.Color.GREEN) {
			            	color = Constants.BLOBVERLAY_COLORMODE_COLOR_GREEN;
			            } else if (b.color.getColor() == com.apw.pedestrians.image.Color.BLUE) {
			            	color = Constants.BLOBVERLAY_COLORMODE_COLOR_BLUE;
			            }
					}
					//If colormode is 2, set displayed blob color to be based upon velocity
					else if (Settings.colorMode == 2) {
						color = (velocity << 16) + (velocity << 8) + velocity;	
					}
					
					//Draw our current blob on screen
					this.theSim.DrawLine(color, b.y, b.x, b.y+b.height, b.x);
					this.theSim.DrawLine(color, b.y, b.x, b.y, b.x+b.width);
					this.theSim.DrawLine(color, b.y+b.height, b.x, b.y+b.height, b.x+b.width);
					this.theSim.DrawLine(color, b.y, b.x+b.width, b.y+b.height, b.x+b.width);
				}
			}
		}*/
		
		
		
		//We then:
		//A. Display those blobs on screen as empty rectangular boxes of the correct color
		//B. Test if those blobs are a useful roadsign/light
		//C. Do whatever we need to do if so
		//D. Write to the console what has happened
		//We need all of the if statements to display the colors,
		//as we need to convert from IPixel colors to Java.awt colors for display reasons
	/*	
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
				g.drawRect(i.x + 8, i.y + 40 - 25, i.width, i.height);
			}
		}
		for(MovingBlob i : peds) {
			g.setColor(java.awt.Color.MAGENTA);
			g.drawRect(i.x + 8, i.y + 40 - 25, i.width, i.height);
		}
		*/
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
		
		//List<MovingBlob> blobs = this.pedDetect.getAllBlobs(control.getProcessedImage(), control.getImageWidth());
		//List<MovingBlob> peds = this.pedDetect.detect(control.getProcessedImage(), control.getImageWidth());
		//this.currentBlobs = blobs;
		//this.currentPeds = peds;
		
		/*
		List<MovingBlob> blobs = this.pedDetect.getAllBlobs(control.getProcessedImage(), control.getImageWidth());
		List<MovingBlob> peds = this.pedDetect.detect(control.getProcessedImage(), control.getImageWidth());
		this.currentBlobs = blobs;
		this.currentPeds = peds;
		
		for (MovingBlob blob: currentBlobs) {
			if (detectStopSign(blob)) {
				if (!blob.seen) {
					stopType = 1;	
				}
				
				blob.type = "Stop";
				determineStop(blob);
				System.out.println("Found a stopsign: " + blob);
				blob.seen = true;
			}
			if (detectLight(blob) == 1) {
				if (!blob.seen) {
					stopType = 2;	
				}
				
				blob.type = "StopLightWidth";
				determineStop(blob);
				System.out.println("Found a " + blob.color.getColor() + "light: " + blob);
				blob.seen = true;
			}
			else if (detectLight(blob) == 2) {
				if (!blob.seen) {
					stopType = 3;	
				}
				
				blob.type = "StopLightWidth";
				determineStop(blob);
				System.out.println("Found a " + blob.color.getColor() + "light: " + blob);
				blob.seen = true;
			}
			else if (detectLight(blob) == 3) {
				stopType = 0;
				
				blob.type = "StopLightWidth";
				System.out.println("Found a " + blob.color.getColor() + "light: " + blob);
				blob.seen = true;
			}
		}
		
//		for(MovingBlob blob : currentPeds) {
			//if (determinePedStop(blob)) {
			//	determineStop(blob, 4);
			//}
//		}
		
		if (emergencyStop) {
			stopType = 5;
			
			System.out.println("EMERGENCY STOP");
			
			determineStop(currentBlobs.get(0)); //This is bad code. I do it so that I can call determineStop without a blob
		}
	}
	*/
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
	/*
	public boolean detectBlobOverlappingBlob(MovingBlob outsideBlob, MovingBlob insideBlob) {
		return (insideBlob.x < outsideBlob.x + outsideBlob.width && 
				insideBlob.width + insideBlob.x > outsideBlob.x) || 
				(insideBlob.y < outsideBlob.y + outsideBlob.height && 
				insideBlob.height + insideBlob.y > outsideBlob.y);
	}
	*/
	/**
	 * A method that determines what speed we need to be traveling at given our wheel angle, and how we have
	 * modified our speed by pressing the arrow keys.
	 * 
	 * <p>Also checks whether it has been told to stop at a stopsign or stoplight, and acts accordingly,
	 * slowing if it needs to slow, and stopping when it needs t stop.
	 * 
	 * @param wheelAngle our current wheel angle
	 * @paramm manualSpeed our modifier for speed based upon arrow key presses
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
	/*
	//Calculates when the car should start to stop, then reduces its speed.
	private void determineStop(MovingBlob closestBlob) {
		if (stopType != 0) {
		if (!closestBlob.seen && stopType == 1) {
			frameWait = Constants.WAIT_AT_STOPSIGN_FRAMES;
			System.out.println("Set wait frames");
		}
		frameWait -= 1;
		
		double blobRealSize = getStopReal(closestBlob); //Gets real size
		double distToBlob = cameraCalibrator.distanceToObj(blobRealSize/cameraCalibrator.relativeWorldScale, closestBlob.width); //Finds distance to closest blob based on real wrold size and pixel size			System.out.println("WEIRD STUFF HAPPENS HERE");
		
		System.out.println("frameWait: " + frameWait);
		System.out.println("stopType: " + stopType);
		System.out.println("desiredSpeed: " + desiredSpeed);
		//System.out.println("getEstimatedSpeed: " + getEstimatedSpeed());
		//System.out.println("distToBlob: " + distToBlob);
		//System.out.println(desiredSpeed - cameraCalibrator.calcStopRate(getEstimatedSpeed(), cameraCalibrator.getStopTime(distToBlob, getEstimatedSpeed())));
		
//		this.desiredSpeed = desiredSpeed - cameraCalibrator.calcStopRate(getEstimatedSpeed(), cameraCalibrator.getStopTime(distToBlob, getEstimatedSpeed()));
		
		if (frameWait == 0) {
			stopType = 0;
		}
		}
	}
	
	//Returns the real size of the object to find distance to it
	private double getStopReal(MovingBlob stopBlob) {
		return sizeCons.SIGN_INFO.get(stopBlob.type).get(1);
	}
	*/
	//Returns the estimated speed IN METERS PER SECOND
	public double getEstimatedSpeed() {
		return currentEstimatedSpeed * Constants.PIN_TO_METER_PER_SECOND;
	}
	
	//Updates the estimated speed
	public void calculateEstimatedSpeed(int gasAmount) {
		currentEstimatedSpeed = gasAmount;
	}
	/*
	//Getting and setting our emergency stop boolean
	public boolean getEmergencyStop() {
		return emergencyStop;
	}
	
	public void setEmergencyStop(boolean emer) {
		this.emergencyStop = emer;
	}
	*/
	public int getDesiredSpeed() {
		return (int) desiredSpeed;
	}

	@Override
	public void paint(CarControl control, Graphics g) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Checks a given blob for the properties of a stopsign (size, age, position, color)
	 * 
	 * <p>These properties are stored in the blob, and you probably will not need to worry about setting them.
	 * 
	 * @param blob the blob that we want to check
	 * @return true if the blob is recognized to be a stopsign, otherwise false
	 */
	/*
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
			/*!blob.seen &&*//*
			(((double) blob.height / (double) blob.width) < 1 + Constants.BLOB_RATIO_DIF &&
			((double) blob.height / (double) blob.width) > 1 - Constants.BLOB_RATIO_DIF)) {
			
			return true;
		}
		
		return false;
	}
	
	public boolean determinePedStop(MovingBlob ped) {
		//System.out.println("Ped Width "+ped.width+" Ped X "+ped.x+" Ped Y "+ped.y);
		if(ped.width >= Constants.PED_MIN_SIZE &&
			ped.x >= Constants.PED_MIN_X &&
			ped.x <= Constants.PED_MAX_X) {
			System.out.println(ped);
			return true;
		}
		return false;
	}
	
	public List<MovingBlob> getBlobs() {
		return this.currentBlobs;
	}
	*/
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
	/*
	public int detectLight(MovingBlob blob) {
		int lightColor = 0;
		
		if (blob.age >= Constants.BLOB_AGE &&
			blob.height >= (2) + Constants.BLOB_MIN_HEIGHT &&
			blob.height <= (2) + Constants.BLOB_MAX_HEIGHT &&
			blob.width >= Constants.BLOB_MIN_WIDTH &&
			blob.width <= Constants.BLOB_MAX_WIDTH &&
			blob.x >= Constants.STOPLIGHT_MIN_X &&
			blob.x <= Constants.STOPLIGHT_MAX_X &&
			blob.y >= Constants.STOPLIGHT_MIN_Y &&
			blob.y <= Constants.STOPLIGHT_MAX_Y &&
			blob.color.getColor() == Color.RED &&
			/*!blob.seen &&*//*
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
			/*!blob.seen &&*//*
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
			/*!blob.seen &&*//*
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
	*/
}

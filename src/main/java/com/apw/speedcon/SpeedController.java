package com.apw.speedcon;

import com.apw.apw3.TrakSim;
import com.apw.drivedemo.DriveTest;
import com.apw.imagemanagement.ImageManager;
import com.apw.pedestrians.PedestrianDetector;
import com.apw.pedestrians.blobtrack.MovingBlob;
import com.apw.pedestrians.image.Color;
import java.awt.Graphics;
import java.util.List;

public class SpeedController {

  TrakSim trackSim = new TrakSim();
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
  private SpeedFinder speedFinder;

  public SpeedController() {
    this.speedFinder = new SpeedFinder();
  }

  //A method to be called every frame. Calculates desired speed and actual speed
  //Also takes stopping into account
  public void onUpdate(int gasAmount, int steerDegs, int manualSpeed, Graphics graf,
      DriveTest dtest, boolean blobsOn, boolean overlayOn) {
    if (cyclesUntilCanDetectStopsign > 0) {
      cyclesUntilCanDetectStopsign--;
    }
    dtest.run();
    //dtest.window.paint(graf);
    this.calculateEstimatedSpeed(gasAmount);
    this.calculateDesiredSpeed(steerDegs, manualSpeed);

    //This part runs on-screen blobs thru a set of tests to figure out if they are
    //relevant, and then what to do with them
    PedestrianDetector pedDetect = new PedestrianDetector();
    ImageManager imageManager = dtest.getImgManager();
    List<MovingBlob> blobs = pedDetect.getAllBlobs(imageManager.getSimpleColorRaster(), 912);
    for (MovingBlob i : blobs) {
      if (blobsOn) {
        if (i.color.getColor() == Color.BLACK) {
          graf.setColor(java.awt.Color.BLACK);
        } else if (i.color.getColor() == Color.GREY) {
          graf.setColor(java.awt.Color.GRAY);
        } else if (i.color.getColor() == Color.WHITE) {
          graf.setColor(java.awt.Color.WHITE);
        } else if (i.color.getColor() == Color.RED) {
          graf.setColor(java.awt.Color.RED);
        } else if (i.color.getColor() == Color.GREEN) {
          graf.setColor(java.awt.Color.GREEN);
        } else if (i.color.getColor() == Color.BLUE) {
          graf.setColor(java.awt.Color.BLUE);
        }
        graf.drawRect(i.x + 8, i.y + 40, i.width, i.height);
      }
      if (detectRedLight(i)) {
        System.out.println("Red light blob " + i);
        setStoppingAtLight();
      } else if (detectYellowLight(i)) {
        System.out.println("Yellow light blob " + i);
      } else if (detectGreenLight(i)) {
        System.out.println("Green light blob " + i);
        readyToGo();
      } else if (detectStopSign(i) && cyclesUntilCanDetectStopsign <= 0) {
        System.out.println("Stop sign blob " + i);
        cyclesUntilCanDetectStopsign = 100;
        setStoppingAtSign();
      } else {
        System.out.println("Blob " + i);
        System.out.println("Blob " + i.color.getColor());
      }
    }


  }

  //This figures out the speed that we want to be traveling at
  public void calculateDesiredSpeed(int wheelAngle, int manualSpeed) {
    double curveSteepness = 0; // steering.getCurveSteepness();
    int shouldStopSign = this.updateStopSign();
    int shouldStopLight = this.updateStopLight();

    //Logic for determining if we need to be slowing down due to a roadsign/light, and why
    if (shouldStopSign == 1 && shouldStopLight == 1) {
      this.desiredSpeed = Math.min(
          Math.max((1 - Math.abs((double) (wheelAngle) / 90.0)) * Constants.MAX_SPEED + manualSpeed,
              Constants.MIN_SPEED), Constants.MAX_SPEED);
    } else if (shouldStopSign == -1) {
      this.desiredSpeed = Constants.STOPSIGN_DRIFT_SPEED;
    } else if (shouldStopSign == 0) {
      this.desiredSpeed = 0;
    } else if (shouldStopLight == -1) {
      this.desiredSpeed = Constants.STOPLIGHT_DRIFT_SPEED;
    } else if (shouldStopLight == 0) {
      this.desiredSpeed = 0;
    }
    if (this.emergencyStop) {
      this.desiredSpeed = 0;
    }
  }

  //Returns the estimated speed IN METERS PER SECOND
  public double getEstimatedSpeed() {
    return currentEstimatedSpeed * Constants.PIN_TO_METER_PER_SECOND;
  }

  //Updates the estimated speed
  public void calculateEstimatedSpeed(int gasAmount) {
    currentEstimatedSpeed = gasAmount;
  }

  //To be called every frame. Checks if we need to be stopping at a stopsign
  //By modifying constants in the Constants.java in speedcon, you can adjust how the stopping
  // behaves
  //Can be triggered by pressing 'P'
  public int updateStopSign() {
    if (stoppingAtSign) {
      if (cyclesToStopAtSign <= 0) {
        cyclesToStopAtSign = Constants.DRIFT_TO_STOPSIGN_FRAMES;
        stoppedAtSign = true;
        stoppingAtSign = false;
        cyclesToGo = Constants.WAIT_AT_STOPSIGN_FRAMES;
      } else {
        cyclesToStopAtSign--;
        return -1;
      }
    }
    if (stoppedAtSign) {
      if (cyclesToGo <= 0) {
        cyclesToGo = Constants.WAIT_AT_STOPSIGN_FRAMES;
        stoppedAtSign = false;
      } else {
        cyclesToGo--;
        return 0;
      }
    }
    return 1;
  }

  //To be called every frame. Checks if we need to be stopping at a stoplight
  //By modifying constants in the Constants.java in speedcon, you can adjust how the stopping
  // behaves
  //Can be triggered by pressing 'O', and released by pressing 'I'
  public int updateStopLight() {
    if (stoppingAtLight) {
      if (cyclesToStopAtLight <= 0) {
        cyclesToStopAtLight = Constants.DRIFT_TO_STOPLIGHT_FRAMES;
        stoppedAtLight = true;
        stoppingAtLight = false;
        readyToGo = false;
      } else {
        cyclesToStopAtLight--;
        return -1;
      }
    }
    if (stoppedAtLight) {
      if (readyToGo) {
        stoppedAtLight = false;
        readyToGo = false;
      } else {
        return 0;
      }
    }
    return 1;
  }

  //Triggered by pressing 'O', this tells us that we have a green light
  public void readyToGo() {
    readyToGo = true;
  }

  //Tells you if we are stopping at a sign currently
  public boolean getStoppingAtSign() {
    return stoppingAtSign;
  }

  //Tells you if we are stopping at a light currently
  public boolean getStoppingAtLight() {
    return stoppingAtLight;
  }

  //Tells us that we have detected a stopsign, and need to stop
  public void setStoppingAtSign() {
    stoppingAtSign = true;
    cyclesToStopAtSign = Constants.DRIFT_TO_STOPSIGN_FRAMES;
  }

  //Tells us that we have seen a red light, and need to stop
  public void setStoppingAtLight() {
    stoppingAtLight = true;
    cyclesToStopAtLight = Constants.DRIFT_TO_STOPLIGHT_FRAMES;
  }

  //Getting and setting our emergency stop boolean
  public boolean getEmergencyStop() {
    return emergencyStop;
  }

  public void setEmergencyStop(boolean emer) {
    this.emergencyStop = emer;
  }

  //This returns our distance from an object. Currently non-functional
  public double getDistance(double focalLength, double realObjHeight, double cameraFrameHeight,
      double objectPixelHeight, double sensorHeight) {

    return (focalLength * realObjHeight * cameraFrameHeight)
        / (objectPixelHeight * sensorHeight);

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
  double calcStopRate(double speed, double time) {
    return (0 - speed) / time;
  }


  //Function used to get the rate to lower the speed by when a stop distance is given.
  double getStopRate(double targetDist, double currentSpeed) {
    return calcStopRate(currentSpeed, getStopTime(targetDist, currentSpeed));
  }

  // End Of Brake Rate Math

  public int getDesiredSpeed() {
    return (int) desiredSpeed;
  }

  // Checks a given blob for the properties of a stopsign (size, age, position, color)
  public boolean detectStopSign(MovingBlob blob) {
    return blob.age > Constants.BLOB_AGE && blob.height > Constants.BLOB_HEIGHT
        && blob.width > Constants.BLOB_WIDTH && blob.x > Constants.STOPSIGN_MIN_X
        && blob.x < Constants.STOPSIGN_MAX_X && blob.y > Constants.STOPSIGN_MIN_Y
        && blob.y < Constants.STOPSIGN_MAX_Y && blob.color.getColor() == Color.RED;
  }

  // Checks a given blob for the properties of a stoplight (size, age, position, color)
  public boolean detectRedLight(MovingBlob blob) {
    return blob.age > Constants.BLOB_AGE && blob.height > Constants.BLOB_HEIGHT
        && blob.width > Constants.BLOB_WIDTH && blob.x > Constants.STOPLIGHT_MIN_X
        && blob.x < Constants.STOPLIGHT_MAX_X && blob.y > Constants.STOPLIGHT_MIN_Y
        && blob.y < Constants.STOPLIGHT_MAX_Y && blob.color.getColor() == Color.RED;
  }

  // Checks a given blob for the properties of a stoplight (size, age, position, color)
  public boolean detectYellowLight(MovingBlob blob) {
    return blob.age > Constants.BLOB_AGE && blob.height > Constants.BLOB_HEIGHT
        && blob.width > Constants.BLOB_WIDTH && blob.x > Constants.STOPLIGHT_MIN_X
        && blob.x < Constants.STOPLIGHT_MAX_X && blob.y > Constants.STOPLIGHT_MIN_Y
        && blob.y < Constants.STOPLIGHT_MAX_Y && blob.color.getColor() == Color.RED;
  }

  // Checks a given blob for the properties of a stoplight (size, age, position, color)
  public boolean detectGreenLight(MovingBlob blob) {
    return blob.age > Constants.BLOB_AGE && blob.height > Constants.BLOB_HEIGHT
        && blob.width > Constants.BLOB_WIDTH && blob.x > Constants.STOPLIGHT_MIN_X
        && blob.x < Constants.STOPLIGHT_MAX_X && blob.y > Constants.STOPLIGHT_MIN_Y
        && blob.y < Constants.STOPLIGHT_MAX_Y && blob.color.getColor() == Color.GREEN;
  }
}
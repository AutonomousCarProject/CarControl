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
    private boolean go;

    private PedestrianDetector pedDetect;
    private CameraCalibration cameraCalibrator;

    private List<MovingBlob> currentBlobs;
    private List<MovingBlob> stopObjects;

    public SpeedControlModule() {
        this.pedDetect = new PedestrianDetector();
        this.currentBlobs = new ArrayList<>();
        this.cameraCalibrator = new CameraCalibration();
    }

    @Override
    public void initialize(CarControl control) {
        control.addKeyEvent(KeyEvent.VK_B, () -> Settings.blobsOn ^= true);
        control.addKeyEvent(KeyEvent.VK_V, () -> Settings.overlayOn ^= true);
        control.addKeyEvent(KeyEvent.VK_F, () -> cameraCalibrator.calibrateCamera(control));
        control.addKeyEvent(KeyEvent.VK_C, () -> Settings.writeBlobsToConsole ^= true);
        control.addKeyEvent(KeyEvent.VK_S, () -> Settings.writeSpeedToConsole ^= true);
        control.addKeyEvent(KeyEvent.VK_M, () -> Settings.colorMode++);
    }

    @Override
    public void update(CarControl control) {
        onUpdate(control);
        control.accelerate(true, getNextSpeed());

        System.out.println("Speed: " + getNextSpeed());
    }

    @Override
    public void paint(CarControl control, Graphics g) {
        if (control.getProcessedImage() == null) return;

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
                if (true) {
                    if (i.color.getColor() == com.apw.pedestrians.image.Color.BLACK) {
                        g.setColor(java.awt.Color.BLACK);
                    } else if (i.color.getColor() == com.apw.pedestrians.image.Color.GREY) {
                        g.setColor(java.awt.Color.GRAY);
                    } else if (i.color.getColor() == com.apw.pedestrians.image.Color.WHITE) {
                        g.setColor(java.awt.Color.WHITE);
                    } else if (i.color.getColor() == com.apw.pedestrians.image.Color.RED) {
                        g.setColor(java.awt.Color.RED);
                    } else if (i.color.getColor() == com.apw.pedestrians.image.Color.GREEN) {
                        g.setColor(java.awt.Color.GREEN);
                    } else if (i.color.getColor() == com.apw.pedestrians.image.Color.BLUE) {
                        g.setColor(java.awt.Color.BLUE);
                    }
                    g.drawRect(i.x + 8, i.y + 40 + vEdit, i.width, i.height);
                }
            }
        }
    }

    //A method to be called every frame. Calculates desired speed and actual speed
    //Also takes stopping into account
    public void onUpdate(CarControl control) {
        int gasAmount = control.getGas();
        int steerDegs = control.getSteering();
        int manualSpeed = control.getManualSpeed();

        com.apw.pedestrians.Constant.LAST_FRAME_MILLIS = com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS;
        com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS = System.currentTimeMillis();
        com.apw.pedestrians.Constant.TIME_DIFFERENCE = com.apw.pedestrians.Constant.CURRENT_FRAME_MILLIS - com.apw.pedestrians.Constant.LAST_FRAME_MILLIS;
        this.calculateEstimatedSpeed(gasAmount);
        this.calculateDesiredSpeed(steerDegs, manualSpeed);
        this.updateStop();  //Finds things that should be stopped at and adds them to stopObjects

        List<MovingBlob> blobs = this.pedDetect.getAllBlobs(control.getProcessedImage(), 912);
        this.currentBlobs = blobs;


        for (MovingBlob i : stopObjects) {
            
            determineStop(i);

        }


        if (emergencyStop) {
            emergencyStop();
        }
    }

    public boolean detectBlobOverlappingBlob(MovingBlob outsideBlob, MovingBlob insideBlob) {
        return (insideBlob.x < outsideBlob.x + outsideBlob.width && insideBlob.width + insideBlob.x > outsideBlob.x) || (insideBlob.y < outsideBlob.y + outsideBlob.height && insideBlob.height + insideBlob.y > outsideBlob.y);
    }

    //This figures out the speed that we want to be traveling at
    public void calculateDesiredSpeed(double wheelAngle, int manualSpeed) {
        this.updateStopSign();
        this.updateStopLight();

        //Logic for determining if we need to be slowing down due to a roadsign/light, and why
        if (go == true) {
            this.desiredSpeed = Math.min(Math.max((1 - Math.abs((double) (wheelAngle) / 90.0)) * Constants.MAX_SPEED + manualSpeed, Constants.MIN_SPEED), Constants.MAX_SPEED);
        } 


    }

    public int getNextSpeed() {
        double distance = this.desiredSpeed - this.currentEstimatedSpeed;
        if (Math.abs(distance) < Constants.MIN_SPEED_INCREMENT) {
            return (int) this.desiredSpeed;
        } else if (distance < 0) {
            return (int) (this.currentEstimatedSpeed - Constants.MIN_SPEED_INCREMENT);
        } else {
            return (int) (this.currentEstimatedSpeed + Constants.MIN_SPEED_INCREMENT);
        }
    }


    //Calculates when the car should start to stop.
    private void determineStop(MovingBlob closestBlob)
    {
        double distToBlob = cameraCalibrator.distanceToObj(75, closestBlob.width);
        if(cameraCalibrator.getStopTime(distToBlob, getEstimatedSpeed()) <= Constants.MIN_STOP_TIME)
        {
            go = false;
            this.desiredSpeed = desiredSpeed - cameraCalibrator.calcStopRate(getEstimatedSpeed(), cameraCalibrator.getStopTime(distToBlob, getEstimatedSpeed()));
        }
        else
        {
            go = true;
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


    //One method that tracks all things needed
    private void updateStop()
    {
        this.updateStopLight();
        this.updateStopSign();
    }
    
    //Finds stop signs and adds them to stopSigns
    public void updateStopSign() {

        for(MovingBlob i : currentBlobs)
        {
            if(i.color.getColor() == Color.RED && detectStopSign(i))
            {
                stopObjects.add(i);
                i.type = "sign";
            }
        }
            }


    //Finds stopLights, if red or yellow, adds to stopLight list
    public void updateStopLight() {

        for(MovingBlob i : currentBlobs)
        {
            if(i.color.getColor() == Color.RED && detectLight(i, currentBlobs) <= 2)
            {
                stopObjects.add(i);
                i.type = "light";
            }
        }
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

    // Checks a given blob for the properties of a stopsign (size, age, position, color)
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

            blob.seen = true;
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

    public int detectLight(MovingBlob blob, List<MovingBlob> bloblist) {
        int lightColor = 0;
        boolean outputLight = false;
        //Figure out the color of our blob

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

            for (MovingBlob b : bloblist) {
                if (b.color.getColor() == Color.BLACK) {
                    if (detectBlobOverlappingBlob(b, blob)) {
                        //Found a red light
                        System.out.println("Found a redlight: " + blob);
                        blob.seen = true;
                        lightColor = 1;
                    }
                }
            }
        } else if (
                blob.age > Constants.BLOB_AGE &&
                        blob.height > Constants.BLOB_MIN_HEIGHT &&
                        blob.height < (1 / 2) * Constants.BLOB_MAX_HEIGHT &&
                        blob.width > Constants.BLOB_MIN_WIDTH &&
                        blob.width < (1 / 2) * Constants.BLOB_MAX_WIDTH &&
                        blob.x > Constants.STOPLIGHT_MIN_X &&
                        blob.x < Constants.STOPLIGHT_MAX_X &&
                        blob.y > Constants.STOPLIGHT_MIN_Y &&
                        blob.y < Constants.STOPLIGHT_MAX_Y &&
                        blob.color.getColor() == Color.YELLOW &&
                        !blob.seen &&
                        ((double) blob.height / (double) blob.width) < 1 + Constants.BLOB_RATIO_DIF &&
                        ((double) blob.height / (double) blob.width) > 1 - Constants.BLOB_RATIO_DIF) {

            for (MovingBlob b : bloblist) {
                if (b.color.getColor() == Color.BLACK) {
                    if (detectBlobOverlappingBlob(b, blob)) {
                        //Found a yellow light
                        System.out.println("Found a yellowlight: " + blob);
                        blob.seen = true;
                        lightColor = 2;
                    }
                }
            }
        } else if (blob.age > Constants.BLOB_AGE &&
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

            for (MovingBlob b : bloblist) {
                if (b.color.getColor() == Color.BLACK) {
                    if (detectBlobOverlappingBlob(b, blob)) {
                        //Found a green light
                        System.out.println("Found a greenlight: " + blob);
                        blob.seen = true;
                        lightColor = 3;
                    }
                }
            }
        } else {
            //Didn't find a light
            return 0;
        }
        //If we made it here, we know that we have a light
        //Therefore, we need to check if that light is inside of a black blob, aka the lamp
        outputLight = true;
        int overlaps = 0;
        for (MovingBlob b : bloblist) {
            if (b.color.getColor() == Color.BLACK) {
                if (detectBlobOverlappingBlob(b, blob)) {
                    overlaps++;
                }
            }
        }
        //System.out.println("Overlaps: " + overlaps);
        if (outputLight) {
            return lightColor;
        } else {
            return 0;
        }
    }

    public CameraCalibration getCalibrator() {
        return cameraCalibrator;
    }

    public void emergencyStop() {
        this.desiredSpeed = cameraCalibrator.calcStopRate(getEstimatedSpeed(), 0.1);
    }
}

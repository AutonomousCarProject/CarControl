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
    private boolean stoppingAtSign;
    private boolean stoppedAtSign;
    private boolean stoppingAtLight;
    private boolean stoppedAtLight;
    private boolean readyToGo;
    private boolean emergencyStop;
    private int cyclesToStopAtSign = Constants.DRIFT_TO_STOPSIGN_FRAMES;
    private int cyclesToGo;
    private int cyclesToStopAtLight = Constants.DRIFT_TO_STOPLIGHT_FRAMES;

    private PedestrianDetector pedDetect;
    private CameraCalibration cameraCalibrator;

    private List<MovingBlob> currentBlobs;

    public SpeedControlModule() {
        this.pedDetect = new PedestrianDetector();
        this.currentBlobs = new ArrayList<>();
        this.cameraCalibrator = new CameraCalibration();
    }

    @Override
    public void initialize(CarControl control) {
        control.addKeyEvent(KeyEvent.VK_B, () -> Settings.blobsOn ^= true);
        control.addKeyEvent(KeyEvent.VK_V, () -> Settings.overlayOn ^= true);
    }

    @Override
    public void update(CarControl control) {
        onUpdate(control);
        control.accelerate(true, getNextSpeed());
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

        List<MovingBlob> blobs = this.pedDetect.getAllBlobs(control.getProcessedImage(), 912);

        for (MovingBlob i : blobs) {
            /* Returns an int value corresponding to the color of the light we are looking at
             * 0 - No light
             * 1 - Red Light
             * 2 - Yellow Light
             * 3 - Green Light
             * */
            int currLight = detectLight(i, blobs);

            if (currLight == 1) {
                setStoppingAtLight();
            } else if (currLight == 2) {
                setStoppingAtLight();
            } else if (currLight == 3) {
                readyToGo();
            } else if (detectStopSign(i, blobs)) {
                System.out.println("Found a stopsign: " + i);
                setStoppingAtSign();
            } else {
            }
        }

        this.currentBlobs = blobs;

        if (emergencyStop) {
            emergencyStop();
        }
    }

    public boolean detectBlobOverlappingBlob(MovingBlob outsideBlob, MovingBlob insideBlob) {
        return (insideBlob.x < outsideBlob.x + outsideBlob.width && insideBlob.width + insideBlob.x > outsideBlob.x) || (insideBlob.y < outsideBlob.y + outsideBlob.height && insideBlob.height + insideBlob.y > outsideBlob.y);
    }

    //This figures out the speed that we want to be traveling at
    public void calculateDesiredSpeed(double wheelAngle, int manualSpeed) {
        int shouldStopSign = this.updateStopSign();
        int shouldStopLight = this.updateStopLight();

        //Logic for determining if we need to be slowing down due to a roadsign/light, and why
        if (shouldStopSign == 1 && shouldStopLight == 1) {
            this.desiredSpeed = Math.min(Math.max((1 - Math.abs((double) (wheelAngle) / 90.0)) * Constants.MAX_SPEED + manualSpeed, Constants.MIN_SPEED), Constants.MAX_SPEED);
        } else if (shouldStopSign == -1) {
            this.desiredSpeed = Constants.STOPSIGN_DRIFT_SPEED;
        } else if (shouldStopSign == 0) {
            this.desiredSpeed = 0;
        } else if (shouldStopLight == -1) {
            this.desiredSpeed = Constants.STOPLIGHT_DRIFT_SPEED;
        } else if (shouldStopLight == 0) {
            this.desiredSpeed = 0;
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

    //Returns the estimated speed IN METERS PER SECOND
    public double getEstimatedSpeed() {
        return currentEstimatedSpeed * Constants.PIN_TO_METER_PER_SECOND;
    }

    //Updates the estimated speed
    public void calculateEstimatedSpeed(int gasAmount) {
        currentEstimatedSpeed = gasAmount;
    }

    //To be called every frame. Checks if we need to be stopping at a stopsign
    //By modifying constants in the Constants.java in SpeedCon, you can adjust how the stopping behaves
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
    //By modifying constants in the Constants.java in SpeedCon, you can adjust how the stopping behaves
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

    public int getDesiredSpeed() {
        return (int) desiredSpeed;
    }

    // Checks a given blob for the properties of a stopsign (size, age, position, color)
    public boolean detectStopSign(MovingBlob blob, List<MovingBlob> bloblist) {
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

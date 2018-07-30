package com.apw.carcontrol;

import java.awt.Insets;
import java.util.HashMap;

import com.apw.apw3.DriverCons;
import com.apw.apw3.MyMath;
import com.apw.fakefirm.Arduino;
import com.apw.fly2cam.FlyCamera;

public class CamControl implements CarControl {
    private final int SteerPin, GasPin;
    private final double LefScaleSt, RitScaleSt;
    protected FlyCamera cam;
    protected HashMap<Integer, Runnable> keyBindings;
    private Arduino driveSys;
    private Insets edges;
    private byte[] cameraImage = null;
    private byte[] processedImage = null;
    private int[] renderedImage = null;
    private int currentSteering = 0;
    private int currentVelocity = 0;
    private int currentManualSpeed = 0;
    private int nrows, ncols;
	
    public CamControl() {
        cam = new FlyCamera();
        cam.Connect(4); // 30 FPS
        nrows = cam.Dimz() >> 16;
        ncols = cam.Dimz() << 16 >> 16;
        
        SteerPin = DriverCons.D_SteerServo;
        GasPin = DriverCons.D_GasServo;
        LefScaleSt = ((double) DriverCons.D_LeftSteer) / 90.0;
        RitScaleSt = ((double) DriverCons.D_RiteSteer) / 90.0;

        driveSys = new Arduino();
        driveSys.pinMode(SteerPin, Arduino.SERVO);
        driveSys.pinMode(GasPin, Arduino.SERVO);

        keyBindings = new HashMap<>();
    }

    @Override
    public byte[] readCameraImage() {
        if (cameraImage == null || (nrows * ncols * 4) != cameraImage.length) {
            cameraImage = new byte[nrows * ncols * 4];
        }
        boolean b = cam.NextFrame(cameraImage);
        if (!b) {
            System.err.println("An error occurred while reading the camera image from FlyCamera.");
        }

        return cameraImage;
    }

    @Override
    public byte[] getRecentCameraImage() {
        return cameraImage;
    }

    @Override
    public byte[] getProcessedImage() {
        return processedImage;
    }

    @Override
    public void setProcessedImage(byte[] image) {
        this.processedImage = image;
    }

    @Override
    public int[] getRGBImage() {
        return renderedImage;
    }

    /**
     * Gets the image to be rendered on the screen. Normally should not be used by any class except the renderer itself.
     *
     * @return The image to be rendered on the TrakSim window.
     */
    protected int[] getRenderedImage() {
        return renderedImage;
    }

    @Override
	public int getImageWidth() {
		return ncols;
	}

	@Override
	public int getImageHeight() {
		return nrows;
	}
    
    @Override
    public void setRenderedImage(int[] renderedImage) {
        this.renderedImage = renderedImage;
    }

    @Override
    public boolean willPaint() {
        return true;
    }


    @Override
    public void exit(int why) {
        //FlyCamera myVid = theVideo;
        try {
            accelerate(true, 0);
            steer(true, 0);
            if (cam != null) {
                cam.Finish();
            }
            if (driveSys != null) {
                driveSys.Close();
            }
        } catch (Exception ignored) {
        }
        System.out.println("-------- Clean Stop -------- " + why);
        System.exit(why);
    }

    @Override
    public void accelerate(boolean absolute, int velocity) {
        if (!absolute) {
            velocity = currentVelocity + velocity;
        }
        if (velocity != 0) {
            velocity = MyMath.iMax(MyMath.iMin(velocity, 90), -90);
            if (velocity == currentVelocity) {
                return;
            }
        }
        if (velocity == 0 && !absolute && currentVelocity == 0) {
            return;
        }

        currentVelocity = velocity;
        if (driveSys == null) {
            return;
        }
        driveSys.servoWrite(GasPin, velocity + 90);
    }

    @Override
    public void steer(boolean absolute, int angle) {
        if (!absolute) {
            angle = currentSteering + angle;
        }
        angle = MyMath.iMax(MyMath.iMin(angle, 90), -90);
        if (angle != 0) if (angle == currentSteering) return;
        currentSteering = angle;
        if (angle < 0) {
            //noinspection ConstantConditions
            if (LefScaleSt < 1.0) { // LefScaleSt = LeftSteer/90.0
                angle = (int) Math.round(LefScaleSt * ((double) angle));
            }
        } else if (angle > 0) {
            //noinspection ConstantConditions
            if (RitScaleSt > 1.0) {
                angle = (int) Math.round(RitScaleSt * ((double) angle));
            }
        }
        if (driveSys == null) {
            return;
        }
        driveSys.servoWrite(SteerPin, angle + 90);
    }

    @Override
    public void manualSpeedControl(boolean absolute, int manualSpeed) {
        if(absolute) {
            currentManualSpeed = manualSpeed;
        }
        else {
            currentManualSpeed += manualSpeed;
        }
    }

    @Override
    public int getGas() {
        return currentVelocity;
    }

    @Override
    public int getSteering() {
        return currentSteering;
    }

    @Override
    public int getManualSpeed() {
        return currentManualSpeed;
    }

    @Override
    public Insets getEdges() {
        return edges;
    }

    @Override
    public void setEdges(Insets edges) {
        this.edges = edges;
    }

    @Override
    public void rectFill(int colo, int rx, int cx, int rz, int c) {
        /*Might be implemented*/
    }

    @Override
    public void addKeyEvent(int keyCode, Runnable action) {
        keyBindings.put(keyCode, action);
    }

	

}
package com.apw.carcontrol;

import com.apw.apw3.DriverCons;
import com.apw.apw3.MyMath;
import com.apw.apw3.SimCamera;
import com.apw.fly2cam.FlyCamera;
import com.apw.sbcio.PWMController;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;

public class CarControlBase implements CarControl {
    protected final int SteerPin, GasPin;
    protected final double LefScaleSt, RitScaleSt;
    protected FlyCamera cam;
    protected HashMap<Integer, Runnable> keyBindings;
    protected PWMController driveSys;
    private Insets edges;
    private byte[] cameraImage = null;
    private byte[] processedImage = null;
    private int[] rgbImage = null;
    private int[] renderedImage = null;
    private int currentSteering = 0;
    private int currentVelocity = 0;
    private int currentManualSpeed = 0;
    private int windowWidth;
    private int windowHeight;
    private int imageWidth = 912;
    private int imageHeight = 480;
    private ArrayList<ColoredLine> lines;
    private ArrayList<ColoredRect> rects;
    private int futureSteeringAngle = 0;

    public CarControlBase(FlyCamera camera, PWMController drivesys) {
        cam = camera;
        cam.Connect(4); // 30 FPS

        SteerPin = DriverCons.D_SteerServo;
        GasPin = DriverCons.D_GasServo;
        LefScaleSt = ((double) DriverCons.D_LeftSteer) / 90.0;
        RitScaleSt = ((double) DriverCons.D_RiteSteer) / 90.0;

        imageHeight = cam.Dimz() >> 16;
        imageWidth = cam.Dimz() << 16 >> 16;
        
        driveSys = drivesys;

        keyBindings = new HashMap<>();

        lines = new ArrayList<>();
        rects = new ArrayList<>();
    }

    @Override
    public byte[] readCameraImage() {
        int nrows = imageHeight;
        int ncols = imageWidth;
        if (cameraImage == null || (nrows * ncols * 4) != cameraImage.length) {
            cameraImage = new byte[nrows * ncols * 4];
        }
        boolean b = cam.NextFrame(cameraImage);
        if (!b) {
            throw new IllegalStateException("Error reading image from camera!");
        }

//        processedImage = null;
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
        return rgbImage;
    }
    
    @Override
    public void setRGBImage(int[] rgbImage) {
        this.rgbImage = rgbImage;
    }

    /**
     * Gets the image to be rendered on the screen. Normally should not be used by any class except the renderer itself.
     *
     * @return The image to be rendered on the TrakSim window.
     */
    @Override
    public int[] getRenderedImage() {
        return renderedImage;
    }
    
    @Override
	public int getImageWidth() {
		return imageWidth;
	}

	@Override
	public int getImageHeight() {
		return imageHeight;
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
                driveSys.close();
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
        }
        if (velocity == 0 && !absolute && currentVelocity == 0) {
            return;
        }

        currentVelocity = velocity;
        if (driveSys != null) {
            driveSys.setServoAngle(GasPin, velocity + 90);
        }
    }

    @Override
    public void steer(boolean absolute, int angle) {
        if (!absolute) {
            angle = currentSteering + angle;
        }

        currentSteering = angle;

        if (driveSys != null) {
            driveSys.setServoAngle(SteerPin, angle + 90);
        }
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
    public int getVelocity() {
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
    public double getPosition(boolean horizontal) {
    	return cam.getPosition(horizontal);
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
        cam.fillRectangle(colo, rx, cx, rz, c);
    }
    
    @Override
    public void drawLine(int color, int x1, int y1, int x2, int y2) {
        lines.add(new ColoredLine(x1, y1, x2, y2, color));
    }

    @Override
    public void setFutureSteeringAngle(int futureSteeringAngle) {
        this.futureSteeringAngle = futureSteeringAngle;
    }

    @Override
    public ArrayList<ColoredLine> getLines() {
        return lines;
    }

    @Override
    public ArrayList<ColoredRect> getRects() {
        return rects;
    }

    @Override
    public void clearLines() {
        lines.clear();
    }

    @Override
    public void clearRects() {
        rects.clear();
    }

    @Override
    public void addKeyEvent(int keyCode, Runnable action) {
        keyBindings.put(keyCode, action);
    }
    
    @Override
    public byte getTile() {
    	return (byte) (cam.PixTile()-1);
    }

    @Override
    public void updateWindowDims(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    @Override
    public int getWindowHeight() {
        return windowHeight;
    }

    @Override
    public int getWindowWidth() {
        return windowWidth;
    }
}

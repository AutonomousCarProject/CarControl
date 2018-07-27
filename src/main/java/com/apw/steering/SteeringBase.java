package com.apw.steering;

import com.apw.apw3.DriverCons;

import java.util.ArrayList;
import java.util.List;

/**
 * Steering base is the base code for all child steering objects.
 *
 * @author kevin
 * @author carl
 * @author nathan
 */
public abstract class SteeringBase implements Steerable {
    final int cameraWidth = 640; // How wide the camera is (pixels)
    final int screenHeight = DriverCons.D_ImHi; // How tall the camera is (pixels)
    final int screenWidth = 912; // How wide the screen is (including maps)
    public int startTarget = 0;
    public int endTarget = 0;
    public Point steerPoint = new Point(0, 0); // Point to where the car attempts to steer towards
    public List<Point> leftPoints = new ArrayList<>();
    public List<Point> rightPoints = new ArrayList<>();
    public List<Point> midPoints = new ArrayList<>();
    Point origin = new Point(cameraWidth / 2, screenHeight);
    boolean usePID = true;
    private double integral, // The integral of the
            previousError;  // PID

    @Override
    public double curveSteepness(double turnAngle) {
        return Math.abs(turnAngle) / (45);
    }

    /**
     * Gets the degree offset between the origin (center bottom of the screen), and the steerPoint (The point
     * where the car is attempting to steer towards).
     *
     * @return the degreeOffset, as a integer.
     */
    public int getDegreeOffset() {
        int xOffset = origin.x - steerPoint.x;
        int yOffset = Math.abs(origin.y - steerPoint.y);

        int tempDeg = (int) (Math.atan2(-xOffset, yOffset) * (180 / Math.PI));

        //System.out.println("\n\n\n" + tempDeg + " " + myPID() + "\n\n\n");
        return (int) ((Math.atan2(-((usePID && (curveSteepness(tempDeg) > 0.3)) ? myPID() : xOffset), yOffset)) * (180 / Math.PI));
    }

    /**
     * Calculate the PID to help smooth out the car path.
     *
     * @return The Pid for the car
     */
    private double myPID() {

        int error = origin.x - steerPoint.x;
        double kP = 0.65;
        double kI = 1;
        double kD = 1;
        double derivative;


        integral += error * DriverCons.D_FrameTime;
        if (error == 0 || (Math.abs(error - previousError) == (Math.abs(error) + Math.abs(previousError)))) {
            integral = 0;
        }
        if (Math.abs(integral) > 100) {
            integral = 0;
        }

        derivative = (error - previousError) / DriverCons.D_FrameTime;
        previousError = error;
        return error * kP + integral * kI + derivative * kD;

    }

    /**
     * Find, and process the image data to assign Points to leftPoints, rightPoints, and midPoints.
     *
     * @param pixels An array of pixels
     */
    public abstract void findPoints(int[] pixels);
}
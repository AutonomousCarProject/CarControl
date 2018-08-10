package com.apw.steering;
import com.apw.apw3.DriverCons;

import java.util.ArrayList;
import java.util.List;

/**
 * steering base is the base code for all child steering objects.
 *
 * @author kevin
 * @author carl
 * @author Nathan Ng ©2018
 */
public abstract class SteeringBase implements Steerable {
    int startTarget = 0;
    int endTarget = 0;
    int cameraHeight = 0;
    int cameraWidth = 0;
    int screenWidth = 912;
    Point origin;
    Point steerPoint = new Point(0, 0); // Point to where the car attempts to steer towards
    List<Point> leftPoints = new ArrayList<>();
    List<Point> rightPoints = new ArrayList<>();
    List<Point> midPoints = new ArrayList<>();
    private boolean usePID = true;
    private double integral, // The integral of the
            previousError;  // PID
    private List<Double> posLog = new ArrayList<>(); // Array list for logging positions fed into it

    /**
     * How steep the curve is
     * @param turnAngle angle from center line to furthest point
     * @return How steep the curve is, (0-1)
     */
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
    int getDegreeOffset() {
        int xOffset = origin.x - steerPoint.x;
        int yOffset = Math.abs(origin.y - steerPoint.y);

        int tempDeg = (int) (Math.atan2(-xOffset, yOffset) * (180 / Math.PI));

        //System.out.println("\n\n\n" + tempDeg + " " + myPID() + "\n\n\n");
        return (int) ((Math.atan2(-(usePID ? myPID() : xOffset), yOffset)) * (180 / Math.PI));
    }

    /**
     * Calculate the PID to help smooth out the car path.
     *
     * @return The Pid for the car
     */
    private double myPID() {

        int error = origin.x - steerPoint.x;
        double kP = 0.8;
        double kI = 0;
        double kD = 0;
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


    /**
     * Adds the car's current x coordinate, y coordinate, and heading to the posLog arrayList
     *
     * @param x         x coordinate
     * @param y         y coordinate
     * @param heading   car's current heading (degrees)
     */
    public void updatePosLog(double x, double y, double heading) { // Reference positions by doing point# * 3 + (0 for x, 1 for y, 2 for heading)
        posLog.add(x);
        posLog.add(y);
        posLog.add(heading);
    }
}

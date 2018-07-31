package com.apw.steering;
import com.apw.apw3.DriverCons;
import com.apw.carcontrol.CarControl;

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
    public int startTarget = 0;
    public int endTarget = 0;
    public Point steerPoint = new Point(0, 0); // Point to where the car attempts to steer towards
    public List<Point> leftPoints = new ArrayList<>();
    public List<Point> rightPoints = new ArrayList<>();
    public List<Point> midPoints = new ArrayList<>();
    public int cameraHeight = 0;
    public int cameraWidth = 0;
    public int screenWidth = 912;
    Point origin;
    boolean usePID = true;
    private double integral, // The integral of the
            previousError;  // PID
    public Point[] leftEdge;
    public Point[] rightEdge;
    public Point[] pathTraveled;
    List<Double> posLog = new ArrayList<>(); // Array list for logging positions fed into it

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
        double kP = 1;
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
     * Not sure what this does
     *
     * @param x       x coordinate
     * @param y       y coordinate
     * @param heading idk
     */
    public void updatePosLog(double x, double y, double heading) { // Reference positions by doing point# * 3 + (0 for x, 1 for y, 2 for heading)
        posLog.add(x);
        posLog.add(y);
        posLog.add(heading);
    }

    public void drawMapArrays() {
        int length = posLog.size() / 3;
        double laneWidth = 4; // Needs to be measured
        leftEdge = new Point[length];
        rightEdge = new Point[length];
        pathTraveled = new Point[length];
        for (int i = 0; i < length; i++) {
            leftEdge[i] = new Point(0, 0);
            rightEdge[i] = new Point(0, 0);
            pathTraveled[i] = new Point(0, 0);
        }
        for (int i = 0; i <= length; i++) {
            leftEdge[i].x = (int) (posLog.get(i * 3) + laneWidth / 2 * Math.cos(posLog.get(i * 3 + 2) + (Math.PI / 2)));
            leftEdge[i].y = (int) (posLog.get(i * 3 + 1) + laneWidth / 2 * Math.sin(posLog.get(i * 3 + 2) + (Math.PI / 2)));
            rightEdge[i].x = (int) (posLog.get(i * 3) + laneWidth / 2 * Math.cos(posLog.get(i * 3 + 2) - (Math.PI / 2)));
            rightEdge[i].y = (int) (posLog.get(i * 3 + 1) + laneWidth / 2 * Math.sin(posLog.get(i * 3 + 2) - (Math.PI / 2)));
            pathTraveled[i].x = (int) ((double) posLog.get(i * 3));
            pathTraveled[i].y = (int) ((double) posLog.get(i * 3 + 1));
        }
    }
}
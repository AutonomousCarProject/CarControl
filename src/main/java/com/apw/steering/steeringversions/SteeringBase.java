package com.apw.steering.steeringversions;
import com.apw.apw3.DriverCons;

import com.apw.carcontrol.CarControl;
import com.apw.steering.steeringclasses.Point;
import com.apw.steering.Steerable;
import java.util.ArrayList;
import java.util.List;
import static com.apw.steering.SteeringConstants.MAX_DIST_LOOK;
import static com.apw.steering.SteeringConstants.MIN_DIST_LOOK;
import static com.apw.steering.SteeringConstants.USE_PID;
import static com.apw.steering.SteeringConstants.K_D;
import static com.apw.steering.SteeringConstants.K_I;
import static com.apw.steering.SteeringConstants.K_P;

/**
 * steering base is the base code for all child steering objects.
 *
 * @author kevin
 * @author carl
 * @author Nathan Ng ©2018
 */
public abstract class SteeringBase implements Steerable {
    private int startTarget = 0;
    private int endTarget = 0;
    private final int cameraHeight;
    private final int cameraWidth;
    private final int screenWidth;
    private final Point origin;
    private Point steerPoint = new Point(0, 0); // Point to where the car attempts to steer towards
    private List<Point> leftPoints = new ArrayList<>();
    private List<Point> rightPoints = new ArrayList<>();
    private List<Point> midPoints = new ArrayList<>();
    private List<Double> posLog = new ArrayList<>(); // Array list for logging positions fed into it
    private double integral, // The integral of the
            previousError;  // PID

    public SteeringBase(int cameraWidth, int cameraHeight, int screenWidth) {
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
        this.screenWidth = screenWidth;
        origin = new Point(cameraWidth / 2, cameraHeight);
    }

    public SteeringBase(CarControl control) {
        this.cameraWidth = control.getImageWidth();
        this.cameraHeight = control.getImageHeight();
        this.screenWidth = this.cameraWidth;
        origin = new Point(cameraWidth / 2, cameraHeight);
    }

    public SteeringBase() {
        cameraWidth = 0;
        cameraHeight = 0;
        screenWidth = 0;
        origin = new Point(0, 0);
    }

    /**
     * How steep the curve is
     * @param turnAngle angle from center line to furthest point
     * @return How steep the curve is, (0-1)
     */
    @Override
    public double curveSteepness(double turnAngle) {
        return Math.abs(turnAngle) / (45);
    }

    public double getFutureSteepness(Point furthestPoint) {
        double furthestPointXOffset = Math.round(furthestPoint.getX() - steerPoint.getX());
        return Math.atan(furthestPointXOffset / steerPoint.getY()) * (180 / Math.PI);
    }

    /**
     * Gets the degree offset between the origin (center bottom of the screen), and the steerPoint (The point
     * where the car is attempting to steer towards).
     *
     * @return the degreeOffset, as a integer.
     */
    public int getDegreeOffset(Point refPoint, Point offPoint) {
        int xOffset = refPoint.x - offPoint.x;
        int yOffset = Math.abs(refPoint.y - offPoint.y);

        int tempDeg = (int) (Math.atan2(-xOffset, yOffset) * (180 / Math.PI));

        return (int) ((Math.atan2(-((USE_PID && (curveSteepness(tempDeg) > 0.3)) ? myPID() : xOffset), yOffset)) * (180 / Math.PI));
    }

    /**
     * Calculate the PID to help smooth out the car path.
     *
     * @return The Pid for the car
     */
    private double myPID() {

        int error = origin.x - steerPoint.x;
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
        return error * K_P + integral * K_I + derivative * K_D;

    }

    public Point calculateSteerPoint() {
        setStartTarget((int) (getMidPoints().size() * MIN_DIST_LOOK));
        setEndTarget((int) (getMidPoints().size() * MAX_DIST_LOOK));

        double ySum = 0;
        double xSum = 0;
        int count = 0;

        // Sum the x's and the y's
        for (int idx = getStartTarget(); idx < getEndTarget(); idx++) {
            xSum += getMidPoints().get(idx).x;
            ySum += getMidPoints().get(idx).y;
            count++;
        }

        int x = (int) (xSum / count);
        int y = (int) (ySum / count);
        return new Point(x, y);
    }

    /**
     * Adds the car's current x coordinate, y coordinate, and heading to the posLog arrayList
     *
     * @param x         x coordinate
     * @param y         y coordinate
     * @param heading   car's current heading (degrees)
     */
    private void updatePosLog(double x, double y, double heading) { // Reference positions by doing point# * 3 + (0 for x, 1 for y, 2 for heading)
        posLog.add(x);
        posLog.add(y);
        posLog.add(heading);
    }

    public int getStartTarget() {
        return startTarget;
    }

    public void setStartTarget(int startTarget) {
        this.startTarget = startTarget;
    }

    public int getEndTarget() {
        return endTarget;
    }

    public void setEndTarget(int endTarget) {
        this.endTarget = endTarget;
    }

    public int getCameraHeight() {
        return cameraHeight;
    }

    public int getCameraWidth() {
        return cameraWidth;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public Point getOrigin() {
        return origin;
    }

    public Point getSteerPoint() {
        return steerPoint;
    }

    public void setSteerPoint(Point steerPoint) {
        this.steerPoint = steerPoint;
    }

    public List<Point> getLeftPoints() {
        return leftPoints;
    }

    public void setLeftPoints(List<Point> leftPoints) {
        this.leftPoints = leftPoints;
    }

    public List<Point> getRightPoints() {
        return rightPoints;
    }

    public void setRightPoints(List<Point> rightPoints) {
        this.rightPoints = rightPoints;
    }

    public List<Point> getMidPoints() {
        return midPoints;
    }

    public void setMidPoints(List<Point> midPoints) {
        this.midPoints = midPoints;
    }

    public List<Double> getPosLog() {
        return posLog;
    }

    public void setPosLog(List<Double> posLog) {
        this.posLog = posLog;
    }

    public double getIntegral() {
        return integral;
    }

    public void setIntegral(double integral) {
        this.integral = integral;
    }

    public double getPreviousError() {
        return previousError;
    }

    public void setPreviousError(double previousError) {
        this.previousError = previousError;
    }
}

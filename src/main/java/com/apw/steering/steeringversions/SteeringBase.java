package com.apw.steering.steeringversions;
import com.apw.apw3.DriverCons;

import com.apw.carcontrol.CarControl;
import com.apw.steering.steeringclasses.Point;
import com.apw.steering.Steerable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
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
    @Getter @Setter
    private int startTarget = 0;
    @Getter @Setter
    private int endTarget = 0;
    @Getter
    private final int cameraHeight;
    @Getter
    private final int cameraWidth;
    @Getter
    private final int screenWidth;
    @Getter
    private final Point origin;
    @Getter @Setter
    private Point steerPoint = new Point(0, 0); // Point to where the car attempts to steer towards
    @Getter @Setter
    private List<Point> leftPoints = new ArrayList<>();
    @Getter @Setter
    private List<Point> rightPoints = new ArrayList<>();
    @Getter @Setter
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

        // Sum the x's and the y's
        for (int idx = getStartTarget(); idx < getEndTarget(); idx++) {
            xSum += getMidPoints().get(idx).x;
            ySum += getMidPoints().get(idx).y;
        }

        int x = (int) (xSum / (getEndTarget() - getStartTarget()));
        int y = (int) (ySum / (getEndTarget() - getStartTarget()));
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
}

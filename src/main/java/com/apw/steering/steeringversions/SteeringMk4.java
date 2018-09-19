package com.apw.steering.steeringversions;

import com.apw.carcontrol.CarControl;
import com.apw.steering.steeringclasses.LaneLine;
import com.apw.steering.steeringclasses.Point;
import java.util.ArrayList;
import static com.apw.steering.SteeringConstants.DEFAULT_ROAD_WIDTH;
import static com.apw.steering.SteeringConstants.LOOK_DIST;
import static com.apw.steering.SteeringConstants.MINIMUM_RELIABLE_OFFSET;
import static com.apw.steering.SteeringConstants.PREVIOUS_SLOPES;
import static com.apw.steering.SteeringConstants.SEARCH_OFFSET;
import static com.apw.steering.SteeringConstants.START_SEARCH;
import static com.apw.steering.SteeringConstants.START_SLOPE;
import static com.apw.steering.SteeringConstants.USE_NO_LANE_DETECTION;

/**
 * SteeringMk4 is remarkably good at detecting lines in the road. It calculates the average width of the road
 * at each respective Y position. This allows it to know how wide a road is, even if only a single line is detected.
 * It is also much more organized compared to SteeringMk2
 *
 * @see com.apw.steering.Steerable
 * @see SteeringMk2
 * @see SteeringBase
 * @author kevin
 */
public class SteeringMk4 extends SteeringBase {
    // Constants

    private Point calcSlopePoint1 = new Point();
    private Point calcSlopePoint2 = new Point();
    private float slope; // Slope of the equation that is equal to road width, with respect to screen height.
    private LaneLine rightLine = new LaneLine(); // LaneLine that contains the left line.
    private LaneLine leftLine = new LaneLine(); // LaneLine that contains the right line.
    private ArrayList<Float> pastSlopes = new ArrayList<>(); // ArrayList that contains the past few slopes


    /**
     * Constructor used for trakSim
     *
     * @param cameraWidth Width of the camera (Excluding map on right side)
     * @param cameraHeight Height of the window
     * @param screenWidth Width of the Window
     */
    public SteeringMk4(int cameraWidth, int cameraHeight, int screenWidth) {
        super(cameraWidth, cameraHeight, screenWidth);
        for (int idx = 0; idx < PREVIOUS_SLOPES; idx++) {
            pastSlopes.add(START_SLOPE);
        }
    }

    /**
     * Constructor used for actual car
     * @param control the control used to control the car.
     */
    public SteeringMk4(CarControl control) {
        super(control);
        for (int idx = 0; idx < PREVIOUS_SLOPES; idx++) {
            pastSlopes.add(START_SLOPE);
        }
    }

    /**
     * Calculates, and Returns the Steering Angle between the steerPoint, and the center of the camera.
     * @param pixels the Array of pixels containing the screen pixels.
     * @return The degree offset
     */
    @Override
    public int getSteeringAngle(int[] pixels) {
        leftLine.clearPoints();
        rightLine.clearPoints();
        findLaneLines(pixels, getCameraHeight() - START_SEARCH, getCameraWidth() / 2, getCameraWidth() / 2);
        setMidPoints(calculateMidPoints());
        setSteerPoint(calculateSteerPoint());
        setLeftPoints(leftLine.getLanePoints());
        setRightPoints(rightLine.getLanePoints());
        return getDegreeOffset(getOrigin(), getSteerPoint());
    }

    /**
     * Recursive method that searches for lane lines detected in the image.
     * Each call to findLaneLines will calculate one row of pixels.
     * @param pixels The image containing the road
     * @param row The current Y-level the method is searching in in the image.
     * @param leftStartingX The current X-level the method begins searching out from to find a white pixel (On the Left)
     * @param rightStartingX The current X-level the method begins searching out from to find a white pixel (on the right)
     */
    private void findLaneLines(int[] pixels, int row, int leftStartingX, int rightStartingX) {
        if (row > getCameraHeight() * LOOK_DIST) {
            Point lastLeftPoint  = leftLine.getLeftPoint(pixels, row, getScreenWidth(), leftStartingX);
            Point lastRightPoint = rightLine.getRightPoint(pixels, row, getScreenWidth(), rightStartingX, getCameraWidth());
            leftLine.getLanePoints().add(lastLeftPoint);
            rightLine.getLanePoints().add(lastRightPoint);

            // If no lane was found, search from the last startingX
            if (lastLeftPoint.isEmpty() && lastRightPoint.isEmpty()) {
                findLaneLines(pixels, row - 1, leftStartingX, rightStartingX);

            // If Only the right lane was found, right starts from last starting X. Left starts from last startingX
            } else if (lastLeftPoint.isEmpty()) {
                int startSearchX = lastRightPoint.getX() - SEARCH_OFFSET;
                findLaneLines(pixels, row - 1, startSearchX, startSearchX);

            // If Only the left lane was found, left starts from last starting X. Right starts from last startingX
            } else if (lastRightPoint.isEmpty()) {
                int startSearchX = lastLeftPoint.getX() + SEARCH_OFFSET;
                findLaneLines(pixels, row - 1, startSearchX, startSearchX);

            // If Both lanes are found start search from last X + or - 100.
            } else {
                findLaneLines(pixels, row - 1, lastLeftPoint.getX() + SEARCH_OFFSET,
                        lastRightPoint.getX() - SEARCH_OFFSET);
            }
        }
    }

    /**
     * Calculate the array of midpoints from the two LaneLines.
     * If Respective Y-level has a point in both lines, average the X to find mid-Point
     * If Respective Y-level has a point in only one line, devide road the road width by 2, and add or subtract
     * to one known point.
     * If Respective Y-level has no point in either line, midPoint is cameraWidth / 2
     * @return
     */
    private ArrayList<Point> calculateMidPoints() {
        ArrayList<Point> midPoints = new ArrayList<>();
        boolean isFirst = true; // Holds weather or not both points have been found yet.

        // Iterate over every point in the left and right lanes.
        for (int idx = 0; idx < leftLine.getLanePoints().size(); idx++) {
            Point leftPoint = leftLine.getLanePoints().get(idx); // The left point at the element idx.
            Point rightPoint = rightLine.getLanePoints().get(idx); // The right point at the element idx.
            int yValue = getCameraHeight() - (START_SEARCH + idx); // The y value of the left and right points.

            // If neither line is found, add a point at cameraWidth / 2, yValue
            if (leftPoint.isEmpty() && rightPoint.isEmpty()) {
                if (USE_NO_LANE_DETECTION) {
                    midPoints.add(new Point(getCameraWidth() / 2, yValue));
                }
            // If Ony the right Point is found, add a point at the x rightPoint - road width / 2.
            } else if (leftPoint.isEmpty()) {
                midPoints.add(new Point(rightPoint.getX() - (calculateRoadWidth(yValue) / 2), yValue));
            // If Only the left Point is found, add a point at the x leftPoint + road width / 2.
            } else if (rightPoint.isEmpty()) {
                midPoints.add(new Point(leftPoint.getX() + (calculateRoadWidth(yValue) / 2), yValue));
            // If both lines are found, average the two lines.
            } else {
                midPoints.add(new Point((int) Math.round((leftPoint.getX() + rightPoint.getX()) / 2.0), yValue));
                // Set x1 and y1 to be the first Points to have lines on both sides.
                if (isFirst) {
                    calcSlopePoint1.setX(Math.abs(leftPoint.getX() - rightPoint.getX()));
                    calcSlopePoint1.setY(yValue);
                    isFirst = false;
                // set x2 and y2 to be the last points to have lines on both sides.
                } else {
                    calcSlopePoint2.setX(Math.abs(leftPoint.getX() - rightPoint.getX()));
                    calcSlopePoint2.setY(yValue);
                }
            }
        }

        if (isReliable(calcSlopePoint1, calcSlopePoint2)) {
            slope = calculateRoadSlope(calcSlopePoint1, calcSlopePoint2);
        }
        return midPoints;
    }

    /**
     * Calculate the slope of the road width. Between Point1, and Point2
     * @param point1 Fist Point to find slope.
     * @param point2 Second Point to find slope.
     * @return the slope of the equation for road width.
     */
    private float calculateRoadSlope(Point point1, Point point2) {
        float slopeSum = 0;
        int x1 = point1.getX();
        int y1 = point1.getY();
        int x2 = point2.getX();
        int y2 = point2.getY();
        pastSlopes.remove(0);
        pastSlopes.add((float) (y1 - y2) / (x1 - x2));

        // Average the slopes in pastSlopes
        for (Float slope : pastSlopes) {
            slopeSum += slope;
        }
        float slope = slopeSum / pastSlopes.size();
        return slope;
    }

    /**
     * Calculate the road width by plugging in the yValue to the equation of the line that passes
     * through calcSlope1 and has the slope.
     * @param yValue The yValue of the camera
     * @return How many pixels we expect the road to be at this yValue.
     */
    private int calculateRoadWidth(int yValue) {
        int roadWidth = DEFAULT_ROAD_WIDTH;
        if (calcSlopePoint1.getX() != 0) {
            // (y - y1) = m(x - x1) solved for x.
            // Or (yValue - calcSlopePoint1.getY()) = slope(roadWidth - calcSlopePoint1.getX()) solved for roadWidth.
            roadWidth = Math.round(((yValue - calcSlopePoint1.getY()) / slope) + calcSlopePoint1.getX());
        }
        return roadWidth;
    }

    /**
     * Checks to see if point 1 and point 2 are far enough away to be considered reliable.
     * @param point1 First Point to use
     * @param point2 Second Point to use
     * @return True if points are more than MINIMUM_RELIABLE_OFFSET, false otherwise.
     */
    private boolean isReliable(Point point1, Point point2) {
        return Math.abs(point1.getY() - point2.getY()) > MINIMUM_RELIABLE_OFFSET;
    }
}

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

public class SteeringMk4 extends SteeringBase {
    // Constants

    private long x1 = 0;
    private long y1 = 0;
    private long x2 = 0;
    private long y2 = 0;
    private float slope;
    private LaneLine rightLine = new LaneLine();
    private LaneLine leftLine = new LaneLine();
    private ArrayList<Float> pastSlopes = new ArrayList<>();


    public SteeringMk4(int cameraWidth, int cameraHeight, int screenWidth) {
        super(cameraWidth, cameraHeight, screenWidth);
        for (int idx = 0; idx < PREVIOUS_SLOPES; idx++) {
            pastSlopes.add(START_SLOPE);
        }
    }

    public SteeringMk4(CarControl control) {
        super(control);
    }

    @Override
    public int drive(int[] pixels) {
        leftLine.clearPoints();
        rightLine.clearPoints();
        findLaneLines(pixels, getCameraHeight() - START_SEARCH, getCameraWidth() / 2, getCameraWidth() / 2);
        setMidPoints(calculateMidPoints());
        setSteerPoint(calculateSteerPoint());
        setLeftPoints(leftLine.getLanePoints());
        setRightPoints(rightLine.getLanePoints());
        return getDegreeOffset();
    }

    private void findLaneLines(int[] pixels, int row, int leftStartingX, int rightStartingX) {
        if (row > getCameraHeight() * LOOK_DIST) {
            Point lastLeftPoint  = leftLine.getLeftPoint(pixels, row, getScreenWidth(), leftStartingX);
            Point lastRightPoint = rightLine.getRightPoint(pixels, row, getScreenWidth(), rightStartingX, getCameraWidth());
            leftLine.getLanePoints().add(lastLeftPoint);
            rightLine.getLanePoints().add(lastRightPoint);

            // If no lane was found, search from the last startingX
            if (lastLeftPoint.isEmpty() && lastRightPoint.isEmpty()) {
                findLaneLines(pixels, row - 1, leftStartingX, rightStartingX);

            // If Only the right lane was found, right starts from last x + 100. Left starts from last startingX
            } else if (lastLeftPoint.isEmpty()) {
                int startSearchX = lastRightPoint.getX() - SEARCH_OFFSET;
                findLaneLines(pixels, row - 1, startSearchX, startSearchX);

            // If Only the left lane was found, left starts from last x - 100. Right starts from last startingX
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

    private ArrayList<Point> calculateMidPoints() {
        ArrayList<Point> midPoints = new ArrayList<>();
        boolean isFirst = true;

        for (int idx = 0; idx < leftLine.getLanePoints().size(); idx++) {
            Point leftPoint = leftLine.getLanePoints().get(idx);
            Point rightPoint = rightLine.getLanePoints().get(idx);
            int yValue = getCameraHeight() - (START_SEARCH + idx);

            if (leftPoint.isEmpty() && rightPoint.isEmpty()) {
                //midPoints.add(new Point(getCameraWidth() / 2, yValue));
            } else if (leftPoint.isEmpty()) {
                midPoints.add(new Point(rightPoint.getX() - (calculateRoadWidth(yValue) / 2), yValue));
            } else if (rightPoint.isEmpty()) {
                midPoints.add(new Point(leftPoint.getX() + (calculateRoadWidth(yValue) / 2), yValue));
            } else {
                midPoints.add(new Point((int) Math.round((leftPoint.getX() + rightPoint.getX()) / 2.0), yValue));
                if (isFirst) {
                    x1 = Math.abs(leftPoint.getX() - rightPoint.getX());
                    y1 = yValue;
                    isFirst = false;
                } else {
                    x2 = Math.abs(leftPoint.getX() - rightPoint.getX());
                    y2 = yValue;
                }
            }
        }

        if (isReliable(y1, y2)) {
            slope = calculateRoadSlope(x1, y1, x2, y2);
        }
        return midPoints;
    }

    private float calculateRoadSlope(long x1, long y1, long x2, long y2) {
        float slopeSum = 0;
        pastSlopes.remove(0);
        pastSlopes.add((float) (y1 - y2) / (x1 - x2));
        for (Float slope : pastSlopes) {
            slopeSum += slope;
        }
        float slope = slopeSum / pastSlopes.size();
        System.out.println(slope);
        return slope;
    }

    private int calculateRoadWidth(int yValue) {
        int roadWidth = DEFAULT_ROAD_WIDTH;
        if (x1 != 0) {
            roadWidth = Math.round(((yValue - y1) / slope) + x1);
        }
        return roadWidth;
    }

    private boolean isReliable(long y1, long y2) { return Math.abs(y1 - y2) > MINIMUM_RELIABLE_OFFSET; }
}

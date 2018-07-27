package com.apw.Steering;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>First version of steering. It finds center point between lanes. Then it averages all
 * the center points, and routes towards this average. This version is not great at tight
 * corners, and it is unable to follow a single line. It is good at staying centered in its
 * own lane.</p>
 *
 * @author kevin
 * @author nathan
 * @author carl
 * @see SteeringBase
 * @see SteeringMk2
 */
public class SteeringMk1 extends SteeringBase {

    private List<Double> posLog = new ArrayList<>(); // Array list for logging positions fed into it
    private int startingPoint = 0; // Where the car starts looking for lines on either side.
    private final int heightOfArea = 32; // How high the car looks for lines
    private final int startingHeight = 272; // how high the car starts looking for lines
    private Point[] leadingMidPoints = new Point[startingHeight + heightOfArea];
    private Point[] pointsAhead = new Point[startingHeight - (screenHeight / 2)]; //points far ahead
    private double weight = 1.0; // >1 = right lane, <1 = left lane
    private boolean weightLane = false;
    private boolean turnAhead = false;
    private boolean turnRightAhead = false;


    /**
     * Constructor that Initializes all points in array.
     */
    public SteeringMk1() {
        for (int i = 0; i < heightOfArea; i++) {
            leftPoints.add(new Point(0, 0));
            rightPoints.add(new Point(0, 0));
            midPoints.add(new Point(0, 0));
        }
        for (int i = 0; i < leadingMidPoints.length; i++) {
            leadingMidPoints[i] = new Point(0, 0);
        }
    }

    /**
     * Reads the image data, then retruns the angle for the servos to be set to.
     * @param pixels The array of pixels on the screen
     * @return The Degree that the servos should be set to.
     */
    @Override
    public int drive(int pixels[]) {
        findPoints(pixels);
        averageMidpoints();
        return getDegreeOffset();
    }

    /**
     * <p>Process the image data From an array of pixels. Starts at the startingHeight
     * (up from the bottom)screen, then works up the screen going out from the center.
     * It looks for pixels that are higher than average luminance, and recognises that as
     * a line.</p>
     *
     * @param pixels An array of pixels (the image)
     */
    public void findPoints(int[] pixels) {
        int roadMiddle = cameraWidth;
        int leftSideTemp = 0;
        int rightSideTemp = 0;
        int count = 0;
        long averageLuminance = 0;
        Boolean found;
        Boolean leftSideFound;
        Boolean rightSideFound;
        startingPoint = 0;

        // Find average luminance
        for (int i = cameraWidth * screenHeight - 1; i > startingHeight * cameraWidth; i--) {
            averageLuminance = averageLuminance + pixels[i];
            count++;
        }
        averageLuminance = (long) (averageLuminance / count * 1.5);
        System.out.println("average luminance " + averageLuminance);

        // Find where road starts on both sides
        leftSideFound = false;
        rightSideFound = false;
        // Iterate over every row of pixels
        for (int row = screenHeight - 22; row > startingHeight + heightOfArea; row--) {
            // Iterate over the pixel columns on left half of camera
            for (int column = roadMiddle / 2; column >= 0; column--) {
                // If the pixel is brighter than average, left side found.
                if (pixels[(screenWidth * (row)) + column] >= averageLuminance) {
                    leftSideFound = true;
                    break;
                }
            }
            // Iterate over the pixel columns on right half of camera
            for (int column = roadMiddle / 2; column < cameraWidth; column++) {
                // If the pixel is brighter than average, right side found.
                if (pixels[(screenWidth * (row)) + column] >= averageLuminance) {
                    rightSideFound = true;
                    break;
                }
            }

            // Set starting point at the first place both line are found.
            if (leftSideFound && rightSideFound) {
                startingPoint = row;
                break;
            }

            leftSideFound = false;
            rightSideFound = false;
        }

        //Next, calculate the roadPoint
        count = 0;

        // Iterate over rows between startingPoint, and startingHeight.
        for (int row = startingPoint; row > startingHeight + heightOfArea; row--) {
            // Iterate over the pixel columns on left half of camera
            for (int column = roadMiddle / 2; column >= 0; column--) {
                // If the pixel is brighter than average, right side found.
                if (pixels[screenWidth * row + column] >= averageLuminance) {
                    leftSideTemp = column;
                    break;
                }
            }
            // Iterate over the pixel columns on right half of camera
            for (int column = roadMiddle / 2; column < cameraWidth; column++) {
                // If the pixel is brighter than average, right side found.
                if (pixels[screenWidth * row + column] >= averageLuminance) {
                    rightSideTemp = column;
                    break;
                }
            }

            // Drift to one side of the lane when upcoming turn.
            if (weightLane && midPoints != null) {
                averageMidpoints();
                checkPointsAhead(pixels);
                if (turnAhead) {
                    if (turnRightAhead) weight = 1.25;
                    else weight = 0.85;
                } else weight = 1;
            }

            leadingMidPoints[count].x = weightLane ? ((int) ((double) roadMiddle / 2.0 * weight)) : roadMiddle / 2;
            leadingMidPoints[count].y = row;
            count++;
            roadMiddle = leftSideTemp + rightSideTemp;
        }

        count = 0;
        for (int i = startingHeight + heightOfArea; i > startingHeight; i--) {
            //center to left
            found = false;
            leftPoints.get(count).y = i;

            for (int j = roadMiddle / 2; j >= 0; j--) {
                if (pixels[screenWidth * i + j] >= averageLuminance) {
                    leftPoints.get(count).x = j;
                    found = true;
                    break;
                }

            }
            if (found) {
                leftPoints.get(count).x = 0;
            }


            //center to right
            found = false;
            rightPoints.get(count).y = leftPoints.get(count).y;
            for (int j = roadMiddle / 2; j < cameraWidth; j++) {
                if (pixels[screenWidth * i + j] >= averageLuminance) {
                    rightPoints.get(count).x = j;
                    found = true;
                    break;
                }

            }
            if (found) {
                rightPoints.get(count).x = cameraWidth;
            }

            midPoints.get(count).x = roadMiddle / 2;
            midPoints.get(count).y = (leftPoints.get(count).y);
            roadMiddle = (leftPoints.get(count).x + rightPoints.get(count).x);
            count++;
        }
    }

    /**
     * Average the points above the target area.
     * @param pixels Array of pixels (The image)
     * @return The average Point
     */
    Point avgPointAhead(int[] pixels) { // Look ahead in the road to see if there is a turn (for following the racing curve)
        int roadMiddle = cameraWidth;
        int leftSideTemp = 0;
        int rightSideTemp = 0;
        boolean foundTurn = false;
        startingPoint = 0;

        //Next, calculate the roadpoint

        int count = 0;

        for (int i = startingHeight; i > screenHeight / 2; i--) {
            for (int j = roadMiddle / 2; j >= 0; j--) {
                if (pixels[screenWidth * i + j] == 16777215) {
                    leftSideTemp = j;
                    break;
                }
            }
            for (int j = roadMiddle / 2; j < cameraWidth; j++) {
                if (pixels[screenWidth * i + j] == 16777215) {
                    rightSideTemp = j;
                    break;
                }
            }
            pointsAhead[count].x = roadMiddle / 2;
            pointsAhead[count].y = i;
            count++;
            roadMiddle = leftSideTemp + rightSideTemp;
        }

        double tempY = 0;
        double tempX = 0;
        int avgX;
        int avgY;
        int tempCount = 0;

        for (int i = 0; i < pointsAhead.length; i++) {
            Point point = new Point(leadingMidPoints[i].x, leadingMidPoints[i].y);
            tempX += point.x;
            tempY += point.y;
            tempCount++;
        }

        avgX = (int) (tempX / tempCount);
        avgY = (int) (tempY / tempCount);

        return new Point(avgX, avgY);
    }

    /**
     * I have no idea
     * @param pixels Array of pixels (The Image)
     */
    void checkPointsAhead(int[] pixels) {
        Point ahead = avgPointAhead(pixels);
        if (Math.abs(ahead.x - origin.x) >= Math.abs((steerPoint.x - origin.x) / (steerPoint.y - origin.y) * (ahead.y - origin.y))) {
            turnAhead = true;
            if (ahead.x - origin.x > 0) turnRightAhead = true;
            else turnRightAhead = false;
        } else turnAhead = false;
    }

    /**
     * Not sure what this does
     * @param x x coordinate
     * @param y y coordinate
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
        Point[] leftEdge = new Point[length];
        Point[] rightEdge = new Point[length];
        Point[] pathTraveled = new Point[length];
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

    /**
     * Average the midpoints, and assign the average to steerPoint.
     */
    private void averageMidpoints() {
        double tempY = 0;
        double tempX = 0;
        int tempCount = 0;
        int weightCount = 0;

        boolean shouldWeight = false;
        double weightFactor = 1;

        // Sum the x's and the y's
        for (int i = 0; i < startingPoint - (startingHeight + heightOfArea); i++) {
            Point point = new Point(leadingMidPoints[i].x, leadingMidPoints[i].y);
            if (i > (startingPoint - (startingHeight + heightOfArea)) / 2) shouldWeight = true;
            tempX += shouldWeight ? weightFactor * point.x : point.x;
            tempY += point.y;
            tempCount++;
            weightCount += shouldWeight ? weightFactor - 1 : 0;
        }
        shouldWeight = false;
        if (tempCount == 0) {
            for (int i = 0; i < midPoints.size(); i++) {
                Point point = new Point(midPoints.get(i).x, midPoints.get(i).y);
                if (i > midPoints.size() / 2) shouldWeight = true;
                tempX += shouldWeight ? weightFactor * point.x : point.x;
                tempY += point.y;
                tempCount++;
                weightCount += shouldWeight ? weightFactor - 1 : 0;
            }
        }

        steerPoint.x = (int) (tempX / tempCount + weightCount);
        steerPoint.y = (int) (tempY / tempCount);

    }
}
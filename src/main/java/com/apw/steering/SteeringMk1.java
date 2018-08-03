package com.apw.steering;

import com.apw.carcontrol.CarControl;


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

    private final int heightOfArea = 32; // How high the car looks for lines
    private final int startingHeight = 272; // how high the car starts looking for lines
    private int startingPoint = 0; // Where the car starts looking for lines on either side.
    private Point[] leadingMidPoints = new Point[startingHeight + heightOfArea];
    private Point[] pointsAhead = new Point[startingHeight - (cameraHeight / 2)]; //points far ahead
    private double weight = 1.0; // >1 = right lane, <1 = left lane
    private boolean weightLane = false;
    private boolean turnAhead = false;
    private boolean turnRightAhead = false;


    /**
     * Constructor that Initializes all points in array.
     */
    SteeringMk1(int cameraWidth, int cameraHeight, int screenWidth) {
        initializeArrays();
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
        this.screenWidth = screenWidth;
        origin = new Point(cameraWidth / 2, cameraHeight);
    }

    SteeringMk1(CarControl control) {
        initializeArrays();
        this.cameraWidth = control.getImageWidth();
        this.cameraHeight = control.getImageHeight();
        origin = new Point(cameraWidth / 2, cameraHeight);
    }

    private void initializeArrays() {
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
     *
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
        startingPoint = 0;
        long averageLuminance = 0;
        Boolean leftSideFound = false;
        Boolean rightSideFound = false;
        Boolean first = true;
        Boolean found = false;
        int count = 0;
        //first before first, find average luminance
        for (int i = cameraWidth * cameraHeight - 1; i > startingHeight * cameraWidth; i--) {
            averageLuminance = averageLuminance + pixels[i];
            count++;
        }
        averageLuminance = (long) (averageLuminance/count * 1.5);
        count = 0;

        //first, find where road starts on both sides
        leftSideFound = false;
        rightSideFound = false;
        for (int i = cameraHeight - 22; i>startingHeight + heightOfArea; i--) {
            for (int j = roadMiddle/2; j>=0; j--) {
                if (pixels[(screenWidth * (i)) + j] >= averageLuminance) {
                    leftSideFound = true;
                    break;
                }
            }
            for (int j = roadMiddle/2; j<cameraWidth; j++) {
                if (pixels[(screenWidth * (i)) + j] >= averageLuminance) {
                    rightSideFound = true;
                    break;
                }
            }

            if (leftSideFound && rightSideFound) {
                startingPoint = i;
                leftSideFound = false;
                rightSideFound = false;
                break;
            }

            leftSideFound = false;
            rightSideFound = false;
        }

        //Next, calculate the roadpoint

        count = 0;

        for (int i = startingPoint; i > startingHeight + heightOfArea; i--) {
            for (int j = roadMiddle/2; j>=0; j--) {
                if (pixels[screenWidth * i + j]  >= averageLuminance) {
                    leftSideTemp = j;
                    break;
                }
            }
            for (int j = roadMiddle/2; j<cameraWidth; j++) {
                if (pixels[screenWidth * i + j]  >= averageLuminance) {
                    rightSideTemp = j;
                    break;
                }
            }
          
            if(weightLane && midPoints != null){
                averageMidpoints();
                checkPointsAhead(pixels);
                if(turnAhead){
                    if(turnRightAhead) weight = 1.25;
                    else weight = 0.85;
                }
                else weight = 1;
            }

            leadingMidPoints[count].x = weightLane?( (int) ((double) roadMiddle / 2.0 * weight)):roadMiddle/2;
            leadingMidPoints[count].y = i;
            count++;
            roadMiddle = leftSideTemp + rightSideTemp;
        }
        int tempCount = count;
        count = 0;
        for (int i = startingHeight + heightOfArea; i>startingHeight; i--) {
            //center to left
            found = false;
            leftPoints.get(count).y = i;

            for (int j = roadMiddle/2; j>=0; j--) {
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
            for (int j = roadMiddle/2; j<cameraWidth; j++) {
                if (pixels[screenWidth * i + j] >= averageLuminance) {
                    rightPoints.get(count).x = j;
                    found = true;
                    break;
                }

            }
            if (found) {
                rightPoints.get(count).x = cameraWidth;
            }

            midPoints.get(count).x = roadMiddle/2;
            midPoints.get(count).y = (leftPoints.get(count).y);
            roadMiddle = (leftPoints.get(count).x + rightPoints.get(count).x);
            count++;
        }
    }

    /**
     * Average the points above the target area.
     *
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
        for (int i = startingHeight; i > cameraHeight / 2; i--) {
            for (int j = roadMiddle / 2; j >= 0; j--) {
                if (pixels[cameraWidth * i + j] == 16777215) {
                    leftSideTemp = j;
                    break;
                }
            }
            for (int j = roadMiddle / 2; j < cameraWidth; j++) {
                if (pixels[cameraWidth * i + j] == 16777215) {
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
     * Checks the average point above the target area to see if a turn is coming up and which direction the turn is going
     *
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

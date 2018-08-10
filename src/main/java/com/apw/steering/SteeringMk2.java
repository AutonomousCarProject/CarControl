package com.apw.steering;

import com.apw.carcontrol.CarControl;

import com.apw.sbcio.fakefirm.ArduinoIO;
import java.util.ArrayList;

/**
 * <p>Version 2 of steering. This version improves center detection by adding the
 * ability to follow a single lane. This makes crashing much rarer, as well as better
 * navigating tight corners.</p>
 *
 * @author kevin
 * @author carl
 * @author nathan
 */
public class SteeringMk2 extends SteeringBase {

    private final int NUM_PREVIOUS = 3;
    private final int MAX_DIFF = 5;
    private final double MIN_DIST_LOOK = 0.8;
    private final double MAX_DIST_LOOK = 1;
    private final double LINE_TRANSLATION_MULTIPLIER = 0.4;

    private boolean leftSideFound = false;
    private boolean rightSideFound = false;
    private ArrayList<Integer> previousHeadings = new ArrayList<>();
    private int furthestY;
    private int previousMidX;

    public SteeringMk2(int cameraWidth, int cameraHeight, int screenWidth) {
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
        this.screenWidth = screenWidth;
        for (int count = 0; count < NUM_PREVIOUS; count ++) {
            previousHeadings.add(0);
        }
        origin = new Point(cameraWidth / 2, cameraHeight);
        furthestY = (int) (cameraHeight * 0.55);
    }

    public SteeringMk2(CarControl control) {
        this.cameraWidth = control.getImageWidth();
        this.cameraHeight = control.getImageHeight();
        this.screenWidth = this.cameraWidth;
        origin = new Point(cameraWidth / 2, cameraHeight);
        for (int count = 0; count < NUM_PREVIOUS; count ++) {
            previousHeadings.add(0);
        }
        furthestY = (int) (cameraHeight * 0.55);
    }

    /**
     * not currently being used.
     *
     * @param pixels the array of pixels.
     * @return Degree to drive to
     */
    @Override
    public int drive(int pixels[]) {
        findPoints(pixels);
        averagePoints();
        int frameDeg = getDegreeOffset();
        double averageDeg = 0;

        previousHeadings.add(frameDeg);
        previousHeadings.remove(0);
        for (Integer deg : previousHeadings) {
            averageDeg += deg;
        }
        return (int)(averageDeg / NUM_PREVIOUS);
    }


    /**
     * Process the camera image, and fill leftPoints, rightPoints, and midPoints.
     *
     * @param pixels An array of pixels
     */
    @Override
    public void findPoints(int[] pixels) {
    	if(pixels == null) {
    		return;
    	}
    	
        int lastX = cameraWidth / 2;
        int midX = cameraWidth / 2; // midX is where the car thinks is the middle of the road
        if (!midPoints.isEmpty()) {
            lastX = midPoints.get(midPoints.size() - 1).x;
        }
        if(lastX != midX){
            lastX = (int) (midX + Math.abs(lastX - midX)/(lastX - midX)*(midX - (midX - Math.abs(lastX - midX))/1.2));
        }

        double m = (cameraHeight * 0.24) / (cameraWidth * 0.91);
        double b = (-m * (0.06 * cameraWidth)) + (cameraHeight * 0.52);
        boolean haveNewMidX = false;
        clearArrays();


        // Iterate through each row in camera
        for (int cameraRow = cameraHeight - 50; cameraRow > furthestY; cameraRow--) {

            // Find left point
            for (int cameraColumn = midX; cameraColumn >= 0; cameraColumn--) {
                if (!leftSideFound && pixels[(screenWidth * (cameraRow)) + cameraColumn] == 0xffffff) {
                    leftSideFound = true;
                    leftPoints.add(new Point(cameraColumn, cameraRow));
                    break;
                }
            }

            // Find Right point
            for (int cameraColumn = midX; cameraColumn <= this.cameraWidth; cameraColumn++) {
                if (!rightSideFound && pixels[(screenWidth * (cameraRow - 1)) + cameraColumn] == 0xffffff) {
                    rightSideFound = true;
                    rightPoints.add(new Point(cameraColumn, cameraRow));
                    break;
                }
            }


            if (!midPoints.isEmpty()) {
                lastX = midPoints.get(midPoints.size() - 1).x;
            }
            // If two Lanes are found, average the two
            if (rightSideFound && leftSideFound) {
                midX = (rightPoints.get(rightPoints.size() - 1).x + leftPoints.get(leftPoints.size() - 1).x) / 2;
                if (Math.abs(midX - lastX) > MAX_DIFF) {
                    if (midX > lastX) {
                        midX = lastX + MAX_DIFF;
                    } else {
                        midX = lastX - MAX_DIFF;
                    }
                }
                midPoints.add(new Point(midX, cameraRow));

            // If One lane is found, add midpoint 100 pixels towards middle.
            } else if (rightSideFound) {
                Point lastRightPoint = rightPoints.get(rightPoints.size() - 1);
                midX = lastRightPoint.x - (int) ((lastRightPoint.y - b) / (2 * m));
                if (Math.abs(midX - lastX) > MAX_DIFF) {
                    if (midX > lastX) {
                        midX = lastX + MAX_DIFF;
                    } else {
                        midX = lastX - MAX_DIFF;
                    }
                }
                midPoints.add(new Point(midX, cameraRow));
            } else if (leftSideFound) {
                Point lastLeftPoint = leftPoints.get(leftPoints.size() - 1);
                midX = lastLeftPoint.x + (int) ((lastLeftPoint.y - b) / (2 * m));
                if (Math.abs(midX - lastX) > MAX_DIFF) {
                    if (midX > lastX) {
                        midX = lastX + MAX_DIFF;
                    } else {
                        midX = lastX - MAX_DIFF;
                    }
                }
                midPoints.add(new Point(midX, cameraRow));

            // If no lanes are found, route towards found lines.
            } else {
                midX = this.cameraWidth / 2;
                if (Math.abs(midX - lastX) > MAX_DIFF) {
                    if (midX > lastX) {
                        midX = lastX + MAX_DIFF;
                    } else {
                        midX = lastX - MAX_DIFF;
                    }
                }
                midPoints.add(new Point(midX, cameraRow));
            }
            if (midX != cameraWidth / 2 && !haveNewMidX) {
                previousMidX = midX;
                haveNewMidX = true;
            }

            rightSideFound = false;
            leftSideFound = false;
        }
        averagePoints();
    }

    /**
     * Clear all the arrays
     */
    private void clearArrays() {
        leftPoints.clear();
        rightPoints.clear();
        midPoints.clear();
    }

    /**
     * Average the midpoints to create the steerPoint.
     */
    private void averagePoints() {

        startTarget = (int) (midPoints.size() * MIN_DIST_LOOK);
        endTarget = (int) (midPoints.size() * MAX_DIST_LOOK);

        double ySum = 0;
        double xSum = 0;
        int temp = 0;

        // Sum the x's and the y's
        for (int idx = startTarget; idx < endTarget; idx++) {
            xSum += midPoints.get(idx).x;
            ySum += midPoints.get(idx).y;
            temp++;
        }

        steerPoint.x = (int) (xSum / (endTarget - startTarget));
        steerPoint.y = (int) (ySum / (endTarget - startTarget));
    }

}
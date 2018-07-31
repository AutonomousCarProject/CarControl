package com.apw.Steering;

import com.apw.fakefirm.Arduino;

import java.util.concurrent.ScheduledThreadPoolExecutor;


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

    private boolean leftSideFound = false;
    private boolean rightSideFound = false;

    public SteeringMk2(int cameraWidth, int cameraHeight) {
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
        origin = new Point(cameraWidth / 2, cameraHeight);
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
        return getDegreeOffset();
    }


    /**
     * Process the camera image, and fill leftPoints, rightPoints, and midPoints.
     *
     * @param pixels An array of pixels
     */
    @Override
    public void findPoints(int[] pixels) {

        clearArrays();
        int midX = cameraWidth / 2; // midX is where the car thinks is the middle of the road
        double distanceAhead = 1.8; // how far ahead the car looks for road. (Eventually dynamic?)


        // Iterate through each row in camera
        for (int cameraRow = cameraHeight - 50; cameraRow > (int) (cameraHeight / distanceAhead); cameraRow--) {

            // Find left point
            for (int cameraColumn = midX; cameraColumn >= 0; cameraColumn--) {
                if (!leftSideFound && pixels[(screenWidth * (cameraRow)) + cameraColumn] >= 16777215) {
                    leftSideFound = true;
                    leftPoints.add(new Point(cameraColumn, cameraRow));
                    break;
                }
            }

            // Find Right point
            for (int cameraColumn = midX; cameraColumn <= this.cameraWidth; cameraColumn++) {
                if (!rightSideFound && pixels[(screenWidth * (cameraRow - 1)) + cameraColumn] >= 16777215) {
                    rightSideFound = true;
                    rightPoints.add(new Point(cameraColumn, cameraRow));
                    break;
                }
            }

            // If two Lanes are found, average the two
            if (rightSideFound && leftSideFound) {
                midX = (rightPoints.get(rightPoints.size() - 1).x + leftPoints.get(leftPoints.size() - 1).x) / 2;
                midPoints.add(new Point(midX, cameraRow));

                // If One lane is found, add midpoint 100 pixels towards middle.
            } else if (rightSideFound) {
                double lastY = rightPoints.get(rightPoints.size() - 1).y;
                int lastX = rightPoints.get(rightPoints.size() - 1).x;
                midX = (int) Math.round(lastX - ((this.cameraWidth) * Math.pow((lastY) / (cameraHeight), 2)));
                midPoints.add(new Point(midX, cameraRow));
            } else if (leftSideFound) {
                double lastY = leftPoints.get(leftPoints.size() - 1).y;
                int lastX = leftPoints.get(leftPoints.size() - 1).x;
                midX = (int) Math.round(lastX + ((this.cameraWidth) * Math.pow((lastY) / (cameraHeight), 2)));
                midPoints.add(new Point(midX, cameraRow));

                // If no lanes are found, route towards found lines.
            } else {
                midX = this.cameraWidth / 2;
                midPoints.add(new Point(midX, cameraRow));
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

        startTarget = (int) (midPoints.size() * 0.5);
        endTarget = (int) (midPoints.size() * 0.7);

        double ySum = 0;
        double xSum = 0;

        // Sum the x's and the y's
        for (int idx = startTarget; idx <= endTarget; idx++) {
            xSum += midPoints.get(idx).x;
            ySum += midPoints.get(idx).y;
        }

        steerPoint.x = (int) (xSum / (endTarget - startTarget));
        steerPoint.y = (int) (ySum / (endTarget - startTarget));
    }

}
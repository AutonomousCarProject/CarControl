package com.apw.steering.steeringversions;

import com.apw.carcontrol.CarControl;

import com.apw.steering.steeringclasses.Point;
import java.util.ArrayList;
import static com.apw.steering.SteeringConstants.MAX_DIFF;
import static com.apw.steering.SteeringConstants.MAX_DIST_LOOK;
import static com.apw.steering.SteeringConstants.MIN_DIST_LOOK;
import static com.apw.steering.SteeringConstants.NUM_PREVIOUS;

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
    private ArrayList<Integer> previousHeadings = new ArrayList<>(); // Array containing the previous frames' degree
    private int furthestY; // Furthest point to look for white lines.
    private int previousMidX;

    /**
     * Constructor used for TrakSim.
     *
     * @param cameraWidth  Width of the camera, (excluding maps on right side)
     * @param cameraHeight Height of the camera
     * @param screenWidth  Width of the screen, (including maps on right side)
     */
    public SteeringMk2(int cameraWidth, int cameraHeight, int screenWidth) {
        super(cameraWidth, cameraHeight, screenWidth);
        furthestY = (int) (cameraHeight * 0.55);
        previousMidX = screenWidth / 2;
        for (int count = 0; count < NUM_PREVIOUS; count++) {
            previousHeadings.add(0);
        }
    }

    /**
     * Constructor used for actual camera
     *
     * @param control CarControl that contains data from other modules.
     */
    public SteeringMk2(CarControl control) {
        super(control);
        furthestY = (int) (getCameraHeight() * 0.55);
        previousMidX = getScreenWidth() / 2;
        for (int count = 0; count < NUM_PREVIOUS; count++) {
            previousHeadings.add(0);
        }
    }

    /**
     * Called to getSteeringAngle the car
     *
     * @param pixels the array of screen pixels.
     * @return Degree the car needs to turn.
     */
    @Override
    public int getSteeringAngle(int pixels[]) {
        findPoints(pixels);
        setSteerPoint(calculateSteerPoint());
        int frameDeg = getDegreeOffset();
        double averageDeg = 0;

        previousHeadings.add(frameDeg);
        previousHeadings.remove(0);
        for (Integer deg : previousHeadings) {
            averageDeg += deg;
        }
        return (int) (averageDeg / NUM_PREVIOUS);
    }


    /**
     * Process the camera image, and fill leftPoints, rightPoints, and midPoints.
     *
     * @param pixels An array of pixels
     */
    private void findPoints(int[] pixels) {
        if (pixels == null) {
            return;
        }
        int lastX = getCameraWidth() / 2;
        int midX = previousMidX; // midX is where the car thinks is the middle of the road
        double m = (getCameraHeight() * 0.24) / (getCameraWidth() * 0.91);
        double b = (-m * (0.06 * getCameraWidth())) + (getCameraHeight() * 0.52);
        boolean haveNewMidX = false;
        clearArrays();


        // Iterate through each row in camera
        for (int cameraRow = getCameraHeight() - 50; cameraRow > furthestY; cameraRow--) {

            // Find left point
            for (int cameraColumn = midX; cameraColumn >= 0; cameraColumn--) {
                if (!leftSideFound && pixels[(getScreenWidth() * (cameraRow)) + cameraColumn] == 0xffffff) {
                    leftSideFound = true;
                    getLeftPoints().add(new Point(cameraColumn, cameraRow));
                    break;
                }
            }

            // Find Right point
            for (int cameraColumn = midX; cameraColumn <= this.getCameraWidth(); cameraColumn++) {
                if (!rightSideFound && pixels[(getScreenWidth() * (cameraRow - 1)) + cameraColumn] == 0xffffff) {
                    rightSideFound = true;
                    getRightPoints().add(new Point(cameraColumn, cameraRow));
                    break;
                }
            }


            if (!getMidPoints().isEmpty()) {
                lastX = getMidPoints().get(getMidPoints().size() - 1).x;
            }
            // If two Lanes are found, average the two
            if (rightSideFound && leftSideFound) {
                midX = (getRightPoints().get(getRightPoints().size() - 1).x + getLeftPoints().get(getLeftPoints().size() - 1).x) / 2;
                if (Math.abs(midX - lastX) > MAX_DIFF) {
                    if (midX > lastX) {
                        midX = lastX + MAX_DIFF;
                    } else {
                        midX = lastX - MAX_DIFF;
                    }
                }
                getMidPoints().add(new Point(midX, cameraRow));

                // If One lane is found, add midpoint 100 pixels towards middle.
            } else if (rightSideFound) {
                Point lastRightPoint = getRightPoints().get(getRightPoints().size() - 1);
                midX = lastRightPoint.x - (int) ((lastRightPoint.y - b) / (2 * m));
                if (Math.abs(midX - lastX) > MAX_DIFF) {
                    if (midX > lastX) {
                        midX = lastX + MAX_DIFF;
                    } else {
                        midX = lastX - MAX_DIFF;
                    }
                }
                getMidPoints().add(new Point(midX, cameraRow));
            } else if (leftSideFound) {
                Point lastLeftPoint = getLeftPoints().get(getLeftPoints().size() - 1);
                midX = lastLeftPoint.x + (int) ((lastLeftPoint.y - b) / (2 * m));
                if (Math.abs(midX - lastX) > MAX_DIFF) {
                    if (midX > lastX) {
                        midX = lastX + MAX_DIFF;
                    } else {
                        midX = lastX - MAX_DIFF;
                    }
                }
                getMidPoints().add(new Point(midX, cameraRow));

                // If no lanes are found, route towards found lines.
            } else {
                midX = this.getCameraWidth() / 2;
                if (Math.abs(midX - lastX) > MAX_DIFF) {
                    if (midX > lastX) {
                        midX = lastX + MAX_DIFF;
                    } else {
                        midX = lastX - MAX_DIFF;
                    }
                }
                getMidPoints().add(new Point(midX, cameraRow));
            }
            if (midX != getCameraWidth() / 2 && !haveNewMidX) {
                previousMidX = midX;
                haveNewMidX = true;
            }//*/

            rightSideFound = false;
            leftSideFound = false;
        }
    }

    /**
     * Clear all the arrays
     */
    private void clearArrays() {
        getLeftPoints().clear();
        getRightPoints().clear();
        getMidPoints().clear();
    }

}
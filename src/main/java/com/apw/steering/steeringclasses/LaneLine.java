package com.apw.steering.steeringclasses;

import java.util.ArrayList;

public class LaneLine {
    private ArrayList<Point> lanePoints;
    private int white = 0xffffff;
    private int lastLeftX;
    private int lastRightX;

    public LaneLine() {
        lanePoints = new ArrayList<>();
    }

    public Point getLeftPoint(int[] pixels, int row, int screenWidth, int startingX) {
        Point leftPoint = new Point();
        int lastLeftX = startingX;

        // Create lastLeftX, which is where the algorithm begins searching for a lane.
        if (!lanePoints.isEmpty()) {
            if (!lanePoints.get(lanePoints.size() - 1).isEmpty()) {
                lastLeftX = lanePoints.get(lanePoints.size() - 1).x + 100;
            }
        }

        // search from lastLeftX to the left edge of the screen for a white pixel.
        for (int column = lastLeftX; column > 0; column--) {
            // If the Pixel is white, Add it to leftPoints.
            if (pixels[(row * screenWidth) + column] == white) {
                leftPoint = new Point(column, row);
                break;
            }
        }
        return leftPoint;
    }

    public Point getRightPoint(int[] pixels, int row, int screenWidth, int startingX, int cameraWidth) {
        Point rightPoint = new Point();

        // search from lastLeftX to the left edge of the screen for a white pixel.
        for (int column = startingX; column < cameraWidth; column++) {
            // If the Pixel is white, Add it to leftPoints.
            if (pixels[(row * screenWidth) + column] == white) {
                rightPoint = new Point(column, row);
                break;
            }
        }
        return rightPoint;
    }

    public void clearPoints() {
        lanePoints.clear();
    }

    public ArrayList<Point> getLanePoints() {
        return lanePoints;
    }
}

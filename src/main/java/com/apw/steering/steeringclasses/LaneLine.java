package com.apw.steering.steeringclasses;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class LaneLine {
    private double slope = 0;
    private double b = 0;
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

    public void calculateSlopeAndB() {
        int numberOfPoints = 0;
        for (Point point : lanePoints) {
            if (!point.isEmpty()) {
                numberOfPoints++;
            }
        }
        long xSum = calculateXSum(lanePoints);
        long ySum = calculateYSum(lanePoints);
        long xSquaredSum = calculateXSquaredSum(lanePoints);
        long xySum = calculateXYSum(lanePoints);
        slope = calculateSlope(numberOfPoints, xSum, ySum, xSquaredSum, xySum);
        b = calculateB(numberOfPoints, xSum, ySum, xSquaredSum, xySum);
    }

    public long calculateApproxValueAtX(long xValue) {
        return Math.round((this.slope * xValue) + this.b);
    }

    private static double calculateB(int numberOfPoints, long xSum, long ySum, long xSquaredSum, long xySum) {
        return ((((float) ySum * xSquaredSum) - (xSum * xySum)) /
                ((numberOfPoints * xSquaredSum) - (float) Math.pow(xSum, 2)));
    }

    private static double calculateSlope(int numberOfPoints, long xSum, long ySum, long xSquaredSum, long xySum) {
        return (((double) numberOfPoints * xySum) - (xSum * ySum)) /
                ((numberOfPoints * xSquaredSum) - Math.pow(xSum, 2));
    }

    private static long calculateXSum(List<Point> points) {
        int xSum = 0;
        for (Point point : points) {
            if (!point.isEmpty()) {
                xSum += point.getX();
            }
        }
        return xSum;
    }

    private static long calculateYSum(List<Point> points) {
        int ySum = 0;
        for (Point point : points) {
            if (!point.isEmpty()) {
                ySum += point.getY();
            }
        }
        return ySum;
    }

    private static long calculateXSquaredSum(List<Point> points) {
        int xSquaredSum = 0;
        for (Point point : points) {
            if (!point.isEmpty()) {
                xSquaredSum += Math.pow(point.getX(), 2);
            }
        }
        return xSquaredSum;
    }

    private static long calculateXYSum(List<Point> points) {
        int xySum = 0;
        for (Point point : points) {
            if (!point.isEmpty()) {
                xySum += point.getX() * (point.getY());
            }
        }
        return xySum;
    }

    public double getB() {
        return b;
    }

    public double getSlope() {
        return slope;
    }

    public void drawApproximatedLine(Graphics g, Color color) {
        long x1 = 0;
        long y1 = calculateApproxValueAtX(0);
        long x2 = 640;
        long y2 = calculateApproxValueAtX(640);

        g.setColor(color);
        g.drawLine((int) x1, (int) y1 + 22, (int) x2, (int) y2 + 22);
    }

    public void removeOutliers(int maximumDeviation) {
        Point lastPoint = lanePoints.get(0);
        for (Point point : lanePoints) {
            if (!point.isEmpty()) {
                lastPoint = point;
                break;
            }
        }

        for (Point point : lanePoints) {
            if (!point.isEmpty()) {
                if (Math.abs(point.getX() - lastPoint.getX()) >= maximumDeviation) {
                    point.makeEmpty();
                }
            } else {
                lastPoint = point;
            }
        }
    }

    public ArrayList<Point> getNonEmptyPoints() {
        ArrayList<Point> nonEmptyPoints = new ArrayList<>();
        for (Point point : lanePoints) {
            if (!point.isEmpty()) {
                nonEmptyPoints.add(point);
            }
        }
        return nonEmptyPoints;
    }

    public void clearPoints() {
        lanePoints.clear();
    }

    public ArrayList<Point> getLanePoints() {
        return lanePoints;
    }
}

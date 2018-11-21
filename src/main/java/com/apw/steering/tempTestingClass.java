package com.apw.steering;

import com.apw.steering.steeringclasses.Point;
import com.apw.steering.steeringclasses.QuadraticRegression;
import com.apw.steering.steeringversions.SteeringMk4;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class tempTestingClass {
    private static int white = 0xffffff;
    private static int black = 0;

    public static void main(String args[]) {

        ArrayList<Point> testData = new ArrayList<>();
        testData.add(new Point(-1, 4));
        testData.add(new Point(0, -2));
        testData.add(new Point(2, 0));
        testData.add(new Point(6, 9));
        testData.add(new Point(9, 20));
        testData.add(new Point(12, 18));
        testData.add(new Point(13, 23));
        testData.add(new Point(20, 26));
        QuadraticRegression regression = new QuadraticRegression(testData);



        /*
        DataCollection dataCollection = new DataCollection(640, 480);
        int modifiedImage[] = dataCollection.readArray("ColorDataReal1.txt");
        SteeringMk4 steering = new SteeringMk4(640,480,640);

        modifiedImage = toBW2(modifiedImage);
        modifiedImage = removeNoise2(modifiedImage);//
        modifiedImage = removeNoise(modifiedImage);
        dataCollection.paint(modifiedImage);


        steering.getSteeringAngle(modifiedImage);
        paintLines(steering, dataCollection);
        //drawApproxLine(steering.getMidPoints(), dataCollection);
        //drawApproxLine(steering.getRightPoints(), dataCollection);
        //drawApproxLine(steering.getLeftPoints(), dataCollection);//*/
    }

    private static int[] toBW2(int[] pixels) {

        int newPixels[] = new int[640 * 480];
        ArrayList<Integer> previousColors = new ArrayList<>();
        double averageColor;
        int numPastFrames = 10;

        for (int i = 0; i < numPastFrames; i++) {
            previousColors.add(pixels[640 * 480 - 1 - i]);
        }

        double averageRightColor = 0;
        for (int y = 250; y < 480; y++) {
            averageRightColor += pixels[getNumberFromCord(639, y)];
        }
        averageRightColor = averageRightColor / (480 - 250);

        for (int pixelIdx = pixels.length - 1; pixelIdx > 0; pixelIdx--) {
            if (pixelIdx < 250 * 640) {
                newPixels[pixelIdx] = black;
                continue;
            }
            int currentPixel = pixels[pixelIdx];
            averageColor = averageArray(previousColors);

            if (Math.abs(currentPixel - averageColor) > 0.25 * averageColor) {
                newPixels[pixelIdx] = white;
            } else {
                newPixels[pixelIdx] = black;
            }

            if (pixelIdx % 640 == 639) {
                previousColors.clear();
                for (int i = 0; i < numPastFrames; i++) {
                    previousColors.add(pixels[pixelIdx - i]);
                }
                if (averageArray(previousColors) > 1.25 * averageRightColor) {
                    previousColors.clear();
                    for (int i = 0; i < numPastFrames; i++) {
                        previousColors.add((int) averageRightColor);
                    }
                }
            }
            if (newPixels[pixelIdx] == black) {
                previousColors.add(currentPixel);
                previousColors.remove(0);
            }
        }

        return newPixels;
    }

    private static int[] removeNoise2(int pixels[]) {
        int whiteWidth = 0;
        for (int idx = 0; idx < pixels.length; idx++) {

            while (pixels[idx] == white && idx < pixels.length) {
                whiteWidth++;
                idx++;
            }
            if (6 >= whiteWidth || whiteWidth >= 40 ) {
                for (int idxToRemove = idx; idxToRemove >= idx - whiteWidth; idxToRemove--) {
                    pixels[idxToRemove] = black;
                }
            }
            whiteWidth = 0;
        }
        return pixels;
    }

    private static double averageArray(List<Integer> list) {
        int sum = 0;
        for (int num : list) {
            sum += num;
        }
        return sum / list.size();
    }

    private static int getNumberFromCord(int x, int y) {
        return (y * 640) + x;
    }

    private static int numOfWhiteAround(int[] pixels, int x, int y) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }

                if (getNumberFromCord(x + i, y + j) < 480 * 640) {
                    if (pixels[getNumberFromCord(x + i, y + j)] == white) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static int[] removeNoise(int[] pixels) {
        for (int x = 0; x < 640; x++) {
            for (int y = 479; y > 232; y--) {
                if (pixels[getNumberFromCord(x, y)] == white && numOfWhiteAround(pixels, x, y) <= 3) {
                    pixels[getNumberFromCord(x, y)] = black;
                }
            }
        }

        return pixels;
    }

    private static int[] toBW3 (int pixels[], ArrayList<Integer> lineColors) {
        for (int pixelIdx = 0; pixelIdx < pixels.length; pixelIdx++) {
            for (int color : lineColors) {
                if (Math.abs(pixels[pixelIdx] - color) <= 0.1 * color) {
                    pixels[pixelIdx] = white;
                    break;
                }
            }
            if (pixels[pixelIdx] != white) {
                pixels[pixelIdx] = black;
            }
        }
        return pixels;
    }

    private static long calculateApproxValueAtX(long xValue, double slope, double b) {
        return Math.round((slope * xValue) + b);
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

    private static void paintLines(SteeringMk4 steering, DataCollection dataCollection) {
        Point steerPoint = steering.getSteerPoint();
        dataCollection.drawPoint(steerPoint.getX(), steerPoint.getY(), 10, Color.cyan);
        for (Point point : steering.getMidPoints()) {
            dataCollection.drawPoint(point.getX(), point.getY() + 22, 5, Color.blue);
        }

        for (Point point : steering.getLeftPoints()) {
            dataCollection.drawPoint(point.getX(), point.getY() + 22, 5, Color.yellow);
        }

        for (Point point : steering.getRightPoints()) {
            dataCollection.drawPoint(point.getX(), point.getY() + 22, 5, Color.yellow);
        }//*/
    }

    private static void drawApproxLine(List<Point> points, DataCollection dataCollection) {
        int numberOfPoints = 0;
        for (Point point : points) {
            if (!point.isEmpty()) {
                numberOfPoints++;
            }
        }
        long xSum = calculateXSum(points);
        long ySum = calculateYSum(points);
        long xSquaredSum = calculateXSquaredSum(points);
        long xySum = calculateXYSum(points);
        double slope = calculateSlope(numberOfPoints, xSum, ySum, xSquaredSum, xySum);
        double b = calculateB(numberOfPoints, xSum, ySum, xSquaredSum, xySum);

        System.out.println("Slope:" + slope);

        long x1 = 0;
        System.out.println("Y Intercept: " + b);
        long y1 = calculateApproxValueAtX(0, slope, b);
        long x2 = 640;
        long y2 = calculateApproxValueAtX(640, slope, b);
        System.out.println("At x = 640: " + y2);

        dataCollection.drawLine((int) x1, (int) y1 + 22, (int) x2, (int) y2 + 22, Color.magenta);
    }
}

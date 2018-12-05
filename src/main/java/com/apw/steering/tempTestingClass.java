package com.apw.steering;

import com.apw.steering.steeringclasses.PolynomialEquation;
import com.apw.steering.steeringclasses.Point;
import com.apw.steering.steeringclasses.PolynomialRegression;
import com.apw.steering.steeringversions.SteeringMk4;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class tempTestingClass implements KeyListener {
    private static int white = 0xffffff;
    private static int black = 0;

    private static int width = 640;
    private static int height = 480;
    private static int screenWidth = 640; // 912 640
    private int[] originalImage;
    private double multiplier = 0.47;

    private String fileName = "ColorDataReal1.txt";

    private DataCollection dataCollection;

    public static void main(String args[]) {
        new tempTestingClass();
    }

    public tempTestingClass() {


        dataCollection = new DataCollection(screenWidth, height);
        dataCollection.addKeyListener(this);

        originalImage = dataCollection.readArray(fileName);
        SteeringMk4 steering = new SteeringMk4(width, height, screenWidth);

        int[] modifiedImage = toBW2(originalImage);
        dataCollection.paint(modifiedImage);


        steering.getSteeringAngle(modifiedImage);

        ArrayList<PolynomialRegression> regressions = new ArrayList<>();
        int degree = 4;
        PolynomialEquation leftRegression = new PolynomialRegression(rotateAndFlip(steering.getLeftLine().getNonEmptyPoints()), degree);
        PolynomialEquation rightRegression = new PolynomialRegression(rotateAndFlip(steering.getRightLine().getNonEmptyPoints()), degree);

        //paintLines(steering, dataCollection);
        int leftMin = steering.getLeftLine().getNonEmptyPoints().get(steering.getLeftLine().getNonEmptyPoints().size() - 1).getY();
        int leftMax = steering.getLeftLine().getNonEmptyPoints().get(0).getY();
        paintEquation(dataCollection, leftRegression, Color.magenta, leftMin, leftMax);
        int rightMin = steering.getRightLine().getNonEmptyPoints().get(steering.getRightLine().getNonEmptyPoints().size() - 1).getY();
        int rightMax = steering.getRightLine().getNonEmptyPoints().get(0).getY();
        paintEquation(dataCollection, rightRegression, Color.magenta, rightMin, rightMax);
        int midMin = Math.max(leftMin, rightMin);
        int midMax = Math.min(leftMax, rightMax);
        paintMidLine(leftRegression, rightRegression, dataCollection, Color.cyan, midMin, midMax);

        System.out.println("LeftLine Regression:\n" + leftRegression.toString() + "\n");
        System.out.println("RightLine Regression:\n" + rightRegression.toString() + "\n");
        //System.out.println("MidPoints Regression:\n" + regressions.get(2).toString() + "\n");//*/
    }

    public void keyReleased(KeyEvent e) { }
    public void keyTyped(KeyEvent e) {
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT) {
            multiplier -= 0.01;
            dataCollection.paint(toBW4(originalImage));
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            multiplier += 0.01;
            dataCollection.paint(toBW4(originalImage));
        }
        System.out.println(multiplier);
    }

    private int[] toBW4(int[] pixels) {
        int previousColor = 0;
        int[] newPixels = new int[height * screenWidth];
        for (int pixelIdx = 0; pixelIdx < pixels.length; pixelIdx++) {
            if (Math.abs(pixels[pixelIdx] - previousColor) > multiplier * previousColor) {
                newPixels[pixelIdx] = white;
                previousColor = pixels[pixelIdx];
                pixelIdx++;
                while (Math.abs(pixels[pixelIdx] - previousColor) <= multiplier * previousColor && pixelIdx + 1 < pixels.length) {
                    newPixels[pixelIdx] = white;
                    previousColor = pixels[pixelIdx];
                    pixelIdx++;
                }
            } else {
                newPixels[pixelIdx] = black;
                previousColor = pixels[pixelIdx];
            }
        }
        return newPixels;
    }

    private static ArrayList<Point> rotateAndFlip(ArrayList<Point> list) {
        ArrayList<Point> rotatedArray = new ArrayList<>();
        for (Point point : list) {
            rotatedArray.add(new Point(point.getY(), screenWidth - point.getX()));
        }
        return rotatedArray;
    }

    private static int[] toBW3(int[] pixels) {

        int[] newPixels = new int[screenWidth * height];
        int numToAverage = -1;
        int averageColor = 0;
        double differenceMultiplier = 0.2;

        for (int y = 0; y < height; y++) {
            int lastWhiteX = 0;
            for (int x = 0; x < width; x++) {

                int pixelIdx = getNumberFromCord(x, y);
                if (y < height * 0.5) {
                    newPixels[pixelIdx] = black;
                    continue;
                }
                averageColor = calculateNextAverage(pixels, x, y, numToAverage, lastWhiteX);
                int currentPixel = pixels[pixelIdx];

                if (Math.abs(currentPixel - averageColor) > differenceMultiplier * averageColor) {

                    newPixels[pixelIdx] = white;
                    //averageColor = calculateNextAverage(pixels, x + 2, y, -2, lastWhiteX);
                    //lastWhiteX = 0;

                    /*while (Math.abs(pixels[getNumberFromCord(x + 1, y)] - averageColor) < 0.15 * averageColor && x + 1 < width) {
                        averageColor = calculateNextAverage(pixels, x + 3, y, -3, lastWhiteX);
                        x++;
                        newPixels[getNumberFromCord(x, y)] = white;
                    }//*/
                    lastWhiteX = 0;
                    continue;
                } else {
                    newPixels[pixelIdx] = black;
                }
                lastWhiteX++;
            }

        }

        return newPixels;
    }

    private static int calculateNextAverage(int[] pixels, int x, int y, int numToAverage, int lastWhiteX) {

        double average = pixels[getNumberFromCord(x, y)];
        int count = 1;
        int i = x + numToAverage;
        while (i != x && lastWhiteX - count > 0) {
            average += pixels[getNumberFromCord(x - i, y)];
            count++;
            if (i < x) {
                i++;
            } else {
                i--;
            }
        }
        average = average / count;

        return (int) Math.round(average);
    }

    private static boolean isClose(int num1, int num2) {
        return Math.abs(num1 - num2) < 0.3 * num1;
    }

    private int[] toBW2(int[] pixels) {

        int newPixels[] = new int[screenWidth * height];
        ArrayList<Integer> previousColors = new ArrayList<>();
        double averageColor;
        int numPastFrames = 10;

        for (int i = 0; i < numPastFrames; i++) {
            previousColors.add(pixels[screenWidth * height - (screenWidth - width) - 1 - i]);
        }

        double averageRightColor = 0;
        for (int y = 250; y < height; y++) {
            averageRightColor += pixels[getNumberFromCord(width - 1, y)];
        }
        averageRightColor = averageRightColor / (height - 250);

        for (int pixelIdx = pixels.length - 1; pixelIdx > 0; pixelIdx--) {
            if (pixelIdx < 250 * screenWidth) {
                newPixels[pixelIdx] = black;
                continue;
            } else if (pixelIdx % screenWidth >= width) {
                newPixels[pixelIdx] = black;
                continue;
            }

            int currentPixel = pixels[pixelIdx];
            averageColor = averageArray(previousColors);

            if (Math.abs(currentPixel - averageColor) > multiplier * averageColor) {
                newPixels[pixelIdx] = white;
            } else {
                newPixels[pixelIdx] = black;
            }

            if (pixelIdx % screenWidth == width - 1) {
                previousColors.clear();
                for (int i = 0; i < numPastFrames; i++) {
                    previousColors.add(pixels[pixelIdx - i]);
                }
                if (averageArray(previousColors) > (1 + multiplier) * averageRightColor) {
                    previousColors.clear();
                    for (int i = 0; i < numPastFrames; i++) {
                        previousColors.add((int) averageRightColor);
                    }
                }
            }
            if (newPixels[pixelIdx] == black || (pixels[pixelIdx] != white && width != screenWidth)) {
                previousColors.add(currentPixel);
                previousColors.remove(0);
            }
        }

        //newPixels = removeNoise(newPixels);
        if (screenWidth == 640) {
            newPixels = removeNoise2(newPixels);
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
            if (5 >= whiteWidth || whiteWidth >= 40 ) {
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
        return (y * screenWidth) + x;
    }

    private static int numOfWhiteAround(int[] pixels, int x, int y) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }

                if (getNumberFromCord(x + i, y + j) < height * screenWidth) {
                    if (pixels[getNumberFromCord(x + i, y + j)] == white) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static int[] removeNoise(int[] pixels) {
        for (int x = 0; x < screenWidth; x++) {
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

    private static void paintEquation(DataCollection dataCollection, PolynomialEquation regression, Color color,
                                      int min, int max) {
        for (int y = min; y < max; y++) {
            dataCollection.drawPoint(screenWidth - (int) Math.round(regression.getYValueAtX(y).doubleValue()), y,
                    3, color);
        }
    }

    private static void paintMidLine(PolynomialEquation leftLine, PolynomialEquation rightLine,
                                     DataCollection dataCollection, Color color, int min, int max) {
        for (int y = min; y < max; y++) {
            int xValue = (int) Math.round((leftLine.getYValueAtX(y).doubleValue() +
                    rightLine.getYValueAtX(y).doubleValue()) / 2);
            dataCollection.drawPoint(screenWidth - xValue, y, 2, color);
        }
    }

    private static void paintLines(SteeringMk4 steering, DataCollection dataCollection) {
        Point steerPoint = steering.getSteerPoint();
        dataCollection.drawPoint(steerPoint.getX(), steerPoint.getY(), 10, Color.cyan);
        for (Point point : steering.getMidPoints()) {
            dataCollection.drawPoint(point.getX(), point.getY(), 5, Color.blue);
        }

        for (Point point : steering.getLeftPoints()) {
            dataCollection.drawPoint(point.getX(), point.getY(), 5, Color.yellow);

        }

        for (Point point : steering.getRightPoints()) {
            dataCollection.drawPoint(point.getX(), point.getY(), 5, Color.yellow);
        }//*/

    }
}

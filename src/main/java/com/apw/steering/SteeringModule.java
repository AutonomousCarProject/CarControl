package com.apw.steering;

import com.apw.apw3.DriverCons;
import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;

import java.awt.*;

public class SteeringModule extends SteeringBase implements Module {
    private boolean leftSideFound = false;
    private boolean rightSideFound = false;

    public SteeringModule() {
    }

    @Override
    public void update(CarControl control) {
        int angle = drive(control.getRGBImage());
        control.steer(true, angle);
    }

    public int drive(int pixels[]) {
        findPoints(pixels);
        averagePoints();
        return getDegreeOffset();
    }

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

    public void findPoints(int[] pixels) {
        clearArrays();
        int midX = cameraWidth / 2; // midX is where the car thinks is the middle of the road
        double distanceAhead = 1.8; // how far ahead the car looks for road. (Eventually dynamic?)
        int screenWidth = 912;


        // Iterate through each row in camera
        for (int cameraRow = screenHeight - 50; cameraRow > (int) (screenHeight / distanceAhead); cameraRow--) {

            // Find left point
            for (int cameraColumn = midX; cameraColumn >= 0; cameraColumn--) {
                if (!leftSideFound && pixels[(screenWidth * (cameraRow)) + cameraColumn] >= 16777215) {
                    leftSideFound = true;
                    leftPoints.add(new Point(cameraColumn, cameraRow));
                    break;
                }
            }

            // Find Right point
            for (int cameraColumn = midX; cameraColumn <= cameraWidth; cameraColumn++) {
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
                midX = (int) Math.round(lastX - ((cameraWidth) * Math.pow((lastY) / (screenHeight), 2)));
                midPoints.add(new Point(midX, cameraRow));
            } else if (leftSideFound) {
                double lastY = leftPoints.get(leftPoints.size() - 1).y;
                int lastX = leftPoints.get(leftPoints.size() - 1).x;
                midX = (int) Math.round(lastX + ((cameraWidth) * Math.pow((lastY) / (screenHeight), 2)));
                midPoints.add(new Point(midX, cameraRow));

                // If no lanes are found, route towards found lines.
            } else {
                midX = cameraWidth / 2;
                midPoints.add(new Point(midX, cameraRow));
            }

            rightSideFound = false;
            leftSideFound = false;
        }
        averagePoints();
    }

    @Override
    public void paint(CarControl control, Graphics g) {
        g.setColor(Color.RED);

        if (DriverCons.D_DrawPredicted) {
            int tempY = 0;
            for (int idx = 0; idx < midPoints.size(); idx++) {
                if (idx >= startTarget && idx <= endTarget) {
                    g.setColor(Color.red);
                    tempY += midPoints.get(idx).y;
                    g.fillRect(midPoints.get(idx).x, midPoints.get(idx).y + control.getEdges().top, 5, 5);
                } else {
                    g.setColor(Color.BLUE);
                }
            }
            System.out.println(tempY / (1.0 * (endTarget - startTarget)));
        }

        if (DriverCons.D_DrawOnSides) {
            for (Point point : leftPoints) {
                g.setColor(Color.YELLOW);
                g.fillRect(point.x + control.getEdges().left, point.y + control.getEdges().top, 5, 5);
            }
            for (Point point : rightPoints) {
                g.fillRect(point.x + control.getEdges().left, point.y + control.getEdges().top, 5, 5);
            }
        }

        // Draw steerPoint on screen
        g.setColor(Color.CYAN);
        g.fillRect(steerPoint.x, steerPoint.y, 7, 7);

        //Draw predicted points and detected lines
        for (Point point : midPoints) {
            if (DriverCons.D_DrawPredicted) {
                control.rectFill(255, point.y, point.x, point.y + 5, point.x + 5);
            }
        }
        if (DriverCons.D_DrawOnSides) {
            for (Point point : leftPoints) {
                int xL = point.x;
                int yL = point.y;
                control.rectFill(16776960, yL, xL, yL + 5, xL + 5);
            }
            for (Point point : rightPoints) {
                int xR = point.x;
                int yR = point.y;
                control.rectFill(16776960, yR, xR, yR + 5, xR + 5);
            }
        }
    }

    private void clearArrays() {
        leftPoints.clear();
        rightPoints.clear();
        midPoints.clear();
    }
}
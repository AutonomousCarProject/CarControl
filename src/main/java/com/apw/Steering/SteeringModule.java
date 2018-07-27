package com.apw.Steering;

import com.apw.apw3.DriverCons;
import com.apw.apw3.TrakSim;
import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;
import com.apw.fakefirm.Arduino;

import java.awt.*;

public class SteeringModule implements Module {

    private int SteerPin;
    private Arduino theServos; // The servo to write to

    SteeringMk2 steering = new SteeringMk2(SteerPin, theServos);

    int sumOfAngles;
    double locX, locY;

    public SteeringModule() {

    }

    @Override
    public void update(CarControl control) {
        int tempDeg = steering.drive(control.getRGBImage());
        steering.findPoints(control.getRGBImage());
        double inRadiusAngle = .57 / 2 * (double) tempDeg;
        double outRadiusAngle = .38 / 2 * (double) tempDeg;
        if (tempDeg < 0) {
            outRadiusAngle = .45 / 2 * tempDeg;
            inRadiusAngle = 0.7 / 2 * (double) tempDeg;
        }

        double turnRadiusIn = 2.68 / Math.tan(Math.toRadians(tempDeg * .37)) + .5 * (1.976);
        double turnRadiusOut = 2.68 / Math.tan(Math.toRadians(tempDeg * .37)) - .5 * (1.976);
        double averageTurnRadius = (turnRadiusIn + turnRadiusOut) / 2;
        double angleTurned = ((double) DriverCons.D_FrameTime / 1000.0) * DriverCons.D_fMinSpeed / averageTurnRadius * 2;

        angleTurned = Math.toDegrees(angleTurned);
        if (tempDeg == 0) angleTurned = 0;

        sumOfAngles += (double) angleTurned;

        locX = locX + (double) Math.cos(Math.toRadians(sumOfAngles)) * (double) DriverCons.D_FrameTime / 1000 * (double) DriverCons.D_fMinSpeed;
        locY = locY + (double) Math.sin(Math.toRadians(sumOfAngles)) * (double) DriverCons.D_FrameTime / 1000 * (double) DriverCons.D_fMinSpeed;

        makeTurnAdjustment();
    }

    @Override
    public void paint(CarControl control, Graphics g) {
        g.setColor(Color.RED);
        //graf.fillRect(100, testSteering.startingPoint, 1, 1);

        if (DriverCons.D_DrawPredicted) {
            int tempY = 0;
            for (int idx = 0; idx < steering.midPoints.size(); idx++) {
                if (idx >= steering.startTarget && idx <= steering.endTarget) {
                    g.setColor(Color.red);
                    tempY += steering.midPoints.get(idx).y;
                    g.fillRect(steering.midPoints.get(idx).x, steering.midPoints.get(idx).y + control.getEdges().top, 5, 5);
                } else {
                    g.setColor(Color.BLUE);
                }
                // graf.fillRect(testSteering.midPoints.get(idx).x, testSteering.midPoints.get(idx).y + edges.top, 5, 5);
            }
            System.out.println(tempY / (1.0 * (steering.endTarget - steering.startTarget)));
        }

        if (DriverCons.D_DrawOnSides) {
            for (Point point : steering.leftPoints) {
                g.setColor(Color.YELLOW);
                g.fillRect(point.x + control.getEdges().left, point.y + control.getEdges().top, 5, 5);
            }
            for (Point point : steering.rightPoints) {
                g.fillRect(point.x + control.getEdges().left, point.y + control.getEdges().top, 5, 5);
            }
        }


        // Draw steerPoint on screen
        g.setColor(Color.CYAN);
        g.fillRect(steering.steerPoint.x, steering.steerPoint.y, 7, 7);


        //Draw predicted points and detected lines
        for (Point point : steering.midPoints) {
            if (DriverCons.D_DrawPredicted) {
                control.rectFill(255, point.y, point.x, point.y + 5, point.x + 5);
            }
        }
        if (DriverCons.D_DrawOnSides) {
            for (Point point : steering.leftPoints) {
                int xL = point.x;
                int yL = point.y;
                control.rectFill(16776960, yL, xL, yL + 5, xL + 5);
            }
            for (Point point : steering.rightPoints) {
                int xR = point.x;
                int yR = point.y;
                control.rectFill(16776960, yR, xR, yR + 5, xR + 5);
            }
        }

    }
}

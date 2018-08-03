package com.apw.steering;

import com.apw.apw3.DriverCons;
import com.apw.carcontrol.CamControl;
import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;
import com.apw.speedcon.SpeedControlModule;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.PrintStream;

public class SteeringModule implements Module {

    PrintStream o;
    private SteeringBase steering;
    private double sumOfAngles = 0;
    private double locX = 0;
    private double locY = 0;
    private Boolean drawnMap = false;
    private int angle = 0;
    private int frameCount = 0;


    public SteeringModule() {
    }

    @Override
    public void initialize(CarControl control) {
        control.addKeyEvent(KeyEvent.VK_LEFT, () -> control.steer(false, -5));
        control.addKeyEvent(KeyEvent.VK_RIGHT, () -> control.steer(false, 5));

        if (control instanceof CamControl) {
           if (DriverCons.D_steeringVersion == 1) {
               steering = new SteeringMk1(control);
           } else if (DriverCons.D_steeringVersion == 2) {
               steering = new SteeringMk2(control);
           }
        } else if (DriverCons.D_steeringVersion == 1) {
                steering = new SteeringMk1(640, 480, 912);
        } else if (DriverCons.D_steeringVersion == 2) {
                steering = new SteeringMk2(640, 480, 912);
        }
    }

    @Override
    public void update(CarControl control) {
        angle = steering.drive(control.getRGBImage());
        //System.out.println(angle);
        control.steer(true, angle);
    }


    @Override
    public void paint(CarControl control, Graphics g) {

        double widthMultiplier = (1.0 * control.getWindowWidth() / steering.screenWidth);
        double heightMultiplier = (1.0 * control.getWindowHeight() / steering.cameraHeight);
        int tempDeg = angle;
        if (control.getVelocity() > 0) {

            double inRadiusAngle = .57 / 2 * (double) tempDeg;
            double outRadiusAngle = .38 / 2 * (double) tempDeg;
            if (tempDeg < 0) {
                outRadiusAngle = .45 / 2 * tempDeg;
                inRadiusAngle = 0.7 / 2 * (double) tempDeg;
            }

            double turnRadiusIn = 2.68 / Math.tan(Math.toRadians(tempDeg * 0.76 - 3.02)) + .5 * (1.976);
            double turnRadiusOut = 2.68 / Math.tan(Math.toRadians(tempDeg * 0.56 - 2.4)) - .5 * (1.976);
            double averageTurnRadius = (turnRadiusIn + turnRadiusOut) / 2;
            double angleTurned = ((double) DriverCons.D_FrameTime / 1000.0) * DriverCons.D_fMinSpeed / averageTurnRadius;

            angleTurned = Math.toDegrees(angleTurned);
            if (tempDeg < 0) {
                angleTurned = angleTurned * DriverCons.D_LeftSteer / DriverCons.D_RiteSteer;
            }
            angleTurned = angleTurned;
            if (tempDeg == 0) angleTurned = 0;

            sumOfAngles += (double) angleTurned;
            sumOfAngles = sumOfAngles;

            //if (sumOfAngles > 360) sumOfAngles = sumOfAngles - 360;
            //if (sumOfAngles < 0) sumOfAngles = sumOfAngles + 360;
            //}


            locX = locX + (double) Math.cos(Math.toRadians(sumOfAngles)) * (double) DriverCons.D_FrameTime / 1000.0 * (double) DriverCons.D_fMinSpeed;


            locY = locY + (double) Math.sin(Math.toRadians(sumOfAngles)) * (double) DriverCons.D_FrameTime / 1000.0 * (double) DriverCons.D_fMinSpeed;
            if (frameCount < 100) frameCount++;
            if (frameCount >= 100 && Math.abs(locX) < 15 && Math.abs(locY) < 15 && Math.abs(tempDeg) < 3 && !drawnMap && (Math.abs(sumOfAngles % 360) < 10)) {
                System.out.println("found origin");
                steering.drawMapArrays();
                drawnMap = true;
            } else if (!drawnMap) {
                steering.updatePosLog(locX, locY, sumOfAngles);
            }
            g.setColor(Color.RED);
            if (drawnMap) {
                System.out.println("drawing map");
                for (int i = 0; i < steering.pathTraveled.length; i++) {
                    Point p1 = steering.pathTraveled[i];
                    Point p2 = steering.leftEdge[i];
                    Point p3 = steering.rightEdge[i];
                    g.drawRect(DriverCons.D_ImWi + 30 + (int) ((double) p1.x), 150 + p1.y, 1, 1);
                    //graf.drawRect(DriverCons.D_ImWi + 30 + (int) ((double) p2.x), 150 + p2.y, 1, 1);
                    //graf.drawRect(DriverCons.D_ImWi + 30 + (int) ((double) p3.x), 150 + p3.y, 1, 1);
                    System.setOut(o);
                    System.out.println(p1.x + " " + p1.y);
                    PrintStream console = System.out;
                    System.setOut(console);
                }
            }
        }
        
     // Draw steering.steerPoint on screen
        g.setColor(Color.CYAN);
        g.fillRect(steering.steerPoint.x, steering.steerPoint.y + 25, 7, 7);

        for (int idx = 0; idx < steering.midPoints.size(); idx++) {

            if (idx >= steering.startTarget && idx <= steering.endTarget) {
                g.setColor(Color.green);
                g.fillRect((int) ((steering.midPoints.get(idx).x - 2) * widthMultiplier),
                        (int) ((steering.midPoints.get(idx).y + 10) * heightMultiplier),
                        4, 4);
            } else {
                g.setColor(Color.blue);
                g.fillRect((int)((steering.midPoints.get(idx).x - 2) * widthMultiplier),
                        (int)((steering.midPoints.get(idx).y + 10) * heightMultiplier),
                        4, 4);
            }

        }

        // Draw left and right sides
        g.setColor(Color.yellow);
        if (DriverCons.D_DrawOnSides) {
            g.setColor(Color.yellow);
            for (Point point : steering.leftPoints) {

                int xL = point.x - 4;
                int yL = point.y - 4;
                g.fillRect((int) (xL * widthMultiplier), (int) (yL * heightMultiplier) + 10, 8, 8);
            }
            for (Point point : steering.rightPoints) {
                int xR = point.x - 4;
                int yR = point.y - 4;
                g.fillRect((int) (xR * widthMultiplier), (int) (yR * heightMultiplier) + 10, 8, 8);

            }
        }
        g.setColor(Color.cyan);
        g.fillRect((int) ((steering.steerPoint.x - 5) * widthMultiplier),
                (int) ((steering.steerPoint.y - 5) * heightMultiplier) + 10, 10, 10);
    }
}
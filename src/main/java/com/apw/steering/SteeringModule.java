package com.apw.steering;

import com.apw.apw3.DriverCons;
import com.apw.carcontrol.CamControl;
import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;
import com.apw.speedcon.SpeedControlModule;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;

public class SteeringModule implements Module {

    PrintStream o;
    private SteeringMk3 steering;
    private double sumOfAngles = 0;
    private double locX = 0;
    private double locY = 0;
    private Boolean drawnMap = false;
    private int angle = 0;
    private int frameCount = 0;
    private Boolean disablePaint = false;


    public SteeringModule() {
    }

    @Override
    public void initialize(CarControl control) {
        control.addKeyEvent(KeyEvent.VK_LEFT, () -> control.steer(false, -5));
        control.addKeyEvent(KeyEvent.VK_RIGHT, () -> control.steer(false, 5));
        
/*
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
        } else if (DriverCons.D_steeringVersion == 3) {
            steering = new SteeringMk3(640, 480, 912);
        }
        */
        steering = new SteeringMk3(640, 480, 912);
    }

    @Override
    public void update(CarControl control) {
        angle = steering.drive(control.getRGBImage());
        //System.out.println(angle);
        control.steer(true, angle);
    }


    @Override
    public void paint(CarControl control, Graphics g) {
    	
    	if(disablePaint) {
    		return;
    	}

        double widthMultiplier = (1.0 * control.getWindowWidth() / steering.screenWidth);
        double heightMultiplier = (1.0 * control.getWindowHeight() / steering.cameraHeight);
        int tempDeg = angle;

        if (steering instanceof SteeringMk3) {
            if (((SteeringMk3) steering).getDrawnMap()) {
                //System.out.println(steering.pathTraveled[0]);
                for (int i = 0; i < steering.pathTraveled.length; i++) {
                		
                    g.setColor(Color.RED);
                    
                    if (steering.onCurve[i] == true) {
                        g.setColor(Color.BLUE);
                    }
                    
                    Point p1 = steering.pathTraveled[i];
                   // p1.x *= 5;
                   // p1.y *= 5;
                    //System.out.println(p1.x + " " + p1.y);
                    g.drawRect(DriverCons.D_ImWi + 100 + (int) ((double) p1.x * 5), 200 + p1.y * 5, 1, 1);
                    //graf.drwawRect(DriverCons.D_ImWi + 30 + (int) ((double) p2.x), 150 + p2.y, 1, 1);
                    //graf.drawRect(DriverCons.D_ImWi + 30 + (int) ((double) p3.x), 150 + p3.y, 1, 1);
                }
                //System.out.println(steering.curvePos.size());
                
                for (int i = 0; i < steering.curvePos.size(); i++) {

                    g.setColor(Color.GREEN);
                    g.fillRect(DriverCons.D_ImWi + 100 + steering.curvePos.get(i).x * 5, steering.curvePos.get(i).y * 5 + 200, 3, 3);
                }
                
            }
        }

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

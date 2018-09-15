package com.apw.steering;

import com.apw.apw3.DriverCons;
import com.apw.carcontrol.CamControl;
import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;

import com.apw.steering.steeringclasses.Point;
import com.apw.steering.steeringversions.SteeringBase;
import com.apw.steering.steeringversions.SteeringMk1;
import com.apw.steering.steeringversions.SteeringMk2;
import com.apw.steering.steeringversions.SteeringMk4;
import java.awt.*;
import java.awt.event.KeyEvent;

public class SteeringModule implements Module {

    private SteeringBase steering;
    private int angle = 0;
    private Boolean disablePaint = false;


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
        } else if (DriverCons.D_steeringVersion == 3) {
            //steering = new SteeringMk3(640, 480, 912);
        } else if (DriverCons.D_steeringVersion == 4) {
            steering = new SteeringMk4(640, 480, 912);
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
    	
    	if(disablePaint) {
    		return;
    	}

        double widthMultiplier = (1.0 * control.getWindowWidth() / steering.getScreenWidth());
        double heightMultiplier = (1.0 * control.getWindowHeight() / steering.getCameraHeight());

        for (int idx = 0; idx < steering.getMidPoints().size(); idx++) {
            if (idx >= steering.getStartTarget() && idx <= steering.getEndTarget()) {
                g.setColor(Color.green);
                g.fillRect((int) ((steering.getMidPoints().get(idx).x - 2) * widthMultiplier),
                        (int) ((steering.getMidPoints().get(idx).y + 10) * heightMultiplier),
                        4, 4);
            } else {
                g.setColor(Color.blue);
                g.fillRect((int)((steering.getMidPoints().get(idx).x - 2) * widthMultiplier),
                        (int)((steering.getMidPoints().get(idx).y + 10) * heightMultiplier),
                        4, 4);
            }
        }

        // Draw left and right sides
        g.setColor(Color.yellow);
        if (DriverCons.D_DrawOnSides) {
            g.setColor(Color.yellow);
            for (com.apw.steering.steeringclasses.Point point : steering.getLeftPoints()) {
                g.setColor(Color.MAGENTA);
                int xL = point.x - 4;
                int yL = point.y - 4;
                g.fillRect((int) (xL * widthMultiplier), (int) (yL * heightMultiplier) + 10, 8, 8);
            }
            for (Point point : steering.getRightPoints()) {
                g.setColor(Color.green);
                int xR = point.x - 4;
                int yR = point.y - 4;
                g.fillRect((int) (xR * widthMultiplier), (int) (yR * heightMultiplier) + 10, 8, 8);
            }
        }
        g.setColor(Color.cyan);
        g.fillRect((int) ((steering.getSteerPoint().x - 5) * widthMultiplier),
                (int) ((steering.getSteerPoint().y - 5) * heightMultiplier) + 10, 10, 10);
    }
}

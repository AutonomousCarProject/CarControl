package com.apw.steering;

import com.apw.apw3.DriverCons;
import com.apw.steering.steeringclasses.LaneLine;

import java.awt.Color;
import com.apw.carcontrol.CamControl;
import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;
import com.apw.steering.steeringclasses.Point;
import com.apw.steering.steeringversions.SteeringBase;
import com.apw.steering.steeringversions.SteeringMk1;
import com.apw.steering.steeringversions.SteeringMk2;
import com.apw.steering.steeringversions.SteeringMk4;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import static com.apw.steering.SteeringConstants.DRAW_STEERING_LINES;
import static com.apw.steering.SteeringConstants.LEFT_LANE_COLOR;
import static com.apw.steering.SteeringConstants.MAX_STEER_DIFFERENCE;
import static com.apw.steering.SteeringConstants.MIDPOINT_COLOR;
import static com.apw.steering.SteeringConstants.PAST_STEERING_ANGLES;
import static com.apw.steering.SteeringConstants.RIGHT_LANE_COLOR;
import static com.apw.steering.SteeringConstants.STEERING_VERSION;
import static com.apw.steering.SteeringConstants.STEER_POINT_COLOR;
import static com.apw.steering.SteeringConstants.TARGET_POINT_COLOR;

public class SteeringModule implements Module {

    private SteeringBase steering;
    private ArrayList<Integer> pastSteeringAngles = new ArrayList<>();
    private DataCollection dataCollection = new DataCollection(912, 480);

    public SteeringModule() {
    }

    @Override
    public void initialize(CarControl control) {
        control.addKeyEvent(KeyEvent.VK_LEFT, () -> control.steer(false, -5));
        control.addKeyEvent(KeyEvent.VK_RIGHT, () -> control.steer(false, 5));
        control.addKeyEvent(KeyEvent.VK_D, () -> dataCollection.writeArray(steering.getPixels(), "ColorDataSim2.txt"));

        for (int idx = 0; idx < PAST_STEERING_ANGLES; idx++) {
            pastSteeringAngles.add(0);
        }

        if (control instanceof CamControl) {
            switch (STEERING_VERSION) {
                case 1:
                    steering = new SteeringMk1(control);
                    break;
                case 2:
                    steering = new SteeringMk2(control);
                    break;
                case 3:
                    //steering = new SteeringMk3(control);
                    break;
                case 4:
                    steering = new SteeringMk4(control);
                    break;
                case 5:
                    break;
            }
        } else {
            switch (STEERING_VERSION) {
                case 1:
                    steering = new SteeringMk1(640, 480, 912);
                    break;
                case 2:
                    steering = new SteeringMk2(640, 480, 912);
                    break;
                case 3:
                    //steering = new SteeringMk3(640, 480, 912);
                    break;
                case 4:
                    steering = new SteeringMk4(640, 480, 912);
                    break;
                case 5:
                    break;

            }
        }

    }

    @Override
    public void update(CarControl control) {

        int angleSum = steering.getSteeringAngle(control.getRGBImage());
        for (Integer angle : pastSteeringAngles) {
            angleSum += angle;
        }
        int averagedSteerAngle = (int) Math.round(((double) angleSum) / (pastSteeringAngles.size() + 1));

        int lastAngle = pastSteeringAngles.get(pastSteeringAngles.size() - 1);
        if (Math.abs(averagedSteerAngle - lastAngle) > MAX_STEER_DIFFERENCE) {
            if (averagedSteerAngle > lastAngle) {
                averagedSteerAngle = lastAngle + MAX_STEER_DIFFERENCE;
            } else {
                averagedSteerAngle = lastAngle - MAX_STEER_DIFFERENCE;
            }
        }

        pastSteeringAngles.add(averagedSteerAngle);
        pastSteeringAngles.remove(0);
        control.steer(true, averagedSteerAngle);
        if (!steering.getMidPoints().isEmpty()) {
            Point furthestPoint = steering.getMidPoints().get(steering.getMidPoints().size() - 1);
            control.setFutureSteeringAngle((int) steering.getFutureSteepness(furthestPoint));
        }
    }


    @Override
    public void paint(CarControl control, Graphics g) {

        if (DRAW_STEERING_LINES) {

            for (int idx = 0; idx < steering.getMidPoints().size(); idx++) {
                if (idx >= steering.getStartTarget() && idx <= steering.getEndTarget()) {
                    g.setColor(TARGET_POINT_COLOR);
                    g.fillRect(steering.getMidPoints().get(idx).x - 2, steering.getMidPoints().get(idx).y,
                            4, 4);
                } else {
                    g.setColor(MIDPOINT_COLOR);
                    g.fillRect(steering.getMidPoints().get(idx).x - 2, steering.getMidPoints().get(idx).y,
                            4, 4);
                }
            }

            // Draw left and right sides
            if (DriverCons.D_DrawOnSides) {
                g.setColor(LEFT_LANE_COLOR);
                for (com.apw.steering.steeringclasses.Point point : steering.getLeftPoints()) {
                    int xL = point.x - 4;
                    int yL = point.y - 4;
                    g.fillRect(xL, yL, 8, 8);
                }
                g.setColor(RIGHT_LANE_COLOR);
                for (Point point : steering.getRightPoints()) {
                    int xR = point.x - 4;
                    int yR = point.y - 4;
                    g.fillRect(xR, yR, 8, 8);
                }
            }
            g.setColor(STEER_POINT_COLOR);
            g.fillRect(steering.getSteerPoint().x - 5, steering.getSteerPoint().y, 10, 10);

        }
    }

}

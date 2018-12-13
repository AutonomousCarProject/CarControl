package com.apw.steering.steeringversions;

import com.apw.carcontrol.CarControl;
import com.apw.steering.steeringclasses.Point;
import com.apw.steering.steeringclasses.PolynomialEquation;
import com.apw.steering.SteeringConstants;

import java.awt.*;

import static com.apw.steering.SteeringConstants.LEFT_LANE_COLOR;
import static com.apw.steering.SteeringConstants.START_SEARCH;

public class SteeringMk5 extends SteeringMk4 {

    public SteeringMk5(CarControl control) {
        super(control);
    }

    public SteeringMk5(int cameraWidth, int cameraHeight, int screenWidth) {
        super(cameraWidth, cameraHeight, screenWidth);
    }

    @Override
    public int getSteeringAngle(int[] pixels) {
        setPixels(pixels);
        super.getLeftLine().clearPoints();
        super.getRightLine().clearPoints();
        findLaneLines(pixels, getCameraHeight() - START_SEARCH, getCameraWidth() / 2, getCameraWidth() / 2);



        return 0;
    }

    public void paint(Graphics g) {
        g.setColor(LEFT_LANE_COLOR);
        for (Point point : getLeftLine().getNonEmptyPoints()) {
            g.fillRect(point.getX(), point.getY(), 5, 5);
        }
    }
}

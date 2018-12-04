package com.apw.steering.steeringversions;

import com.apw.carcontrol.CarControl;
import com.apw.steering.steeringclasses.LaneLine;
import com.apw.steering.steeringclasses.Point;
import java.util.ArrayList;
import static com.apw.steering.SteeringConstants.START_SEARCH;

public class SteeringMk5 extends SteeringMk4 {

    public SteeringMk5(int cameraWidth, int cameraHeight, int screenWidth) {
        super(cameraWidth, cameraHeight, screenWidth);
    }

    public SteeringMk5(CarControl control) {
        super(control);
    }

    public int getSteeringAngle(int pixels[]) {
        setPixels(pixels);
        getLeftLine().clearPoints();
        getRightLine().clearPoints();
        findLaneLines(pixels, getCameraHeight() - START_SEARCH, getCameraWidth() / 2, getCameraWidth() / 2);
        removeOutliers(getLeftLine().getNonEmptyPoints());
        //removeOutliers();

        return 0;
    }

    private void ininializeLaneLines(ArrayList<LaneLine> laneLines, LaneLine laneLine) {
        ArrayList<Point> nonEmptyPoints = laneLine.getNonEmptyPoints();

        for (int numArray = 0; numArray < 4; numArray++) {
            LaneLine line = new LaneLine();
            //for ()
        }
    }

    private void initializeLaneLines(ArrayList<LaneLine> laneLines, LaneLine laneLine, int sizeOfArray) {
        //int number
    }

    private int numberOf1ToAdd(int sizeOfArray, int numberOfArray) {
        return sizeOfArray - (numberOfArray * (sizeOfArray / numberOfArray));
    }

    private Point findIntersectionPoint() {
        int intersectionX = (int) ((getLeftLine().getB() - getRightLine().getB()) /
                (getRightLine().getSlope() - getLeftLine().getSlope()));
        return new Point(intersectionX, (int) getLeftLine().calculateApproxValueAtX(intersectionX));
    }
}

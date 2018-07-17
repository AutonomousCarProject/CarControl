package Steering;

import apw3.DriverCons;

public class Steering {
	public Point[] leftPoints = new Point[32];
	public Point[] rightPoints = new Point[32];
	
	public Point[] midPoints = new Point[32];
	
	//767 is white
	
	private int heightOfArea = 32;
	private int startingHeight = 272;
	
	private int screenWidth = 912;
	private int cameraWidth = 640;
	private int screenHeight = DriverCons.D_ImHi;
	private Point steerPoint = new Point(0, 0);
	
	private Point origin = new Point(cameraWidth/2, screenHeight);
	
	private Boolean found = false;
	
	public Steering() {
		for (int i = 0; i<32; i++) {
			leftPoints[i] = new Point(0, 0);
			rightPoints[i] = new Point(0, 0);
			midPoints[i] = new Point(0, 0);
		}
	}
	
	public Point[] findPoints(int[] pixels) {
		int roadMiddle = cameraWidth;

		for (int i = 0; i<heightOfArea; i++) {
			//center to left
			found = false;
			leftPoints[i].y = startingHeight + i;
			
			for (int j = roadMiddle/2; j>=0; j--) {
				if (pixels[(screenWidth * (i + startingHeight)) + j] == 16777215) {
					leftPoints[i].x = j;
					found = true;
					break;
				}
				
			}
			if (found == false) {
				leftPoints[i].x = 0;
			}
			
			
			//center to right
			found = false;
			rightPoints[i].y = leftPoints[i].y;
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[(screenWidth * (i + startingHeight)) + j] == 16777215) {
					rightPoints[i].x = j;
					found = true;
					break;
				}
			}
			if (found == false) {
				rightPoints[i].x = cameraWidth;
			}
			roadMiddle = (leftPoints[i].x + rightPoints[i].x);
			midPoints[i].x = (leftPoints[i].x + rightPoints[i].x)/2;
			midPoints[i].y = (leftPoints[i].y);
		}
		return midPoints;
	}
	
	public double curveSteepness(double turnAngle) {
		return turnAngle/(90);
	}


	/*
	find the average point from the midpoints array
	 */
	public void averageMidpoints() {
        double tempY = 0;
        double tempX = 0;

        // Sum the x's and the y's
	    for (Point point: midPoints) {
            tempX += point.x;
            tempY += point.y;
        }

        // assign the steerPoint to the average x's and y's
        steerPoint.x = (int)(tempX / midPoints.length);
	    steerPoint.y = (int)(tempY / midPoints.length);
    }


    public int getDegreeOffset() {
	    int xOffset = origin.x - steerPoint.x;
	    int yOffset = Math.abs(origin.y - steerPoint.y);

	    Point[] tempMidPoints = midPoints;
	    int tempDeg = (int)((Math.atan2(-xOffset, yOffset)) * (180 / Math.PI));

	    return (int)((Math.atan2(-xOffset, yOffset)) * (180 / Math.PI));
    }
}
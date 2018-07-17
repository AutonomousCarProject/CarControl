package Steering;

import apw3.DriverCons;

public class Steering {
	public Point[] leftPoints = new Point[32];
	public Point[] rightPoints = new Point[32];
	
	public Point[] midPoints = new Point[32];
	
	public int startingPoint = 0;
	
	//767 is white

	public int heightOfArea = 32;
	public int startingHeight = 272;

	private int screenWidth = 912;
	private int cameraWidth = 640;
	private int screenHeight = DriverCons.D_ImHi;
	private Point steerPoint = new Point(0, 0);

	public Point[] leadingMidPoints = new Point[startingHeight + heightOfArea];
	
	Point origin = new Point(screenWidth/2, screenHeight);
	
	Boolean found = false;
	Boolean leftSideFound = false;
	Boolean rightSideFound = false;

	public Steering() {
		for (int i = 0; i<32; i++) {
			leftPoints[i] = new Point(0, 0);
			rightPoints[i] = new Point(0, 0);
			midPoints[i] = new Point(0, 0);
		}
		for (int i = 0; i<leadingMidPoints.length; i++) {
			leadingMidPoints[i] = new Point(0, 0);
		}
	}
	
	public Point[] findPoints(int[] pixels) {
		int roadMiddle = cameraWidth;
		int leftSideTemp = 0;
		int rightSideTemp = 0;
		startingPoint = 0;
		
		//first, find where road starts on both sides
		for (int i = screenHeight - 1; i>startingHeight + heightOfArea; i--) {
			
			for (int j = roadMiddle/2; j>=0; j--) {
				if (pixels[(screenWidth * (i)) + j] == 16777215) {
					leftSideFound = true;
					break;
				}
			}
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[(screenWidth * (i)) + j] == 16777215) {
					rightSideFound = true;
					break;
				}
			}

			if (leftSideFound && rightSideFound) {
				startingPoint = i;
				leftSideFound = false;
				rightSideFound = false;
				break;
			}
			leftSideFound = false;
			rightSideFound = false;
		}
		
		//Next, calculate the roadpoint 
		
		int count = 0;
		
		for (int i = startingPoint; i > startingHeight + heightOfArea; i--) {
			for (int j = roadMiddle/2; j>=0; j--) {
				if (pixels[screenWidth * i + j] == 16777215) {
					leftSideTemp = j;
					break;
				}
			}
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[screenWidth * i + j] == 16777215) {
					rightSideTemp = j;
					break;
				}
			}
			
			leadingMidPoints[count].x = roadMiddle / 2;
			leadingMidPoints[count].y = i;
			count++;
			roadMiddle = leftSideTemp + rightSideTemp;
		}
		
		count = 0;
		for (int i = startingHeight + heightOfArea; i>startingHeight; i--) {
			//center to left
			found = false;
			leftPoints[count].y = i;
			
			for (int j = roadMiddle/2; j>=0; j--) {
				if (pixels[screenWidth * i + j] == 16777215) {
					leftPoints[count].x = j;
					found = true;
					break;
				}
				
			}
			if (found == false) {
				leftPoints[count].x = 0;
			}
			
			
			//center to right
			found = false;
			rightPoints[count].y = leftPoints[count].y;
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[screenWidth * i + j] == 16777215) {
					rightPoints[count].x = j;
					found = true;
					break;
				}
			}
			if (found == false) {
				rightPoints[count].x = cameraWidth;
			}
			
			midPoints[count].x = roadMiddle/2;
			midPoints[count].y = (leftPoints[count].y);
			roadMiddle = (leftPoints[count].x + rightPoints[count].x);
			count++;
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


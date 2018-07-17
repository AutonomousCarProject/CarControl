package Steering;

import apw3.DriverCons;

public class Steering {
	public point[] leftPoints = new point[32];
	public point[] rightPoints = new point[32];
	
	public point[] midPoints = new point[32];
	
	public int startingPoint = 0;
	
	//767 is white
	
	public int heightOfArea = 32;
	public int startingHeight = 272;
	
	int screenWidth = 912;
	int cameraWidth = 640;
	int screenHeight = DriverCons.D_ImHi;
	
	public point[] leadingMidPoints = new point[startingHeight + heightOfArea];
	
	point origin = new point(screenWidth/2, screenHeight);
	
	Boolean found = false;
	Boolean leftSideFound = false;
	Boolean rightSideFound = false;
	
	public Steering() {
		for (int i = 0; i<32; i++) {
			leftPoints[i] = new point(0, 0);
			rightPoints[i] = new point(0, 0);
			midPoints[i] = new point(0, 0);
		}
		for (int i = 0; i<leadingMidPoints.length; i++) {
			leadingMidPoints[i] = new point(0, 0);
		}
	}
	
	public point[] findPoints(int[] pixels) {
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
	
	public double turnAngle(point tarPoint) {
		return Math.atan(Math.abs((origin.x-tarPoint.x)/(origin.y-tarPoint.y)))*(180/Math.PI);
	}
	
	public double curveSteepness(double turnAngle) {
		return turnAngle/(90);
	}
}

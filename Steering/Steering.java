package Steering;

import apw3.DriverCons;

public class Steering {

	public point[] leftPoints = new point[32];
	public point[] rightPoints = new point[32];
	
	public point[] midPoints = new point[32];
	
	//767 is white
	
	int heightOfArea = 32;
	int startingHeight = 272;
	
	int screenWidth = 912;
	int cameraWidth = 640;
	int screenHeight = DriverCons.D_ImHi;
	
	point origin = new point(screenWidth/2, screenHeight);
	
	Boolean found = false;
	
	public Steering() {
		for (int i = 0; i<32; i++) {
			leftPoints[i] = new point(0, 0);
			rightPoints[i] = new point(0, 0);
			midPoints[i] = new point(0, 0);
		}
	}
	
	public point[] findPoints(int[] pixels) {
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
	
	public double turnAngle(point tarPoint) {
		return Math.atan(Math.abs((origin.x-tarPoint.x)/(origin.y-tarPoint.y)))*(180/Math.PI);
	}
	
	public double curveSteepness(double turnAngle) {
		return turnAngle/(90);
	}
}


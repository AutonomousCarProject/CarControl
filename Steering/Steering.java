package Steering;

import apw3.DriverCons;

public class Steering {
<<<<<<< HEAD
	
	point[] leftPoints = new point[57];
	point[] rightPoints = new point[57];
=======
	public point[] leftPoints = new point[32];
	public point[] rightPoints = new point[32];
	
	public point[] midPoints = new point[32];
>>>>>>> 76f26e96c70a5bde34e3d862c309b999d602c9bc
	
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
			
			for (int j = roadMiddle; j>=0; j--) {
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
			for (int j = roadMiddle; j<cameraWidth; j++) {
				if (pixels[(screenWidth * (i + startingHeight)) + j] == 16777215) {
					rightPoints[i].x = j;
					found = true;
					break;
				}
			}
			if (found == false) {
				rightPoints[i].x = cameraWidth;
			}
			roadMiddle = (leftPoints[i].x + rightPoints[i].x) / 2;
			midPoints[i].x = (leftPoints[i].x + rightPoints[i].x)/2;
			midPoints[i].y = (leftPoints[i].y);
		}
		return midPoints;
	}
	
<<<<<<< HEAD
	public int getOffset(int[] pixels, int topBound, int bottomBound) {
		int currentOffset = 0;
		
		for (int i = topBound; i <= bottomBound; i++) {
			for (int j = 320; j < 640; j++) {
				//pixels[i * 640 + j];
			}
		}
		return currentOffset;
	}
	
	public double turnAngle(point tarPoin) {
		return arctan(Math.abs((origin.x-tarPoint.x)/(origin.y-tarPoint.y)))*(180/Math.PI);
	}
	
	public double curveSteepness(double turnAngle) {
		return turnAngle/(90);
	}
=======

>>>>>>> 76f26e96c70a5bde34e3d862c309b999d602c9bc
}

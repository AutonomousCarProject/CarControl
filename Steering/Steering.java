package Steering;

import apw3.DriverCons;

public class Steering {
	public point[] leftPoints = new point[57];
	public point[] rightPoints = new point[57];
	
	//767 is white
	
	int heightOfArea = 32;
	int startingHeight = 272;
	
	int screenWidth = 912;
	int cameraWidth = 640;
	int screenHeight = DriverCons.D_ImHi;
	
	Boolean found = false;
	
	public Steering() {
		for (int i = 0; i<57; i++) {
			leftPoints[i] = new point(0, 0);
			rightPoints[i] = new point(0, 0);
		}
	}
	
	public void findPoints(int[] pixels) {
		System.out.println(pixels.length);
		for (int i = 0; i<heightOfArea; i++) {
			//center to left
			found = false;
			leftPoints[i].y = startingHeight + i;
			
			for (int j = cameraWidth / 2; j>=0; j--) {
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
			for (int j =cameraWidth / 2; j<cameraWidth; j++) {
				if (pixels[(screenWidth * (i + startingHeight)) + j] == 16777215) {
					rightPoints[i].x = j;
					found = true;
					break;
				}
			}
			if (found == false) {
				rightPoints[i].x = cameraWidth;
			}
		}
	}
	

}

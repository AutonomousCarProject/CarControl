package Steering;

import apw3.DriverCons;

public class Steering {
	point[] leftPoints = new point[57];
	point[] rightPoints = new point[57];
	
	//767 is white
	
	int heightOfArea = 57;
	int startingHeight = 272;
	
	int screenWidth = DriverCons.D_ImWi;
	int screenHeight = DriverCons.D_ImHi;
	
	Boolean found = false;
	
	public Steering() {
		for (int i = 0; i<57; i++) {
			leftPoints[i] = new point(0, 0);
			rightPoints[i] = new point(0, 0);
		}
	}
	
	public void findPoints(int[] pixels) {
		for (int i = 0; i<heightOfArea; i++) {
			//center to left
			found = false;
			for (int j = screenWidth / 2; j>0; j--) {
				if (pixels[screenWidth * i + j] == 767) {
					leftPoints[i].x = j;
					leftPoints[i].y = startingHeight + i;
					found = true;
					break;
				}
			}
			if (found == false) {
				leftPoints[i].x = 0;
			}
			
			
			//center to right
			found = false;
			for (int j = screenWidth / 2; j<screenWidth; j++) {
				if (pixels[screenWidth * i + j] == 767) {
					rightPoints[i].x = j;
					rightPoints[i].y = startingHeight + i;
					break;
				}
			}
			if (found == false) {
				rightPoints[i].x = screenWidth;
			}
		}
	}
	
	public int getOffset(int[] pixels, int topBound, int bottomBound) {
		int currentOffset = 0;
		
		for (int i = topBound; i <= bottomBound; i++) {
			for (int j = 320; j < 640; j++) {
				//pixels[i * 640 + j];
			}
		}
		return currentOffset;
	}
}

package com.apw.Steering;


import com.apw.apw3.DriverCons;
import com.apw.apw3.TrakSim;

public class Steering {

	
	public int startingPoint = 0;

	
	//767 is white

	public int heightOfArea = 32;
	public int startingHeight = 272;

	private int screenWidth = 912;
	private int cameraWidth = 640;
	private int screenHeight = DriverCons.D_ImHi;
	public Point steerPoint = new Point(0, 0);

	public Point[] leadingMidPoints = new Point[startingHeight + heightOfArea];
	public Point[] leftPoints = new Point[heightOfArea];
	public Point[] rightPoints = new Point[heightOfArea];
	
	public Point[] midPoints = new Point[heightOfArea];
	Point origin = new Point(cameraWidth/2, screenHeight);
	
	Boolean found = false;
	Boolean leftSideFound = false;
	Boolean rightSideFound = false;
	
	private TrakSim theSim;
	
	private double integral, derivative, previousError;
	private double kP = 0.65;
	private double kI = 1;
	private double kD = 1;
	private boolean usePID = false;
	
	private double weight = 1;
	
	private long averageLuminance = 0;
	

	public Steering() {
		for (int i = 0; i<heightOfArea; i++) {
			leftPoints[i] = new Point(0, 0);
			rightPoints[i] = new Point(0, 0);
			midPoints[i] = new Point(0, 0);
		}
		for (int i = 0; i<leadingMidPoints.length; i++) {
			leadingMidPoints[i] = new Point(0, 0);
		}
		this.theSim = theSim;
	}
	
	public Point[] findPoints(int[] pixels) {
		int roadMiddle = cameraWidth;
		int leftSideTemp = 0;
		int rightSideTemp = 0;
		startingPoint = 0;
		averageLuminance = 0;
		Boolean first = true;
		int count = 0;
		//first before first, find average luminance
		for (int i = cameraWidth * screenHeight - 1; i > startingHeight * cameraWidth; i--) {
			averageLuminance = averageLuminance + pixels[i];
			count++;
		}
		averageLuminance = (long) (averageLuminance/count * 1.5);
		System.out.println("average luminance " + averageLuminance);
		count = 0;
		
		//first, find where road starts on both sides
		leftSideFound = false;
		rightSideFound = false;
		for (int i = screenHeight - 22; i>startingHeight + heightOfArea; i--) {
			for (int j = roadMiddle/2; j>=0; j--) {
				if (pixels[(screenWidth * (i)) + j] >= averageLuminance) {
					leftSideFound = true;
					break;
				}
			}
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[(screenWidth * (i)) + j] >= averageLuminance) {
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
		
		count = 0;
		
		for (int i = startingPoint; i > startingHeight + heightOfArea; i--) {
			for (int j = roadMiddle/2; j>=0; j--) {
				if (pixels[screenWidth * i + j]  >= averageLuminance) {
					leftSideTemp = j;
					break;
				}
			}
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[screenWidth * i + j]  >= averageLuminance) {
					rightSideTemp = j;
					break;
				}
			}
			leadingMidPoints[count].x = (int) ((double) roadMiddle / 2.0 * weight);
			leadingMidPoints[count].y = i;
			count++;
			roadMiddle = leftSideTemp + rightSideTemp;
		}
		int tempCount = count;
		count = 0;
		for (int i = startingHeight + heightOfArea; i>startingHeight; i--) {
			//center to left
			found = false;
			leftPoints[count].y = i;
			
			for (int j = roadMiddle/2; j>=0; j--) {
				if (pixels[screenWidth * i + j] >= averageLuminance) {
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
				if (pixels[screenWidth * i + j] >= averageLuminance) {
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

	public boolean checkMidpoints(Point[] points, byte pixels[], int rowLength){
		int invalidPoints = 0;
		for(int i = 0; i < points.length; i++){
			if(pixels[points[i].x+rowLength*points[i].y]!=4 && pixels[points[i].x+rowLength*points[i].y]!=5){
				invalidPoints++;
			}
		}
		if(invalidPoints == points.length){
			return false;
		}
		return true;
	}

	public Point[] findPointsLeft(int[] pixels) {
		int roadMiddle = cameraWidth;
		int leftSideTemp = 0;
		int rightSideTemp = 0;
		startingPoint = 0;
		averageLuminance = 0;
		Boolean first = true;
		int count = 0;
		//first before first, find average luminance
		for (int i = cameraWidth * screenHeight - 1; i > startingHeight * cameraWidth; i--) {
			averageLuminance = averageLuminance + pixels[i];
			count++;
		}
		averageLuminance = (long) (averageLuminance/count * 1.5);
		System.out.println("average luminance " + averageLuminance);
		count = 0;

		//first, find where road starts on both sides
		leftSideFound = false;
		rightSideFound = false;
		for (int i = screenHeight - 22; i>startingHeight + heightOfArea; i--) {
			for (int j = 0; j < roadMiddle/2; j++) {
				if (pixels[(screenWidth * (i)) + j] >= averageLuminance) {
					leftSideFound = true;
					break;
				}
			}
			for (int j = roadMiddle/2; j > 0; j--) {
				if (pixels[(screenWidth * (i)) + j] >= averageLuminance) {
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

		count = 0;

		for (int i = startingPoint; i > startingHeight + heightOfArea; i--) {
			for (int j = 0; j < roadMiddle/2; j++) {
				if (pixels[screenWidth * i + j]  >= averageLuminance) {
					leftSideTemp = j;
					break;
				}
			}
			for (int j = roadMiddle/2; j > 0; j--) {
				if (pixels[screenWidth * i + j]  >= averageLuminance) {
					rightSideTemp = j;
					break;
				}
			}
			leadingMidPoints[count].x = (int) ((double) roadMiddle / 2.0 * weight);
			leadingMidPoints[count].y = i;
			count++;
			roadMiddle = leftSideTemp + rightSideTemp;
		}
		int tempCount = count;
		count = 0;
		for (int i = startingHeight + heightOfArea; i>startingHeight; i--) {
			//center to left
			found = false;
			leftPoints[count].y = i;

			for (int j = 0; j < roadMiddle/2; j++) {
				if (pixels[screenWidth * i + j] >= averageLuminance) {
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
			for (int j = roadMiddle/2; j > 0; j--) {
				if (pixels[screenWidth * i + j] >= averageLuminance) {
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

	public Point[] findPointsRight(int[] pixels) {
		int roadMiddle = cameraWidth;
		int leftSideTemp = 0;
		int rightSideTemp = 0;
		startingPoint = 0;
		averageLuminance = 0;
		Boolean first = true;
		int count = 0;
		//first before first, find average luminance
		for (int i = cameraWidth * screenHeight - 1; i > startingHeight * cameraWidth; i--) {
			averageLuminance = averageLuminance + pixels[i];
			count++;
		}
		averageLuminance = (long) (averageLuminance/count * 1.5);
		System.out.println("average luminance " + averageLuminance);
		count = 0;

		//first, find where road starts on both sides
		leftSideFound = false;
		rightSideFound = false;
		for (int i = screenHeight - 22; i>startingHeight + heightOfArea; i--) {
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[(screenWidth * (i)) + j] >= averageLuminance) {
					leftSideFound = true;
					break;
				}
			}
			for (int j = cameraWidth; j > roadMiddle/2; j--) {
				if (pixels[(screenWidth * (i)) + j] >= averageLuminance) {
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

		count = 0;

		for (int i = startingPoint; i > startingHeight + heightOfArea; i--) {
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[screenWidth * i + j]  >= averageLuminance) {
					leftSideTemp = j;
					break;
				}
			}
			for (int j = cameraWidth; j > roadMiddle/2; j--) {
				if (pixels[screenWidth * i + j]  >= averageLuminance) {
					rightSideTemp = j;
					break;
				}
			}
			leadingMidPoints[count].x = (int) ((double) roadMiddle / 2.0 * weight);
			leadingMidPoints[count].y = i;
			count++;
			roadMiddle = leftSideTemp + rightSideTemp;
		}
		int tempCount = count;
		count = 0;
		for (int i = startingHeight + heightOfArea; i>startingHeight; i--) {
			//center to left
			found = false;
			leftPoints[count].y = i;

			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[screenWidth * i + j] >= averageLuminance) {
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
			for (int j = cameraWidth; j > roadMiddle/2; j--) {
				if (pixels[screenWidth * i + j] >= averageLuminance) {
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
		return Math.abs(turnAngle)/(45);
	}


	/*
	find the average point from the midpoints array
	 */
	public void averageMidpoints() {
        double tempY = 0;
        double tempX = 0;
        int tempCount = 0;

        // Sum the x's and the y's
	    for (int i = 0; i<startingPoint - (startingHeight + heightOfArea); i++) {
	    		Point point = new Point (leadingMidPoints[i].x, leadingMidPoints[i].y);
            tempX += point.x;
            tempY += point.y;
            tempCount++;
        }
	    if (tempCount == 0) {
		    for (int i = 0; i<midPoints.length; i++) {
	    			Point point = new Point (midPoints[i].x, midPoints[i].y);
	    			tempX += point.x;
	    			tempY += point.y;
	    			tempCount++;
		    }
	    }

        steerPoint.x = (int)(tempX / tempCount);
	    steerPoint.y = (int)(tempY / tempCount);

    }


    public double getDegreeOffset() {
	    int xOffset = origin.x - steerPoint.x;
	    int yOffset = Math.abs(origin.y - steerPoint.y);

	    int tempDeg = (int)((Math.atan2(-xOffset, yOffset)) * (180 / Math.PI));
	    
	    
	    return ((Math.atan2(-(usePID?myPID():xOffset), yOffset)) * (180 / Math.PI));
    }
    
    public double myPID() {
 
    		int error = origin.x - steerPoint.x;
    		
    		integral += error * DriverCons.D_FrameTime;
    		if (error == 0 || (Math.abs(error-previousError)==(Math.abs(error)+Math.abs(previousError)))) {
    			integral = 0;
    		}
    		if (Math.abs(integral) > 100) {
    			integral = 0;
    		}

    		derivative = (error - previousError)/DriverCons.D_FrameTime;
    		previousError = error;
    		return error*kP + integral*kI + derivative*kD;
 
    }
}

package com.apw.steering;

import java.awt.Color;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.apw.steering.*;

import com.apw.steering.Point;


public class SteeringMk3 extends SteeringBase {
	
	private double sumOfAngles = 0;
	private double locX = 0;
	private double locY = 0;
	private double prevAngle = 0;
	private int ranThroughOrigin = 0;

	private int frameCount = 0;
	public boolean drawnMap = false;
	private int count = 0;
	private int mapPos = 0;

	public Point[] leftEdge;
	public Point[] rightEdge;
	public Point[] pathTraveled;
	public boolean[] onCurve;

	private final int heightOfArea = 32; // How high the car looks for lines
	private final int startingHeight = 400; // how high the car starts looking for lines

	private int startingPoint = 0; // Where the car starts looking for lines on either side.
	private Point[] leadingMidPoints = new Point[startingHeight + heightOfArea];
	private Point[] pointsAhead = new Point[startingHeight - (cameraHeight / 4)]; // points far ahead
	private double weight = 1.0; // >1 = right lane, <1 = left lane
	private boolean weightLane = false;
	private boolean turnAhead = false;
	private boolean turnRightAhead = false;

	public List<Double> posLog = new ArrayList<Double>();
	ArrayList<ArrayList<Integer>> curveLog = new ArrayList<ArrayList<Integer>>(1);
	private int curveNo = 0;
	public ArrayList<Point> curvePos = new ArrayList<Point>();

	/**
	 * Constructor that Initializes all points in array.
	 */
	public SteeringMk3() {
		for (int i = 0; i < heightOfArea; i++) {
			leftPoints.add(new Point(0, 0));
			rightPoints.add(new Point(0, 0));
			midPoints.add(new Point(0, 0));
		}
		for (int i = 0; i < leadingMidPoints.length; i++) {
			leadingMidPoints[i] = new Point(0, 0);
		}
	}

	public int drive(int pixels[]) {
		int angleToTurn = 0;
		//System.out.println(ranThroughOrigin);
		if (ranThroughOrigin == -1) {
			//System.out.println("here");
			mapPos = mapPos % (posLog.size() / 4);

			double xOffset1 = (posLog.get((mapPos + 1) * 4) - posLog.get((mapPos) * 4));
			double yOffset1 = ((posLog.get((mapPos + 1) * 4 + 1) - posLog.get(mapPos * 4 + 1)));
			double xOffset2 = (posLog.get((mapPos + 2) * 4) - posLog.get((mapPos + 1) * 4));
			double yOffset2 = ((posLog.get((mapPos + 2) * 4 + 1) - posLog.get((mapPos + 1) * 4 + 1)));
			double slope1;
			if (yOffset1 != 0)
				slope1 = yOffset1 / xOffset1;
			else
				slope1 = 0;
			double slope2;
			if (yOffset2 != 0)
				slope2 = yOffset2 / xOffset2;
			else
				slope2 = 0;
			double angleTurned = Math.toDegrees(Math.atan(slope2)) - Math.toDegrees(Math.atan(slope1));

			if (angleTurned < 0)
				angleTurned = angleTurned * (double) com.apw.apw3.DriverCons.D_RiteSteer
						/ (double) com.apw.apw3.DriverCons.D_LeftSteer * 2;
			// angleTurned = Math.toRadians(angleTurned);
			if (angleTurned == 0)
				angleTurned = 0;

			double averageTurnRadius;
			double tempDeg;
			averageTurnRadius = ((double) com.apw.apw3.DriverCons.D_FrameTime / 1000.0)
					* com.apw.apw3.DriverCons.D_fMinSpeed / angleTurned;
			tempDeg = (Math.toDegrees(Math.atan(Math.toRadians(2.68 / averageTurnRadius))) + 2.68669) / 0.650924;
			// tempDeg * .665987 - 2.73143
			// y=0.650924x-2.68669
			if (angleTurned == 0) {
				tempDeg = 0;
			}

			mapPos++;
			mapPos = mapPos % (int) (posLog.size() / 4.0);

			angleToTurn = (int) tempDeg;
			//System.out.println(tempDeg + " " + posLog.get(mapPos * 4) + " " + posLog.get((mapPos + 1) * 4) + " "
					//+ posLog.get((mapPos + 2) * 4) + " " + angleTurned);
			

		} else {
			findPoints(pixels);
			averageMidpoints();
			angleToTurn = getDegreeOffset();

			double turnRadiusIn = 2.68 / Math.tan(Math.toRadians(angleToTurn * 0.7665 - 3.0287));
			double turnRadiusOut = 2.68 / Math.tan(Math.toRadians(angleToTurn * 0.5654 - 2.4337));
			double averageTurnRadius = (turnRadiusIn + turnRadiusOut) / 2;
			double angleTurned = ((double) com.apw.apw3.DriverCons.D_FrameTime / 1000.0)
					* com.apw.apw3.DriverCons.D_fMinSpeed / averageTurnRadius;

			angleTurned = Math.toDegrees(angleTurned);
			if (angleToTurn < 0) {
				angleTurned = angleTurned * (double) com.apw.apw3.DriverCons.D_LeftSteer
						/ (double) com.apw.apw3.DriverCons.D_RiteSteer / 2.0;
			}
			if (angleToTurn == 0)
				angleTurned = 0;
			// if (prevAngle == 0 && Math.abs(tempDeg) <= 0.8) angleTurned = 0;

			sumOfAngles += angleTurned;
			sumOfAngles = sumOfAngles % 360;

			locX = locX + (double) Math.cos(Math.toRadians(sumOfAngles)) * (double) com.apw.apw3.DriverCons.D_FrameTime
					/ 1000.0 * (double) com.apw.apw3.DriverCons.D_fMinSpeed;

			locY = locY + (double) Math.sin(Math.toRadians(sumOfAngles)) * (double) com.apw.apw3.DriverCons.D_FrameTime
					/ 1000.0 * (double) com.apw.apw3.DriverCons.D_fMinSpeed;
			if (frameCount < 100)
				frameCount++;
			if (ranThroughOrigin == 0 && frameCount >= 100 && locX >= 4 && (locX) <= 7 && Math.abs(locY) < 15
					&& Math.abs(angleTurned) < 3 && !drawnMap
					&& (((Math.abs(360 + sumOfAngles % 360) < 30)) || Math.abs(sumOfAngles % 360) < 30)) {
				//System.out.println("found origin");
				drawMapArrays();
				drawnMap = true;
				ranThroughOrigin++;
				sumOfAngles = 0;
			} else if (ranThroughOrigin == -1 && frameCount >= 100 && locX >= 4 && (locX) <= 7 && Math.abs(locY) < 15
					&& Math.abs(angleToTurn) < 3 && !drawnMap
					&& (((Math.abs(360 + sumOfAngles % 360) < 30)) || Math.abs(sumOfAngles % 360) < 30)) {
				ranThroughOrigin++;
				frameCount = 0;
				if ((Math.abs(180 - sumOfAngles) < 10 || Math.abs(180 + sumOfAngles) < 10)
						&& Math.abs(angleToTurn) <= 1) {
					sumOfAngles = 180;
				}
				if (Math.abs(sumOfAngles) < 10 && Math.abs(angleToTurn) <= 1) {
					sumOfAngles = 0;
				}
				updatePosLog(locX, locY, sumOfAngles);

				sumOfAngles = 0;
			} else if (!drawnMap) {
				if ((Math.abs(180 - sumOfAngles) < 10 || Math.abs(180 + sumOfAngles) < 10)
						&& Math.abs(angleToTurn) <= 1) {
					sumOfAngles = 180;
				}
				if (Math.abs(sumOfAngles) < 10 && Math.abs(angleToTurn) <= 1) {
					sumOfAngles = 0;
				}
				updatePosLog((double) locX, (double) locY, (double) sumOfAngles);
			}
			//System.out.println(sumOfAngles);
			System.out.println(locX + " " + locY + " " + angleToTurn + " " + sumOfAngles);
		}
		if (angleToTurn < 0)
			angleToTurn = (int) (angleToTurn * 4.0 / 3.0);
		
		
		return angleToTurn;

	}

	/**
	 * <p>
	 * Process the image data From an array of pixels. Starts at the startingHeight
	 * (up from the bottom)screen, then works up the screen going out from the
	 * center. It looks for pixels that are higher than average luminance, and
	 * recognises that as a line.
	 * </p>
	 *
	 * @param pixels
	 *            An array of pixels (the image)
	 */
	public void findPoints(int[] pixels) {
		int roadMiddle = cameraWidth;
		int leftSideTemp = 0;
		int rightSideTemp = 0;
		startingPoint = 0;
		long averageLuminance = 0;
		Boolean leftSideFound = false;
		Boolean rightSideFound = false;
		Boolean first = true;
		Boolean found = false;
		int count = 0;
		// first before first, find average luminance
		for (int i = cameraWidth * cameraHeight - 1; i > startingHeight * cameraWidth; i--) {
			averageLuminance = averageLuminance + pixels[i];
			count++;
		}
		averageLuminance = (long) (averageLuminance / count * 1.5);
		count = 0;

		// first, find where road starts on both sides
		leftSideFound = false;
		rightSideFound = false;
		for (int i = cameraHeight - 22; i > startingHeight + heightOfArea; i--) {
			for (int j = roadMiddle / 2; j >= 0; j--) {
				if (pixels[(screenWidth * (i)) + j] >= averageLuminance) {
					leftSideFound = true;
					break;
				}
			}
			for (int j = roadMiddle / 2; j < cameraWidth; j++) {
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

		// Next, calculate the roadpoint

		count = 0;

		for (int i = startingPoint; i > startingHeight + heightOfArea; i--) {
			for (int j = roadMiddle / 2; j >= 0; j--) {
				if (pixels[screenWidth * i + j] >= averageLuminance) {
					leftSideTemp = j;
					break;
				}
			}
			for (int j = roadMiddle / 2; j < cameraWidth; j++) {
				if (pixels[screenWidth * i + j] >= averageLuminance) {
					rightSideTemp = j;
					break;
				}
			}

			if (weightLane && midPoints != null) {
				averageMidpoints();
				checkPointsAhead(pixels);
				if (turnAhead) {
					if (turnRightAhead)
						weight = 1.25;
					else
						weight = 0.85;
				} else
					weight = 1;
			}

			leadingMidPoints[count].x = weightLane ? ((int) ((double) roadMiddle / 2.0 * weight)) : roadMiddle / 2;
			leadingMidPoints[count].y = i;
			count++;
			roadMiddle = leftSideTemp + rightSideTemp;
		}
		int tempCount = count;
		count = 0;
		for (int i = startingHeight + heightOfArea; i > startingHeight; i--) {
			// center to left
			found = false;
			leftPoints.get(count).y = i;

			for (int j = roadMiddle / 2; j >= 0; j--) {
				if (pixels[screenWidth * i + j] >= averageLuminance) {
					leftPoints.get(count).x = j;
					found = true;
					break;
				}

			}
			if (found == false) {
				leftPoints.get(count).x = 0;
			}

			// center to right
			found = false;
			rightPoints.get(count).y = leftPoints.get(count).y;
			for (int j = roadMiddle / 2; j < cameraWidth; j++) {
				if (pixels[screenWidth * i + j] >= averageLuminance) {
					rightPoints.get(count).x = j;
					found = true;
					break;
				}

			}
			if (found == false) {
				rightPoints.get(count).x = cameraWidth;
			}

			midPoints.get(count).x = roadMiddle / 2;
			midPoints.get(count).y = (leftPoints.get(count).y);
			roadMiddle = (leftPoints.get(count).x + rightPoints.get(count).x);
			count++;
		}
	}

	/**
	 * Average the points above the target area.
	 *
	 * @param pixels
	 *            Array of pixels (The image)
	 * @return The average Point
	 */
	Point avgPointAhead(int[] pixels) { // Look ahead in the road to see if there is a turn (for following the racing
										// curve)
		int roadMiddle = cameraWidth;
		int leftSideTemp = 0;
		int rightSideTemp = 0;
		boolean foundTurn = false;
		startingPoint = 0;

		// Next, calculate the roadpoint

		int count = 0;

		for (int i = startingHeight; i > cameraHeight / 2; i--) {
			for (int j = roadMiddle / 2; j >= 0; j--) {
				if (pixels[screenWidth * i + j] == 16777215) {
					leftSideTemp = j;
					break;
				}
			}
			for (int j = roadMiddle / 2; j < cameraWidth; j++) {
				if (pixels[screenWidth * i + j] == 16777215) {
					rightSideTemp = j;
					break;
				}
			}
			pointsAhead[count].x = roadMiddle / 2;
			pointsAhead[count].y = i;
			count++;
			roadMiddle = leftSideTemp + rightSideTemp;
		}

		double tempY = 0;
		double tempX = 0;
		int avgX;
		int avgY;
		int tempCount = 0;

		for (int i = 0; i < pointsAhead.length; i++) {
			Point point = new Point(leadingMidPoints[i].x, leadingMidPoints[i].y);
			tempX += point.x;
			tempY += point.y;
			tempCount++;
		}

		avgX = (int) (tempX / tempCount);
		avgY = (int) (tempY / tempCount);

		return new Point(avgX, avgY);
	}

	/**
	 * Checks the average point above the target area to see if a turn is coming up
	 * and which direction the turn is going
	 *
	 * @param pixels
	 *            Array of pixels (The Image)
	 */
	void checkPointsAhead(int[] pixels) {
		Point ahead = avgPointAhead(pixels);
		if (Math.abs(ahead.x - origin.x) >= Math
				.abs((steerPoint.x - origin.x) / (steerPoint.y - origin.y) * (ahead.y - origin.y))) {
			turnAhead = true;
			if (ahead.x - origin.x > 0)
				turnRightAhead = true;
			else
				turnRightAhead = false;
		} else
			turnAhead = false;
	}

	/**
	 * Average the midpoints, and assign the average to steerPoint.
	 */
	private void averageMidpoints() {
		double tempY = 0;
		double tempX = 0;
		int tempCount = 0;
		int weightCount = 0;

		boolean shouldWeight = false;
		double weightFactor = 1;

		// Sum the x's and the y's
		for (int i = 0; i < startingPoint - (startingHeight + heightOfArea); i++) {
			Point point = new Point(leadingMidPoints[i].x, leadingMidPoints[i].y);
			if (i > (startingPoint - (startingHeight + heightOfArea)) / 2)
				shouldWeight = true;
			tempX += shouldWeight ? weightFactor * point.x : point.x;
			tempY += point.y;
			tempCount++;
			weightCount += shouldWeight ? weightFactor - 1 : 0;
		}
		shouldWeight = false;
		if (tempCount == 0) {
			for (int i = 0; i < midPoints.size(); i++) {
				Point point = new Point(midPoints.get(i).x, midPoints.get(i).y);
				if (i > midPoints.size() / 2)
					shouldWeight = true;
				tempX += shouldWeight ? weightFactor * point.x : point.x;
				tempY += point.y;
				tempCount++;
				weightCount += shouldWeight ? weightFactor - 1 : 0;
			}
		}

		steerPoint.x = (int) (tempX / tempCount + weightCount);
		steerPoint.y = (int) (tempY / tempCount);

	}

	/**
	 * Not sure what this does
	 *
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param heading
	 *            idk
	 */
	public void updatePosLog(double x, double y, double heading) { // Reference positions by doing point# * 3 + (0 for
																	// x, 1 for y, 2 for heading)
		posLog.add(x);
		posLog.add(y);
		posLog.add(heading);
	}

	public void drawMapArrays(){
		curveLog.add(new ArrayList<Integer>());
		
		int length = (int) ((double)posLog.size()/3.0);
		leftEdge = new Point[length];
		rightEdge = new Point[length];
		pathTraveled = new Point[length];
		onCurve = new boolean[length];
		double laneWidth = 30; // Needs to be measured
		int currentCurveSum = 0;
		for(int i = 0; i < length; i++){
			leftEdge[i] = new Point (0,0);
			rightEdge[i] = new Point (0,0);
			pathTraveled[i] = new Point (0,0);
			onCurve[i] = false;
		}
		for(int i = 0; i < length; i++){
			leftEdge[i].x = (int)(posLog.get(i*3) + laneWidth/2*Math.cos(posLog.get(i*3+2)+(Math.PI/2)));
			
			leftEdge[i].y = (int)(posLog.get(i*3+1) + laneWidth/2*Math.sin(posLog.get(i*3+2)+(Math.PI/2)));
			rightEdge[i].x = (int)(posLog.get(i*3) + laneWidth/2*Math.cos(posLog.get(i*3+2)-(Math.PI/2)));
			rightEdge[i].y = (int)(posLog.get(i*3+1) + laneWidth/2*Math.sin(posLog.get(i*3+2)-(Math.PI/2)));
			pathTraveled[i].x = (int)((double)posLog.get(i*3));
			pathTraveled[i].y = (int)((double)posLog.get(i*3+1));
			//dSystem.out.println(pathTraveled[i].x + " " + pathTraveled[i].y);
			if (i >= 1 && Math.abs(posLog.get(i*3+2)-(posLog.get((i-1)*3+2))) > 2) {
				if (onCurve[i-1] == false) {
					curvePos.add(new Point(pathTraveled[i].x, pathTraveled[i].y));
				}
				onCurve[i] = true;
				if (i >= 1 && i-1 != -1) {
					curveLog.get(curveNo).add((int) (posLog.get(i*3+2) - posLog.get((i-1)*3+2)));
					currentCurveSum += posLog.get(i*3+2) - posLog.get((i-1)*3+2);
				}
			}
			else {
				if (i!=0 && onCurve[i-1] == true) {
					
					if (Math.abs(currentCurveSum) < 50) {
						curveLog.remove(curveLog.size()-1);
						curveLog.add(new ArrayList<Integer>());
					}
					else {
						curvePos.add(new Point(pathTraveled[i].x, pathTraveled[i].y));
						curveLog.add(new ArrayList<Integer>());
						System.out.println("Curve " + (curveNo-1) + " = " + currentCurveSum);
						curveNo++;
					}
					currentCurveSum = 0;
				}
			}
		}
		
	}

}

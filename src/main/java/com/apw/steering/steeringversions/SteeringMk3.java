package com.apw.steering.steeringversions;

import java.util.ArrayList;
import java.util.List;

import com.apw.steering.steeringclasses.Point;
import static com.apw.steering.SteeringConstants.HEIGHT_OF_AREA;
import static com.apw.steering.SteeringConstants.STARTING_HEIGHT;


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

	private int startingPoint = 0; // Where the car starts looking for lines on either side.
	private Point[] leadingMidPoints = new Point[STARTING_HEIGHT + HEIGHT_OF_AREA];
	private Point[] pointsAhead = new Point[STARTING_HEIGHT - (getCameraHeight() / 2)]; // points far ahead
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
		for (int i = 0; i < HEIGHT_OF_AREA; i++) {
			getLeftPoints().add(new Point(0, 0));
			getRightPoints().add(new Point(0, 0));
			getMidPoints().add(new Point(0, 0));
		}
		for (int i = 0; i < leadingMidPoints.length; i++) {
			leadingMidPoints[i] = new Point(0, 0);
		}
	}

	public int getSteeringAngle(int pixels[]) {
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
			angleToTurn = getDegreeOffset(getOrigin(), getSteerPoint());

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
	 * Process the image data From an array of pixels. Starts at the STARTING_HEIGHT
	 * (up from the bottom)screen, then works up the screen going out from the
	 * center. It looks for pixels that are higher than average luminance, and
	 * recognises that as a line.
	 * </p>
	 *
	 * @param pixels
	 *            An array of pixels (the image)
	 */
	private void findPoints(int[] pixels) {
		int roadMiddle = getCameraWidth();
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
		for (int i = getCameraWidth() * getCameraHeight() - 1; i > STARTING_HEIGHT * getCameraWidth(); i--) {
			averageLuminance = averageLuminance + pixels[i];
			count++;
		}
		averageLuminance = (long) (averageLuminance / count * 1.5);
		count = 0;

		// first, find where road starts on both sides
		leftSideFound = false;
		rightSideFound = false;
		for (int i = getCameraHeight() - 22; i > STARTING_HEIGHT + HEIGHT_OF_AREA; i--) {
			for (int j = roadMiddle / 2; j >= 0; j--) {
				if (pixels[(getScreenWidth() * (i)) + j] >= averageLuminance) {
					leftSideFound = true;
					break;
				}
			}
			for (int j = roadMiddle / 2; j < getCameraWidth(); j++) {
				if (pixels[(getScreenWidth() * (i)) + j] >= averageLuminance) {
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

		for (int i = startingPoint; i > STARTING_HEIGHT + HEIGHT_OF_AREA; i--) {
			for (int j = roadMiddle / 2; j >= 0; j--) {
				if (pixels[getScreenWidth() * i + j] >= averageLuminance) {
					leftSideTemp = j;
					break;
				}
			}
			for (int j = roadMiddle / 2; j < getCameraWidth(); j++) {
				if (pixels[getScreenWidth() * i + j] >= averageLuminance) {
					rightSideTemp = j;
					break;
				}
			}

			if (weightLane && getMidPoints() != null) {
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
		for (int i = STARTING_HEIGHT + HEIGHT_OF_AREA; i > STARTING_HEIGHT; i--) {
			// center to left
			found = false;
			getLeftPoints().get(count).y = i;

			for (int j = roadMiddle / 2; j >= 0; j--) {
				if (pixels[getScreenWidth() * i + j] >= averageLuminance) {
					getLeftPoints().get(count).x = j;
					found = true;
					break;
				}

			}
			if (found == false) {
				getLeftPoints().get(count).x = 0;
			}

			// center to right
			found = false;
			getRightPoints().get(count).y = getLeftPoints().get(count).y;
			for (int j = roadMiddle / 2; j < getCameraWidth(); j++) {
				if (pixels[getScreenWidth() * i + j] >= averageLuminance) {
					getRightPoints().get(count).x = j;
					found = true;
					break;
				}

			}
			if (found == false) {
				getRightPoints().get(count).x = getCameraWidth();
			}

			getMidPoints().get(count).x = roadMiddle / 2;
			getMidPoints().get(count).y = (getLeftPoints().get(count).y);
			roadMiddle = (getLeftPoints().get(count).x + getRightPoints().get(count).x);
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
		int roadMiddle = getCameraWidth();
		int leftSideTemp = 0;
		int rightSideTemp = 0;
		boolean foundTurn = false;
		startingPoint = 0;

		// Next, calculate the roadpoint

		int count = 0;

		for (int i = STARTING_HEIGHT; i > getCameraHeight() / 2; i--) {
			for (int j = roadMiddle / 2; j >= 0; j--) {
				if (pixels[getScreenWidth() * i + j] == 16777215) {
					leftSideTemp = j;
					break;
				}
			}
			for (int j = roadMiddle / 2; j < getCameraWidth(); j++) {
				if (pixels[getScreenWidth() * i + j] == 16777215) {
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
		if (Math.abs(ahead.x - getOrigin().x) >= Math
				.abs((getSteerPoint().x - getOrigin().x) / (getSteerPoint().y - getOrigin().y) * (ahead.y - getOrigin().y))) {
			turnAhead = true;
			if (ahead.x - getOrigin().x > 0)
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
		for (int i = 0; i < startingPoint - (STARTING_HEIGHT + HEIGHT_OF_AREA); i++) {
			Point point = new Point(leadingMidPoints[i].x, leadingMidPoints[i].y);
			if (i > (startingPoint - (STARTING_HEIGHT + HEIGHT_OF_AREA)) / 2)
				shouldWeight = true;
			tempX += shouldWeight ? weightFactor * point.x : point.x;
			tempY += point.y;
			tempCount++;
			weightCount += shouldWeight ? weightFactor - 1 : 0;
		}
		shouldWeight = false;
		if (tempCount == 0) {
			for (int i = 0; i < getMidPoints().size(); i++) {
				Point point = new Point(getMidPoints().get(i).x, getMidPoints().get(i).y);
				if (i > getMidPoints().size() / 2)
					shouldWeight = true;
				tempX += shouldWeight ? weightFactor * point.x : point.x;
				tempY += point.y;
				tempCount++;
				weightCount += shouldWeight ? weightFactor - 1 : 0;
			}
		}

		getSteerPoint().x = (int) (tempX / tempCount + weightCount);
		getSteerPoint().y = (int) (tempY / tempCount);

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

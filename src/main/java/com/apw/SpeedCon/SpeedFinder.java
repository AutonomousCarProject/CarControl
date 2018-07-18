package com.apw.SpeedCon;


public class SpeedFinder {

	
	
	
	//It's the bad way, but it's a way
	double calculateSpeed(int gasAmount)
	{
		return gasAmount*Constants.PIN_TO_METER_PER_SECOND;
	}
	
	
	//Takes change in distance between two points and time taken to travel that distance
	//to generate a more accurate speed.
	double calculateSpeed(double distanceChange, double timeChange)
	{
		return distanceChange/timeChange;
	}
	
	
}

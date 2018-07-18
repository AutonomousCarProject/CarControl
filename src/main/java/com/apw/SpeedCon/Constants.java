package com.apw.SpeedCon;

public class Constants {
		public static final double 
		MAX_SPEED = 12,					//Car's maximum speed
		MIN_SPEED = 4,					//Car's minimum speed
		PIN_TO_METER_PER_SECOND = 0.4,	//Conversion for motor position to m/s. DO NOT TOUCH
		GRAV = 9.80665,					//Average gravitational acceleration
		SQRT_GRAV = 3.132,				//The square root of the average gravitational acceleration
		FRICT = 0.75,					//Given coefficient of friction
		SQRT_FRICT = 0.866;				//The square root of the given coefficient of friction
		
		public static final int
		DRIFT_TO_STOPSIGN_FRAMES = 30,	//Frames to drift forward after stopsign detected
		WAIT_AT_STOPSIGN_FRAMES = 30,	//Frames to wait at stopsign once stopped
		STOPSIGN_DRIFT_SPEED = 3,		//Speed at which the car pulls up to a stopsign
		DRIFT_TO_STOPLIGHT_FRAMES = 30,	//Frames to drift forward after stoplight detected
		STOPLIGHT_DRIFT_SPEED = 3;		//Speed at which the car pulls up to a stoplight
}
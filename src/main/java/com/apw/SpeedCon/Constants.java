package com.apw.SpeedCon;

public class Constants {
		public static final double 
		//Speed
		MAX_SPEED = 12,					//Car's maximum speed
		MIN_SPEED = 4,					//Car's minimum speed
		PIN_TO_METER_PER_SECOND = 0.4,	//Conversion for motor position to m/s. DO NOT TOUCH
		
		//Math
		GRAV = 9.80665,					//Average gravitational acceleration
		SQRT_GRAV = Math.sqrt(GRAV),	//The square root of the average gravitational acceleration
		FRICT = 0.75,					//Given coefficient of friction
		SQRT_FRICT = Math.sqrt(FRICT);	//The square root of the given coefficient of friction
		
		public static final int
		//Stop Frames
		DRIFT_TO_STOPSIGN_FRAMES = 15,	//Frames to drift forward after stopsign detected
		WAIT_AT_STOPSIGN_FRAMES = 5,	//Frames to wait at stopsign once stopped
		STOPSIGN_DRIFT_SPEED = 4,		//Speed at which the car pulls up to a stopsign
		DRIFT_TO_STOPLIGHT_FRAMES = 15,	//Frames to drift forward after stoplight detected
		STOPLIGHT_DRIFT_SPEED = 4,		//Speed at which the car pulls up to a stoplight
		
		//Blobs
		BLOB_HEIGHT = 18,				//Filtered height of a blob in pixels
		BLOB_WIDTH = 18,				//Filtered width of a blob in pixels
		BLOB_AGE = -1,					//Filtered age of a blob in frames
		STOPLIGHT_MIN_Y = 0,			//Filtered position of a blob in pixels
		STOPLIGHT_MAX_Y = 240,			//Filtered position of a blob in pixels
		STOPLIGHT_MIN_X = 8,			//Filtered position of a blob in pixels
		STOPLIGHT_MAX_X = 648,			//Filtered position of a blob in pixels
		STOPSIGN_MIN_Y = 32,				//Filtered position of a blob in pixels
		STOPSIGN_MAX_Y = 512,			//Filtered position of a blob in pixels
		STOPSIGN_MIN_X = 480,				//Filtered position of a blob in pixels
		STOPSIGN_MAX_X = 648;			//Filtered position of a blob in pixels
}
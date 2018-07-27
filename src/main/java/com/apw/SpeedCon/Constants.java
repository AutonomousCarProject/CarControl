package com.apw.SpeedCon;

public class Constants {
	
		//Order of variable types within categories (boolean -> int -> double)
		//Add more if need be
	
	
		//Debug related constants
		public static final boolean
		DEFAULT_OVERLAY = false,		//Sets the default state as to the display of blob detection boxes
		DEFAULT_BLOBS = false,			//Sets the default state as to the display of blob boundaries
		DEFAULT_BLOBS_CONSOLE = false,	//Sets the default state as to the printing of all blob information
		DEFAULT_SPEED_CONSOLE = false;	//Sets the default state as to the printing of the current speed
		public static final int
		DEFAULT_COLOR_MODE = 0,			//Sets the default state as to the blob boundary color (0 = age, 1 = color, 2 = velocity) 
		NUM_COLOR_MODES = 3;			//Sets the number current total number of blob boundary color modes
		public static final double
		DISPLAY_AGE_MAX = 5,			//Max age compared to for age based blob boundary color
		DISPLAY_AGE_MIN = 0;			//Min age compared to for age based blob boundary color
	
		
		//Speed related constants
		public static final double
		MAX_SPEED = 90,					//Car's maximum speed
		MIN_SPEED = 50,					//Car's minimum speed
		PIN_TO_METER_PER_SECOND = 0.4;	//Conversion for motor position to m/s. DO NOT TOUCH
		
		
		//Math related constants
		public static final double
		GRAV = 9.80665,					//Average gravitational acceleration
		SQRT_GRAV = Math.sqrt(GRAV),	//The square root of the average gravitational acceleration
		FRICT = 0.75,					//Given coefficient of friction
		SQRT_FRICT = Math.sqrt(FRICT);	//The square root of the given coefficient of friction
		
		
		//Stopping related constants
		public static final int
		DRIFT_TO_STOPSIGN_FRAMES = 0,	//Frames to drift forward after stopsign detected
		WAIT_AT_STOPSIGN_FRAMES = 50,	//Frames to wait at stopsign once stopped
		STOPSIGN_DRIFT_SPEED = 12,		//Speed at which the car pulls up to a stopsign
		DRIFT_TO_STOPLIGHT_FRAMES = 0,	//Frames to drift forward after stoplight detected
		STOPLIGHT_DRIFT_SPEED = 12,		//Speed at which the car pulls up to a stoplight
		MIN_SPEED_INCREMENT = 20,		//Min increment amount between current and next frame speed
		MAX_SPEED_INCREMENT = 20;		//Max increment amount between current and next frame speed
		public static final double
		MAX_OBJECT_WIDTH = 320,			//Maximum width in pixels of object height before car slows
		MAX_OBJECT_HEIGHT = 200;		//Maximum height of in pixels of an object before car slows
		
		
		//Blob filter related constants
		public static final int
		BLOB_MIN_HEIGHT = 4,			//Filtered minimum height of a blob in pixels
		BLOB_MAX_HEIGHT = 20,			//Filtered maximum height of a blob in pixels
		BLOB_MIN_WIDTH = 4,				//Filtered minimum width of a blob in pixels
		BLOB_MAX_WIDTH = 20,			//Filtered maximum width of a blob in pixels
		BLOB_AGE = 1,					//Filtered age of a blob in frames
		STOPLIGHT_MIN_Y = 0,			//Filtered position of a blob in pixels, top of screen
		STOPLIGHT_MAX_Y = 240,			//Filtered position of a blob in pixels, middle of screen
		STOPLIGHT_MIN_X = 160,			//Filtered position of a blob in pixels, left quarter of screen
		STOPLIGHT_MAX_X = 480,			//Filtered position of a blob in pixels, right quarter of screen
		STOPSIGN_MIN_Y = 0,				//Filtered position of a blob in pixels, top of screen
		STOPSIGN_MAX_Y = 480,			//Filtered position of a blob in pixels, bottom of screen
		STOPSIGN_MIN_X = 400,			//Filtered position of a blob in pixels, right 5/8th of the screen
		STOPSIGN_MAX_X = 640;			//Filtered position of a blob in pixels, right of the screen
		public static final double
		BLOB_RATIO_DIF = 0.9;			//Filtered allowed multiplier difference between blob height and width
		
}
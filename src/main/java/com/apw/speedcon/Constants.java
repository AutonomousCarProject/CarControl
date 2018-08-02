package com.apw.speedcon;

public class Constants {

  
	//Default Debug Overlays
	public static final boolean
	DEFAULT_OVERLAY = false,		//Sets the default state as to the display of blob detection boxes
	DEFAULT_BLOBS = false,			//Sets the default state as to the display of blob boundaries
	DEFAULT_WRITE_BLOBS_TO_CONSOLE = false,	//Sets the default state as to the printing of all blob information
	DEFAULT_WRITE_SPEED_TO_CONSOLE = false;	//Sets the default state as to the printing of the current speed
	public static final int
	DEFAULT_COLOR_MODE = 0,			//Sets the default state as to the blob boundary color (0 = age, 1 = color, 2 = velocity) 
	NUM_COLOR_MODES = 3;			//Sets the number current total number of blob boundary color modes
	
	//Speed Related Constants
	public static final double
	MAX_SPEED = 50,					//Car's maximum speed
	MIN_SPEED = 15,					//Car's minimum speed
	PIN_TO_METER_PER_SECOND = 0.4,	//Conversion for motor position to m/s. DO NOT TOUCH
	MAX_OBJECT_WIDTH = 320,			//Maximum width in pixels of object height before car slows
	MAX_OBJECT_HEIGHT = 200,		//Maximum height of in pixels of an object before car slows
	MIN_STOP_TIME = 5,				//The lest amount of time the car will take to slow to stop, starts stopping if time needed is below
	MIN_STOP_DISTANCE = 1;			//The min amount of space you need to stop before a sign or light in meters.
	
	//Math Constants
	public static final double
	GRAV = 9.80665,					//Average gravitational acceleration
	SQRT_GRAV = Math.sqrt(GRAV),	//The square root of the average gravitational acceleration
	FRICT = 0.75,					//Given coefficient of friction
	SQRT_FRICT = Math.sqrt(FRICT);	//The square root of the given coefficient of friction
	
	//Stop Frames
	public static final int
	WAIT_AT_STOPSIGN_FRAMES = 50,	//Frames to wait at stopsign once stopped
	MIN_SPEED_INCREMENT = 10,		//Min increment amount between current and next frame speed
	MAX_SPEED_INCREMENT = 20;		//Max increment amount between current and next frame speed
	
	//Blob Filters
	public static final int
	BLOB_MIN_HEIGHT = 4,			//Filtered minimum height of a blob in pixels
	BLOB_MAX_HEIGHT = 20,			//Filtered maximum height of a blob in pixels
	BLOB_MIN_WIDTH = 4,				//Filtered minimum width of a blob in pixels
	BLOB_MAX_WIDTH = 20,			//Filtered maximum width of a blob in pixels
	BLOB_AGE = 1,					//Filtered age of a blob in frames
	STOPLIGHT_MIN_Y = 0,			//Filtered position of a blob in pixels
	STOPLIGHT_MAX_Y = 240,			//Filtered position of a blob in pixels
	STOPLIGHT_MIN_X = 160,			//Filtered position of a blob in pixels
	STOPLIGHT_MAX_X = 480,			//Filtered position of a blob in pixels
	STOPSIGN_MIN_Y = 0,				//Filtered position of a blob in pixels
	STOPSIGN_MAX_Y = 480,			//Filtered position of a blob in pixels
	STOPSIGN_MIN_X = 400,			//Filtered position of a blob in pixels
	STOPSIGN_MAX_X = 640;			//Filtered position of a blob in pixels
	public static final double
	BLOB_RATIO_DIF = 0.9,			//Filtered allowed multiplier difference between blob height and width
	DISPLAY_AGE_MAX = 5,			//Max age compared to for age based blob boundary color
	DISPLAY_AGE_MIN = 0;			//Min age compared to for age based blob boundary color
	
	//Overlay and Blobverlay constants
	public static final int
	OVERLAY_STOPLIGHT_HITBOX_COLOR 	 = 0xffa500,	//Color of the stoplight hitbox in overlay
	OVERLAY_STOPSIGN_HITBOX_COLOR 	 = 0xff69b4,	//Color of the stopsign hitbox in overlay
	BLOBVERLAY_COLORMODE_AGE_5_COLOR = 0x000000,	//Color of the oldest blobs in overlay
	BLOBVERLAY_COLORMODE_AGE_4_COLOR = 0x333333,	//Color of the older blobs in overlay
	BLOBVERLAY_COLORMODE_AGE_3_COLOR = 0x666666,	//Color of the middle-aged blobs in overlay
	BLOBVERLAY_COLORMODE_AGE_2_COLOR = 0x999999,	//Color of the teenage blobs in overlay
	BLOBVERLAY_COLORMODE_AGE_1_COLOR = 0xcccccc,	//Color of the child blobs in overlay
	BLOBVERLAY_COLORMODE_AGE_0_COLOR = 0xffffff,	//Color of the newborn blobs in overlay
	BLOBVERLAY_COLORMODE_COLOR_BLACK = 0x000000,	//Black hexvalue
	BLOBVERLAY_COLORMODE_COLOR_GRAY  = 0xd3d3d3,	//Gray hexvalue
	BLOBVERLAY_COLORMODE_COLOR_WHITE = 0xffffff,	//White hexvalue
	BLOBVERLAY_COLORMODE_COLOR_RED   = 0xff0000,	//Red hexvalue
	BLOBVERLAY_COLORMODE_COLOR_GREEN = 0x00ff00,	//Green hexvalue
	BLOBVERLAY_COLORMODE_COLOR_BLUE  = 0x0000ff;	//Blue hexvalue
	
	//Pedestrian Filters
	public static final int
	PED_MIN_SIZE = 30,
	PED_MAX_X = 912,
	PED_MIN_X = 128;
	
	//Other
	public static final int
	SCREEN_WIDTH = 640,
	SCREEN_FILTERED_WIDTH = 640,
	SCREEN_HEIGHT = 480;
}
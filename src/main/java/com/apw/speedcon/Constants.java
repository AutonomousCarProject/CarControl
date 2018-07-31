package com.apw.speedcon;

public class Constants {
    public static final boolean
            //Default Debug Overlays
            DEFAULT_OVERLAY = false,
            DEFAULT_BLOBS = false,
            DEFAULT_WRITE_BLOBS_TO_CONSOLE = false,
            DEFAULT_WRITE_SPEED_TO_CONSOLE = false;
    public static final double
            //Speed Related Constants
            MAX_SPEED = 20,                    //Car's maximum speed
            MIN_SPEED = 10,                    //Car's minimum speed
            PIN_TO_METER_PER_SECOND = 0.4,    //Conversion for motor position to m/s. DO NOT TOUCH
            MAX_OBJECT_WIDTH = 320,            //Maximum width in pixels of object height before car slows
            MAX_OBJECT_HEIGHT = 200,        //Maximum height of in pixels of an object before car slows
            MIN_STOP_TIME = 5,                //The lest amount of time the car will take to slow to stop, starts stopping if time needed is below
            MIN_STOP_DISTANCE = 1,           //The min amount of space you need to stop before a sign or light in meters.

    //Math Constants
    GRAV = 9.80665,                    //Average gravitational acceleration
            SQRT_GRAV = Math.sqrt(GRAV),    //The square root of the average gravitational acceleration
            FRICT = 0.75,                    //Given coefficient of friction
            SQRT_FRICT = Math.sqrt(FRICT);    //The square root of the given coefficient of friction
    public static final int
            //Stop Frames
            DRIFT_TO_STOPSIGN_FRAMES = 0,    //Frames to drift forward after stopsign detected
            WAIT_AT_STOPSIGN_FRAMES = 50,    //Frames to wait at stopsign once stopped
            STOPSIGN_DRIFT_SPEED = 12,        //Speed at which the car pulls up to a stopsign
            DRIFT_TO_STOPLIGHT_FRAMES = 0,    //Frames to drift forward after stoplight detected
            STOPLIGHT_DRIFT_SPEED = 12,        //Speed at which the car pulls up to a stoplight
            MIN_SPEED_INCREMENT = 10,        //Min increment amount between current and next frame speed
            MAX_SPEED_INCREMENT = 20,        //Max increment amount between current and next frame speed

    //Blob Filters
    BLOB_MIN_HEIGHT = 4,            //Filtered minimum height of a blob in pixels
            BLOB_MAX_HEIGHT = 20,            //Filtered maximum height of a blob in pixels
            BLOB_MIN_WIDTH = 4,                //Filtered minimum width of a blob in pixels
            BLOB_MAX_WIDTH = 20,            //Filtered maximum width of a blob in pixels
            BLOB_AGE = 1,                    //Filtered age of a blob in frames
            STOPLIGHT_MIN_Y = 0,            //Filtered position of a blob in pixels
            STOPLIGHT_MAX_Y = 240,            //Filtered position of a blob in pixels
            STOPLIGHT_MIN_X = 160,            //Filtered position of a blob in pixels
            STOPLIGHT_MAX_X = 480,            //Filtered position of a blob in pixels
            STOPSIGN_MIN_Y = 0,                //Filtered position of a blob in pixels
            STOPSIGN_MAX_Y = 480,            //Filtered position of a blob in pixels
            STOPSIGN_MIN_X = 400,            //Filtered position of a blob in pixels
            STOPSIGN_MAX_X = 640;            //Filtered position of a blob in pixels
    public static final double
            BLOB_RATIO_DIF = 0.9,
            DISPLAY_AGE_MAX = 5,
            DISPLAY_AGE_MIN = 0;
    public static final int
            DEFAULT_COLOR_MODE = 0,
            NUM_COLOR_MODES = 3;
}
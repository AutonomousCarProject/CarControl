package com.apw.pedestrians;

public class Constant {
    /*
     * Image
     */

    //time that last frame was collected
    public static long LAST_FRAME_MILLIS = 0;
    public static long CURRENT_FRAME_MILLIS = 0;
    public static long TIME_DIFFERENCE = 0;

    //constants for color margin calibrations
    //ratio of absolute average deviation to greyMargin
    public static float GREY_RATIO = 0.4f;

    //how far to set black and white margins from mean
    public static int BLACK_RANGE = 100;
    public static int WHITE_RANGE = 100;

    //threshold between light and dark means on dynamic ranging
    public static int LIGHT_DARK_THRESHOLD = 381;


    /*
     * MovingBlobDetection
     */

    //maximum time before unmatched MovingBlob is deleted
    public static int MAX_TIME_OFF_SCREEN = 0;

    //maximum distance in pixels between blobs that can be matched
    public static int DISTANCE_LIMIT_X = 40;
    public static int DISTANCE_LIMIT_Y = 20;

    //maximum size difference in pixels between blobs that can be matched
    public static int MAX_CHANGE_WIDTH = 29;
    public static int MAX_CHANGE_HEIGHT = 34;

    /*
     * BlobFilter
     */

    //regular filters

    //Minimum age to not be filtered
    public static int AGE_MIN = 0;

    //Maximum
    public static int VELOCITY_X_MAX = 150;
    public static int VELOCITY_Y_MAX = 25;
    public static float VELOCITY_X_MIN = 1f;
    public static int VELOCITY_Y_MIN = 0;
    public static float MAX_VELOCITY_CHANGE_X = 200;
    public static float MAX_VELOCITY_CHANGE_Y = 200;
    //Unified Blob filters

    //stuff
    public static float MAX_WIDTH_HEIGHT_RATIO = .8f;
    public static int MAX_WIDTH = 1300;
    public static int MAX_HEIGHT = 1200;
    public static float MIN_SCALED_VELOCITY_X = 0f;
    public static int MIN_SCALED_VELOCITY_Y = 0;
}

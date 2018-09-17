package com.apw.steering;

import java.awt.Color;

public class SteeringConstants {

    // SteeringModule Constants
    static final int STEERING_VERSION = 4; // Which Version of steering to use
    static final boolean DRAW_STEERING_LINES = true;
    static final Color RIGHT_LANE_COLOR = Color.yellow;
    static final Color LEFT_LANE_COLOR = Color.yellow;
    static final Color MIDPOINT_COLOR = Color.blue;
    static final Color TARGET_POINT_COLOR = Color.red;
    static final Color STEER_POINT_COLOR = Color.cyan;

    // SteeringBase Constants
    public static final boolean USE_PID = false; // Weather or not to use PID
    public static final double K_P = 1;
    public static final double K_I = 0;
    public static final double K_D = 0;

    // SteeringMk1 Constants
    public static final int HEIGHT_OF_AREA = 32; // How high the car looks for lines
    public static final int STARTING_HEIGHT = 272; // how high the car starts looking for lines

    // SteeringMk2 Constants
    public static final int NUM_PREVIOUS = 3; // Number of previous frames to average degree to steer to
    public static final int MAX_DIFF = 5; // Maximum X Pixel difference from one row to the next
    public static final double MIN_DIST_LOOK = 0; // Percent of midPoints to start at
    public static final double MAX_DIST_LOOK = 0.7; // Percent of midPoints to end at.


    // SteeringMk3 Constants

    // SteeringMk4 Constants
    public static final float LOOK_DIST = 0.55f; // How high on the screen the car calculates road points
    public static final float PREVIOUS_SLOPES = 10f; // How many previous slopes the car averages
    public static final float START_SLOPE = 0.18f; // Starting slope for the road width
    public static final int START_SEARCH = 50; // Pixel to start line search (Number of pixels from the bottom of the screen)
    public static final int SEARCH_OFFSET = 100; // Number of pixels to offset
    public static final int DEFAULT_ROAD_WIDTH = 400; // The road width if slope is unknown
    public static final int MINIMUM_RELIABLE_OFFSET = 20; // Minimum required pixel difference to be considered reliable.
    public static final boolean USE_NO_LANE_DETECTION = false;
}

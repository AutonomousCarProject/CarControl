package com.apw.steering;

/**
 * Point class holds an x, and y.
 * Intended to be used to store location of point on road.
 *
 * @author kevin
 * @author carl
 * @author nathan
 * @see Steerable
 */
public class Point {
    public int x;
    public int y;

    /**
     * This is a basic constructor that assigns this.x, any this.y to the x and y passed in through the
     * constructor
     *
     * @param x pixel starting from left side of screen
     * @param y pixel starting from top of screen
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
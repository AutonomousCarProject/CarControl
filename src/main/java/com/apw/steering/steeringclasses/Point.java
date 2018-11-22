package com.apw.steering.steeringclasses;

import com.apw.steering.Steerable;

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
    private boolean isEmpty;

    /**
     * This is a basic constructor that assigns this.x, any this.y to the x and y passed in through the
     * constructor
     *
     * @param x pixel starting from left side of screen
     * @param y pixel starting from top of screen
     */
    public Point(int x, int y) {
        this.isEmpty = false;
        this.x = x;
        this.y = y;
    }

    public void makeEmpty() {
        this.x = 0;
        this.y = 0;
        isEmpty = true;
    }

    public Point() {
        this.isEmpty = true;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}

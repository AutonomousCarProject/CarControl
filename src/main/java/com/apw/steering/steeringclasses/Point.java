package com.apw.steering.steeringclasses;

import com.apw.steering.Steerable;
import lombok.Getter;

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
    @Getter
    public int x;
    @Getter
    public int y;
    @Getter
    private final boolean isEmpty;

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

    public Point() {
        this.isEmpty = true;
    }
}
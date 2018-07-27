package com.apw.Steering;


/**
 * Interface to allow multiple versions of steering code.
 *
 * @author kevin
 * @author carl
 * @author nathan
 * @see SteeringBase
 */
public interface Steerable {

    /**
     * Gets how steep a turn will be. 1.0 being the sparest turn, 0 being no turn.
     * @param turnAngle angle from center line to furthest point
     * @return how steep the turn will be
     */
    double curveSteepness(double turnAngle);

    int drive(int pixels[]);

    /**
     * Find, and process the image data to assign Points to leftPoints, rightPoints, and midPoints.
     *
     * @param pixels An array of pixels
     */
    void findPoints(int[] pixels);
}
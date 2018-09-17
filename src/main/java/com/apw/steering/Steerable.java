package com.apw.steering;


import com.apw.steering.steeringversions.SteeringBase;

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
     *
     * @param turnAngle angle from center line to furthest point
     * @return how steep the turn will be
     */
    double curveSteepness(double turnAngle);

    int getSteeringAngle(int pixels[]);
}
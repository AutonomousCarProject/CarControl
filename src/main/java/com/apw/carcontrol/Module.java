package com.apw.carcontrol;

import java.awt.*;

public interface Module {
    /**
     * Initialize the driver constants with the ones needed for this module
     *
     * @see com.apw.apw3.DriverCons
     */
    default void initialize(CarControl control) {
    }

    /**
     * Update the CarControl with new data (steering/speed/image values).
     *
     * @param control The CarControl to update.
     */
    void update(CarControl control);

    /**
     * Draws on the screen based on info from the CarControl.
     *
     * @param control The CarControl to source data from.
     */
    void paint(CarControl control, Graphics g);
}

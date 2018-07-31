/**
 * A module to make the ArduinoIO interface work with MrModule's file system
 * 
 * @author Colton Jelsema
 */

package com.apw.sbcio.fakefirm;

import java.awt.Graphics;

import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;
import com.apw.sbcio.PWMController;

public class ArduinoModule implements Module{
    private PWMController driveSys;
    
    public ArduinoModule(PWMController system){
    	driveSys = system;
    }

    public void initialize(CarControl control) {
    	driveSys.Write(ArduinoIO.FORCE_START, 0, 0);
    }
    
    /**
     * Checks buffer input with arduino and sends null info to prevent timeout
     * 
     * @param control unused, may be put to use with digitalRead
     */
	@Override
	public void update(CarControl control) {

		driveSys.digitalRead();
		
		driveSys.Write(0, 0, 0);
		
	}
	
	/**
	 * Closes the port properly to assure future open calls work
	 */
	public void Close(){
		driveSys.close();
	}

	@Override
	public void paint(CarControl control, Graphics g) {
		
	}

}

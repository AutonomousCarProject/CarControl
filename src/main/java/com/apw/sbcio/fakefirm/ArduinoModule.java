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
    }
    
	@Override
	public void update(CarControl control) {

		driveSys.digitalRead();
		
		driveSys.Write(0, 0, 0);
		
	}
	
	public void Close(){
		driveSys.close();
	}

	@Override
	public void paint(CarControl control, Graphics g) {
		// TODO Auto-generated method stub
		
	}

}

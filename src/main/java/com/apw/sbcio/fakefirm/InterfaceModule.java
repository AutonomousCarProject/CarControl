package com.apw.sbcio.fakefirm;

import java.awt.Graphics;

import com.apw.carcontrol.CarControl;
import com.apw.carcontrol.Module;

public class InterfaceModule implements Module{
    private ArduinoIO driveSys;

    public void initialize(CarControl control) {
    }
    
	@Override
	public void update(CarControl control) {

		driveSys.digitalRead();
		
		control.accelerate(false, 0);
		
	}
	
	public void Close(){
		driveSys.close();
	}

	@Override
	public void paint(CarControl control, Graphics g) {
		// TODO Auto-generated method stub
		
	}

}

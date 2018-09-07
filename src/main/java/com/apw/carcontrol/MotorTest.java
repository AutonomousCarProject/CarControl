package com.apw.carcontrol;

import com.apw.sbcio.PWMController;
import com.apw.sbcio.fakefirm.ArduinoIO;

public class MotorTest {
    private static PWMController driveSys = new ArduinoIO();
    private static final int GasPin = 10, velocity = 6;
    
    private static final long initTime = 5000, runTime = 5000;
    
	public static void main(String[] args) {
    	driveSys.Write(ArduinoIO.FORCE_START, 0, 0);

    	System.out.println("Initializing");
    	pause(initTime);

    	for(int i = 0; i < 500; i++) {
    		driveSys.Write(0, 0, 0);
    	}
    	System.out.println("Running");

    	long startTime = System.currentTimeMillis();
    	while (System.currentTimeMillis() - startTime < runTime) {
    		driveSys.setServoAngle(GasPin, velocity + 90);	
    	}
       
		driveSys.close();
	}
	
	private static void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

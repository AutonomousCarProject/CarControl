package com.apw.carcontrol;

import java.awt.Graphics;

public class LatencyTestModule implements Module {
	private long lastTime = -1;

	@Override
	public void update(CarControl control) {
		long time = System.currentTimeMillis();
		if(lastTime != -1) {
	        System.out.println("Time since last run cycle: " + (time - lastTime) + "ms.");
		}
		lastTime = time;
	}

	@Override
	public void paint(CarControl control, Graphics g) {		
	}
}

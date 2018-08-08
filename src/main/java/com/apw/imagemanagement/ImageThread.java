package com.apw.imagemanagement;

public class ImageThread implements Runnable {
	
	private int imageType;
	private ImageManagementModule owner;
	private boolean newData = false;
	
	public ImageThread(ImageManagementModule newowner, int newtype) {
		owner = newowner;
		imageType = newtype;
	}

	@Override
	public void run() {
		while(true) {
			if(newData && imageType == 1) {
				owner.BWPixels = owner.getBlackWhiteRaster();
				newData = false;
			}
			else if(newData && imageType == 2) {
				owner.simplePixels = owner.getSimpleColorRaster();
				newData = false;
			}
		}
		
	}
	
	public void newData() {
		newData = true;
	}
	

}

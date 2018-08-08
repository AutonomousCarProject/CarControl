package com.apw.imagemanagement;

public class ImageThread implements Runnable {
	
	private int imageType;
	private ImageManagementModule owner;
	private boolean newData = false;
	private Thread imageThread;
	private int debugCount = 0;
	
	public ImageThread(ImageManagementModule newowner, int newtype) {
		owner = newowner;
		imageType = newtype;
	}

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(newData && imageType == 1) {
				owner.BWPixels = owner.getBlackWhiteRaster();
				debugCount++;
				newData = false;
			}
			else if(newData && imageType == 2) {
				owner.simplePixels = owner.getSimpleColorRaster();
				debugCount++;
				newData = false;
			}
		}
		
	}
	
	public void newData() {
		newData = true;
	}

	public void start() {
		if(imageType == 1) {
			imageThread = new Thread(this, "BW Image Processing Thread");
			imageThread.start();
		}
		if(imageType == 2) {
			imageThread = new Thread(this, "SimpleColor Image Processing Thread");
			imageThread.start();
		}
		
	}
	

}

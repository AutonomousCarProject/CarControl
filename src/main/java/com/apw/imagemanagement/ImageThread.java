package com.apw.imagemanagement;

public class ImageThread implements Runnable {
	
	private int imageType;
	private boolean image;
	private ImageManagementModule owner;
	private boolean newData = false;
	private Thread imageThread;
	private int debugCount = 0;
	
	public ImageThread(ImageManagementModule newowner, int newtype, boolean image) {
		owner = newowner;
		imageType = newtype;
		this.image = image;
	}

	@Override
	public void run() {
		while(true) {

			try {
				synchronized (this) {
					this.wait();
				}
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}

			if (image) {
				switch (imageType) {
					case 1:
						owner.displayPixels = owner.getRGBRaster();
						break;
					case 2:
						owner.displayPixels = owner.getMonoRGBRaster();
						break;
					case 3:
						owner.displayPixels = owner.getSimpleRGBRaster();
						break;
					case 4:
						owner.displayPixels = owner.getBlackWhiteRaster();
						break;
					case 5:
						owner.displayPixels = owner.getRoad();
						break;
					case 6:
						owner.displayPixels = owner.getRobertsCross();
						break;
					default:
						throw new IllegalStateException("No image management viewType: " + imageType);
				}
			} else {
				if (imageType == 1) {
					owner.BWPixels = owner.getBlackWhiteRaster();

				} else {
					owner.simplePixels = owner.getSimpleColorRaster();
				}
				newData = false;
			}
		}
	}
	
	public void newData() {
		//newData = true;

	}

	public void start() {
		if(image){
			imageThread = new Thread(this, "Display Image Processing Thread");
			imageThread.start();
		}
		else if(imageType == 1) {
			imageThread = new Thread(this, "BW Image Processing Thread");
			imageThread.start();
		}
		 else if(imageType == 2) {
			imageThread = new Thread(this, "SimpleColor Image Processing Thread");
			imageThread.start();
		}else{
			imageThread = new Thread(this, "NULL");
			imageThread.start();
		}

	}
	public void updateRaster(int viewType){
		imageType = viewType;
	}
	

}

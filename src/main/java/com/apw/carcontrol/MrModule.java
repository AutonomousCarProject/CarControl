package com.apw.carcontrol;

import com.apw.imagemanagement.ImageManagementModule;
import com.apw.sbcio.PWMController;
import com.apw.sbcio.fakefirm.ArduinoIO;
import com.apw.sbcio.fakefirm.ArduinoModule;
import com.apw.speedcon.SpeedControlModule;

import com.apw.steering.SteeringModule;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

public class MrModule extends JFrame implements Runnable, KeyListener {

    private PWMController driveSys = new ArduinoIO();
    private ArrayList<Module> modules = new ArrayList<>(); // Contains each module
    private final CarControl carControl; // A CarControl that holds data for each module
    private CarControl speedControl; // A CarControl that holds data specifically for speed
    private CarControl steeringControl; // A CarControl that holds data specifically for steering

    private boolean initialized = false;

    private static final int FPS = 50; // Number of frames per second run is called
    private static final int initDelay = 100; // Initial delay before run is called

    private MrModule(boolean realCam, boolean hasWindow) {
        if (realCam) {
            carControl = new CamControl(driveSys);
            speedControl = new CamControl(driveSys);
            steeringControl = new CamControl(driveSys);
        } else {
            carControl = new TrakSimControl(driveSys);
            speedControl = new TrakSimControl(driveSys);
            steeringControl = new TrakSimControl(driveSys);
        }

        final int winWidth = carControl.getImageWidth();
        final int winHeight = carControl.getImageHeight();
        carControl.updateWindowDims(getWidth(), getHeight());

        createModules(hasWindow, winWidth, winHeight);

        headlessInit();
    }

    /**
     * Gets called once, and starts the executor service to call run every FPS
     */
    private void headlessInit() {
        // driveSys = new ArduinoIO();
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this, initDelay, Math.round(1000.0 / FPS), TimeUnit.MILLISECONDS);

        Future run = executorService.submit(this);

        try {
            run.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void createModules(boolean useWindowModule, int winWidth, int winHeight) {
    	if(useWindowModule) {
	        WindowModule windowModule = new WindowModule(winWidth, winHeight);
	        windowModule.addKeyListener(this);
	        modules.add(windowModule);
    	}

        modules.add(new ImageManagementModule(winWidth, winHeight, carControl.getTile()));
        modules.add(new SpeedControlModule());
        modules.add(new SteeringModule());
        modules.add(new ArduinoModule(driveSys));
        //modules.add(new LatencyTestModule());

        for (Module module : modules) {
            module.initialize(carControl);
        }

        initialized = true;
    }

    /**
     * <p>
     * Update method is called once every FPS.
     * Method calls and manages each module's update. Specifically, ImageManagement, Speed and steering.
     * ImageManagement, speed and steering are each in their own thread. Speed and steering
     * run after ImageManagement process their respective image.
     * </p>
     */
    private void update() {
        //read the camera image, and update windowModule.
        carControl.readCameraImage();
        carControl.setEdges(getInsets());
        modules.get(0).update(carControl);
        modules.get(4).update(carControl);

        // FIXME: Does not print exceptions to console.
        ImageManagementModule imageModule = (ImageManagementModule) modules.get(1);
        // Start Thread to get the RGB image. (for camera)
        CompletableFuture<CarControl> futureRGBImage = CompletableFuture.supplyAsync(() -> {
            carControl.setRGBImage(imageModule.getRGBRaster(carControl.getRecentCameraImage()));
            carControl.setRenderedImage(carControl.getRGBImage());
            return carControl;
        });
        // Start Thread to get the black and white image (for steering)
        CompletableFuture<CarControl> futureBWImage = CompletableFuture.supplyAsync(() -> {
            steeringControl.setRGBImage(imageModule.getBlackWhiteRaster(carControl.getRecentCameraImage()));
            return steeringControl;
        });
        // Start Thread to get the simple image (for speed)
        CompletableFuture<CarControl> futureSimpleImage = CompletableFuture.supplyAsync(() -> {
            speedControl.setProcessedImage(imageModule.getSimpleColorRaster(carControl.getRecentCameraImage()));
            return speedControl;
        });

        // Call steering Module after futureBWImage is finished
        CompletableFuture<Void> futureSteering = futureBWImage.thenAcceptAsync(carControl -> modules.get(3).update(carControl));
        // Call speed module after futureSimpleImage is finished
        CompletableFuture<Void> futureSpeed = futureSimpleImage.thenAcceptAsync(carControl -> modules.get(2).update(carControl));

        // Run when all finished.
        CompletableFuture.allOf(futureSpeed, futureSteering, futureRGBImage)
                .thenAccept(v -> paint())
                .exceptionally(ex -> {
                    System.out.println(ex.getMessage());
                    return null;
                })
                .join();

    }

    private void paint() {
        if (!modules.isEmpty()) {
            for (Module module : modules) {
                module.paint(carControl, ((WindowModule) (modules.get(0))).getGraphics());
            }
        }
    }

    @Override
    public void run() {
        if(!initialized) {
            return;
        }

        try {
            update();
            //paint();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        boolean realcam = false;
        boolean window = true;
        if(args.length > 0) {
        	if(args[0].toLowerCase().equals("sim")) {
        		realcam = false;
        	}
        	if(args[0].toLowerCase().equals("headless")) {
        		window = false;
        	}
        }
        new MrModule(realcam, window);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (carControl instanceof TrakSimControl) {
            for (Map.Entry<Integer, Runnable> binding : ((TrakSimControl) carControl).keyBindings.entrySet())
                if (e.getKeyCode() == binding.getKey())
                    binding.getValue().run();
        } else if (carControl instanceof CamControl) {
        	for (Map.Entry<Integer, Runnable> binding : ((CamControl) carControl).keyBindings.entrySet())
                if (e.getKeyCode() == binding.getKey())
                    binding.getValue().run();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }

}

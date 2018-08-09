package com.apw.carcontrol;

import com.apw.apw3.DriverCons;
import com.apw.imagemanagement.ImageManagementModule;
import com.apw.sbcio.PWMController;
import com.apw.sbcio.fakefirm.ArduinoIO;
import com.apw.sbcio.fakefirm.ArduinoModule;
import com.apw.speedcon.SpeedControlModule;

import com.apw.steering.SteeringModule;

import javax.swing.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

public class MrModule extends JFrame implements Runnable, KeyListener {

    private ScheduledExecutorService executorService;
    private PWMController driveSys = new ArduinoIO();
    private ArrayList<Module> modules;
    private CarControl control;

    private boolean initialized = false;

    private int windowWidth = 912;
    private int windowHeight = 480;

    private static final int FPS = 60;
    private static final int initDelay = 100;
    
    private boolean window;

    private MrModule(boolean realcam, boolean window) {
        if (realcam)
            control = new CamControl(driveSys);
        else
            control = new TrakSimControl(driveSys);

        windowWidth = control.getImageWidth();
        windowHeight = control.getImageHeight();
        
        this.window = window;
        
        headlessInit();
        createModules(window);
    }

    private void headlessInit() {
        driveSys = new ArduinoIO();
        modules = new ArrayList<>();

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this, initDelay, 1000 / FPS, TimeUnit.MILLISECONDS);

        Future run = executorService.submit(this);

        try {
            run.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void createModules(boolean window) {
    	if(window) {
	        WindowModule windowModule = new WindowModule(windowWidth, windowHeight);
	        windowModule.addKeyListener(this);
	        modules.add(windowModule);
    	}
    	
        modules.add(new ImageManagementModule(windowWidth, windowHeight, control.getTile()));
        modules.add(new SpeedControlModule());
        modules.add(new SteeringModule());
        modules.add(new ArduinoModule(driveSys));
        modules.add(new LatencyTestModule());

        for (Module module : modules) {
            module.initialize(control);
        }

        initialized = true;
    }

    private void update() {
        if (control instanceof TrakSimControl) {
            ((TrakSimControl) control).cam.theSim.SimStep(1);
        }
        
        control.readCameraImage();
        control.setEdges(getInsets());
        control.updateWindowDims(getWidth(), getHeight());

        for (Module module : modules) {
            module.update(control);
        }

/*
        // bad code here for example
        ImageManagementModule imageModule = (ImageManagementModule) modules.get(0);
        CompletableFuture<int[]> futureBWImage =
                CompletableFuture.supplyAsync(() -> imageModule.getBlackWhiteRaster(control.getRecentCameraImage()));
        CompletableFuture<byte[]> futureSimpleImage =
                CompletableFuture.supplyAsync(() -> imageModule.getSimpleColorRaster(control.getRecentCameraImage()));

        // Call steering Module
        CompletableFuture<Void> futureSteering = futureBWImage.thenAcceptAsync(image -> modules.get(2).update(control, futureBWImage));
        // Call speed module
        CompletableFuture<Void> futureSpeed = futureSimpleImage.thenAcceptAsync(image -> modules.get(1).update(control, futureSimpleImage));
        // Wait for them all to finish
        CompletableFuture<Void> futureComplete = CompletableFuture.allOf(futureSpeed, futureSteering)
                .thenAccept(v -> paint())
                // Handle errors
                .exceptionally(ex -> null);
        // This makes java wait
        futureComplete.join();


        // bad code here for example */

    }


    @Override
    public void run() {
        if(!initialized) {
            return;
        }

        try {
            update();
            paint();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void paint() {
        if (!modules.isEmpty()) {
            for (Module module : modules) {
            	if(window) {
            		module.paint(control, ((WindowModule) (modules.get(0))).getGraphics());
            	}
            }
        }
    }

    public static void main(String[] args) {
        boolean realcam = true;
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
        if ((control instanceof TrakSimControl))
            for (Map.Entry<Integer, Runnable> binding : ((TrakSimControl) control).keyBindings.entrySet())
                if (e.getKeyCode() == binding.getKey())
                    binding.getValue().run();
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }

}

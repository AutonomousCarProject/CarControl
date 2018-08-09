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
import java.security.Key;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

public class MrModule extends JFrame implements Runnable, KeyListener {

    private ScheduledExecutorService executorService;
    private PWMController driveSys = new ArduinoIO();
    private ArrayList<Module> modules;
    private CarControl control;

    private final int windowWidth = 912;
    private final int windowHeight = 480;
    
    private final int FPS = 60;
    private final int initDelay = 100;

    private MrModule(boolean realcam) {
        if (realcam)
            control = new CamControl(driveSys);
        else
            control = new TrakSimControl(driveSys);

        headlessInit();
        createModules();
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

    private void createModules() {
        WindowModule windowModule = new WindowModule(windowWidth, windowHeight);
        windowModule.addKeyListener(this);

        modules.add(windowModule);
        modules.add(new ImageManagementModule(windowWidth, windowHeight, control.getTile()));
        modules.add(new SpeedControlModule());
        modules.add(new SteeringModule());
        modules.add(new ArduinoModule(driveSys));

        for (Module module : modules) {
            module.initialize(control);
        }
    }

    private void update() {
        if (control instanceof TrakSimControl) {
            ((TrakSimControl) control).cam.theSim.SimStep(1);
        }

        control.readCameraImage();
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

    private void paint() {
        if (!modules.isEmpty())
            for (Module module : modules)
                module.paint(control, ((WindowModule) (modules.get(0))).getGraphics());
    }

    @Override
    public void run() {
        try {
            update();
            paint();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        boolean realcam = DriverCons.D_LiveCam;
        if(args.length > 0 && args[0].toLowerCase().equals("sim")) {
            realcam = false;
        }
        new MrModule(realcam);
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

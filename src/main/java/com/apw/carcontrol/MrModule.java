package com.apw.carcontrol;

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


    private static final int windowWidth = 912;
    private static final int windowHeight = 480;

    private MrModule(boolean simulate) {
        if (simulate) {
            control = new TrakSimControl(driveSys);
        }
        else {
            control = new CamControl(driveSys);
        }

        headlessInit();
        createModules();
    }

    private void headlessInit() {
        driveSys = new ArduinoIO();
        modules = new ArrayList<>();

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this, 0, 1000 / 20, TimeUnit.MILLISECONDS);

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
                module.paint(control, ((WindowModule) (modules.get(0))).getGraphics());
            }
        }
    }

    public static void main(String[] args) {
        boolean simulate = true;
        if(args.length > 0 && args[0].toLowerCase().equals("car")) {
            simulate = false;
        }
        new MrModule(simulate);
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

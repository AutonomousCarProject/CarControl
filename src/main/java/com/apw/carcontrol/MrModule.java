package com.apw.carcontrol;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MrModule extends JFrame implements Runnable {

    private ScheduledExecutorService executorService;
    private ArrayList<Module> modules;
    private TrakSimControl trakSimControl;
    private BufferedImage displayImage;

    private final int width = 1920;
    private final int height = 1080;

    private MrModule() {
        init();
        setupWindow();
        createModules();
    }

    private void init() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        displayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        trakSimControl = new TrakSimControl();
        modules = new ArrayList<>();
    }

    private void setupWindow() {
        executorService.scheduleAtFixedRate(this, 0, 1000 / 60, TimeUnit.MILLISECONDS);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height);
        setVisible(true);
    }

    private void createModules() {
        modules.add(new ImageManagementModule(width, height));
        modules.add(new SpeedControlModule());
        modules.add(new SteeringModule());
    }

    private void update() {
        trakSimControl.readCameraImage();
        for (Module module : modules)
            module.update(trakSimControl);
    }

    private void paint() {
        for (Module module : modules)
            module.paint(trakSimControl, displayImage);
    }

    @Override
    public void run() {
        update();
        paint();
    }

    public static void main(String[] args) {
        new MrModule();
    }
}

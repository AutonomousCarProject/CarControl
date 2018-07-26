package com.apw.carcontrol;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MrModule extends JFrame implements Runnable {

    private ScheduledExecutorService executorService;
    private ArrayList<Module> modules;
    private TrakSimControl trakSimControl;
    private BufferedImage displayImage, bufferImage;
    private ImageIcon displayIcon;

    // FIXME breaks if dimensions are not 640x480
    private final int width = 912;
    private final int height = 480;

    private MrModule() {
        init();
        setupWindow();
        createModules();
    }

    private void init() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        displayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bufferImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        displayIcon = new ImageIcon(displayImage);
        this.add(new JLabel(displayIcon));
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
        modules.add(new ImageManagementModule(height, width));
        modules.add(new SpeedControlModule());
        modules.add(new SteeringModule());
    }

    private void update() {
        trakSimControl.readCameraImage();
        for (Module module : modules) {
            module.update(trakSimControl);
        }
    }

    public void paint(Graphics g) {
        super.paint(g);

        int[] renderedImage = trakSimControl.getRenderedImage();

        if(renderedImage != null) {
            int[] displayPixels = ((DataBufferInt) bufferImage.getRaster().getDataBuffer()).getData();
            System.arraycopy(renderedImage, 0, displayPixels, 0, renderedImage.length);

            BufferedImage tempImage = displayImage;
            displayImage = bufferImage;
            bufferImage = tempImage;

            displayIcon.setImage(displayImage);
        }

        for (Module module : modules) {
            module.paint(trakSimControl);
        }
    }

    @Override
    public void run() {
        trakSimControl.cam.theSim.SimStep(1);
        update();
        this.repaint();
    }

    public static void main(String[] args) {
        new MrModule();
    }
}

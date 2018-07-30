package com.apw.carcontrol;

import com.apw.ImageManagement.ImageManagementModule;
import com.apw.SpeedCon.SpeedControlModule;
import com.apw.Steering.SteeringModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MrModule extends JFrame implements Runnable, KeyListener {

    private ScheduledExecutorService executorService;
    private ArrayList<Module> modules;
    private TrakSimControl trakSimControl;
    private BufferedImage displayImage, bufferImage;
    private ImageIcon displayIcon;

    // FIXME breaks if dimensions are not 912x480
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
        trakSimControl = new TrakSimControl();
        modules = new ArrayList<>();
    }

    private void setupWindow() {
        executorService.scheduleAtFixedRate(this, 0, 1000 / 15, TimeUnit.MILLISECONDS);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height + 25);
        setResizable(false);
        setVisible(true);
        addKeyListener(this);
        add(new JLabel(displayIcon));
    }

    private void createModules() {
        modules.add(new ImageManagementModule(width, height));
        modules.add(new SpeedControlModule());
        modules.add(new SteeringModule());

        for (Module module : modules)
            module.initialize(trakSimControl);
    }

    private void update() {
        trakSimControl.cam.theSim.SimStep(1);
        trakSimControl.readCameraImage();
        trakSimControl.setEdges(getInsets());
        for (Module module : modules) {
            module.update(trakSimControl);
        }
    }

    @Override
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
            module.paint(trakSimControl, g);
        }
    }

    @Override
    public void run() {
        update();
        repaint();
    }

    public static void main(String[] args) {
        new MrModule();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        for (Map.Entry<Integer, Runnable> binding : trakSimControl.keyBindings.entrySet()) {
            if (e.getKeyCode() == binding.getKey()) {
                binding.getValue().run();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {  }

    @Override
    public void keyReleased(KeyEvent e) {  }
}

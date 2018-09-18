package com.apw.carcontrol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class WindowModule extends JFrame implements Module {

    private GraphicsDevice graphicsDevice;

    private BufferedImage displayImage, bufferImage;

    private int windowWidth, windowHeight;

    private boolean fullscreen;

    public WindowModule(int windowWidth, int windowHeight) {
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;

        graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        displayImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        bufferImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public void initialize(CarControl control) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(windowWidth, windowHeight + 25);
        setResizable(true);
        setVisible(true);
        setIgnoreRepaint(true);

        control.addKeyEvent(KeyEvent.VK_F11, () -> {
            fullscreen = !fullscreen;
            setVisible(false);
            dispose();
            setUndecorated(fullscreen);
            if (fullscreen) {
                graphicsDevice.setFullScreenWindow(this);
                validate();
            } else {
                graphicsDevice.setFullScreenWindow(null);
                setVisible(true);
            }
        });
    }

    @Override
    public void update(CarControl control) {
        control.setEdges(getInsets());
        control.updateWindowDims(getWidth(), getHeight());
    }

    @Override
    public void paint(CarControl control, Graphics g) {
        int[] renderedImage = control.getRenderedImage();

        if (renderedImage != null) {
            int[] displayPixels = ((DataBufferInt) bufferImage.getRaster().getDataBuffer()).getData();
            System.arraycopy(renderedImage, 0, displayPixels, 0, renderedImage.length);

            BufferedImage tempImage = displayImage;
            displayImage = bufferImage;
            bufferImage = tempImage;

            
            g.drawImage(displayImage, control.getEdges().left, control.getEdges().top,
                    control.getWindowWidth() - control.getEdges().left - control.getEdges().right,
                    control.getWindowHeight() - control.getEdges().top - control.getEdges().bottom ,
                    null);
            for (ColoredLine line : control.getLines()) {
                g.setColor(line.getColor());
                g.drawLine(line.getStart().x, line.getStart().y, line.getEnd().x, line.getEnd().y);
            }
            for (ColoredRect rect : control.getRects()) {
                g.setColor(rect.getColor());
                g.drawRect(rect.getPosition().x, rect.getPosition().y, rect.getWidth(), rect.getHeight());
            }
        }

        control.clearLines();
        control.clearRects();
    }
}

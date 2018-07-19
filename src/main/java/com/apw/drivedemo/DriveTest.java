package com.apw.drivedemo;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.apw.apw3.SimCamera;
import com.apw.apw3.TrakSim;
import com.apw.ImageManagement.ImageManager;

public class DriveTest extends TimerTask {
    public static final int FRAME_RATE_NUMBER = 4;	//4 corresponds to 30fps
    public static final int FPS = 30;

    private JFrame window;
    private TrakSim sim;
	private SimCamera simcam;
	private ImageManager imagemanager;
	private int nrows, ncols;
	private BufferedImage displayimage;
	private Icon displayicon;
	private JLabel displaylabel;

        	public DriveTest() {
        		sim = new TrakSim();
        		sim.SimStep(2);
        		simcam = new SimCamera();
        		simcam.Connect(FRAME_RATE_NUMBER);
        		imagemanager = new ImageManager(simcam);

                		nrows = imagemanager.getNrows();
        		ncols = imagemanager.getNcols();

                		window = new JFrame();
        		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        		window.setSize(ncols, nrows);
        		displayimage = new BufferedImage(ncols, nrows, BufferedImage.TYPE_INT_RGB);
        		displayicon = new ImageIcon(displayimage);
        		displaylabel = new JLabel();
        		displaylabel.setIcon(displayicon);
        		window.add(displaylabel);
        		window.setVisible(true);

                		System.out.println("**************" + nrows + " " + ncols);
        	}

        	public static void main(String[] args) {
        		Timer displayTaskTimer = new Timer();
        		displayTaskTimer.scheduleAtFixedRate(new DriveTest(), new Date(), 1000/FPS);
        	}

        	@Override
	public void run() {
        		int[] imagepixels = imagemanager.getRGBRaster();
        		int[] displaypixels = ((DataBufferInt) displayimage.getRaster().getDataBuffer()).getData();
        		System.arraycopy(imagepixels, 0, displaypixels, 0, imagepixels.length);

                		System.out.println("Repainting");

                		window.repaint();
        	}

        }

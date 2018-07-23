package com.apw.drivedemo;

import com.apw.ImageManagement.ImageManager;
import com.apw.ImageManagement.ImageManipulator;

import com.apw.apw3.SimCamera;
import com.apw.drivedemo.TimerRepaint;
import com.apw.drivedemo.TrakManager;

import javax.swing.*;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Date;
import java.util.Timer;

public class DriveTest extends JFrame {

    public static final int FPS = 15;

    public static ImageManager imageManager;
    public static Timer displayTaskTimer;

    private int viewType;
    private int width, height;
    private int ncols, nrows;
    private BufferedImage displayImage, bufferImage, tempImage;
    private int[] displayPixels, imagePixels, emptyPixels;
    private ImageIcon displayicon;
    private JLabel displaylabel;

    public static void main(String[] args){
        //Timer displayTaskTimer = new Timer();
        TrakManager starter = new TrakManager();
        init(new Timer(),starter.getImageManager());
        displayTaskTimer.scheduleAtFixedRate(starter, new Date(), 1000 / FPS);
        autoDriveTest(createDriveTest(3));
        //displayTaskTimer.scheduleAtFixedRate(new TimerRepaint(createDriveTest(3)), new Date(), 1000 / FPS);
    }
    public static void init(Timer refreshTimer, ImageManager imageMng) {
        displayTaskTimer = refreshTimer;
        imageManager = imageMng;
    }
    public static void init(){
        displayTaskTimer = new Timer();
        imageManager = new ImageManager(new SimCamera());
    }
    public static DriveTest createDriveTest(){
        return new DriveTest();
    }
    public static DriveTest createDriveTest(int viewType){
        return new DriveTest(viewType);
    }
    public static DriveTest createDriveTest(int viewType, int width, int height){
        return new DriveTest(viewType,width,height);
    }
    public static DriveTest autoDriveTest(DriveTest dtest){
        try{
            displayTaskTimer.scheduleAtFixedRate(new TimerRepaint(dtest), new Date(), 1000 / FPS);
        }catch(Exception ex){
            System.out.println("Auto update initialization failed");
        }
        return dtest;
    }
    private DriveTest(){
        viewType=1;
        ncols=imageManager.getNcols();
        nrows=imageManager.getNrows();
        width=ncols;
        height=nrows;
        finishInit();
    }
    private DriveTest(int viewType){
        this.viewType=viewType;
        ncols=imageManager.getNcols();
        nrows=imageManager.getNrows();
        width=ncols;
        height=nrows;
        finishInit();
    }
    private DriveTest(int viewType, int width, int height){
        this.viewType=viewType;
        ncols=imageManager.getNcols();
        nrows=imageManager.getNrows();
        this.width=width;
        this.height=height;
        finishInit();
    }
    private void finishInit(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width,height);
        displayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bufferImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        emptyPixels=new int[width*height];
        displayicon = new ImageIcon(displayImage);
        displaylabel = new JLabel();
        displaylabel.setIcon(displayicon);
        add(displaylabel);
        setVisible(true);
        System.out.println("--------------" + width + " " + height);
    }
    @Override
    public void paint(Graphics g){
        //repaints the window
        super.paint(g);

        //pulls and manipulates image from TrakSim
        imagePixels=null;
        switch(viewType){
            case 1:
                imagePixels = imageManager.getRGBRaster();
                break;
            case 2:
                imagePixels = imageManager.getMonoRGBRaster();
                break;
            case 3:
                imagePixels = imageManager.getSimpleRGBRaster();
                break;
            case 4:
                imagePixels = imageManager.getBWRGBRaster();
                break;
        }

        //Copies TrakSim image onto the buffer
        if(width!=ncols||height!=nrows)
            ImageManipulator.limitTo(emptyPixels,imagePixels,ncols,nrows,width,height,false);
        else
            emptyPixels = imagePixels;
        displayPixels = ((DataBufferInt) bufferImage.getRaster().getDataBuffer()).getData();
        System.arraycopy(emptyPixels, 0, displayPixels, 0, emptyPixels.length);

        //Replaces the buffer
        tempImage=displayImage;
        displayImage=bufferImage;
        bufferImage=tempImage;

        //paints extra information about steering and speed
        testPaint(g);
    }

    /** Paints extra information about steering and speed
     *
     * @param graf the graphics to edit
     */
    private void testPaint(Graphics graf){
        steerPaint(graf);
        speedPaint(graf);
    }

    /** Paints extra information about steering
     *
     * @param graf the graphics to edit
     */
    private void steerPaint(Graphics graf){

    } //~steerPaint

    /** Paints extra information about speed
     *
     * @param graf the graphics to edit
     */
    private void speedPaint(Graphics graf){

    } //~speedPaint

}

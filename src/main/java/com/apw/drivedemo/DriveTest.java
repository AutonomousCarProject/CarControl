package com.apw.drivedemo;

import com.apw.ImageManagement.ImageManager;
import com.apw.ImageManagement.ImageManipulator;
import com.apw.Steering.Point;
import com.apw.Steering.Steering;
import com.apw.apw3.DriverCons;
import com.apw.apw3.SimCamera;
import com.apw.apw3.TrakSim;
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
    public static Steering testSteering;

    private int viewType;
    private int width, height;
    private int ncols, nrows;
    private BufferedImage displayImage, bufferImage, tempImage;
    private int[] displayPixels, imagePixels, emptyPixels;
    private ImageIcon displayicon;
    private JLabel displaylabel;
    private Insets edges;

    public static void main(String[] args){
        //Timer displayTaskTimer = new Timer();
        TrakManager starter = new TrakManager();
        init(new Timer(),starter.getImageManager(), new Steering(starter.getSim()));
        displayTaskTimer.scheduleAtFixedRate(starter, new Date(), 1000 / FPS);
        autoDriveTest(createDriveTest(3));
        //displayTaskTimer.scheduleAtFixedRate(new TimerRepaint(createDriveTest(3)), new Date(), 1000 / FPS);
    }
    public static void init(Timer refreshTimer, ImageManager imageMng, Steering steerSys) {
        displayTaskTimer = refreshTimer;
        imageManager = imageMng;
        testSteering = steerSys;
    }
    public static void init(){
        displayTaskTimer = new Timer();
        imageManager = new ImageManager(new SimCamera());
        testSteering = new Steering(new TrakSim());
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
        edges = getInsets();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width,height);
        //setLayout(GridBagLayout());
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
        Point[] hi = testSteering.findPoints(imageManager.getRGBRaster());
        int vEdit = (getHeight()-480)/2+10;
        graf.setColor(Color.RED);
        //graf.fillRect(100, testSteering.startingPoint, 1, 1);
        if (DriverCons.D_DrawCurrent == true) {
            for (int i = 0; i<testSteering.startingPoint - (testSteering.startingHeight + testSteering.heightOfArea); i++) {
                graf.fillRect(testSteering.leadingMidPoints[i].x, testSteering.leadingMidPoints[i].y +  + edges.top+vEdit, 5, 5);
            }
        }


        for (int i = 0; i<hi.length; i++) {
            if (DriverCons.D_DrawPredicted == true) {
                graf.setColor(Color.BLUE);
                graf.fillRect(hi[i].x, hi[i].y + edges.top + vEdit, 5, 5);
            }
            if (DriverCons.D_DrawOnSides == true) {
                graf.setColor(Color.YELLOW);
                graf.fillRect(testSteering.leftPoints[i].x + edges.left, testSteering.leftPoints[i].y + edges.top+ vEdit, 5, 5);
                graf.fillRect(testSteering.rightPoints[i].x + edges.left, testSteering.rightPoints[i].y + edges.top + vEdit, 5, 5);
            }
        }
    } //~steerPaint

    /** Paints extra information about speed
     *
     * @param graf the graphics to edit
     */
    private void speedPaint(Graphics graf){

    } //~speedPaint

}

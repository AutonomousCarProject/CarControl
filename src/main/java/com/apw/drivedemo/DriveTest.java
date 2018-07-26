package com.apw.drivedemo;

import com.apw.ImageManagement.ImageManager;
import com.apw.ImageManagement.ImageManipulator;
import com.apw.SpeedCon.SpeedController;
import com.apw.Steering.Point;
import com.apw.Steering.Steering;
import com.apw.apw3.DriverCons;
import com.apw.apw3.SimCamera;
import com.apw.apw3.TrakSim;
import com.apw.drivedemo.TimerRepaint;
import com.apw.drivedemo.TrakManager;
import com.apw.pedestrians.PedestrianDetector;
import com.apw.pedestrians.blobtrack.MovingBlob;


import javax.swing.*;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Date;
import java.util.List;
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
    private byte[] limitArray;
    private ImageIcon displayicon;
    private JLabel displaylabel;
    private Insets edges;

    @Deprecated
    public static void main(String[] args){
        //Timer displayTaskTimer = new Timer();
        TrakManager starter = new TrakManager();
        init(new Timer(),starter.getImageManager(), new Steering(starter.getSim()));
        displayTaskTimer.scheduleAtFixedRate(starter, new Date(), 1000 / FPS);
        autoDriveTest( new DriveTest(5));
        autoDriveTest( new DriveTest(2));
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
    public static DriveTest autoDriveTest(DriveTest dtest){
        try{
            displayTaskTimer.scheduleAtFixedRate(new TimerRepaint(dtest), new Date(), 1000 / FPS);
        }catch(Exception ex){
            System.out.println("Auto update initialization failed");
        }
        return dtest;
    }
    public DriveTest(){
        viewType=1;
        ncols=imageManager.getNcols();
        nrows=imageManager.getNrows();
        width=ncols;
        height=nrows;
        finishInit();
    }
    public DriveTest(int viewType){
        this.viewType=viewType;
        ncols=imageManager.getNcols();
        nrows=imageManager.getNrows();
        width=ncols;
        height=nrows;
        finishInit();
    }
    public DriveTest(int viewType, int width, int height){
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
            case 5:
                imagePixels = imageManager.getMonoRGB2Raster();
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
        PedestrianDetector pedDetect = new PedestrianDetector();
        int vEdit = (getHeight()-480)/2+10;
        limitArray = new byte[640*480];
        ImageManipulator.limitTo(limitArray,imageManager.getSimpleColorRaster(),ncols,nrows,640,480,false);
        List<MovingBlob> blobs = pedDetect.getAllBlobs(limitArray, 640);

        //We then:
        //A. Display those blobs on screen as empty rectangular boxes of the correct color
        //B. Test if those blobs are a useful roadsign/light
        //C. Do whatever we need to do if so
        //D. Write to the console what has happened
        //We need all of the if statements to display the colors,
        //as we need to convert from IPixel colors to Java.awt colors for display reasons
        for(MovingBlob i : blobs) {
            if (true) {
                if (i.color.getColor() == com.apw.pedestrians.image.Color.BLACK) {
                    graf.setColor(java.awt.Color.BLACK);
                } else if (i.color.getColor() == com.apw.pedestrians.image.Color.GREY) {
                    graf.setColor(java.awt.Color.GRAY);
                } else if (i.color.getColor() == com.apw.pedestrians.image.Color.WHITE) {
                    graf.setColor(java.awt.Color.WHITE);
                } else if (i.color.getColor() == com.apw.pedestrians.image.Color.RED) {
                    graf.setColor(java.awt.Color.RED);
                } else if (i.color.getColor() == com.apw.pedestrians.image.Color.GREEN) {
                    graf.setColor(java.awt.Color.GREEN);
                } else if (i.color.getColor() == com.apw.pedestrians.image.Color.BLUE) {
                    graf.setColor(java.awt.Color.BLUE);
                }
                graf.drawRect(i.x + 8, i.y + 40+vEdit, i.width, i.height);
            }
        }
    } //~speedPaint


}


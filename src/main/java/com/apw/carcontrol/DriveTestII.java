package com.apw.carcontrol;

import com.apw.ImageManagement.ImageManager;
import com.apw.ImageManagement.ImageManipulator;
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
import java.util.TimerTask;


public class DriveTestII extends JFrame {

    public static final int FPS = 15;

    public static ImageManager imageManager;
    public static TrakSimControl control = new TrakSimControl();
    public static Module module = new ImageManagementModule(640, 480);
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
        imageManager = starter.getImageManager();
        init(new Timer(), new Steering(starter.getSim()));
        displayTaskTimer.scheduleAtFixedRate(starter, new Date(), 1000 / FPS);
        autoDriveTest( new DriveTestII(5));
        autoDriveTest( new DriveTestII(2));
        //displayTaskTimer.scheduleAtFixedRate(new TimerRepaint(createDriveTest(3)), new Date(), 1000 / FPS);
    }
    public static void init(Timer refreshTimer, Steering steerSys) {
        displayTaskTimer = refreshTimer;
        testSteering = steerSys;
    }

    public static DriveTestII autoDriveTest(DriveTestII dtest){
        try{
            displayTaskTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    dtest.repaint();
                }
            }, new Date(), 1000 / FPS);
        }catch(Exception ex){
            System.out.println("Auto update initialization failed");
        }
        return dtest;
    }
    public DriveTestII(){
        viewType=1;
        width=ncols;
        height=nrows;
        finishInit();
    }
    public DriveTestII(int viewType){
        this.viewType=viewType;
        ncols=width=912;
        nrows=height=480;
        finishInit();
    }
    public DriveTestII(int viewType, int width, int height){
        this.viewType=viewType;
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

        control.readCameraImage();
        module.update(control);

        //pulls and manipulates image from TrakSim
        imagePixels=control.getRenderedImage();
        imagePixels = imageManager.getRGBRaster();

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
        module.paint(control);
    }
}


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

    //VARIABLES

    //Constants
    public static final int FPS = 15;                               //Frames per second the window will run at unless defined, as well as the Frame Rate of TrakSim

    //Universal window variables
    public static ImageManager imageManager;                        //Object to get camera images and change them
    public static Timer displayTaskTimer;                           //Object used to control window updates

    //Universal control variables
    public static Steering testSteering;                            //Object to control the steering

    //Internal variables
    private int viewType;                                           //How the image in the window looks: 1 = RGB, 2 = Green based Grayscale, 3 = 7 Color view, 4 = Black and White only, 5 = true grayscale,
    private int width, height;                                      //The width and height of the window
    private int ncols, nrows;                                       //The width and height of the traksim object
    private BufferedImage displayImage, bufferImage, tempImage;     //Images used for displaying on the window
    private int[] displayPixels, imagePixels, emptyPixels;          //Arrays used by images for transfering data
    private byte[] limitArray;                                      //Array used for speed code
    private ImageIcon displayicon;                                  //How the image is linked to the window
    private JLabel displaylabel;                                    //How the image is linked to the window
    private Insets edges;                                           //Object used for steering drawing

    /**Main Method
     * Starts DriveTest as well as traksim
     *
     * Note: If you do not run this method, DriveTest and TrakManager will not control TrakSim
     *
     * @param args
     */
    public static void main(String[] args){
        TrakManager starter = new TrakManager();                                         //Creates a TrakManager object, which will run TrakSim at a constant Framerate
        init(new Timer(),starter.getImageManager(), new Steering(starter.getSim()));     //Initializes DriveTest
        displayTaskTimer.scheduleAtFixedRate(starter, new Date(), 1000 / FPS);    //Initializes TrakManager at FPS frames per second
        autoDriveTest( new DriveTest(5));                                       //Format to create a new DriveTest window that updates automatically
    }

    /** Method that initializes DriveTest (Will run automatically if not ran manually)
     *
     * @param refreshTimer Object used to control window updates
     * @param imageMng Object to get camera images and change them
     * @param steerSys Object to control the steering
     */
    public static void init(Timer refreshTimer, ImageManager imageMng, Steering steerSys) { //Assigns variables
        displayTaskTimer = refreshTimer;
        imageManager = imageMng;
        testSteering = steerSys;
    }

    /** Method that initializes DriveTest (Will run automatically if not ran manually)
     *
     * Init method if you don't have existing variables you want to use
     *
     */
    public static void init(){                                                              //Assigns variables
        displayTaskTimer = new Timer();
        imageManager = new ImageManager(new SimCamera());
        testSteering = new Steering(new TrakSim());
    }

    /** Makes the given DriveTest refresh its image automatically
     *
     * @param dtest DriveTest to make so that it refreshes automatically
     * @return same DriveTest given
     */
    public static DriveTest autoDriveTest(DriveTest dtest){
        displayTaskTimer.scheduleAtFixedRate(new TimerRepaint(dtest), new Date(), 1000 / FPS);  //Initializes TrakManager at FPS frames per second
        return dtest;                                                                                  //Returns the given DriveTest
    }

    /** Makes the given DriveTest refresh its image automatically
     *
     * @param dtest DriveTest to make so that it refreshes automatically
     * @param FPS How many times per second the window should refresh
     * @return same DriveTest given
     */
    public static DriveTest autoDriveTest(DriveTest dtest, int FPS){
        displayTaskTimer.scheduleAtFixedRate(new TimerRepaint(dtest), new Date(), 1000 / FPS);  //Initializes TrakManager at the given frames per second
        return dtest;                                                                                  //Returns the given DriveTest
    }

    /** Creates a new DriveTest with an RGB view
     *
     */
    public DriveTest(){
        if(displayTaskTimer==null)                  //Checks if DriveTest is initialized
            init();                                 //Initializes DriveTest
        viewType=1;                                 //Sets the ViewType to RGB
        ncols=imageManager.getNcols();              //Copies the width of the image from imageManager
        nrows=imageManager.getNrows();              //Copies the height of the image from imageManager
        width=ncols;                                //Sets the width of the window image to be identical to the image gotten from imageManager
        height=nrows;                               //Sets the height of the window image to be identical to the image gotten from imageManager
        finishInit();                               //Calls finishInit() to finish constructor arguments
    }

    /** Creates a new DriveTest with the given view
     * @param viewType How the image in the window looks: 1 = RGB, 2 = Green based Grayscale, 3 = 7 Color view, 4 = Black and White only, 5 = true grayscale, 6 = logarithmic grayscale, 7 = green based grayscale and 255 is guaranteed white
     */
    public DriveTest(int viewType){
        if(displayTaskTimer==null)                  //Checks if DriveTest is initialized
            init();                                 //Initializes DriveTest
        this.viewType=viewType;                     //Sets the ViewType to the given value
        ncols=imageManager.getNcols();              //Copies the width of the image from imageManager
        nrows=imageManager.getNrows();              //Copies the height of the image from imageManager
        width=ncols;                                //Sets the width of the window image to be identical to the image gotten from imageManager
        height=nrows;                               //Sets the height of the window image to be identical to the image gotten from imageManager
        finishInit();                               //Calls finishInit() to finish constructor arguments
    }
    /** Creates a new DriveTest with the given view and will remove some pixels based on the given width and height
     * @param viewType How the image in the window looks: 1 = RGB, 2 = Green based Grayscale, 3 = 7 Color view, 4 = Black and White only, 5 = true grayscale, 6 = logarithmic grayscale, 7 = green based grayscale and 255 is guaranteed white
     * @param width The width of the image in the window in pixels (640 will limit only to the camera) (if width < 1 the width will be set to default)
     * @param height The height of the image of the window in pixels (480 will limit only to the camera) (if height < 1 the height will be set to default)
     */
    public DriveTest(int viewType, int width, int height){
        if(displayTaskTimer==null)                  //Checks if DriveTest is initialized
            init();                                 //Initializes DriveTest
        this.viewType=viewType;                     //Sets the ViewType to the given value
        ncols=imageManager.getNcols();              //Copies the width of the image from imageManager
        nrows=imageManager.getNrows();              //Copies the height of the image from imageManager
        if(width>0&&width<ncols)                    //Checks if the given width is an acceptable value
            this.width=width;                       //Sets the width to the given value
        else
            this.width=ncols;                       //Sets the width of the window image to be identical to the image gotten from imageManager
        if(height>0&&height<nrows)                  //Checks if the given height is an acceptable value
            this.height=height;                     //Sets the height to the given value
        else
            this.height=nrows;                      //Sets the height of the window image to be identical to the image gotten from imageManager
        finishInit();
    }

    /** Initializes variables common between all constructors
     *
     * Only called within constructors
     *
     */
    private void finishInit(){
        edges = getInsets();                                                            //Sets the insets value, which is required for steering's paint method
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);                                 //Sets the program to close when the window closes
        setSize(width,height);                                                          //Sets the window to be the size defined earlier
        displayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);    //Creates the image that will be displayed
        bufferImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);     //Creates the image one frame ahead of the one that will be displayed
        emptyPixels=new int[width*height];                                              //Creates an array that is the same size as the window
        displayicon = new ImageIcon(displayImage);                                      //How the image is linked to the window
        displaylabel = new JLabel();                                                    //How the image is linked to the window
        displaylabel.setIcon(displayicon);                                              //Attaches the ImageIcon to the JLabel
        add(displaylabel);                                                              //Attaches the JLabel to the window
        setVisible(true);                                                               //Makes the window visible
        System.out.println("--------------" + width + " " + height);                    //Sends a message to the console of the window size
    }

    @Override
    /** Handles all graphics for the window
     *
     * call repaint() not paint()
     */
    public void paint(Graphics g){
        //repaints the window
        super.paint(g);                                                                 //calls the JFrame paint function
        //paints extra information about steering and speed
        testPaint(g);
    }
    public void updateWindow(){
        //pulls and manipulates image from TrakSim
        imagePixels=null;                                                               //removes data from imagePixels
        switch(viewType){                                                               //Sets window based on viewType
            case 1:                                                                     //RGB
                imagePixels = imageManager.getRGBRaster();
                break;
            case 2:                                                                     //Green based Grayscale
                imagePixels = imageManager.getMonoRGBRaster();
                break;
            case 3:                                                                     //7 Color view
                imagePixels = imageManager.getSimpleRGBRaster();
                break;
            case 4:                                                                     //Black and White only
                imagePixels = imageManager.getBWRGBRaster();
                break;
            case 5:                                                                     //true grayscale
                imagePixels = imageManager.getMonoRGB2Raster();
                break;
            default:
                imagePixels = imageManager.getRGBRaster();
        }

        //Copies TrakSim image onto the buffer

        if(width!=ncols||height!=nrows)                                                                 //checks if the window is a different size from the imageManager image
            ImageManipulator.limitTo(emptyPixels,imagePixels,ncols,nrows,width,height,false);     //removes extra data from the imageManager image
        else
            emptyPixels = imagePixels;                                                                  //points emptyPixels to the data for later use
        displayPixels = ((DataBufferInt) bufferImage.getRaster().getDataBuffer()).getData();            //Pulls the array used in bufferImage
        System.arraycopy(emptyPixels, 0, displayPixels, 0, emptyPixels.length);           //Moves the data from emptyPixels to bufferImage

        //Replaces the buffer
        tempImage=displayImage;
        displayImage=bufferImage;
        bufferImage=tempImage;
    }

    /** Paints extra information about steering and speed
     *
     * @param graf the graphics to edit
     */
    private void testPaint(Graphics graf){
        steerPaint(graf);       //Paints components related to steer control
        speedPaint(graf);       //Paints components related to speed control
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

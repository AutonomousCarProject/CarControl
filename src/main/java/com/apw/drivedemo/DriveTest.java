package com.apw.drivedemo;

import com.apw.ImageManagement.ImageManager;
import com.apw.ImageManagement.ImageManipulator;
import com.apw.SpeedCon.Settings;
import com.apw.SpeedCon.SpeedController;
import com.apw.Steering.Point;
import com.apw.Steering.Steering;
import com.apw.apw3.*;
import com.apw.drivedemo.TimerRepaint;
import com.apw.drivedemo.TrakManager;
import com.apw.pedestrians.PedestrianDetector;
import com.apw.pedestrians.blobtrack.MovingBlob;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Timer;

public class DriveTest extends JFrame implements KeyListener, MouseListener {

    //VARIABLES

    //Constants
    public static final int FPS = 30;                               //Frames per second the window will run at unless defined, as well as the Frame Rate of TrakSim

    //Universal window variables
    public static ImageManager imageManager;                        //Object to get camera images and change them
    public static Timer displayTaskTimer;                           //Object used to control window updates
    
    
    //Universal control variables
    public static TrakManager starter;                           //Object to control the steering

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

    //DrDemo mouse variables
    //private static final int  ImgWi = DriverCons.D_ImWi;
    //private static final boolean  StartLive = DriverCons.D_StartLive;
    private static int NoneStep = 0;

    /**Main Method
     * Starts DriveTest as well as traksim
     *
     * Note: If you do not run this method, DriveTest and TrakManager will not control TrakSim
     *
     * @param args
     */
    public static void main(String[] args){
        starter = new TrakManager();                                         //Creates a TrakManager object, which will run TrakSim at a constant Framerate
        init(new Timer(),starter.getImageManager());     //Initializes DriveTest
        displayTaskTimer.scheduleAtFixedRate(starter, new Date(), 1000 / FPS);    //Initializes TrakManager at FPS frames per second
        new DriveTest(3).autoDriveTest(); //Format to create a new DriveTest window that updates automatically
        //new DriveTest(4).autoDriveTest(); //Format to create a new DriveTest window that updates automatically
        //new DriveTest(1).autoDriveTest(); //Format to create a new DriveTest window that updates automatically

    }

    /** Method that initializes DriveTest (Will run automatically if not ran manually)
     *
     * @param refreshTimer Object used to control window updates
     * @param imageMng Object to get camera images and change them
     *
     */
    public static void init(Timer refreshTimer, ImageManager imageMng) { //Assigns variables
        displayTaskTimer = refreshTimer;
        imageManager = imageMng;
    }

    /** Method that initializes DriveTest (Will run automatically if not ran manually)
     *
     * Init method if you don't have existing variables you want to use
     *
     */
    public static void init(){                                                              //Assigns variables
        displayTaskTimer = new Timer();
        imageManager = new ImageManager(new SimCamera());
    }

    /** Makes the given DriveTest refresh its image automatically
     *
     */
    public void autoDriveTest(){
        displayTaskTimer.scheduleAtFixedRate(new TimerRepaint(this), new Date(), 1000 / FPS);  //Initializes TrakManager at FPS frames per second
        //return dtest;                                                                                  //Returns the given DriveTest
    }

    /** Makes the given DriveTest refresh its image automatically
     *
     * @param FPS How many times per second the window should refresh
     */
    public void autoDriveTest( int FPS){
        displayTaskTimer.scheduleAtFixedRate(new TimerRepaint(this), new Date(), 1000 / FPS);  //Initializes TrakManager at the given frames per second
        //return dtest;                                                                                  //Returns the given DriveTest
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
        setSize(14+width,38+height);                                                          //Sets the window to be the size defined earlier
        displayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);    //Creates the image that will be displayed
        bufferImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);     //Creates the image one frame ahead of the one that will be displayed
        emptyPixels=new int[width*height];                                              //Creates an array that is the same size as the window
        displayicon = new ImageIcon(displayImage);                                      //How the image is linked to the window
        displaylabel = new JLabel();                                                    //How the image is linked to the window
        displaylabel.setIcon(displayicon);                                              //Attaches the ImageIcon to the JLabel
        add(displaylabel);                                                              //Attaches the JLabel to the window
        addMouseListener(this);
        addKeyListener(this);
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
        //testPaint(g);
    }
    public void updateWindow(){
        //pulls and manipulates image from TrakSim
        switch(viewType){                                                               //Sets window based on viewType
            case 1:                                                                     //RGB
                imagePixels = imageManager.getRGBRasterFull();
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
            case 6:
                imagePixels = imageManager.getRoad();
                break;
            default:
                imagePixels = imageManager.getRGBRaster();
        }

        //Copies TrakSim image onto the buffer
        if(width!=ncols||height!=nrows)                                                                 //checks if the window is a different size from the imageManager image
            ImageManipulator.limitTo(emptyPixels,imagePixels,ncols,nrows,width,height);     //removes extra data from the imageManager image
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
//        speedPaint(graf);       //Paints components related to speed control
    }

    /** Paints extra information about steering
     *
     * @param graf the graphics to edit
     */
    private void steerPaint(Graphics graf){
        starter.testSteering.paint(graf, imageManager, edges,(getHeight()-480)/2+10);
    } //~steerPaint

    /** Paints extra information about speed
     *
     * @param graf the graphics to edit
     */
    private void speedPaint(Graphics graf){
        starter.speedControl.paint(graf,imageManager,(getHeight()-480)/2+10);
    } //~speedPaint

    /**
	 * Converts a RGB pixel array to BufferedImage for painting. Adapted from
	 * example code found on StackExchange.
	 *
	 * @param pixels The pixel array
	 *
	 * @param width  Its width
	 *
	 * @param height Its height
	 *
	 * @return The BufferedImage result
	 */
	public BufferedImage Int2BufImg(int[] pixels, int width, int height) // (in DrDemo)
			throws IllegalArgumentException {
		int lxx = 0;
		int[] theData = null; // Raster raz = null; DataBufferInt DBI = nell;
		BufferedImage bufIm = null;
		if (pixels != null)
			lxx = pixels.length;
		if (lxx == 0)
			return null;
		if (bufIm == null) // (should be never)
			bufIm = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		theData = ((DataBufferInt) bufIm.getRaster().getDataBuffer()).getData();
		System.arraycopy(pixels, 0, theData, 0, lxx);
		return bufIm;
	} // ~Int2BufImg

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) //Left arrow key decrements steering angle by 5
            starter.SteerMe(false, -5);
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) //Right arrow key increments steering angle by 5
            starter.SteerMe(false, 5);
        if (e.getKeyCode() == KeyEvent.VK_UP) //Up arrow key increments speed by 1
            starter.AxLR8(false,1);
        if (e.getKeyCode() == KeyEvent.VK_DOWN) //Down arrow key decrements speed by 1
            starter.AxLR8(false,-1);
        if (e.getKeyCode() == KeyEvent.VK_P) //P simulates a detected stopsign
            starter.speedControl.setStoppingAtSign();
        if (e.getKeyCode() == KeyEvent.VK_O) //O simulates a detected redlight
            starter.speedControl.setStoppingAtLight();
        if (e.getKeyCode() == KeyEvent.VK_I) //I simulates a detected greenlight
            starter.speedControl.readyToGo();
        if (e.getKeyCode() == KeyEvent.VK_B) //B toggles blob rectangle overlays
            Settings.blobsOn ^= true;
        if (e.getKeyCode() == KeyEvent.VK_V) //V toggles detection boundry overlays
            Settings.overlayOn ^= true;
        if (e.getKeyCode() == KeyEvent.VK_C) //C toggles writting detected blob information to console
            Settings.writeBlobsToConsole ^= true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // SteerMe(true, 0);
    }
    @Override
    public void mouseExited(MouseEvent evt) {
    }

    @Override
    public void mousePressed(MouseEvent evt) {
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
    }

    /**
     * Recognize a mouse rollover into the top left corner of the screen from
     * outside the window, so to start up self-driving software (or whatever).
     */
    @Override
    public void mouseEntered(MouseEvent evt) { // (in DrDemo)
        /*
        Insets edges = getInsets();
        int nx = 0, Vx = 0, Hx = 0, why = 0;
        while (true) {
            why++; // why = 1
            if (!StartLive)
                return; // don't even log
            why++; // why = 2
            if (starter.Calibrating != 0)
                break;
            why++; // why = 3
            //if (!CameraView)
            //    break;
            if (evt != null)
                if (edges != null) {
                    Hx = evt.getX() - edges.left;
                    Vx = evt.getY() - edges.top;
                } // ~if
            why++; // why = 4
            if (Hx > ImgWi)
                break;
            nx = starter.sim.GridBlock(Vx, Hx); // find which screen chunk it came in..
            why++; // why = 5
            if (nx != 0x10001)
                break; // top left corner only (from outside win)..
            NoneStep = 0; // continuous
            starter.sim.SimStep(0);
            unPaused = true; // start it running..
            why++; // why = 6
            if (StartYourEngines > 0)
                break;
            if (ContinuousMode)
                starter.sim.SimStep(2);
            StartYourEngines++;
            DidFrame = 0;
            if (SimSpedFixt)
                AxLR8(true, StartGas);
            else if (DarkState < 2)
                DarkState = 2;
            // You can use (DarkState >= 2) to start your self-driving software,
            // ..or else insert code here to do that..
            why = 0;
            break;
        } // ~while
        System.out.println(HandyOps.Dec2Log("(DrDemo) Got MousEnt = ", why,
                HandyOps.Dec2Log(" @ ", Vx,
                        HandyOps.Dec2Log(",", Hx, HandyOps.Int2Log(": ", nx, HandyOps.Dec2Log(" ", StartYourEngines,
                                HandyOps.TF2Log(" g=", unPaused, HandyOps.TF2Log(" cv=", CameraView, HandyOps.Dec2Log(
                                        " ns=", NoneStep,
                                        HandyOps.Dec2Log(" ", starter.Calibrating, HandyOps.PosTime(((" @ ")))))))))))));
                                        //*/
    } // ~mouseEntered

    /**
     * Accepts clicks on screen image to control operation
     */
    @Override
    public void mouseClicked(MouseEvent evt) { // (in DrDemo)
    	
    	File out = new File("./screenshot.png");
		try {
			ImageIO.write(imageManager.getRoadLines(), "png", out);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
        Insets edges = getInsets();
        int kx = 0, nx = 0, zx = 0, Vx = 0, Hx = 0, why = 0;
        boolean didit = false;
        if (evt != null)
            if (edges != null) { // we only implement/o'ride this one
                Hx = evt.getX() - edges.left;
                Vx = evt.getY() - edges.top;
            } // ~if
        if (Hx < DriverCons.D_ImWi) {
            why = starter.sim.GridBlock(Vx, Hx); // find which screen chunk it's in..
            zx = why & 0xFF;
            nx = why >> 16;
            if (nx < 3) { // top half, switch to camera view..
                didit = ((nx | zx) == 1); // top left corner simulates covering lens..
                if (didit)
                    starter.sim.DarkFlash(); // unseen if switches to live cam
                //CameraView = (theVideo != null) && (CamPix != null);
                /*
                if (CameraView) {
                    starter.sim.SimStep(0);
                    if (starter.Calibrating > 0)
                        SteerMe(true, 0);
                    else if (starter.Calibrating < 0)
                        AxLR8(true, 0); // stop
                    else if (didit) { // if click top-left, stop so ESC can recover..
                        AxLR8(true, 0);
                        unPaused = false;
                        StartYourEngines = 0;
                    } // ~if
                    starter.Calibrating = 0;
                } // ~if
                //*/
                //DidFrame = 0;
                //unPaused = CameraView && (nx == 1);
            } // ~if // top edge runs // (nx<3)
            else if (nx == 3) { // middle region, manual steer/calibrate..
                if (starter.Calibrating < 0) {
                    if (zx == 4)
                        starter.AxLR8(true, 0);
                    //else
                        //starter.AxLR8(false, Grid_Moves[zx & 7]);
                } // ~if
                else if (zx == 4)
                    starter.SteerMe(true, 0); // Grid_Moves={0,-32,-8,-1,0,1,8,32,..
                else
                    //starter.SteerMe(false, Grid_Moves[zx & 7]);
                starter.sim.FreshImage();
            } // ~if
            else if (starter.Calibrating > 0) {
                starter.SteerMe(true, 0); // straight ahead
                starter.Calibrating = -1;
            } // ~if
            else if (starter.Calibrating < 0) {
                starter.AxLR8(true, 0); // stop
                starter.Calibrating = 0;
                starter.sim.SimStep(1);
                starter.StartYourEngines = 0;
            } // ~if
            else if (nx == 5) { // bottom, switch to sim view..
                //CameraView = false;
                //DidFrame = 0;
                if (starter.sim.IsCrashed())
                    starter.sim.SimStep(0); // clear crashed mode
                if (zx == 2)
                    NoneStep = 1; // left half: 1-step
                else
                    NoneStep = 0; // right half: continuous
                //if (ContinuousMode)
                //    starter.sim.SimStep(2);
                //else
                //    starter.sim.SimStep(1);
                //unPaused = ((zx > 1) && (zx < 4));
            } // ~if // corners: DrDemo not control speed
            else if (nx == 4) { // low half..
                if (zx < 2)
                    starter.Stopit(0); // low half, left edge, kill it politely
                //else if (!CameraView) // otherwise toggle pause..
                    //unPaused = !unPaused;
            }
        } // ~if
        /*
        else if (ShowMap) {
            zx = starter.sim.GetMapSize(); // MapHy,MapWy = size of full map
           nx = Hx - 2 - ImgWi;
            if ((Vx < (zx >> 16)) && (nx < (zx & 0xFFF)))
                starter.sim.SetStart(Vx, nx, MyMath.Trunc8(starter.sim.GetFacing()));
            else
                zx = starter.sim.ZoomMap2true(true, Vx, Hx); // sets facing to -> click
            unPaused = false; // pause if click on map
            starter.sim.FreshImage();
        } // ~if

        if (starter.Calibrating == 0) {
            why = 256;
            if (!unPaused) { // pause it..
                why--; // why = 255
                if (StartYourEngines > 0)
                    starter.sim.SimStep(0);
                StartYourEngines = 0;
                AxLR8(true, 0);
            } // ~if
            else if (StartYourEngines == 0) { // start..
                why++; // why = 257
                if (ContinuousMode)
                    starter.sim.SimStep(2);
                // else starter.sim.SimStep(1);
                StartYourEngines++;
                DidFrame = 0;
                if (SimSpedFixt && (ServoTestCount == 0))
                    AxLR8(true, StartGas);
                else if (DarkState < 2)
                    DarkState = 2;
            }
        } // ~if
        //*/
        System.out.println(HandyOps.Dec2Log("(DrDemo) Got click @ ", Vx,
                HandyOps.Dec2Log(",", Hx, HandyOps.Dec2Log(": ", nx, HandyOps.Dec2Log("/", zx,
                        HandyOps.Dec2Log(" +", kx, HandyOps.Dec2Log(" ", 0, HandyOps.TF2Log(" s=", true,
                                HandyOps.TF2Log(" g=", true, HandyOps.TF2Log(" cv=", true,
                                        HandyOps.Dec2Log(" ns=", NoneStep, HandyOps.Dec2Log(" ", starter.Calibrating,
                                                HandyOps.Dec2Log(" ", why, HandyOps.PosTime((" @ ")))))))))))))));
    } // ~mouseClicked
    
    
    
    
    
    
    
    
    
}

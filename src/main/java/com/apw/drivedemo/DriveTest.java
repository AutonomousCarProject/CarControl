package com.apw.drivedemo;

import com.apw.ImageManagement.ImageManager;
import com.apw.ImageManagement.ImageManipulator;
import com.apw.Steering.Point;
import com.apw.Steering.SteerControlCheck;
import com.apw.Steering.Steering;
import com.apw.apw3.*;

import com.apw.fakefirm.Arduino;
import com.apw.fly2cam.FlyCamera;

import javax.swing.*;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class DriveTest extends JFrame implements MouseListener {

	//VARIABLES

	//Constants
	public static final int FRAME_RATE_NUMBER = 4;	//4 corresponds to 30fps
    public static final int FPS = 15;

    //Steering and Camera Systems
    private Steering testSteering;
    
    //internal variables
    private TrakSim sim;
	private FlyCamera camSys;
	private ImageManager imageManager;
	private int nRows, nCols, width, height;
	private Arduino driveSys;
	private BufferedImage displayImage;
	private BufferedImage bufferImage;
	private BufferedImage tempImage;
	private Icon displayicon;
	private JLabel displaylabel;
	private int viewType;
	private int[] imagePixels;
	private int[] displayPixels;
	private int[] emptyPixels;

	//DrDemo Random Variables
	private int Calibrating = 0;
	private int SteerDegs = 0;
	private int GasPedal = 0;
	public int DidFrame = 0;
	public int DarkState = 0;
	private boolean OhDark = false;
	private boolean BusyPaint = false;
	private boolean CanDraw = false;
	public boolean CameraView = false;
	public boolean unPaused = false;
	private Insets edges;
	private boolean StepMe = false,SimSpedFixt = DriverCons.D_FixedSpeed;
	private final int[] Grid_Moves = {0, -32, -8, -1, 0, 1, 8, 32, 0, 1, 8, 32, 0, 1, 8, 32};
	private static final long serialVersionUID = 1L; // unneed but Java insists {
	private static final int MinESCact = DriverCons.D_MinESCact,
			MaxESCact = DriverCons.D_MaxESCact, StartGas = MinESCact * 9 / 4,
			ScrPix = TrakSim.nPixels, nServoTests = DriverCons.D_nServoTests,
			ScrHi = DriverCons.D_ImHi, ImgWi = DriverCons.D_ImWi, Haf_Scrn = ImgWi / 2,
			ScrTop = ScrHi - 1 - DriverCons.D_DrawDash, SimTile = DriverCons.D_BayTile,
			FastSet = (MaxESCact + MinESCact) / 2,
			ScrWi = TrakSim.WinWi,
			ScrFrTime = DriverCons.D_FrameTime, Scr2L = ScrWi * 2,
			CamFPS = FlyCamera.FrameRate_15 - 1, // CamTime = 528>>CamFPS, // cam frame in ms
	//SecondViewType = DriverCons.D_SecondViewType,
	DrawDash = DriverCons.D_DrawDash, CarColo = DriverCons.D_CarColo,
			AddColo = DriverCons.D_MarinBlue,
			SteerPin = DriverCons.D_SteerServo, GasPin = DriverCons.D_GasServo;
	private static final double LefScaleSt = ((double) DriverCons.D_LeftSteer) / 90.0,
			RitScaleSt = ((double) DriverCons.D_RiteSteer) / 90.0;
	private static final boolean StartInCalibrate = DriverCons.D_StartInCalibrate,
			ContinuousMode = false, DrawStuff = true, LiveCam = DriverCons.D_LiveCam,
			StartLive = DriverCons.D_StartLive, ShowMap = DriverCons.D_ShowMap,
			ShoClikGrid = DriverCons.D_ShoClikGrid;
	private static int StartYourEngines = 0, NoisyFrame = 999, // (35*4096+34)*4096+33,
			ServoTstPos = 0, ServoTestCount = 0, // remaining number of steps
			NoneStep = 0, // >0: pause simulation after each recalc
			ViDied = 0, CamTile = 0, CamTall = 0, CamWide = 0, CamFrame = 0;


	//CONSTRUCTORS

	@Deprecated
	/** DrDemo Constructor
	 *
	 * @param sim
	 * @param camSys
	 * @param viewType 1 = RGB, 2 = Monochrome, 3 = 7 Color Simplified, 4 = Only Black and White
	 */
	public DriveTest(TrakSim sim, FlyCamera camSys, int viewType) {
      	super();

      	//Allocate passed variables
      	this.sim=sim;
       	this.camSys=camSys;
    	this.viewType = viewType;

		//Check for window setting
		width=-1;

    	finishInit();
	} //~DriveTest

	/** Main Constructor
	 *
	 * @param viewType 1 = RGB, 2 = Monochrome, 3 = 7 Color Simplified, 4 = Only Black and White
	 */
    public DriveTest(int viewType){
		super();

		//Initialize Camera
		if(DriverCons.D_LiveCam){
			camSys = new FlyCamera();
		}else {
			camSys = new SimCamera();
			sim = new TrakSim();
		}
		camSys.Connect(FRAME_RATE_NUMBER);

		//Initialize Arduino
		driveSys = new Arduino();
		driveSys.pinMode(SteerPin, Arduino.SERVO);
		driveSys.pinMode(GasPin, Arduino.SERVO);

		//Set window Type
		this.viewType = viewType;

		//Check for window setting
		width=-1;

		finishInit();
	} //~DriveTest


	/** Size Choosing Constructor
	 *
	 * @param viewType 1 = RGB, 2 = Monochrome, 3 = 7 Color Simplified, 4 = Only Black and White
	 */
	public DriveTest(int viewType, int width, int height){
		super();

		//Initialize Camera
		if(DriverCons.D_LiveCam){
			camSys = new FlyCamera();
		}else {
			camSys = new SimCamera();
			sim = new TrakSim();
		}
		camSys.Connect(FRAME_RATE_NUMBER);

		//Initialize Arduino
		driveSys = new Arduino();
		driveSys.pinMode(SteerPin, Arduino.SERVO);
		driveSys.pinMode(GasPin, Arduino.SERVO);

		//Set window Type
		this.viewType = viewType;

		//Set window size variables
		this.width = width;
		this.height = height;

		finishInit();
	} //~DriveTest

	/** Common elements from both constructors
	 *
	 */
	private void finishInit(){
		//Initialize ImageManager and pull significant variables
		imageManager = new ImageManager(camSys);
		nRows = imageManager.getNrows();
		nCols = imageManager.getNcols();

		//Initialize window width and height to default if not set
		if(width==-1){
			width=nCols;
			height=nRows;
		}

		//Initialize both images for use in window
		displayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		bufferImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		//Set window properties
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(width, height);

		//extra array for window resizing
		emptyPixels = new int[width*height];

		//Create objects for use in window
		displayicon = new ImageIcon(displayImage);
		displaylabel = new JLabel();
		displaylabel.setIcon(displayicon);

		//Add objects to window
		this.add(displaylabel);
		this.addMouseListener(this);

		//Make window visible
		this.setVisible(true);

		//Initialize steering and speed controls
		initializeControl();

		//Startup debug statement
		System.out.println("**************" + nRows + " " + nCols);
	} //~finishInit

	//MAIN

	/** main method
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		//Creates the window and makes it update at the set frame rate
		Timer displayTaskTimer = new Timer();
		displayTaskTimer.scheduleAtFixedRate(new TimerRepaint(new DriveTest(3)	//IMPORTANT		set the viewType here
		) {
			@Override
			public void run() {
				window.sim.SimStep(1);
				window.TestServos(); // (replace this with your own code)
				window.repaint();
			}
		}, new Date(), 1000 / FPS);
		displayTaskTimer.scheduleAtFixedRate(new TimerRepaint(new DriveTest(2,640,480)	//IMPORTANT		set the viewType here
		) {
			@Override
			public void run() {
				window.sim.SimStep(1);
				//window.TestServos(); // (remove this from successive calls)
				window.repaint();
			}
		}, new Date(), 1000 / FPS);
	} //~main

	@Deprecated
	/** DrDemo version of main method
	 *
	 * @param sim
	 * @param camSys
	 * @param viewType 1 = RGB, 2 = Monochrome, 3 = 7 Color Simplified, 4 = Only Black and White
	 */
	public static void subMain(TrakSim sim, FlyCamera camSys, int viewType){
		Timer displayTaskTimer = new Timer();
		displayTaskTimer.scheduleAtFixedRate(new TimerRepaint(new DriveTest(sim, camSys, viewType)
		) {
			@Override
			public void run() {
				window.sim.SimStep(1);
				//window.TestServos(); // (Uses DrDemo Steering Code instead)
				window.repaint();
			}
		}, new Date(), 1000 / FPS);
	} //~subMain

	//GRAPHICS
	//PAINT

	@Override
	/** paint method
	 *
	 */
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
		if(width!=nCols||height!=nRows)
		ImageManipulator.limitTo(emptyPixels,imagePixels,nCols,nRows,width,height,false);
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
	} //~paint

	//MOUSE

	@Override
	/** Checks if the mouse is clicked
	 *  Needs to be updated
	 */
	public void mouseClicked(MouseEvent evt){
		edges = this.getInsets();
		int kx = 0, nx = 0, zx = 0, Vx = 0, Hx = 0, why = 0;
		boolean didit = false;
		if (evt != null) if (edges != null) { // we only implement/o'ride this one
			Hx = evt.getX() - edges.left;
			Vx = evt.getY() - edges.top;
		} //~if
		if (Hx < ImgWi) {
			why = sim.GridBlock(Vx, Hx); // find which screen chunk it's in..
			zx = why & 0xFF;
			nx = why >> 16;
			if (nx < 3) { // top half, switch to camera view..
				didit = ((nx | zx) == 1); // top left corner simulates covering lens..
				if (didit) sim.DarkFlash(); // unseen if switches to live cam
				//CameraView = (camSys != null)
				//		&& (CamPix != null);
				CameraView = true;
				if (CameraView) {
					sim.SimStep(0);
					if (Calibrating > 0) SteerMe(true, 0);
					else if (Calibrating < 0) AxLR8(true, 0); // stop
					else if (didit) { // if click top-left, stop so ESC can recover..
						AxLR8(true, 0);
						unPaused = false;
						StartYourEngines = 0;
					} //~if
					Calibrating = 0;
				} //~if
				DidFrame = 0;
				unPaused = CameraView && (nx == 1);
			} //~if // top edge runs // (nx<3)
			else if (nx == 3) { // middle region, manual steer/calibrate..
				if (Calibrating < 0) {
					if (zx == 4) AxLR8(true, 0);
					else AxLR8(false, Grid_Moves[zx & 7]);
				} //~if
				else if (zx == 4) SteerMe(true, 0);  // Grid_Moves={0,-32,-8,-1,0,1,8,32,..
				else SteerMe(false, Grid_Moves[zx & 7]);
				sim.FreshImage();
			} //~if
			else if (Calibrating > 0) {
				SteerMe(true, 0); // straight ahead
				Calibrating = -1;
			} //~if
			else if (Calibrating < 0) {
				AxLR8(true, 0); // stop
				Calibrating = 0;
				sim.SimStep(1);
				StartYourEngines = 0;
			} //~if
			else if (nx == 5) { // bottom, switch to sim view..
				CameraView = false;
				DidFrame = 0;
				if (sim.IsCrashed()) sim.SimStep(0); // clear crashed mode
				if (zx == 2) NoneStep = 1; // left half: 1-step
				else NoneStep = 0; // right half: continuous
				if (ContinuousMode) sim.SimStep(2);
				else sim.SimStep(1);
				unPaused = ((zx > 1) && (zx < 4));
			} //~if // corners: DrDemo not control speed
			else if (nx == 4) { // low half..
				if (zx < 2) Stopit(0); // low half, left edge, kill it politely
				else if (!CameraView) // otherwise toggle pause..
					unPaused = !unPaused;
			}
		} //~if
		else if (ShowMap) {
			zx = sim.GetMapSize(); // MapHy,MapWy = size of full map
			nx = Hx - 2 - ImgWi;
			if ((Vx < (zx >> 16)) && (nx < (zx & 0xFFF)))
				sim.SetStart(Vx, nx, MyMath.Trunc8(sim.GetFacing()));
			else zx = sim.ZoomMap2true(true, Vx, Hx); // sets facing to -> click
			unPaused = false; // pause if click on map
			sim.FreshImage();
		} //~if
		if (Calibrating == 0) {
			why = 256;
			if (!unPaused) { // pause it..
				why--; // why = 255
				if (StartYourEngines > 0) sim.SimStep(0);
				StartYourEngines = 0;
				AxLR8(true, 0);
			} //~if
			else if (StartYourEngines == 0) { // start..
				why++; // why = 257
				if (ContinuousMode) sim.SimStep(2);
				// else sim.SimStep(1);
				StartYourEngines++;
				DidFrame = 0;
				if (SimSpedFixt && (ServoTestCount == 0)) AxLR8(true, StartGas);
				else if (DarkState < 2) DarkState = 2;
			}
		} //~if
		System.out.println(HandyOps.Dec2Log("(DrDemo) Got click @ ", Vx,
				HandyOps.Dec2Log(",", Hx, HandyOps.Dec2Log(": ", nx,
						HandyOps.Dec2Log("/", zx, HandyOps.Dec2Log(" +", kx,
								HandyOps.Dec2Log(" ", StartYourEngines,
										HandyOps.TF2Log(" s=", true,
												HandyOps.TF2Log(" g=", unPaused, HandyOps.TF2Log(" cv=", CameraView,
														HandyOps.Dec2Log(" ns=", NoneStep, HandyOps.Dec2Log(" ", Calibrating,
																HandyOps.Dec2Log(" ", why,
																		HandyOps.PosTime((" @ ")))))))))))))));
	} //~mouseClicked

	@Override
	public void mousePressed(MouseEvent e) {

	} //~mousePressed

	@Override
	public void mouseReleased(MouseEvent e) {

	} //~mouseReleased

	@Override
	public void mouseEntered(MouseEvent e) {

	} //~mouseEntered

	@Override
	public void mouseExited(MouseEvent e) {

	} //~mouseExited

	//STOP

	/** Stop the system code
	 *
	 * @param why int code for why the system stopped
	 */
	private void Stopit(int why) { // gotta turn the camera & JSSC off..
		//FlyCamera myVid = theVideo;
		try {
			AxLR8(true, 0);
			SteerMe(true, 0);
			if (camSys != null) camSys.Finish();
			if (driveSys != null) driveSys.Close();
		} catch (Exception ex) {
		}
		System.out.println("-------- Clean Stop -------- " + why);
		System.exit(why);
	} //~Stopit

	//DEFAULT CONTROL

	/** DrDemo steering control
	 * Sends a steering servo message to the hardware (and to TrakSim).
	 *
	 * @param fixt True: whar is a signed absolute angle (usually 0);
	 *             False: whar is a signed inc/decrement to current setting
	 * @param whar The angle (increment) for the steering servo
	 */
	public void SteerMe(boolean fixt, int whar) { // -> SetServo // SteerServo=9
		if (!fixt) whar = SteerDegs + whar; // SteerDeg is centered on 0
		whar = MyMath.iMax(MyMath.iMin(whar, 90), -90);
		if (whar != 0) if (whar == SteerDegs) return;
		SteerDegs = whar;
		if (Calibrating == 0) {
			if (whar < 0) {
				if (LefScaleSt < 1.0) // LefScaleSt = LeftSteer/90.0
					whar = (int) Math.round(LefScaleSt * ((double) whar));
			} //~if
			else if (whar > 0) if (RitScaleSt > 1.0)
				whar = (int) Math.round(RitScaleSt * ((double) whar));
		} //~if
		if (driveSys == null) return;
		//StepMe = true;
		driveSys.servoWrite(SteerPin, whar + 90);
	} //~SteerMe

	/** DrDemo speed control
	 * Sends a drive ESC message to the hardware (and to TrakSim).
	 *
	 * @param fixt True: whar is a signed absolute velocity;
	 *             False: whar is a signed inc/decrement to current setting
	 * @param whar The velocity (increment) for the ESC
	 */
	public void AxLR8(boolean fixt, int whar) { // -> SetServo // GasServo=10
		if (!fixt) whar = GasPedal + whar; // GasPed is centered on 0
		if (whar != 0) {
			whar = MyMath.iMax(MyMath.iMin(whar, 90), -90);
			if (whar == GasPedal) return;
		} //~if
		if (Calibrating == 0) if (whar == 0) if (!fixt)
			if (StartYourEngines == 0) if (GasPedal == 0) return;
		GasPedal = whar;
		if (driveSys == null) return;
		StepMe = true;
		driveSys.servoWrite(GasPin, whar + 90);
	} //~AxLR8

	//CONTROL

	/** Steering and Speed control on each frame
	 *
	 */
	private void TestServos() { // exercise steering & ESC servos
		 //Graphics graf = new BufferedImage(nCols, nRows, BufferedImage.TYPE_INT_RGB).getGraphics();
		 steerCode();
		 speedCode();
	} //~TestServos

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

	/** initialize objects needed for control
	 *
	 */
	private void initializeControl(){
		testSteering = new Steering();
		AxLR8(false,10);
	} //~initializeControl

	/** Per frame code for controlling steering
	 *
	 */
	private void steerCode(){
		Point[] hi = testSteering.findPoints(imageManager.getRGBRaster());
		testSteering.averageMidpoints();
		int tempDeg = testSteering.getDegreeOffset();
		driveSys.servoWrite(SteerPin, (int)((tempDeg) + 90));

	} //~steerCode

	/** Per frame code for controlling speed
	 *
	 */
	private void speedCode(){
		
	} //~speedCode
}

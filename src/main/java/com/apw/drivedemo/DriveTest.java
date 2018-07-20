package com.apw.drivedemo;

import com.apw.ImageManagement.ImageManager;
import com.apw.Steering.Point;
import com.apw.Steering.SteerControlCheck;
import com.apw.Steering.Steering;
import com.apw.apw3.*;
import com.apw.fakefirm.Arduino;
import com.apw.fly2cam.FlyCamera;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DriveTest extends TimerTask implements MouseListener {
    public static final int FRAME_RATE_NUMBER = 4;	//4 corresponds to 30fps
    public static final int FPS = 15;

    private JFrame window;
    private TrakSim sim;
	private FlyCamera simcam;
	private ImageManager imagemanager;
	private int nrows, ncols;
	private Arduino driveSys;
	private BufferedImage displayimage;
	private BufferedImage bufferimage;
	private BufferedImage tempimage;
	private Icon displayicon;
	private JLabel displaylabel;
	private int viewType;
	private DrDemo clicks;
	private Steering testSteering;
	//private SteerControlCheck steerMng;
	private int[] imagepixels;
	private int[] displaypixels;

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



	public DriveTest(TrakSim sim, FlyCamera simcam, DrDemo clicks, int viewType) {
        		this.sim=sim;
        		this.simcam=simcam;
        		this.clicks=clicks;
        		this.viewType = viewType;
				finishInit();
        	}
        	public DriveTest(int viewType){
				if(DriverCons.D_LiveCam){
					simcam = new FlyCamera();
				}else {
					simcam = new SimCamera();
					sim = new TrakSim();
				}
				driveSys = new Arduino();
				driveSys.pinMode(SteerPin, Arduino.SERVO);
				driveSys.pinMode(GasPin, Arduino.SERVO);
				simcam.Connect(FRAME_RATE_NUMBER);
				clicks=null;
				this.viewType = viewType;
				finishInit();
			}
			private void finishInit(){
				imagemanager = new ImageManager(simcam);
				nrows = imagemanager.getNrows();
				ncols = imagemanager.getNcols();
				switch(viewType){
					case 2:
						displayimage = new BufferedImage(ncols, nrows, BufferedImage.TYPE_BYTE_GRAY);
						bufferimage = new BufferedImage(ncols, nrows, BufferedImage.TYPE_BYTE_GRAY);
						break;
					case 1:
					case 3:
					case 4:
						displayimage = new BufferedImage(ncols, nrows, BufferedImage.TYPE_INT_RGB);
						bufferimage = new BufferedImage(ncols, nrows, BufferedImage.TYPE_INT_RGB);
						break;
				}

				window = new JFrame();
				window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				window.setSize(ncols, nrows);
				switch(viewType){
					case 2:
						displayimage = new BufferedImage(ncols, nrows, BufferedImage.TYPE_BYTE_GRAY);
						break;
					case 1:
					case 3:
					case 4:
						displayimage = new BufferedImage(ncols, nrows, BufferedImage.TYPE_INT_RGB);
						break;
				}
				displayicon = new ImageIcon(displayimage);
				displaylabel = new JLabel();
				displaylabel.setIcon(displayicon);
				//window.addMouseListener(this);
				window.add(displaylabel);
				window.addMouseListener(this);
				window.setVisible(true);

				testSteering = new Steering();
				AxLR8(false,10);



				System.out.println("**************" + nrows + " " + ncols);
			}
        	public static void main(String[] args) {
        		Timer displayTaskTimer = new Timer();
        		displayTaskTimer.scheduleAtFixedRate(new DriveTest(3), new Date(), 1000/FPS);
        	}
			public static void subMain(TrakSim sim, FlyCamera simcam, DrDemo clicks, int viewType){
				Timer displayTaskTimer = new Timer();
				displayTaskTimer.scheduleAtFixedRate(new DriveTest(sim, simcam, clicks, viewType), new Date(), 1000/FPS);
			}
        	@Override
	public void run() {
		imagepixels=null;
		switch(viewType){
			case 1:
				imagepixels = imagemanager.getRGBRaster();
				break;
			case 2:
				imagepixels = imagemanager.getMonoRGBRaster();
				break;
			case 3:
				imagepixels = imagemanager.getSimpleRGBRaster();
				break;
			case 4:
				imagepixels = imagemanager.getBWRGBRaster();
				break;
		}
		displaypixels = ((DataBufferInt) bufferimage.getRaster().getDataBuffer()).getData();
		System.arraycopy(imagepixels, 0, displaypixels, 0, imagepixels.length);
		sim.SimStep(1);
		TestServos(); // (replace this with your own code)
		//imagepixels = ((DataBufferInt) bufferimage.getRaster().getDataBuffer()).getData();
		//displaypixels = ((DataBufferInt) displayimage.getRaster().getDataBuffer()).getData();
		//System.arraycopy(imagepixels, 0, displaypixels, 0, imagepixels.length);
		tempimage=displayimage;
		displayimage=bufferimage;
		bufferimage=tempimage;
		window.repaint();
	}
	/*
	@Override
	public void mouseClicked(MouseEvent e) {
		if(clicks!=null){
			//clicks.setVisible(true);
			clicks.mouseClicked(e);
			//clicks.setVisible(false);

		}
	}
	//*/
	@Override
	public void mouseClicked(MouseEvent evt){
		edges = window.getInsets();
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
				//CameraView = (simcam != null)
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
	}
	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}
	private void Stopit(int why) { // gotta turn the camera & JSSC off..
		//FlyCamera myVid = theVideo;
		try {
			AxLR8(true, 0);
			SteerMe(true, 0);
			if (simcam != null) simcam.Finish();
			if (driveSys != null) driveSys.Close();
		} catch (Exception ex) {
		}
		System.out.println("-------- Clean Stop -------- " + why);
		System.exit(why);
	}
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

	/**
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


	// Steering and Speed Control on each frame
	public void TestServos() { // exercise steering & ESC servos
		Graphics graf = bufferimage.getGraphics();
		steerCode(graf);
	} //~TestServos
	public void steerCode(Graphics graf){
		Point[] hi = testSteering.findPoints(imagemanager.getRGBRaster());
		testSteering.averageMidpoints();
		int tempDeg = testSteering.getDegreeOffset();
		driveSys.servoWrite(SteerPin, (int)((tempDeg) + 90));
		bufferimage.getGraphics().setColor(Color.RED);
		bufferimage.getGraphics().fillRect(100, testSteering.startingPoint, 1, 1);
		if (DriverCons.D_DrawCurrent == true) {
			for (int i = 0; i<testSteering.startingPoint - (testSteering.startingHeight + testSteering.heightOfArea); i++) {
				//bufferimage.getGraphics().fillRect(testSteering.leadingMidPoints[i].x, testSteering.leadingMidPoints[i].y +  + edges.top, 5, 5);
			}
		}


		for (int i = 0; i<hi.length; i++) {
			if (DriverCons.D_DrawPredicted == true) {
				bufferimage.getGraphics().setColor(Color.BLUE);
				//bufferimage.getGraphics().fillRect(hi[i].x, hi[i].y + edges.top, 5, 5);
			}
			if (DriverCons.D_DrawOnSides == true) {
				bufferimage.getGraphics().setColor(Color.YELLOW);
				//bufferimage.getGraphics().fillRect(testSteering.leftPoints[i].x + edges.left, testSteering.leftPoints[i].y + edges.top, 5, 5);
				//bufferimage.getGraphics().fillRect(testSteering.rightPoints[i].x + edges.left, testSteering.rightPoints[i].y + edges.top, 5, 5);
			}
		}
	}
}

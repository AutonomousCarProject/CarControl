package com.apw.drivedemo;

import com.apw.ImageManagement.ImageManager;
import com.apw.Steering.Point;
import com.apw.Steering.Steering;
import com.apw.SpeedCon.SpeedController;
import com.apw.apw3.DriverCons;
import com.apw.apw3.MyMath;
import com.apw.apw3.SimCamera;
import com.apw.apw3.TrakSim;
import com.apw.fakefirm.Arduino;
import com.apw.fly2cam.FlyCamera;

import java.awt.*;
import java.util.TimerTask;

public class TrakManager extends TimerTask {

    //Constants
    public static final int FRAME_RATE_NUMBER = 4;	//4 corresponds to 30fps

    //DrDemo Necessary Variables
    protected int Calibrating = 0;
    private int SteerDegs = 0;
    private int GasPedal = 0;
    private static final int SteerPin = DriverCons.D_SteerServo, GasPin = DriverCons.D_GasServo;
    private static final double LefScaleSt = ((double) DriverCons.D_LeftSteer) / 90.0,
            RitScaleSt = ((double) DriverCons.D_RiteSteer) / 90.0;
    protected static int StartYourEngines = 0;
    private boolean StepMe = false;


    protected TrakSim sim;
    private FlyCamera camSys;
    private Arduino driveSys;
    protected Steering testSteering;
    private ImageManager imageManager;
    protected SpeedController speedControl;
    private int ncols, nrows;

    public TrakManager(){
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

        //Initialize ImageManager and pull significant variables
        imageManager = new ImageManager(camSys);
        nrows = imageManager.getNrows();
        ncols = imageManager.getNcols();

        //Initialize steering and speed controls
        initializeControl();

        //Startup debug statement
        System.out.println("**************" + nrows + " " + ncols);


    }
    public ImageManager getImageManager(){
        return imageManager;
    }
    public TrakSim getSim(){
        return sim;
    }
    @Override
    public void run() {
        sim.SimStep(1);
        TestServos(); // (replace this with your own code)
    }
    private void TestServos() { // exercise steering & ESC servos
        //Graphics graf = new BufferedImage(nCols, nRows, BufferedImage.TYPE_INT_RGB).getGraphics();
        steerCode();
        speedCode();
    } //~TestServos


    public void Stopit(int why) { // gotta turn the camera & JSSC off..
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

    /** initialize objects needed for control
     *
     */
    private void initializeControl(){
        testSteering = new Steering();
        speedControl = new SpeedController();
        AxLR8(false,20);
    } //~initializeControl

    /** Per frame code for controlling steering
     *
     */
    private void steerCode(){
        testSteering.run(imageManager, driveSys,SteerPin);
    } //~steerCode

    /** Per frame code for controlling speed
     *
     */
    private void speedCode(){
        speedControl.run(this,GasPedal,testSteering, imageManager);
        System.out.println(GasPedal);
    } //~speedCode
}

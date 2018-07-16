/* TrakSim Car Simulator for use with NWAPW Year 3 Autonomous Car Project
 * Use this package for testing with fly2cam.FlyCamera + fakefirm.Arduino
 *
 * This simulator pretends to be a camera using the FlyCamera API, and
 * watches the commands being sent to the Arduino through FakeFirmata,
 * and controls the simulated car based on those commands, then shows
 * what a forward-facing camera on the simulated car would see.
 *
 * TrakSim copyright 2018 Itty Bitty Computers and released at this time
 * to the public as open source. There are no warranties of any kind.
 *
 * FakeFirmata is designed to work with JSSC (Java Simple Serial Connector),
 * but if you are developing your self-driving code on some computer other
 * than LattePanda, you can substitute package noJSSC, which has the same
 * APIs (as used by FakeFirmata) but does nothing.
 */
package apw3;                                       // 2018 June 12

/**
 * Separate Java file for Driving Simulator constants.
 *
 * These are here to make it easy to change the operation of TrakSim,
 *   (and then recompile). See ReadMe for descriptions.
 *
 * Some of these constants are checked for reasonable values,
 *   and TrakSim won't run unless they are.
 */
public class DriverCons { // TrakSim constant parameters

  public static final boolean // options & diagnostic switches..
    D_LiveCam = false,       // F: omit connecting to FlyCamera
    D_StartLive = false,    // T: start in live camera if possible
    D_FixedSpeed = false,    // ignore speed control, assume fMinSpeed
    D_StayInTrack = true,   // ignore steering control, stay centered in track
    D_ShoTrkTstPts = false, // T: show test points used to calc StayInTrack
    D_ShoClikGrid = false,  // T: to see where to click to steer/accelerate
    D_ShowMap = true,       // T: show the map next to the scene
    D_DoCloseUp = true,     // T: show close-up map if there is room
    D_RampServos = false,   // T: servos take time to arrive at setting
    D_TrakNoPix = false,    // T: draw track only, omit artifacts & trees
    D_UseTexTrak = true,    // T: use text file to build track to drive
    D_Reversible = false,   // T: allow reverse (untested)
    D_StartInCalibrate = false, // T: use this to calibrate servo limits
    D_Log_Draw = false, D_Log_Log = false, D_Fax_Log = false,
    D_Mini_Log = false, D_NoisyMap = true;

  public static final int
    D_Vramp = 68, D_Hramp = 172, // Initial pos'n for car, meters from NW
    D_RampA = 300,     // Initial orient'n for car, c-wise degrees from north
    D_Zoom35 = 35,     // 35mm-equivalent focal length for "camera"
    D_BayTile = 1,     // Bayer8 tiling code (RG/GB) as defined by Pt.Grey
    D_FrameTime = 200, // =5/fps, nominal frame rate for camera (must >= 20ms)
    D_nServoTests = 0, // number of times to run through test (1: no ESC)
    D_ServoMsgPos = 200*0x10000+0, // position of warning in image file,
      D_ServoMsgTL = 40*0x10001, D_ServoMsgSiz = 40*0x10000+80, // posn on screen
    D_ImHi = 480, D_ImWi = 640, // Camera image (and display window) size
    D_HalfTall = 100, D_HalfMap = 128,  // 2x2 grid map size (in 2m units)
    D_DrawDash = 12,                    // dashboard height at bottom of image
    D_SteerServo = 9, D_GasServo = 10,  // FakeArduino output pins for servos
    D_MinESCact = 10, D_MaxESCact = 22, // active range of ESC, in steps +90
    D_LeftSteer = 33, D_RiteSteer = 44, // (measured) full range (33,44)
    D_MarinBlue = 0x0099FF, // the color of driving info added to image
    D_SteerColo = 0xCC9900, // the color of the steering wheel in the image
    D_CreamWall = 0xFFFFCC, // (indoor) wall&door colors..
    D_DarkWall = 0x999966, D_BackWall = 0x66CC66, D_PilColo = 0x666666,
    D_CarColo = 0xFF0099, D_ArtiColo = 0xFFCC00, // pink car color, amber a'fact
    D_Transprnt = 0xFEFEFE, // magical interior image color -> transparent
    D_Anim_Log = 0x50020,   // log artifact +5 for 1st 32 frames if NoisyMap=T
    D_TweakRx = 0,     // adjust TurnRadius if >0, Zoom35 if <0
    D_xCloseUp = 3,    // 2^x magnification, x=0 to let TrakSim decide
    D_xTrLiteTime = 3, // 2^x seconds red time = green time +2secs yellow
    D_Crummy = 255,    // (power of 2) size of BreadCrumbs list for map display
    D_CheckerBd = 1;   // (power of 2) =1 to checker 1x1m, =2 for 2x2, =0 off

  public static final double D_TurnRadius = 7.0, // nom. meters in park coords
      // measured from midline fully cramped, servo position = 0/180
    D_fMinSpeed = 4.0, // measured min (8x actual m/s = mph/2) @ MinESCact;
      // 1mph = 0.5m/s park speed = 3"/sec @ 1:8 scale floor speed
      // 1mph floor speed is 8mph park speed = 4m/s
    D_WhiteLnWi = 0.25,   // in real-world meters, here 10"
    D_Acceleration = 0.1, // time (in secs) to achieve fMinSpeed
    D_CameraHi = 1.2;     // camera height above track in park meters

  public static final String D_SceneFiName = "TrackImg."; // +"indx" -> map file

  } //~DriverCons // (CC)

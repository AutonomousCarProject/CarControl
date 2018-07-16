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
 * SimCamera is based on fly2cam.FlyCamera, which is in the public domain.
 */
package apw3;                                      // 2018 May 25

import apw3.DriverCons;
import apw3.TrakSim;

import fly2cam.FlyCamera;

/**
 * FlyCamera Simulator for TrakSim (but different name)
 * Use this for testing in parallel with fly2cam.FlyCamera
 */
public class SimCamera extends FlyCamera {
  // public static final int FrameRate_15 = 3, FrameRate_30 = 4,
  //     BaseRose = 480, BaseColz = 640, BaseTile = 1;

  // int rose, // (protected) actual number of rows = FlyCap2.fc2Image.rows/2
  //     colz, // actual number of columns = FlyCap2.fc2Image.cols/2
  //     tile, // see FlyCapture2Defs.fc2BayerTileFormat
  //     errn, // returns an error number, see ErrorNumberText()
  //     FrameNo, // counts the number of good frames seen (nobody looks)
  //     pending; // >0 after frame look-ahead

  private static final int SimTile = DriverCons.D_BayTile,
      CamHi = DriverCons.D_ImHi, CamWi = TrakSim.WinWi;

  private static final boolean NoisyFaker = DriverCons.D_Mini_Log;

  private TrakSim theSim = null;

 /**
  * Gets one frame from the (fake) camera by fetching it from TrakSim.
  *
  * @param  pixels  Fills this array with pixels in Bayer8 encoding
  *
  * @return         True if success, false otherwise
  */
  public boolean NextFrame(byte[] pixels) { // fills pixels, false if can't (SimCam)
    if (theSim == null) return false;
    return theSim.GetSimFrame(rose,colz,pixels);} //~NextFrame // in apw3.SimCamera

 /**
  * Terminate the (fake) camera session.
  * Required by Flir/Pt.Grey drivers to prevent memory leaks.
  */
  public void Finish() { // required at end to prevent memory leaks (SimCam)
    errn = 0;
    if (NoisyFaker || errn<0) // NF=Mini_Log
      System.out.println(HandyOps.Dec2Log(" (SimCam) --> Finis ",errn,""));
    rose = 0;
    colz = 0;
    tile = 0;
    FrameNo = 0;} //~Finish

 /**
  * Start a new (fake) camera session with the specified frame rate.
  *
  * @param  frameRate  =4 for 30 fps, =3 for 15, =2 for 7.5 pfs
  *
  * @return         True if success, false otherwise
  */
  public boolean Connect(int frameRate) { // (SimCam) rtns F at eof
    int why = 27;          // sets rose,colz,tile frameRate is ignored
    errn = 0;
    while (true) {
      if (frameRate != 0) if (frameRate != FrameRate_15)
        if (frameRate != FrameRate_30) if (frameRate != FrameRate_15-1)
          if (frameRate != FrameRate_15-2) break; // why = 27
      why = 4;
      if (theSim == null) break;
      why++; // why = 5
      if (!theSim.StartImage(CamHi,CamWi,1)) break;
      rose = CamHi;
      colz = CamWi;
      tile = SimTile;
      FrameNo = 0;
      why = 0;
      break;} //~while
    errn = why;
    if (NoisyFaker || why>0) // NF=Mini_Log
    System.out.println(HandyOps.Dec2Log("(SimCam) --> Connect ",CamHi,
        HandyOps.Dec2Log("/",CamWi,HandyOps.Dec2Log(" ",why,"")))); // why =
    return why==0;} //~Connect

  // public static String ErrorNumberText(int errno) -> super
  // public int Dimz() {return (rose<<16)+colz;} // access cam image size (SimCam)
  // public int PixTile() {return tile;} // Bayer encoding, frex RG/GB =1, GB/RG =3

 /**
  * Tells if this camera is live or fake.
  *
  * @return      false, because this camera is only a fake
  */
  public boolean Live() {return false;} // this is not a live camera (SimCam)

  public String toString() { // (SimCam)
    return "apw3.SimCamera " + errn + ": " + ErrorNumberText(errn);}

  public SimCamera() {
    if (NoisyFaker) System.out.println(HandyOps.Dec2Log("apw3.SimCamera ",CamHi,
        HandyOps.Dec2Log("/",CamWi,"")));
    theSim = new TrakSim();
    theSim.StartPatty("FlyCam");
    rose = CamHi;
    colz = CamWi;
    tile = SimTile;
    FrameNo = 0;
    errn = 0;}} //~SimCamera (apw3) (SC)

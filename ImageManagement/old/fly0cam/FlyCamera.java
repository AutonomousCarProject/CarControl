
package group1.fly0cam; // (same API as fly2cam)                  // 2017 February 27

import java.io.File;
import java.io.FileInputStream;

public class FlyCamera {
  public static final int FrameRate_15 = 3, FrameRate_30 = 4;

  public int rose, // actual number of rows = FlyCap2.fc2Image.rows/2
      colz, // actual number of columns = FlyCap2.fc2Image.cols/2
      tile, // see FlyCapture2Defs.fc2BayerTileFormat
      errn, // returns an error number, see ErrorNumberText()
      frameNo, // counts the number of good frames seen (nobody looks)
      pending; // >0 after frame look-ahead
  private FileInputStream theFile;

  private static byte[] camBytes = null;
  private static byte[] FourBytes = null;

  public boolean NextFrame(byte[] pixels) { // fills pixels, false if can't
    int lxx = 0, info = 0, want = 0, grow = 0, why = 9;
    try { while (true) {
      why++; // why = 10
      if (FourBytes==null) FourBytes = new byte[4];
      if (FourBytes==null) break;
      why++; // why = 11
      if (pixels != null) want = pixels.length;
      else if (tile<0) want--; // from Connect
        else break;
      why++; // why = 12
      if (theFile==null) break;
      why++; // why = 13
      if (pending==0) {
        lxx = theFile.read(FourBytes);
        if (lxx<4) break; // why = 13
        why++; // why = 14
        if (want==0) break; // if (tile >= 0)
        if (want > 0xFFFFFF) break;
        why++; // why = 15
        if (((FourBytes[0]-0xFC)&254) !=0) break;
        if (((FourBytes[1]-0xB8)&255) !=0) break;
        info = FourBytes[2]&255; // = tall/16
        lxx = FourBytes[3]&255;  // = wide/16
        if (info+lxx==0) { // end of file (no more frames)
          why = 18;
          break;}
        if (info<2) break; // min 32x32
        if (info>128) break; // max 2Kx4K
        if (lxx<2) break;
        if (lxx==255) lxx++;
        if (info==7) info = 120;
          else info = info<<4;
        lxx = lxx<<4;
        if (tile <= 0) {
          if (tile==0) tile = ((FourBytes[0]&1)<<1)+1;
          rose = info;
          colz = lxx;}
        else if (rose != info) break; // why = 15
        else if (colz != lxx) break;
        lxx = info*lxx*4;
        if (camBytes==null) camBytes = new byte[lxx];
        if (camBytes==null) {
          why = 9;
          break;}
        why++; // why = 16
        info = theFile.read(camBytes);
        if (info<lxx) break;
        if (tile<0) {
          tile = ((FourBytes[0]&1)<<1)+1;
          pending = lxx;
          why = 0;
          break;}} //~if (pending=0)
      else lxx = pending;
      why = 0;
      if (want != lxx) why = -20;
      if (want<lxx) lxx = want;
      if (lxx>0) System. arraycopy(camBytes,0,pixels,0,lxx);
      frameNo++;
      pending = 0;
      break;} //~while
    } catch (Exception ex) {
      pending = 0;
      why = 17;}
    errn = why;
    return why <= 0;} //~NextFrame

  public void Finish() { // required at end to prevent memory leaks
    errn = 0;
    if (theFile==null) errn = -1;
      else try { theFile.close(); } catch (Exception ex) {errn--;}
    theFile = null;
    frameNo = 0;
    pending = 0;
    rose = 0;
    colz = 0;
    tile = 0;} //~Finish

  public boolean Connect(int frameRate, String fiName) { // required at start, sets rose,colz,tile

//    System.out.println("test: " + new File("group1/fly0cam/FlyCapped.By8").exists());

    int why = 27;                         // frameRate is ignored; rtns false at eof
    File myFile = new File(fiName);
    if (theFile==null) {
      // theFile = null;
      frameNo = 0;
      pending = 0;
      rose = 0;
      colz = 0;
      tile = 0;}
    else Finish();
    errn = 0;
    while (true) {
      if (frameRate != 0) if (frameRate != FrameRate_15) if (frameRate != FrameRate_30)
        if (frameRate != FrameRate_15-1) break; // why = 27
      why = 1;
      if (myFile==null) break;
      why++; // why = 2
      try { theFile = new FileInputStream(myFile);
        } catch (Exception ex) {theFile = null;}
      if (theFile==null) break;
      why++; // why = 3
      tile = -1;
      if (!NextFrame(null)) {
        why = errn; // why = 10..19
        Finish();
        break;}
      if (pending==0) break; // why = 3 (can't)
      why = 0;
      break;} //~while
    if (why>0) if (why<3) System.out.println("Can't open file " + fiName);
    errn = why;
    System.out.println("Tile: " + tile);
    return why==0;} //~Connect


  public static String ErrorNumberText(int errno) { // to explain errn in toString()
    if (errno == -20) return "ByteArray is not same size as received data";
    switch (errno) {
    case -1: return "No camera connected";
    case 0: return "No error";
    case 1: return "Cannot open File";
    case 2: return "Cannot open FileInputStream";
    case 3: return "No cameras detected";
    case 4: return "fc2GetCameraFromIndex did not find first camera";
    case 5: return "fc2Connect failed to connect to first camera";
    case 6: return "fc2StartCapture failed";
    case 7: return "fc2CreateImage failed";
    case 8: return "No error";
    case 9: return "fc2RetrieveBuffer failed";
    case 10: return "Couldn't allocate 4 bytes";
    case 11: return "ByteArray to NextFrame is null";
    case 12: return "Connect failed or not called (context = null)";
    case 13: return "File is too short";
    case 14: return "ByteArray is way too short or too long";
    case 15: return "File is wrong format";
    case 16: return "fc2RetrieveBuffer failed, possibly file corruption";
    case 17: return "Exception thrown";
    case 18: return "No pixel data received";
    case 19: return "Unknown file image size";
    case 20: return "No error";
    case 21: return "fc2StopCapture failed";
    case 22: return "fc2DestroyImage failed";
    case 23: return "Both fc2StopCapture and fc2DestroyImage failed";
    case 26: return "fc2GetProperty failed";
    case 27: return "Unknown frame rate";
    case 28: return "fc2SetProperty failed";} //~switch
    return "fc2RetrieveBuffer probably returned some format other than Bayer8";
  } //~ErrorNumberText

  public int Dimz() {return (rose<<16)+colz;} // access to image size from camera
  public int PixTile() {return tile;} // image Bayer encoding, frex RG/GB = 1
  public String toString() {return "fly0cam.FlyCamera " + ErrorNumberText(errn);}
  public boolean Live() {return false;} // this is not a live camera (fly0cam)

  public FlyCamera() {
    FourBytes = new byte[4];
    theFile = null;
    frameNo = 0;
    pending = 0;
    rose = 0;
    colz = 0;
    tile = 0;
    errn = 0;}} //~FlyCamera (fly0cam) (FC)

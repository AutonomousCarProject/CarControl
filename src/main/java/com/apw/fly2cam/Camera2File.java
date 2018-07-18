package com.apw.fly2cam;                                         // 2017 February 27

import java.io.File;
import java.io.FileOutputStream;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Graphics;
import java.awt.Insets; // not available at win creation
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Camera2File extends JFrame implements MouseListener {
  private static final long serialVersionUID = 1L; // don't need it but Java insists {

  final static String fiName = "FlyCapped"; // => "FlyCapped.By8";
  final boolean PauseReco = false, NoisyFaker = false, TestColorz = false;
  final static int BigCam = 2, fps = 15, FrameTime = 66, // 15/second
      ImHi = 240*BigCam, ImWi = 320*BigCam, nPixels = ImHi*ImWi,
      Lin2 = ImWi*2, Lin6 = Lin2*6, CornerBox = 32;
  private BufferedImage theImag = null; // (needed in myAction)..
  private static JFrame theWindow = null;   // bottom-up right-to-left = Little-Endian
  
 boolean autoShutter = true;

  public static class RunSoon implements Runnable { @Override
    public void run() {starting();}} //~RunSoon

  private OldFlyCamera theVideo = null;
  private FileOutputStream theFile = null;
  private Timer TickTock = null;
  private BufferedImage theBuff = null;
  private byte[] theHead = null;
  private short[] camBytes = null;
  private int[] thePixels = null;
  private final int[] Digitz = {0x696,0x9F1,0xB95,0x9DA,0x62F,0xDDA,0x6D2,0x8BC,0xA5A,0x4B6};

  private class myAction implements ActionListener { @Override
    public void actionPerformed(ActionEvent evt) {
      theImag = null; // so paint will get next img
      if (theWindow != null) theWindow.repaint();}} //~myAction

  private myAction doOften = null; // private boolean TestColorz = true;

  public int IzFile, tile, twosie, nFiles, toRecord, CamFPS, nFrames;

  public void mouseEntered(MouseEvent evt) {} // we don't need these but Java wants them..
  public void mouseExited(MouseEvent evt) {}
  public void mousePressed(MouseEvent evt) {}
  public void mouseReleased(MouseEvent evt) {}

  public void StopRecording() {
    System.out.println("** StopRecord " + (theFile != null) + " " + (theHead != null)
        + " +" + toRecord + " **");
    if (theFile==null) return;
    try { if (theHead != null) {
      theHead[0] = (byte) 0xFC;
      theHead[1] = (byte) 0xB8;
      theHead[2] = (byte) 0;
      theHead[3] = (byte) 0;               // WB trashes the data..
      theFile.write(theHead);}
    theFile.flush();
    theFile.close();
    } catch (Exception ex) {}
    theFile = null;
    toRecord = 0;} //~StopRecording

  public void WriteFrame() {
    if (theFile==null) return;
    if (camBytes==null) return;
    if (theHead==null) return;
     if (!PauseReco) if (toRecord>0) try {
      theHead[0] = (byte) ((tile>>1)+0xFC); // =FD for tile=3
      theHead[1] = (byte) 0xB8;
      theHead[2] = (byte) (ImHi>>4);
      theHead[3] = (byte) (ImWi>>4);        // WB trashes the data..
      theFile.write(theHead);
      byte[] temp = new byte[camBytes.length];
      for(int i = 0; i < camBytes.length; i++){
        temp[i] = (byte)(camBytes[i] >> 8);
      }
      theFile.write(temp);
      toRecord--;}
    catch (Exception ex) {toRecord = 0;}
    if (toRecord <= 0) StopRecording();} //~WriteFrame

  public void AddFrameNo() { //
    int bitz, here, thar = ImWi*10-7, whom = nFrames, colo = 0xFFCC00; // orange
    int[] myPix = thePixels;
    if (toRecord>0) if (!PauseReco) {
      colo = 0xFF0000; // red
      whom = toRecord;}
    if (myPix == null) return;
    while (true) {
      bitz = whom/10;
      here = whom-bitz*10;
      whom = bitz;
      if (here<0) break;
      if (here>9) break;
      bitz = Digitz[here]|0x1110000;
      while (bitz>0x1110) {
        if ((bitz&1) !=0)
          myPix[thar] = colo;
        bitz = bitz>>1;
        if ((bitz&0x1000)==0) thar = thar-ImWi;
          else thar = thar+ImWi*3-1;}
      if (whom==0) break;
      thar = thar-1;}} //~AddFrameNo                         // (in Camera2File)

  public void AddRedDot() {
    int thar = ImWi*5+5;
    int[] myPix = thePixels;
    if ((toRecord&8) !=0) if (!PauseReco) return;
    if (myPix == null) return;
    // myPix[thar] = 0xFF0000;
    myPix[thar+1] = 0xFF0000;
    myPix[thar+2] = 0xFF0000;
    myPix[thar+ImWi] = 0xFF0000;
    if (!PauseReco) {
      myPix[thar+ImWi+1] = 0xFF0000;
      myPix[thar+ImWi+2] = 0xFF0000;}
    myPix[thar+ImWi+3] = 0xFF0000;
    thar = thar+ImWi*2;
    myPix[thar] = 0xFF0000;
    myPix[thar+3] = 0xFF0000;
    myPix[thar+ImWi+1] = 0xFF0000;
    myPix[thar+ImWi+2] = 0xFF0000;
    if (PauseReco) return;
    myPix[thar+1] = 0xFF0000;
    myPix[thar+2] = 0xFF0000;} //~AddRedDot

  public boolean CamSize(OldFlyCamera myVid) { // true if good
    int hi = 0, wi = 0;
    twosie = 0;
    tile = 0;
    if (myVid != null) {
      tile = myVid.PixTile();
      hi = myVid.Dimz();}
    if (tile != 1) if (tile != 3) return false;
    if (hi==0) return false;
    wi = hi&0xFFFF;
    hi = hi>>16;
    if (wi != ImWi) {
      if (hi+hi == ImHi) {
        if (wi+wi == ImWi) twosie++;} // gotta double what cam gives us
      else if (ImHi+ImHi == hi) if (ImWi+ImWi == wi)
        twosie--; // gotta halve what cam gives us
      if (twosie !=0) return true;}
    else if (hi == ImHi) return true;
    System.out.println("Wrong image size, should be "
        + wi + "x" + hi + " / " + ImWi + "x" + ImHi);
    return false;} //~CamSize

  public boolean RestartCam() { // true if did
    boolean didit = false;
    OldFlyCamera myVid = theVideo;
    if (camBytes != null) if (myVid != null) if (IzFile == 1) {
      IzFile = -1;
      myVid.Finish();
      if (myVid.Connect(CamFPS, 0, autoShutter ? 0 : 50, 0)) if (CamSize(myVid)) didit = myVid.NextFrame(camBytes);}
    return didit;} //~RestartCam

  public boolean GetCameraImg() { // -> thePixels, true if OK
    int rx, cx, zx = 0, whom = 0, here = 0, thar = 0;        // (in Camera2File)
    boolean gotit = false;
    short[] myBy = camBytes; // local vars visible in debugger ;-)
    int[] myPix = thePixels;
    OldFlyCamera myVid = theVideo;
    if (myBy != null) if (myPix != null) if (myVid != null)
      gotit = myVid.NextFrame(myBy);
    if (!gotit) {
      if (toRecord==0) return false;
      if (IzFile != 1) return false;
      if (!RestartCam()) return false;}
    if (IzFile <= 0) if (myVid != null) {
      if(myVid.Live()) IzFile = 2;
        else IzFile = 1;}
    for (rx=0; rx<ImHi; rx++) {
      if (twosie>0) { // double what cam gives us..
        if ((rx&1) !=0) { // recopy even lines onto odd..
          for (cx=0; cx<ImWi; cx++) {
            myPix[thar] = myPix[thar-ImWi];
            thar++;} //~for (cx)
          continue;} //~if (rx&1)
        zx = ImWi>>1;} //~if (twosie>0)
      else zx = ImWi;
      for (cx=zx; cx>0; cx--) {
        if (myBy == null) break; // can't, Java throws an exception instead
        if (twosie<0) zx = here+Lin2+Lin2; // (cam rows are double-wide)
        else if (twosie>0) zx = here+ImWi; // (cam rows are half-wide)
          else zx = here+Lin2;
        if (tile==1) whom = ((((( (int) myBy[here])&255)<<8) // RG/GB
            | ( (int) myBy[here+1])&255)<<8)
            | ( (int) myBy[zx+1])&255; // (ignore 2nd green)
        else whom = ((((( (int) myBy[zx])&255)<<8)           // GB/RG
            | ( (int) myBy[here])&255)<<8)
            | ( (int) myBy[here+1])&255; // if (tile==3)
        if (twosie<0) here = here+4; // (skip alternate pixels)
          else here = here+2;
        myPix[thar] = whom; // <- unsaturated result
        if (twosie>0) { // gotta H-double what cam gives us..
          thar++;
          myPix[thar] = whom;}
        thar++;} //~for (cx)
      if (twosie<0) here = here+Lin6; // =ImWi*4*3: skip alternate cam lines
      else if (twosie>0) here = here+ImWi; // (cam rows are half-wide)
        else here = here+Lin2;} //~for (rx)
    if (TestColorz) {
      myBy[0] = 0;       myBy[Lin2] = 0;                             // black..
      myBy[1] = 0;       myBy[Lin2+1] = 0;
      if (tile==1) { // (RG/GB)                                      // red..
        myBy[2] = (byte) 255; myBy[Lin2+2] = 0;}
      else {myBy[2] = 0; myBy[Lin2+2] = (byte) 255;}
      myBy[3] = 0;       myBy[Lin2+3] = 0;
      if (tile==1) {                                                 // green..
        myBy[4] = 0;     myBy[Lin2+4] = (byte) 255;
        myBy[5] = (byte) 255; myBy[Lin2+5] = 0;}
      else {
        myBy[4] = (byte) 255; myBy[Lin2+4] = 0;
        myBy[5] = 0;     myBy[Lin2+5] = (byte) 255;}
      myBy[6] = 0;       myBy[Lin2+6] = 0;                           // blue..
      if (tile==1) {
        myBy[7] = 0; myBy[Lin2+7] = (byte) 255;}
      else {myBy[7] = (byte) 255; myBy[Lin2+7] = 0;}
      myBy[8] = 119;     myBy[Lin2+8] = 119;                         // gray..
      myBy[9] = 119;     myBy[Lin2+9] = 119;
      myBy[10] = (byte) 255;  myBy[Lin2+10] = (byte) 255; // white..
      myBy[11] = (byte) 255;  myBy[Lin2+11] = (byte) 255;}
    return true;} //~GetCameraImg

  public BufferedImage Int2BufImg(int[] pixels, int width, int height)
      throws IllegalArgumentException {
    int lxx = 0;
    int[] theData = null; // Raster raz = null;  DataBufferInt DBI = nell;
    BufferedImage bufIm = null;
    if (pixels != null) lxx = pixels.length;
    if (lxx==0) return null;
    if (width==ImWi) if (height==ImHi) bufIm = theBuff;
    if (bufIm==null) {
      bufIm = new BufferedImage(width,height, BufferedImage. TYPE_INT_RGB);
      }
    if (bufIm==null) return null;
    theData = ((DataBufferInt) bufIm.
        getRaster().getDataBuffer()). getData();
    System. arraycopy(pixels,0,theData,0,lxx);
    return bufIm;} //~Int2BufImg                                       // (in Camera2File)

  // @Override // unneeded, just gotta spell it correctly
  public void paint(Graphics graf) {
    Insets edges;
    edges = getInsets(); // should be valid now
    super.paint(graf);
    if (theImag == null) try {
      if (thePixels != null) { // already got default blue-gray or prior image
        if (GetCameraImg()) { // ..camBytes trash if GetCam fails
          AddFrameNo();
          if (toRecord>0) AddRedDot();}
        else if (toRecord>0) StopRecording();
        theImag = Int2BufImg(thePixels,ImWi,ImHi); // WriteFr writes (&trashes) camBytes..
        if (toRecord>0) if (!PauseReco) if ((((FrameTime>>6)|IzFile|nFrames)&1) !=0)
          WriteFrame();} // assumes fps=15
      if (theImag == null) {
        System.out.println("Got null camera image " + nFrames);
        System.exit(-1);}}
    catch (Exception ex) {theImag = null;}
    if (theImag != null) if (graf != null) if (edges != null) {
      graf.drawImage(theImag,edges.left,edges.top,null);
      nFrames++;}} //~paint

  @Override public void mouseClicked(MouseEvent evt) {
    int Vx = 0, Hx = 0;                        // we only implement/o'ride this one
    String aName = ".By8";
    File myFile = null;
    OldFlyCamera myVid = theVideo;
    if (evt != null) {
      Hx = evt.getX();
      Vx = evt.getY();}
    System.out.println("Got click @ " + Vx + "," + Hx + " IzFi=" + IzFile + " toRec=" + toRecord
        + " nFi=" + nFiles + " theFi=" + (theFile != null) + " 2x=" + twosie);
    if (IzFile==0) return; // ignore if no image on-screen
    if (toRecord==0) {
      if (theFile != null) StopRecording(); // sets it =null
      if (evt==null) return;
      if (theHead==null) return;
      if (IzFile<0) if (!RestartCam()) return; // cam file stopped, can't restart
      if (Vx<CornerBox) {
        if (Hx<CornerBox) toRecord = 1; // top-left: do one frame
        else if (Hx>ImWi-CornerBox) toRecord = 8;} // top-right: do 8 frames
      else if (Vx>ImHi-CornerBox) {
        if (Hx<CornerBox) toRecord = fps*20; // botm-left: do 20 secs
        else if (Hx>ImWi-CornerBox) toRecord = fps*60;} // botm-right: do one minute
      if (toRecord==0) toRecord = (fps*5)|1; // anywhere else: do 5 secs
      if (toRecord>fps) toRecord = (toRecord)&-8|8; // always show red dot 1st & last 1/2 sec..
      if (nFiles>0) aName = nFiles + aName;
      else if (myVid != null) if (!myVid.Live()) aName = "0.By8";
      aName = fiName + aName;
      nFiles++;                            // if fails, probably can't write there..
      myFile = new File(aName); try {
      if (myFile != null) theFile = new FileOutputStream(myFile);
      } catch (Exception ex) {theFile = null;}
      // TestColorz = true;
      System.out.println(".. open " + aName + " toRec=" + toRecord
          + " " + (myFile != null) + " " + (theFile != null));
        if (theFile==null) toRecord = 0;} //~if (toRecord=0)
    else StopRecording();} //~mouseClicked

  private static void starting() {theWindow = new Camera2File();}

  public static void main(String[] args) {
    Runnable runFrameLater = new RunSoon();
    System.out.println("(main) image size " + ImWi + "x" + ImHi + " = " + nPixels);
    SwingUtilities.invokeLater(runFrameLater);}

  Camera2File() { // doesn't get called (soon enough) in Java !?
    int nx = nPixels;
    boolean didit = false;
    Timer titok;
    OldFlyCamera myVid;
    String sayso = "= ";
    int[] myPix;
    tile = 0;
    IzFile = 0;
    nFiles = 0;
    twosie = 0;
    nFrames = 0;
    toRecord = 0; // number of frames to go (while recording)
    CamFPS = 0;
    if (fps==15) CamFPS = OldFlyCamera. FrameRate_15;
    else if (fps==30) CamFPS = OldFlyCamera. FrameRate_30;
    myPix = new int[nPixels];
    thePixels = myPix;
    while (nx>0) {
      nx--;
      myPix[nx] = 0x6699CC;} //~while // prefill with blue-gray
    theHead = new byte[4];
    theVideo = new OldFlyCamera();
    doOften = new myAction();
    TickTock = new Timer(FrameTime,doOften);
    theBuff = new BufferedImage(ImWi,ImHi, BufferedImage. TYPE_INT_RGB);
    camBytes = new short[nPixels*4];
    titok = TickTock;
    myVid = theVideo;
    try {
      if (myVid.Connect(CamFPS, 0, autoShutter ? 0 : 50, 0)) didit = CamSize(myVid);
    } catch (Exception ex) {didit = false;}
    if (!didit) {
      System.out.println("Connect failed");
      System.exit(-1);
      return;}
    else if (titok != null) titok.start();
    sayso = sayso + ImWi + "x" + ImHi;
    if (twosie>0) sayso = "doubled " + sayso;
    else if (twosie<0) sayso = "halved " + sayso;
    if (tile==1) sayso = "RG/GB " + sayso;
    else if (tile==3) sayso = "GB/RG " + sayso;
    System.out.println("Connected " + sayso);
    setTitle("FlyCapture Camera"); // was: this.setTitle etc..
    setSize(ImWi+18,ImHi+40); // make it larger for insets to come
    setDefaultCloseOperation( JFrame. EXIT_ON_CLOSE);
    addMouseListener(this); // (MouseListener)
    setVisible(true);}} //~Camera2File (flycam2file) (CF)

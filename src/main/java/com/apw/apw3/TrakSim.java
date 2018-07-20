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
package com.apw.apw3;                                       // 2018 June 13


import com.apw.fakefirm.Arduino;
import com.apw.fakefirm.SimHookBase;

import java.io.File;
import java.io.FileInputStream;

/**
 * The main TrakSim Car Simulator class..
 */
public class TrakSim {


    // fGratio cnvrts ESC steps to nominal velocity; fMinSpeed=4.0,MinESC=10
    // ..adjust multiplier so it's correct for your car: *1.0 => fGratio=0.4
    private static final double fMinSpeed = DriverCons.D_fMinSpeed,
            fMinESC = (double) DriverCons.D_MinESCact,
            fGratio = 1.0 * fMinSpeed / fMinESC;
    private static final String SceneFiName = DriverCons.D_SceneFiName;
    private static final boolean RampServos = DriverCons.D_RampServos,
            Log_Draw = DriverCons.D_Log_Draw, Log_Log = DriverCons.D_Log_Log,
            Mini_Log = DriverCons.D_Mini_Log, DoCloseUp = DriverCons.D_DoCloseUp,
            ShowMap = DriverCons.D_ShowMap, TrakNoPix = DriverCons.D_TrakNoPix,
            NoisyMap = DriverCons.D_NoisyMap, Reversible = DriverCons.D_Reversible,
            UseTexTrak = DriverCons.D_UseTexTrak, Fax_Log = DriverCons.D_Fax_Log,
            ShoClikGrid = DriverCons.D_ShoClikGrid, GoodLog = true;

    private static final int Vramp = DriverCons.D_Vramp, // imported constants,
            ImHi = DriverCons.D_ImHi, ImWi = DriverCons.D_ImWi, // ..local names
            Hramp = DriverCons.D_Hramp, RampA = DriverCons.D_RampA,
            HalfMap = DriverCons.D_HalfMap, HalfTall = DriverCons.D_HalfTall,
            SteerServo = DriverCons.D_SteerServo, GasServo = DriverCons.D_GasServo,
            MinESCact = DriverCons.D_MinESCact, MaxESCact = DriverCons.D_MaxESCact,
            DrawDash = DriverCons.D_DrawDash,
            FrameTime = DriverCons.D_FrameTime,
            Zoom35 = DriverCons.D_Zoom35, xCloseUp = DriverCons.D_xCloseUp,
            CreamWall = DriverCons.D_CreamWall, DarkWall = DriverCons.D_DarkWall,
            BackWall = DriverCons.D_BackWall, PilasterCo = DriverCons.D_PilColo,
            CarColo = DriverCons.D_CarColo, ArtiColo = DriverCons.D_ArtiColo,
            MarinBlue = DriverCons.D_MarinBlue, //SteerColo = DriverCons.D_SteerColo,
            xTrLiteTime = DriverCons.D_xTrLiteTime,
            BayerTile = DriverCons.D_BayTile, // 1=RG/GB

    MapTall = HalfTall * 2, MapWide = HalfMap * 2, SteerMid = ImWi / 3,
            ImHaf = ImHi / 2, ImMid = ImWi / 2, MapWiBit = 8, Tintx = 0, ArtBase = 8,
            zx50 = 28, // = fudge-factor to make Zoom35=50 come out right
            ZoomPix = ImWi * zx50 / Zoom35, // divide this by distance for pix/meter
            ParkDims = MapTall * 0x10000 + MapWide, CheckerBd = DriverCons.D_CheckerBd,
            ServoStepRate = 0, // = FrameTime/20, // Vscale = DriverCons.D_Vscale,
            TweakRx = DriverCons.D_TweakRx, Crummy = DriverCons.D_Crummy;

    public static final int WinWi = (ShowMap ? MapWide + 16 : 0) + ImWi, // window width
            Lin2 = WinWi * 2, nPixels = ImHi * WinWi; // + pix in whole window
    private static final double TurnRadius = DriverCons.D_TurnRadius,
            LfDeScaleSt = 90.0 / ((double) DriverCons.D_LeftSteer),
            RtDeScaleSt = 90.0 / ((double) DriverCons.D_RiteSteer),
            WhiteLnWi = DriverCons.D_WhiteLnWi, Fby3 = 1.0 / 3.0,
            Acceleration = DriverCons.D_Acceleration,
            fFtime = (double) FrameTime / 1000.0, dFtime = 1.0 / fFtime, // fps
            fTurn4m = 2.0 / (TurnRadius * Math.PI), // -> degs/degs/m
            fSweeper = 20.0, // how much to look aside in StayInTrk
            fMaxSpeed = fGratio * ((double) MaxESCact),
            MaxRspeed = (Reversible ? -0.5 * fMaxSpeed : 0.0),
            fTime4mass = fMinESC * fFtime / Acceleration;
    private static final String[] CompNames = {" N  ", " NNE ", " NE ", " ENE ",
            " E  ", " ESE ", " SE ", " SSE ", " S  ", " SSW ", " SW ", " WSW ",
            " W  ", " WNW ", " NW ", " NNW "};
    private static final String StopInfo // see: case '@':
            // [O r c f] v-rng tall wide H-off ppm anim..
            = " 90 128 29 1 44 0~1 -- stop sign full-on (default)\n"
            + " 90 64 15 60 22 0~2 -- stop sign back full-on\n"
            + " 160 128 17 32 44 0~3 -- stop sign front angled\n"
            + " 160 64 9 50 22 0~4 -- stop sign back angled\n"
            + " 180 64 3 76 22 0~5 -- stop sign S-edge-on\n"
            + " 0 64 3 80 22 0~6 -- stop sign P-edge-on\n"
            + " 255 25 84 50~7 \n" // dark traffic lite
            + " 0 255 25 110 50 1~8`5 -- green lite\n"
            + " 0 255 25 136 50 1~9`6 -- yellow lite\n"
            + " 0 255 25 162 50 1~10`7 -- red lite\n"
            + " 0 64 23 55^132 25~11 -- pedestrian standing left\n"
            + " 0 64 29 52^66 25~12 -- pedestrian stepping left\n"
            + " 0 64 23 1^132 25~13 -- pedestrian standing right\n"
            + " 0 64 29 25^132 25~14 -- pedestrian stepping right\n"
            + " 0 83 99 412^172 32~15 -- white painted STOP line";
    private static final String MapMacros
            = "ZE 0x1000F0 -- right corner, N->E"
            + "\nZS 0x100001 -- right corner, E->S"
            + "\nZW 0x100010 -- right corner, S->W"
            + "\nZN 0x10000F -- right corner, W->N"
            + "\nZW 0x1000F0 -- left corner, N->W"
            + "\nZN 0x100001 -- left corner, E->N"
            + "\nZE 0x100010 -- left corner, S->E"
            + "\nZS 0x10000F -- left corner, W->S"
            + "\nZNEE 0xF3E1D1 -- inside curve, N->E"
            + "\nZESS 0x311213 -- inside curve, E->S"
            + "\nZSWW 0x1D2F3F -- inside curve, S->W"
            + "\nZWNN 0xDFFEFD -- inside curve, W->N"
            + "\nZNWW 0xFCCDCF -- outside curve, N->W"
            + "\nZENN 0xC1D4F4 -- outside curve, E->N"
            + "\nZSEE 0x144341 -- outside curve, S->E"
            + "\nZWSS 0x4F3C1C -- outside curve, W->S";
    private static int SceneTall = 0, SceneWide = 0, SceneBayer = 0,
            NuData = 0, Waiting = 0, ShoSteer = 0, SteerWhee = 0, GasBrake = 0,
            SpecGas = 0, SpecWheel = 0, CarTest = 0, tRadix = 0, t2Radix = 0,
            nClients = 0, ZoomFocus = 0, GroundsColors = 0, LookFrame = 0,
            ZooMapTopL = 0, ZooMapDim = 0, ZooMapShf = 0, ZooMapBase = 0,
            Wally = 0, Darken = 0, MapIxBase = 0, MapHy = 0, MapWy = 0,
            ImageWide = 0, nuIxBase = ArtBase + 4, FrameNo = 0, // incr'd each rebuild
            NextFrUpdate = 0, ProcessingTime = 0, OpMode = 0, DroppedFrame = 0,
            FakeTimeBase = 0, RealTimeBase = 0, // time base for artifact animations
            FakeRealTime = 0, RealTimeNow = 0, TimeBaseSeq = 0, PixScale = 0,
            nCrumbs = 0, NumFax = 0, RectMap = 0, // RecenLnLoc = 0, RecenLnPtr = 0,
            TopCloseView = 0, SeePaintTopL = 0, SeePaintSize = 0, SeePaintImgP = 0,
            TmpI = 0, TripLine = 0, SameData = 0, PavColo = 0x666666,
            PavDk = 0x333333, GrasColo = 0x00FF00, GrasDk = 0x009900;
    private static double SloMotion = 0.0, Velocity = 0.0, VuEdge = 0.0,
            effTurnRad = TurnRadius, // effective = nearest we can see
            AverageAim = 0.0, WhitLnSz = WhiteLnWi * 1.4, Facing, Vposn, Hposn,
            Vcarx, Hcarx, Lcarx, Rcarx, Tcarx, Bcarx, // used to find car position
            fZoom, Dzoom, fSpeed, fSteer, FltWi, fImHaf, fMapWi, WiZoom,
            ZooMapScale, ZooMapWhLn, Vsee, Hsee, Voffm, Hoffm;
    private static boolean StepOne = false, SimBusy = false, Moved = false,
            DarkOnce = false, ClockTimed = false, unScaleStee = false,
            InWalls = false, SimInTrak = DriverCons.D_StayInTrack,
            ShoTrkTstPts = DriverCons.D_ShoTrkTstPts,
            SimSpedFixt = DriverCons.D_FixedSpeed;
    private static String TempStr;
    private static int[] RangeRow = null; // screen row ix'd by dx in 2m steps
    private static int[] RowRange = null; // dx in m/16 (6cm units) ix'd by row
    private static int[] MapIndex = null;
    private static int[] theFax = null;
    private static int[] ActivAnima = null;
    private static int[] TrakImages = null;
    private static int[] myScreen = null;
    private static int[] PrioRaster = null;
    private static int[] BreadCrumbs = null;
    private static int[] GridLocTable = null;
    private static double[] RasterMap = null;
    final int[] TinyBits = {//    *       *     * *     * *         *   * * *     * *   * * *
            0x25552, 0x22227,  //  *   *     *         *       *   *   *   *       *           *
            0x61247, 0x61216,  //  *   *     *       *       *     *   *   * *     * *       *
            0x15571, 0x74616,  //  *   *     *     *           *   * * *       *   *   *   *
            0x34652, 0x71244,  //    *     * * *   * * *   * *         *   * *       *     *
            0x25252, 0x25316,  //    *       *     *       *  * * *     * *  *       *  0x11244 *
            0x49D9B9, 0x74647,  //  *   *   *   *   * *     *  *       *      *       *          *
            //    *       * *   *   *   *  * *       *    *   *   *        *
            0x34216, 0x699996,  //  *   *       *   *     * *  *           *  *   *   *      *
            2, 0x00700};  //    *     * *     *       *  * * *   * *      *   *    *   *
    // + " 60     55 78 274^172 36~16=16 -- BlueCar front\n"
    // + " 32+45  54 157 190^2  36~17=16 -- BlueCar right-front\n"
    // + " 60+90  54 155 352^114 36~18=16 -- BlueCar right\n"
    // + " 32+135 54 157 352^58 36~19=16 -- BlueCar right-back\n"
    // + " 60+180 55 78 190^172 36~20=16 -- BlueCar back\n"
    // + " 32+225 54 157 190^58 36~21=16 -- BlueCar left-back\n"
    // + " 60+270 54 155 190^114 36~22=16 -- BlueCar left\n"
    // + " 0      54 157 352^2  36~23=16 -- BlueCar left-front";
    private final int[] Grid_Locns = {6, 12, 16, 18, 26, 29, // (GridLocT) block index
            0, 16, ImHaf - 40, ImHaf - 2, ImHi - DrawDash, ImHi,  // vert div'ns
            0, 20, ImWi - 20, ImWi, 0, ImWi,  // top horz divs, (no gas)
            0, ImWi / 7, ImWi * 2 / 7, ImWi * 3 / 7, ImWi * 4 / 7, ImWi * 5 / 7, ImWi * 6 / 7, ImWi, // mid
            0, 20, ImWi, // ImWi/4, ImWi/2, ImWi*3/4, ImWi, // no gas posns (obsolete)
            0, 20, ImMid, ImWi - 20, ImWi};                   // bottom horz div'ns
    private final int[] TapedWheel = {-2 * 65536 + 0 * 256 + 12,         // 0..0,
            2 * 65536 + 1 * 256 + 17, 5 * 65536 + 3 * 256 + 22, 9 * 65536 + 6 * 256 + 28,       // 1..3,
            12 * 65536 + 10 * 256 + 34, 15 * 65536 + 15 * 256 + 41, 18 * 65536 + 22 * 256 + 48, // 4..6,
            21 * 65536 + 31 * 256 + 56, 23 * 65536 + 42 * 256 + 64, 25 * 65536 + 54 * 256 + 73, // 7..9,
            28 * 65536 + 67 * 256 + 83, 33 * 65536 + 81 * 256 + 92,                   // 10..11
            0x1FE000, 0x1FE000, 0x1FE000, 0x1FE000, 0,            // 0 @12/-2
            0x1FE000, 0x1FE000, 0x1FE000, 0x1F8000, 0,            // 1 @17/+2
            0x6000, 0x1FF000, 0xFF000, 0xFF000, 0xF0000, 0,        // 2 @22/+5
            0xF000, 0xFF800, 0x7F800, 0x7E000, 0x70000, 0,         // 3 @28/+9
            0x1800, 0xFC00, 0x7FC00, 0x3F800, 0x3E000, 0x18000, 0,  // 4 @34/12
            0x600, 0x1E00, 0x7F00, 0x1FC00, 0xF800, 0xE000, 0,      // 5 @41/15
            0x300, 0x780, 0x1F80, 0x7F00, 0x7C00, 0x3800, 0x2000, 0, // 6 @48/18
            0xC0, 0x3E0, 0x7C0, 0x1F80, 0x1F00, 0x0E00, 0x400, 0,    // 7 @56/21
            0x30, 0x78, 0xF8, 0x1F0, 0x3E0, 0x7C0, 0x380, 0x100, 0,   // 8 @64/23
            0x4, 0xE, 0x1F, 0x3E, 0x7C, 0xF8, 0xF0, 0x60, 0x20, 0,     // 9 @73/25
            0x1, 0x3, 0x7, 0xF, 0x1F, 0x1E, 0xC, 0x4, 0,             // 10 @83/28
            1, 3, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // lsb: scrn botm  // 11 @92/33;  [12 @97/38]
    private int Left_X, Rite_X, PreViewLoc, PreViewAim;
    private SimHookBase SerialCalls = null;

    public TrakSim() {
        StartPatty("TrakSimCons");
    }

    private static double Cast_I2F(int whom) {
        // convert int rep'n of fix-point -> float..
        return MyMath.Fix2flt(whom, 12);
    } //~Cast_I2F

    private static int Cast_F2I(double whom) { // cvt to int rep'n of fix-point..
        if (MyMath.fAbs(whom) >= 524288.0) return 0;
        return MyMath.Trunc8(whom * 4096.0);
    } //~Cast_F2I

    public static void LineSlope(double Vat, double Hat, double Vstp,
                                 double Hstp, boolean logy, String msg) { // result -> Voffm,Hoffm; logy=F
        // As if facing northward, V/Hat: S,E coordinates of any point along line
        //   path [M*V+H = K], and V/Hstp: cos,sin of c-wise angle frm north,
        //   so that Vat-Vstp,Hat+Hstp advances (one unit) in direction facing.
        // Voffm is multiplier M, Hoffm is constant K, in line equation along path
        //   where for any point (V,H), M*V+H<K if (V,H) is left of the line
        // Math.abs(Vstp)>Math.abs(Hstp); Use Vstp=0 to repeat previous V/Hstp
        if (Vstp == 0.0) {
            Vstp = Vsee;
            Hstp = Hsee;
        } //~if
        if (Vstp == 0.0) Voffm = Vstp;
        else Voffm = Hstp / Vstp;
        Hoffm = Voffm * Vat + Hat;
        Vsee = Vstp;
        Hsee = Hstp;
        if (Mini_Log) if (logy)
            System.out.println(HandyOps.Flt2Log("(LinSlope) " + msg, Hoffm,
                    HandyOps.Flt2Log(" *", Voffm, HandyOps.Flt2Log(" (", Vat,
                            HandyOps.Flt2Log("/", Hat, HandyOps.Flt2Log(" ", Vstp,
                                    HandyOps.Flt2Log("/", Hstp, ")")))))));
    } //~LineSlope

    public static int EncoPavEdge(int tall, int wide, int recell, int why) {
        int eps, bitz = 0x80000000; // see bits in (BuildFram)
        // tall = HiWord(xcell)-HiWord(ocell); wide = LoWord(xcell-ocell);
        //  .. tall:wide is a proportion only (the line slope)
        // recell = xcell+ocell, // = 2*(center of line) = 25cm units
        //  .. any point on the line should work the same
        double Mconst, Kconst;     /** might could revise this to use LineSlope? **/
        boolean EW = false, grtr = false; // log only
        if ((MyMath.iAbs(tall) > MyMath.iAbs(wide)) || (why < 0)) { // more N-S than E-W,
            bitz = 0xA0000000;         // ..or else tagged diagonal (west in cell)
            if (tall < 0) bitz = bitz | 0x40000000; // going north, paved to west (<)
            else grtr = true; // otherwise going south, paved to east (>)
            Mconst = -MyMath.Fix2flt(wide, 0); // (coerced to float..)
            if (tall == wide) Mconst = -1.0;
            else if (tall + wide == 0) Mconst = 1.0;
            else if (wide != 0) Mconst = Mconst / tall; // (..so this is float divide)
            Kconst = Mconst * (recell >> 16) + MyMath.SgnExt(recell);
        } //~if // in 25cm units
        else { // edge line more E-W than N-S..       // ..(must decode meters)
            EW = true; // EW: (bitz&0x20000000 ==0)
            if (wide > 0) bitz = bitz | 0x40000000; // going east, paved to north (<)
            else grtr = true; // otherwise going west, paved to south (>)
            Mconst = -MyMath.Fix2flt(tall, 0);
            if (tall == wide) Mconst = -1.0;
            else if (tall + wide == 0) Mconst = 1.0;
            else if (tall != 0) Mconst = Mconst / wide;
            Kconst = Mconst * MyMath.SgnExt(recell) + (recell >> 16);
        } //~else
        eps = Cast_F2I(Mconst);
        if (eps >= 0x1000) Mconst = Cast_I2F(0xFFF);
        else if (why != 0) if (eps + 1 == 0) Mconst = Cast_I2F(eps - 1);
        Kconst = Kconst + MyMath.Fix2flt(32, 12);
        // RatLib floats are 1-bit sign, 19-bit integer, 12-bit fraction.. ¥¥¥
        //   masking Mconst&0x1FFF gets only signed fraction (&0x1000 is sign)
        //   Kconst has worst case range (45¡ @ bot corner = 2*(-399..+910))
        //     for 1-bit sign + 11-bit int + [31-2-13-12 =] 4-bit fract;
        //     implicit /4 then +5 shift here puts decode bin.pt in bit 19.
        bitz = ((Cast_F2I(Kconst) & 0xFFFFC0) << 5) // must decode(/4) into meters
                + ((Cast_F2I(Mconst) & 0x1FFF) >> 2) + bitz;           // || Mini_Log
        if (Log_Draw || NoisyMap) if (SameData != bitz) {
            SameData = bitz;
            System.out.println(HandyOps.Hex2Log("(EPE) = x", bitz, 8,
                    HandyOps.Flt2Log(HandyOps.IffyStr(EW, " EW Kc=", " NS Kc="), Kconst,
                            HandyOps.Flt2Log(" Mc=", Mconst, HandyOps.Dec2Log(" ", tall,
                                    HandyOps.Dec2Log("/", wide, HandyOps.Int2Log(HandyOps.IffyStr(grtr,
                                            " > ", " < "), recell, HandyOps.Dec2Log(" ", why, ""))))))));
        } //~if
        return bitz;
    } //~EncoPavEdge

    /**
     * Converts contiguous white RGB pixels into transparent for BuildMap.
     * For best results, images should be semi-convex, where all outside
     * edges are visible from its containing rectangle (serpentine regions
     * take longer).
     *
     * @param tall    The image height, in pixel rows.
     * @param wide    The image width, in pixels.
     * @param theImgs An array of RGB pixels containing artifact images.
     */
    public static void WhiteAlfa(int tall, int wide, int[] theImgs) {
        final int Transprnt = DriverCons.D_Transprnt;
        int info, rx, cx, uppy = 0, here = 0, thar = -wide, tops = tall * wide;
        boolean seen = true;
        if (theImgs == null) return;
        if (tall < 4) return; // not a credible image array
        if (wide < 4) return;
        if (Mini_Log) if (NoisyMap) System.out.println(HandyOps.Dec2Log("  <WhitAlf> ",
                tall, HandyOps.Dec2Log("/", wide, HandyOps.ArrayDumpLine(
                        theImgs,
                        20, 11))));
        for (rx = tall - 1; rx >= 0; rx += -1)
            for (cx = wide - 1; cx >= 0; cx += -1) { // top-to-bottom..
                if (here >= theImgs.length) break;
                info = theImgs[here];
                if (info >= 0xFFFFFF) {
                    info = info & 0xFFFFFF;
                    if (seen) info = -1;
                    else if (thar < 0) info = -1;
                    else if (thar < theImgs.length) if (theImgs[thar] < 0) info = -1;
                    if (info < 0) uppy++;
                } //~if
                theImgs[here] = info;
                if (cx == 0) seen = true;
                else seen = info < 0;
                here++;
                thar++;
            } //~for
        while (uppy != 0) {
            seen = true;
            here = tops;
            uppy = 0;
            thar = tops + wide;
            for (rx = tall - 1; rx >= 0; rx += -1)
                for (cx = wide - 1; cx >= 0; cx += -1) { // then bottom-up..
                    here--;
                    if (here >= theImgs.length) break;
                    info = theImgs[here];
                    thar--;
                    if (info != Transprnt) { // magical interior transparent =0xFEFEFE
                        if (info < 0xFFFFFF) {
                            seen = info < 0;
                            continue;
                        } //~if
                        if (seen) info = -1;
                        else if (thar < 0) info = -1;
                        else if (thar >= theImgs.length) info = -1;
                        else if (theImgs[thar] < 0) info = -1;
                    } //~if
                    else info = -1;
                    if (info < 0) {
                        theImgs[here] = info;
                        seen = true;
                        uppy--;
                    } //~if
                    else if (cx == 0) seen = true;
                    else seen = false;
                } //~for
            if (uppy == 0) break;
            here = -1;
            seen = true;
            thar = -1 - wide;
            uppy = 0;
            for (rx = tall - 1; rx >= 0; rx += -1)
                for (cx = wide - 1; cx >= 0; cx += -1) { // then top-down again..
                    here++;
                    if (here >= theImgs.length) break;
                    info = theImgs[here];
                    thar++;
                    if (info < 0xFFFFFF) {
                        seen = info < 0;
                        continue;
                    } //~if
                    if (seen) info = -1;
                    else if (thar < 0) info = -1;
                    else if (thar >= theImgs.length) info = -1;
                    else if (theImgs[thar] < 0) info = -1;
                    if (info < 0) {
                        theImgs[here] = info;
                        seen = true;
                        uppy++;
                    } //~if
                    else if (cx == 0) seen = true;
                    else seen = false;
                }
        } //~while               // Mini_Log=F..
        if (Mini_Log) if (NoisyMap) System.out.println(HandyOps.Dec2Log("  (WhitAlf) ",
                tall, HandyOps.Dec2Log("/", wide, HandyOps.ArrayDumpLine(
                        theImgs,
                        20, 11))));
    } //~WhiteAlfa

    private static int GotImgOps(String theList) {
        int res = HandyOps.NthOffset(0, "\nU", theList); // >0: track info has paint
        if (res < 0) if (HandyOps.NthOffset(0, "\nY", theList) < 0) // <0: other images
            if (HandyOps.NthOffset(0, "\nJ", theList) < 0)          // <0: other images
                if (HandyOps.NthOffset(0, "\n@", theList) < 0)
                    if (HandyOps.NthOffset(0, "\nO", theList) < 0) res = 0;
        if (Mini_Log) if (NoisyMap)
            System.out.println(HandyOps.Dec2Log("  (GotImOp) ", res, ""));
        return res;
    } //~GotImgOps

    private static int Ad2PntMap(int rx, int cx, int img,
                                 int tall, int wide, int PaintIx, int[] myMap, int size) { // => RectMap
        int nx, yx, zx, abit, info = (rx << 16) + cx, more = (tall << 16) + wide - 0x10001,
                whar = 0, locn = 0, here = 0, thar = 0, why = 0;   // no early exits
        String aLine = "";
        boolean flip = false, logy = false, hirez = (img & 0x04000000) != 0;
        double Vx, Hx, aim;
        while (true) {
            why++; // why = 1
            if (myMap == null) break;
            here = myMap[ArtBase + 2]; // ArtBase = 8, +3 for preface, needs +3 ditto
            why++; // why = 2
            if (here <= 0) break;
            if (here > myMap.length) break;
            why++; // why = 3
            if (PaintIx < 6) break;
            why++; // why = 4
            if (img == 0) break;
            why++; // why = 5
            here = here + 3; // cuz myMap includes 3-int header not there at runtime
            thar = here + 3 - PaintIx;
            if (thar < 8) break;
            if (thar > myMap.length - 4) break;
            why++; // why = 6
            if (myMap[thar + 2] != 0) break; // last error exit
            why++; // why = 7
            if (more == 0) more++; // 1x1 pix image allowed, but don't fail 0-test
            myMap[thar] = img;
            myMap[thar + 1] = info; // locn&dims both in 25cm units if lo-res,
            myMap[thar + 2] = more; // but if hi-res, dims only in 3cm units
            tall--; // convert to meters-1..
            wide--;
            if ((img & 0x04000000) != 0) { // hi-res..
                tall = tall >> 3;
                wide = wide >> 3;
            } //~if
            wide = ((cx & 3) + wide) >> 2;
            tall = tall >> 2;
            PaintIx = PaintIx - 3;
            cx = cx >> 2; // now in meters (8 bits)
            rx = rx >> 2; // now in meters
            zx = 1 << (31 - (cx & 31));
            locn = (rx << 3) + (cx >> 5) + here; // pack 32/int (3 bits remaining)
            for (yx = tall; yx >= 0; yx += -1) {
                whar = (yx << 3) + locn; // now includes +3 for hdr
                abit = zx;
                for (nx = wide; nx >= 0; nx += -1) {
                    if (whar >= here) if (whar < here + size)
                        myMap[whar] = myMap[whar] | abit;
                    if (abit < 0) abit = 0x40000000;
                    else if (abit < 2) {
                        abit = 0x80000000;
                        whar++;
                    } //~if
                    else abit = abit >> 1;
                }
            } //~for
            why = 0; // success!
            break;
        } //~while               // Mini_Log=F..
        if (Mini_Log) {
            System.out.println(HandyOps.Dec2Log("(AdPnt2Mp) ", locn,
                    HandyOps.Dec2Log(" +", tall, HandyOps.Dec2Log("/", wide,
                            HandyOps.Dec2Log(" -> ", PaintIx, HandyOps.Dec2Log(" @ ", thar,
                                    HandyOps.Dec2Log(" => ", img & 0xFFFFFF, HandyOps.Dec2Log("+", img >> 24,
                                            HandyOps.Int2Log("/", info, HandyOps.Int2Log("/", more,
                                                    HandyOps.Dec2Log(" @@ ", here,
                                                            HandyOps.Dec2Log(" = ", why, aLine))))))))))));
        } //~if   // why =
        return PaintIx;
    } //~Ad2PntMap

    private static void MapBucket(int deep, int rx, int cx,
                                  int colo, int[] myMap) {                                      // "(Bkt) '"
        int thar = rx * HalfMap + cx + nuIxBase, bitz = colo ^ 0x22222222, info = 0;
        boolean seen = false;
        while (true) {
            if (myMap == null) break;
            if (cx < 0) break;
            if (rx < 0) break;
            if (cx >= HalfMap) break;
            if (thar < 0) break;
            if (thar >= myMap.length) break;
            info = myMap[thar];
            if (info == colo) break;
            if (info != 0) if (info != bitz) break;
            seen = true;
            if (Log_Draw || NoisyMap) {
                if (TmpI > 72) {
                    TempStr = TempStr + "\n    ";
                    TmpI = 0;
                } //~if
                TempStr = TempStr + HandyOps.Dec2Log(" ", rx,
                        HandyOps.Dec2Log("/", cx, HandyOps.IffyStr(info == 0, "", "`")));
                TmpI = TmpI + 8;
            } //~if
            myMap[thar] = colo;
            if (cx <= 0) break;
            if (rx <= 0) break;
            if (thar <= 8) break;
            if (thar > myMap.length - 2) break;
            info = myMap[thar + 1];
            if (info != colo) if (info == bitz) info = 0;
            if (info == 0) MapBucket(deep + 1, rx, cx + 1, colo, myMap);
            info = myMap[thar - 1];
            if (info != colo) if (info == bitz) info = 0;
            if (info == 0) MapBucket(deep + 1, rx, cx - 1, colo, myMap);
            if (thar > myMap.length - (HalfMap + 2)) break;
            if (thar < HalfMap) break;
            info = myMap[thar + HalfMap];
            if (info != colo) if (info == bitz) info = 0;
            if (info == 0) MapBucket(deep + 1, rx + 1, cx, colo, myMap);
            info = myMap[thar - HalfMap];
            if (info != colo) if (info == bitz) info = 0;
            if (info != 0) break;
            thar = thar - HalfMap;
            rx--;
        } //~while
        if (Log_Draw || NoisyMap) if (deep == 0) if (!seen)
            TempStr = TempStr + HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log("/", cx,
                    HandyOps.Colo2Log(" = ", colo, HandyOps.Hex2Log(" / x", info, 8,
                            HandyOps.Dec2Log(" @ ", thar, "")))));
    } //~MapBucket

    /**
     * Builds a track map from a text description.
     *
     * @param theList The text description. See ReadMe for details
     * @param ImDims  The height & width of theImgs, packed as (H<<16)+W.
     * @param theImgs An array of RGB pixels containing artifact images.
     * @return The track map ready to write to a file
     */
    public static int[] BuildMap(String theList, int ImDims,
                                 int[] theImgs) { // used BildMap codes: @ABcdEFgHIjKLmNOPQRSTuVWXYZ
        final int GridSz = HalfTall * HalfMap;
        double deep, whar, Vstp, Hstp, Vinc, Hinc;            // see: case '@':
        boolean EW = (theImgs != null), frax = (theList != "");
        int lino = HandyOps.Countem("\n", theList), dont = -1, rpt, recell,
                tall, wide, rx, cx, kx = 0, yx = 0, zx = 0, nxt = 0, wait = 0,
                here = 0, thar = 0, colo = 0, ImgHi = 0, ImgWi = 0, ImSz = 0,
                nImgs = 0, sofar = 0, xcell = 0, AfterStop = 0, nCells = 0,
                PaintIx = 0, nerrs = 0, info = 0, prio = 0, prev = 0, aim = 0;
        double Vat = 0.0, Hat = 0.0;
        boolean fini = false;
        int[] DidCells = new int[256];
        int[] AnimInfo = null;
        // AnimInfo[5] = myMap[ArtBase(+3)] -> myMap[1stLst] -> 1st obj & posn
        // AnimInfo[6]..[15] = myMap[ArtBase(+3)+1].. -> myMap[2ndLst]..
        // all non-anim8 obj's come next, then 00, then myMap[1stLst]
        // myMap[1stLst+0] -> 1st obj, 1stPosn & time
        // myMap[1stLst+1] -> 1st obj, 2stPosn & time = 1stPosn+5
        String aStr = "", xStr = "\n.StopInfo\n", theText = StopInfo,
                aLine = HandyOps.NthItemOf(true, 1, theList),
                aWord = HandyOps.NthItemOf(false, 5, aLine);
        char xCh = HandyOps.CharAt(0, aWord);
        int[] myMap = null;
        if (theImgs != null) nxt = theImgs.length;
        if (NoisyMap) System.out.println(HandyOps.Dec2Log("++(BldMap) ", lino,
                HandyOps.Int2Log(" ", ImDims, HandyOps.Dec2Log(" ", (nxt + 999) >> 10,
                        HandyOps.TF2Log("K ", frax, " '" + aLine + "'")))));
        if (!frax) return null; // if (theList == "")
        if (lino < 3) return null; // too few lines to be real
        theList = HandyOps.ReplacAll("", "\r", theList); // cuz Windoze leaves them in
        if (xCh >= '0') if (xCh <= '9')
            info = HandyOps.SafeParseInt(aWord) << 16; // line width in cm
        nuIxBase = ArtBase + 3; // 8+3 -> 11
        if (nxt > 0) yx = GotImgOps(theList); // >0: has paint, <0: other ims
        if (yx != 0) while (true) { // we have managed art or paint..
            kx = HandyOps.NthOffset(0, xStr, theList); // xStr = "\n.StopInfo\n"
            xStr = "";
            if (yx > 0) {
                xcell = GridSz >> 3; // sb =6400, the map size
                PaintIx = 3;
            } //~if // init'ly just the terminator
            yx = HandyOps.NthOffset(0, "\n. .", theList);
            if (yx > 0) theList = HandyOps.Substring(0, yx + 2, theList);
            if (kx > 0) if (kx < yx) {
                xStr = HandyOps.RestOf(kx + 11, theList);
                for (lino = 1; lino <= 99; lino++) {
                    aStr = HandyOps.NthItemOf(true, lino, xStr);
                    if (aStr == "") break;
                    kx = HandyOps.NthOffset(0, "~", aStr);
                    if (kx < 0) continue;
                    zx = ~zx; // = -zx-1
                    if (kx < 10) break;
                    aStr = HandyOps.Substring(kx + 1, 4, aStr);
                    if (HandyOps.SafeParseInt(aStr) != lino) break;
                    zx = -zx;
                } //~for // net effect: zx++
                if (zx < 8) {
                    System.out.println(HandyOps.Dec2Log("** Invalid StopInfo ", zx,
                            HandyOps.Dec2Log("/", kx, HandyOps.Dec2Log(" ", lino, " <??> = '"
                                    + aStr + "'\n  .... '" + xStr + "' ...."))));
                    nerrs++;
                } //~if
                else theText = xStr;
            } //~if
            AfterStop = HandyOps.Countem("\n", theText) + 2;
            theText = theText + "\n\n\n";
            for (lino = 0; lino <= 32; lino++) DidCells[lino] = 0;
            for (lino = 2; lino <= 999; lino++) {
                xStr = HandyOps.NthItemOf(true, lino, theList);
                xCh = HandyOps.CharAt(0, xStr);
                if (xCh == '.') break;
                if (xCh == '@') {
                    zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 4, xStr));
                    if (zx > 4) if (zx < 16) {
                        DidCells[zx]++;
                        zx = 1 << zx;
                        if ((sofar & zx) == 0) sofar = sofar + zx + 1;
                        theText = theText + "\n" + xStr;
                        nCells++;
                    }
                } //~if
                else if (xCh == 'J') {
                    zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 4, xStr));
                    yx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, xStr));
                    if (yx > 0) if (zx > 4) if (zx < 16) {
                        DidCells[zx] = DidCells[zx] + yx;
                        zx = 1 << zx;
                        if ((sofar & zx) == 0) sofar = sofar + zx + 1;
                        theText = theText + "\n" + xStr;
                        nCells = nCells + yx;
                    }
                } //~if
                else if (xCh == 'Y') {
                    zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 4, xStr));
                    if (zx > 15) { // OK if it finds too many..
                        yx = HandyOps.Countem(HandyOps.Dec2Log("=", zx, " "), theList);
                        nImgs = nImgs + yx;
                    } //~if
                    else if (zx == 0) nImgs = nImgs + 6;
                    else if (zx > 1) if (zx < 4) nImgs = nImgs + 8;
                } //~if
                else if (xCh == 'O') nImgs++;
                else if (xCh == 'T') nImgs++;
                else if (xCh == 'K') // "\nK_ " // shouldn't be here, delete it..
                    theList = HandyOps.RepNthLine("--" + xStr, lino, theList);
                else if (xCh == 'U') if (PaintIx > 0) PaintIx = PaintIx + 3;
            } //~for
            rpt = sofar & 15; // + sequences
            sofar = nuIxBase; // = ArtBase+3 -> 11
            yx = nImgs * 4 + rpt + ArtBase + 1; // = offset to 1st sequence (excl hdr)
            aim = (((rpt * 256) + nCells) * 256 + yx) * 256 + nImgs; // (for log)
            if (nCells > 0) {
                AnimInfo = new int[16];
                // AnimInfo[5] = myMap[ArtBase(+3)] -> myMap[1stLst] -> 1st obj & posn
                // AnimInfo[6]..[15] = myMap[ArtBase(+3)+1].. -> myMap[2ndLst]..
                // all non-anim8 obj's come next, then 00, then myMap[1stLst]
                // myMap[1stLst+0] -> 1st obj, 1stPosn & time
                // myMap[1stLst+1] -> 1st obj, 2stPosn & time = 1stPosn+5
                for (lino = 5; lino <= 15; lino++) {
                    kx = DidCells[lino]; // how many steps in this object's journey
                    if (kx == 0) continue; // this sequence is unused
                    zx = (lino << 28) + yx; // = this object's anchor item
                    DidCells[lino] = zx; // -> start of this object's sequence
                    AnimInfo[lino] = yx + 3; // ditto, but including 3-int hdr
                    DidCells[lino + 16] = kx; // for log
                    yx = yx + kx + 1;
                } //~for // ordinary image specs follow last anchor item
                theText = HandyOps.RepNthLine(HandyOps.Dec2Log("\nK_ ", yx + 3,
                        HandyOps.Dec2Log(" ", nCells, HandyOps.Dec2Log(" ", rpt, ""))),
                        AfterStop, theText);
            } //~if
            nImgs = nCells * 4 + yx; // = total (image) index space
            DidCells[16] = -rpt; // visually marks middle
            aStr = "\n  ==" + HandyOps.ArrayDumpLine(DidCells, 32, 4)
                    + " (Imgs)\n  _:_" + HandyOps.ArrayDumpLine(AnimInfo, 0, 5)
                    + "\n   -- '";
            tall = ImDims >> 16;
            wide = MyMath.SgnExt(ImDims);
            ImSz = tall * wide; // should be = nxt
            if (ImDims < 0x100000) ImDims = -1;
                // else if (tall<16) ImDims = 0;
            else if (wide < 16) ImDims = -2;
            else if (wide > 0xFFF) ImDims = -3;
            else if (tall > 0xFFF) ImDims = -4;
            else if (ImSz > nxt) ImDims = -5;
            else if (nImgs <= 0) ImDims = -6;
            else if (nImgs > 9999) ImDims = -6;
            else thar = nImgs + nuIxBase + GridSz + 2; // -> images
            if (thar > 0) {        // nuIxBase = ArtBase+3;
                if (PaintIx > 0) thar = xcell + PaintIx + thar; // xcell = GridSz>>3;
                here = thar + nxt + 2; // nxt = theImgs.length;
                myMap = new int[here];
                here = here - 2;
            } //~if
            if (myMap != null) nuIxBase = nuIxBase + nImgs; // nImgs = img index size
            else if (ImDims >= 0) ImDims = -8;
            if (!Mini_Log || !NoisyMap) {
                xStr = HandyOps.NthItemOf(true, 1, theText);
                if (xStr.length() > 77) xStr = aStr + HandyOps.Substring(0, 72, xStr)
                        + "..'";
                else xStr = aStr + xStr + "'";
            } //~if
            else xStr = aStr + theText + "' ----";
            wait = nuIxBase + GridSz;
            //
            // myMap contents & where (tag) in log to see it..
            // =-= from 0 to ArtBase-1 are global parameters (ArtBase=8)
            // =-= from ArtBase to nuIxBase-1 is the (3-part) index to artifacts
            // from nuIxBase to nuIxBase+GridSz-1 is the grid map (GridSz=12800)
            // >>==>> from wait=nuIxBase+GridSz to wait+PaintIx-1 is paint index
            // from wait+PaintIx to thar=wait+PaintIx+GridSz/8-1 is paint map
            // from thar=wait+PaintIx+GridSz/8 on is copy of theImgs
            //  .. (which is ignored unless you write it to a file)
            //
            System.out.println(HandyOps.Int2Log("  (Images) ", ImDims,        // {BuildMap}
                    HandyOps.Dec2Log(" #", nImgs, HandyOps.Dec2Log(" +", sofar,
                            HandyOps.Dec2Log(": ", tall, HandyOps.Dec2Log("/", wide,
                                    HandyOps.Dec2Log(" ", here, HandyOps.Dec2Log("/", thar,
                                            HandyOps.Dec2Log(" [", nuIxBase, HandyOps.Dec2Log(" ", wait,
                                                    HandyOps.Dec2Log(" ", PaintIx, HandyOps.Dec2Log("] ", nxt,
                                                            HandyOps.Int2Log(" ", aim, xStr)))))))))))));
            xStr = "";                       // HandyOps.IffyStr(ImDims <= 0,xStr,
            ImgWi = wide;
            ImgHi = tall;
            wait = nuIxBase + GridSz + PaintIx + 2; // (includes hdr)
            yx = ImSz;
            if (ImDims > 0) { // here = myMap.length-2 = thar+theImgs.length
                for (lino = here + 1; lino >= 0; lino += -1) { // copy/reformat pixels & clear index..
                    colo = 0;
                    if (lino >= thar) if (lino < here) {
                        yx--;
                        if (yx >= 0) if (yx < theImgs.length) {
                            colo = theImgs[yx];
                            if (colo < 0) colo = -1; // transparent
                            else if (colo < 0x01000000) colo = colo << 8; // (Tiff file format)
                            else colo = -1;
                        }
                    } //~if
                    myMap[lino] = colo;
                } //~for // (copy/reformat pixels)
                for (lino = 5; lino <= 15; lino++) { // copy anchors to index..
                    zx = DidCells[lino];
                    if (zx == 0) continue;
                    if (sofar > 0) if (sofar < myMap.length) myMap[sofar] = zx;
                    sofar++;
                } //~for // (copy anchors to index)
                xStr = " (" + yx + HandyOps.IffyStr(yx > 0, "?)", ")");
            } //~if // (ImDims>0)
            else { // no (or in-) valid images..
                nuIxBase = ArtBase + 4;
                sofar = nuIxBase;
                theImgs = null;
                myMap = null;
                PaintIx = 0;
                ImDims = 0;
                ImgWi = 0;
                nImgs = 0;
                ImSz = 0;
            } //~else
            break;
        } //~while // (we may have managed art)
        else xStr = "";
        for (lino = 0; lino <= 32; lino++) DidCells[lino] = 0; // done with this
        nCells = 0;
        if (myMap == null) myMap = new int[nuIxBase + GridSz + 4];
        if (myMap == null) return null;
        nuIxBase++;
        here = nuIxBase;
        if (here > 0) if (here < myMap.length - 1) {
            myMap[here - 1] = 0; // end of artifact list
            if (NoisyMap) myMap[here] = 0xDEADBEEF;
        } //~if // visual start of map in log
        if (myMap.length < 16) return null; // last early exit
        recell = HandyOps.SafeParseInt(aLine); // park dimensions in scaled meters
        colo = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 2, aLine)); // GroundsColors
        here = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 3, aLine)); // start posn
        aim = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 4, aLine)) + info; // & aim
        if (PaintIx > 0) wait = nuIxBase - 3 + GridSz + PaintIx; // (not counting hdr)
        myMap[0] = 0x4C696C45;
        myMap[1] = nuIxBase + GridSz + PaintIx + xcell; // size of map + header + artifact list
        myMap[2] = thar; // file offset to artifact images
        myMap[0 + 3] = ImDims; // artifact image size
        myMap[1 + 3] = 0; // no textures
        myMap[2 + 3] = nuIxBase - 3; // offset to map => MapIxBase // nuIxBase=ArtBase+4
        myMap[3 + 3] = recell; // park dimensions in scaled meters
        myMap[4 + 3] = colo; // GroundsColors = 9ggg1ppp
        myMap[5 + 3] = here; // start posn of car
        myMap[6 + 3] = aim; // orientation + line width
        myMap[ArtBase + 2] = wait; // =0 if no lines, else offset to line map
        SameData = 0;
        if (GoodLog) if (NoisyMap) {
            xStr = "=-= (BldMap)" + xStr + HandyOps.ArrayDumpLine(myMap, 22, 22) + " ";
            if (PaintIx > 0) if (wait > PaintIx + 4) aim = wait - 4 - PaintIx;
            System.out.println(xStr);
        } //~if
        aim = 0;
        wait = 0;
        xcell = 0;
        recell = 0;
        xCh = '\0';
        aLine = "";
        for (lino = 2; lino <= 999; lino++) {
            if (Mini_Log) if (NoisyMap) if (xCh != '\0') // prev'ly unlogged lines..
                System.out.println(HandyOps.Dec2Log(" (Bx)     ", lino - 1,
                        " -> '" + aLine + "'"));
            colo = 0;
            here = 0;
            frax = false;
            aLine = HandyOps.NthItemOf(true, lino, theList);
            xCh = HandyOps.CharAt(0, aLine);
            if (xCh == '.') {
                try {
                    xStr = "";
                    if (!fini) {
                        zx = HandyOps.NthOffset(0, "\nK_ ", theText);
                        if (zx > 0) {
                            xStr = theText;
                            theText = HandyOps.Substring(0, zx, xStr);
                            xStr = HandyOps.RestOf(zx, xStr);
                            sofar = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 2, xStr));
                        } //~if
                        else fini = true;
                    } //~if
                    if (fini) {
                        aWord = "' (Imgs)\n  =-=";
                        for (zx = 5; zx <= 15; zx++) { // validate sequence lists..
                            yx = AnimInfo[zx] & 0xFFFF;
                            if (yx <= 0) continue;
                            if (yx < myMap.length) if (myMap[yx] != 0) continue;
                            aWord = HandyOps.Dec2Log("' ** Oops! ", zx, " <??> (Imgs)\n  --");
                            nerrs++;
                            break;
                        } //~for
                        aWord = aWord + HandyOps.ArrayDumpLine(myMap, nImgs + 7, 5)
                                + "\n  _:_" + HandyOps.ArrayDumpLine(AnimInfo, 0, 5);
                    } //~if
                    else if (Mini_Log && NoisyMap) {
                        zx = HandyOps.Countem("\n", xStr);
                        aWord = "'\n --" + HandyOps.ArrayDumpLine(myMap, 36, 4)
                                + "\n  _:_" + HandyOps.ArrayDumpLine(AnimInfo, 0, 5)
                                + HandyOps.Dec2Log(" -- next do (", zx, ").. {") + xStr + "}";
                    } //~if
                    else aWord = "'";
                    aWord = " '" + aLine + aWord;
                    System.out.println(HandyOps.Dec2Log(HandyOps.IffyStr(nImgs == 0, "(done) ",
                            "(done+Art) "), lino, HandyOps.TF2Log(" ", fini,
                            HandyOps.Dec2Log(" ", colo, HandyOps.Dec2Log(" ", WinWi, aWord)))));
                    if (fini) break;
                    // if (colo>0) {
                    theList = HandyOps.RepNthLine(aLine + xStr + "\n... (end)", lino, theList);
                    // theText = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
                    fini = true;
                    xCh = '\0'; // (already logged)
                } catch (Exception ex) {
                    here = -lino;
                    break;
                }
                continue;
            } //~if // (xCh == '.')
            Vat = 0.0;
            Hat = 0.0;
            aWord = HandyOps.NthItemOf(false, 2, aLine);
            if (HandyOps.NthOffset(0, ".", aWord) >= 0) frax = true;
            rx = HandyOps.SafeParseInt(aWord);
            Vat = HandyOps.SafeParseFlt(aWord);
            aWord = HandyOps.NthItemOf(false, 3, aLine);
            if (HandyOps.NthOffset(0, ".", aWord) >= 0) frax = true;
            cx = HandyOps.SafeParseInt(aWord);
            Hat = HandyOps.SafeParseFlt(aWord);
            aWord = "";
            nxt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 4, aLine));
            here = rx * HalfMap + cx + nuIxBase;
            recell = 0;
            // colo = 0;
            tall = 0;
            wide = 0;
            rpt = -1;
            kx = 0;
            switch (xCh) { // convert gentle curve specs to straight line segments..
                case 'L': // turn left (CCW, outside)
                case 'R': // turn right (C-wise, inside)
                    rpt--; // rpt = -2
                    if (prio == 0) break; // gotta have a previous endpoint
                    nxt = aim + 1;
                    if (xCh == 'L') nxt = nxt + 4;
                    if (rx > 0) nxt = nxt + 8;
                    aWord = HandyOps.NthItemOf(true, nxt, MapMacros);
                    theList = HandyOps.RepNthLine(aLine + "\n" + aWord, lino, theList);
                    rpt = -1;
                    break; // case 'L'/'R'
                case 'A': // advance straight in same direction..
                    rpt = -3;
                    if (prio == 0) break; // gotta have a previous endpoint
                    rpt--; // rpt = -4
                    if (rx <= 0) break;
                    xCh = HandyOps.CharAt(aim, "NESW");
                    nxt = rx;
                    rx = prio >> 19;
                    cx = MyMath.SgnExt(prio) >> 3;
                    rpt--; // rpt = -5
                    if (rx <= 0) break;
                    rpt--; // rpt = -6
                    if (cx <= 0) break;
                    info = 0x341C >> (aim << 2);
                    if ((info & 3) == 0) {
                        info = info >> 2;
                        rx = ((info & 1) - (info & 2)) * nxt + rx;
                    } //~if
                    else cx = ((info & 1) - (info & 2)) * nxt + cx;
                    aWord = "" + xCh + HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log(" ", cx,
                            HandyOps.Dec2Log("   -- advance +", nxt, "")));
                    theList = HandyOps.RepNthLine(aLine + "\n" + aWord, lino, theList);
                    rpt = -1;
                    break; // case 'A'
                case 'Z': // insert segments for standard curve..             // doing 'Z'
                    rpt = -8;
                    if (prio == 0) break; // gotta have a previous endpoint
                    rpt--; // rpt = -9
                    if (rx <= 0x010101) break; // gotta have a defined trajectory
                    rpt--; // rpt = -10
                    if (rx >= 0xFFFFFF) break;
                    info = rx;
                    rx = prio >> 19;
                    cx = MyMath.SgnExt(prio) >> 3;
                    rpt--; // rpt = -11
                    if (rx <= 0) break;
                    rpt--; // rpt = -12
                    if (cx <= 0) break;
                    aStr = aLine;
                    for (nxt = 1; nxt <= 3; nxt++) {
                        xCh = HandyOps.CharAt(nxt, aLine);
                        if (Log_Log) if (Mini_Log) if (NoisyMap)
                            System.out.println(HandyOps.Dec2Log("    ... '" + xCh + "' ", rx,
                                    HandyOps.Dec2Log("/", cx, HandyOps.Hex2Log(" ", info, 6, ""))));
                        if (xCh < 'E') break;
                        if (xCh > 'W') break;
                        tall = info >> 4;
                        tall = (tall & 7) - (tall & 8);
                        wide = (info & 7) - (info & 8);
                        if ((tall | wide) == 0) break; // normal exit for corners
                        rx = rx + tall;
                        cx = cx + wide;
                        if (rx <= 0) break;
                        if (cx <= 0) break;
                        if (rx >= HalfTall) break;
                        if (cx >= HalfMap) break;
                        aWord = "" + xCh + HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log(" ", cx,
                                HandyOps.Dec2Log(HandyOps.IffyStr(tall < 0, "  (", "  (+"), tall,
                                        HandyOps.Dec2Log(HandyOps.IffyStr(wide < 0, ",", ",+"), wide, ")"))));
                        aStr = aStr + "\n" + aWord;
                        if (NoisyMap) System.out.println("  +++ '" + aWord + "'");
                        aWord = "";
                        info = info >> 8;
                    } //~for // (nx)
                    if (NoisyMap) System.out.println(HandyOps.Dec2Log("  [*] ", rx,
                            HandyOps.Dec2Log("/", cx, HandyOps.Int2Log(" (", prio,
                                    HandyOps.Substring(0, 66, HandyOps.ReplacAll("\\", "\n",
                                            HandyOps.RepNthLine(") '", 1, aStr))) + "'"))));
                    if (rx > 0) if (cx > 0) if (rx < HalfTall) if (cx < HalfMap)
                        theList = HandyOps.RepNthLine(aStr, lino, theList);
                    rpt = -1;
                    break; // case 'Z'
                case 'N': // northward right edge, r/c is end (end facing north)
                case 'S': // southward
                case 'E': // eastward
                case 'W': // westward         // Gentle Curve (*: use new direction)..
                    rpt = -14;                  // Inside:  0,0 +3+1 +1+1* +1+3* net +5+5
                    if (rx <= 0) break;         // Outside: 0,0 +4+1 +3+3* +1+4* net +8+8
                    rpt--; // rpt = -15
                    if (cx <= 0) break;
                    if (frax) { // allow for fractional position..
                        rx = MyMath.Trunc8(Vat * 8.0);
                        cx = MyMath.Trunc8(Hat * 8.0);
                    } //~if
                    else {
                        rx = rx << 3;
                        cx = cx << 3;
                    } //~else
                    thar = (rx << 16) | cx; // this run's endpoint..
                    rpt--; // rpt = -16
                    if (((prio ^ thar) & 0x3FF83FF8) == 0) break; // but not to same cell
                    if (false) {
                        zx = prio; // soon: my 1st step
                        yx = MyMath.SgnExt(zx);
                        if (yx < cx) zx = (zx | 7) + 1;
                        else if (yx > cx) zx = (zx & -8) - 1;
                        yx = zx >> 16;
                        if (yx < rx) zx = (zx | 0x70000) + 0x10000;
                        else if (yx > rx) zx = (zx & -0x80000) - 0x10000;
                    } //~if
                    EW = true;
                    aim = 3;
                    if (xCh == 'N') { // center-top of this cell..
                        if (frax) thar = thar & 0x3FF83FFF;
                        else thar = thar + 4;
                        EW = false;
                        aim = 0;
                    } //~if
                    else if (xCh == 'S') { // center-bottom..
                        if (frax) thar = thar | 0x70000;
                        else thar = thar + 0x70004;
                        EW = false;
                        aim = 2;
                    } //~if
                    else if (xCh == 'E') { // right
                        if (frax) thar = thar | 7;
                        else thar = thar + 0x40007;
                        aim = 1;
                    } //~if
                    else if (xCh == 'W') { // left (aim=3)
                        if (frax) thar = thar & 0x3FFF3FF8;
                        else thar = thar + 0x40000;
                    } //~if
                    if (prio == 0) { // if no previous endpoint, then this is start..
                        prio = thar;
                        dont = thar;
                        prev = 0;
                        rpt = -1; // not an error
                        break;
                    } //~if
                    if (false) if (!frax) {
                        kx = prio + 0x10001; // step ++ over into my 1st cell
                        zx = (0x40004000 - ((kx ^ prio) & 0x80008)) & 0x3FF83FF8; // mask, =0 if not
                        info = prio - 0x10001; // step -- back ditto
                        yx = (0x40004000 - ((info ^ prio) & 0x80008)) & 0x3FF83FF8;
                        recell = 0x3FF83FF8 & ~(zx | yx); // mask: not into me (frex, H when V)
                        zx = (recell >> 3) & prio | (yx >> 3) & info | zx & kx;
                    } //~if // = my 1st cell
                    rpt--; // rpt = -17
                    // if (zx==thar) break;
                    if (prio == thar) break; // end=bgn OK only if dif't facing edge
                    // recell = 0;
                    kx = prio >> 16;
                    info = MyMath.SgnExt(prio);
                    rpt--; // rpt = -18
                    if (info <= 0) break;
                    rpt--; // rpt = -19
                    if (kx <= 0) break;
                    rx = thar >> 16; // end cell, now at the correct edge..
                    cx = MyMath.SgnExt(thar);
                    tall = rx - kx;
                    wide = cx - info;
                    nxt = Math.abs(wide);
                    yx = Math.abs(tall);
                    EW = yx < nxt; // only used to determine end ro/co
                    yx = (yx + nxt) * 3; // max + cells to do
                    colo = EncoPavEdge(tall, wide, prio, 0); // use same line formula for all
                    Vat = MyMath.Fix2flt(tall, 0);
                    Hat = MyMath.Fix2flt(wide, 0);
                    whar = MyMath.aTan0(Hat, -Vat);       // aTan0(y=0,x=1)=0 <- ccw frm E
                    MyMath.Angle2cart(whar); // aim, C-wise from north
                    Hinc = MyMath.Sine * 2.0; // to advance in direction aimed, but <1 cell
                    Vinc = -MyMath.Cose * 2.0;
                    Vat = MyMath.Fix2flt(kx, 0); // 1st cell to do, in 25cm units (Gr/8)
                    Hat = MyMath.Fix2flt(info, 0); // (1st cell now = prio, skipped)
                    if (NoisyMap) {
                        System.out.println(HandyOps.Dec2Log("    (()) ", lino,
                                HandyOps.TF2Log(" -> ", EW, HandyOps.Dec2Log("/", yx,
                                        HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log("/", cx, // whar in degs..
                                                HandyOps.Dec2Log(" - ", kx, HandyOps.Dec2Log("/", info,
                                                        HandyOps.Dec2Log(" = ", tall, HandyOps.Dec2Log("/", wide,
                                                                HandyOps.Int2Log(" (", prio, HandyOps.Flt2Log(") ", whar,
                                                                        HandyOps.Flt2Log(" --> ", Vat, HandyOps.Flt2Log("/", Hat,
                                                                                HandyOps.Flt2Log(HandyOps.IffyStr(Vinc < 0, " ", " +"), Vinc,
                                                                                        HandyOps.Flt2Log("/", Hinc, HandyOps.TF2Log(" ", frax,
                                                                                                HandyOps.Int2Log("\n      __ (", thar, ") -- '" + aLine
                                                                                                        + "'"))))))))))))))))));
                        aLine = "";
                    } //~if
                    prio = thar;
                    // nCells = 0; // DidCells[0]
                    recell = 0;
                    nxt = 0; // <0 done, >0 on last row                       // (BuildMap)
                    for (zx = 0; zx <= yx; zx++) {
                        xcell = (((int) Math.round(Vat)) << 16) | ((int) Math.round(Hat)) & 0xFFFF;
                        Vat = Vat + Vinc;
                        Hat = Hat + Hinc;
                        if (((xcell ^ recell) & 0x3FF83FF8) == 0) continue;
                        here = (prio ^ recell) & 0x3FF83FF8; // =0 if now doing end cell
                        if (here == 0) nxt = -1; // do this cell then stop
                        else if (nxt > 0) break; // already did end cell in major dir'n
                        else if (EW) {
                            if ((here & 0xFFFF) == 0) nxt++;
                        } //~if // major dim'n hit..
                        else if ((here & -0x10000) == 0) nxt++; // ..so stop unless next matches
                        rpt = 0;
                        info = 0; // =0: no kitty-corners to do
                        if (wait == 0) if (prev != 0) if (recell != 0) {
                            cx = recell - (prev & -8); // both V&H must change +/-1..
                            rx = recell - (prev & -0x80000);
                            if ((rx & -0x80000) != 0) if ((cx & 0x3FF8) != 0) {
                                rpt = recell & -0x10000 | prev & 0xFFFF;
                                info = recell & 0xFFFF | prev & -0x10000;
                            } //~if
                            else if (EW) {
                                rx = recell & 0x70000;
                                if (rx > 0x40000) info = recell + 0x80000;
                                else if (rx < 0x40000) info = recell - 0x80000;
                            } //~if
                            else if ((recell & 4) == 0) info = recell - 8;
                            else if ((recell & 3) != 0) info = recell + 8;
                        } //~if
                        if ((recell != 0) || NoisyMap) for (kx = -1; kx <= 1; kx++) {
                            rx = MyMath.iMin(nCells - 1, 255);
                            if (((dont ^ recell) & 0x3FF83FF8) == 0)
                                recell = recell | 0x80000000; // don't do init cell
                            else if (recell > 0) for (rx = rx; rx >= 0; rx += -1) {
                                if (rx < 0) break;
                                if (rx > 255) continue;
                                if (((DidCells[rx] ^ recell) & 0x3FF83FF8) != 0) continue;
                                recell = recell | 0x80000000; // already did this cell
                                break;
                            } //~for // (rx)
                            rx = (recell >> 19) & 0x0FFF; // now in grid (2m) units, same as input
                            cx = MyMath.SgnExt(recell) >> 3;
                            thar = rx * HalfMap + cx + nuIxBase;
                            if (NoisyMap) if (lino < 240) System.out.println(HandyOps.Dec2Log(
                                    HandyOps.IffyStr(recell > 0, "    (++) ", "    (--) "), lino,
                                    HandyOps.Dec2Log("/", zx, HandyOps.Int2Log(" ", recell & 0x7FFFFFFF,
                                            HandyOps.Dec2Log(" -> ", rx, HandyOps.Dec2Log("/", cx,
                                                    HandyOps.Hex2Log(" = x", colo, 8, HandyOps.Dec2Log(" @ ", thar,
                                                            HandyOps.Int2Log(" ", here, HandyOps.Dec2Log("/", nxt,
                                                                    HandyOps.Dec2Log(" ", wait, HandyOps.Dec2Log(" ", kx,
                                                                            HandyOps.Dec2Log(" #", nCells, HandyOps.IffyStr(info == 0, "",
                                                                                    HandyOps.Int2Log(" ", info, HandyOps.IffyStr(rpt == 0, "",
                                                                                            HandyOps.Int2Log(" / ", rpt, "")))))))))))))))));
                            if (recell > 0) { // add this edge cell to map..
                                if (myMap != null) if (thar > 0) if (thar < myMap.length)
                                    myMap[thar] = colo;
                                DidCells[nCells & 255] = recell; // note that we did this cell..
                                nCells++;
                                prev = recell;
                                if (nxt < 0) break;
                            } //~if
                            if (kx == 0) recell = rpt;
                            else if (kx < 0) recell = info;
                            if ((recell & -0x10000) == 0) break;
                        } //~for // (kx)
                        recell = xcell; // this is 1st hit in this next cell
                        if (nxt < 0) break;
                    } //~for // (zx)
                    dont = -1; // test init cell only once
                    if (wait > 0) wait--;
                    xCh = '\0'; // (already logged)
                    continue; // case 'N'/'S'/'E'/'W' (already logged)
                case '_': // set TripLine (lots more logging on one pixel line)
                    TripLine = rx;                  // ..(immediate, not in file)
                    break;
                case '^': // set miscellaneous options (immediate, not in file)..
                    rpt = -100;
                    if (rx < 0) break;
                    if (rx > 9) break;
                    switch (rx) {
                        case 1: // toggle FixedSpeed..
                            SimSpedFixt = !SimSpedFixt;
                            break;
                        case 2: // toggle StayInTrack..
                            SimInTrak = !SimInTrak;
                            ShoTrkTstPts = SimInTrak;
                            break;
                        case 3: // turn both on(off)..
                            SimSpedFixt = !SimSpedFixt;
                            SimInTrak = !SimInTrak;
                            ShoTrkTstPts = SimInTrak;
                            break;
                        case 0:
                            break;
                    } //~switch
                    if (NoisyMap) {
                        aLine = " __ '" + aLine + "'";
                        System.out.println(HandyOps.Dec2Log("  (misc.opt) ", rx,
                                HandyOps.TF2Log(" Fx=", SimSpedFixt,
                                        HandyOps.TF2Log(" SiT=", SimInTrak,
                                                aLine))));
                        aLine = "";
                    } //~if
                    xCh = '\0'; // (already logged)
                    continue;
                case 'U': // painted (line) spec..
                    recell = rx;
                    yx = cx;
                    if (frax) { // allow for fractional position..
                        rx = MyMath.Trunc8(Vat * 8.0);
                        cx = MyMath.Trunc8(Hat * 8.0);
                    } //~if
                    else { // otherwise NW corner of cell..
                        rx = rx << 3;
                        cx = cx << 3;
                    } //~else
                    rpt = -83;
                    if (rx <= 0) break; // words 2&3 are V&H coords, in 2m grid units
                    rpt--; // rpt = -84
                    if (cx <= 0) break;
                    rpt--; // rpt = -85
                    if (nxt < 0) break; // word 4 is option bits..
                    if (nxt > 7) break; // currently only 90x rotation & hi-res
                    xStr = HandyOps.NthItemOf(false, 5, aLine);
                    wide = 0; // word 5 either points to index, or is image offset..
                    tall = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aLine));
                    if (tall > 0)
                        wide = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 7, aLine));
                    else tall = 0;
                    thar = HandyOps.SafeParseInt(xStr);
                    kx = HandyOps.NthOffset(0, "^", xStr);
                    if (kx <= 0) {
                        rpt--; // rpt = -86
                        kx = thar; // kx is logged
                        if (thar < 11) break;
                        if (thar > AfterStop) break;
                        xStr = HandyOps.NthItemOf(true, thar, theText);
                        aLine = aLine + "'\n  __ + '" + xStr;
                        zx = HandyOps.NthOffset(0, HandyOps.Dec2Log("~", thar, ""), xStr);
                        rpt--; // rpt = -87
                        if (zx < 0) break; // invalid line+ in index line
                        if ((tall == 0) || (wide == 0)) {
                            tall = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 2, xStr));
                            wide = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 3, xStr));
                            if ((nxt & 1) != 0) { // flip H/V..
                                zx = tall;
                                tall = wide;
                                wide = zx;
                            }
                        } //~if
                        xStr = HandyOps.NthItemOf(false, 4, xStr);
                        thar = HandyOps.SafeParseInt(xStr);
                        kx = HandyOps.NthOffset(0, "^", xStr);
                    } //~if
                    rpt = -88;
                    if (tall <= 0) break;
                    if (wide <= 0) break;
                    rpt--; // rpt = -89
                    if (thar <= 0) break;
                    thar = (nxt << 24) + thar;
                    nxt = (nxt << 16) + kx; // nxt is logged
                    if (kx > 0) {
                        kx = HandyOps.SafeParseInt(HandyOps.RestOf(kx + 1, xStr));
                        if (kx > 0) thar = kx * ImgWi + thar;
                    } //~if
                    rpt--; // rpt = -90
                    if (PaintIx < 6) break;
                    zx = PaintIx;
                    rpt--; // rpt = -91
                    if (yx < HalfMap) {
                        PaintIx = Ad2PntMap(rx, cx, thar, tall, wide, PaintIx, myMap, GridSz >> 3);
                        rpt--; // rpt = -92
                        if (PaintIx == zx) break;
                    } //~if // something went wrong in Ad2LnMp
                    // else if (yx+wide>WinWi) break; // rpt = -91: Image Viewer off-screen
                    // else if (recell+tall>ImHi) break; // off-image..
                    else if ((tall - 1) * ImgWi + thar > ImSz) break; // also checks optn=0 (+wide)
                    else if (SeePaintTopL == 0) {
                        cx = yx;
                        rx = recell;
                        SeePaintTopL = (recell << 16) + yx;
                        SeePaintSize = (tall << 16) + wide;
                        SeePaintImgP = thar;
                    } //~if
                    else break; // rpt = -91: ignore more than one (only accept first)
                    rpt = -1;
                    aWord = "";
                    break; // case 'U'
                case 'T': // start up RealTime Base..
                    rpt = -20;
                    if (ImDims == 0) break;
                    rpt--; // rpt = -21
                    if (rx <= 0) break; // words 2&3 are V&H coords, in 2m grid units
                    rpt--; // rpt = -22
                    if (cx <= 0) break;
                    rpt--; // rpt = -23
                    nxt = nxt & 15; // word 4 test bits: +8=(V>), +4=(V<), +2=(H>), +1=(H<)
                    // word 5 is added seconds; word 6 is restart +
                    yx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine));
                    zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aLine));
                    if (yx < 0) yx = 0;
                    if (zx > 99) zx = 0;
                    if (zx > 0) aStr = HandyOps.Hex2Log(" 0x", (zx << 16) + (yx & 0xFFFF), 6,
                            " 0 0 0 0 0 4");
                    else aStr = HandyOps.Dec2Log(" ", yx, " 0 0 0 0 0 4");
                    // nxt bits: +8=(V>), +4=(V<), +2=(H>), +1=(H<)..
                    aWord = "\nO " + HandyOps.NthItemOf(false, 2, aLine) + " ";
                    if (frax) { // allow for fractional position, but add test bits..
                        cx = (MyMath.Trunc8(Hat * 8.0) & 0xFFF) + (nxt << 12);
                        aWord = aWord + HandyOps.Fixt8th(" ", cx, aStr);
                    } //~if
                    else aWord = aWord + HandyOps.Dec2Log(" ", (nxt << 9) + cx, aStr);
                    theList = HandyOps.RepNthLine(aWord, lino, theList);
                    if (NoisyMap) System.out.println(HandyOps.Dec2Log("  {$} ", rx,
                            HandyOps.Dec2Log("/", cx, HandyOps.Substring(0, 66,
                                    HandyOps.ReplacAll("\\", "\n",
                                            HandyOps.RepNthLine(") '", 1, aWord))) + "'")));
                    rpt = -1;
                    aWord = "";
                    break; // case 'T'
                case 'J': // macro to build animation sequence..
                    if (!fini) continue; // time-based objects at end
                    rpt = -24;
                    if (ImDims == 0) break;
                    rpt--; // rpt = -25
                    if (rx <= 0) break; // words 2&3 are V&H coords, in 2m grid units
                    rpt--; // rpt = -26
                    if (cx <= 0) break;
                    rpt--; // rpt = -27
                    if (nxt < 5) break;  // word 4 is sequence+ (5-15), word 5 is + steps
                    if (nxt > 15) break;
                    aim = HandyOps.NthOffset(0, " --", aLine);
                    // if (aim<0) aim = HandyOps.NthOffset(0," //",aLine);
                    if (aim > 16) {
                        xStr = HandyOps.RestOf(aim, aLine); // preserve comment
                        aStr = HandyOps.Substring(0, aim, aLine);
                    } //~if
                    else {
                        xStr = " --";
                        aStr = aLine;
                    } //~else
                    rpt--; // rpt = -28
                    kx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aStr));
                    if (kx <= 0) break;
                    if (kx > 99) break;   // words 6&7 are 1st & last StopInfo ln+s
                    yx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aStr));
                    zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 7, aStr));
                    rpt--; // rpt = -29
                    if (yx < 11) nxt = 0;
                    else if (yx > zx) nxt = 0;
                    else if (zx > AfterStop) nxt = 0;
                    if (nxt == 0) { // nxt is logged..
                        nxt = (((yx << 8) + zx) << 8) + AfterStop;
                        break;
                    } //~if
                    yx = (((zx << 10) + yx) << 10) + yx; // word 8 is aim, word 9 is step size..
                    aim = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 8, aStr));
                    whar = HandyOps.SafeParseFlt(HandyOps.NthItemOf(false, 9, aStr));
                    MyMath.Angle2cart(aim); // angles are in degrees, not radians
                    Hstp = MyMath.Sine * whar;
                    Vstp = -MyMath.Cose * whar; // word 10 is start time, word 11 increment
                    rpt--; // rpt = -30
                    whar = HandyOps.SafeParseFlt(HandyOps.NthItemOf(false, 10, aStr));
                    Vinc = HandyOps.SafeParseFlt(HandyOps.NthItemOf(false, 11, aStr));
                    if (whar < 0.0) break; // we don't do negative time..
                    if (Vinc < 0.0) break;
                    rpt--; // rpt = -31
                    if ((whar == 0) != (Vinc == 0)) break; // end frame cannot be inc'd
                    xcell = 0;
                    if (false) {
                        xcell = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 12, aStr));
                        if (xcell + 180 < 0) xcell = 0;
                        else if (xcell >= 360) xcell = 0;
                        zx = HandyOps.NthOffset(0, "+", aStr);
                        if (zx > 0) aim = HandyOps.SafeParseInt(HandyOps.RestOf(zx + 1, aStr));
                    } //~if
                    else aim = 0;
                    Hinc = 1.0;
                    deep = 0.0;
                    aStr = HandyOps.NthItemOf(false, 12, aStr); // word 12: step thru index
                    if (aStr != "") while (true) {
                        xCh = HandyOps.CharAt(0, aStr);
                        if (xCh != '-') if ((xCh < '0') || (xCh > '9')) break;
                        zx = HandyOps.NthOffset(0, "*", aStr);
                        if (zx > 0) {
                            Hinc = HandyOps.SafeParseFlt(HandyOps.RestOf(zx + 1, aStr));
                            zx = MyMath.Trunc8(Hinc);
                            if (zx >= (yx & 0x3FF)) if (zx <= (yx >> 20)) {
                                deep = Hinc - MyMath.Fix2flt(zx, 0);
                                yx = yx & -0x400 | zx;
                            }
                        } //~if
                        Hinc = HandyOps.SafeParseFlt(aStr);
                        break;
                    } //~while
                    aWord = aLine;
                    zx = 0;
                    for (kx = kx - 1; kx >= 0; kx += -1) {
                        if (aim > 0) aStr = "+" + aim + " ";
                        else aStr = " ";
                        aStr = HandyOps.Fixt8th("@ ", MyMath.Trunc8(Vat * 8.0 + 0.5),
                                HandyOps.Fixt8th(" ", MyMath.Trunc8(Hat * 8.0 + 0.5),
                                        HandyOps.Dec2Log(" ", nxt, HandyOps.Dec2Log(" ", yx & 0x3FF,
                                                HandyOps.Fixt8th(aStr, MyMath.Trunc8(whar * 8.0 + 0.5),
                                                        HandyOps.Dec2Log(xStr + " +", zx, ""))))));
                        aWord = aWord + "\n" + aStr;
                        deep = deep + Hinc; // Hinc could be frac'l or multi-line and/or neg
                        if (NoisyMap)
                            System.out.println(HandyOps.Flt2Log("  {..} ", deep, " " + aStr));
                        whar = whar + Vinc;
                        Vat = Vat + Vstp;
                        Hat = Hat + Hstp;
                        if (xcell != 0) {
                            aim = aim + xcell;
                            while (aim >= 360) aim = aim - 360;
                            while (aim < 0) aim = aim + 360;
                        } //~if
                        while (deep >= 1.0) {
                            deep = deep - 1.0;
                            yx++;
                            if ((yx & 0x3FF) > (yx >> 20))
                                yx = (yx >> 10) & 0x3FF | yx & -0x400;
                        } //~while
                        while (deep <= -1.0) {
                            deep = deep + 1.0;
                            yx--;
                            if ((yx & 0x3FF) < ((yx >> 10) & 0x3FF))
                                yx = (yx >> 20) | yx & -0x400;
                        } //~while
                        zx++;
                    } //~for
                    theList = HandyOps.RepNthLine(aWord, lino, theList);
                    if (NoisyMap) System.out.println(HandyOps.Dec2Log("  <$> ", rx,
                            HandyOps.Dec2Log("/", cx, HandyOps.Dec2Log(" (", nxt,
                                    HandyOps.Substring(0, 66, HandyOps.ReplacAll("\\", "\n",
                                            HandyOps.RepNthLine(") '", 1, aWord))) + "'"))));
                    rpt = -1;
                    aWord = "";
                    break; // case 'J'
                case '@': // specify coords at (frame) time for (animated) artifact
                case 'Y': // macro to insert artifact from standard descriptor
                    if ((xCh == '@') != fini) continue; // time-based objects at end
                    rpt = -32;
                    if (ImDims == 0) break;
                    rpt--; // rpt = -33
                    if (rx <= 0) break; // words 2&3 are V&H coords, in 2m grid units
                    rpt--; // rpt = -34
                    if (cx <= 0) break;
                    rpt--; // rpt = -35
                    if (AfterStop < 12) break;
                    rpt--; // rpt = -36
                    kx = nxt;            // word 4 is type/sequence + (0-15)
                    if (kx < 0) break;
                    if (kx == 4) break; // use 'T' to start timer
                    if (kx > AfterStop) break;
                    EW = (kx < 5) || (kx > 15);
                    if (EW == fini) break; // all&only +5-15 are time-based
                    rpt--; // rpt = -37
                    nxt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine));
                    if (nxt < 0) break;  // word 5 is facing, c-wise degrees from north
                    rpt--; // rpt = -38  // ..or else line+ in StopInfo => (items +4-9)
                    if ((kx < 4) || (kx > 15)) {
                        if (nxt > 359) break;
                        if (nxt < 180) rpt = nxt + 180; // back side
                        else rpt = nxt - 180;
                        if (nxt < 270) yx = nxt + 90; // S-edge side
                        else yx = nxt - 270;
                    } //~if    // (preserve fractional coordinates)..
                    aWord = "\nO " + HandyOps.NthItemOf(false, 2, aLine) + " "
                            + HandyOps.NthItemOf(false, 3, aLine) + " ";
                    // aWord = "\n" + HandyOps.Dec2Log("O ",rx,HandyOps.Dec2Log(" ",cx," "));
                    if (kx == 0) aWord = aLine // stop sign..
                            + HandyOps.Dec2Log(aWord, nxt, HandyOps.NthItemOf(true, 1, theText))
                            + HandyOps.Dec2Log(aWord, rpt, HandyOps.NthItemOf(true, 2, theText))
                            + HandyOps.Dec2Log(aWord, nxt, HandyOps.NthItemOf(true, 3, theText))
                            + HandyOps.Dec2Log(aWord, rpt, HandyOps.NthItemOf(true, 4, theText))
                            + HandyOps.Dec2Log(aWord, yx, HandyOps.NthItemOf(true, 5, theText))
                            + aWord + "0 " + HandyOps.NthItemOf(true, 6, theText);
                    else if (kx > 15) { // use StopInfo line +kx++ for items +5-9..
                        rpt = -39;
                        xStr = HandyOps.NthItemOf(true, kx, theText);
                        if (xStr == "") break; // no such line
                        aStr = HandyOps.NthItemOf(false, 5, xStr);
                        zx = HandyOps.SafeParseInt(aStr); // => +9, ppm must be >0, <256..
                        rpt--; // rpt = -40
                        if (zx <= 0) break;
                        if (zx > 255) break;
                        if (HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, xStr)) != 0)
                            break; // rpt = -40 // => +10, anim+ must be 0
                        xStr = aLine + HandyOps.Dec2Log(aWord, nxt, " ") + xStr;
                        for (zx = 1; zx <= 99; zx++) { // add all linked lines..
                            aStr = HandyOps.NthItemOf(true, kx + zx, theText);
                            yx = HandyOps.NthOffset(0, "=", aStr);
                            if (yx <= 0) break;
                            if (HandyOps.SafeParseInt(HandyOps.RestOf(yx + 1, aStr)) != kx) break;
                            yx = HandyOps.NthOffset(0, "+", aStr);
                            if (yx > 0) { // got a view angle offset..
                                yx = HandyOps.SafeParseInt(HandyOps.RestOf(yx + 1, aStr));
                                if (yx <= 0) break;
                                if (yx >= 360) break;
                                yx = yx + nxt;
                                while (yx >= 360) yx = yx - 360;
                            } //~if
                            else yx = nxt;
                            xStr = xStr + HandyOps.Dec2Log(aWord, yx, " ") + aStr;
                        } //~for
                        aWord = xStr;
                    } //~if
                    else if (kx > 4) { // gotta insert time slot for '@' into seq list..
                        rpt = -41;
                        if (nxt < 11) break; // +5 is line+ in StopInfo => (items +5-9)
                        rpt--; // rpt = -42
                        if (nxt + 2 > AfterStop) break;
                        xStr = HandyOps.NthItemOf(true, nxt, theText);
                        if (xStr == "") break;
                        zx = HandyOps.NthOffset(0, HandyOps.Dec2Log("~", nxt, ""), xStr);
                        if (zx < 0) break; // rpt = -42 (invalid line+ in index line)
                        yx = HandyOps.NthOffset(0, "+", aLine);
                        if (yx <= 0) yx = 0;
                        else if (yx < zx) { // got a view angle (doesn't work)..
                            yx = HandyOps.SafeParseInt(HandyOps.RestOf(yx + 1, aLine));
                            if (yx < 0) yx = 0;
                            else if (yx >= 360) yx = 0;
                        } //~if
                        else yx = 0;
                        aWord = aWord + "0 " + xStr; // HandyOps.Dec2Log(aWord,yx," ")
                        nxt = AnimInfo[kx & 15]; // -> this obj's list (++ im's) in myMap
                        zx = MyMath.SgnExt(nxt); // (nxt is logged)
                        rpt--; // rpt = -43
                        if (zx <= 0) break;
                        yx = (nxt >> 16) + zx; // -> current item in seq list, => +11
                        zx = nxt + 0x10000; // (update AnimInfo after last error exit)
                        nxt = HandyOps.NthOffset(0, " --", aLine);
                        if (nxt < 0) nxt = HandyOps.NthOffset(0, " //", aLine);
                        rpt--; // rpt = -44
                        aStr = "";
                        if (nxt >= 0) {
                            if (nxt < 13) break;
                            aStr = HandyOps.RestOf(nxt, aLine); // preserve any comment for log
                            xStr = HandyOps.Substring(0, nxt + 1, aLine);
                        } //~if
                        else xStr = aLine;
                        // nxt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false,6,xStr));
                        nxt = HandyOps.NthOffset(0, " --", aWord);
                        if (nxt < 0) nxt = HandyOps.NthOffset(0, " //", aWord);
                        rpt--; // rpt = -45
                        if (nxt >= 0) {
                            if (nxt < 18) break;
                            if (aStr == "") aStr = HandyOps.RestOf(nxt, aWord);
                            aWord = HandyOps.Substring(0, nxt + 1, aWord);
                        } //~if
                        xStr = HandyOps.NthItemOf(false, 6, xStr); // +6 is exp.time => +12
                        rpt--; // rpt = -46
                        if (xStr == "") break;
                        AnimInfo[kx & 15] = zx;
                        aWord = aLine + HandyOps.Dec2Log(aWord, kx,
                                HandyOps.Dec2Log(" ", yx, " ")) + xStr + aStr;
                    } //~if
                    else if ((kx & -2) == 2) { // traffic light (animated, see Animatro)..
                        xStr = HandyOps.NthItemOf(true, 7, theText); // dark traffic lite..
                        zx = HandyOps.NthOffset(0, " --", xStr); // remove comment, if any..
                        // if (zx<0) zx = HandyOps.NthOffset(0," //",xStr);
                        if (zx >= 0) xStr = HandyOps.Substring(0, zx + 1, xStr);
                        zx = HandyOps.NthOffset(0, " 0 ", xStr);
                        if (zx >= 0) if (zx < 8) xStr = HandyOps.RestOf(zx + 2, xStr);
                        if (nxt < 270) yx = nxt + 90; // left side
                        else yx = nxt - 270;
                        if (nxt < 90) zx = nxt + 270; // right side
                        else zx = nxt - 90;
                        aStr = " 80" + xStr;
                        aWord = aLine
                                + HandyOps.Dec2Log(aWord, nxt, HandyOps.Dec2Log(aStr, kx, " `1"))
                                + HandyOps.Dec2Log(aWord, rpt, HandyOps.Dec2Log(aStr, kx, " `2"))
                                + HandyOps.Dec2Log(aWord, yx, HandyOps.Dec2Log(aStr, 5 - kx, " `3"))
                                + HandyOps.Dec2Log(aWord, zx, HandyOps.Dec2Log(aStr, 5 - kx, " `4"))
                                + aWord + " 0 0" + xStr + "0 `5"
                                + aWord + " 0 " + HandyOps.NthItemOf(true, 8, theText)
                                + aWord + " 0 " + HandyOps.NthItemOf(true, 9, theText)
                                + aWord + " 0 " + HandyOps.NthItemOf(true, 10, theText);
                    } //~if
                    else kx = -1;
                    if (kx >= 0) {
                        theList = HandyOps.RepNthLine(aWord, lino, theList);
                        if (NoisyMap) System.out.println(HandyOps.Dec2Log("  [$] ", rx,
                                HandyOps.Dec2Log("/", cx, HandyOps.Dec2Log(" (", kx,
                                        HandyOps.Substring(0, 66, HandyOps.ReplacAll("\\", "\n",
                                                HandyOps.RepNthLine(") '", 1, aWord))) + "'"))));
                        rpt = -1;
                    } //~if
                    else rpt = -47;
                    aWord = "";
                    break; // case '@'/'Y'
                case 'K': // ("\nK_ ") do back end of mobile artifact list..
                    rpt = -48;
                    if (ImDims == 0) break;
                    rpt--; // rpt = -49
                    if (rx <= 0) break;
                    rpt--; // rpt = -50
                    sofar = rx; // = resume object specs here
                    if (cx <= 0) break; // = + img-times (nCells)
                    rpt--; // rpt = -51
                    if (nxt <= 0) break; // = + sequences
                    rpt = -1;
                    break; // case 'K'
                case 'O': // artifact info (see ShoArtif)..
                    rpt = -55;
                    if (ImDims == 0) break;
                    rpt--; // rpt = -56
                    if (rx <= 0) break;
                    rpt--; // rpt = -57
                    if (cx <= 0) break;
                    rpt--; // rpt = -58
                    if (nxt < 0) break;
                    rpt--; // rpt = -59
                    if (nxt > 359) break; // +4: view angle, 0= centered facing north
                    // xcell = 0;
                    kx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 10, aLine));
                    // if (kx==4) if (nxt==1) xcell = 0x800;    // +10: anim'n seq +
                    // nxt = nxt<<4;
                    if (frax) { // allow for fractional position..
                        rx = MyMath.Trunc8(Vat * 8.0);
                        cx = MyMath.Trunc8(Hat * 8.0);
                    } //~if
                    else { // otherwise centered in cell..
                        rx = (rx << 3) + 4;
                        cx = (cx << 3) + 4;
                    } //~else
                    zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine));
                    if (kx != 4) { // kx=4: start up RealTime Base
                        if (zx <= 0) nxt = 0; // +5: range of view, 0=full
                        else if (zx < 360) nxt = (zx << 16) + nxt;
                        else nxt = 0;
                    } //~if
                    tall = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aLine));
                    wide = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 7, aLine));
                    zx = 0;                   // +6,7: height & width in pixels
                    aStr = HandyOps.NthItemOf(false, 8, aLine); // +8: image offset
                    yx = HandyOps.NthOffset(0, "^", aStr);
                    here = HandyOps.SafeParseInt(aStr);
                    rpt--; // rpt = -60
                    if (yx > 0) if (theImgs != null) {
                        xStr = HandyOps.RestOf(yx + 1, aStr);
                        yx = nxt;
                        nxt = HandyOps.SafeParseInt(xStr); // nxt is logged
                        if (nxt <= 0) break; // rpt = -60
                        if (nxt >= ImgHi) break;
                        nxt = nxt * ImgWi + here;
                        if (nxt > here) if (nxt < theImgs.length) here = nxt;
                        nxt = yx;
                    } //~if // (restore prior)
                    yx = 0;
                    rpt--; // rpt = -61
                    if (kx != 4) {              // +10: (kx) anim'n seq +, +9: ppm
                        if (tall <= 0) break;
                        rpt--; // rpt = -62
                        if (wide <= 0) break;
                        yx = wide >> 1;
                        rpt--; // rpt = -63
                        if (here <= 0) break;
                        here = (tall - 1) * ImgWi + yx + here; // now -> bottom middle
                        rpt--; // rpt = -64
                        if (here + yx >= ImSz) break;
                        zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 9, aLine));
                        rpt--; // rpt = -65
                        if (zx <= 0) break;
                        rpt--; // rpt = -66
                        if (zx > 255) break;
                        zx = (zx << 24) + here;
                    } //~if
                    else here = 0;
                    rpt = -67;
                    if (myMap == null) break;
                    if (kx < 0) kx = 0;
                    else if (kx > 15) kx = 0;    // +11: seq list ptr, +12: exp'n time..
                    else if (kx > 4) {
                        thar = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 11, aLine));
                        if (thar > 0) if (thar < myMap.length) {
                            Vat = HandyOps.SafeParseFlt(HandyOps.NthItemOf(false, 12, aLine));
                            aim = MyMath.Trunc8(Vat * 8.0);
                            myMap[thar] = ((sofar - 3) << 16) + (aim & 0xFFFF);
                        } //~if
                        nxt = 0;
                    } //~if
                    yx = (tall << 16) + 65536 - yx;
                    xcell = (((kx << 12) + rx) << 16) + cx; // +xcell
                    if (NoisyMap) {
                        if (aLine != "") {
                            if (aLine.length() > 36) aLine = "\n    '" + aLine + "'";
                            else aLine = " '" + aLine + "'";
                        } //~if
                        if (kx > 4) aLine = " t=" + aStr + aLine;
                        System.out.println(HandyOps.Dec2Log("(Art) ", lino,
                                HandyOps.Dec2Log("/", sofar, HandyOps.Dec2Log(" < ", nImgs,
                                        HandyOps.Dec2Log(" -> ", rx, HandyOps.Dec2Log("/", cx,
                                                HandyOps.Dec2Log(" @ ", tall, HandyOps.Dec2Log("/", wide,
                                                        HandyOps.Dec2Log(" ", kx, HandyOps.Dec2Log(" -> ", here,
                                                                HandyOps.Hex2Log("/x", here, 5, aLine)))))))))));
                        aLine = "";
                    } //~if
                    rpt--; // rpt = -68
                    if (sofar >= nImgs) break;
                    if (sofar < 0) break;
                    if (sofar > myMap.length - 5) break;
                    myMap[sofar] = xcell; // +0: map coords in pk meters *4 (25cm) +anim
                    myMap[sofar + 1] = nxt; // +1: range of view, view angle (or TimeBase+s)
                    if (kx != 4) {
                        myMap[sofar + 2] = zx;  // +2: ppm/pix offset
                        myMap[sofar + 3] = yx;  // +3: height and width
                        sofar = sofar + 4;
                    } //~if
                    else sofar = sofar + 2; // (start RealTime Base needs only +2 words)
                    xCh = '\0'; // (already logged)
                    continue; // case 'O'
                case 'I': // initialize as no prior
                    dont = -1;
                    prev = 0;
                    prio = 0;
                    rpt = -1;
                    break; // case 'I'
                case 'X': // erase overstrike memory
                    dont = prio; // ..but don't overstrike endpoint
                    if (rx > 0) wait = rx; // disable kitty-corner fill (wait) advances
                    else wait = 0;
                    if (wait == 0) prev = 0; // enable kitty-corner fill close to prev end
                    nCells = 0;
                    break; // case 'X'
                case 'P': // pilaster
                    rpt = -70;
                    if (rx <= 0) break;
                    rpt--; // rpt = -71
                    if (cx <= 0) break;
                    colo = 0x77777777;
                    dont = -1;
                    prev = 0;
                    prio = 0;
                    rpt = 0;
                    rpt = -1;
                    break; // case 'P'
                case 'B': // bucket fill
                    rpt = -72;
                    if (rx <= 0) break;
                    rpt--; // rpt = -73
                    if (cx <= 0) break;
                    if (nxt == 0) colo = 0x66666666; // 0 -> floor
                    else colo = 0x44444444;      // 1 -> track
                    TempStr = "(Bkt) '" + aLine + "'";
                    TmpI = TempStr.length();
                    MapBucket(0, rx, cx, colo, myMap);
                    System.out.println(TempStr);
                    dont = -1;
                    aLine = "";
                    nCells = 0;
                    prev = 0;
                    prio = 0;
                    rpt = -1;
                    break; // case 'B'
                case 'F': // rect fill w/grass (2x2m grid coords) or track
                    rpt = -74;
                    if (rx <= 0) break;
                    rpt--; // rpt = -75
                    if (cx <= 0) break;
                    nxt = nxt - rx;
                    info = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine)) - cx;
                    zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aLine));
                    if (zx == 0) colo = 0x66666666; // 0 -> floor
                    else if (zx < 0) colo = 0;      // -1 -> virgin (non-park)
                    else colo = 0x44444444;     // 1 -> track
                    for (nxt = nxt; nxt >= 0; nxt += -1) {
                        thar = here;
                        here = here + HalfMap;
                        for (yx = info; yx >= 0; yx += -1) {
                            if (myMap == null) break;
                            if (thar < 0) break;
                            if (thar < myMap.length) myMap[thar] = colo;
                            thar++;
                        }
                    } //~for // (yx)
                    nCells = 0;
                    dont = -1;
                    prev = 0;
                    prio = 0;
                    rpt = -1;
                    break; // case 'F'/'T'
                case 'V': // (white) N-S wall
                case 'H': // E-W
                    rpt = -76;
                    if (rx <= 0) break;
                    rpt--; // rpt = -77
                    if (cx <= 0) break;
                    colo = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine)) & 7;
                    rpt--; // rpt = -78
                    if (colo == 0) break;
                    rpt = nxt; // (not: rpt = -1;)
                    nxt = 1;
                    if (xCh == 'V') {
                        nxt = HalfMap;
                        rpt = rpt - rx;
                    } //~if
                    else rpt = rpt - cx;
                    if (rpt < 0) {
                        rpt = -79;
                        break;
                    } //~if
                    if (colo < 4) break; // single thinkness, go do it
                    colo = colo & 3;
                    recell = nxt;
                    nCells = 0;
                    dont = -1;
                    prev = 0;
                    prio = 0;
                    break; // case 'V'/'H'
                case 'Q': // convert track edge to grass (no border yet)
                    rpt = -80;
                    if (myMap == null) break;
                    thar = myMap.length - 1;
                    for (thar = thar; thar >= nuIxBase; thar += -1)
                        if (myMap != null) if (thar > 0)
                            if (thar < myMap.length) if (myMap[thar] < 0)
                                myMap[thar] = 0x66666666;
                    dont = -1;
                    wait = 0;
                    prev = 0;
                    prio = 0;
                    rpt = -1;
                    break; // case 'Q'
                default:
                    rpt = -1;
                    nxt = 0;
                    break;
            } //~switch
            if (NoisyMap) {
                if (aLine != "") aLine = " '" + aLine + "'";
                else if (((rpt + 1) | rx | cx | nxt) == 0) continue;
                if (aLine.length() > 36) aLine = "\n  __ " + aLine;
                if (aWord != "") aLine = aLine + "\n  + + '" + aWord + "'";
                if (rpt < 0) {
                    if (rpt + 1 < 0) {
                        aLine = ") <??>" + aLine;
                        nerrs++;
                    } //~if
                    else aLine = ")" + aLine;
                } //~if
                aWord = HandyOps.IffyStr(nImgs == 0, "(BildMap)", "(BildArt)")
                        + HandyOps.Dec2Log(" ", lino, HandyOps.Dec2Log(" -> ", rx,
                        HandyOps.Dec2Log("/", cx, HandyOps.Dec2Log(" ", kx,
                                HandyOps.Dec2Log(" ", nImgs, HandyOps.IffyStr(nImgs == 0, " (",
                                        HandyOps.Dec2Log("/", sofar, " (")))))));
                System.out.println(HandyOps.Int2Log(aWord, prio, HandyOps.Int2Log(" +", nxt,
                        HandyOps.IffyStr(rpt == -1, aLine, HandyOps.Dec2Log(" *", rpt,
                                HandyOps.IffyStr(rpt < 0, aLine, HandyOps.Dec2Log(") @ ", here,
                                        HandyOps.Hex2Log(" = x", colo, 8, aLine))))))));
                aWord = "";
            } //~if
            xCh = '\0'; // (already logged)
            for (rpt = rpt; rpt >= 0; rpt += -1) {
                if (here < 0) break;
                if (here >= myMap.length) break;
                myMap[here] = colo;
                if (colo == 0) break;
                if (recell != 0) if (here < myMap.length - (HalfMap + 2)) {
                    myMap[here + 1] = colo;
                    myMap[here + HalfMap] = colo;
                    myMap[here + HalfMap + 1] = colo;
                } //~if
                here = here + nxt;
            } //~for // (rpt)
            here = 0;
        } //~for // (lino)
        prio = nuIxBase;
        here = 0;
        nxt = 0;
        if (myMap != null) {
            if (myMap.length > ArtBase + 3) here = myMap[ArtBase + 2] + 2; // needs +3..
            if (here > 8) if (PaintIx > 0) if (here < myMap.length) if (myMap[here] == 0)
                for (thar = here; thar >= here + 6 - PaintIx; thar += -3) {         // shouldn't happen
                    if (myMap[thar] != 0) break;
                    myMap[thar] = 0x80000000;
                    nxt--;
                }
        } //~if
        if (NoisyMap) {
            aLine = "";
            if (here > PaintIx + 12) {
                aLine = "+3";
                thar = here + 3 - 12 - PaintIx;
            } //~if
            else thar = 0;
            if (myMap != null) {
                aLine = aLine + "  =-=" + HandyOps.ArrayDumpLine(myMap, 57, 32);
                if (prio > 0) if (prio < myMap.length - 1)
                    if (myMap[prio] == 0xDEADBEEF) myMap[prio] = 0;
            } //~if
            System.out.println(HandyOps.Dec2Log("--(BldMap) ", lino,
                    HandyOps.Dec2Log(" ", PaintIx, HandyOps.Dec2Log(" ", AfterStop,
                            HandyOps.Dec2Log(" ", nxt, HandyOps.Dec2Log(" ", here, aLine))))));
        } //~if
        return myMap;
    } //~BuildMap

    /**
     * Returns an angle (in non-negative degrees) halfway between here and thar.
     * Corrects for large and negative angles. If here and thar are 180 degrees
     * out of phase, returns the lowest non-negative result.
     *
     * @param here First angle in dregrees
     * @param thar Second angle in dregrees
     * @return The angle of a ray bisecting the acute vertext
     */
    public static double MidAngle(double here, double thar) {
        double aim;
        while (here < 0.0) here = here + 360.0; // normalize them..
        while (here > 360.0) here = here - 360.0;
        while (thar < 0.0) thar = thar + 360.0;
        while (thar > 360.0) thar = thar - 360.0;
        if (here > thar) {
            aim = thar;
            thar = here;
            here = aim;
        } //~if
        aim = (thar + here) * 0.5;
        if (here + 180.0 < thar) {
            if (aim < 180.0) aim = aim + 180.0;
            else aim = aim - 180.0;
        } //~if
        return aim;
    } //~MidAngle

    /**
     * Gets a pointer to the current image TrakSim is drawing on.
     * <p>
     * TrakSim maintains its own integer array to be used as a screen buffer,
     * but a client can "borrow" TrakSim's drawing tools by saving the screen
     * buffer, then restoring it when done.
     *
     * @return The current screen buffer
     */
    public int[] GetMyScreenAry() {
        return myScreen;
    } // so to trade out multiple..

    /**
     * Gets the size of the current image TrakSim is drawing on.
     * <p>
     * TrakSim maintains its own integer array to be used as a screen buffer,
     * but a client can "borrow" TrakSim's drawing tools by saving the screen
     * buffer, then restoring it when done.
     *
     * @return The height and width of screen buffer packed into an integer
     */
    public int GetMyScreenDims() {
        return (SceneTall << 16) + SceneWide;
    }

    /**
     * Gets the current frame count. TrakSim increases this number each time
     * some data changes and it is redrawn. Screen buffers requested between
     * changes do not increase the frame count.
     *
     * @return The current frame count
     */
    public int GetFrameNo() {
        return FrameNo;
    }

    /**
     * Gets the width of the image file (if loaded), or else =0. Use this
     * to convert pixel row & column into position for SeeOnScrnPaint
     *
     * @return The image width
     */
    public int GetImgWide() {
        return ImageWide;
    }

    /**
     * Sets the position and direction of the simulated car.
     *
     * @param Vat The vertical (southward) component of the position in meters
     * @param Hat The horizontal (eastward) component of the position in meters
     * @param aim The direction facing, in degrees clockwise from north
     */
    public void SetStart(int Vat, int Hat, int aim) {
        System.out.println(HandyOps.Dec2Log(" (SetStart) ", Vat, HandyOps.Dec2Log("/", Hat,
                HandyOps.Dec2Log(" -> ", aim, ""))));
        Vposn = (double) Vat;
        Hposn = (double) Hat;
        Facing = (double) aim; // aim is in degrees clockwise from North
        AverageAim = Facing;
    } //~SetStart

    /**
     * Sets/overrides the top row for the close-up map (if showing).
     *
     * @param here The image row to start map on
     */
    public void SetCloseTop(int here) {
        int nx, info, why = 0; // final int xCloseUp = 3; // xC=2..5
        while (true) {
            why++; // why = 1
            if (!ShowMap) break;
            why++; // why = 2
            if (!DoCloseUp) break;
            why++; // why = 3
            if (here < 0) break;
            why++; // why = 4
            if (here == 0) {
                TopCloseView = 0;
                ZooMapBase = 0;
                ZooMapDim = 0;
                break;
            } //~if
            if (here > ImHi - 32) break;
            why++; // why = 5
            if (RasterMap == null) {
                RasterMap = new double[ImHaf * 4]; // for V,H at each end
                if (RasterMap == null) break;
            } //~if
            why++; // why = 6 (commit)
            TopCloseView = here;
            if (((xCloseUp - 2) & 0xFF) < 4) nx = MapWiBit - (xCloseUp & 7);
            else if (TurnRadius < 7.5) nx = 4;
            else nx = 5; // = log2(displayed close-up width in park meters)
            ZooMapShf = MapWiBit - nx; // MapWiBit=log2(MapWide=256)=8, so ZSf= 4 or 3
            // zx = 1<<ZooMapShf; // MapWide=256, so zx=8 or =16, =pix/m in c-u view
            info = (ImHi - here) >> ZooMapShf; // = close-up height in pix, now pk meters
            ZooMapBase = (here << 16) + ImWi + 2; // ZooMapShf cvts c-u(pix) <-> meters..
            ZooMapDim = (info << 16) + (1 << nx); // = displayed close-up size in meters
            ZooMapScale = MyMath.Fix2flt(1 << (ZooMapShf + 1), 0); // cv 2m grid -> img pix
            if (WhitLnSz == 0.0) ZooMapWhLn = 0.0;
            else ZooMapWhLn = 2.8 / ZooMapScale; // 1pix = white line width (meters) in c-u
            why = 0;
            break;
        } //~while
        System.out.println(HandyOps.Dec2Log("(SetCloTop) ", here,
                HandyOps.Int2Log("  ZMD=", ZooMapDim, HandyOps.Dec2Log(" ZSf=", ZooMapShf,
                        HandyOps.Int2Log(" ZB=", ZooMapBase, HandyOps.Flt2Log(" ZSc=", ZooMapScale,
                                HandyOps.Flt2Log(" ZW=", ZooMapWhLn,
                                        HandyOps.Dec2Log(" = ", why, ""))))))));
    } //~SetCloseTop

    /**
     * Converts a row/column click location on the close-up map (if showing)
     * to a true (park) position in 64ths of a meter, packed into an integer.
     *
     * @param aim2 If true, also points the car to be facing the click point
     * @param rx   The image row clicked on
     * @param cx   The image column clicked on, relative to the window top/left
     * @return A packed integer, the vertical (southward) component
     * in the high 16 bits, horizontal (eastward) in the low 16;
     * the components are fixed-point, with 6-bit fractions.
     */
    public int ZoomMap2true(boolean aim2, int rx, int cx) { // rtns (r,c)<<6
        int base = (rx << 16) - ZooMapBase + cx, zx = base << (6 - ZooMapShf),
                rez = (ZooMapTopL << 6) + zx;
        double Vat = 0, Hat = 0; // get posn: Vat = MyMath.Fix2flt(rez&-0x10000,22);
        if (aim2) while (true) { //    Hat = MyMath.Fix2flt(MyMath.SgnExt(rez),6);
            Vat = MyMath.Fix2flt(rez & -0x10000, 22) - Vposn;
            Hat = MyMath.Fix2flt(MyMath.SgnExt(rez), 6) - Hposn;
            if (MyMath.fAbs(Vat) + MyMath.fAbs(Hat) < 1.5) break;
            Facing = MyMath.aTan0(Hat, -Vat);
            AverageAim = Facing;
            // NuData++; // caller does
            break;
        } //~while   // ZMD=12,16 ZSf=4 ZB=34,322 ZSc=32. ZW=0.1 ZT=10,8 // ¥
        if (Mini_Log) System.out.println(HandyOps.TF2Log("(ZooMp2tru) ", aim2,
                HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log("/", cx,
                        HandyOps.Int2Log(" ", base, HandyOps.Int2Log(" ", zx,
                                HandyOps.Hex2Log(" = x", rez, 8, // HandyOps.IffyStr(!aim2,"",
                                        HandyOps.Flt2Log(" - ", Vposn, HandyOps.Flt2Log("/", Hposn,
                                                HandyOps.Flt2Log(" -> ", Vat, HandyOps.Flt2Log("/", Hat,
                                                        HandyOps.Flt2Log(" => ", Facing, ""))))))))))));
        return rez;
    } //~ZoomMap2true

    /**
     * Converts a row/column position on the perspective view generated by TrakSim
     * to an image pixel position of the close-up map (if shown) or zero otherwise.
     * <p>
     * This is useful for adding to the close-up map display information derived
     * from examining the perspective view, which is the normal way a self-driving
     * car program would be operating. Alternatively (yx=true) you can convert
     * direct map coordinates (in park meters) to screen pixel in the close-up map.
     * <p>
     * The close-up map jumps around dynamically depending on the position and
     * direction of the car. This allows your software to add information to the
     * map view. Use ZoomMap2true to reverse the calculation.
     *
     * @param yx  If true, converts a position in meters instead of screen coord
     * @param Vat The image row in pixels, or map vertical in park meters
     * @param Hat The image column in pixels, or map horizontal in park meters
     * @return A packed integer, the pixel row in the high 16 bits,
     * the column in the low 16, or else 0 if not visible.
     */
    public int ZoomMapCoord(boolean yx, double Vat, double Hat) {
        // Vat,Hat in screen img posn, rtn map pix posn, or =0 if off-map
        double Vinc = 0.0, Hinc = 0.0, Vstp = 0.0, Hstp = 0.0, deep = 0.0;
        int nx = 0, kx = 0, zx = 0, rx = 0, cx = 0, why = 0;
        while (true) {
            why++; // why = 1
            if (!ShowMap) break;
            why++; // why = 2
            if (!DoCloseUp) break;
            why++; // why = 3
            if (ZooMapDim == 0) break;
            why++; // why = 4
            if (ZooMapBase == 0) break;
            why++; // why = 5
            if (ZooMapScale == 0.0) break; // ZMS*(2m grid) -> img pix in close-up
            why++; // why = 6
            if (Vat < 0.0) break;
            if (Hat < 0.0) break;
            if (!yx) { // yx=false converts image coordinate '(ZoMaCo) F' ..........
                why++; // why = 7
                if (RasterMap == null) break;
                rx = (int) Math.round((Vat - fImHaf) * 4.0);
                why++; // why = 8
                if (rx <= 0) break;
                why++; // why = 9
                if (rx >= RasterMap.length - 4) break;
                why++; // why = 10
                deep = Hat / FltWi; // = fractional position in image (1.0 = right edge)
                Vinc = RasterMap[rx]; // left end of raster (in grid=2m)
                Hinc = RasterMap[rx + 1];
                Vstp = RasterMap[rx + 2] - Vinc; // (distance to) right end
                Hstp = RasterMap[rx + 3] - Hinc;     // convert to close-up pix scale..
                rx = ((int) Math.round((Vstp * deep + Vinc) * ZooMapScale));
                // ZMD=17,32 ZSf=3 ZB=98,322 ZSc=16. ZW=0.2 ZT=80,30
                if (rx <= 0) break; // why = 10
                if (rx >= (MapHy << ZooMapShf)) break;
                cx = ((int) Math.round((Hstp * deep + Hinc) * ZooMapScale));
                why++; // why = 11
                if (cx <= 0) break;
                if (cx >= (MapWy << ZooMapShf)) break;
                why++;
            } //~if // why = 12
            else { // yx=true converts park coordinate '(ZoMaCo) T' ................
                why = 15;
                if (Hat >= fMapWi) break;
                why++; // why = 16
                deep = ZooMapScale * 0.5; // scales from meters instead of 2m grid
                rx = (int) Math.round(Vat * deep); // in close-up pix..
                cx = (int) Math.round(Hat * deep);
            } //~else
            // ZMD=32,32 ZSf=3 ZB=224,642 ZSc=16. ZW=0.2 ZT=8,16
            kx = (rx << 16) + cx; // (in c-u pix) then back to pk meters for bounds test..
            zx = ((kx >> ZooMapShf) & 0xFFF0FFF) - ZooMapTopL; // ZooMapShf cvts c-u(pix)..
            if ((zx & 0x80008000) != 0) nx = 0; // above or left       // .. <-> meters
            else if (((ZooMapDim - zx) & 0x80008000) != 0) nx = 0; // below or right
            else nx = kx + ZooMapBase - (ZooMapTopL << (ZooMapShf));
            if (nx != 0) why = 0; // else why = 13/16
            break;
        } //~while
        return nx;
    } //~ZoomMapCoord

    /**
     * Replaces the TrakSim pixel buffer with your own array.
     * You are responsible for saving the previous screen and restoring it
     * before calling any TrakSim method that expects to be using its own
     * screen buffer, such as NextFrame.
     *
     * @param theAry The pixel array, at least tall*wide in length
     * @param tall   The number of pixel rows
     * @param wide   The number of pixel columns
     * @param tile   Should be 1, because that's what TrakSim knows
     */
    public void SetMyScreen(int[] theAry, int tall, int wide, int tile) {
        int size = tall * wide;
        if (theAry == null) return;
        if (size > theAry.length) return;
        if (tall <= 0) return;
        if (wide <= 0) return;
        myScreen = theAry;
        SceneBayer = tile;
        SceneTall = tall;
        SceneWide = wide;
        PixScale = 0;
    } //~SetMyScreen

    /**
     * Gets the TrakSim pixel at the specified row and column.
     * This might be your own buffer, if you called SetMyScreen.
     *
     * @param rx The pixel row
     * @param cx The pixel column
     * @return The pixel in that position, = 0x00RRGGBB
     */
    public int PeekPixel(int rx, int cx) { // <- myScreen
        int here = SceneWide, didit = 0;
        int[] myPix = myScreen;
        if (rx >= 0) if (rx <= 0x4000) if (here > 0) if (here <= 0x4000) {
            here = rx * here + cx; // T68 compiler uses fast multiply
            didit++;
        }
        if (didit == 0) here = rx * WinWi + cx;
        if (myPix != null) if (here < myPix.length)
            if (here >= 0) return myPix[here];
        return 0;
    } //~PeekPixel

    /**
     * Sets the TrakSim pixel at the specified row and column.
     * This might be your own buffer, if you called SetMyScreen.
     *
     * @param colo The pixel to go in that position, = 0x00RRGGBB
     * @param rx   The pixel row
     * @param cx   The pixel column
     */
    public void PokePixel(int colo, int rx, int cx) { // -> myScreen
        int here = SceneWide, didit = 0;
        int[] myPix = myScreen;
        if (rx >= 0) if (rx <= 0x4000) if (here > 0) if (here <= 0x4000) {
            here = rx * here + cx; // T68 compiler uses fast multiply
            didit++;
        }
        if (didit == 0) here = rx * WinWi + cx;
        if (myPix != null) if (here < myPix.length)
            if (here >= 0) myPix[here] = colo;
    } //~PokePixel

    /**
     * Sets a whole rectangle of TrakSim pixels from one corner of the
     * rectangle in pixel coordinates, to the opposite corner.
     * This might be your own buffer, if you called SetMyScreen.
     *
     * @param colo The pixel color to fill that rectangle, = 0x00RRGGBB
     * @param rx   The pixel row in one corner
     * @param cx   The pixel column in the same corner
     * @param rz   The pixel row in the other corner (inclusive)
     * @param cz   The pixel column there
     */
    public void RectFill(int colo, int rx, int cx, int rz, int cz) {
        int here, thar, whar;
        int[] myPix = myScreen;
        if (myPix == null) return;
        if (rz < rx) {
            here = rz;
            rz = rx;
            rx = here;
        }
        if (cz < cx) {
            here = cz;
            cz = cx;
            cx = here;
        }
        if (rx < 0) rx = 0;
        if (rz >= SceneTall) rz = SceneTall - 1;
        if (cx < 0) cx = 0;
        if (cz >= SceneWide) cz = SceneWide - 1;
        whar = rx * SceneWide + cx;
        for (rx = rx; rx <= rz; rx++) {
            thar = whar;
            whar = whar + SceneWide;
            for (here = cx; here <= cz; here++) {
                if (thar >= 0) if (thar < myPix.length) myPix[thar] = colo;
                thar++;
            }
        }
    } //~RectFill

    /**
     * Draws a line of TrakSim pixels.
     * This might be your own buffer, if you called SetMyScreen.
     *
     * @param colo The pixel color to draw that line, = 0x00RRGGBB
     * @param rx   The pixel row at one end
     * @param cx   The pixel column at the same end
     * @param rz   The pixel row at the other end
     * @param cz   The pixel column there
     */
    public void DrawLine(int colo, int rx, int cx, int rz, int cz) {
        int nx;
        double here, step;
        if (SceneBayer > 0) if ((rz == rx) || (cz == cx)) {
            RectFill(colo, rx, cx, rz, cz);
            return;
        } //~if
        rz = rz - rx;
        cz = cz - cx;
        if (MyMath.iAbs(rz) > MyMath.iAbs(cz)) { // more vert than horiz..
            if (rz < 0) {
                cx = cx + cz;
                cz = -cz;
                rx = rx + rz;
                rz = -rz;
            } //~if
            step = MyMath.Fix2flt(cz, 0) / MyMath.Fix2flt(rz, 0);
            here = MyMath.Fix2flt(cx, 0);
            for (nx = 0; nx <= rz; nx++) {
                PokePixel(colo, nx + rx, MyMath.Trunc8(here));
                here = here + step;
            } //~for
            return;
        } //~if
        if (cz < 0) {
            rx = rx + rz;
            rz = -rz;
            cx = cx + cz;
            cz = -cz;
        } //~if
        step = MyMath.Fix2flt(rz, 0) / MyMath.Fix2flt(cz, 0);
        here = MyMath.Fix2flt(rx, 0);
        for (nx = 0; nx <= cz; nx++) {
            PokePixel(colo, MyMath.Trunc8(here), nx + cx);
            here = here + step;
        }
    } //~DrawLine

    /**
     * Draws a tiny decimal digit (or one of six other special characters)
     * on the TrakSim pixel buffer. The digit is 5 pixels high and 4 wide.
     * This might be drawn on your own buffer, if you called SetMyScreen.
     *
     * @param whom The digit to draw, a 4-bit index into "0123456789NESW.-"
     * @param rx   The pixel row at the bottom right corner
     * @param cx   The pixel column there
     * @param colo The color to draw that digit, = 0x00RRGGBB
     */
    public void ShoDigit(int whom, int rx, int cx, int colo) {
        int ro, co, bitz = TinyBits[whom & 15], wide = bitz >> 20, thar = wide;
        for (ro = 4; ro >= 0; ro += -1) {
            for (co = 3; co >= 0; co += -1) {
                if ((bitz & 1) != 0) {
                    if (PixScale > 0) RectFill(colo, rx - PixScale, cx - PixScale, rx, cx);
                    else PokePixel(colo, rx, cx);
                } //~if
                cx = cx - 1 - PixScale;
                bitz = bitz >> 1;
                if (wide == 0) continue;
                if (co != 2) continue;
                if ((thar & 1) != 0) {
                    if (PixScale > 0) RectFill(colo, rx - PixScale, cx - PixScale, rx, cx);
                    else PokePixel(colo, rx, cx);
                } //~if
                cx = cx - 1 - PixScale;
                thar = thar >> 1;
            } //~for // (co)
            if (PixScale > 0) {
                if (wide != 0) cx = cx - 4;
                cx = cx + (PixScale << 2);
                rx = rx - PixScale;
            } //~if
            cx = cx + 4;
            if (wide != 0) cx++;
            rx--;
        }
    } //~ShoDigit

    /**
     * Draws a text string (which must be only the characters: " 0123456789NESW.-")
     * on the TrakSim pixel buffer.
     *
     * @param aLine The text string to draw
     * @param rx    The pixel row at the bottom (right corner)
     * @param cx    The pixel column there, or the left end if negative
     * @param colo  The color to draw that text, = 0x00RRGGBB
     */
    public void LabelScene(String aLine, int rx, int cx, int colo) {
        int nx, bitz, doit = aLine.length(), zx = doit, more = PixScale + 1;
        char xCh;
        if (cx < 0) cx = ((more * doit) << 2) - cx; // H-posn<0 places left end of number
        cx = (more << 2) + cx;
        for (nx = doit - 1; nx >= 0; nx += -1) {
            xCh = HandyOps.CharAt(nx, aLine);
            cx = cx - (more << 2);
            if (xCh < ' ') break;
            if (xCh == ' ') {
                zx--;
                continue;
            } //~if
            if (xCh == '-') xCh = '/'; // for now, 'cuz we need '-' but not '/'
            bitz = (int) xCh;
            if (xCh > 'A') switch (bitz & 7) {
                case 3: // 'S'
                    bitz = 12;
                    break;
                case 5: // 'E'
                    bitz = 11;
                    break;
                case 6: // 'N'
                    bitz = 10;
                    break;
                case 7: // 'W'
                    bitz = 13;
                    break;
            } //~switch
            ShoDigit(bitz, rx, cx, colo);
            zx--;
            if (xCh != 'N') if (xCh != 'W') continue;
            cx = cx - (more + more);
        } //~for // (nx)
    } //~LabelScene

    /**
     * Sets (enlarged) pixel size for ShoDigit.
     * You should restore it to 0 (no enlargement) when done.
     *
     * @param size The number of screen pixels per digit pixel
     */
    public void SetPixSize(int size) {
        if (size < 0) return;
        if (size * 8 > ImHi) return;
        if (size > 0) size--;
        PixScale = size;
    } //~SetPixSize

    /**
     * Draws a red "X" in the lower left corner of the TrakSim pixel buffer.
     */
    public void DrawRedX() { // to show it crashed
        int rx;
        System.out.println(HandyOps.Dec2Log(" (DrRedX) o", OpMode, ""));
        for (rx = 0; rx <= 4; rx++) { // draw red (crashed) "X" in lower left corner..
            PokePixel(0xFF0000, ImHi - 8 + rx, 4 + rx);
            PokePixel(0xFF0000, ImHi - 8 + rx, 8 - rx);
        }
    } //~DrawRedX

    /**
     * Draws a tan steering wheel slightly to the left of center at the bottom
     * of the TrakSim pixel buffer. Optionally also shows the dashboard.
     * <p>
     * The steering wheel may be drawn with a piece of "white tape" on the top,
     * so it's easy to see when it it turned off-center.
     * The wheel has 23 positions from -11 to +11 where this tape is shown,
     * and it is not shown if the position is out of range.
     * Alternatively, you can give it a signed angle -127 to +127 and have it
     * scaled non-linearly to fit in the shorter range.
     *
     * @param posn   The position to draw the white tape, -11 to +11,
     *               or -127 to +127 if scaled
     * @param scaled True if the position is to be scaled
     * @param dash2  Also draw the dashboard from available information
     */
    public void DrawSteerWheel(int posn, boolean scaled, boolean dash2) {
        // with white tape (if posn in range) + steer/speed servo settings..
        int prio = posn, info, nx, doit, here, thar, whar, rx, cx, bitz = 0;
        String myDash = "", LefDash = "";
        /*
        if (DrawDash > 0) if (dash2) { // show dashboard..
            if (OpMode == 3) bitz = 0xCC0000; // red when crashed,
            else if (MyMath.iAbs(SpecWheel) > 88) bitz = 0xFF; // blue when crimped
            RectFill(bitz, SceneTall - DrawDash, 0, SceneTall, SceneWide);
            LefDash = " " + SpecWheel + " " + FrameNo;
            LabelScene(LefDash, SceneTall - 4, -12, 0xFFFFFF);
            myDash = HandyOps.Fixt8th(" ", RealTimeNow, " ");
            LabelScene(myDash, SceneTall - 4, myDash.length() * 2 + ImMid, 0xFF9900);
            if (Log_Draw) LefDash = LefDash + " t=" + myDash;
            myDash = HandyOps.Flt2Log(" ", Velocity * dFtime, " "); // (5/fps) dFtime=5
            LabelScene(myDash, SceneTall - 4, myDash.length() * 2 + SteerMid, 0xFF9999);
            if (Log_Draw) myDash = " '" + LefDash + "/" + myDash + "'";
        } //~if // (dash2)
        //*/ //MARKERPOINT
        if (scaled) if (posn < 127) { // scale it to TapedWheel..
            info = MyMath.iAbs(posn);
            if (info != 0) for (nx = (TapedWheel[0] & 255) - 1; nx >= 0; nx += -1) {
                if (nx > 0) if (((TapedWheel[nx & 15] >> 8) & 255) > info) continue;
                if (posn < 0) info = -nx;
                else info = nx;
                break;
            } //~for // always exits here, with info set
            posn = info;
        } //~if // (scaled)
        for (doit = 0; doit <= 1; doit++)
            for (here = doit; here <= 11; here++) { // draw steering wheel..
                thar = TapedWheel[here & 15];
                whar = thar >> 16;
                //info = SteerColo; // SteerColo=0xCC9900;
                if (doit == 0) {
                    if (here == posn) info = 0xFFFFFF; // PixWhit;
                    whar = whar + SteerMid;
                } //~if
                else {
                    if (here + posn == 0) info = 0xFFFFFF; // PixWhit;
                    whar = SteerMid - 1 - whar;
                } //~else
                if (Log_Draw) myDash = "";
                for (thar = thar & 255; thar <= 99; thar++) {
                    if (thar >= TapedWheel.length) break;
                    bitz = TapedWheel[thar]; // lsb is bottom of screen
                    if (bitz == 0) break;
                    for (cx = 0; cx <= 31; cx++) {
                        rx = ImHi - 1 - cx;
                        //if ((bitz & 1) != 0) PokePixel(info, rx, whar);
                        //bitz = bitz >> 1;
                        if (bitz == 0) break;
                    } //~for // (cx)
                    if (doit == 0) whar++;
                    else whar--;
                }
            }
    } //~DrawSteerWheel

    /**
     * Draws a rectangular grid on the screen according to the specification
     * array given to NewGridTbl.
     */
    public void DrawGrid() { // to show where to click..
        // private final int[] Grid_Locns = {6,12,16,18,26,28,  0,16,80,126,228,240,
        //   0,20,300,320,  0,320,  0,45,91,137,182,228,274,320, 0,320,  0,20,300,320};
        int here, thar, whar, nx, prio, fini, lft, rit,
                tops, info = 0, next = 0, botm = ImHi - 1;
        int[] grids = GridLocTable;
        if (grids == null) return;
        if (grids.length < 4) return;
        thar = grids[1]; // (end of) 1st group, vert'l divisions
        whar = grids[0]; // (next) top of vert'l group
        fini = whar - 1; // end of index
        if (fini <= 0) return;
        if (whar >= thar) return;
        if (whar > 0) if (whar < grids.length) next = grids[whar];
        rit = thar;
        for (here = 2; here <= fini; here++) {
            if (grids == null) break;
            whar++;
            tops = next;
            next = botm;
            if (whar < thar) if (whar > 0) if (whar < grids.length)
                next = grids[whar];
            if (tops >= botm) break;
            if (next > botm) next = botm;
            if (tops >= next) continue;
            if (tops > 0) for (nx = 0; nx <= ImWi - 1; nx++) if ((nx & 8) == 0) PokePixel(0xFF0000, tops, nx);
            if (grids == null) break;
            lft = rit;
            if (here > 0) if (here < grids.length) rit = grids[here];
            prio = 0;
            while (lft < rit) {
                if (grids == null) break;
                if (lft > 0) if (lft < grids.length) info = grids[lft];
                if (prio > 0) if (prio < info) for (nx = tops; nx <= next - 1; nx++)
                    if (((nx - tops) & 8) == 0) PokePixel(0xFF0000, nx, prio);
                prio = info;
                if (prio > ImWi - 2) break;
                lft++;
            }
        }
    } //~DrawGrid

    /**
     * Converts a click location on the window to a row&column number
     * corresponding to the rectangular specification given to NewGridTbl.
     *
     * @param rx The image row clicked on
     * @param cx The image column clicked on, relative to the window top/left
     * @return A packed integer, the grid row in the high 16 bits,
     * and the horizontal cell number in the low 16, or 0 if none
     */
    public int GridBlock(int rx, int cx) { // find click rgn of screen..
        int nx, fini, here, thar = 0, zx = 0, lino = 0, posn = 0;
        int[] grids = GridLocTable;
        if (grids == null) return 0;
        if (grids.length < 4) return 0;
        here = grids[1]; // (end of) 1st group, vert'l divisions
        fini = grids[0]; // top of 1st vert'l group, = end of index
        if (fini <= 0) return 0;
        if (fini >= here) return 0;
        for (here = here - 1; here >= fini; here += -1) {
            if (grids == null) break;
            if (here > 0) if (here < grids.length) zx = grids[here];
            if (rx >= zx) lino = here + 1 - fini;
            if (rx >= zx) break;
        } //~for
        if (grids == null) return 0;
        here = lino;
        if (here > 0) if (here < fini) if (here < grids.length - 2) {
            if (here + 1 == fini) thar = grids.length;
            else thar = grids[here + 1];
            nx = grids[here];
            for (thar = thar - 1; thar >= nx; thar += -1) {
                if (grids == null) break;
                if (thar > 0) if (thar < grids.length) zx = grids[thar];
                if (cx >= zx) posn = thar + 1 - nx;
                if (cx >= zx) break;
            }
        } //~if
        return (lino << 16) + posn;
    } //~GridBlock

    /**
     * Uses a specially formatted integer array to specify a grid of click
     * regions on the window. The grid consists of three or more ordered
     * sequences of integers, the first listing the beginnings (offsets into
     * the array) of each of the rest. The second sequence gives the pixel row
     * for the top each horizontal group (all the rest), which in turn give
     * left edge of each cell in sequence for  not replace it with your own) is the array
     * Grid_Locns, seeing which is probably the easy to understand the format.
     * There are seven sequences, the index starting at 0, then the six
     * sequences starting at the offsets it points to: 6,12,16,18,26,29.
     * The first of those, starting a offset 6, defines five regions vertically,
     * The top of the first region is the screen top =0, and extends for 16
     * pixel rows, the second to the middle of the screen less 40 pixel rows,
     * then the middle, then the top edge of the dashboard, which ends at the
     * bottom. The horizontal cells of the top group are the left 20 pixels as
     * row +1 cell +1, the right 20 pixels (+3), and everything in between as
     * cell +2 in row +1. The second group has only one cell, from the left
     * edge (0) to the right edge (ImWi); any click in that region is (2,1).
     * The next row, just above the middle of the screen, has seven equal cells.
     * The third row, from the middle down to the dashboard, has a little sliver
     * on the left (click there to properly shut down the serial port and exit).
     * And so on. Your only constraint is that all the cells on a horizontal row
     * are the same height.
     *
     * @param grids The array
     * @return True if properly formatted, or false if rejected.
     */
    public boolean NewGridTbl(int[] grids) { // to replace default screen grid..
        int nx, kx, zx, lxx, info = 0, fini = 0,          // grids = Grid_Locns
                here = 0, thar = 0, prio = 0, prev = 0, why = 0;
        // private final int[] Grid_Locns = {4,8,12,20,  0,16,228,240,
        //    0,20,300,320,   0,45,91,137,182,228,274,320,  0,20,300,320};
        while (true) {
            why++; // why = 1
            if (grids == null) break; // why = 1: null table
            why++; // why = 2
            lxx = grids.length;
            if (lxx < 4) break; // why = 2: not a useful size
            why++; // why = 3
            thar = grids[0];
            if (thar < 2) break; // why = 3: not enough division sets
            why++; // why = 4
            fini = thar - 1;
            for (here = 1; here <= fini; here++) {
                if (grids == null) break;
                nx = thar;
                thar = lxx;
                if (here > 0) if (here < grids.length) thar = grids[here];
                why++; // why = 5
                if (nx > thar) break; // why = 5: table index out of order
                why++; // why = 6
                prio = 0;
                for (nx = nx; nx <= thar; nx++) { // null division set OK
                    if (nx == thar) { // done with this group,
                        why = 4; // ..do next
                        break;
                    } //~if
                    if (grids != null) if (nx > 0) if (nx < grids.length) info = grids[nx];
                    if (info < prio) break; // why = 6: group item out of order (null OK)
                    prio = info;
                } //~for
                if (why > 4) break;
            } //~for
            if (why > 4) break;
            GridLocTable = grids;
            why = 0;
            break;
        } //~while
        if (why > 0) System.out.println(HandyOps.Dec2Log("NewGridTb failed, why = ", why,
                HandyOps.Dec2Log(" @ ", here, "")));
        return why == 0;
    } //~NewGridTbl

    /**
     * Requests TrakSim to set its simulation mode to one of three states.
     * mode=0: (initially) Paused, the simulated car does not move.
     * This is required to clear a crashed condition (OpMode==3).
     * mode=1: Single-step, the simulated car is updated this once only;
     * call SimStep(1) after each steering&gas update if your driving code
     * takes up more than FrameTime ticks (including TrakSim time).
     * This does not clear a crashed condition (OpMode==3).
     * mode=2: Real-time, the simulated car is updated on every call to
     * GetSimFrame (through SimCamera.NextFrame), as many times as
     * necessary to catch up to the real-time count of FrameTime ticks;
     * calling before the next tick (or if the simulated car is not moving
     * unless FreshImage was called) returns the previous image. Using this
     * mode when your driving software is too slow will miss frames (same
     * as happens with a real camera). Does nothing if crashed (OpMode==3).
     * mode=3: Crashed, same as Paused, but draws a red "X" on the dashboard.
     * Call SimStep(0) to clear the crashed condition.
     *
     * @param mode True to get the horizontal (east-west) coordinate
     */
    public void SimStep(int mode) {
        int nuly = mode, prio = OpMode;
        boolean doit = true;
        if (prio == 3) if (nuly > 0) doit = false;
        if (nuly < 0) {
            if (DriverCons.D_StartInCalibrate)
                if (SpecWheel == 0) unScaleStee = true; // used to calibrate servos
            nuly = 0;
        } //~if
        else if (nuly >= 3) {
            nuly = 3;
            NuData++;
        } //~if
        else if (nuly == 0) unScaleStee = false;
        else if (nuly == 2) ClockTimed = true;
        if (doit) {
            OpMode = nuly;
            StepOne = (nuly == 1);
            NextFrUpdate = 0;
        } //~if // in mode=2 this is when to update image
        System.out.println(HandyOps.Dec2Log(" (SimStep) o", mode,
                HandyOps.Dec2Log("/", prio, HandyOps.IffyStr(prio == 3, " (Crashed)",
                        HandyOps.IffyStr((nuly == 3) && (prio < 3), " ($$ CrashMe $$)",
                                HandyOps.TF2Log(" Fx=", SimSpedFixt, HandyOps.TF2Log(" Tr=", SimInTrak,
                                        HandyOps.IffyStr(unScaleStee, " (cal)", "")))))))
        );
        NuData++;
    } //~SimStep

    private int ViewAngle(int locn, int info) { // rtn >0 if in view, <0 if not
        double Vstp = 0.0, Hstp = 0.0;      // locn is artifact posn in 25cm units
        int res, aim, Hx, Vx = info >> 16, locx = locn & 0x0FFF0FFF, Vz = locx >> 16,
                Hz = locx & 0xFFFF;
        if (PreViewLoc != locx) {
            Vstp = MyMath.Fix2flt(Vz, 2) - Vposn; // Vz/Hz in 25cm grid/8 units,
            Hstp = Hposn - MyMath.Fix2flt(Hz, 2); // ..Vposn/Hposn in 1m park coords
            aim = (int) Math.round(MyMath.aTan0(Hstp, Vstp)); // degs c-wise from north
            PreViewLoc = locx;                          // .. from artifact to car
            PreViewAim = aim;
        } //~if
        else aim = PreViewAim;
        if (Vx < 6) Vx = 360; // view range < 6 degs, assume 360
        Vx = Vx >> 1;
        Hx = (info & 0xFFF) - aim; // view angle, 0= center facing as spec'd (N=0)
        while (Hx > 180) Hx = Hx - 360; // ..so Hx is angle from center to right
        while (Hx + 180 < 0) Hx = Hx + 360; // ..=angle from mid-view (f.north)
        res = (Vx - Hx) | (Vx + Hx); // Vx>0 so never: res=0
        return res;
    } //~ViewAngle

    private int Animatron(int whar, int locn, boolean logy) { // for animated art
        String aLine = ""; // used if logz=T  // may also retn TmpI = updated whar
        final int Anim_Log = DriverCons.D_Anim_Log;
        boolean uppy = false, logz = false, skipper = false;
        int why = 0, kx = 0, zx = 0, nx = 0, here = 0, thar, info,
                seen = locn, anim = (locn >> 28) & 15;
        int[] theInx = MapIndex;
        locn = locn & 0x0FFFFFFF; // whar = nx*4+ArtBase; whar -> locn
        TmpI = 0; // (Anim8) anim whar == locn =  why +FrameNo k=kx z=zx n=nx
        // ../TimeBaseSeq t=RealTimeNow/FakeRealTime ClockTimed/TimeOnly -> TmpI
        // \n  skipper o OpMode/StepOne  xTrLiteTime ---> ActivAnima \n @@ aLine
        if (anim > 0) while (true) {
            why++; // why = 1
            if (anim > 4) { // one-shot indexed movable stuff (setup @ [%]f)..
                why = 3;
                if (NoisyMap) if (Anim_Log > 0) if ((Anim_Log >> 16) == anim)
                    if ((Anim_Log & 0xFFFF) > FrameNo) logz = GoodLog; ///¥ change to Mini_Log;
                TmpI = (whar << 16) + whar + 1; // anchors are only one word long
                locn = -5;
                here = ActivAnima[anim & 15]; // -> this object's sequence list
                kx = here; // (for log)
                if (here <= 0) break; // why = 3
                why++; // why = 4
                while (true) { // fetch img ref(w/locn) from anim'n list by frame +..
                    if (here <= 0) break; // why = +4
                    why++; // why = +5
                    if (theInx == null) break;
                    why++; // why = +6
                    if (here >= theInx.length) break;
                    zx = theInx[here]; // -> 1st/next image in this sequence, exp.time
                    if (logz) {
                        if (aLine == "") aLine = "\n  @@";
                        aLine = aLine + HandyOps.Dec2Log(" ", here,
                                HandyOps.Int2Log(":", zx, ""));
                    } //~if
                    kx = MyMath.SgnExt(zx); // expiration time in secs/8 (<1hr)
                    why++; // why = +7
                    if (kx > 0) if (kx < RealTimeNow) { // if (RealTimeNow>0)
                        why = (why & -32) + 36; // why = *32 +4
                        here++;
                        kx = here;
                        uppy = true; // " =^= "
                        logy = true;
                        continue;
                    } //~if
                    why++; // why = +8
                    if (uppy) if (here > 0) ActivAnima[anim & 15] = here;
                    here = zx >> 16; // -> image in separate animated artifact images
                    if (here <= 0) break;
                    why++; // why = +9
                    if (theInx == null) break;
                    if (here > theInx.length - 4) break;
                    zx = theInx[here] & 0x0FFFFFFF; // location of image in grid
                    info = theInx[here + 1];        // orientation & view range
                    why++; // why = +10
                    if (zx == 0) break;
                    locn = zx;
                    thar = 0;
                    if (info == 0) thar = here;
                    else while (true) {
                        if (ViewAngle(locn, info) > 0) { // >0 if good
                            thar = here;
                            break;
                        } //~if
                        here = here + 4;
                        if (here <= 0) break;
                        if (here > theInx.length - 4) break;
                        zx = theInx[here] & 0x0FFFFFFF;
                        if (zx != locn) break;
                        info = theInx[here + 1];
                        if (info != 0) continue;
                        thar = here;
                        break;
                    } //~while
                    why++; // why = +11
                    if (thar > 0) {
                        TmpI = (thar << 16) + whar + 1;
                        why++;
                    } //~if // why = +12 (good)
                    else locn = -6; // why = +11 (none visi)
                    break;
                } //~while
                break;
            } //~if // (one-shot indexed movable stuff)
            else if (anim == 4) { // start up RealTime Base
                why = 32; // [from Fig-8] 0x48A4A0A4 = 4.A4=164.A.A4=164=20*8+4=20.5
                TmpI = (whar << 16) + whar + 2; // it's two words long: locn/time
                zx = (locn >> 12) & 15; // bits: +8=(V>), +4=(V<), +2=(H>), +1=(H<) [=8+2]
                kx = locn & 0xFFF0FFF;
                locn = -4;
                nx = 0;
                if (whar > 0) if (whar < theInx.length - 1) nx = theInx[whar + 1]; // [=1]
                why++; // why = 33
                if ((nx & -0x10000) > TimeBaseSeq) break;
                nx = (nx & 0xFFFF) << 3; // nx: restart +, added seconds, now s/8
                if (RealTimeBase != 0) break;
                seen = zx;
                why++; // why = 34
                if (zx > 3) {
                    info = ((int) Math.round(Vposn * 8.0)) - (kx >> 16);
                    if (zx > 7) { // bits: +8=(V>)
                        if (info > 0) seen = zx - 16;
                    } //~if
                    else if (info < 0) seen = zx - 16;
                } //~if // bits: +4=(V<)
                zx = zx & 3;
                if (zx > 0) {
                    info = ((int) Math.round(Hposn * 8.0)) - MyMath.SgnExt(kx);
                    if (zx > 1) { // bits: +2=(H>)
                        if (info > 0) zx = -zx;
                    } //~if
                    else if (info < 0) zx = -zx; // bits: +1=(H<)
                    if (zx < 0) { // if both H&V are tested, both must fire..
                        if (seen < 4) seen = seen | -16;
                    } //~if
                    else if (seen < 0) seen = seen & 15;
                } //~if // only V fired, so untrigger
                if (zx < 0) {
                    zx = TimeBaseSeq >> 16;
                    TimeBaseSeq = TimeBaseSeq + 0x10000;
                    FakeTimeBase = FakeRealTime / 125 - nx; // FTB,RTB both in 1/8th secs..
                    RealTimeBase = HandyOps.TimeSecs(true) / 125 - nx;
                    if (GoodLog) System.out.println(HandyOps.Dec2Log("** TimBase ", FrameNo,
                            HandyOps.Fixt8th(" = ", FakeTimeBase,
                                    HandyOps.Fixt8th(" / ", RealTimeBase, HandyOps.Fixt8th(" +", nx,
                                            HandyOps.Dec2Log(" #", zx, HandyOps.PosTime(" @ ")))))));
                    zx = seen;
                } //~if
                else why++; // why = 35
                break;
            } //~if // why = 34/35 // (start up RealTime Base)
            else if (anim > 1) { // traffic light (see StopInfo)
                why = 13;
                locn = -2;
                if (theInx == null) break;
                why++; // why = 14
                if (whar < ArtBase) break; // whar should -> blanked-out light
                why++; // why = 15
                // if (((whar-ArtBase)&3) !=0) break; // (obsolete)
                skipper = true;
                if (ClockTimed) zx = HandyOps.TimeSecs(false);
                else zx = FakeRealTime >> 10; // FRT in ms
                zx = zx & ((2 << xTrLiteTime) - 1);
                why++; // why = 16
                for (nx = 0; nx <= 9; nx++) {
                    whar = whar + 4;
                    if (whar >= theInx.length) break; // why = +16
                    why++; // why = 17
                    info = theInx[whar];
                    if (info == 0) why++; // why = +18 (locn = -2)
                    else if (((seen - info) & 0x0FFFFFFF) != 0) why = why + 2; // why = +19
                    else if ((info & -0x10000000) != 0x10000000) {
                        // skip over different aspects (a=2/3)..
                        why = (why & -32) + 48; // why = *32 +16
                        continue;
                    } //~if
                    else locn = info & 0x0FFFFFFF; // why = +17 (good, 1st anim+1)
                    break;
                } //~for
                kx = (1 << xTrLiteTime) - 2;
                if ((((zx >> xTrLiteTime) + anim) & 1) == 0) kx = 8; // red
                else if ((zx & kx) == kx) kx = 4; // yellow
                else kx = 0; // green
                kx = whar + kx;
                if (locn > 0) {
                    why = why + 4; // => +21
                    if (kx > 0) if (kx < theInx.length) {
                        why = why + 4; // => +25 (good result)
                        info = theInx[kx];
                        if (info > 0) if ((info & -0x10000000) == 0x10000000)
                            TmpI = (kx << 16) + kx + 4;
                    } //~if
                    if (TmpI == 0) locn = locn - 0x80000000;
                }
            } //~if // (traffic light)
            else locn = -1; // if (anim==1) // animation skipover (unused phases)
            break;
        } //~while
        if (locn < 0) uppy = false;
        if (skipper) if (TmpI == 0) for (nx = 9; nx >= 0; nx += -1) {
            whar = whar + 4;
            if (whar < theInx.length) if (((theInx[whar] - seen) & 0x0FFFFFFF) == 0)
                continue;
            TmpI = (whar << 16) + whar;
            break;
        } //~for                // GoodLog = true, logy = Fax_Log = T..
        if (GoodLog) if (logy) System.out.println(HandyOps.Dec2Log("(Anim8) ", anim,
                HandyOps.Int2Log(HandyOps.IffyStr(uppy, " =^= ", " =#= "), locn,
                        HandyOps.Dec2Log(" ", whar, HandyOps.Dec2Log(" = ", why,  // why =
                                HandyOps.Dec2Log(" #", FrameNo, HandyOps.IffyStr(anim == 0, "",
                                        HandyOps.Int2Log(" k=", kx, HandyOps.Int2Log(" z=", zx,
                                                HandyOps.Int2Log(" n=", nx, HandyOps.Int2Log("/", TimeBaseSeq,
                                                        HandyOps.Fixt8th(" t=", RealTimeNow, HandyOps.Dec2Log("/", FakeRealTime,
                                                                HandyOps.TF2Log(" ", ClockTimed,
                                                                        HandyOps.Int2Log(" -> ", TmpI, HandyOps.IffyStr(TmpI == 0, aLine,
                                                                                HandyOps.Int2Log("\n    ", seen, HandyOps.TF2Log(" ", skipper,
                                                                                        HandyOps.Int2Log(" ", here, HandyOps.Dec2Log(" o", OpMode,
                                                                                                HandyOps.TF2Log("/", StepOne, HandyOps.Dec2Log(" ", xTrLiteTime,
                                                                                                        " ---> " + HandyOps.ArrayDumpLine(ActivAnima, 0, 36)
                                                                                                                + aLine))))))))))))))))))))));
        return locn;
    } //~Animatron

    private void ShoArtifax() { // sort by distance, then display far-to-near..
        double fudge = 6.0; // (patch as needed)
        final int RmapMsk = 1023; // final boolean Fax_Log = true;
        final double fImMid = (double) ImMid, // ImMid = ImWi/2 // zx50 = 28
                ZoomRatio = (50 * 64.0) / (Zoom35 * fImMid);
        boolean doit = (OpMode == 2) || StepOne, logz, logy = Fax_Log; // T: detailed log
        int Vbase = (int) Math.round(Vposn * 4.0), Hbase = (int) Math.round(Hposn * 4.0),
                pint = (int) Math.round(Facing), whar = ArtBase, here, Pmap, oops,
                tall, wide, nx, yx, zx, dx = 0, seen = 0, pixn = 0, ImgWi = 0,
                Vx = 0, Hx = 0, rx = 0, cx = 0, lxx = 0, far = 0, anim = 0, ppm = 0,
                tops = 0, lino = 0, whom = 0, locn = 0, info = 0, why = 0;
        String aLine = "";
        double step = 0.0, Vat = 0.0, Hat = 0.0, aim = 0.0, RoWiM = 0.0, fox = 0.0;
        int[] theImgs = TrakImages;
        int[] theInx = MapIndex;
        int[] myFax = theFax;
        while (true) {
            why++; // why = 1
            if (NumFax <= 0) break; // calc'd by InitIndx
            why++; // why = 2
            if (theInx == null) break;
            PreViewAim = 0;
            PreViewLoc = 0;
            far = theInx.length;
            ImgWi = MyMath.SgnExt(theInx[0]);
            if (theInx.length > 4) dx = theInx[2];
            why++; // why = 3
            if (ImgWi < 64) break;
            if ((ImgWi & 15) != 0) break;
            why++; // why = 4
            if (myFax == null) break;
            why++; // why = 5
            if (theImgs == null) break;
            why++; // why = 6
            if (VuEdge == 0.0) break;
            why++; // why = 7
            if (TrakNoPix) break;
            why++; // why = 8
            fox = ZoomRatio * fudge;
            lxx = theImgs.length;
            if (RealTimeBase != 0) if (doit) { // RTN in seconds/8..
                if (StepOne) RealTimeNow = (FakeRealTime / 125 - FakeTimeBase);
                else RealTimeNow = HandyOps.TimeSecs(true) / 125 - RealTimeBase;
            } //~if
            while (pint > 360) pint = pint - 360; // normalize range for later use
            while (pint < 0) pint = pint + 360;
            if (dx == 0) yx = 11;
            else yx = dx + 2;
            if (false)
                System.out.println("An8 =-=" + HandyOps.ArrayDumpLine(theInx, yx, 5));
            for (nx = NumFax - 1; nx >= 0; nx += -1) { // calc in-view, distance
                why++; // why = 9
                here = whar; // whar = nx*4+ArtBase;
                whar = whar + 4;
                if (here < 0) break;
                why++; // why = 10
                if (here > theInx.length - 8) break; // can't
                why++; // why = 11
                locn = theInx[here]; // locn in 25cm units; anim = locn>>28
                info = theInx[here + 1]; // range of view, view angle in degrees
                yx = theInx[here + 4]; // look-ahead for alternate view
                seen = locn;
                anim = (locn >> 28) & 15; // is this in view angle?..
                if (anim < 4) if (info != 0) if (ViewAngle(locn, info) < 0) {
                    if (((yx - locn) & 0x0FFFFFFF) == 0) continue;
                    locn = -1; // last one not visi, hide it
                    anim = 0;
                } //~if
                if (anim != 0) {
                    locn = Animatron(here, locn, logy); // (see StopInfo)
                    if (TmpI > 0) {
                        whar = TmpI & 0xFFFF; // next up
                        here = TmpI >> 16;
                    }
                } //~if // here -> actual data
                why = 8;
                if (locn == 0) break; // (normal exit); <0: obj del'd by Animatro
                RoWiM = 0.0;
                step = 0.0;
                aim = 0.0;
                cx = 0;
                dx = 0;
                Vx = 0;
                Hx = 0;
                if (locn > 0) if (locn != whom) { // duplicate locn = alt view
                    whom = locn;
                    info = locn - (Vbase << 16) - Hbase; // (for log) V/Hbase in 25cm units
                    Vx = (locn >> 16) - Vbase; // position of artifact, relative to car
                    Hx = (locn & 0xFFF) - Hbase; // ..measured in half-meters (50cm)
                    aim = MyMath.aTan0(MyMath.Fix2flt(Hx, 0), -MyMath.Fix2flt(Vx, 0));
                    aim = aim - Facing; // aim is angle of deviation from center-view
                    while (aim > 180.0) aim = aim - 360.0;
                    while (aim + 180.0 < 0.0) aim = aim + 360.0;
                    Vx = Vx * Vx; // (includes 4 bits of fractional square meters)
                    Hx = Hx * Hx;
                    if (aim > VuEdge) dx = -8; // not in view
                    else if (aim + VuEdge >= 0.0) { // dx = radial distance, cx = pix H-posn..
                        step = Math.sqrt(MyMath.Fix2flt(Vx + Hx, 4)) * 4.0;
                        if (false) if ((aim > 5.0) || (aim + 5.0 < 0.0)) {
                            MyMath.Angle2cart(aim); // off-center, adjust distance..
                            step = step / MyMath.Cose;
                        } //~if
                        dx = (int) Math.round(step);
                        cx = MyMath.iMax((int) Math.round(aim * fImMid / VuEdge + fImMid), 0);
                    } //~if
                    else dx = -8;
                    if (lino > myFax.length - 2) break; // (can't)
                    myFax[lino] = here;
                    myFax[lino + 1] = (dx << 16) + cx; // dx in 25cm units, cx in pixels
                    lino = lino + 2;
                } //~if
                if (Mini_Log) if (logy) System.out.println(HandyOps.Dec2Log("  (;;) ", nx,
                        HandyOps.Dec2Log(" +", lino, HandyOps.Dec2Log(" = ", here,
                                HandyOps.Dec2Log(" ", dx, HandyOps.Dec2Log("/", cx,
                                        HandyOps.Int2Log(" => ", locn, HandyOps.Int2Log(" ", info,
                                                HandyOps.Dec2Log(" ", Vx, HandyOps.Dec2Log("/", Hx,
                                                        HandyOps.Flt2Log(" ", aim, HandyOps.Flt2Log(" = ", step,
                                                                HandyOps.Int2Log(" ", seen, "")))))))))))));
            } //~for // (calc in-view)
            if (why > 8) break;
            if (Mini_Log) if (logy) aLine = "  (sort) "
                    + HandyOps.ArrayDumpLine(myFax, lino, 9) + "\n   .... ";
            why = 12;
            whar = lino - 2;
            if (whar > 0) for (nx = whar; nx >= 2; nx += -2)
                for (lino = 2; lino <= nx; lino += 2) { // sort..
                    if (lino < 2) break;
                    if (lino > myFax.length - 2) break; // (can't)
                    Vx = myFax[lino - 1];
                    Hx = myFax[lino + 1];
                    if (Vx <= Hx) continue; // bubble sort is good enough for few arts..
                    myFax[lino - 1] = Hx;
                    myFax[lino + 1] = Vx;
                    info = myFax[lino];
                    oops = myFax[lino - 2];
                    if (Mini_Log) if (logy) aLine = aLine + HandyOps.Dec2Log(" ", lino,
                            HandyOps.Int2Log(":", info, HandyOps.Int2Log("`", Hx,
                                    HandyOps.Int2Log("/", oops, HandyOps.Int2Log("`", Vx, "")))));
                    myFax[lino - 2] = info;
                    myFax[lino] = oops;
                } //~for
            if (Mini_Log) if (logy) System.out.println(aLine + "\n     => "
                    + HandyOps.ArrayDumpLine(myFax, whar + 2, 8));
            why++; // why = 13
            for (nx = whar; nx >= 0; nx += -2) { // draw them (no cont, 4brk @ front)..
                oops = 0;
                why++; // why = 14
                if (nx > myFax.length - 2) break; // (can't)
                whar = myFax[nx];
                dx = myFax[nx + 1];
                why++; // why = 15
                if (dx < 0) {
                    oops = 2048;
                    dx = 0;
                } //~if
                why = 13;
                cx = dx & 0xFFFF;
                dx = dx >> 16; // in 25cm units, max 1.4K
                rx = MyMath.iMin(dx, 255); // RR[] indexed by park 25cm units..
                rx = RangeRow[rx & 255]; // convert distance to screen row (MkRngTb)
                Pmap = 0;
                tall = 0;
                wide = 0;
                yx = 0; // >0 is a good ref
                if (whar > 0) if (whar < theInx.length - 8) {
                    yx++; // >0 is a good ref
                    locn = theInx[whar];
                    anim = locn >> 28; // logged only
                    locn = locn & 0x0FFFFFFF;
                    Pmap = theInx[whar + 2]; // image offset & ppm (pix/meter)
                    tall = theInx[whar + 3];
                } //~if // [anim &] image H & W
                if (Mini_Log) if (logy) aLine = "  (::)" + HandyOps.Dec2Log(" ", nx,
                        HandyOps.Dec2Log(" = ", dx, HandyOps.Dec2Log(" => ", rx,
                                HandyOps.Dec2Log("/", cx, HandyOps.Dec2Log(" @ ", whar,
                                        HandyOps.Dec2Log(" #", anim, HandyOps.Dec2Log(" o", OpMode,
                                                HandyOps.IffyStr(oops > 0, " (<0)", ""))))))));
                if (whar > 0) { //                                             (ShoArtifx)
                    if (oops == 0) if (((anim - 1) & 31) > 3) // can't crash into traffic lights
                        if (dx < 7) SimStep(5); // should also crash if grazes corner (CrashMe)
                    if (ShowMap) { // map coord in meters, cell center..
                        info = ZoomMapCoord(true, MyMath.Fix2flt(locn >> 16, 2),
                                MyMath.Fix2flt(locn & 0xFFF, 2));
                        zx = info & 0xFFF;
                        info = info >> 16;
                        if (info > 0) { // put amber dot there in close-up map..
                            PokePixel(ArtiColo, info, zx + 1);
                            PokePixel(ArtiColo, info, zx);
                            info++;
                            PokePixel(ArtiColo, info, zx + 1);
                            PokePixel(ArtiColo, info, zx);
                        }
                    } //~if // (whar>0)
                    zx = 0;
                    if (RoWiM == 0.0) {
                        zx = (rx - ImHaf) << 2; // ImHaf = ImHi/2
                        if (pint < 8) zx++; // car facing N/S (use H)
                        else if (pint < 82) zx = 0; // not square-on
                        else if (pint < 98) {
                        } // car facing E/W (use V)
                        else if (pint < 172) zx = 0;
                        else if (pint < 188) zx++;
                        else if (pint < 262) zx = 0;
                        else if (pint < 278) {
                        } else if (pint < 352) zx = 0;
                        else zx++;
                    } //~if // (RoWiM=0) // (RM[] in grid=2m)..
                    if (zx > 0) RoWiM = fImMid / MyMath.fAbs(RasterMap[(zx + 2) & RmapMsk]
                            - RasterMap[zx & RmapMsk]);
                    if (Mini_Log) if (logy)
                        System.out.println(HandyOps.Dec2Log("   (``) ", oops,
                                HandyOps.Flt2Log(" (", fImMid, HandyOps.Flt2Log("/", VuEdge,
                                        HandyOps.Dec2Log(") @ ", whar, HandyOps.Int2Log(" => ", locn,
                                                HandyOps.Int2Log(" ", tall, HandyOps.Int2Log(" -> ", Pmap,
                                                        HandyOps.Hex2Log("/x", Pmap, 5, HandyOps.Dec2Log(" ", Hx,
                                                                HandyOps.Dec2Log(" : ", Vx, HandyOps.IffyStr(RoWiM == 0.0, "",
                                                                        HandyOps.Flt2Log(" p/m=", RoWiM, "")))))))))))));
                    // whar = whar-ppm;
                    Vx = 0;
                    Vat = 0.0;
                    Hat = 0.0;
                    if (tall > 0) while (true) {
                        ppm = (Pmap >> 24) & 255; // SS ppm = 44
                        Pmap = Pmap & 0x00FFFFFF;
                        wide = -MyMath.SgnExt(tall);
                        tall = tall >> 16;
                        // anim = tall>>12; // logged only (also test for unreasonableness)
                        tall = tall & 255;                   // ImMid = ImWi/2..
                        tops = Pmap - (tall - 1) * ImgWi - wide;   // dx = dist in 25cm units..
                        // ZoomRatio = (50*64.0)/(Zoom35*fImMid) = 3200/(35*160) = 20/35
                        // [dx=13] (3x1m dist) st = f*(13*44=572*20/35)/1024 = 5720/(280*64)..
                        step = MyMath.Fix2flt(dx * ppm, 10) * fox;  // ..fox=ZR*2; st=20*2/64 =5/8
                        Vat = 0.0;                 // step = fractional im.pix per scr.pix
                        if (ppm == 0) oops = oops | 64;
                        if (wide <= 0) oops = oops | 32;
                        if (tall <= 0) oops = oops | 16;
                        if (tops < 0) oops = oops + 8;
                        if (Pmap + wide >= lxx) oops = oops + 4;
                        if (step > 64.0) oops = oops + 2;
                        if (step < 0.01) oops++;
                        tall = MyMath.Trunc8(MyMath.Fix2flt(tall, 0) / step) + 1; // + V-rows
                        zx = MyMath.Trunc8(MyMath.Fix2flt(wide, 0) / step) + 1; // + H-cols/2
                        cx = cx - 1 - zx; // off left end of image on screen
                        Hat = MyMath.Fix2flt(Pmap, 0) - MyMath.Fix2flt(zx, 0) * step + 0.5;
                        if (zx < 0) {
                            oops = oops | 128;
                            zx = 0;
                        } //~if
                        for (zx = zx + zx; zx >= 0; zx += -1) { // outer loop is horizontal..
                            cx++;                // if (oops>0) log only, then exit
                            Vx = MyMath.Trunc8(Hat);
                            Hat = Hat + step;
                            Vat = 0.0;
                            if (cx >= ImWi) oops = oops | 256; // off right end
                            else if (cx + ImWi < 0) oops = oops | 512; // off left end
                            else if (cx < 0) oops = oops | 0x1000; // kill inner loop, but not outer
                            if ((oops == 0) || Mini_Log && logy) for (dx = 0; dx <= tall + 12; dx++) {
                                yx = rx + 2 - dx;                       // do this column up..
                                if (yx < 0) oops = oops | 0x2000; // off top of screen
                                if (Vx < tops) oops = oops | 0x4000; // (miscalc: off top of img)
                                info = 0;
                                if (Vx > 0) if (Vx < theImgs.length) info = theImgs[Vx];
                                if (Mini_Log) if (logy) {
                                    logz = ((dx & 15) == 0);
                                    if (yx == TripLine) logz = true;
                                    else if (oops == 0) if (dx > 3) if (dx + 5 < tall) if (info < 0)
                                        logz = false;
                                    if (logz) System.out.println(HandyOps.Dec2Log("  _ _ ", dx,
                                            HandyOps.Dec2Log("/", zx, HandyOps.Dec2Log(" ", rx,
                                                    HandyOps.Dec2Log(" ", yx, HandyOps.Dec2Log("/", cx,
                                                            HandyOps.Dec2Log(" @ ", Vx, HandyOps.Flt2Log(" ", Vat * 16.0,
                                                                    HandyOps.Hex2Log(" x", oops, 4,
                                                                            HandyOps.IffyStr(yx >= ImHi - 1, " = ^",
                                                                                    HandyOps.IffyStr(info < 0, " = _",
                                                                                            HandyOps.Colo2Log(" = ", info, ""))))))))))));
                                } //~if
                                if (oops != 0) break; // otherwise commit..
                                if (info >= 0) if (yx < ImHi - 1) PokePixel(info, yx, cx);     //MARKERPOINT
                                Vat = Vat + step;
                                while (Vat >= 1.0) {
                                    Vx = Vx - ImgWi;
                                    Vat = Vat - 1.0;
                                }
                            } //~for // dx (this column up)
                            if ((oops & 0xFFF) != 0) break;
                            oops = 0;
                        } //~for // zx (outer loop is horizontal)
                        break;
                    } //~while // (tall>0)
                    if (Mini_Log) if (logy) aLine = aLine + HandyOps.Dec2Log(" ++ ", anim,
                            HandyOps.Dec2Log(" ", tall, HandyOps.Dec2Log("/", wide,
                                    HandyOps.Dec2Log(" *", ppm, HandyOps.Dec2Log(" @ ", Pmap,
                                            HandyOps.Dec2Log(" ", ImgWi, HandyOps.Dec2Log(" ", tops,
                                                    HandyOps.Flt2Log(" st=", step * 16.0, "/16"))))))));
                } //~if //  (::)
                if (Mini_Log) if (aLine != "") System.out.println(aLine);
                if (OpMode == 3) break;
            } //~for // nx (draw them)
            if (why > 13) break;
            why = 0;
            break;
        } //~while              // HandyOps.IffyStr(!logy,"",   // why =
        if ((why > 0) || GoodLog) System.out.println(HandyOps.Dec2Log(" (ShoArt) ", why,
                HandyOps.Flt2Log(" ", ZoomRatio * 256.0, HandyOps.Dec2Log("/256 ", whar,
                        HandyOps.Dec2Log(" ", NumFax, HandyOps.Dec2Log(" ", lino,
                                HandyOps.Dec2Log(" ", ImgWi, HandyOps.Dec2Log(" ", far,
                                        HandyOps.Flt2Log(" ", fudge, HandyOps.Dec2Log(" o", OpMode,
                                                HandyOps.TF2Log("/", StepOne, HandyOps.Fixt8th(" t=", RealTimeNow,
                                                        HandyOps.Dec2Log("=", FakeRealTime, HandyOps.Fixt8th("-", FakeTimeBase,
                                                                HandyOps.Flt2Log(" ", fox, "")
                                                        ))))))))))))));
    } //~ShoArtifax

    /**
     * Converts a park coordinate in meters to the scenery color to display
     * there, usually track pavement or non-track "grass" (or carpet or whatever).
     * The coordinates are given in floating-point park meters, because you get
     * a very precise edge of the track with a resolution something on the order
     * of 3mm (1/8th inch) so that the white line looks credible, even very close
     * to the car.
     *
     * @param optn The bits of this parameter specify options:
     *             +1 gives walls as small negative numbers,
     *             so caller can branch off to build vertical walls;
     *             +2 returns the car color on those locations covered by it;
     *             +4 darkens walls (for map, so they are distinct from white);
     *             +5 ignores CheckerBd (flat track)
     *             +8 returns track values directly, all else as 0
     * @param Vat  The vertical coordinate, in park meters
     * @param Hat  The horizontal coordinate, in park meters
     * @param ink  The minimum width of the white line so it's visible
     *             even at distances (this should be slightly wider than
     *             your pixel width in park meters)
     * @return An 0x00RRGGBB color, or zero if outside the park, or
     * a number -4 to -1 for walls with optn=1.
     */
    public int MapColor(int optn, double Vat, double Hat, double ink) {
        double whom = WhitLnSz,      // +1 rtns walls<0, +8 rtns non-trk=0
                ledge = MyMath.fMin(MyMath.fMax(ink, whom), 1.4),
                wide, deep = 0.0, Mconst = 0.0, Kconst = 0.0;
        int why = 0, thar = 0, whar = 0, Vx = 0, Hx = 0, info = 0, colo = 0,
                tnt = 0, more = 0, rx = MyMath.Trunc8(Vat * 4.0), yx = rx >> 2,
                cx = MyMath.Trunc8(Hat * 4.0), zx = cx >> 2, kx = (rx << 16) + cx, nx,
                bitz = 0,
                Pmap = (yx >> 1) * HalfMap + (zx >> 1) + MapIxBase;
        boolean car2 = (optn & 2) != 0, flat = (optn & 5) == 5, tkonly = (optn & 8) != 0,
                logy = optn < 0, tript = (TripLine > 0) && (yx == TripLine) && GoodLog;
        int[] theImgs = TrakImages;
        int[] theInx = MapIndex;
        rx = yx; // rx,cx now both in meters..
        cx = zx;
        if ((optn & 4) != 0) tnt = -0x333333;
        TmpI = Pmap; // returned cell+ (some callers care)
        optn = optn & 1; // +1 gives walls as small negative numbers,
        while (true) { // ..so caller can branch off to build vertical walls
            why++; // why = 1
            if (rx < 0) break;
            if (rx >= MapHy) break;
            why++; // why = 2
            if (cx < 0) break;
            if (cx >= MapWy) break;
            why++; // why = 3
            if (car2) while (true) { // CarTest, Vcarx,Hcarx,Lcarx,Rcarx,Tcarx,Bcarx,
                car2 = tript;
                whar = (rx << 16) - CarTest + cx; // = 0..5,0..5 if near/on car
                if ((whar & 0x80008000) != 0) break; // above or left (negative)
                why = why + 8; // why = +8 => 15/16
                if (((0x50005 - whar) & 0x80008000) != 0) break; // below or right
                why = why + 8; // why = +16 => 23/24
                car2 = true;
                whom = Vat * Vcarx + Hat;
                if (whom < Lcarx) break;
                why = why + 8; // why = +24 => 31/32
                if (whom > Rcarx) break;
                why = why + 8; // why = +32 => 39/40
                whom = Hat * Hcarx + Vat;
                if (whom < Tcarx) break;
                why = why + 8; // why = +40 => 47/48
                if (whom > Bcarx) break;
                why = why + 8; // why = +48 => 51 "-> F09"
                colo = CarColo;
                if (colo == 0) colo++;
                break;
            } //~while
            if (colo != 0) break; // why = +3 (why = 51)
            why++; // why = +4
            if (theInx == null) break;
            why++; // why = +5
            if (Pmap < 0) break;
            if (Pmap >= theInx.length) break;
            info = theInx[Pmap]; // last use except log
            why++; // why = +6
            if (tkonly) {
                if (info < 0) colo = info;
                else if (info > 0x40000000) if (info < 0x60000000) colo = PavColo;
                break;
            } //~if // why = +6/14/.. // all others default =0
            if (info == 0) { // outside the park..
                if (Wally == 0) break; // still behind last BG
                if (OpMode == 3) if (FrameNo < 2) break; // bad car placement
                if (GroundsColors > 0) break; // no walls if outdoors
                why++; // why = +7
                info++;
            } //~if // inside, so put a wall in front (outside the park)
            else if (info > 0x77000000) info = 4; // (obsolete)
            why++; // why = +7/+8
            if (!flat) if (RectMap > 0) while (true) { // we have paint (see Ad2PntMap)
                if (info >= 0) if (info <= 4) break; // ..but not through walls
                why = why + 0x10000; // why = 1,x // rx,cx both in meters..
                thar = (rx << 3) + (cx >> 5) + RectMap;
                if (thar <= 0) break;
                if (thar >= theInx.length) break;
                why = why + 0x10000; // why = 2,x
                whar = theInx[thar];
                if (whar == 0) break; // no paint near here..
                why = why + 0x10000; // why = 3,x
                if (theImgs == null) {
                    theImgs = TrakImages;
                    if (theImgs == null) break;
                } //~if
                why = why + 0x10000; // why = 4,x
                nx = whar << (cx & 31);
                if (nx >= 0) break; // 'if ((whar<<(cx&31)) >= 0)' failed in T68
                thar = RectMap;
                why = why + 0x30000; // why = +7,x
                while (true) { // look for paint that covers Vat,Hat (3x cont)..
                    why = why + 0x10000; // why = +0(+8n),x
                    thar = thar - 3;
                    why = why + 0x10000; // why = +1(+8n),x
                    if (thar < 0) break;
                    Hx = theInx[thar + 2]; // hi&wi as displayed
                    why = why + 0x10000; // why = +2,x
                    if (Hx == 0) break; // normal exit if none
                    if (Hx < 0) continue; // (deleted)
                    more = theInx[thar]; // = offset of this pix in image +opt
                    Vx = kx - theInx[thar + 1]; // map posn of img => rel coord of pix in img
                    why = why + 0x10000; // why = +3,x
                    if ((Vx & 0x80008000) != 0) continue; // pix outside bounds rect..
                    if ((more & 0x04000000) != 0) { // hi-res (32ppm) image..
                        nx = (MyMath.Trunc8(Vat * 32.0) & 7) << 16; // get low-order fract bits..
                        Vx = (MyMath.Trunc8(Hat * 32.0) & 7) + (Vx << 3) + nx;
                    } //~if // so 8x better res
                    why = why + 0x10000; // why = +4 =12,x
                    if (((Hx - Vx) & 0x80008000) != 0) continue;
                    zx = Vx & 0xFFFF;
                    Vx = Vx >> 16;
                    nx = 0;
                    if ((more & 0x03000000) != 0) switch ((more >> 24) & 3) {
                        case 0: // no rotate..
                            Hx = zx;
                            break;
                        case 1: // rotate img 90 degs c-wise..
                            whar = Vx;
                            Vx = (Hx & 0xFFFF) - zx;
                            Hx = whar;
                            break;
                        case 2: // rotate img 180 degs..
                            Vx = (Hx >> 16) - Vx;
                            Hx = (Hx & 0xFFFF) - zx;
                            break;
                        case 3: // rotate img 90 degs c-c-wise..
                            whar = (Hx >> 16) - Vx;
                            Vx = zx;
                            Hx = whar;
                            break;
                    } //~switch
                    else Hx = zx;
                    more = (more & 0xFFFFFF) + Vx * ImageWide + Hx;
                    why = why + 0x10000; // why = +5,x
                    if (more < 0) break;
                    why = why + 0x10000; // why = +6,x
                    if (theImgs == null) break;
                    if (more >= theImgs.length) break;
                    nx = theImgs[more];
                    if (nx > 0) colo = nx;
                    else if (nx == 0) colo = 0x010101;
                    why = why + 0x10000; // why = +7,x
                    if (colo == 0) continue;
                    why = why | 64;
                    break;
                } //~while // found paint (look for paint that covers)
                break;
            } //~while // (we have paint)
            if (colo != 0) break; // why = 71/115
            if (info > 4) if (info < 0x40000000) info = 0; // (no BG for now)
            colo = GrasColo;
            if (info < 0) { // pavement edge crosses this square..
                // Track edge consists of straight-line segments, mx+ny=k,
                //   starting and ending on cell edges; any vertex within
                //   a cell is bevelled to a straight line in that cell.
                // Every line segment is defined either more horiz or vert
                //   as the greater dimension (45 can be either), so inside
                //   the track in that cell is x+my<k (for m<1) or mx+y.
                // Two bits control whether to multiply m* V or H, and
                //   whether to compare > or <; two fix-point constants
                //   |m|<1.0 and |k|<2047.0 complete the table info.
                // Adjacent cells may share the same formula, which only
                //   defines a polarized abstract line on the map.
                // The white line is nominally 10" (25cm) wide inside the edge,
                //   but never less than one pixel for 1m pixels or smaller.
                //
                Kconst = MyMath.Fix2flt((info & 0x0FFFF800) - (info & 0x10000000), 19);
                Mconst = MyMath.Fix2flt((info & 0x03FF) - (info & 0x400), 10);
                // Horz = (info&0x20000000) ==0;
                // Grtr = (info&0x40000000) ==0;
                if ((info & 0x20000000) == 0) { // H-line..
                    deep = Hat * Mconst + Vat; // in meters if Mcon=0
                    if ((info & 0x40000000) == 0) { // > is in
                        if (deep > Kconst + ledge) colo = PavColo; // gray
                        else if (deep >= Kconst) colo = 0xFFFFFF;
                    } //~if // white line
                    else if (deep + ledge < Kconst) colo = PavColo; // gray
                    else if (deep <= Kconst) colo = 0xFFFFFF;
                } //~if // white
                else {
                    deep = Vat * Mconst + Hat; // V-line..
                    if ((info & 0x40000000) == 0) { // > is in
                        if (deep > Kconst + ledge) colo = PavColo; // gray
                        else if (deep >= Kconst) colo = 0xFFFFFF;
                    } //~if // white
                    else if (deep + ledge < Kconst) colo = PavColo; // gray
                    else if (deep <= Kconst)
                        colo = 0xFFFFFF;
                } //~else
                if (ledge == 0.0) if (colo == 0xFFFFFF) colo = PavColo;
            } //~if // (info<0)
            else if (info > 0x70000000) colo = -2; // artifact (or tree)
            else if (info > 0x40000000) { // grass or pavement
                if (info < 0x60000000) colo = PavColo;
            } //~if // gray
            // else colo = GrasColo;} //~if // (grass)
            else if (info <= 4) { // (white) wall in indoor track..
                if (optn > 0) colo = info | -4; // so caller can build vertical walls
                else if ((info & 1) != 0) colo = CreamWall + tnt; // near-white cream
                else if (info != 4) colo = DarkWall + tnt; // shadowy gray-tan
                else if (PilasterCo == 0) colo = DarkWall + tnt;
                else colo = PilasterCo;
            } //~if // (wall in indoor track)
            else if (optn > 0) colo = -1; // (no BG for now)
            else colo = DarkWall + tnt;
            if (CheckerBd > 0) if (!flat) if (((rx ^ cx) & CheckerBd) != 0) {
                if (colo == PavColo) colo = PavDk;
                else if (colo == GrasColo) colo = GrasDk;
            } //~if
            why++; // why = 8/9
            break;
        } //~while               // Pmap = (rx>>1)*HalfMap+(cx>>1)+MapIxBase;
        if (GoodLog) if (tript) { // if (rx>0)
            if (RectMap == 0) logy = (colo != 0);
            else if (NoisyMap) if (why > 0x40000) logy = true;
            if (((colo + 1) & 255) == 0) colo = colo & 0x7F7F9E; // show TripLine in blue..
            else colo = colo & -2 | 254;
        } //~if
        if (logy) {
            System.out.println(HandyOps.Dec2Log(" (MapCol) ", rx,
                    HandyOps.Dec2Log(" ", cx, HandyOps.Dec2Log(" ", Pmap,
                            HandyOps.Hex2Log(" = x", info, 8, HandyOps.Colo2Log(" -> ", colo,
                                    HandyOps.Int2Log(" .. ", whar, HandyOps.Dec2Log("/", thar,
                                            HandyOps.Dec2Log("_", RectMap, HandyOps.Flt2Log(" ", Kconst,
                                                    HandyOps.Flt2Log("/", Mconst, HandyOps.Flt2Log(" ", ledge,
                                                            HandyOps.Flt2Log(" ", deep, HandyOps.Flt2Log("/", whom,
                                                                    HandyOps.Flt2Log("/", ink, HandyOps.Int2Log("\n   = ", why,    // why =
                                                                            HandyOps.TF2Log(" c2=", car2, "")))))))))))))))));
            TempStr = "";
        } //~if
        return colo;
    } //~MapColor

    /**
     * Draw on-screen an image portion from TrakImages.
     *
     * @param rx   The top pixel row of the image on the screen
     * @param cx   The left pixel column
     * @param tall The image height in pixels
     * @param wide The image width in pixels
     * @param here The image offset in TrakImages, in ints/pix from top-left
     * @param colo The border color, -1 omits
     */
    public void SeeOnScrnPaint(int rx, int cx, int tall, int wide, int here,
                               int colo) { // show image portion on-screen..
        int thar, lino, posn, info; //  here = SeePaintImgP,
        //  rx = SeePaintTopL>>16, cx = SeePaintTopL&0xFFFF,
        //  tall = (SeePaintSize>>16)-1, wide = (SeePaintSize&0xFFFF)-1,
        //  int more = SceneWide, didit = 0; int[] myPix = myScreen;
        int[] theImgs = TrakImages;
        if (theImgs == null) return;
        System.out.println(HandyOps.Dec2Log(" (SeePint) ", rx,
                HandyOps.Dec2Log("/", cx, HandyOps.Dec2Log(" ", tall,
                        HandyOps.Dec2Log("/", wide, HandyOps.Dec2Log(" ", here, ""))))));
        if (rx < 0) return;
        if (cx < 0) return;
        if (here < 0) return;
        if (here >= theImgs.length) return;
        for (lino = 0; lino <= tall; lino++) {
            thar = here;
            here = here + ImageWide;
            for (posn = 0; posn <= wide; posn++) {
                if (thar < 0) break;
                if (thar >= theImgs.length) break;
                info = theImgs[thar];
                thar++;
                if (info < 0) {
                    if (colo < 0) continue;
                    info = 0;
                } //~if
                PokePixel(info, rx + lino, cx + posn);
            }
        } //~for
        if (colo < 0) return;
        if (rx == 0) return;
        if (rx + tall + 2 > ImHi) return;
        if (cx + wide + 2 > WinWi) return;
        DrawLine(MarinBlue, rx - 1, cx - 1, rx - 1, cx + wide + 1); // frame it..
        DrawLine(MarinBlue, rx + tall + 1, cx - 1, rx + tall + 1, cx + wide + 1);
        DrawLine(MarinBlue, rx, cx - 1, rx + tall, cx - 1);
        DrawLine(MarinBlue, rx, cx + wide + 1, rx + tall, cx + wide + 1);
    } //~SeeOnScrnPaint

    private void BuildFrame() { // no early return, always logs
        double ledge, Vat = Vposn, Hat = Hposn, fHafIm = ((double) FltWi) * 0.5 + 1.0,
                GridTall = (double) HalfTall, GridWide = (double) HalfMap,
                Vbase, Hbase, Vpx, Hpx, ftmp; // Mconst, Kconst,
        int xx, zx, info, Vmap, Hmap, Pmap, chekt, robe, // whar,
                bitz = -1, optn = 0,
                Lww = 0, Rww = 0, LwL = 0, RwL = 0, Mtrk = 0, colo = 0, far = 0,
                rx = 0, cx = 0, kx = 0, dx = 0, nx = 0, yx = 0, doit = 0,
                step = 0, PxWi = 0, why = 0, tBase = 0, here = 0, thar = nPixels;
        double deep = 0.0, Vinc = 0.0, Hinc = 0.0, Vstp = 0.0, Hstp = 0.0;
        boolean logy = false, seen = false, solidGry = false; // Horz, Grtr;
        String aWord = "", myDash = "", LefDash = "", OopsLog = "";
        int[] theImgs = TrakImages;
        int[] theInx = MapIndex;
        int[] myPix = myScreen;
        FakeRealTime = FakeRealTime + FrameTime; // steadily increments
        FrameNo++;
        Wally = 0;
        PixScale = 0;
        if (LookFrame + FrameNo == 0) LookFrame = 0;
        while (Facing < 0.0) Facing = Facing + 360.0;
        while (Facing > 360.0) Facing = Facing - 360.0;
        //*
        if ((1 > 0) || (ZooMapDim != 0)) {
            kx = (MyMath.Trunc8(Facing + Facing) + 22) / 45; // if (1>0) {
            aWord = CompNames[kx & 15];
            myDash = HandyOps.Flt2Log(" ", Vposn, HandyOps.Flt2Log("S ", Hposn,
                    HandyOps.Flt2Log("E -- ", Facing, HandyOps.Dec2Log(aWord, GasBrake, ""))));
            if (DroppedFrame > 0) LefDash = " (-" + DroppedFrame + ") ";
            else LefDash = " ";
            DroppedFrame = 0;
            LefDash = " " + HandyOps.Dec2Log("", SteerWhee,
                    HandyOps.Dec2Log(LefDash, FrameNo, HandyOps.IffyStr(LookFrame == 0, "",
                            HandyOps.Dec2Log(" (", LookFrame, ")"))));
        } //~if
        //*/ //MARKERPOINT
        //*
        System.out.println(HandyOps.Dec2Log("(..BF..) ", FrameNo, // (**frozen format**)
                HandyOps.Dec2Log(" ", NuData, HandyOps.Int2Log(" ", ZooMapDim, " [ s="
                        + HandyOps.Dec2Log(LefDash + HandyOps.IffyStr(OpMode == 3, "# ! ", "# / ")
                        + myDash + " =g ] ", ImHi - 1, HandyOps.Int2Log(" ", TripLine,
                        HandyOps.Flt2Log(HandyOps.IffyStr(SimSpedFixt, " [Fx ", " [v="), Velocity,
                                HandyOps.Dec2Log("] ", optn, HandyOps.PosTime(" @ ")))))))));
        //*/    //MARKERPOINT
        if (Log_Draw) OopsLog = " %";
        for (cx = 0; cx <= ImWi - 1; cx++) PrioRaster[cx] = 0;
        Vmap = MyMath.Trunc8(Vposn); // = current posn in park meters (2x map grid)
        Hmap = MyMath.Trunc8(Hposn);
        while (true) { // (once through, exit early if error)
            NuData = 0;
            why++; // why = 1
            if (myPix == null) break; // (only early exit)
            why++; // why = 2 // if (DoScenery||ShowMap) {
            MyMath.Angle2cart(Facing); // angles are in degrees, not radians
            Hstp = MyMath.Sine; // sin grows to right, step E to edge
            Vstp = -MyMath.Cose; // cos shrinks down (sin^2+cos^2 = 1m) step S
            // MyMath.Angle2cart(Facing+90.0); // this now points left-to-right,
            Vinc = Hstp; // -MyMath.Cose; // for stepping across the view screen
            Hinc = -Vstp; // MyMath.Sine;

            ZooMapTopL = 0;                    // calc edges of close-up map..
            yx = ZooMapDim >> 16; // close-up size, in park meters
            zx = ZooMapDim & 0xFFF; // park size: MapHy,MapWy; Vmap=Vposn
            if (ShowMap) if (DoCloseUp) if (ZooMapDim != 0) while (true) {
                for (nx = RasterMap.length - 1; nx >= 0; nx += -1) RasterMap[nx] = 0.0;
                // ZMD=32,32 ZSf=3 ZB=224,642 ZSc=16. ZW=0.2 ZT=8,16
                if (zx >= MapWy) if (yx >= MapHy) break; // no offset needed
                if (MyMath.Trunc8(TurnRadius * 2.0) + 4 > yx) step = 2;
                else step = 1; // in park meters, how much car to show (at edge)
                kx = (step << 16) + kx; // was: =MyMath.Trunc8((Facing+22.5)/45.0)
                switch (kx & 15) {
                    case 0:
                    case 1:
                    case 15: // facing north..
                        rx = Vmap + step - yx; // now top of close-up, in park meters
                        cx = Hmap - (zx >> 1); // left edge of close-up, ditto
                        break;
                    case 2: // facing north-east..
                        rx = Vmap + step - yx;
                        cx = MyMath.iMin(Hmap - step, MapWide - zx);
                        break;
                    case 3:
                    case 4:
                    case 5: // facing east..
                        rx = Vmap - (yx >> 1);
                        cx = MyMath.iMin(Hmap - step, MapWide - zx);
                        break;
                    case 6: // facing south-east..
                        rx = MyMath.iMin(Vmap - step, MapTall - yx);
                        cx = MyMath.iMin(Hmap - step, MapWide - zx);
                        break;
                    case 7:
                    case 8:
                    case 9: // facing south..
                        rx = MyMath.iMin(Vmap - step, MapTall - yx);
                        cx = Hmap - (zx >> 1);
                        break;
                    case 10: // facing south-west..
                        rx = MyMath.iMin(Vmap - step, MapTall - yx);
                        cx = Hmap + step - zx;
                        break;
                    case 11:
                    case 12:
                    case 13: // facing west..
                        rx = Vmap - (yx >> 1);
                        cx = Hmap + step - zx;
                        break;
                    case 14: // facing north-west..
                        rx = Vmap + step - yx;
                        cx = Hmap + step - zx;
                        break;
                } //~switch
                bitz = (rx << 16) + cx; // for log
                if (rx > 0) if (rx + yx > MapHy) rx = MapHy - yx;
                if (rx < 0) rx = 0;
                if (cx > 0) if (cx + zx > MapWy) cx = MapWy - zx;
                if (cx < 0) cx = 0;
                if (rx + cx == 0) break; // offset is 0
                ZooMapTopL = (rx << 16) + cx; // in park meters
                break;
            } //~while // (DoCloseUp) // ZooMapShf cvts c-u(pix) <-> meters

            // calc: Vcarx, Hcarx, Lcarx, Rcarx, Tcarx, Bcarx, CarTest..
            //   Vat,Hat is in car if:  CarTest<(Vat,Hat)<(CarTest+5,5)
            //     && Lcarx<Vat*Vcarx+Hat<Rcarx && Tcarx<Hat*Hcarx+Vat<Bcarx
            logy = Log_Draw && (FrameNo < 4);
            Vbase = Vposn + Vstp + Vinc; // right front corner of car
            Hbase = Hposn + Hstp + Hinc;
            Vpx = Vposn - (Vstp * 3.0 + Vinc); // left rear corner
            Hpx = Hposn - (Hstp * 3.0 + Hinc);
            deep = MyMath.fMin(MyMath.fMin(Vbase - 2.0 * Vinc, Vbase),
                    MyMath.fMin(Vpx + Vinc * 2.0, Vpx)); // northmost corner
            ledge = MyMath.fMin(MyMath.fMin(Hbase - 2.0 * Hinc, Hbase),
                    MyMath.fMin(Hpx + Hinc * 2.0, Hpx)); // westmost corner
            CarTest = (MyMath.Trunc8(deep) << 16) + MyMath.Trunc8(ledge); // ...........
            if (logy) System.out.println(HandyOps.Int2Log(" (CarTst) ", CarTest,
                    HandyOps.Flt2Log(" ", Facing, HandyOps.Flt2Log(" @ ", Vposn,
                            HandyOps.Flt2Log("/", Hposn, HandyOps.Flt2Log(" + ", Vstp,
                                    HandyOps.Flt2Log("/", Hstp, HandyOps.Flt2Log(" > ", Vinc,
                                            HandyOps.Flt2Log("/", Hinc, HandyOps.Flt2Log(" -> ", Vbase,
                                                    HandyOps.Flt2Log("/", Hbase, HandyOps.Flt2Log(" .. ", Vpx,
                                                            HandyOps.Flt2Log("/", Hpx, aWord)))))))))))));
            if (Log_Draw) aWord = "";
            // Northward facing, calc Lc,Rc,Tc,Bc, same (Vstp,Hstp)?
            // Southward facing, calc Pc,Dc,Ac,Zc, all numbers =N
            // Westward facing, calc Sc,Nc,Wc,Ec, same (Vstp,Hstp)?
            // Eastward facing, calc Cc,Oc,Yc,Fc, all numbers =W

            if (MyMath.fAbs(Vstp) > MyMath.fAbs(Hstp)) { // more N-S than E-W..
                if (Vstp < 0.0) { // (car aimed north)..
                    LineSlope(Vpx, Hpx, -Vstp, Hstp, logy, "Lc="); // using left-rear corner
                    Lcarx = Hoffm - 0.1; // = Hstp/Vstp*Vpx+Hpx (Hstp=0 if directly N)
                    Vcarx = Voffm;
                    LineSlope(Vbase, Hbase, 0.0, 0.0, logy, "Rc="); // using right-front
                    Rcarx = Hoffm + 0.1;
                    LineSlope(Hbase, Vbase, Hinc, -Vinc, logy, "Tc="); // also as north
                    Tcarx = Hoffm - 0.1;                    // ..(just flip V <-> H)
                    Hcarx = Voffm;
                    LineSlope(Hpx, Vpx, 0.0, 0.0, logy, "Bc="); // using left-rear as S
                    Bcarx = Hoffm + 0.1;
                } //~if // (aimed north)
                else { // (car aimed south, same as N, but sin/cos both neg'd)..
                    LineSlope(Vbase, Hbase, Vstp, -Hstp, logy, "Pc="); // using right-front
                    Lcarx = Hoffm - 0.1;          // ..cuz passenger side is facing west
                    Vcarx = Voffm;
                    LineSlope(Vpx, Hpx, 0.0, 0.0, logy, "Dc="); // driver side is east
                    Rcarx = Hoffm + 0.1;
                    LineSlope(Hpx, Vpx, -Hinc, Vinc, logy, "Ac="); // using left-rear as N
                    Tcarx = Hoffm - 0.1;
                    Hcarx = Voffm;
                    LineSlope(Hbase, Vbase, 0.0, 0.0, logy, "Zc="); // right-front as S
                    Bcarx = Hoffm + 0.1;
                }
            } //~if // (aimed south) (more N-S)
            else if (Hstp < 0.0) { // more E-W than N-S (car aimed west)..
                LineSlope(Hpx, Vpx, -Hstp, Vstp, logy, "Sc="); // left-rear faces S
                Bcarx = Hoffm + 0.1;
                Hcarx = Voffm;
                LineSlope(Hbase, Vbase, 0.0, 0.0, logy, "Nc="); // other corner faces N
                Tcarx = Hoffm - 0.1;
                LineSlope(Vbase, Hbase, -Vinc, Hinc, logy, "Wc="); // also west
                Lcarx = Hoffm - 0.1;
                Vcarx = Voffm;
                LineSlope(Vpx, Hpx, 0.0, 0.0, logy, "Ec="); // left-rear is east
                Rcarx = Hoffm + 0.1;
            } //~if // (aimed west)
            else { // (car aimed east, same as W, but sin/cos both neg'd)
                LineSlope(Hbase, Vbase, Hstp, -Vstp, logy, "Cc="); // right-front as S
                Bcarx = Hoffm + 0.1;
                Hcarx = Voffm;
                LineSlope(Hpx, Vpx, 0.0, 0.0, logy, "Oc="); // other corner faces N
                Tcarx = Hoffm - 0.1;
                LineSlope(Vpx, Hpx, Vinc, -Hinc, logy, "Yc="); // back also west
                Lcarx = Hoffm - 0.1;
                Vcarx = Voffm;
                LineSlope(Vbase, Hbase, 0.0, 0.0, logy, "Fc="); // right-front is east
                Rcarx = Hoffm + 0.1;
            } //~else // (aimed east, more E-W)
// ZMD=32,32 ZSf=3 ZB=224,642 ZSc=16. ZW=0.2 ZT=8,16
// (car) +1 90. +D 0./1. +W 1./0. CT=23,29 Vc=0./0. Tc=22.9/25.1 Lc=28.9/33.1 0
//   .. 1,4 8,31 ZT=8,31 23./29. 23./29.
            if (Mini_Log) if (!logy) System.out.println(HandyOps.Dec2Log("// (car) #", FrameNo,
                    HandyOps.Flt2Log(" ", Facing, HandyOps.Flt2Log(" +D ", Vstp,
                            HandyOps.Flt2Log("/", Hstp, HandyOps.Flt2Log(" +W ", Vinc,
                                    HandyOps.Flt2Log("/", Hinc, HandyOps.Int2Log(" CT=", CarTest,
                                            HandyOps.Int2Log(" [", Left_X, HandyOps.Int2Log("/", Rite_X,
                                                    HandyOps.PosTime("] @ ")))))))))));
            // seen = false; // if (DoScenery) {
            if (theInx == null) break; // why = 2
            why++; // why = 3
            if (theInx.length < 8) break;
            PxWi = ImageWide; // theInx[0]&0xFFFF; // width of images frame
            // iBase = theInx[3]; // map dimensions (unneed, fixed at 256x200)
            // eBase = theInx[2]; // 1st edge spec (obsolete, now in index)
            // MapIxBase = theInx[2]; // front of 2x2m map (InitIndx got it)
            tBase = theInx[1]; // front of texture map
            step = (0xFF00 - 0x3300) / (ImHaf >> 1);
            why = 4;
            if (TopCloseView == 0) kx = MapHy;
            else if (TopCloseView <= MapHy) kx = TopCloseView - 1;
            else kx = MapHy;
            for (rx = ImHi - 1; rx >= 0; rx += -1) { // fill defaults..................................
                logy = false;
                if (rx > 0) if (rx == TripLine) logy = Log_Draw || GoodLog; // GoodLog=T
                // if (rx<4) logy = Log_Draw; else
                // if (rx>ImHi-5) logy = Log_Draw;
                // else if (rx>ImHaf+2) logy = false;
                // else if (rx>ImHaf-8) logy = Log_Draw;
                // else if ((rx&15)==0) logy = Log_Draw;
                if (InWalls) bitz = 0xCC9966; // darkened wall color, in case overshot
                else if (rx < ImHaf) { // outdoor sky..
                    bitz = 0xFFFFFF - ((colo >> 1) & 0xFF00) - ((colo & 0xFF00) << 8);
                    zx = colo + step;
                    if (zx < 0xFF00) colo = zx;
                    else colo = 0xFFFF;
                } //~if // aiming for 3399FF in highest sky
                else bitz = 0xFF00; // green // 0xFFFFCC99; // not dirt
                if (1 > 0) if (rx > ImHi - 1) bitz = 0x333300; // dk.brown
                if (ZooMapDim == 0) dx = -1;
                else dx = rx - (ZooMapBase >> 16); // <0 if above close-up view (in c-u pix)
                // ZMD=32,32 ZSf=3 ZB=224,642 ZSc=16. ZW=0.2 ZT=8,16
                if (dx >= 0) { // meters, close-up res..
                    Vat = MyMath.Fix2flt(dx + (ZooMapTopL >> (16 - ZooMapShf)), ZooMapShf);
                    // ZooMapShf cvts c-u(pix) <-> meters; Vat is pk meters, close-up res
                    if (optn > 1) if (Vat + 1.6 > Vposn) if (Vat - 1.6 < Vposn) logy = Log_Draw;
                } //~if
                if (logy) System.out.println(HandyOps.Dec2Log("   (BF..) ", rx,
                        HandyOps.Colo2Log(" ", bitz, HandyOps.Hex2Log(" ", colo, 8,
                                HandyOps.Dec2Log(" ", WinWi, HandyOps.Dec2Log(" ", kx,
                                        HandyOps.Dec2Log("=", MapHy, HandyOps.Dec2Log("/", MapWy,
                                                HandyOps.Dec2Log(" ", dx, HandyOps.Dec2Log("/", nx,
                                                        HandyOps.TF2Log(" ", InWalls, HandyOps.PosTime(" @ "))))))))))));
                for (cx = WinWi - 1; cx >= 0; cx += -1) {
                    info = 0; // default black separator, letterbox around map(s)
                    if (logy) info = 0x0000FF; // (TripLine)
                    zx = cx - 2 - ImWi;
                    deep = 0.0;
                    far = 0;
                    if (logy) if (rx == zx) far = 0x80000000;
                    if (cx < ImWi) {
                        if (logy) if ((rx == cx) || (cx < 4) || (cx > ImWi - 3))
                            System.out.println(HandyOps.Dec2Log("   ..  .. ", rx,
                                    HandyOps.Dec2Log("/", cx, HandyOps.Colo2Log(" => ", bitz,
                                            HandyOps.Colo2Log("/", info, "")))));
                        info = bitz;
                    } //~if
                    else if (ShowMap) if (zx >= 0) {
                        if (logy) if (far == 0) if (zx < 4) far = 0x80000000; // ||(cx>WinWi-8)
                        if (rx < kx) { // map shown 1 pix = 1m
                            if (WhitLnSz > 0.0)
                                deep = 1.4; // 1m pixels, allow extra for diagonal white line
                            Vat = MyMath.Fix2flt(rx, 0);
                            Hat = MyMath.Fix2flt(zx, 0); // in park meters
                            if (zx < MapWy) // zx = (rx>>1)*HalfMap+(zx>>1)+MapIxBase;
                                if (logy) if (far == 0) if (zx > MapWy - 4) far = 0x80000000;
                        } //~if
                        else if (dx >= 0) {     // ZMTL is in close-up pix coords..
                            Hat = MyMath.Fix2flt(zx + ((ZooMapTopL & 0xFFF) << ZooMapShf),
                                    ZooMapShf); // in park meters, close-up resolution
                            deep = ZooMapWhLn;
                        } //~if // ZooMapShf cvts c-u(pix) <-> meters
                        else zx = -1;
                        if (logy) if (far == 0) {
                            // if ((zx&15)==0) far = 0x80000000; else
                            if (optn > 1) if (Vat + 1.6 > Vposn) if (Vat - 1.6 < Vposn)
                                if (Hat + 1.6 > Hposn) if (Hat - 1.6 < Hposn) far = 0x80000000;
                        } //~if
                        if (far < 0) System.out.println(HandyOps.Dec2Log("   . .. . ", rx,
                                HandyOps.Flt2Log(" ", Vat, HandyOps.Flt2Log("/", Hat,
                                        HandyOps.Dec2Log(" ", zx, HandyOps.Flt2Log(" ", deep, ""))))));
                        if (zx >= 0) info = MyMath.iMax(MapColor(far + 6, Vat, Hat, deep), 0);
                    } //~if
                    if (myPix == null) break;                       // (in side map)
                    thar--;
                    if (thar < 0) break;
                    if (thar < myPix.length)
                        myPix[thar] = info;
                }
            } //~for //~for (cx) (rx: fill defaults)
            far = 0;
            if (ShowMap) { // add trail of breadcrumbs...................
                thar = MyMath.iMin(nCrumbs - 1, Crummy);
                for (thar = thar; thar >= 0; thar += -1) {
                    info = BreadCrumbs[thar & Crummy];
                    if (info == 0) continue;
                    rx = info >> 16;
                    if (TopCloseView > 0) if (rx >= TopCloseView) continue;
                    cx = (info & 0xFFF) + ImWi + 2;
                    if (PeekPixel(rx, cx) != CarColo) // car avatar already there
                        PokePixel(ArtiColo, rx, cx);
                } //~for
                if (ShoTrkTstPts) { // show stay-in-track points as small blue "x"s..
                    if (Left_X != 0) {
                        rx = Left_X >> 16;
                        cx = Left_X & 0xFFF;
                        PokePixel(MarinBlue, rx, cx);
                        PokePixel(MarinBlue, rx - 1, cx - 1);
                        PokePixel(MarinBlue, rx - 1, cx + 1);
                        PokePixel(MarinBlue, rx + 1, cx - 1);
                        PokePixel(MarinBlue, rx + 1, cx + 1);
                    } //~if
                    if (Rite_X != 0) {
                        rx = Rite_X >> 16;
                        cx = Rite_X & 0xFFF;
                        PokePixel(MarinBlue, rx, cx);
                        PokePixel(MarinBlue, rx - 1, cx - 1);
                        PokePixel(MarinBlue, rx - 1, cx + 1);
                        PokePixel(MarinBlue, rx + 1, cx - 1);
                        PokePixel(MarinBlue, rx + 1, cx + 1);
                    }
                } //~if
                zx = MyMath.iMin(kx + 8, MapTall - 8); // MapHy
                for (thar = 0; thar <= zx; thar += 8) { // some yellow tics..
                    PokePixel(0xFFFF00, thar, ImWi + 2);          // ..along the top & left..
                    if (thar == 0) continue;
                    if ((thar & 31) != 0) continue;
                    PokePixel(0xFFFF00, thar, ImWi + 1);
                    PokePixel(0xFFFF00, thar, ImWi);
                } //~for
                zx = MyMath.iMin(MapWy + 8, MapWide - 8);
                for (thar = 8; thar <= zx; thar += 8) {
                    cx = thar + ImWi + 2;
                    PokePixel(0xFFFF00, 0, cx);
                    if ((thar & 31) != 0) continue;
                    PokePixel(0xFFFF00, 1, cx);
                    PokePixel(0xFFFF00, 2, cx);
                } //~for
                info = ZoomMapCoord(true, Vposn, Hposn);
                rx = info >> 16;
                cx = info & 0xFFF;    // car avatar sb already there..
                if (info > 0) if (PeekPixel(rx, cx) == CarColo) {
                    DrawLine(MarinBlue, rx - 1, cx, rx + 1, cx);
                    DrawLine(MarinBlue, rx, cx - 1, rx, cx + 1);
                }
            } //~if // (info) (ShowMap)
            //*
            if (1 > 0) { // 1=12 to draw it
                if (ShoClikGrid) DrawGrid();
                LabelScene(myDash, ImHi - 4, ImWi - 8, -1);
                myDash = HandyOps.Flt2Log(" ", Velocity * dFtime, " "); // (5/fps) dFtime=5
                LabelScene(myDash, ImHi - 4, myDash.length() * 2 + SteerMid, 0xFF9999);
                info = -1;
                if (StepOne) info = 0x66FFFF; // cyan for 1-step
                else if (OpMode == 2) info = 0x66FF00; // green for real-time
                else if (OpMode > 2) info = 0xFF0099; // red for crashed
                LabelScene(LefDash, ImHi - 4, -12, info);
                LefDash = HandyOps.Fixt8th(" ", RealTimeNow, " ");
                LabelScene(LefDash, SceneTall - 4, LefDash.length() * 2 + ImMid, 0xFF9900);
                myDash = myDash + " t=" + LefDash;
            } //~if // (1>0)
            //*/    //MARKERPOINT
            // Vmap = MyMath.Trunc8(Vposn); // = current posn in park meters (2x grid)
            // Hmap = MyMath.Trunc8(Hposn);
            info = -1;
            if (Vmap > 3) if (Hmap > 3) if (Vmap < MapTall - 4) if (Hmap < MapWide - 4) info = 0;
            if (info != 0) {
                if (OpMode < 3) System.out.println(HandyOps.Dec2Log("** Crashed ** ", Vmap,
                        HandyOps.Dec2Log("/", Hmap, "")));
                SimStep(4);
            } //~if // (CrashMe)
            else { // if (DoScenery) // draw scenery back-to-front....................
                Pmap = (Vmap >> 1) * HalfMap + (Hmap >> 1) + MapIxBase;
                if (Pmap > 0) if (theInx != null) if (Pmap < theInx.length)
                    info = theInx[Pmap];
                if (OpMode < 3) if (info >= 0)
                    if (!SimSpedFixt) if ((info & 0x60000000) != 0x40000000) SimStep(6);

                //*
                System.out.println(HandyOps.Dec2Log(HandyOps.IffyStr(OpMode == 3, "(DoSc $$) ",
                        "(DoSc) "), 1, HandyOps.Dec2Log(" ", Vmap,
                        HandyOps.Dec2Log("/", Hmap, HandyOps.Hex2Log(" = ", info, 8,
                                HandyOps.Dec2Log(" ", LookFrame, HandyOps.Dec2Log(" ", TripLine,
                                        HandyOps.PosTime(" @ "))))))));
                //*/ //MARKERPOINT

                //
                // The car is at (Vposn,Hposn), facing (init'ly NW) -> Facing (Fa)
                // The screen is ImWi pixels wide, which imaged at distance dx
                //   is dx/fZoom meters wide, and the map position of the left edge
                //   of the image plane at distance dx is -90 degrees to the left
                //   (=Fc-90 in degrees C-wise from North) to map point Vat/Hat
                //   ¥ = (VIc-dx*cos(Lp)/(2*fZoom),HIc+dx*sin(Lp)/(2*fZoom)).
                // Stepping from the left, each pixel adds dx*cos(Lp)/(ImWi*fZoom)
                //   to V and subtracts dx*sin(Lp)/(ImWi*fZoom) from H.
                // We calculate Vbase = -cos(Fa)-cos(Lp)/(2*fZoom) and Hbase sim'ly,
                //   then each row, add dx (=deep)*Vbase to Vposn for the V part of Lx;
                //   and step Vstp = dx*cos(Lp)/(ImWi*fZoom) and Hstp sim'ly.
                //
                // MyMath.Angle2cart(Facing); // angles are in degrees, not radians
                // Hstp = MyMath.Sine; // sin grows to right, step E to edge
                // Vstp = -MyMath.Cose; // cos shrinks down (sin^2+cos^2 = 1m) step S
                // MyMath.Angle2cart(Facing-90.0); // this now points left-to-right,
                // Vinc = MyMath.Cose; // for stepping across the view screen
                // Hinc = -MyMath.Sine;
                // deep = 256.0;
                // Vbase = Vstp*256.0; // about double the park width (in 2m grid locs),
                // Hbase = Hstp*256.0; // so guaranteed to be outside the park
                doit = 0;
                rx = RangeRow[255] - 1; // at horizon, mid-screen
                for (dx = 255; dx >= -ImHaf; dx += -1) {    // convert depth to raster line number..
                    if (rx > ImHi - 1) break; // normal exit at bottom of screen
                    if (dx < 0) break;
                    robe = RangeRow[dx & 255]; // dx: nominal depth in park 25cm units
                    // far = far&0xFFFFFF|(robe<<24);
                    // if (Vscale>0) robe = (robe-ImHaf)*Vscale+ImHaf;
                    if (robe <= rx) continue;
                    step = ImWi;     // 8x cont, 4x brk..
                    while (rx < robe) {
                        rx++;
                        far = 1;
                        if (rx == TripLine) {
                            if (LookFrame == 0) far = far | 0x80000000;
                            System.out.println(HandyOps.Dec2Log("---TripLin=", TripLine,
                                    HandyOps.Dec2Log("/", LookFrame, "---")));
                            seen = false;
                        }
                        cx = rx - ImHaf; // RowRange is bottom half of screen only // ImHaf=240
                        if (cx > ImHaf - 1) break;
                        doit = RowRange[cx];
                        deep = ((double) doit) * 0.03125; // RR: m*16 (6cm), deep: 2m
                        Vbase = deep * Vstp + Vposn * 0.5; // current center of the view at this dx,
                        Hbase = deep * Hstp + Hposn * 0.5; // ..in grid coords (2m)
                        ftmp = deep * 0.125 * WiZoom; // WiZoom = Dzoom*32/FltWi = 16/(ImWi*fZoom)
                        Vpx = Vinc * ftmp; // step size across image (in grid/pix)..
                        Hpx = Hinc * ftmp; // .. = {deep/(ImWi/2)/zoom}*(sin|cos)
                        Voffm = Vpx * fHafIm; // fHafIm = ((double)FltWi)/2+1.0 (+1 for extra +Vpx)
                        Hoffm = Hpx * fHafIm;
                        Vat = Vbase - Voffm; // start here at left edge of screen (in grid=2m)
                        Hat = Hbase - Hoffm;
                        Voffm = Vbase + Voffm; // (end here, for bounds check)
                        Hoffm = Hbase + Hoffm;
                        cx = cx << 2;
                        if (DoCloseUp) if (ShowMap) if (ZooMapDim != 0) if (cx >= 0)
                            if (RasterMap != null) if (cx < RasterMap.length - 4) {
                                RasterMap[cx + 3] = Hoffm; // right end of raster (in grid=2m)
                                RasterMap[cx + 2] = Voffm;
                                RasterMap[cx + 1] = Hat;   // left end
                                RasterMap[cx] = Vat;
                            } //~if
                        if (InWalls) { // calc shade for distant walls..
                            cx = 0;
                            doit = doit >> 3; // = distance in half-meters (=deep*4)
                            if (doit > 63) Darken = 0x3F3F3F;
                            else if (doit > 0) { // ZoomPix=ImWi*50/Zoom35.. ¥ I don't believe this ¥¥
                                cx = ZoomPix * 2 / doit; // = pix/meter (12cm @ 1:8), = baseboard
                                Darken = doit * 0x10101;
                            } //~if
                            else Darken = 0;
                            Wally = (cx << 24) + CreamWall;
                        } //~if // (InWalls) ..near-white cream
                        else doit = 0;
                        thar = rx * WinWi - 1;
                        if (CheckerBd == 0) if (solidGry) if (rx > ImHaf + 32) {
                            if (Log_Draw) if (optn > 1)
                                OopsLog = OopsLog + HandyOps.Dec2Log(" Sr", rx, "");
                            for (cx = 0; cx <= ImWi - 1; cx++) {
                                thar++;
                                if (myPix != null) if (thar > 0) if (thar < myPix.length)
                                    myPix[thar] = PavColo;
                            } //~for
                            continue;
                        } //~if // (solidGry)
                        if (ShowMap) {
                            if (Mtrk != 0) if (ZooMapDim != 0) { // find view trapezoid corners
                                if (LwL == 0) {
                                    LwL = ZoomMapCoord(true, Vat + Vat, Hat + Hat);
                                    if (LwL != 0) seen = false;
                                } //~if
                                if (RwL == 0) {
                                    RwL = ZoomMapCoord(true, Voffm + Voffm, Hoffm + Hoffm);
                                    if (RwL != 0) seen = false;
                                }
                            } //~if // (Mtrk)
                            /*
                            if (rx == ImHi - 2 - 1) { // 1=12
                                if (ZooMapDim != 0) { // draw view trapezoid..
                                    zx = ZoomMapCoord(false, MyMath.Fix2flt(rx, 0), 1.0); // sb = Lww
                                    Lww = ZoomMapCoord(true, Vat + Vat, Hat + Hat); // should be visible
                                    Rww = ZoomMapCoord(true, Voffm + Voffm, Hoffm + Hoffm);
                                    if (LwL > 0) if (Lww > 0)
                                        DrawLine(MarinBlue + Tintx, Lww >> 16, Lww & 0xFFF, LwL >> 16, LwL & 0xFFF);
                                    if (RwL > 0) if (Rww > 0)
                                        DrawLine(MarinBlue + Tintx, Rww >> 16, Rww & 0xFFF, RwL >> 16, RwL & 0xFFF);
                                    if (Lww > 0) if (Rww > 0) // MarinBlue=0x0099FF
                                        DrawLine(MarinBlue + Tintx, Lww >> 16, Lww & 0xFFF, Rww >> 16, Rww & 0xFFF);
                                } //~if
                                zx = MyMath.Trunc8(Vat + Vat);
                                cx = MyMath.Trunc8(Voffm + Voffm);
                                if (TopCloseView > 0) {
                                    if (zx >= TopCloseView) cx = 0;
                                    else if (cx >= TopCloseView) cx = 0;
                                } //~if
                                if (cx > 0) DrawLine(MarinBlue + Tintx * 2, zx,
                                        MyMath.Trunc8(Hat + Hat) + ImWi + 2, cx,
                                        MyMath.Trunc8(Hoffm + Hoffm) + ImWi + 2);
                            }
                            //*/    //MARKERPOINT
                        } //~if // (1) (ShowMap)
                        if (VuEdge == 0.0) if (Mtrk != 0) {
                            VuEdge = MyMath.aTan0(Hoffm + Hoffm - Hposn, Vposn - Voffm - Voffm) - Facing;
                            while (VuEdge > 180.0) VuEdge = VuEdge - 360.0;
                            while (VuEdge + 180.0 < 0.0) VuEdge = VuEdge + 360.0;
                            if (VuEdge < 0.0) VuEdge = -VuEdge;
                            seen = false;
                        } //~if
                        cx = 0; // log bounds check..
                        if (MyMath.fMin(Vat, Voffm) > GridTall) cx = 8;
                        else if (MyMath.fMax(Vat, Voffm) < 0.0) cx = 4;
                        if (MyMath.fMin(Hat, Hoffm) > GridWide) cx = cx + 2;
                        else if (MyMath.fMax(Hat, Hoffm) < 0.0) cx++;
                        if (cx != 0) seen = false;
                        // if (TrakNoPix) if (rx<ImHaf+4) cx = cx|16;
                        seen = true; // if (TrakNoPix) if (rx<ImHaf+4) cont_inue;
                        solidGry = true;
                        info = 0;
                        if ((cx & 15) == 0) for (cx = 0; cx <= ImWi - 1; cx++) { // .. repeat until off right..
                            bitz = PrioRaster[cx];
                            thar++;
                            Vat = Vat + Vpx;
                            Hat = Hat + Hpx;
                            if (WhitLnSz > 0) ftmp = Vpx + Hpx;
                            else ftmp = 0.0;
                            info = 0;
                            if (Vat < 0.0) info++;
                            if (Hat < 0.0) info++;
                            if (Vat >= GridTall) info++;
                            if (Hat >= GridWide) info++;
                            if (info > 0) { // often outside park at first, so..
                                solidGry = false;
                                step--;
                                if (Log_Draw) if (optn > 1) if (step > 0) if (step < 3) {
                                    aWord = " Or" + rx;
                                    OopsLog = OopsLog + aWord;
                                } //~if // (step)
                                continue;
                            } //~if // (often outside park)
                            // Vmap = MyMath.Trunc8(Vat+Vat);
                            // Hmap = MyMath.Trunc8(Hat+Hat);             // TripLine: far<0
                            colo = MapColor(far, Vat + Vat, Hat + Hat, ftmp); // ODD(far)
                            PrioRaster[cx] = TmpI;
                            if (colo == 0) {
                            } // continue;                     // (BuildFrame)
                            else if ((colo & -4) == -4) { // indoor (white) wall..
                                yx = colo;
                                kx = Wally >> 24;
                                here = thar;
                                if (colo == -4) {
                                    if (PilasterCo == 0) info = DarkWall;
                                    else info = PilasterCo;
                                    kx = 0;
                                } //~if
                                else if ((colo & 1) == 0) { // what about BackWall?
                                    info = DarkWall; // shadowy gray-tan
                                    kx = 0;
                                } //~if // might be door, so no baseboard
                                else info = CreamWall; // ..near-white cream
                                if (Wally == 0) if (colo != -4) Wally = info; // seen a wall
                                info = info - Darken;
                                for (zx = rx; zx >= 0; zx += -1) {
                                    colo = info; // darken baseboard edge..
                                    kx--;
                                    if (kx == 0) if ((((colo & 0xCCCCCC) + 0xCCCCCC) & 0x1111110)
                                            == 0x1111110) colo = colo - 0x333333;
                                    if (here > 0) if (myPix != null) if (here < myPix.length)
                                        myPix[here] = colo;
                                    if (TmpI == bitz) if (kx < 0) break; // already did this wall
                                    if (GoodLog || Log_Draw) if (zx > 0) if (zx == TripLine)
                                        System.out.println(HandyOps.Dec2Log("  (wall) ", kx,
                                                HandyOps.Dec2Log(" ", zx, HandyOps.Dec2Log(" ", rx,
                                                        HandyOps.Dec2Log("/", cx, HandyOps.Colo2Log(" = ", colo,
                                                                HandyOps.Dec2Log(" ", yx, HandyOps.Int2Log(" ", Wally,
                                                                        HandyOps.Int2Log(" ", far, "")))))))));
                                    here = here - WinWi;
                                } //~for
                                continue;
                            } //~if // colo = yx;} //~if // (wall in indoor track)
                            if (colo < 0) continue; // (obsolete) artifact
                            if (Mtrk == 0) {
                                Mtrk = rx;
                                seen = false;
                            } //~if
                            if (PavColo != GrasColo) if (WhitLnSz > 0.0) {
                                zx = 0;
                                if (cx > 0) zx = PeekPixel(rx, cx - 1);
                                if ((colo == PavColo) || (colo == PavDk)) {
                                    // if (Mtrk==0) Mtrk = cx;
                                    if (zx == GrasColo) colo = 0xEEEEEE; // grass next to paved..
                                    else if (zx == GrasDk) colo = 0xEEEEEE;
                                    zx = PeekPixel(rx - 1, cx); // if grass above paved..
                                    if (zx == GrasColo) colo = 0xEEEEEE; // ..near-white to replace gray
                                    else if (zx == GrasDk) colo = 0xEEEEEE;
                                } //~if // (PavColo)
                                // else if (chekt>0) colo = PavDk;
                                else if ((colo == GrasColo) || (colo == GrasDk)) {
                                    if (zx == PavColo) colo = 0xEEEEEE; // paved next to grass..
                                    else if (zx == PavDk) colo = 0xEEEEEE;
                                    // else if (chekt>0) colo = GrasDk; // dk.grn
                                    solidGry = false;
                                } //~if // (GrasColo)
                                else solidGry = false;
                            } //~if
                            else solidGry = false;
                            if (rx == TripLine) { // show TripLine in blue..
                                if (((colo + 1) & 255) == 0) colo = colo & 0x7F7F77;
                                else colo = colo | 255;
                            } //~if
                            if (colo > 0) if (thar > 0) if (myPix != null) if (thar < myPix.length)
                                myPix[thar] = colo;
                        }
                    }
                }
            } //~for(cx)~while(rx)~for(dx)~else
            if (!TrakNoPix) if (TrakImages != null) if (NumFax > 0) ShoArtifax();
            if (Mini_Log) System.out.println(HandyOps.Dec2Log(" (StWh) ", rx,
                    HandyOps.Dec2Log(" ", ShoSteer, HandyOps.Dec2Log("/", SteerWhee,
                            HandyOps.Int2Log(" [", Lww, HandyOps.Int2Log("/", Rww, // botm scrn corners
                                    HandyOps.Int2Log(" ", LwL, HandyOps.Int2Log("/", RwL, // blu top scrn edges
                                            HandyOps.Dec2Log("] ", TopCloseView, HandyOps.Int2Log(" ", ZooMapTopL,
                                                    HandyOps.Int2Log(" ", far, HandyOps.TF2Log(" ", TrakImages != null,
                                                            HandyOps.Flt2Log(" ", VuEdge, HandyOps.Dec2Log(" ", NumFax,
                                                                    HandyOps.PosTime(" @ ")))))))))))))));
            // if (why != 8) break; // if scenery went bad, still do steering wheel..
            DrawSteerWheel(ShoSteer, false, false);
            why = 0;
            if (OpMode == 3) DrawRedX();
            if (SeePaintTopL > 0) SeeOnScrnPaint(SeePaintTopL >> 16, SeePaintTopL & 0xFFFF,
                    SeePaintSize >> 16, SeePaintSize & 0xFFFF, SeePaintImgP, MarinBlue);
            why--; // why = -1
            break;
        } //~while // (once through)
        if (GoodLog) {
            if (OopsLog.length() == 2) OopsLog = "";
            OopsLog = OopsLog + HandyOps.PosTime(" @ ") + " @";
            myDash = myDash + " = ";
            System.out.println(HandyOps.Dec2Log(" (BildFram) ", FrameNo, // why =
                    HandyOps.Dec2Log(myDash, why, OopsLog)));
        }
    } //~BuildFrame

    private void InTrackIt() { // calc new posn & aim to stay centered in track
        boolean doit = true, logy = Mini_Log;
        int // Vat = MyMath.Trunc8(Vposn), Hat = MyMath.Trunc8(Hposn),
                whom = MapColor(8, Vposn, Hposn, 0), // =0 if off-track
                nx = 0, ledge = 0, ridge = 0, more = 0, info = 0, optn = 0, why = 0;
        String aStr, aLine = "";
        double Vstp = 0.0, Hstp = 0.0, Vinc = 0.0, Hinc = 0.0, nuly = 0.0,
                Atmp = 0.0, Ztmp = 0.0, Vat = 0.0, Hat = 0.0, Ccon = 0.0, Tcon = 0.0,
                Mconst = 0.0, Kconst = 0.0, good = 0.0,
                Vlft = Vposn, Hlft = Hposn, Vrit = Vposn, Hrit = Hposn,
                aim = Facing, avg = AverageAim, ftmp;
        while (true) { // find edges..
            why++; // why = 1                 // Grtr: (whom&0x40000000)==0 // > is in
            while (aim < 0.0) aim = aim + 360.0;  // Horz: (whom&0x20000000)==0
            while (aim > 360.0) aim = aim - 360.0;
            nuly = aim;
            good = aim;
            if (whom < 0) { // on edge, which?
                if ((whom & 0x20000000) == 0) { // more H than V..
                    if (aim < 180.0) { // car facing east..
                        if ((whom & 0x40000000) != 0) {
                            ridge = whom;
                            whom = -whom;
                        } //~if
                        else ledge = whom;
                    } //~if
                    else if ((whom & 0x40000000) == 0) { // else facing west..
                        ridge = whom;
                        whom = -whom;
                    } //~if
                    else ledge = whom;
                } //~if
                else if ((aim < 90.0) || (aim > 270.0)) { // car facing north..
                    if ((whom & 0x40000000) != 0) {
                        ridge = whom;
                        whom = -whom;
                    } //~if
                    else ledge = whom;
                } //~if
                else if ((whom & 0x40000000) == 0) { // else facing south..
                    ridge = whom;
                    whom = -whom;
                } //~if // whom>0: starting on right edge
                else ledge = whom;
            } //~if // whom<0: starting on left edge
            else if (whom > 0) whom = 0; // whom=0: not starting on any edge
            else doit = false; // not starting on track
            MyMath.Angle2cart(aim + 90.0);
            Hstp = MyMath.Sine;
            Vstp = -MyMath.Cose;
            for (nx = 5; nx >= -5; nx += -1) { // stepping out 5m to each side, +5m if off-track
                if (ridge <= 0) { // right edge still unseen..
                    Vat = Vrit + Vstp;
                    Hat = Hrit + Hstp;
                    info = MapColor(8, Vat, Hat, 0);
                    if (info == 0) {
                        if ((whom == 0) && !doit) { // was/still off-track,
                            Vrit = Vat; // keep looking..
                            Hrit = Hat;
                        } //~if
                        else ridge = ridge & 0x7FFFFFFF;
                    } //~if // off-track, prior is best
                    else if ((whom == 0) && !doit) { // was off-track, this is left edge..
                        Vlft = Vat + Vstp;
                        Hlft = Hat + Hstp;
                        Vrit = Vlft;
                        Hrit = Hlft;
                        if (info > 0) ledge = 0; // overshot edge
                        else if (MapColor(5, Vlft, Hlft, 0) == PavColo) ledge = info; // inside
                        else ledge = info & 0x7FFFFFFF;
                        whom--;
                    } //~if
                    else if (info > 0) { // (inside track)
                        Vrit = Vat;
                        Hrit = Hat;
                    } //~if
                    else if (((info - ledge) & 0x7FFFFFFF) == 0) { // edge = left (ignore)..
                        Vrit = Vat;
                        Hrit = Hat;
                    } //~if
                    else if (MapColor(5, Vrit, Hrit, 0) == PavColo) { // still inside..
                        ridge = info;
                        Vrit = Vat;
                        Hrit = Hat;
                    } //~if
                    else if (ridge == 0) ridge = info & 0x7FFFFFFF; // just past edge
                    else ridge = ridge & 0x7FFFFFFF;
                } //~if // prior was best edge
                if (ledge <= 0) { // left edge still unseen..
                    Vat = Vlft - Vstp;
                    Hat = Hlft - Hstp;
                    more = MapColor(8, Vat, Hat, 0);
                    if (more == 0) if ((whom == 0) && !doit) { // was/still off-track,
                        Vlft = Vat; // keep looking..
                        Hlft = Hat;
                    } //~if
                    else ledge = ledge & 0x7FFFFFFF; // off-track, prior is best
                    else if ((whom == 0) && !doit) { // was off-track, this is right edge..
                        Vrit = Vat - Vstp;
                        Hrit = Hat - Hstp;
                        Vlft = Vrit;
                        Hlft = Hrit;
                        if (more > 0) ridge = 0; // overshot edge
                        else if (MapColor(5, Vrit, Hrit, 0) == PavColo) ridge = more; // inside
                        else ridge = more & 0x7FFFFFFF;
                        whom++;
                    } //~if
                    else if (more > 0) { // (inside track)
                        Vlft = Vat;
                        Hlft = Hat;
                    } //~if
                    else if (((more - ridge) & 0x7FFFFFFF) == 0) { // edge = rite (ignore)..
                        Vlft = Vat;
                        Hlft = Hat;
                    } //~if
                    else if (MapColor(5, Vlft, Hlft, 0) == PavColo) { // still inside..
                        ledge = more;
                        Vlft = Vat;
                        Hlft = Hat;
                    } //~if
                    else if (ledge == 0) ledge = more & 0x7FFFFFFF; // just past edge
                    else ledge = ledge & 0x7FFFFFFF;
                } //~if // prior was best edge
                if (logy) System.out.println(HandyOps.Dec2Log("  .:. ", nx,
                        HandyOps.Flt2Log(" L=", Vlft, HandyOps.Flt2Log("/", Hlft,
                                HandyOps.Int2Log(" ", more, HandyOps.Flt2Log(" R=", Vrit,
                                        HandyOps.Flt2Log("/", Hrit, HandyOps.Int2Log(" ", info,
                                                HandyOps.IffyStr(whom > 0, " + ", HandyOps.IffyStr(whom < 0, " - ", " = "))
                                                        + HandyOps.IffyStr(ledge < 0, "l", HandyOps.IffyStr(ledge > 0, "L", "."))
                                                        + HandyOps.IffyStr(ridge < 0, "_r",
                                                        HandyOps.IffyStr(ridge > 0, "_R", "_,"))))))))));
                if (ledge > 0) if (ridge > 0) break; // stop when both sides found
                if (nx > 0) continue;
                if (whom == 0) break; // still no edges seen @ 5m
                if (doit) break;
            } //~for // good enough (stepping out 5m to each side)
            if (ShowMap) { // show farthest test points regardless..
                Left_X = ZoomMapCoord(true, Vlft, Hlft);
                Rite_X = ZoomMapCoord(true, Vrit, Hrit);
            } //~if
            if (!doit) if (whom == 0) { // no track, no edges ever seen,
                SimStep(7); // so crash it (CrashMe)
                // NuData++; // (SS did)
                break;
            } //~if // why = 1   // else if no edges seen, don't place car..
            nx = 0;
            if (!doit) nx = 4;
            else if (ledge != 0) nx++;
            else if (ridge != 0) nx++;
            if (nx > 0) { // can/should center the car in the track..
                Vat = (Vlft + Vrit) * 0.5;
                Hat = (Hlft + Hrit) * 0.5;
                if (nx > 3) { // was off-track..
                    Vposn = Vat;
                    Hposn = Hat;
                } //~if
                else if (MyMath.fAbs(Vposn - Vat) + MyMath.fAbs(Hposn - Hat) > 1.5) {
                    Vposn = (Vat + Vposn) * 0.5; // in track but not close..
                    Hposn = (Hat + Hposn) * 0.5;
                }
            } //~if
            why++; // why = 2
            if (ledge == 0) {
                if (ridge == 0) break; // why = 2: didn't find any edges, don't steer
                why = 5; // saw only right edge
                optn = 4;                                                   // o=04
                if ((ridge & 0x20000000) != 0) optn++;
            } //~if // (N-S)            // o=05
            else if (ridge == 0) {
                why = 6; // saw only left edge
                optn = 4;                                                   // o=04
                if ((ledge & 0x20000000) != 0) optn++;
            } //~if // (N-S)            // o=05
            if (ledge != 0) {
                Kconst = MyMath.Fix2flt((ledge & 0x0FFFF800) - (ledge & 0x10000000), 19);
                Mconst = MyMath.Fix2flt((ledge & 0x03FF) - (ledge & 0x400), 10);
            } //~if
            else {
                Kconst = MyMath.Fix2flt((ridge & 0x0FFFF800) - (ridge & 0x10000000), 19);
                Mconst = MyMath.Fix2flt((ridge & 0x03FF) - (ridge & 0x400), 10);
            } //~else
            if (logy) aLine = HandyOps.Flt2Log(" K=", Kconst,       // aLine: " M="
                    HandyOps.Flt2Log(" M=", Mconst, "; "));
            why++; // why = 3/6/7
            if (optn == 0) { // why = 3: saw both edges, calculate midline..
                Ccon = MyMath.Fix2flt((ridge & 0x0FFFF800) - (ridge & 0x10000000), 19);
                Tcon = MyMath.Fix2flt((ridge & 0x03FF) - (ridge & 0x400), 10);
                if (logy) aLine = HandyOps.Flt2Log(aLine, Ccon,           // aLine..
                        HandyOps.Flt2Log("/", Tcon, " "));
                if (((ledge ^ ridge) & 0x20000000) != 0) { // incompatible sides..
                    Atmp = MyMath.fAbs(Tcon);
                    Ztmp = MyMath.fAbs(Mconst);       // why = 3 (don't steer) cuz..
                    if (Atmp < 0.125) if (Ztmp < 0.125) break; // ..both too close to axis
                    why++; // why = 4
                    if (Atmp > Ztmp) { // convert right edge before take average..
                        if ((ledge & 0x20000000) != 0) optn = 18; // left Vert     // o=12
                        else optn = 16;                                       // o=10
                        // change: [x*t+y = c] => [y*n+x = z]; n = 1/t, z = c/t
                        Ztmp = 1.0 / Tcon;
                        Kconst = (Ztmp * Ccon + Kconst) * 0.5;
                        Mconst = (Mconst + Ztmp) * 0.5;
                        why++;
                    } //~if // why = 5
                    else { // convert left edge..
                        if ((ridge & 0x20000000) != 0) optn = 34; // right Vert    // o=22
                        else optn = 32;                                       // o=20
                        Ztmp = 1.0 / Mconst;
                        Kconst = (Ztmp * Kconst + Ccon) * 0.5;
                        Mconst = (Tcon + Ztmp) * 0.5;
                    }
                } //~if // why = 4
                else { // both sides aimed the same compass quadrant (+45)..
                    if ((ledge & 0x20000000) != 0) optn = 50; // both Vert       // o=32
                    else optn = 48; // both Horz                            // o=30
                    Kconst = (Kconst + Ccon) * 0.5;
                    Mconst = (Tcon + Mconst) * 0.5;
                } //~else // why = 5
                if (ledge != 0) if (ridge != 0) if (((ledge - ridge) & 0x7FFFFFFF) != 0) //
                    optn = optn + 8;
            } //~if // why = 3/4/5; // else why = 6/7
            // turn equation into angle & point near posn (nuly <- Ztmp <- Atmp)..
            if ((optn & 3) == 0) { // preferred aim is Horiz..
                why = why + 5; // why = 8/9/10/11/12 => 24/../28
                Atmp = MyMath.aTan0(1.0, Mconst);
            } //~if // Mc>0 is 1st quadrant
            else // preferred aim is Vert.. // why = 3/4/5/6/7 => 19/../23
                Atmp = MyMath.aTan0(Mconst, 1.0); // ..(Mc>0 is still 1st quadrant)
            ftmp = aim + 90.0 - Atmp; // aim: 0..360, Atmp: -45..135; ftmp: -45..495
            if (((optn >> 1) & optn & 0x10) != 0) // both edges in same quadrant..
                optn = optn | 0x80;
            else if ((optn & 0x30) == 0) // one edge only, probly no change..
                optn = optn | 0x80;
            else if (MyMath.fAbs(Tcon) + MyMath.fAbs(Mconst) > 1.8)
                optn = optn | 0x80; // or else both edges near/@ 45, so
            if ((optn & 0x80) != 0) { // ..probably good..
                if (ftmp > 405.0) Ztmp = Atmp; // ftmp=90: Atmp=aim, =270: Atmp=aim-180
                else if (ftmp > 315.0) optn = optn | 0x40; // ftmp=0/180: Atmp worthless..
                else if (ftmp > 225.0) Ztmp = Atmp + 180.0;  // ..(450=360+90) +/-45: good
                else if (ftmp < 45.0) optn = optn | 0x40;
                else if (ftmp < 135.0) Ztmp = Atmp;
                else optn = optn | 0x40;
            } //~if
            else optn = optn | 0x40;
            if ((optn & 0x70) == 0) good = Ztmp; // (one edge) 1st alt if straight NG
            else if ((optn & 0x40) == 0) { // else ignore divergent edges
                if ((ledge | ridge) < 0) good = Ztmp; // some edge is oblique or far
                else nuly = Ztmp;
            } //~if
            if (logy) {
                if (good != aim) aStr = HandyOps.Flt2Log(" G=", good, " = ");
                else aStr = " = ";
                if (nuly != aim) aStr = HandyOps.Flt2Log(" N=", nuly, aStr);
                System.out.println(HandyOps.Hex2Log("  :::: x", optn, 2,
                        HandyOps.Flt2Log(" ", nuly, HandyOps.Flt2Log(" ", aim,
                                HandyOps.Flt2Log(" a=", Atmp, HandyOps.Flt2Log("/", Mconst,
                                        HandyOps.Flt2Log(" ", ftmp, HandyOps.Flt2Log(" z=", Ztmp,
                                                HandyOps.Dec2Log(aStr, why, HandyOps.IffyStr(nx == 0, "",
                                                        HandyOps.Dec2Log(" *", nx, "*")))))))))));
                nx = 0;
            } //~if
            why = why | 16; // why = 19/../28
            break;
        } //~while // (find edges)
        if (why > 1) { // not steering (or already did), but still moving..
            Vat = Vposn;
            Hat = Hposn;
            ftmp = nuly - aim;
            while (true) {
                info = 0;
                MyMath.Angle2cart(nuly); // Facing is in degrees, not radians
                Atmp = MyMath.Sine;
                Ztmp = MyMath.Cose;
                Vinc = -Ztmp * Velocity;
                Hinc = Atmp * Velocity;
                Vposn = Vat + Vinc;
                Hposn = Hat + Hinc;
                if (!doit) { // omit look-ahead 2nd pass thru..
                    Vinc = 0.0;
                    Hinc = 0.0;
                } //~if
                if (why > 16) if (why < 127) // look ahead an extra step..
                    info = MapColor(5, Vposn + Vinc, Hposn + Hinc, 0); // omit paint
                if (logy) System.out.println(HandyOps.Dec2Log("  =.= ", why,
                        HandyOps.Flt2Log(" ", nuly, HandyOps.Flt2Log("/", aim,
                                HandyOps.Flt2Log(" => ", Vposn, HandyOps.Flt2Log("/", Hposn,
                                        HandyOps.Flt2Log(" v=", Velocity, HandyOps.Flt2Log(" a=", Atmp,
                                                HandyOps.Flt2Log(" z=", Ztmp, HandyOps.Flt2Log(" ", fSweeper,
                                                        HandyOps.Int2Log(" ", info, HandyOps.IffyStr(nx == 0, "",
                                                                HandyOps.Dec2Log(" *", nx, "*")))))))))))));
                nx = 0;
                if (info == 0) break; // set posn & get out (no test)
                if (info == PavColo) break; // posn is good
                why = why + 32;
                if (why < 64) { // failed why<32, try for 1-edge..
                    if (good != aim) { // got 1st alt to try..
                        nuly = good;
                        continue;
                    } //~if
                    why = why + 32;
                } //~if
                if (why < 96) { // failed why<64 or didn't have 1st alt, try +sweep..
                    nuly = ftmp + aim;
                    if (ftmp > 0.0) nuly = nuly + fSweeper;
                    else if (ftmp + 180.0 < 0.0) nuly = nuly + fSweeper;
                    else nuly = nuly - fSweeper;
                } //~if
                else if (why < 127) // failed why<96, try -sweep..
                    nuly = MidAngle(avg, nuly);
                else if (doit) { // failed why<128, try again not looking ahead..
                    nuly = ftmp + aim;
                    why = why - 128;
                    doit = false;
                } //~if
                else nuly = aim;
            } //~while // keep aim (& fail, but out)
            Facing = nuly; //
            why = -why;
        } //~if
        if (!logy) return; // logy=Mini_Log=F [Last_Minit=T]   // aLine: " M="
        if (aLine == "") aLine = " ";
        System.out.println(HandyOps.Dec2Log("(InTrax) ", FrameNo,
                HandyOps.Flt2Log(" ", aim, HandyOps.Hex2Log(" ", optn, 2,
                        HandyOps.Flt2Log(" => ", Vposn, HandyOps.Flt2Log("/", Hposn,
                                HandyOps.Flt2Log(" ", Facing, HandyOps.Int2Log(aLine, whom,
                                        HandyOps.TF2Log(" ", SimInTrak, HandyOps.Flt2Log(" ", avg,
                                                HandyOps.Dec2Log(" = ", why, "")))))))))));        // why =
        if ((optn & 4) == 0) aLine = HandyOps.Flt2Log(" k=", Kconst,
                HandyOps.Flt2Log("/", Mconst, ""));
        else aLine = "";
        System.out.println(HandyOps.Int2Log("  [", Left_X, HandyOps.Int2Log("/", Rite_X,
                HandyOps.Int2Log("] ", ledge, HandyOps.Int2Log("/", ridge,
                        HandyOps.Flt2Log(" ", Vlft, HandyOps.Flt2Log("/", Hlft,
                                HandyOps.Flt2Log("|", Vrit, HandyOps.Flt2Log("/", Hrit,
                                        HandyOps.Flt2Log(" +", Vstp, HandyOps.Flt2Log("/", Hstp,
                                                HandyOps.Flt2Log(" a=", Atmp,
                                                        HandyOps.Flt2Log(" z=", Ztmp, aLine)))))))))))));
    } //~InTrackIt

    private void MoveCar() { // called once for each frame (not nec'ly sync'sly)
        boolean logy = Log_Log || Log_Draw; // =F
        int here, thar = -1, prio = SteerWhee, info = SpecWheel - prio,
                prev = GasBrake, more = SpecGas - prev, nx = -1, why = 0;
        double d1 = 0.0, d2 = 0.0, d3 = 0.0, d4 = 0.0, d5 = 0.0,
                t1 = 0.0, t2 = 0.0, t3 = 0.0, doing = 0.0, test = 0.0,
                MPF = fMinSpeed * fFtime, // meters per frame; fFtime=0.2, fMinSpeed=4.0
                oV = Velocity;
        // " (MovCar) FrameNo OpMode +NuData s: SpecWheel=prio fSteer
        //   g: SpecGas=prev fSpeed Velocity/oV doing f: Facing = why
        while (true) {
            why++; // why = 1
            Left_X = 0;
            Rite_X = 0;
            if (OpMode == 3) {
                GasBrake = 0;        // GasBrake is servo angle speed setting in degs
                fSpeed = 0.0;        // fSpeed is the nominal meters/frame (m/s/fps)
                Velocity = 0.0;      // Velocity is simulated, after physics
                break;
            } //~if
            why++; // why = 2
            if (ShowMap) if (nCrumbs == 0) {
                here = (MyMath.Trunc8(Vposn) << 16) + MyMath.Trunc8(Hposn);
                BreadCrumbs[nCrumbs & Crummy] = here;
                nCrumbs++;
            } //~if
            if ((SpecGas | prev) == 0) { // fSpeed doesn't track perfectly thru 0..
                GasBrake = 0;
                fSpeed = 0.0;
                Velocity = 0.0;
                nx = 0;
            } //~if
            if ((SpecWheel | prio) == 0) { // neither does fSteer..
                SteerWhee = 0;
                ShoSteer = 0;
                fSteer = 0.0;
                if (nx == 0) break;
            } //~if // why = 2 (stopped)
            // if ((info|more) !=0) while (true) {
            why++; // why = 3
            if (RampServos) if (ServoStepRate > 0) { // ServoStepRate=0
                if (info > ServoStepRate) info = ServoStepRate;
                else if (info + ServoStepRate < 0) info = -ServoStepRate;
                if (more > ServoStepRate) more = ServoStepRate;
                else if (more + ServoStepRate < 0) more = -ServoStepRate;
            } //~if
            GasBrake = GasBrake + more; // (only) new setting (as ramped)
            if (SimSpedFixt) {                                      // =FixedSpeed
                if (SpecGas <= 0) fSpeed = 0.0; // (5/fps)
                else fSpeed = MPF; // fFtime=0.2, fMinSpeed=4.0
                doing = fSpeed; // (5/fps) so doing=0.8 = 4m/s = 8mph park
                Velocity = doing;
            } //~if // V is in meters/frame (after physics)
            // else if (GasBrake<MinESCact) fSpeed = 0.0;
            else fSpeed = MyMath.Fix2flt(prev + GasBrake, 1) * fGratio * fFtime;
            // if (info==0) break; // why = 3
            why++; // why = 4
            SteerWhee = SteerWhee + info; // new setting (as ramped)
            fSteer = MyMath.Fix2flt(prio + SteerWhee, 1); // avg new+old
            info = MyMath.iAbs(SteerWhee);      // scale it to TapedWheel..
            if (info != 0) for (nx = (TapedWheel[0] & 255) - 1; nx >= 0; nx += -1) {
                if (nx > 0) if (((TapedWheel[nx & 15] >> 8) & 255) > info) continue;
                if (SteerWhee < 0) info = -nx;
                else info = nx;
                break;
            } //~for // always exits here, with info set
            ShoSteer = info;
            // break;} //~while
            if (!SimSpedFixt) {                                     // !FixedSpeed
                why++; // why = 5
                t1 = fSpeed; // fSpeed = (prev+GasBrake)*fGratio*fFtime/2 (ramp up)
                if (t1 < MPF) {
                    why++; // why = 6
                    fSpeed = fSpeed * 0.5;
                    if (fSpeed <= 0.1) {
                        Velocity = 0.0;
                        fSpeed = 0.0;
                    } //~if
                    if (Velocity == 0.0) break;
                } //~if // why = 6
                // fMaxSpeed = fGratio*Float4int(MaxESCact), // =13m/s
                doing = MyMath.fMax(MyMath.fMin(fSpeed, fMaxSpeed), MaxRspeed);
                doing = doing - Velocity; // how much we need to accelerate in m/s // (5/fps)
                t2 = doing * fTime4mass; // (m/s)*(a*s/m) -> a, a=servo step angle
                // if (t2>fFtime) doing = doing*fFtime; // (I don't believe this)
                test = Velocity;
                Velocity = Velocity + doing; // (5/fps)
                if (doing == 0.0) doing = Velocity; // doing is in meters/frame
                else doing = (Velocity + test) * 0.5;
            } //~if // (!SimSpedFixt)
            why = why + 3; // why = 7/8/9
            if (OpMode == 3) break;
            if (OpMode == 0) break;         // SloMotion needs to apply also to Velocity
            // if (SloMotion>0.0) if (SloMotion<1.0) doing = doing*SloMotion; // (5/fps)
            if (!SimInTrak) {
                why = why + 3; // why = 10/11/12
                t3 = doing * fSteer * fTurn4m; // = Facing change as fn(travel) T4m=2/(TR*pi)
                test = Facing + t3; // = new Facing for this frame // @(5/fps)
                d1 = (Facing + test) * 0.5;
                MyMath.Angle2cart(d1); // Facing is in degrees, not radians
                d2 = MyMath.Sine;
                d3 = MyMath.Cose;
                Facing = test; // @(5/fps)
                Hposn = d2 * doing + Hposn;
                Vposn = Vposn - d3 * doing;
            } //~if // (!SimInTrak)
            else InTrackIt(); // logs if Mini_Log
            AverageAim = MidAngle(Facing, AverageAim);
            NuData++;
            if (ShowMap) {
                here = (MyMath.Trunc8(Vposn) << 16) + MyMath.Trunc8(Hposn);
                thar = (BreadCrumbs[(nCrumbs - 1) & Crummy] + 0x20002 - here) & 0xFFC0FFC;
                if (thar == 0) break; // adjacent crumbs must be in dif't grid cells
                BreadCrumbs[nCrumbs & Crummy] = here;
                nCrumbs++;
            } //~if
            break;
        } //~while // why = 7/8/9/10/11/12
        if (logy || Mini_Log) if (((SpecGas | SpecWheel) != 0) || !Moved) {
            System.out.println(HandyOps.Dec2Log(" (MovCar) ", FrameNo,
                    HandyOps.Dec2Log(HandyOps.IffyStr(SimSpedFixt, " Fs o", " o"), OpMode,
                            HandyOps.Dec2Log(" +", NuData, HandyOps.Dec2Log(" s:", SpecWheel,
                                    HandyOps.Dec2Log("=", prio, HandyOps.Flt2Log(" ", fSteer,
                                            HandyOps.Dec2Log(" g:", SpecGas, HandyOps.Dec2Log("=", prev,
                                                    HandyOps.Flt2Log(" ", fSpeed, HandyOps.Flt2Log(" ", Velocity,
                                                            HandyOps.Flt2Log("/", oV, HandyOps.Flt2Log(" ", doing,
                                                                    HandyOps.Flt2Log(" f:", Facing, HandyOps.Dec2Log(" = ", why, // why =
                                                                            HandyOps.PosTime(HandyOps.IffyStr(SimInTrak, " SiT @ ",
                                                                                    " @ ")))))))))))))))));
            if (logy) System.out.println(HandyOps.Flt2Log("      ", fTurn4m,
                    HandyOps.Flt2Log(" ", t1,   // HandyOps.Flt2Log(" (",SloMotion,")"
                            HandyOps.Flt2Log(" ", t2, HandyOps.Flt2Log(" ", t3,
                                    HandyOps.Flt2Log(" / ", d1, HandyOps.Flt2Log(" ", d2,
                                            HandyOps.Flt2Log("/", d3, ""))))))));
            Moved = true;
        }
    } //~MoveCar

    /**
     * Essentially the same as NextFrame in fly2cam.FlyCamera.
     * Camera properties are independently defined, so they are verified.
     * In single-step mode SimStep(1) the scene is updated as needed and
     * returned immediately; in real-time mode SimStep(2) the FrameTime
     * is compared to the system clock, and if greater, excess frames are
     * processed and discarded, or if less, processing stalls.
     *
     * @param rose   The number of image rows
     * @param colz   The number of image columns
     * @param pixels The array to fill with Bayer8-coded pixels
     * @return True if success, otherwise false
     */
    public boolean GetSimFrame(int rose, int colz, byte[] pixels) {
        int rx, cx = 0, info = 0, here = 0, thar = 0;
        byte aByte;                 // from GetCameraIm -> boolean NextFrame
        int[] myPix = myScreen;
        if (Log_Log || Mini_Log) if ((NuData > 0) || (FrameNo < 2) || SimBusy || DarkOnce)
            System.out.println(HandyOps.Dec2Log(" (GetSmFram) ", FrameNo,
                    HandyOps.Dec2Log(" o", OpMode, HandyOps.Dec2Log(" ", NuData,
                            HandyOps.TF2Log(" ", SimBusy, HandyOps.Flt2Log(" {", Vposn,
                                    HandyOps.Flt2Log(" ", Hposn, HandyOps.Flt2Log(" ", Facing,
                                            HandyOps.PosTime(HandyOps.IffyStr(DarkOnce, "}*D @ ", "} @ "))))))))));
        if (!SimBusy) {
            SimBusy = true;
            try {
                if (OpMode == 2) {
                    cx = HandyOps.TimeSecs(true);
                    if (NextFrUpdate > 0) if (ProcessingTime > 0) while (true) {
                        if (cx > NextFrUpdate + FrameTime + ProcessingTime) {
                            DroppedFrame++;
                            MoveCar();
                            rx = HandyOps.TimeSecs(true) - cx;
                            ProcessingTime = (ProcessingTime + rx - cx) >> 1;
                            NextFrUpdate = NextFrUpdate + FrameTime;
                            cx = rx;
                        } //~if
                        else if (ProcessingTime + cx > NextFrUpdate) break;
                        else cx = HandyOps.TimeSecs(true);
                    } //~while
                    MoveCar();
                    rx = HandyOps.TimeSecs(true) - cx;
                    if (ProcessingTime == 0) ProcessingTime = rx;
                    else ProcessingTime = (ProcessingTime + rx) >> 1;
                    if (NextFrUpdate == 0) NextFrUpdate = cx + rx;
                    else if (ProcessingTime + ProcessingTime > FrameTime) // FT too fast..
                        NextFrUpdate = cx + rx + FrameTime;
                    else NextFrUpdate = NextFrUpdate + FrameTime - ProcessingTime;
                } //~if
                else if (OpMode == 1) MoveCar(); // could crash, so..
                if (NuData > 0) BuildFrame();
            } catch (Exception ex) {
                System.out.println(ex);
            }
            if (OpMode == 1) OpMode = 0;
            SimBusy = false;
            StepOne = false;
        }
        if (rose != ImHi) return false;
        if (colz != WinWi) return false;
        if (pixels == null) return false;
        if (myPix == null) return false;
        for (rx = 0; rx <= ImHi - 1; rx++) {                   // BayerTile=1 = RG/GB..
            for (cx = 0; cx <= WinWi - 1; cx++) {
                if (myPix == null) break;
                if (here < 0) break;
                if (!DarkOnce) if (here < myPix.length)
                    info = myPix[here]; // each int: xxRRGGBB
                here++;
                aByte = (byte) ((info >> 8) & 255); // green
                if (pixels == null) break;
                if (thar < 0) break;
                if (thar < pixels.length - (Lin2 + 1)) {
                    pixels[thar + Lin2 + 1] = (byte) (info & 255); // red
                    pixels[thar + Lin2] = aByte;
                    pixels[thar + 1] = aByte;
                    pixels[thar] = (byte) ((info >> 16) & 255);
                } //~if
                thar = thar + 2;
            } //~for
            thar = thar + Lin2;
        } //~for
        DarkOnce = false;
        return true;
    } //~GetSimFrame

    public boolean StartImage(int rose, int colz, int tile) {
        final int myHi = ImHi;
        int why = 0;
        while (true) {
            why++; // why = 1 // if (DoScenery) {
            if (MapIndex == null) break;
            why++; // why = 2
            if (false) if (!TrakNoPix) if (TrakImages == null) break;
            why++; // why = 3
            if (rose != myHi) break;
            why++; // why = 4
            if (colz != WinWi) break;
            why++; // why = 5
            if (tile != 1) break;
            if (myScreen == null) SetMyScreen(new int[nPixels], rose, colz, tile);
            // sets: myScreen = new int[nPixels];
            //       SceneTall = rose;  SceneWide = colz;  SceneBayer = tile;
            why++; // why = 6
            if (myScreen == null) break;
            why = 0;
            break;
        } //~while
        return why == 0;
    } //~StartImage

    public void SetServo(int pin, int set2) {
        String msg = "";
        int why = 0, prio = 0, info = 0, centered = set2 - 90;
        double valu = (double) centered, tmp = valu;
        if (!unScaleStee) if (pin == SteerServo) { // because steering is not full-scale
            why = 8;
            info = centered; // (for log)
            if (centered < 0) { // LfDeScaleSt = 90.0/LeftSteer..
                why = 14;
                if (LfDeScaleSt > 1.0) tmp = LfDeScaleSt * valu;
                else why = 12;
            } else if (centered > 0) {
                why = 6;
                if (RtDeScaleSt > 1.0) tmp = RtDeScaleSt * valu;
                else why = 4;
            }
            centered = (int) Math.round(tmp);
        }
        if (set2 >= 0) if (set2 <= 180) while (true) { // once through
            if (pin == GasServo) { // GasServo = 10
                prio = SpecGas;
                if (prio == centered) break;
                SpecGas = centered;
                NuData++;
            } //~if (pin=GasServo)
            else if (pin == SteerServo) { // SteerServo = 9
                why++;
                prio = SpecWheel;
                if (prio == centered) break;
                SpecWheel = centered;
                why = -why;
                NuData++;
            } //~if (pin=SteerServo)
            break;
        } //~while (once)
        if (GoodLog) { //
            if (pin == SteerServo) msg = HandyOps.Flt2Log(" {", valu,
                    HandyOps.Flt2Log("/", LfDeScaleSt,
                            HandyOps.Flt2Log("/", RtDeScaleSt, "} (Steer) +")));
            else if (pin == GasServo) msg = " (Speed) +";
            else msg = " (?) ";
            System.out.println(HandyOps.Dec2Log("(SetServo) ", pin, HandyOps.Dec2Log(" = ",
                    set2, HandyOps.Dec2Log(" #", FrameNo, HandyOps.Dec2Log(", ", prio,
                            HandyOps.Flt2Log("=", centered, HandyOps.Dec2Log("=", info,
                                    HandyOps.Dec2Log(msg, NuData, HandyOps.Dec2Log("/", why,
                                            HandyOps.PosTime(" @ "))))))))));
        }
    } //~SetServo

    /**
     * Requests TrakSim to return its next NextFrame->GetFrame image black.
     * Use this to test briefly covering the lens as a signal to your software.
     */
    public void DarkFlash() {
        DarkOnce = true;
    }

    /**
     * Requests TrakSim to redraw its scene on next NextFrame->GetFrame call.
     * Use this if you are drawing on the image and you need a fresh copy.
     */
    public void FreshImage() {
        NuData++;
    } // to redraw the scene

    /**
     * TrakSim will crash the car (stop simulation) if it runs off the track
     * (or other Bad Things Happen), this tells you if it did.
     *
     * @return True if crashed, otherwise false
     */
    public boolean IsCrashed() {
        return (OpMode == 3);
    }

    public double ScaleSpeed() {
        return Velocity * dFtime;
    } // in m/s park, 8x actual

    public double GetFacing() {
        return Facing;
    } // in degrees clockwise from north

    /**
     * Gets the actual map size as read in from the file.
     *
     * @return The map height (north-south) and width (east-west)
     * in park meters, packed into a single integer.
     */
    public int GetMapSize() {
        if (TopCloseView == 0) return (MapHy << 16) + MapWy; // in park meters
        return (MyMath.iMin(MapHy, TopCloseView) << 16) + MapWy;
    } //~GetMapSize

    /**
     * Gets one coordinate of the current car position.
     *
     * @param horz True to get the horizontal (east-west) coordinate
     * @return The east-west coordinate of the car position in park meters
     * if horz=true, otherwise the north-south coordinate
     */
    public double GetPosn(boolean horz) {
        if (horz) return Hposn;
        return Vposn;
    } //~GetPosn

    /**
     * TrakSim will crash the car (stop simulation) if it runs off the track
     * (or other Bad Things Happen). This is one of those Bad Things.
     *
     * @param seen True to draw the red "X" on the screen
     */
    public void CrashMe(boolean seen) { // caller logs
        SimStep(3);
        if (seen) DrawRedX();
        NuData++;
    } //~CrashMe

    /**
     * Gets the pixel row corresponding to the car turn radius, and also
     * twice the turn radius. Your software can use this to make dicisions
     * (but you probably shouldn't, because it 's not available except during
     * simulation) or to annotate the screen for debugging purposes. Note that
     * the perspective of the screen viewing angle means these numbers are not
     * linear. If the focal length of the simulated camera is too long, or the
     * turn radius too short, one or both numbers could be below the bottom of
     * the screen, or possibly approximated at the horizon (screen middle).
     *
     * @return The row position of the turn radius relative to the car
     * in the low half, and twice the turn radius in the high 16.
     */
    public int TurnRadRow() { // image row+ at turn radius & 2x turn radius
        return t2Radix * 0x10000 + tRadix;
    } //~TurnRadRow

    private void MakeRangeTbl() { // what row+ is far?
        // SOHCAHTOA = "Some Old Hags Cant Always Have Their Own Aspirin"
        //   SOH,CAH,TOA: sin=opp/hyp; cos=adj/hyp; tan=opp/adj
        final double CameraHi = DriverCons.D_CameraHi;
        double tmp, aim = -0.25, far = 1.0 / 256.0, // fZoom=Zoom35/50.0, ImHaf=ImHi/2
                // CameraHi = 1.2, // Camera height above track in park meters
                // ZoomPix = ImWi*zx50/Zoom35, // divide this by distance for pix/meter
                ratio = ((double) (ImHaf)) * fZoom, z16 = fZoom * CameraHi * 16.0; // sb = 1.3*1.2*16=27
        boolean logz, logy = true;
        int dx = 0, nx = 0, rx = 0, px = 0, yx = 0, zx = 255, // effTurnRad=7.0
                tx = MyMath.Trunc8(effTurnRad * 16.0 + 0.5), t2x = tx + tx;
        int[] xData = new int[256];
        int[] zData = new int[ImHaf];
        if (zData == null) return;
        if (xData == null) return;
        RowRange = zData;
        RangeRow = xData;
        // ++ M.R.T. ++ 1.3 120 112 => -434176. 24. 7.
        // if (Vscale>0) ratio = ((double)(Vscale))*ratio;
        tRadix = 0;
        t2Radix = 0;
        for (nx = 0; nx <= 240; nx++) { // 0 to 60 down, in quarter-degrees
            aim = aim + 0.25;
            MyMath.Angle2cart(aim); // r=CameraHi/sin(aim), dx=r*cos(aim)*zoom
            if (MyMath.Sine < far) continue;
            tmp = z16 / MyMath.Sine; // = slant height (radius) *16
            dx = (int) Math.round(tmp * MyMath.Cose); // = true distance in meters*16
            // if (dx >= 256) continue;
            // if (dx<0) break;
            // xx = 2.0/MyMath.Cose; // nominal radius to screen @ 2m
            rx = ((int) Math.round(ratio * MyMath.Sine / MyMath.Cose)) + ImHaf;
            // if (px>0) if (rx>px+1) RoCorner = px;
            px = rx;
            if (tRadix == 0) {
                if (dx == tx) tRadix = rx;
                else if (dx < tx) tRadix = rx - 1;
            } //~if
            if (t2Radix == 0) {
                if (dx == t2x) t2Radix = rx;
                else if (dx < t2x) t2Radix = rx - 1;
            } //~if
            while (yx + ImHaf <= rx) {
                if (yx >= ImHaf) break;
                if (yx >= 0) RowRange[yx] = dx; // meters*16
                yx++;
            } //~while
            dx = (dx + 2) >> 2; // now it's dx in park 25cm units to index this table..
            while (zx > dx) {
                if (zx < 0) break;
                RangeRow[zx & 255] = rx;
                zx--;
            } //~while
            if (zx <= 0) if (yx >= ImHaf) break;
        } //~for // (only exit) rx valid, >ImHi
        zx = 0;
        yx = MyMath.iMax(rx, ImHi);
        while (RangeRow[zx & 255] == 0) {
            RangeRow[zx & 255] = yx;
            zx++;
        } //~while
        t2Radix = MyMath.iMax(t2Radix, ImHaf + 1);
        if (tRadix == 0) tRadix = ImHi + 2;
    } //~MakeRangeTbl

    /**
     * Changes the focal length of the simulated camera.
     *
     * @param newFoc35 The new focal length in 35mm-equivalent millimeters
     */
    public void Refocus(int newFoc35) { // set new lens focal length (see Zoom35)
        double fc35, fx50;
        if (ZoomFocus != 0) {
            if (newFoc35 < 25) return; // ignore unreasonable later requests..
            if (newFoc35 > 250) return;
        }
        VuEdge = 0.0; // recalc next display
        ZoomFocus = newFoc35;
        fx50 = MyMath.Fix2flt(zx50, 0); // zx50=28 = fudge-factor so Zoom35=50 OK
        fc35 = MyMath.Fix2flt(newFoc35, 0);
        fZoom = fc35 / fx50; // = 2x for 100mm-equivalent lens (NOT) [=1.3 for fc35=35]
        Dzoom = fx50 * 0.5 / fc35; // "denom-zoom" = 1/(2*fZoom)
        WiZoom = (Dzoom * 32.0) / FltWi; // FltWi = (double)ImWi;
        System.out.println(HandyOps.Dec2Log(" (Refocus) ", newFoc35,
                HandyOps.Dec2Log(" (", ZoomPix, HandyOps.Flt2Log(" ", fc35,
                        HandyOps.Flt2Log(" ", fx50, HandyOps.Flt2Log(" ", FltWi,
                                HandyOps.Flt2Log(") = ", fZoom, HandyOps.Flt2Log(" ", Dzoom,
                                        HandyOps.Flt2Log(" ", WiZoom, HandyOps.Flt2Log("=", (WiZoom * 1000),
                                                "/K"))))))))));
        MakeRangeTbl();
    } //~Refocus // also sets tRadix

    public void GotBytes(byte[] msg, int lxx) {
        if (msg == null) return;
        if ((((int) msg[0]) & 0xF0) != Arduino.ANALOG_MESSAGE) return;
        if (msg.length >= 3) SetServo(((int) msg[0]) & 0xF,
                (((int) msg[2]) << 7) | ((int) msg[1]) & 0x7F);
    } //~GotBytes

    private int Color4ix(int whom) { // only frm InitInd
        int why = whom & 15, info = GroundsColors;
        if (whom > 1) {        // unpack grounds colors from single int..
            info = info >> 16;
            whom = whom & 1;
        }
        if (whom > 0) {
            whom = info >> 12;
            whom = (whom & 7) * 3;
            // info = info&0xFFF;
            if ((info & 15) >= whom) info = info - whom;
            whom = whom << 4;
            if ((info & 255) >= whom) info = info - whom;
            whom = whom << 4;
            if ((info & 0xFFF) >= whom) info = info - whom;
        }
        info = ((info & 0xF00) << 8) | ((info & 0xF0) << 4) | info & 0xF;
        if (Log_Log) System.out.println(HandyOps.Dec2Log(" (Colo4ix) ", why,
                HandyOps.Hex2Log(" ", GroundsColors, 8, HandyOps.Hex2Log(" ", info, 8,
                        HandyOps.Hex2Log(" ", whom, 4, "")))));
        return (info << 4) + info;
    } //~Color4ix

    private boolean InitIndex(int[] theInx) { // true if success, always logs
        int myHi = ImHi,
                tall, wide, aim, anim, zx = ArtBase, nx = 0, rx = 0, cx = 0,
                tops = 0, bitz = 0, prio = 0, info = 0, lxx = 0, why = 0;
        int[] myFax = null;
        String aWord = "";                     // frm ReadTrakInd, LoadTrackInf
        boolean OK = true;
        InWalls = false;
        NumFax = 0; // soon: number of artifact spex
        RectMap = 0;
        for (nx = 0; nx <= 15; nx++) ActivAnima[nx] = 0;
        if (theInx != null) if (theInx.length > ArtBase) {
            ImageWide = theInx[0] & 0xFFFF;
            lxx = theInx[3];
            GroundsColors = theInx[4];
            nx = theInx[2]; // start of map
            RectMap = theInx[ArtBase - 1]; // = myMap[ArtBase+2] when constr'd
            MapIxBase = nx;
            if (nx > ArtBase) if (GroundsColors != 0) { // set colors & start pos'n..
                info = theInx[6];
                rx = theInx[5];
                for (nx = nx; nx >= 0; nx += -1) {
                    aim = theInx[zx];
                    if (aim == 0) break;
                    NumFax++;
                    anim = (aim >> 28) & 15;
                    aim = aim & 0x0FFFFFFF;
                    bitz = bitz | (1 << anim);
                    if (anim > 4) ActivAnima[anim & 15] = aim;
                    else if (anim < 4) {
                        // if (aim==prio) NumFax--; // no need to count alt views, but OK
                        prio = aim;
                        zx = zx + 3;
                    } //~if
                    else zx++;
                    zx++;
                } //~for
                if (NumFax > 0) NumFax++;
                cx = rx & 0xFFFF;
                rx = rx >> 16;
                SetStart(rx, cx, info & 0xFFFF);
                info = info >> 16;
                if (info > 99) info = -info;
                else if (info == 0) info = (int) Math.round(WhiteLnWi * 100.0);
                if (info >= 0) {
                    WhitLnSz = ((double) info) * 0.014; // white line width
                    aWord = aWord + HandyOps.Flt2Log(" W= ", WhitLnSz, "");
                } //~if
                PavColo = Color4ix(0);
                PavDk = Color4ix(1);
                GrasColo = Color4ix(2);
                GrasDk = Color4ix(3);
                if (!TrakNoPix) if (NumFax > 0) if (TrakImages != null) {
                    myFax = new int[NumFax * 2];
                    theFax = myFax;
                    if (myFax == null) NumFax = 0;
                    else for (info = NumFax * 2 - 1; info >= 0; info += -1)
                        if (info >= 0) if (info < myFax.length) myFax[info] = 0;
                }
            }
        } //~if
        if (GroundsColors < 0) for (nx = nx; nx <= HalfMap * HalfTall - 1; nx += HalfMap + 1) { // diagonal
            if (nx > HalfMap * (HalfTall - 2)) {
                InWalls = true; // no walls, but also no BG, call it inside..
                break;
            } //~if
            if (theInx != null)
                if (nx > 0) if (nx < theInx.length) info = theInx[nx];
            if (info <= 0) continue;
            if (info >= 0x70000000) break; // found BG, it's probly outdoors
            if (info >= 0x40000000) continue;
            if (info <= 4) InWalls = true; // found a wall, it's inside
            break;
        } //~for           // otherwise outside background (InWalls=false)
        tall = lxx >> 16; // lxx = theInx[3] = park dims
        wide = lxx & 0xFFF;
        MapHy = tall;
        MapWy = wide;
        fMapWi = MyMath.Fix2flt(wide, 0);
        ZooMapScale = 0.0; // ZMS*(2m grid) -> img pix in close-up
        ZooMapWhLn = 0.0; // ZMW*(img pix) -> park meters: ZMPS*ZMS = 2.0*1.4
        ZooMapTopL = 0; // (ditto) park coords for top-left corner of close-up
        ZooMapBase = 0; // where on the image it is shown
        ZooMapShf = 0; // shift: img pix : park meters
        ZooMapDim = 0; // in park meters as used to calc edges of close-up
        while (true) { // if ((GroundsColors<0)||(lxx != ParkDims)) // =200<<16 +256
            why++; // why = 1              // ..close-up is useful in all cases
            if (!DoCloseUp) break;
            why++; // why = 2
            if (!ShowMap) break;
            why++; // why = 3
            if ((1 << MapWiBit) != MapWide) break; // verify correct constant
            why++; // why = 4
            if (tall >= ImHaf) if (tall + 99 > myHi) break; // ..unless no space for it
            why++; // why = 5
            if ((rx | cx) == 0) {
                rx = MyMath.Trunc8(Vposn); // (for log)
                cx = MyMath.Trunc8(Hposn);
            } //~if
            lxx = MyMath.iMax(tall + 2, myHi - 16 - MapTall); // = top row of displayed c-u
            SetCloseTop(lxx);
            if (((ZooMapDim & 0xFF) << ZooMapShf) != MapWide) OK = false;
            // these two for log (example only) as if car is centered (not)...........
            rx = MyMath.iMax(rx - (info >> 1), 0);
            cx = MyMath.iMax(cx - ((ZooMapDim >> 1) & 0xFFF), 0);
            ZooMapTopL = (rx << 16) + cx; // in pk meters (using formula from BuildFrame)..
            aWord = aWord + HandyOps.Int2Log("\n// ZMD=", ZooMapDim,
                    HandyOps.Dec2Log(" ZSf=", ZooMapShf, HandyOps.Int2Log(" ZB=",
                            ZooMapBase, HandyOps.Flt2Log(" ZSc=", ZooMapScale,
                                    HandyOps.Flt2Log(" ZW=", ZooMapWhLn,
                                            HandyOps.Int2Log(" ZT=", ZooMapTopL,
                                                    HandyOps.IffyStr(OK, "", " OOPS")))))));
            // ZMD=32,32 ZSf=3 ZB=224,642 ZSc=16. ZW=0.2 ZT=8,16
            ZooMapTopL = 0;
            why = 0;
            break;
        } //~while
        nx = GroundsColors;
        aWord = aWord + HandyOps.Dec2Log(" -- iW=", ImageWide,
                HandyOps.Dec2Log(" L=", RectMap, ""));
        System.out.println(HandyOps.Dec2Log(" (IniIndx) = ", why,      // why =
                HandyOps.Dec2Log(" ", tall, HandyOps.Dec2Log("/", wide,
                        HandyOps.Hex2Log(" ", nx, 8, HandyOps.Colo2Log(" G=", GrasColo,
                                HandyOps.Colo2Log("/", GrasDk, HandyOps.Colo2Log(" T=", PavColo,
                                        HandyOps.Colo2Log("/", PavDk, HandyOps.TF2Log(" I=", InWalls,
                                                HandyOps.Dec2Log(" ", myHi, HandyOps.Dec2Log(" ", NumFax,
                                                        HandyOps.Hex2Log(" x", bitz, 4, aWord + "\n  ---> "
                                                                + HandyOps.ArrayDumpLine(ActivAnima, 0, 3))))))))))))));
        return why >= 0;
    } //~InitIndex

    private void ReadTrakIndex() { // only frm LoadTrackIn; see also BuildMap
        int here, thar,
                lxx = 0, tops = 0, xize = 0, info = 0, tall = 0, wide = 0, why = 0;
        boolean LilEndian = false;
        byte[] bytes8 = new byte[12];
        int[] theInx;
        byte[] xData;
        File myFile = new File(SceneFiName + "indx");
        FileInputStream theFile = null;
        try {
            while (true) {
                why++; // why = 1
                if (myFile == null) break;
                why++; // why = 2
                try {
                    theFile = new FileInputStream(myFile);
                } catch (Exception ex) {
                    theFile = null;
                }
                if (theFile == null) break;
                why++; // why = 3
                info = theFile.read(bytes8);
                if (info < 8) break;
                why++; // why = 4
                info = HandyOps.Int4bytes(false, bytes8[0], bytes8[1], bytes8[2], bytes8[3]);
                if (info == 0x4C696C45) LilEndian = true; // "LilE"
                else if (info == 0x456C694C) LilEndian = true;
                else if (info != 0x42696745) if (info != 0x45676942) break; // "BigE"
                why++; // why = 5
                xize = HandyOps.Int4bytes(LilEndian, bytes8[4], bytes8[5],
                        bytes8[6], bytes8[7]);
                tops = HandyOps.Int4bytes(LilEndian, bytes8[8], bytes8[9],
                        bytes8[10], bytes8[11]);
                info = xize;
                if (xize < 9999) break;
                if (xize > 9999999) break;
                if (!TrakNoPix) if (tops > 0) {
                    info = tops;
                    if (tops < 512) break;
                    if (tops > 9999999) break;
                    if ((tops & 255) != 0) break;
                } //~if
                lxx = xize * 4; // index size in ints, now in bytes
                xData = new byte[lxx];
                why++; // why = 6
                if (xData == null) break;
                why++; // why = 7
                theInx = new int[xize];
                if (theInx == null) break;
                why++; // why = 8
                info = theFile.read(xData);
                if (info < lxx) break;
                here = lxx - 4;
                lxx = 0; // thar = xize-1;
                why = 9;
                for (thar = xize - 1; thar >= 0; thar += -1) {
                    if (xData == null) break;
                    why++;
                    if (here < 0) break;
                    if (here < xData.length - 4)
                        info = HandyOps.Int4bytes(LilEndian, xData[here], // why = 10
                                xData[here + 1], xData[here + 2], xData[here + 3]);
                    if (theInx == null) break;
                    why++;
                    if (thar < 0) break;
                    if (thar < theInx.length)
                        theInx[thar] = info; // why = 11
                    here = here - 4;
                    why = 9;
                } //~for
                if (why > 9) break;
                if (!InitIndex(theInx)) {
                    why = 19;
                    break;
                }
                why = 12;
                MapIndex = theInx;
                theInx = null;
                xData = null;
                if (!TrakNoPix) if (tops > 0) {
                    lxx = tops * 4; // image size in ints, now in bytes
                    xData = new byte[lxx + 4];
                    why++; // why = 13
                    if (xData == null) break;
                    why++; // why = 14
                    theInx = new int[tops];
                    if (theInx == null) break;
                    why++; // why = 15
                    info = theFile.read(xData);
                    if (info < lxx) break;
                    here = 0;
                    why = 16;
                    for (thar = 0; thar <= tops - 1; thar++) { // TIFF = RGBA, we want ABGR..
                        if (xData == null) break;
                        why++;
                        if (here < 0) break;
                        if (here < xData.length - 4)
                            info = HandyOps.Int4bytes(false, xData[here], xData[here + 1],
                                    xData[here + 2], xData[here + 3]);
                        if (theInx == null) break; // why = 17
                        why++;
                        if (thar < 0) break;
                        if (thar < theInx.length) theInx[thar] = info; // why = 18
                        here = here + 4;
                        why = 16;
                    } //~for
                    if (why > 16) break;
                    TrakImages = theInx;
                }
                why = 0;
                break;
            }
        } catch (Exception ex) {
            why = -why;
        }
        if (why != 0) if (why < 13) MapIndex = null;
        System.out.println(HandyOps.Dec2Log("ReadPatsIndx ", xize >> 10,
                HandyOps.Dec2Log("K / ", tops >> 10, HandyOps.Dec2Log("K (", info,
                        HandyOps.Dec2Log(") = ", why, "")))));
    } //~ReadTrakIndex

    private boolean LoadTrackInfo() { // true if success
        int nx, zx, tall = 0, wide = 0;
        String theList, filename;
        int[] theInx = null; // loading this is an exercise for the user ;-)
        int[] myMap = null;
        if (UseTexTrak) while (true) {
            try {
                MapIndex = null;
                TrakImages = null;
                theList = HandyOps.ReadWholeTextFile(SceneFiName + "txt");
                if (theList == "") break;
                if (!TrakNoPix) if (GotImgOps(theList) != 0) while (true) {
                    filename = SceneFiName + "tiff";
                    zx = HandyOps.ReadTiff32Image(filename, null);
                    if (zx == 0) break;
                    tall = zx >> 16;
                    wide = zx & 0xFFFF;
                    nx = tall * wide;
                    theInx = new int[nx];
                    if (theInx == null) break;
                    nx = HandyOps.ReadTiff32Image(filename, theInx);
                    if (nx == zx) {
                        WhiteAlfa(tall, wide, theInx);
                        TrakImages = theInx;
                    } //~if
                    else theInx = null;
                    break;
                } //~while
                myMap = BuildMap(theList, (tall << 16) + wide, theInx);
                if (myMap == null) break;
                for (nx = 0; nx <= myMap.length - 4; nx++) {
                    if (myMap == null) break;
                    if (nx < 0) break;
                    if (nx < myMap.length - 3) myMap[nx] = myMap[nx + 3];
                } //~for
            } catch (Exception ex) {
                break;
            }
            if (InitIndex(myMap)) MapIndex = myMap;
            break;
        } //~while
        if (MapIndex == null) {
            ReadTrakIndex();
            if (MapIndex == null) return false;
        } //~if
        return true;
    } //~LoadTrackInfo

    private void Valid8consts() {
        boolean NG = false;
        String aWord = "";
        int info = HandyOps.Countem("aba", "ababaaabaaababb"), // =3
                whar = HandyOps.NthOffset(2, "aba", "ababaaabaaababb"); // =6
        double Vat = HandyOps.SafeParseFlt("12.3"),
                Hat = HandyOps.SafeParseFlt(" -0.01 ");
        if (NoisyMap) {
            aWord = HandyOps.Flt2Log("*** FltTest: ", Vat * 10.0,
                    HandyOps.Flt2Log(" ", Hat * 1000.0,
                            HandyOps.Flt2Log(" ", HandyOps.SafeParseFlt("0x22.2"),
                                    HandyOps.Dec2Log(" ", info, HandyOps.Dec2Log(" ", whar, " ")))));
            info = HandyOps.Int4bytes(false, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78);
            whar = HandyOps.Int4bytes(true, (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12);
            NG = (info != whar) || (info != 0x12345678);
            if (NG) aWord = "<!> " + aWord;
            System.out.println(HandyOps.Hex2Log(aWord, info, 8,
                    HandyOps.Hex2Log(" = ", whar, 8, HandyOps.TF2Log(" *** ", NG, ""))));
            aWord = "";
        } //~if
        if (ParkDims != 0xC80100) { // ParkDims checked only here
            System.out.println("<!> TrakSim is designed only for ParkDims=200x256");
            NG = true;
        } //~if
        if (BayerTile != 1) {
            System.out.println("<!> TrakSim is designed only for BayerTile=1");
            NG = true;
        } //~if
        while (true) {
            if (ImHi == 480) if (ImWi == 640) break;
            if (ImHi == 240) if (ImWi == 320) break;
            System.out.println(HandyOps.Dec2Log("<!> TrakSim has not been tested"
                    + " with image sizes other than V=480 x H=640 and V=240 x H=320"
                    + " (", ImHi, HandyOps.Dec2Log("x", ImWi, ")")));
            break;
        } //~while
        if (1 > 32) {
            System.out.println("<!> A large 1 leaves no image space for track");
            if (1 * 4 > ImHi) NG = true;
        } //~if
        while (true) {
            if (MapTall == 200) if (MapWide == 256) break;
            System.out.println("<!> TrakSim is designed for park size V=200 x H=256");
            NG = true;
            break;
        } //~while
        while (true) {
            if (Vramp > 0) if (Vramp < MapTall) if (Hramp > 0) if (Hramp < MapWide) break;
            System.out.println(HandyOps.Dec2Log("<!> Vramp,Hramp must be inside park: ",
                    MapTall, HandyOps.Dec2Log(",", MapWide, "")));
            NG = true;
            break;
        } //~while
        if ((RampA < 0) || (RampA >= 360))
            System.out.println("<!> RampA should be in the range 0-359");
        if ((Zoom35 < 20) || (Zoom35 > 255))
            System.out.println("<!> TrakSim probably won't work well with Zoom35 as extreme "
                    + HandyOps.IffyStr(Zoom35 < 99, "close-up", "telephoto") + " lens");
        if (FrameTime < 20) {
            System.out.println("<!> TrakSim is designed only for FrameTime >= 20ms");
            NG = true;
        } //~if
        if (SteerServo == GasServo) {
            System.out.println("<!> TrakSim requires distinct Steer & Gas Servos");
            NG = true;
        } //~if
        while (true) {
            if (SteerServo > 0) if (SteerServo < 16)
                if (GasServo > 0) if (GasServo < 16) break;
            System.out.println("<!> TrakSim is designed only for servo pins 1-15");
            NG = true;
            break;
        } //~while
        if (MinESCact >= MaxESCact) {
            System.out.println("<!> MinESCact >= MaxESCact can't work");
            NG = true;
        } //~if
        else while (true) {
            if (MinESCact >= 0) if (MinESCact < 90)
                if (MaxESCact > 0) if (MaxESCact <= 90) break;
            System.out.println("<!> TrakSim is designed for MinESCact/MaxESCact"
                    + " only in the range 0-90");
            NG = true;
            break;
        } //~while
        // while (true) {
        //   if (LeftSteer >= 0) if (LeftSteer <= 90)
        //     if (RiteSteer >= 0) if (RiteSteer <= 90) break;
        //   System.out.println("<!> TrakSim is designed for LeftSteer/RiteSteer"
        //       + " only in the range 0-90");
        //   NG = true;
        //   break;} //~while
        info = Crummy + 1;
        if ((info & -info) != info) { // if (DoScenery) if (!TrakNoPix)
            System.out.println("<!> Masks like Crummy only work as 2^n-1");
            NG = true;
        } //~if
        info = CheckerBd + 1;
        if (!TrakNoPix) if ((info & -info) != info) // if (DoScenery)
            System.out.println("<!> Masks like CheckerBd only work as 2^n-1");
        if ((MarinBlue & -0x01000000) != 0) aWord = "MarinBlue";
        //else if ((SteerColo & -0x01000000) != 0) aWord = "SteerColo";
        else if ((CarColo & -0x01000000) != 0) aWord = "CarColo";
        else if ((DarkWall & -0x01000000) != 0) aWord = "DarkWall";
        else if ((BackWall & -0x01000000) != 0) aWord = "BackWall";
        else if ((CreamWall & -0x01000000) != 0) aWord = "CreamWall";
        if (aWord != "")
            System.out.println("<!> TrakSim is designed for 00 alpha channel"
                    + " in colors like " + aWord);
        if (TurnRadius < 0.5) {
            System.out.println("<!> I don't think TrakSim can simulate a car"
                    + " with a TurnRadius less than 2 meters");
            NG = true;
        } //~if
        else if (TurnRadius < 2.0)
            System.out.println("<!> Wow! That's a really short TurnRadius");
        else if (TurnRadius > 64.0)
            System.out.println("<!> Wow! That's a really long TurnRadius,"
                    + " Did you intend your car to have no steering wheel?");
        if (fMinSpeed < 0.0) {
            System.out.println("<!> TrakSim does not simulate a car going backwards");
            NG = true;
        } //~if
        else if (fMinSpeed > 32.0) System.out.println("<!> MINimum speed 65mph"
                + " (fMinSpeed>32 m/s) is ridiculous");
        if (WhiteLnWi < 0.0) {
            System.out.println("<!> You can't have negative width WhiteLnWi");
            NG = true;
        } //~if
        else if (WhiteLnWi > 2.0)
            System.out.println("<!> You want *really* wide WhiteLnWi?");
        else if (WhiteLnWi == 0.0) System.out.println("<!> No white lines, eh?");
        if (Acceleration < 0.0) {
            System.out.println("<!> You can't have negative Acceleration");
            NG = true;
        } //~if
        else if (Acceleration > 30.0)
            System.out.println("<!> Your car has no get-up-and-go (Acceleration)?");
        else if (Acceleration == 0.0)
            System.out.println("<!> That's a pretty zippy car. OK");
        if (NG) System.exit(8);
    }

    public void StartPatty(String whom) {
        System.out.println(HandyOps.Dec2Log("StartingSim #", nClients, " " + whom));
        if (nClients == 0) {
            if (ShowMap) BreadCrumbs = new int[Crummy + 1];
            PrioRaster = new int[ImWi];
            ActivAnima = new int[16];
            FltWi = (double) ImWi;
            fImHaf = (double) ImHaf;
            Valid8consts(); // quits if bogus defined constants
            Refocus(Zoom35); // does these things:
            // Dzoom = ((double)zx50)/((double)Zoom35*2); // "denom-zoom" = 1/(2*fZoom)
            // WiZoom = (Dzoom*32.0)/FltWi; // = 16/(ImWi*fZoom)
            // fZoom = ((double)Zoom35)/((double)zx50); // = 2x for 100mm-equivalent lens
            // MakeRangeTbl(); // builds RangeRo,RowRang, sets tRadix
            if (TweakRx != 0) while (tRadix > ImHi - 8 - 1) { // TweakRx=0
                if (TweakRx > 0) {
                    effTurnRad = effTurnRad + MyMath.Fix2flt(TweakRx, 0); // +TurnRadius/4.0;
                    MakeRangeTbl();
                } //~if
                else if (TweakRx < 0) Refocus(ZoomFocus + TweakRx);
                else break;
            } //~while
            SetStart(Vramp, Hramp, RampA);
            // Vposn = Vramp; Hposn = Hramp; Facing = RampA;
            if (!LoadTrackInfo()) {
                System.out.println("Unable to load track map");
                System.exit(3);
            }
            if (!NewGridTbl(Grid_Locns)) {
            }
            Velocity = 0.0;
            fSpeed = 0.0;
            fSteer = 0.0;
            SteerWhee = 0;
            GasBrake = 0;
            NuData++;
            SerialCalls = new SimHookX();
            Arduino.HookExtend(SerialCalls);
        }
        nClients++;
    } //~StartPatty

    public class SimHookX extends SimHookBase {
        public void SendBytes(byte[] msg, int lxx) {
            GotBytes(msg, lxx);
        }
    } //~SimHookX
} //~TrakSim (apw3) (TS) (PA)

package com.apw.gpu;

import com.aparapi.Kernel;

public class LineSlopeKernel extends Kernel {


    /**
     * vertical coordinate of any point along a line
     */
    private double Vat;
    /**
     * horizontal coordinate of any point along a line
     */
    private double Hat;
    /**
     * cos of c-wise angle from north
     */
    private double Vstp;
    /**
     * sin of c-wise angle from north
     */
    private double Hstp;
    private boolean logy;
    private String msg;

    LineSlopeKernel() {
        KernelManager.AddKernel(KernelType.LINE_SLOPE_KERNAL, this);
    }

    @Override
    public void run() {

        // TODO implement LineSlope method from TrakSim

        /*
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
     */
    }
}
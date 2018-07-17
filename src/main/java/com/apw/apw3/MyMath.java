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
package com.apw.apw3;                                     // 2018 May 25

/**
 * Some Pseudo-Math Operations..
 */
public class MyMath {

    private static int[] pow2 = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048,
            0x1000, 0x2000, 0x4000, 0x8000, 0x10000, 0x20000, 0x40000, 0x80000,
            0x100000, 0x200000, 0x400000, 0x800000, 0x01000000, 0x02000000,
            0x04000000, 0x08000000, 0x10000000, 0x20000000, 0x40000000, 0};

    /**
     * Sine and Cose are the return values set by Angle2cart.
     */
    public static double Sine = 0.0, Cose = 1.0, preAngle = 0.0;

    /**
     * Sign-extends the low 16 bits of a packed number pair.
     * Use (whom>>16) to get the high 16 bits.
     *
     * @param whom The packed number pair
     * @return The signed 16-bit (now 32-bit) number from the low half
     */
    public static int SgnExt(int whom) {
        return (whom & 0x7FFF) - (whom & 0x8000);
    }

    /**
     * Strongly-typed (not overloaded) absolute value of an integer.
     *
     * @param whom The signed number
     * @return The same number as a positive
     */
    public static int iAbs(int whom) {
        return Math.abs(whom);
    }

    /**
     * Strongly-typed (not overloaded) maximum of two integers.
     *
     * @param lft One of the two numbers
     * @param rit The other of the two numbers
     * @return The larger (more positive) of the two
     */
    public static int iMax(int lft, int rit) {
        return Math.max(lft, rit);
    }

    /**
     * Strongly-typed (not overloaded) minimum of two integers.
     *
     * @param lft One of the two numbers
     * @param rit The other of the two numbers
     * @return The lesser (more negative) of the two
     */
    public static int iMin(int lft, int rit) {
        return Math.min(lft, rit);
    }

    /**
     * Strongly-typed (not overloaded) maximum of two floating-point numbers.
     *
     * @param lft One of the two numbers
     * @param rit The other of the two numbers
     * @return The larger (more positive) of the two
     */
    public static double fMax(double lft, double rit) {
        return Math.max(lft, rit);
    }

    /**
     * Strongly-typed (not overloaded) minimum of two floating-point numbers.
     *
     * @param lft One of the two numbers
     * @param rit The other of the two numbers
     * @return The lesser (more negative) of the two
     */
    public static double fMin(double lft, double rit) {
        return Math.min(lft, rit);
    }

    /**
     * Strongly-typed (not overloaded) absolute value of a floating-point number.
     *
     * @param whom The signed number
     * @return The same number as a positive
     */
    public static double fAbs(double whom) {
        return Math.abs(whom);
    }

    /**
     * Converts a floating-point number to an integer by discarding the fraction.
     *
     * @param whom The floating-point number
     * @return The same number as an integer
     */
    public static int Trunc8(double whom) {
        return (int) Math.round(Math.floor(whom));
    }

    /**
     * Reduces a floating-point number to just its sign.
     *
     * @param whom The floating-point number
     * @return The sign of that number, -1.0 or +1.0 (or 0.0)
     */
    public static double Signum(double whom) {
        if (whom < 0.0) whom = -1.0;
        else if (whom > 0.0) whom = 1.0;
        return whom;
    }

    /**
     * Converts an integer representation of fixed-point to floating-point.
     *
     * @param whom  The floating-point number
     * @param fbits The number of bits in the fraction
     * @return The same number as floating-point
     */
    public static double Fix2flt(int whom, int fbits) {
        double valu = (double) whom; // std Fix2flt fbits: 0,1,2,4,6,10,12,19,22
        int shft = pow2[Math.abs(fbits) & 31];
        if (fbits != 0) { // only these few are used..
            if (fbits == 12) valu = valu / 4096.0;  // compass = F2f(..,2); Vat = ..,4);
            else if (fbits == 2) valu = valu / 4.0; // Mconst = ..,12); Kconst = ..,19);
            else if (fbits == 4) valu = valu / 16.0;
            else if (fbits == 19) valu = valu / 524288.0;
            else if (fbits == 10) valu = valu / 1024.0;
            else if (fbits == 22) valu = valu / 4194304.0;
            else if (fbits == 6) valu = valu / 64.0;
            else if (fbits == 1) valu = valu / 2.0;
                // else if (fbits==24) valu = valu/16777216.0;
            else if (fbits > 30) valu = 0.0; // we don't do that big/small..
            else if (fbits + 30 < 0) valu = 0.0;
            else if (shft == 0) valu = 0.0;
            else if (fbits > 0) valu = valu / ((double) shft);
            else if (fbits < 0) valu = valu * ((double) shft);
        }
        return valu;
    } //~Fix2flt

    /**
     * The full-circle arc-tangent of a slope represented as x and y coordinates
     * returned as floating-point degrees. The sine of aTan0(y,x) = y,
     * and the cosine of aTan0(y,x) = x.
     * <p>
     * TrakSim uses a (TV) raster coordinate system where (0,0) is the northwest
     * or upper left corner, and angles are in degrees clockwise from north.
     *
     * @param y The floating-point sine component of the angle
     * @param x The floating-point cosine component of the angle
     * @return The angle in degrees, between -180 and +180
     */
    public static double aTan0(double y, double x) { // in degrees, +/-0..180
        try {
            return Math.atan2(y, x) * 180 / Math.PI;
        } catch (Exception ex) {
            return 0.0;
        }
    } //~aTan0

    /**
     * The inverse function of aTan0, which returns both sine and cosine.
     * <p>
     * TrakSim uses a (TV) raster coordinate system where (0,0) is the northwest
     * or upper left corner, and angles are in degrees clockwise from north.
     *
     * @param degs The floating-point angle in degrees
     * @return The sine is in MyMath.Sine and the cosine is in MyMath.Cose
     */
    public static void Angle2cart(double degs) { // in degrees c-wise frm north
        double radn;
        if (degs == preAngle) return;
        preAngle = degs;
        radn = (((double) degs) * Math.PI) / 180.0;
        Sine = (double) Math.sin(radn);
        Cose = (double) Math.cos(radn);
    } //~Angle2cart

    public MyMath() { // outer class const'r..FakeMath() {
        System.out.println("MyMath");
    }
} //~MyMath (apw3) (MM)

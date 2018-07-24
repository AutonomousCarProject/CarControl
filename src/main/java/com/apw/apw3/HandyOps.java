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
 * than LattePanda, you can substitute package nojssc, which has the same
 * APIs (as used by FakeFirmata) but does nothing.
 */
package com.apw.apw3;                                        // 2018 May 25

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

/**
 * Handy Operations that should be simple (but maybe aren't so in Java)..
 */
public class HandyOps { // first some useful debug logging ops..
    private static long start = System.currentTimeMillis();
    private static int HardEndian = 0, // =-1 if running on Little-Endian
            TifImDimz = 0, TifDataOfx = 0; // cached by ReadTiff32Im for 2nd read
    private static String fraction = "", TifFileNm = "";

    public HandyOps() { // (unused) class const'r..FakeHandy() {
        System.out.println("new HandyOps");
    }

    /**
     * Strongly-typed (not overloaded) string selector.
     * This is part of a collection of tools for building debug print lines.
     *
     * @param whom The selector
     * @param tru  Returned if whom=true
     * @param fls  Returned if whom=false
     * @return The selected string, tru if whom=true, else fls
     */
    public static String IffyStr(boolean whom, String tru, String fls) {
        if (whom) return tru;
        return fls;
    } //~IffyStr

    /**
     * Formats a boolean (as "T" or "F") and returns it with a prefix and suffix.
     * This is one of several similar tools for building debug print lines.
     *
     * @param before The prefix
     * @param whom   The boolean value to be formatted
     * @param after  The suffix
     * @return The combined string
     */
    public static String TF2Log(String before, boolean whom, String after) {
        if (whom) return before + "T" + after;
        return before + "F" + after;
    } //~TF2Log

    /**
     * Formats an integer and returns it with a prefix and suffix.
     * This is one of several similar tools for building debug print lines.
     *
     * @param before The prefix
     * @param whom   The number to be formatted
     * @param after  The suffix
     * @return The combined string
     */
    public static String Dec2Log(String before, int whom, String after) {
        return before + whom + after;
    } //~Dec2Log

    /**
     * Formats an integer as hexadecimal and returns it with a prefix and suffix.
     * This is one of several similar tools for building debug print lines.
     *
     * @param before The prefix
     * @param whom   The number to be formatted
     * @param nx     The number of digits to be formatted (excess bits ignored)
     * @param after  The suffix
     * @return The combined string
     */
    public static String Hex2Log(String before, int whom, int nx, String after) {
        String HexDigs = "0123456789ABCDEF";
        if (nx > 0) {
            nx--;
            try {
                before = before
                        + String.valueOf(HexDigs.charAt((whom >> (nx * 4)) & 15));
            } catch (Exception ex) {
            }
            if (nx > 0) return Hex2Log(before, whom, nx, after);
        }
        return before + after;
    } //~Hex2Log

    /**
     * Formats an integer according to its likely value and returns it
     * with a prefix and suffix. If it appears to be two 16-bit integers packed
     * into one 32-bit number, the two parts are returned separated by a comma.
     * Very large numbers (more than signed 15 bits) are returned as hexadecimal.
     * <p>
     * This is one of several similar tools for building debug print lines.
     *
     * @param before The prefix
     * @param whom   The number to be formatted
     * @param after  The suffix
     * @return The combined string
     */
    public static String Int2Log(String before, int whom, String after) {
        int info = MyMath.SgnExt(whom);
        if (info == whom) return Dec2Log(before, whom, after);
        if ((whom & 0xF0000000) == 0x80000000)
            return Int2Log(before + "`", whom & 0x7FFFFFFF, after);
        if (((whom + 0x04000400) & 0xF800F800) == 0)
            return Dec2Log(before, whom >> 16, Dec2Log(",", info, after));
        return Hex2Log(before, whom, 8, after);
    } //~Int2Log

    /**
     * Formats an integer representation of a color and returns it as a
     * 3-digit RGB (each as 4-bit hex) with a prefix and suffix.
     * <p>
     * This is one of several similar tools for building debug print lines.
     *
     * @param before The prefix
     * @param whom   The number to be formatted
     * @param after  The suffix
     * @return The combined string
     */
    public static String Colo2Log(String before, int whom, String after) {
        whom = (whom >> 12) & 0xF00 | (whom >> 8) & 0xF0 | (whom >> 4) & 15; // rtn RGB as 3 hex
        return Hex2Log(before, whom, 3, after);
    } //~Colo2Log

    /**
     * Formats a floating-point number and returns it with a prefix and suffix.
     * If there is no fractional part, no digits are shown after the decimal point;
     * otherwise a single (rounded) fractional digit is shown.
     * <p>
     * This is one of several similar tools for building debug print lines.
     *
     * @param before The prefix
     * @param whom   The number to be formatted
     * @param after  The suffix
     * @return The combined string
     */
    public static String Flt2Log(String before, double whom, String after) {
        String info = String.valueOf(((double) Math.round(whom * 10.0)) / 10.0);
        int lxx = info.length(), whar = info.indexOf('.');
        if (whar > 0) if (whar + 1 < lxx) try {
            whar++;
            if (Math.floor(whom) != whom) whar++;
            info = info.substring(0, whar);
        } catch (Exception ex) {
        }
        return before + info + after;
    } //~Flt2Log

    /**
     * Formats the time since TrakSim started as mm:ss:ms and returns it
     * with a prefix. It is normally added to the end of the debug line
     * for a visual way to see where processing time is being spent.
     * <p>
     * This is one of several similar tools for building debug print lines.
     *
     * @param before The prefix
     * @return The combined string
     */
    public static String PosTime(String before) {
        long now = System.currentTimeMillis() - start, secs = now / 1000,
                mins = secs / 60;
        now = now - secs * 1000;
        secs = secs - mins * 60;
        return Dec2Log(before, (int) mins, Dec2Log(IffyStr(secs > 9, ":", ":0"),
                (int) secs, Dec2Log(IffyStr(now > 99, ".",
                        IffyStr(now > 9, ".0", ".00")), (int) now, "")));
    } //~PosTime

    /**
     * A safe (no exceptions) non-OOPS way to extract a character from a string.
     *
     * @param here The (0-base) character position
     * @param aStr The string from which to extract that character
     * @return The character extracted, or '\0' if out of bounds
     */
    public static char CharAt(int here, String aStr) {
        if (here < 0) return '\0';
        try {
            if (here >= aStr.length()) return '\0';
            return aStr.charAt(here);
        } catch (Exception ex) {
            return '\0';
        }
    } //~CharAt

    /**
     * Returns the time since TrakSim started, as integer seconds.
     *
     * @param ms2 True: returns milliseconds instead of seconds
     * @return The time
     */
    public static int TimeSecs(boolean ms2) {
        long now = System.currentTimeMillis() - start;
        if (ms2) return (int) now;
        return (int) (now / 1000);
    } //~TimeSecs

    /**
     * A safe (no exceptions) way to extract an integer number from a string.
     * If the number begins with "0x" then it is assumed to be hexadecimal.
     * Leading white space is ignored, and the first character that is not
     * part of the number ends the scan.
     *
     * @param aStr The string from which to extract that character
     * @return The number parsed, or 0 if none
     */
    public static int SafeParseInt(String aStr) {
        char xCh;
        int here, valu = 0, mode = 0;
        fraction = "";
        try {
            for (here = 0; here <= aStr.length() - 1; here++) {
                xCh = CharAt(here, aStr);
                if (xCh <= '\0') break;
                if (xCh == '.') {
                    if (mode < 0) break;
                    try {
                        fraction = aStr.substring(here + 1);
                        if ((mode & 3) > 0) fraction = "-" + fraction;
                    } catch (Exception ex) {
                        fraction = "";
                    }
                    break;
                } //~if
                if (valu == 0) {
                    if (xCh <= ' ') continue;
                    if (xCh == '-') {
                        if (mode != 0) break;
                        mode++;
                        continue;
                    } //~if
                    if (xCh == 'x') {
                        if (mode < 0) break;
                        mode = mode - 4;
                        continue;
                    }
                } //~if
                if (xCh < '0') break;
                if (mode < 0) {
                    if (xCh > 'F') break;
                    if (xCh > '9') if (xCh < 'A') break;
                    valu = valu * 16 + (((int) xCh) & 15);
                    if (xCh > '9') valu = valu + 9;
                } //~if
                else if (xCh > '9') break;
                else valu = valu * 10 + (((int) xCh) & 15);
            } //~for
        } catch (Exception ex) {
        }
        if ((mode & 3) > 0) valu = -valu;
        return valu;
    } //~SafeParseInt

    /**
     * A safe (no exceptions) way to count the number of (non-overlapping)
     * occurrences of a particular string within a larger string.
     * An empty string cannot be counted.
     *
     * @param aWord The string to look for
     * @param aStr  The string in which to find and count it
     * @return The number of occurrences found, or 0 if none
     */
    public static int Countem(String aWord, String aStr) {
        int lxx = aWord.length(), whar, thar = 0, nby = 0;
        try {
            if (lxx == 0) return 0;
            while (true) {
                whar = aStr.indexOf(aWord, thar);
                if (whar < 0) break;
                thar = whar + lxx;
                nby++;
            }
        } //~while //~try
        catch (Exception ex) {
        }
        return nby;
    } //~Countem

    /**
     * A safe (no exceptions) way to find the (0-based) offset of a
     * particular string within a larger string, while ignoring zero
     * or more initial (non-overlapping) occurrences. An empty string
     * cannot be found.
     *
     * @param whom  The number of occurrences to skip over
     * @param aWord The string to look for
     * @param aStr  The string in which to find it
     * @return The (indexOf) offset, or -1 if not found
     */
    public static int NthOffset(int whom, String aWord, String aStr) {
        int lxx = aWord.length(), whar = -2, thar = 0;
        try {
            if (lxx > 0) if (whom >= 0) while (true) {
                whar = aStr.indexOf(aWord, thar);
                if (whar < 0) break;
                whom--;
                if (whom < 0) break;
                thar = thar + lxx;
            }
        } //~while //~try
        catch (Exception ex) {
        }
        return whar;
    } //~NthOffset

    /**
     * A safe (no exceptions) way to extract a word or line from a string.
     * When getting words, excess white space is ignored, so the returned
     * string is empty only when asking for a word beyond the end.
     * Consecutive \n line breaks result in empty lines being returned,
     * so that you always get the nth line of a multi-line string.
     * Words or lines past the end or before the front (0 or less) are empty.
     *
     * @param lino True to get a whole line, false to get a word
     * @param whom The (1-base) line or word number to get
     * @param aStr The string from which to extract that word or line
     * @return The number parsed, or 0 if none
     */
    public static String NthItemOf(boolean lino, int whom, String aStr) {
        char xCh;
        int lxx = aStr.length(), here;
        boolean seen = false;
        String aWord = "";
        whom--; // comes in 1-based, now 0-base
        try {
            if (whom >= 0) for (here = 0; here <= lxx - 1; here++) {
                xCh = CharAt(here, aStr);
                if (xCh <= '\0') break;
                if (xCh <= ' ') {
                    if (!lino) { // one or more spaces..
                        if (seen) whom--;
                        seen = false;
                        if (whom < 0) break;
                        continue;
                    } //~if
                    else if (xCh < ' ') { // any ctl counts as \n or \r
                        whom--;
                        if (whom < 0) break;
                        continue;
                    }
                } //~if
                else seen = true;
                if (whom <= 0) aWord = aWord + xCh;
            } //~for
        } catch (Exception ex) {
        }
        return aWord;
    } //~NthItemOf

    /**
     * A safe (no exceptions) way to extract an floating-point number from
     * a string. Leading white space is ignored, and the first character
     * that is not part of the number ends the scan.
     *
     * @param aStr The string from which to extract that character
     * @return The number parsed, or 0.0 if none
     */
    public static double SafeParseFlt(String aStr) {
        double valu = MyMath.Fix2flt(SafeParseInt(NthItemOf(false, 1, aStr)), 0),
                base = 1.0;
        int fpart = SafeParseInt(fraction), mode = 0;
        String nufract = "" + fpart;
        try {
            if (fpart != 0) {
                while (CharAt(mode, fraction) == '0') mode++;
                mode = nufract.length() + mode;
                if (fpart < 0) mode--;
                while (mode > 0) {
                    base = base * 10.0;
                    mode--;
                } //~while
                valu = valu + MyMath.Fix2flt(fpart, 0) / base;
            }
        } //~if //~try
        catch (Exception ex) {
        }
        fraction = "";
        return valu;
    } //~SafeParseFlt

    /**
     * A safe (no exceptions) way to replace a single line with one or more
     * new lines. A zero or negative line number is the same as no original.
     *
     * @param aLine The original string in which to replace the line
     * @param whom  The (1-base) line or word number to replace
     * @param aStr  The string to replace that line with
     * @return The number parsed, or 0 if none
     */
    public static String RepNthLine(String aLine, int whom, String aStr) {
        char xCh;
        int lxx = aStr.length(), here, thar = 0;
        String aWord = "";
        if (lxx == 0) return aLine;
        if (whom <= 0) return aStr;
        whom--; // comes in 1-based, now 0-base
        try {
            for (here = 0; here <= lxx - 1; here++) {
                xCh = CharAt(here, aStr);
                if (xCh <= '\0') break;
                if (xCh >= ' ') continue;
                whom--;
                if (whom == 0) aWord = aStr.substring(0, here + 1);
                if (whom >= 0) continue;
                thar = here;
                break;
            } //~for
            if (thar > 0) aStr = aStr.substring(thar);
            else aStr = "";
            aStr = aWord + aLine + aStr;
        } //~try
        catch (Exception ex) {
        }
        return aStr;
    } //~RepNthLine

    /**
     * A safe (no exceptions) way to get the rest of a string.
     * Text past the end is considered empty.
     *
     * @param here The (0-base) position to start
     * @param aStr The string from which to extract that substring
     * @return The rest of the string, after whom
     */
    public static String RestOf(int here, String aStr) {
        try {
            if (here < 0) return "";
            if (aStr == "") return "";
            if (here >= aStr.length()) return "";
            return aStr.substring(here);
        } catch (Exception ex) {
        }
        return "";
    } //~RestOf

    /**
     * A safe (no exceptions) way to extract a substring.
     * Text past the end is empty.
     *
     * @param here The (0-base) position to start
     * @param lxx  The desired length (the result could be less)
     * @param aStr The string from which to extract that substring
     * @return The substring
     */
    public static String Substring(int here, int lxx, String aStr) {
        try {
            if (here < 0) return "";
            if (aStr == "") return "";
            if (here >= aStr.length()) return "";
            lxx = here + lxx;
            if (lxx >= aStr.length()) return RestOf(here, aStr);
            return aStr.substring(here, lxx);
        } catch (Exception ex) {
        }
        return "";
    } //~Substring

    /**
     * A safe (no exceptions) way to replace every instance of a particular
     * string in a larger text with another string. An empty string
     * cannot be replaced. Only original instances are replaced;
     * if replacement results in additional instances of the sought string,
     * the new instances will not be replaced.
     *
     * @param aStr The new string to replace it
     * @param xStr The string to find and be replaced
     * @param whom The original string in which to find and replace
     * @return The updated string whom, after all replacements
     */
    public static String ReplacAll(String aStr, String xStr, String whom) {
        int lxx, more = 0, here = 0, thar = 0, skip = 0;
        if (whom == "") return "";
        if (xStr == "") return whom;
        try {
            lxx = xStr.length();
            if (aStr != "") here = aStr.length();
            if (here > lxx) more = here - lxx;
            while (true) {
                here = NthOffset(skip, xStr, whom);
                if (here < 0) break;
                if (here < thar) {
                    skip++;
                    continue;
                }
                if (here > 0) whom = whom.substring(0, here) + aStr
                        + whom.substring(here + lxx);
                else if (aStr == "") whom = whom.substring(here + lxx);
                else whom = aStr + whom.substring(here + lxx);
                thar = thar + more;
            }
        } //~while //~try
        catch (Exception ex) {
        }
        return whom;
    } //~ReplacAll

    /**
     * A safe way to format part of all of an integer array as a text string
     * of integers. The result string is divided into lines so that no line
     * is longer than 80; it is assumed that this will be attached to text of
     * a specified length from another source, which will be considered in
     * folding the first line.
     *
     * @param theAry The array to be formatted
     * @param nw     The maximum number of words to format, or all if 0
     * @param pos    The assumed length of its prefix; if pos<0 then it will
     *               stop after the first 0 value encountered
     * @return The formatted string
     */
    public static String ArrayDumpLine(int[] theAry, int nw, int pos) {
        boolean stop0 = pos < 0; // nw>0 is max +ints to show, =0 to show all
        int nx = 0, zx = 2;  // pos is len of text in front, <0 to stop on 00
        String aLine = "", aWord;
        try {
            if (stop0) pos = -pos;
            if (theAry == null) return " []";
            if (nw > theAry.length) nw = 0;
            if (nw <= 0) nw = theAry.length;
            if (nw > 255) zx++;
            if (nw > 4095) zx++;
            for (nx = 0; nx <= nw - 1; nx++) {
                if (nx < 0) break;
                if (nx >= theAry.length) break;
                aWord = Int2Log(" ", theAry[nx], "");
                if (aWord.length() + pos > 80) {
                    aLine = aLine + Hex2Log("\n  +", nx, zx, "");
                    pos = zx + 6;
                }
                aLine = aLine + aWord;
                if (stop0) if (aWord == " 0") break;
                pos = aWord.length() + pos;
            }
        } catch (Exception ex) {
            aLine = aLine + " [" + nx + "?]";
        }
        return aLine;
    } //~ArrayDumpLine

    /**
     * A safe way to read a whole text file.
     * The result string is divided into lines so that no line is longer than
     * 80; it is assumed that this will be attached to text of a specified
     * length from another source, which will be considered in folding the
     * first line.
     *
     * @param filename The name of the text file
     * @return The text as read
     */
    public static String ReadWholeTextFile(String filename) {
        String content = "";      // adapted from what I found on StackOverflow..
        File myFile = new File(filename); // for ex foo.txt
        FileReader reader = null;
        try {
            reader = new FileReader(myFile);
            char[] chars = new char[(int) myFile.length()];
            reader.read(chars);
            content = new String(chars);
            reader.close();
        } //~try
        catch (Exception ex) {
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (Exception ex) {
            }
        } //~finally
        return content;
    } //~ReadWholeTextFile

    /**
     * Packs 4 bytes into an integer with due respect to Endian-ness.
     *
     * @param LiLEfi True if the data is Little-Endian, false if Big-End
     * @param by0    First data byte
     * @param by1    Secong data byte
     * @param by2    Third data byte
     * @param by3    Fourth data byte
     * @return The reconstructed integer
     */
    public static int Int4bytes(boolean LiLEfi, byte by0, byte by1,
                                byte by2, byte by3) {
        if (HardEndian == 0) {
            HardEndian++;
            if (Int4bytes(false, (byte) 0, (byte) 0, (byte) 0, (byte) 1) != 1)
                HardEndian = -HardEndian;
            System.out.println(Dec2Log("** Int4by = ", HardEndian, " **"));
        } //~if
        if ((HardEndian > 0) == LiLEfi) // flip-Endian..
            return ((((((((int) by3) & 255) << 8) | ((int) by2) & 255) << 8)
                    | ((int) by1) & 255) << 8) | ((int) by0) & 255;
        return ((((((((int) by0) & 255) << 8) | ((int) by1) & 255) << 8) // unflipped..
                | ((int) by2) & 255) << 8) | ((int) by3) & 255;
    } //~Int4bytes

    /**
     * A simple way to read the pixels from a Tiff32 file.
     * Call it twice, first with null to get the size of the image,
     * then with an array big enough to hold as many of those pixels
     * as you want. It may fail if you don't call it the first time.
     *
     * @param filename The name of the Tiff file
     * @param pixels   An array to put the pixels into,
     *                 or null to get only the dimensions
     * @return The height*0x10000+width, or zero if error
     */
    public static int ReadTiff32Image(String filename, int[] pixels) {
        final boolean NoisyMap = DriverCons.D_NoisyMap;
        boolean markt = (pixels != null), logy = !markt, LilEndian = false;
        int tall = TifImDimz >> 16, wide = TifImDimz & 0xFFFF, // rtns (tall,wide) if valid
                sofar = 1024, nx, zx = 0, ixat = 0, here = 0, thar = 0, res = 0,
                whar = 0, rose = 0, nby = 0, samp = 0, lxx = 0, tops = 0, xize = 0,
                prio = 0, info = 0, why = 0;
        byte[] xData = null; // byte[] TifHdr = null;
        File myFile = new File(filename);
        FileInputStream theFile = null;
        String aStr = " = ";
        try {
            while (true) {
                why++; // why = 1
                if (filename == "") break;
                why++; // why = 2
                if (myFile == null) break;
                if (filename != TifFileNm) {
                    markt = false;
                    TifFileNm = "";
                    TifDataOfx = 0;
                    TifImDimz = 0;
                    tall = 0;
                    wide = 0;
                } //~if
                else if (TifDataOfx > 0) {
                    whar = TifDataOfx & 0xFFFFFF;
                    samp = TifDataOfx >> 24;
                } //~if
                else markt = false;
                if (whar > 0) if (whar < sofar) sofar = whar;
                why++; // why = 3
                if (sofar < 8) break;
                why++; // why = 4
                xData = new byte[sofar];
                if (xData == null) break;
                why++; // why = 5
                theFile = new FileInputStream(myFile);
                if (theFile == null) break;
                // markt = theFile.markSupported(); // not in LP/Win10/Eclipse
                // if (markt) theFile.mark(1111); // ("Eclipse" is Greek for "failure" ;-)
                why++; // why = 6
                lxx = theFile.read(xData);
                if (lxx < 8) break;
                why++; // why = 7
                if (xData[0] == 0x49) LilEndian = true; // = 'I' (Intel/inverted)
                else if (xData[0] != 0x4D) break; // 'M' (Motorola/Mac)
                if (LilEndian) info = xData[2];
                else info = xData[3];
                if (info != 0x2A) break; // =42 (otherwise not Tiff)
                if (!markt) { // gotta read the index..
                    why++; // why = 8
                    ixat = Int4bytes(LilEndian, xData[4], xData[5], xData[6], xData[7]);
                    if (ixat < 8) break; // invalid start of index
                    if (ixat + 32 > lxx) break; // didn't get enough to use
                    if (ixat > 555) break; // we don't do files with index at end
                    why++; // why = 9
                    here = ixat - 2;
                    if (here < 0) break;
                    if (here > xData.length - 4) break;
                    why++; // why = 10
                    tops = Int4bytes(LilEndian, xData[here], xData[here + 1], xData[here + 2],
                            xData[here + 3]);
                    if (LilEndian) tops = tops >> 16;
                    else tops = MyMath.SgnExt(tops);
                    if (tops < 4) break; // didn't get enough index to use
                    else if (tops * 12 + 12 > lxx) break; // index bigger than what we got
                    why++; // why = 11
                    here = ixat + 2;
                    for (nx = tops - 1; nx >= 0; nx += -1) { // extract Tiff info..
                        // see: http://cool.conservation-us.org/bytopic/imaging/std/tiff5.html
                        prio = why;
                        info = Int4bytes(LilEndian, xData[here - 2], xData[here - 1], xData[here],
                                xData[here + 1]);
                        thar = here;                   // log ->  @@ thar: info = zx aStr
                        if (LilEndian) info = info >> 16;
                        else info = MyMath.SgnExt(info);
                        here = here + 8;
                        aStr = "";
                        if (info == 256) { // ImageWidth
                            if (logy) aStr = " ImageWidth";
                            zx = Int4bytes(LilEndian, xData[here], xData[here + 1],
                                    xData[here + 2], xData[here + 3]);
                            wide = zx;
                        } //~if
                        else if (info == 257) { // ImageLength
                            if (logy) aStr = " ImageLength";
                            zx = Int4bytes(LilEndian, xData[here], xData[here + 1],
                                    xData[here + 2], xData[here + 3]);
                            tall = zx;
                        } //~if
                        else if (info == 259) { // Compression (must be =1)
                            if (logy) aStr = " Compression=1";
                            zx = Int4bytes(LilEndian, xData[here], xData[here + 1], xData[here + 2],
                                    xData[here + 3]);
                            if ((zx & ~0x01010001) != 0) why = why | 256;
                        } //~if
                        else if (info == 262) { // PhotometricInterpretation (must be =2)
                            if (logy) aStr = " PhotometricInterpretation=2";
                            zx = Int4bytes(LilEndian, xData[here], xData[here + 1], xData[here + 2],
                                    xData[here + 3]);
                            if ((zx == 0) || ((zx & ~0x02020002) != 0)) why = why | 512;
                        } //~if
                        else if (info == 273) { // StripOffsets (must be 1 strip)
                            if (logy) aStr = " StripOffsets";
                            whar = Int4bytes(LilEndian, xData[here], xData[here + 1],
                                    xData[here + 2], xData[here + 3]);
                            zx = Int4bytes(LilEndian, xData[here - 4], xData[here - 3],
                                    xData[here - 2], xData[here - 1]);
                            if ((zx & ~0x01010001) == 0) zx = whar;
                            else why = why | 1024;
                        } //~if
                        else if (info == 277) { // SamplesPerPixel (must be =3 or =4)
                            if (logy) aStr = " SamplesPerPixel";
                            samp = Int4bytes(LilEndian, xData[here], xData[here + 1],
                                    xData[here + 2], xData[here + 3]) >> 16;
                            zx = Int4bytes(LilEndian, xData[here - 4], xData[here - 3],
                                    xData[here - 2], xData[here - 1]);
                            if ((zx & ~0x01010001) == 0) zx = samp;
                            else why = why | 2048;
                        } //~if
                        else if (info == 278) { // RowsPerStrip (must = tall)
                            if (logy) aStr = " RowsPerStrip";
                            zx = Int4bytes(LilEndian, xData[here], xData[here + 1], xData[here + 2],
                                    xData[here + 3]);
                            if (zx != tall) why = why | 4096;
                        } //~if
                        else if (info == 279) { // StripByteCounts
                            if (logy) aStr = " StripByteCounts";
                            zx = Int4bytes(LilEndian, xData[here], xData[here + 1],
                                    xData[here + 2], xData[here + 3]);
                            nby = zx;
                        } //~if
                        else if (info == 284) { // PlanarConfiguration (must be =1)
                            if (logy) aStr = " PlanarConfiguration=1";
                            zx = Int4bytes(LilEndian, xData[here], xData[here + 1], xData[here + 2],
                                    xData[here + 3]);
                            if ((zx & ~0x01010001) != 0) why = why | 8192;
                        } //~if
                        else if (info == 320) why = why | 128; // ColorMap (should be none)
                        if (logy) System.out.println(Dec2Log("  @@ ", thar, Int2Log(": ", info,
                                Int2Log(" = ", zx, IffyStr(why == prio, aStr,
                                        Int2Log(" ! ", why, aStr))))));
                        here = here + 4;
                    } //~for // (extract Tiff info)
                    aStr = " = ";
                    if (why > 11) break;
                    why++; // why = 12
                    if (samp < 3) break;
                    if (samp > 4) break;
                    why++; // why = 13
                    if (wide < 16) break;
                    if (wide > 4096) break;
                    why++; // why = 14
                    if (tall < 16) break;
                    if (tall > 4096) break;
                    why++; // why = 15
                    if (tall * wide * samp != nby) break;
                } //~if // (gotta read the index)
                why = 16; // aStr = " = ";
                if (samp < 3) break;
                if (samp > 4) break;
                why++; // why = 17
                if (wide < 16) break;
                if (wide > 4096) break;
                why++; // why = 18
                if (tall < 16) break;
                if (tall > 4096) break;
                if (tall + wide > 8111) break;
                why++; // why = 19
                nby = tall * wide;
                if (whar < 8) break;
                if (whar > 999999) break;
                why++; // why = 20
                res = (tall << 16) + wide;
                if (pixels == null) {
                    theFile.close();
                    theFile = null;
                    if (!markt) {
                        TifFileNm = filename;
                        TifDataOfx = (samp << 24) + whar;
                        TifImDimz = res;
                    } //~if
                    if (nby <= 0) lxx = 0;
                        // else if (samp==4) lxx = -nby>>2;
                        // else if (samp==3) lxx = -(nby/3);
                    else lxx = -nby;
                    why = 0;
                    break;
                } //~if
                why++; // why = 21
                whar = whar - sofar;
                if (whar < 0) break;
                why++; // why = 22
                if (whar > 0)
                    theFile.skip((long) whar);
                why++; // why = 23
                xData = new byte[nby * samp];
                lxx = theFile.read(xData);
                if (lxx > 0) aStr = "+1K = ";
                if (samp == 4) lxx = lxx >> 2;
                else if (samp == 3) lxx = lxx / 3;
                else break;
                why++; // why = 24
                if (lxx < nby) break;
                theFile.close();
                theFile = null;
                why++; // why = 25
                here = 0;
                lxx = MyMath.iMin(lxx, pixels.length) - 1;
                for (thar = 0; thar <= lxx; thar++) {
                    info = Int4bytes(false, (byte) 0, xData[here], xData[here + 1], xData[here + 2]);
                    here = here + samp;
                    pixels[thar] = info;
                } //~for
                why = 0;
                break;
            } //~while
            if (theFile != null) theFile.close(); // if (why != 16)
        } catch (Exception ex) {
            aStr = " = ";
            why = -why;
        }
        filename = " '" + filename + "'";
        System.out.println(TF2Log("RdTif32Im = ", pixels != null,     // aStr=" = "
                Dec2Log(" ", tall, Dec2Log("/", wide, Dec2Log(" ", lxx,
                        IffyStr((why == 0) && !NoisyMap, filename, Dec2Log(" #", tops,
                                Dec2Log(" @ ", ixat, Dec2Log(" s", samp, TF2Log(" ", LilEndian,
                                        Dec2Log(" @ ", whar, Int2Log(aStr, why, filename)))))))))))); // why =
        if (why != 0) {
            TifFileNm = "";
            TifDataOfx = 0;
            TifImDimz = 0;
            res = 0;
        } //~if
        return res;
    } //~ReadTiff32Image

    /**
     * Formats a fixed.point integer with three bits of fraction and returns it
     * as a precise decimal number with a prefix and suffix. Similar to Flt2Log.
     *
     * @param before The prefix
     * @param whom   The number to be formatted
     * @param after  The suffix
     * @return The combined string
     */
    public static String Fixt8th(String before, int whom, String after) {
        try {
            if (whom == 0) return before + "0." + after;
            if (whom < 0) return before + Fixt8th("-", -whom, after);
            before = before + (whom >> 3);
            whom = whom & 7;
            if (whom == 0) return before + ".0" + after;
            if (whom == 4) return before + ".5" + after;
            if ((whom & 1) == 0) return before + Dec2Log(".", (whom >> 1) * 25, after);
            return before + Dec2Log(".", whom * 125, after);
        } catch (Exception ex) {
        }
        return before + ".?" + after;
    } //~Fixt8th
} //~HandyOps (apw3) (HO)

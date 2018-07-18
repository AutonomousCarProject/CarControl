// The actual class FlyCamera reads images from camera..   -- 2017 February 27

//   You need both FlyCapture2_C.dll and FlyCapture2.dll in your Java project folder

package com.apw.pedestrians; // (same API as fly0cam)

public class PedestrianFlyCamera {
    public static final int FrameRate_15 = 3, FrameRate_30 = 4;
    private static final int HDRCtrl = 0x1800;
    private static final int HDRShutter1 = 0x1820;
    private static final int HDRShutter2 = 0x1840;
    private static final int HDRShutter3 = 0x1860;
    // at
    // start,
    // sets
    // rose,colz,tile
    private static final int HDRShutter4 = 0x1880;
    private static final int HDRGain1 = 0x1824;
    private static final int HDRGain2 = 0x1844;
    private static final int HDRGain3 = 0x1864;
    private static final int HDRGain4 = 0x1884;
    private static final int HDROn = 0x82000000;
    private static final int HDROff = 0x80000000;
    private static final int FC2_ERROR_OK = 0;

    static {
        System.loadLibrary("FlyCamera");
    }

    public int debug = 50, debug2 = 50;
    private int rose, // actual number of rows = FlyCap2.fc2Image.rows/2
            colz, // actual number of columns = FlyCap2.fc2Image.cols/2
            tile, // see FlyCapture2Defs.fc2BayerTileFormat
            errn; // returns an error number, see ErrorNumberText()
    private long stuff; // used for error reporting, or not at all
    public PedestrianFlyCamera() {
        rose = 0;
        colz = 0;
        tile = 0;
        errn = 0;
    }

    public static String ErrorNumberText(int errno) { // to explain errn in toString()
        if (errno == -20) return "ByteArray is not same size as received data";
        switch (errno) {
            case -1:
                return "No camera connected";
            case 0:
                return "No error";
            case 1:
                return "fc2CreateContext failed";
            case 2:
                return "fc2GetNumOfCameras failed";
            case 3:
                return "No cameras detected";
            case 4:
                return "fc2GetCameraFromIndex did not find first camera";
            case 5:
                return "fc2Connect failed to connect to first camera";
            case 6:
                return "fc2StartCapture failed";
            case 7:
                return "fc2CreateImage failed";
            case 8:
                return "No error";
            case 9:
                return "fc2RetrieveBuffer failed";
            case 10:
                return "rawImage.format = 0 (probably unset)";
            case 11:
                return "ByteArray to NextFrame is null";
            case 12:
                return "Connect failed or not called (context == null)";
            case 13:
                return "Something in context corrupted";
            case 14:
                return "ByteArray is way too short or too long";
            case 15:
                return "GetByteArrayElements failed (couldn't access bytes)";
            case 16:
                return "fc2RetrieveBuffer failed, possibly timeout";
            case 17:
                return "fc2GetImageData failed";
            case 18:
                return "No pixel data received";
            case 19:
                return "Unknown camera image size";
            case 20:
                return "No error";
            case 21:
                return "fc2StopCapture failed";
            case 22:
                return "fc2DestroyImage failed";
            case 23:
                return "Both fc2StopCapture and fc2DestroyImage failed";
            case 24:
                return "fc2CreateImage failed (RGB)";
            case 25:
                return "fc2ConvertImageTo (RGB) failed";
            case 26:
                return "fc2GetProperty failed";
            case 27:
                return "Unknown frame rate";
            case 28:
                return "fc2SetProperty failed";
        } // ~switch
        return "fc2RetrieveBuffer probably returned some format other than Bayer8";
    } // ~ErrorNumberText

    public static void main(String[] args) { // to test the interface
        int tall = 0, wide = 0, pix = -1;
        short[] buff;
        PedestrianFlyCamera hello = new PedestrianFlyCamera();
        if (hello.Connect(0, 0, 0, 0)) tall = hello.Dimz();
        wide = tall & 0xFFFF;
        tall = tall >> 16;
        if (tall > 0) if (wide > 0) { // we got reasonable image size, get one image..
            buff = new short[tall * wide * 4];
            if (hello.NextFrame(buff)) // got an image, extract 1st pixel..
                pix = (((int) buff[0]) << 24) | ((((int) buff[1]) & 255) << 16)
                        | ((((int) buff[wide + wide]) & 255) << 8) | (((int) buff[wide + wide + 1]) & 255);
            else
                pix = 0; // no image came
            // set breakpoint here to look in the debugger, or else log pix
            wide = pix;
        } // otherwise Java complains that it's unused
        hello.Finish();
        pix = 0;
    } // ~main

    public native boolean Connect(int frameRate, int exposure, int shutter, int gain); // required

    public native int SetExposure(int exposure);

    public native int GetExposure();

    public native int SetShutter(int shutter);

    public native int GetShutter();

    public native int SetGain(int gain);

    public native int GetGain();

    public void SetHDR(long[] HDRShutters, long[] HDRGains) {
        // Initialize HDR Registers
        SafeWriteRegister(new long[][]{{HDRShutter1, HDRShutters[0]}, {HDRShutter2, HDRShutters[1]}, {HDRShutter3, HDRShutters[2]}, {HDRShutter4, HDRShutters[3]},
                {HDRGain1, HDRGains[0]}, {HDRGain2, HDRGains[1]}, {HDRGain3, HDRGains[2]}, {HDRGain4, HDRGains[3]},
                {HDRCtrl, HDROn}}, "Error writing HDR shutter/gain registers");
    }

    public void SafeWriteRegister(long addr, long val, String err) {
        if (WriteRegister(addr, val) != FC2_ERROR_OK) {
            throw new IllegalStateException(err + " (error code: " + -val + ").");
        }
    }

    public void SafeWriteRegister(long addrsVals[][], String err) {
        for (long addrVal[] : addrsVals) {
            int error;
            if ((error = WriteRegister(addrVal[0], addrVal[1])) != FC2_ERROR_OK) {
                throw new IllegalStateException(err + " (error code: " + -error + ").");
            }
        }
    }

    public long SafeReadRegister(long address, String err) {
        long val = ReadRegister(address);

        if (val < 0) {
            throw new IllegalStateException(err + " (error code: " + -val + ").");
        }

        return val;
    }

    public long[] SafeReadRegisterBlock(long addressHigh, long addressLow, String err) {
        long[] vals = ReadRegisterBlock(addressHigh, addressLow);

        if (vals.length == 1 && vals[0] < 0) {
            throw new IllegalStateException(err + " (error code: " + -vals[0] + ").");
        }

        return vals;
    }
    // can't

    public native int WriteRegister(long address, long val);

    public native long ReadRegister(long address);

    public native long[] ReadRegisterBlock(long addressHigh, long addressLow);

    public native boolean NextFrame(short[] pixels); // fills pixels, false if

    public native void Finish(); // required at end to prevent memory leaks

    public int Dimz() {
        return (rose << 16) + colz;
    } // access to image size from camera

    public int PixTile() {
        return tile;
    } // image Bayer encoding, frex RG/GB = 1, GB/RG = 3

    public String toString() {
        return "fly2cam.PedestrianFlyCamera " + ErrorNumberText(errn);
    }

    public boolean Live() {
        return tile > 0;
    } // we have a live camera (fly2cam)
} // ~FlyCamera (fly2cam) (F2)

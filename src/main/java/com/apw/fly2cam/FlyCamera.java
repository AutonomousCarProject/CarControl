// The actual class FlyCamera reads images from camera.. // 2018 May 14

// You also need FlyCapture2_C.dll + FlyCapture2.dll in your Java project folder

package com.apw.fly2cam; // (same API as fly0cam, same as 2017 Feb 27 but subclassable)

public class FlyCamera { // (in Java/fly2cam)
    public static final int FrameRate_15 = 3, FrameRate_30 = 4,
            BaseRose = 480, BaseColz = 640, BaseTile = 1;
    protected

    int rose, // (fly2cam) actual number of rows = FlyCap2.fc2Image.rows/2
            colz, // actual number of columns = FlyCap2.fc2Image.cols/2
            tile, // see FlyCapture2Defs.fc2BayerTileFormat
            FrameNo, // counts the number of good frames seen (nobody looks)
            errn; // returns an error number, see ErrorNumberText()
    private long stuff; // used for error reporting, or not at all

    //static {System.loadLibrary("fly2cam/FlyCamera");} // comment this line out if no DLLs

    public FlyCamera() { // (in Java/fly2cam)
        FrameNo = 0;
        rose = 0;
        colz = 0;
        tile = 0;
        errn = 0;
    }

    /**
     * Gets a text description of an error number.
     *
     * @param errno The error number from one of the other API calls
     * @return The text description
     */
    public static String ErrorNumberText(int errno) { // errn -> String (fly2cam)
        // if (errn==0) errno = errn;
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
        } //~switch
        return "fc2RetrieveBuffer probably returned some format other than Bayer8";
    } //~ErrorNumberText

    public static void main(String[] args) { // to test the interface (fly2cam)
        int tall = 0, wide = 0, pix = -1;
        byte[] buff;
        FlyCamera hello = new FlyCamera();
        if (hello.Connect(0)) tall = hello.Dimz();
        wide = tall & 0xFFFF;
        tall = tall >> 16;
        if (tall > 0) if (wide > 0) { // we got reasonable image size, get one image..
            buff = new byte[tall * wide * 4];
            if (hello.NextFrame(buff)) // got an image, extract 1st pixel..
                pix = (((int) buff[0]) << 24) | ((((int) buff[1]) & 255) << 16)
                        | ((((int) buff[wide + wide]) & 255) << 8) | (((int) buff[wide + wide + 1]) & 255);
            else pix = 0; // no image came
            // set breakpoint here to look in the debugger, or else log pix
            wide = pix;
        } // otherwise Java complains that it's unused
        hello.Finish();
        pix = 0;
    } //~main

    /**
     * Start a new camera session with the specified frame rate.
     *
     * @param frameRate =4 for 30 fps, =3 for 15, =2 for 7.5 pfs
     * @return True if success, false otherwise
     */
    public native boolean Connect(int frameRate); // required, sets rose,colz,tile

    /**
     * Gets one frame from the Chameleon3 or FireFly camera.
     *
     * @param pixels Fills this array with pixels in Bayer8 encoding
     * @return True if success, false otherwise
     */
    public native boolean NextFrame(byte[] pixels); // fills pixels, false if cant

    /**
     * Terminate the session.
     * Required by Flir/Pt.Grey drivers to prevent memory leaks.
     */
    public native void Finish(); // required at end to prevent memory leaks

    /**
     * Gets the image size (rows and columns) for this camera.
     *
     * @return The number of pixel rows in the high 16, columns in low 16
     */
    public int Dimz() {
        return (rose << 16) + colz;
    } // access cam image size (fly2cam)

    /**
     * Gets the Bayer8 encoding number for this camera.
     *
     * @return The Bayer8 encoding number 1 (RG/GB) to 4
     */
    public int PixTile() {
        return tile;
    } // Bayer encoding, frex RG/GB =1, GB/RG =3

    /**
     * Tells if this camera is live or not.
     *
     * @return true if this camera is connected, false otherwise
     */
    public boolean Live() {
        return tile > 0;
    } // we have a live camera (fly2cam)

    public String toString() { // (fly2cam)
        return "fly2cam.FlyCam " + errn + ": " + ErrorNumberText(errn);
    }
} //~FlyCamera (fly2cam) (F2)

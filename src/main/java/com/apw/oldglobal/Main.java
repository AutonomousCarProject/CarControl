package com.apw.oldglobal;

import com.apw.fly2cam.OldFlyCamera;

public class Main
{
    public static void main(String[] args)
    {
        OldFlyCamera cam = new OldFlyCamera();
        cam.Connect(OldFlyCamera.FrameRate_30, 0, 0, 0);
        
        int dimz = cam.Dimz();
        int height = dimz >> 16;
        int width = (dimz << 16) >> 16;
        
        short[] bites = new short[width * height * 4];
        System.out.println(cam.NextFrame(bites));

        int r = (bites[width * 2] & 0xffff) >> 4;
        int g = (bites[0] & 0xffff) >> 4;
        int b = (bites[1] & 0xffff) >> 4;
    }
}

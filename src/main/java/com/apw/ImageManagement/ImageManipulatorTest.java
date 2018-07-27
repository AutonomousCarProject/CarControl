package com.apw.ImageManagement;

import com.apw.apw3.SimCamera;

public class ImageManipulatorTest {
    public static void main(String[] args){
        long start;
        ImageManager im = new ImageManager(new SimCamera());
        start = System.nanoTime();
        for(int i = 0; i<20;i++){
            im.getMonochrome2Raster();
        }
        start=System.nanoTime()-start;
        System.out.println(start);
        start = System.nanoTime();
        for(int i = 0; i<20;i++){
            im.getBlackWhiteRaster();
        }
        start=System.nanoTime()-start;
        System.out.println(start);
    }
}

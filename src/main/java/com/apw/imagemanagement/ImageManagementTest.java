package com.apw.imagemanagement;

import com.apw.apw3.TrakSim;
import com.apw.carcontrol.TrakSimControl;
import com.apw.sbcio.fakefirm.ArduinoIO;

import java.lang.reflect.Method;

public class ImageManagementTest {

    public static void main(String[] args){
        boolean millis = true;
        boolean recall = true;
        try {
            //Thread.currentThread().sleep(20000);
        }catch(Exception e){

        }
        long time = System.currentTimeMillis();
        ArduinoIO driveSys = new ArduinoIO();
        int amount = (int)Math.pow(10,1);
        new TrakSim().SimStep(1);
        TrakSimControl simControl = new TrakSimControl(driveSys);
        simControl.updateWindowDims(912,480);
        ImageManagementModule m = new ImageManagementModule(simControl.getWindowWidth(),simControl.getWindowHeight(),simControl.getTile());

        if(millis){
                System.out.println();
                System.out.println("INIT");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
            for(int i=0;i<amount;i++)
                m.setupArrays(simControl.readCameraImage());
            System.out.println();
            System.out.println("ARRAY");
            System.out.println(System.nanoTime()-time);
            time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getRGBRaster();
                System.out.println();
                System.out.println("RGB");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getMonochromeRaster();
                System.out.println();
                System.out.println("MONO");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getMonochrome2Raster();
                System.out.println();
                System.out.println("MONO2");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getBlackWhiteRaster();
                System.out.println();
                System.out.println("BW");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getSimpleColorRaster();
                System.out.println();
                System.out.println("SIMPLE");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getMonoRGBRaster();
                System.out.println();
                System.out.println("MONO-RGB");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getSimpleRGBRaster();
                System.out.println();
                System.out.println("SIMPLE-RGB");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getBWRGBRaster();
                System.out.println();
                System.out.println("BW-RGB");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getRobertsCross();
                System.out.println();
                System.out.println("ROBERTS");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getRoad();
                System.out.println();
                System.out.println("ROAD");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getEdgeBlackWhiteRaster();
                System.out.println();
                System.out.println("BW-EDGE");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.update(simControl);
                System.out.println();
                System.out.println("UPDATE");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
        }else{
                System.out.println();
                System.out.println("INIT");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
            for(int i=0;i<amount;i++)
                m.setupArrays(simControl.readCameraImage());
            System.out.println();
            System.out.println("ARRAY");
            System.out.println(System.nanoTime()-time);
            time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getRGBRaster();
                System.out.println();
                System.out.println("RGB");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getMonochromeRaster();
                System.out.println();
                System.out.println("MONO");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getMonochrome2Raster();
                System.out.println();
                System.out.println("MONO2");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getBlackWhiteRaster();
                System.out.println();
                System.out.println("BW");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getSimpleColorRaster();
                System.out.println();
                System.out.println("SIMPLE");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getMonoRGBRaster();
                System.out.println();
                System.out.println("MONO-RGB");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getSimpleRGBRaster();
                System.out.println();
                System.out.println("SIMPLE-RGB");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getBWRGBRaster();
                System.out.println();
                System.out.println("BW-RGB");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getRobertsCross();
                System.out.println();
                System.out.println("ROBERTS");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getRoad();
                System.out.println();
                System.out.println("ROAD");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getEdgeBlackWhiteRaster();
                System.out.println();
                System.out.println("BW-EDGE");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.update(simControl);
                System.out.println();
                System.out.println("UPDATE");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
            }
        }
    }

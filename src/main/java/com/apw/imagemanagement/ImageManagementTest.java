package com.apw.imagemanagement;

import com.apw.apw3.TrakSim;
import com.apw.carcontrol.TrakSimControl;
import com.apw.sbcio.fakefirm.ArduinoIO;

import java.lang.reflect.Method;

public class ImageManagementTest {

    public static void main(String[] args){
        boolean millis = true;
        boolean recall = true;
        long time = System.currentTimeMillis();
        ArduinoIO driveSys = new ArduinoIO(false);
        int amount = (int)Math.pow(10,6);
        //new TrakSim();
        TrakSimControl simControl = new TrakSimControl(driveSys);
        byte[] p = simControl.getRecentCameraImage();
        ImageManagementModule m = new ImageManagementModule(simControl.getWindowWidth(),simControl.getWindowHeight(),simControl.getTile());

        if(millis){
            if(recall){
                System.out.println();
                System.out.println("INIT");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getRGBRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("RGB");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getMonochromeRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("MONO");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getMonochrome2Raster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("MONO2");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getBlackWhiteRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("BW");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getSimpleColorRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("SIMPLE");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getMonoRGBRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("MONO-RGB");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getSimpleRGBRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("SIMPLE-RGB");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getBWRGBRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("BW-RGB");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getRobertsCross(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("ROBERTS");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getRoad(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("ROAD");
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
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getRGBRaster(p);
                System.out.println();
                System.out.println("RGB");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getMonochromeRaster(p);
                System.out.println();
                System.out.println("MONO");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getMonochrome2Raster(p);
                System.out.println();
                System.out.println("MONO2");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getBlackWhiteRaster(p);
                System.out.println();
                System.out.println("BW");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getSimpleColorRaster(p);
                System.out.println();
                System.out.println("SIMPLE");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getMonoRGBRaster(p);
                System.out.println();
                System.out.println("MONO-RGB");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getSimpleRGBRaster(p);
                System.out.println();
                System.out.println("SIMPLE-RGB");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getBWRGBRaster(p);
                System.out.println();
                System.out.println("BW-RGB");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getRobertsCross(p);
                System.out.println();
                System.out.println("ROBERTS");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.getRoad(p);
                System.out.println();
                System.out.println("ROAD");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
                for(int i=0;i<amount;i++)
                    m.update(simControl);
                System.out.println();
                System.out.println("UPDATE");
                System.out.println(System.currentTimeMillis()-time);
                time = System.currentTimeMillis();
            }
        }else{
            if(recall){
                System.out.println();
                System.out.println("INIT");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getRGBRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("RGB");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getMonochromeRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("MONO");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getMonochrome2Raster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("MONO2");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getBlackWhiteRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("BW");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getSimpleColorRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("SIMPLE");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getMonoRGBRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("MONO-RGB");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getSimpleRGBRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("SIMPLE-RGB");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getBWRGBRaster(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("BW-RGB");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getRobertsCross(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("ROBERTS");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getRoad(simControl.getRecentCameraImage());
                System.out.println();
                System.out.println("ROAD");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.update(simControl);
                System.out.println();
                System.out.println("UPDATE");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
            }else{
                System.out.println();
                System.out.println("INIT");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getRGBRaster(p);
                System.out.println();
                System.out.println("RGB");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getMonochromeRaster(p);
                System.out.println();
                System.out.println("MONO");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getMonochrome2Raster(p);
                System.out.println();
                System.out.println("MONO2");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getBlackWhiteRaster(p);
                System.out.println();
                System.out.println("BW");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getSimpleColorRaster(p);
                System.out.println();
                System.out.println("SIMPLE");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getMonoRGBRaster(p);
                System.out.println();
                System.out.println("MONO-RGB");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getSimpleRGBRaster(p);
                System.out.println();
                System.out.println("SIMPLE-RGB");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getBWRGBRaster(p);
                System.out.println();
                System.out.println("BW-RGB");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getRobertsCross(p);
                System.out.println();
                System.out.println("ROBERTS");
                System.out.println(System.nanoTime()-time);
                time = System.nanoTime();
                for(int i=0;i<amount;i++)
                    m.getRoad(p);
                System.out.println();
                System.out.println("ROAD");
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
}

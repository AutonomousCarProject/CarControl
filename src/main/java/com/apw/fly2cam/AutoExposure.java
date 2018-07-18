package com.apw.fly2cam;

import com.apw.oldglobal.Log;
import com.apw.oldimage.IImage;
import com.apw.oldimage.IPixel;
import com.apw.oldimage.Image;

public class AutoExposure implements IAutoExposure
{
    private int framerate;
    private int currentFrame;
    private OldFlyCamera flyCam;

    private static final int BLOCK_SIZE = 64;
    private static final int SAMPLE_SIZE = 1;
    private static final int MAX_BOOST = 10;

    public AutoExposure(IImage image, int framerate)
    {
        if(image instanceof Image)
        {
            flyCam = ((Image) image).flyCam;
        }
        else
        {
            flyCam = null;
        }
        
        if(framerate < 2)
        {
            throw new IllegalArgumentException("AutoExposure framerate must be at least 2.");
        }
        
        this.framerate = framerate;
        this.currentFrame = framerate - 2;
    }
    
    @Override
    public void autoAdjust(IPixel[][] pixels)
    {
        if (flyCam == null) return;
        if(++currentFrame == framerate - 1)
        {
            new Thread(() -> flyCam.SetShutter(2000)).start();
            return;
        }
        
        if(currentFrame != framerate) return;
        
        currentFrame = 0;

        final int height = pixels.length;
        final int width = pixels[0].length;

        final int mult = (765 * height * width) / (SAMPLE_SIZE * SAMPLE_SIZE);
        final int darkThresh = (int) (mult * 0.4f);
        final int lightThresh = (int) (mult * 0.6f);
        
        int sum = 0;
        for (int i1 = 0; i1 < width; i1 += BLOCK_SIZE)
        {
            for (int j1 = 0; j1 < height; j1 += BLOCK_SIZE)
            {
                int totalBrightness = 0;
                for (int i2 = 0; i2 < BLOCK_SIZE; i2 += SAMPLE_SIZE)
                {
                    for (int j2 = 0; j2 < BLOCK_SIZE; j2 += SAMPLE_SIZE)
                    {
                        final int i = i1 + i2;
                        final int j = j1 + j2;
                        
                        if(i >= width) continue;
                        if(j >= height) continue;    

                        IPixel pixel = pixels[j][i];
                        totalBrightness += pixel.getRed() + pixel.getGreen() + pixel.getBlue();
                    }
                }

                if (totalBrightness <= darkThresh)
                {
                    sum--;
                }
                else if (totalBrightness >= lightThresh)
                {
                    sum++;
                }
            }
        }

        final int maxSum = (int) (Math.ceil(width / (float) BLOCK_SIZE) * Math.ceil(height / (float) BLOCK_SIZE));
        final float sumRatio = (float) sum / maxSum;

        final int shutterBoost = Math.round(-MAX_BOOST * sumRatio);
        
        Log.d("Shutter boost", shutterBoost);
        
        Thread t = new Thread(() -> 
        {
//            try
            {
                Log.d("Current shutter", flyCam.GetShutter());
                flyCam.SetShutter(flyCam.GetShutter() + shutterBoost);
            }
//            catch (InterruptedException e)
//            {
//                e.printStackTrace();
//            }
        });
        t.start();
    }
}

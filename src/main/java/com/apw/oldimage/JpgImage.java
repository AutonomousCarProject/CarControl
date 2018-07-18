package com.apw.oldimage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//Defines image as an 2d array of pixels
public class JpgImage implements IImage {
    private static final int rMask = 0b00000000_11111111_00000000_00000000;
    private static final int gMask = 0b00000000_00000000_11111111_00000000;
    private static final int bMask = 0b00000000_00000000_00000000_11111111;
    BufferedImage img = null;
    private IPixel[][] image;

    public JpgImage(String fileName) {
        try {
            img = ImageIO.read(new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        image = new IPixel[img.getWidth()][img.getHeight()];
    }

    @Override
    public IPixel[][] getImage() {
        return image;
    }

    @Override
    public void setImage(IPixel[][] image) {
        // TODO Auto-generated method stub

    }

    // gets a single frame
    @Override
    public void readCam() {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb & rMask) >> 16;
                int g = (rgb & gMask) >> 8;
                int b = rgb & bMask;

                image[x][y] = new Pixel((short) r, (short) g, (short) b);

                // if (x == 7 && y == 7)
                // {
                // image[x][y] = new Pixel((short) 255, (short) 0, (short) 0);
                // }
            }
        }
    }

    public void autoColor() {
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAutoFreq(int autoFreq) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getFrameNo() {
        return 0;
    }
}

package com.apw.Steering;

import com.apw.ImageManagement.ImageManager;

@Deprecated
public class SteerControlCheck {

    private ImageManager imageManager;
    private int ncols;

    public SteerControlCheck(ImageManager imageManager, int ncols) {
        this.imageManager = imageManager;
        this.ncols = ncols;
    }

    public int getDegreeOffset() {
        //return averagePxl();
        return edgePxl();
    }

    private int edgePxl() {
        int count = 0;
        double sum = 0;
        byte[] whitePxls = imageManager.getBlackWhiteRaster();
        int basestart = 300 * ncols;
        for (int c = 320; c < 640; c++) {
            if (whitePxls[c + basestart] == 1) {
                sum += c;
                count++;
                break;
            }

        }
        for (int c = 320; c > 0; c--) {
            if (whitePxls[c + basestart] == 1) {
                sum += c;
                count++;
                break;
            }
        }
        if (count != 0) {
            sum /= count;
        } else {
            return 0;
        }
        sum -= 320;
        return (int) ((1.0 / 5.2) * sum / Math.abs(sum) * Math.pow(Math.abs(sum), 1.0 / 2.5));
    }

    private int averagePxl() {
        int count = 0;
        double sum = 0;
        byte[] whitePxls = imageManager.getBlackWhiteRaster();
        int basestart = 295 * ncols;
        for (int c = 0; c < 640; c++) {
            if (whitePxls[c + basestart] == 1) {
                count++;
                sum += c;
            }

        }
        if (count != 0) {
            sum /= count;
        } else {
            return 0;
        }
        sum -= 320;
        return -(int) (sum / Math.abs(sum) * Math.pow(Math.abs(sum), 1.0 / 3));
    }
}

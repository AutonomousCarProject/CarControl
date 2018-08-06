package com.apw.gpu;

import com.aparapi.Kernel;
import com.apw.imagemanagement.SimpleThresholds;

public class ImageKernels {

    private static double luminanceMultiplier = 1;

    private static int getBit(byte tile, int pos) {
        return (tile >> pos) & 1;
    }

    public static int boolBit(boolean check) {
        if (check) return 1;
        return 0;
    }

    private static int getPos(int x, int y, byte tile, int ncols, int nrows) {
        return (y * ncols * (4 - getBit(tile, 2)) + (2 + getBit(tile, 2)) * x + getBit(tile, 1) * (2 * ncols - (2 * ncols - 1) * getBit(tile, 2)) + getBit(tile, 0)) % ((4 - getBit(tile, 2)) * ncols * nrows);
    }

    private static byte combineTile(byte tile1, byte tile2) {
        return (byte) (((int) tile1) ^ ((int) tile2));
    }

    public static class BlackWhiteRasterKernel extends Kernel {

        byte[] bayer;
        byte[] mono;
        int nrows;
        int ncols;
        byte tile;

        int averageLuminance;

        @Override
        public void run() {
            System.out.println("BlackWhiteRasterKernel.run");
            int row = getGlobalId(0);
            int col = getGlobalId(1);
            if (col == 0)
                averageLuminance = 0;
            int R = (bayer[getPos(col, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
            int G = (bayer[getPos(col, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
            int B = (bayer[getPos(col, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);
            averageLuminance += (R + G + B) / 3;

            if (col == 0)
                averageLuminance /= ncols;

            if (col < ncols) {
                int R1 = (bayer[getPos(col, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
                int G1 = (bayer[getPos(col, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
                int B1 = (bayer[getPos(col, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);
                int R2 = (bayer[getPos(col + 1, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
                int G2 = (bayer[getPos(col + 1, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
                int B2 = (bayer[getPos(col + 1, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);
                int R3 = (bayer[getPos(col + 2, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
                int G3 = (bayer[getPos(col + 2, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
                int B3 = (bayer[getPos(col + 2, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);

                int pix = (R1 + R2 + R3 + B1 + B2 + B3 + G1 + G2 + G3) / 9;
                // int pix = (R2 + G2 + B2)/3;
                if (!(col + 1 >= 640 || row < 240 || row > 455)) {
                    if (pix > luminanceMultiplier * averageLuminance) {
                        mono[row * ncols + col + 1] = 1;
                    } else {
                        mono[row * ncols + col + 1] = 0;
                    }
                } else {
                    mono[row * ncols + col + 1] = 0;
                }
            }
        }
    }

    public static class BlackWhite2RasterKernel extends Kernel {

        byte[] bayer;
        byte[] mono;
        int nrows;
        int ncols;
        byte tile;

        int averageLuminance;

        @Override
        public void run() {
            System.out.println("BlackWhite2RasterKernel.run");
            int row = getGlobalId(0);
            int col = getGlobalId(1);

            if (col == 0)
                averageLuminance = 0;
            int R = (bayer[getPos(col, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
            int G = (bayer[getPos(col, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
            int B = (bayer[getPos(col, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);

            if (col == 0)
                averageLuminance = (R + G + B) / 3;

            if (!(col >= 640 || row < 240 || row > 455)) {
                if ((averageLuminance + (R + G + B) / 3) / 2 > averageLuminance * 1.5) {
                    mono[row * ncols + col] = 1;
                } else {
                    mono[row * ncols + col] = 0;
                }
            } else {
                mono[row * ncols + col] = 0;
            }
            averageLuminance = (averageLuminance + (R + G + B) / 3) / 2;
        }
    }

    public static class SimpleColorRasterKernel extends Kernel {

        byte[] bayer;
        byte[] simple;
        int nrows;
        int ncols;
        byte tile;

        public void setVars(byte[] bayer, byte[] simple, int nrows, int ncols, byte tile) {
            this.bayer = bayer; this.simple = simple;
            this.nrows = nrows; this.ncols = ncols;
            this.tile = tile;
        }

        @Override
        public void run() {
            int row = getGlobalId(0);
            int col = getGlobalId(1);

            int R = (bayer[getPos(col, row, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
            int G = (bayer[getPos(col, row, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
            int B = (bayer[getPos(col, row, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);
            //int B = (((int)bayer[(r*ncols*2 + c)*2 + 1+2*ncols-ncols*2*getBit(tile,1)-getBit(tile,0)])&0xFF);			//Bottom right (blue)
            double Y = R * .299000 + G * .587000 + B * .114000;
            double U = R * -.168736 + G * -.331264 + B * .500000 + 128;
            double V = R * .500000 + G * -.418688 + B * -.081312 + 128;
            R = (int) (1.4075 * (V - 128));
            G = (int) (0 - 0.3455 * (U - 128) - (0.7169 * (V - 128)));
            B = (int) (1.7790 * (U - 128));
            //If one of the colors has a value 50 greater than both other colors
            //it assigns that pixel to that color
            if (R > G + SimpleThresholds.redGreen && R > B + SimpleThresholds.redBlue) {
                simple[row * ncols + col] = 0;
            } else if (G > R + SimpleThresholds.greenRed && G > B + SimpleThresholds.greenBlue) {
                simple[row * ncols + col] = 1;
            } else if (B > R + SimpleThresholds.blueRed && B > G + SimpleThresholds.blueGreen) {
                simple[row * ncols + col] = 2;
            } else if (R < G + SimpleThresholds.yellowDiff && G < R + SimpleThresholds.yellowDiff && (R > B + SimpleThresholds.yellowBlue)) {
                simple[row * ncols + col] = 6;
            }
            //Otherwise it sees if one of the colors has a value above 170 for white
            // if not, 85 for grey and below 85 for black
            else if (Y > SimpleThresholds.whitePoint) {
                simple[row * ncols + col] = 3;
            } else if (Y > SimpleThresholds.greyPoint) {
                simple[row * ncols + col] = 4; //0x808080
            } else {
                simple[row * ncols + col] = 5;
            }
        }
    }

}

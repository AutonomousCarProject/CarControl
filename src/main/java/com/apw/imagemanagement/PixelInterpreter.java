package com.apw.imagemanagement;

public class PixelInterpreter {

        private static double luminanceMultiplier = 1.6;


        public static int convertToMonochrome(byte[] bayer, int nrows, int ncols, byte tile, int r, int c) {

            return ((( bayer[(r * ncols * 2 + c) * 2 + 1]) & 0xFF));            //Use only top right

        }

        public static int convertToMonochrome2(byte[] bayer, int nrows, int ncols, byte tile, int r, int c) {

                    int R = (bayer[getPos(c,r,combineTile((byte)0,tile),ncols,nrows)]&0xFF);
                    int G = (bayer[getPos(c,r,combineTile((byte)1,tile),ncols,nrows)]&0xFF);
                    int B = (bayer[getPos(c,r,combineTile((byte)3,tile),ncols,nrows)]&0xFF);
                    //double Y = R *  .299000 + G *  .587000 + B *  .114000;

                    return (R+G+B)/3;
                    //mono[r * ncols + c] = (byte) Y;

        }
        public static int convertToBlackWhite(byte[] bayer, int nrows, int ncols, byte tile, int r, int c, int lumin, int luminUp, int luminDown) {
            int a = convertToBlackWhiteCore(bayer,nrows,ncols,tile,r,c,lumin);
            int b1 = convertToBlackWhiteCore(bayer,nrows,ncols,tile,r-1,c,luminUp);
            int b2 = convertToBlackWhiteCore(bayer,nrows,ncols,tile,r-1,c+1,luminUp);
            int b3 = convertToBlackWhiteCore(bayer,nrows,ncols,tile,r,c+1,lumin);
            int b4 = convertToBlackWhiteCore(bayer,nrows,ncols,tile,r+1,c+1,luminDown);
            int b5 = convertToBlackWhiteCore(bayer,nrows,ncols,tile,r+1,c,luminDown);
            int b6 = convertToBlackWhiteCore(bayer,nrows,ncols,tile,r+1,c-1,luminDown);
            int b7 = convertToBlackWhiteCore(bayer,nrows,ncols,tile,r,c-1,lumin);
            int b8 = convertToBlackWhiteCore(bayer,nrows,ncols,tile,r-1,c-1,luminUp);

                if(a+b1+b2+b3+b4+b5+b6+b7+b8==8) {
                    return 1;
                }

            return 0;
        }
        private static int convertToBlackWhiteCore(byte[] bayer, int nrows, int ncols, byte tile, int r, int c, int lumin) {

            int pix = 0;
            pix += (bayer[getPos(c , r, combineTile((byte) 0, tile), ncols, nrows)] & 0xFF);
            pix += (bayer[getPos(c , r, combineTile((byte) 1, tile), ncols, nrows)] & 0xFF);
            pix += (bayer[getPos(c, r, combineTile((byte) 3, tile), ncols, nrows)] & 0xFF);
            if (pix > luminanceMultiplier * lumin) {
                return 1;
            }
            return 0;


        }
        public static int luminRow(int[] bayer, int nrows, int ncols, byte tile, int r, int c) {
            int averageLuminance = 0;
            for(int i = 0; c < ncols; c++) {
                int R = (bayer[getPos(i,r,combineTile((byte)0,tile),ncols,nrows)]&0xFF);
                int G = (bayer[getPos(i,r,combineTile((byte)1,tile),ncols,nrows)]&0xFF);
                int B = (bayer[getPos(i,r,combineTile((byte)3,tile),ncols,nrows)]&0xFF);
                averageLuminance += (R + G + B);
            }
            return averageLuminance / ncols;
        }

    public int getRobertsCross(byte[] bayer, int nrows, int ncols, byte tile, int r, int c, int lumin) {
        int a1 = convertToMonochrome2(bayer, nrows, ncols, tile,r,c);
        int a2 = convertToMonochrome2(bayer, nrows, ncols, tile,r+1,c+1);
        int a3 = convertToMonochrome2(bayer, nrows, ncols, tile,r,c+1);
        int a4 = convertToMonochrome2(bayer, nrows, ncols, tile,r+1,c);

        return convertToRobertsCross(a1,a2,a3,a4);
        //return output;
    }
        private static int convertToRobertsCross(int a1, int a2, int a3, int a4) {
                    return Math.abs(a1 - a2) + Math.abs(a3 - a4);

        }



        public static int getBit(byte tile, int pos){
            return (tile >> pos) & 1;
        }

        /**
         *
         * @param x
         * @param y
         * @param tile				0 = X0	1 = 0X	2 = 00	3 = 00
         * 								00		00		X0		0X
         * @param ncols
         * @param nrows
         * @return
         */
        public static int getPos(int x, int y, byte tile, int ncols, int nrows){
            //return (y*ncols*(3*boolBit(bayer)+1)+2*x+getBit(tile,1)*(boolBit(bayer)+1)*ncols+getBit(tile,0))%((3*boolBit(bayer)+1)*ncols*nrows);
            return (y*ncols*(4-getBit(tile,2))+(2+getBit(tile,2))*x+getBit(tile,1)*(2*ncols-(2*ncols-1)*getBit(tile,2))+getBit(tile,0))%((4-getBit(tile,2))*ncols*nrows);
        }
        public static byte combineTile(byte tile1, byte tile2){
            return (byte)(((int)tile1)^((int)tile2));
        }

    }



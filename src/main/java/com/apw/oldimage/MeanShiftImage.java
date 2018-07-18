package com.apw.oldimage;

/**
 * Mean-shift algorithm on hue
 * Implemented as a shim inbetween images
 */

public class MeanShiftImage {
    public static double[][] meanShift(double[][] hue, final int WINDOW_SIZE) {
        /** MEEEEAAAAN SHIIIIFT **/
        //step 2: throw out any grays b/c the hues on them will be weird
        //and also create histogram

        int[] histogram = new int[360];

        //for now we'll hue them to thier own number
        for (int i = 0; i < hue.length; i++) {
            for (int o = 0; o < hue[0].length; o++) {
                if (hue[i][o] >= 1) histogram[0]++;
                else histogram[(int) (hue[i][o] * 360)]++;
            }
        }

        int[] shiftResults = new int[360];
        //step 3: mean shift every bin, and but the results in an array
        for (int i = 0; i < shiftResults.length; i++) {
            int lastwm;
            int wm = i;
            do {
                lastwm = wm;
                double suma = 0;
                double sumb = 0;
                double total = 0;
                for (int o = lastwm - WINDOW_SIZE; o < lastwm + WINDOW_SIZE; o++) {
                    int index = o;
                    if (index < 0) index += 360;
                    else if (index >= 360) index -= 360;
                    suma += Math.cos(Math.toRadians(index)) * histogram[index];
                    sumb += Math.sin(Math.toRadians(index)) * histogram[index];
                    total += histogram[index];
                }

                wm = (int) Math.toDegrees(Math.atan2(sumb / total, suma / total));
                if (wm < 0) wm += 360;
            } while (wm != lastwm);
            shiftResults[i] = wm;
        }

        //step 4: assign them bins to them hawt pixels
        for (int i = 0; i < hue.length; i++) {
            for (int o = 0; o < hue[0].length; o++) {
                if (hue[i][o] >= 1) hue[i][o] = shiftResults[0];
                else hue[i][o] = shiftResults[(int) (hue[i][o] * 360)] / 360.0;
            }
        }

        return hue;
    }
}

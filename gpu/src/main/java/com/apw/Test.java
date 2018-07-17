package com.apw;

import com.aparapi.Kernel;
import com.aparapi.Range;

public class Test {
    public static void main(String[] args) {

        final float inA[] = { 1.0f, 2.0f, 3.0f };
        final float inB[] = { 3.0f, 2.0f, 1.0f };
        assert (inA.length == inB.length);
        final float[] result = new float[inA.length];

        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int i = getGlobalId();
                result[i] = inA[i] + inB[i];
            }
        };

        Range range = Range.create(result.length);
        kernel.execute(range);
    }
}

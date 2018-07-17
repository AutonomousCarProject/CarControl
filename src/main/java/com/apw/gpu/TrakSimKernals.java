package com.apw.gpu;

import com.aparapi.Kernel;
import com.aparapi.Range;

public class TrakSimKernals {
    public static void main(String[] args) {

        var range = Range.create(0, 0);

        var kernel = new NothingKernal();

        kernel.execute(range);

    }

    public static class NothingKernal extends Kernel {


        NothingKernal() {

        }


        @Override
        public void run() {

            // TODO anything

        }

    }
}

package com.apw.gpu;

import com.apw.gpu.KernelManager.KernelNotFoundException;
import com.aparapi.Range;

import java.util.Arrays;

public class ImageProcessing {
    public static void main(String[] args) {
        Range range = Range.create(4, 4);

        KernelManager.GetKernel(KernelType.MONOCHROME_RASTER_KERNEL).put(new int[] { 1 });

        /*try {
            KernelManager.ExecuteKernel(KernelType.MONOCHROME_RASTER_KERNEL, range);
        } catch (KernelNotFoundException e) {
            e.printStackTrace();
        }*/

    }
}

package com.apw.gpu;

import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.HashMap;

public class KernelManager {

    public static void ExecuteKernel(KernelType kernelType, Range range) throws KernelNotFoundException {

        if (!KernelList.containsKey(kernelType))
            throw new KernelNotFoundException("Kernel " + kernelType.toString() + " was not found in KernelList");

        var kernel = KernelList.get(kernelType);

        kernel.execute(range);

        kernel.dispose();
    }

    private static HashMap<KernelType, Kernel> KernelList = new HashMap<>();

    public static void AddKernel(KernelType type, Kernel kernel)
    {
        KernelList.put(type, kernel);
    }

    private static class KernelNotFoundException extends Exception {
        KernelNotFoundException(String message) {
            super(message);
        }
    }

}

package com.apw.gpu;

import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.HashMap;
import java.util.function.Function;

public class KernelManager {

    private static HashMap<KernelType, Kernel> KernelList = new HashMap() {{
       put(KernelType.MONOCHROME_RASTER_KERNEL, new MonochromeRasterKernel());
    }};

    /**
     * Executes a specified kernel over a set range.
     *
     * @param kernelType The kernel to execute.
     * @param range      The range the kernel executes over.
     * @throws KernelNotFoundException If the specified kernel does not exist in the KernelList.
     */
    public static void ExecuteKernel(KernelType kernelType, Range range) throws KernelNotFoundException {

        if (!KernelList.containsKey(kernelType))
            throw new KernelNotFoundException("Kernel " + kernelType.toString() + " was not found in KernelList");

        Kernel kernel = KernelList.get(kernelType);

        kernel.execute(range);

        kernel.dispose();
    }

    /**
     * Adds a kernel to the KernelList.
     *
     * @param type   The type of kernel being added.
     * @param kernel The kernel to add.
     */
    public static void AddKernel(KernelType type, Kernel kernel) {
        KernelList.put(type, kernel);
    }

    public static Kernel GetKernel(KernelType type)
    {
        return KernelList.get(type);
    }

    static class KernelNotFoundException extends Exception {
        KernelNotFoundException(String message) {
            super(message);
        }
    }

}

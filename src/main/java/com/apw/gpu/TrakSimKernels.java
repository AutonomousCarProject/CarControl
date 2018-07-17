package com.apw.gpu;
import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.internal.kernel.KernelArg;
import com.aparapi.opencl.OpenCL;

import java.util.HashMap;

public class TrakSimKernels {

    public static void ExecuteKernel(KernelType kernelType, Range range) throws KernelNotFoundException {

        if (!KernelList.containsKey(kernelType))
            throw new KernelNotFoundException("Kernel " + kernelType.toString() + " was not found in KernelList");

        var kernel = KernelList.get(kernelType);

        kernel.execute(range);

        kernel.dispose();
    }

    private static HashMap<KernelType, Kernel> KernelList = new HashMap<KernelType, Kernel>()
    {{
      put(KernelType.LINE_SLOPE_KERNAL, new LineSlopeKernel());
    }};

    public static class KernelNotFoundException extends Exception {

        public KernelNotFoundException(String message) {
            super(message);
        }

    }

}

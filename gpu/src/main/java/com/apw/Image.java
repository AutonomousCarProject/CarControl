package com.apw;

import com.nativelibs4java.opencl.*;
import org.bridj.Pointer;
import sun.rmi.runtime.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.nativelibs4java.opencl.library.IOpenCLLibrary.*;

public class Image {

    public static void main(String[] args) throws IOException {
        CLContext context = JavaCL.createBestContext();
        CLQueue queue = context.createDefaultQueue();

        int n = 1024;

        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        int width = image.getWidth();
        int height = image.getHeight();

        System.out.println(context.getPlatform().getVersion());

        // Create OpenCL input and output buffers
        CLImage2D imageIn = context.createImage2D(CLMem.Usage.Input, image, false);
        CLImage2D imageOut = context.createImage2D(CLMem.Usage.Output, image, false);

        ImageKernels kernels = new ImageKernels(context);
        int[] globalSizes = new int[] { n };
        CLEvent addEvt = kernels.test(queue, imageIn, imageOut, new int[] {width, height}, null);

        BufferedImage result = imageOut.read(queue, addEvt); // blocks until add_floats finished

        FileOutputStream os = new FileOutputStream("test");
        ImageIO.write(result, "png", os);
        os.close();

    }
}

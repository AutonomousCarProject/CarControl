package com.apw.gpu;
import com.aparapi.Range;

public class TrakSimKernaels {

    public static void ExecuteKernel(Range range)
    {
        var kernel = new LineSlopeKernel();

        kernel.execute(range);

        kernel.dispose();
    }

}

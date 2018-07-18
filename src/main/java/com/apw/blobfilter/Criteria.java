/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apw.blobfilter;

import com.apw.blobtrack.MovingBlob;
import java.util.List;

/**
 *
 * @author peter_000
 */
public interface Criteria 
{
    public boolean meets(MovingBlob b);
    
    public static List<MovingBlob> filter(List<MovingBlob> movingBlobs, List<Criteria> criteria, MovingBlobGraphAnalyzer mbga)
    {
        for(MovingBlob m : movingBlobs)
        {
            for(Criteria c : criteria)
            {
                if(c.meets(m))
                {
                    m.incrementScore();
                }
            }
        }
        return mbga.analyze(movingBlobs);
    }
}

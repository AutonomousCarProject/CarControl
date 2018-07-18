/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apw.blobfilter;

import com.apw.blobtrack.MovingBlob;

import java.util.List;

/**
 * @author peter_000
 */
public interface MovingBlobGraphAnalyzer {
    public List<MovingBlob> analyze(List<MovingBlob> blobsWithScores);
}

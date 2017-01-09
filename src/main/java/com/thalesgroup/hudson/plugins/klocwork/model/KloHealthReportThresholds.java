/*******************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS                             *
 * Author : Loic Quentin                                                        *
 *  		                                                                    *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *                                                                              *
 *******************************************************************************/
package com.thalesgroup.hudson.plugins.klocwork.model;

import java.io.Serializable;

public class KloHealthReportThresholds implements Serializable {

    private final String threshold;
    private final String newThreshold;
    private final String failureThreshold;
    private final String newFailureThreshold;
    private final String healthy;
    private final String unHealthy;
    private final String thresholdLimit;


    @SuppressWarnings("unused")
    public KloHealthReportThresholds(String threshold, String newThreshold, String failureThreshold, String newFailureThreshold, String healthy, String unHealthy, String thresholdLimit) {
        this.threshold = threshold;
        this.newThreshold = newThreshold;
        this.failureThreshold = failureThreshold;
        this.newFailureThreshold = newFailureThreshold;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.thresholdLimit = thresholdLimit;
    }

    @SuppressWarnings("unused")
    public String getThreshold() {
        return threshold;
    }

    @SuppressWarnings("unused")
    public String getNewThreshold() {
        return newThreshold;
    }

    @SuppressWarnings("unused")
    public String getFailureThreshold() {
        return failureThreshold;
    }

    @SuppressWarnings("unused")
    public String getNewFailureThreshold() {
        return newFailureThreshold;
    }

    @SuppressWarnings("unused")
    public String getHealthy() {
        return healthy;
    }

    @SuppressWarnings("unused")
    public String getUnHealthy() {
        return unHealthy;
    }

    @SuppressWarnings("unused")
    public String getThresholdLimit() {
        return thresholdLimit;
    }

}

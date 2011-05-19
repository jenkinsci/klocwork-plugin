/*******************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS                             *
 * Author : Aravindan Mahendran                                                 *
 *                                                                              *
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
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.klocwork.config;

import java.io.Serializable;

public class KloConfigSeverityEvaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String threshold;

    private String newThreshold;

    private String failureThreshold;

    private String newFailureThreshold;

    private String healthy;

    private String unHealthy;

    private boolean highSeverity = true;

    private boolean lowSeverity = true;

    public KloConfigSeverityEvaluation() {
    }

    public KloConfigSeverityEvaluation(String threshold,
                                       String newThreshold, String failureThreshold,
                                       String newFailureThreshold, String healthy, String unHealthy,
                                       boolean highSeverity, boolean lowSeverity) {

        this.threshold = threshold;
        this.newThreshold = newThreshold;
        this.failureThreshold = failureThreshold;
        this.newFailureThreshold = newFailureThreshold;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.highSeverity = highSeverity;
        this.lowSeverity = lowSeverity;
    }

    public String getThreshold() {
        return threshold;
    }

    public String getNewThreshold() {
        return newThreshold;
    }

    public String getFailureThreshold() {
        return failureThreshold;
    }

    public String getNewFailureThreshold() {
        return newFailureThreshold;
    }

    public String getHealthy() {
        return healthy;
    }

    public String getUnHealthy() {
        return unHealthy;
    }

    public boolean isHighSeverity() {
        return highSeverity;
    }

    public boolean isLowSeverity() {
        return lowSeverity;
    }

    public boolean isAllSeverities() {
        return (isHighSeverity() && isLowSeverity());
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public void setNewThreshold(String newThreshold) {
        this.newThreshold = newThreshold;
    }

    public void setFailureThreshold(String failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public void setNewFailureThreshold(String newFailureThreshold) {
        this.newFailureThreshold = newFailureThreshold;
    }

    public void setHealthy(String healthy) {
        this.healthy = healthy;
    }

    public void setUnHealthy(String unHealthy) {
        this.unHealthy = unHealthy;
    }

    public void setHighSeverity(boolean highSeverity) {
        this.highSeverity = highSeverity;
    }

    public void setLowSeverity(boolean lowSeverity) {
        this.lowSeverity = lowSeverity;
    }
}

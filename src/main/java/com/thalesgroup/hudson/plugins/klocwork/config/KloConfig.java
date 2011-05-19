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

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

public class KloConfig implements Serializable {

    private String klocworkReportPattern;

    private KloConfigSeverityEvaluation configSeverityEvaluation = new KloConfigSeverityEvaluation();

    private KloConfigGraph configGraph = new KloConfigGraph();

    public KloConfig() {
    }

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public KloConfig(String klocworkReportPattern, String threshold,
                     String newThreshold, String failureThreshold,
                     String newFailureThreshold, String healthy, String unHealthy,
                     boolean highSeverity, boolean lowSeverity,
                     int xSize, int ySize, boolean diplayAllError,
                     boolean displayHighSeverity, boolean displayLowSeverity) {

        this.klocworkReportPattern = klocworkReportPattern;

        this.configSeverityEvaluation = new KloConfigSeverityEvaluation(
                threshold, newThreshold, failureThreshold, newFailureThreshold, healthy,
                unHealthy, highSeverity, lowSeverity);

        this.configGraph = new KloConfigGraph(xSize, ySize, diplayAllError,
                displayHighSeverity, displayLowSeverity);
    }

    public String getKlocworkReportPattern() {
        return klocworkReportPattern;
    }

    public KloConfigSeverityEvaluation getConfigSeverityEvaluation() {
        return configSeverityEvaluation;
    }

    public KloConfigGraph getConfigGraph() {
        return configGraph;
    }

}

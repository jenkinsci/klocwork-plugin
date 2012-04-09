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

    private KloConfigTrendGraph trendGraph = new KloConfigTrendGraph();

    private KloConfigBuildGraph buildGraph = new KloConfigBuildGraph();

    private KloConfigSeverityEvaluation configSeverityEvaluation = new KloConfigSeverityEvaluation();

    private boolean linkReview = true;
    private boolean linkBuildLog = true;
    private boolean linkParseLog = true;
    private boolean publishBuildGraph = true;
    private boolean publishProjectGraph = true;

    private String numToKeep;

    public KloConfig() {

    }

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public KloConfig(String klocworkReportPattern,
                     boolean linkReview, boolean linkBuildLog, boolean linkParseLog,
                     boolean publishBuildGraph, boolean publishProjectGraph,
                     String trendNum, String interval,
                     int trendXSize, int trendYSize,
                     boolean displayAllError,
                     boolean displayHighSeverity,
                     boolean displayLowSeverity,
                     int buildXSize, int buildYSize, boolean neww,
                     boolean existing, boolean fixed,
                     String threshold,
                     String newThreshold, String failureThreshold,
                     String newFailureThreshold, String healthy, String unHealthy,
                     boolean highSeverity, boolean lowSeverity) {

        this.klocworkReportPattern = klocworkReportPattern;

        this.linkReview = linkReview;
        this.linkBuildLog = linkBuildLog;
        this.linkParseLog = linkParseLog;
        this.publishBuildGraph = publishBuildGraph;
        this.publishProjectGraph = publishProjectGraph;

        this.trendGraph = new KloConfigTrendGraph(trendXSize, trendYSize, displayAllError,
                displayHighSeverity, displayLowSeverity, interval, trendNum);

        this.buildGraph = new KloConfigBuildGraph(buildXSize, buildYSize, neww,
                existing, fixed);

        this.configSeverityEvaluation = new KloConfigSeverityEvaluation(
                threshold, newThreshold, failureThreshold, newFailureThreshold, healthy,
                unHealthy, highSeverity, lowSeverity);

    }

    public String getKlocworkReportPattern() {
        return klocworkReportPattern;
    }

    public KloConfigSeverityEvaluation getConfigSeverityEvaluation() {
        return configSeverityEvaluation;
    }

    public KloConfigTrendGraph getTrendGraph() {
        return trendGraph;
    }

    public KloConfigBuildGraph getBuildGraph() {
        return buildGraph;
    }

    public boolean getPublishBuildGraph() {
        return publishBuildGraph;
    }

    public boolean getPublishProjectGraph() {
        return publishProjectGraph;
    }

    public boolean getLinkReview() {
        return linkReview;
    }

    public boolean getLinkBuildLog() {
        return linkBuildLog;
    }

    public boolean getLinkParseLog() {
        return linkParseLog;
    }

    public String getNumToKeep() {
        return numToKeep;
    }
}

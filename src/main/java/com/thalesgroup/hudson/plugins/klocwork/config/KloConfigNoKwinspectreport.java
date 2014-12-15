/*******************************************************************************
 * NOTE: Since Feb 2012 this class is completely defunct, as the use of kwjlib
 * means results are always available, regardless of Klocwork version.
 * However, the class remains to ensure that projects created with older 
 * versions of the plugin can still be loaded (serialisation legacy purposes).
 */
/*******************************************************************************
 * Copyright (c) 2011 Emenda Software Ltd.                                      *
 * Author : Jacob Larfors                                                       *
 *		                                                                        *
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
package com.thalesgroup.hudson.plugins.klocwork.config;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

public class KloConfigNoKwinspectreport implements Serializable {

    private String klocworkReportPattern;

    private boolean kwinspectreportDeprecated;

    private boolean publishBuildGraph;
    private boolean publishProjectGraph;

    private KloConfigSeverityEvaluation configSeverityEvaluation;
    private KloConfigTrendGraph trendGraph;
    private KloConfigBuildGraph buildGraph;

    public KloConfigNoKwinspectreport() {
    }

    @DataBoundConstructor
    public KloConfigNoKwinspectreport(
            String klocworkReportPattern,
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
            boolean highSeverity, boolean lowSeverity
    ) {
        super();
        this.kwinspectreportDeprecated = false;
        this.klocworkReportPattern = klocworkReportPattern;
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

    public KloConfigNoKwinspectreport(
            boolean kwinspectreportDeprecated,
            String klocworkReportPattern,
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
            boolean highSeverity, boolean lowSeverity
    ) {
        super();
        this.kwinspectreportDeprecated = kwinspectreportDeprecated;
        this.klocworkReportPattern = klocworkReportPattern;
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

    public boolean getKwinspectreportDeprecated() {
        return kwinspectreportDeprecated;
    }

    public void setKwinspectreportDeprecated(boolean kwinspectreportDeprecated) {
        this.kwinspectreportDeprecated = kwinspectreportDeprecated;
    }

    public boolean getPublishProjectGraph() {
        return publishProjectGraph;
    }

    public boolean getPublishBuildGraph() {
        return publishBuildGraph;
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

}

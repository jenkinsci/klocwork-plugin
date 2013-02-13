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

import com.thalesgroup.hudson.plugins.klocwork.graph.KloPieChart;
import com.thalesgroup.hudson.plugins.klocwork.graph.KloTrendGraph;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

public class KloConfig implements Serializable {
    
    //The noKwinspectreport is now completely defunct, but remains
    //for serialisation legacy purposes (so that Jenkins projects
    //created with earlier versions of the plugin can still be
    //loaded into the new one.
    private KloConfigNoKwinspectreport noKwinspectreport;

    private KloConfigTrendGraph trendGraph = new KloConfigTrendGraph();

    private KloConfigBuildGraph buildGraph = new KloConfigBuildGraph();

    private KloConfigSeverityEvaluation configSeverityEvaluation = new KloConfigSeverityEvaluation();

	private String klocworkReportPattern;
    private boolean linkReview = true;
    private boolean linkBuildLog = true;
    private boolean linkParseLog = true;
    private boolean publishBuildGraph = true;
    private boolean publishProjectGraph = true;
	
    private String numToKeep;
    private String host;
    private String port;
    private String project;

    
    

    public KloConfig() {
        
    }

       @DataBoundConstructor
        @SuppressWarnings("unused")
    public KloConfig(
                    boolean linkReview, boolean linkBuildLog, boolean linkParseLog, 
                    String host, String port,String project,
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
        
        this.host=host;
        this.port=port;
        this.project=project;

        //Set noKwinspectreport to null, as it is never used
        this.noKwinspectreport = null;
    }
	
    public String getKlocworkReportPattern() {
        // return noKwinspectreport.getKlocworkReportPattern();
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

    public String getHost() {
        return host;
    }   

    public String getPort() {
        return port;
    }

    public String getProject() {
        return project;
    }

    
}

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
    public KloConfig(// boolean noKwinspectreport, // String klocworkReportPattern,
    				 KloConfigNoKwinspectreport noKwinspectreport,
					 // boolean klocworkReportPatternConfig,
                     boolean linkReview, boolean linkBuildLog, boolean linkParseLog, 
                     String host, String port,String project
					 /*,
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
                     boolean highSeverity, boolean lowSeverity */ ) {
					 
		// this.noKwinspectreport = noKwinspectreport;
                
		if (noKwinspectreport == null) {
			if (this.noKwinspectreport == null ) {
				this.noKwinspectreport = new KloConfigNoKwinspectreport(
													/* bool noKwinspectreport */true,
													/* klocworkReportPattern */ "",
													/* publishBuildGraph */ true,
													/* publishProjectGraph */ true,
													/* trendNum */ "0",
													/* interval */ "1",
													/* trendXSize */ KloTrendGraph.DEFAULT_CHART_WIDTH,
													/* trendYSize */KloTrendGraph.DEFAULT_CHART_HEIGHT,
													/* displayAllError */ true,
													/* displayHighSeverity */ true,
													/* displayLowSeverity */ true,
													/* buildXSize */ KloPieChart.DEFAULT_CHART_WIDTH,
													/* buildYSize */ KloPieChart.DEFAULT_CHART_HEIGHT,
													/* neww */ true,
													/* existing */ true,
													/* fixed */ true,
													/* threshold */ "",
													/* newThreshold */ "",
													/* failureThreshold */ "",
													/* newFailureThreshold */ "",
													/* healthy */ "",
													/* unHealthy */ "",
													/* highSeverity */ true,
													/* lowSeverity */ true
													);
                                
				// klocworkReportPattern = new KloConfigNoKwinspectreport("", true);
			} else {
				noKwinspectreport.setKwinspectreportDeprecated(true);
			}
		} else {
                     
			this.noKwinspectreport = noKwinspectreport;
                     
		}
		
        this.klocworkReportPattern = this.noKwinspectreport.getKlocworkReportPattern();
        this.linkReview = linkReview;
        this.linkBuildLog = linkBuildLog;
        this.linkParseLog = linkParseLog;
        this.publishBuildGraph = this.noKwinspectreport.getPublishBuildGraph();
        this.publishProjectGraph = this.noKwinspectreport.getPublishProjectGraph();

        this.trendGraph = this.noKwinspectreport.getTrendGraph();

        this.buildGraph = this.noKwinspectreport.getBuildGraph();

        this.configSeverityEvaluation = this.noKwinspectreport.getConfigSeverityEvaluation();
        
        this.host=host;
        this.port=port;
        this.project=project;


    }
    public KloConfigNoKwinspectreport getNoKwinspectreport() {
	return noKwinspectreport;
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

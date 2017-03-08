/*******************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS *
 * Author : Loic Quentin *
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell *
 * copies of the Software, and to permit persons to whom the Software is *
 * furnished to do so, subject to the following conditions: *
 * *
 * The above copyright notice and this permission notice shall be included in *
 * all copies or substantial portions of the Software. *
 * *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN *
 * THE SOFTWARE. *
 * *
 *******************************************************************************/
package com.thalesgroup.hudson.plugins.klocwork;

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
import com.thalesgroup.hudson.plugins.klocwork.config.KloConfigTrendGraph;
import com.thalesgroup.hudson.plugins.klocwork.graph.KloTrendGraph;
import com.thalesgroup.hudson.plugins.klocwork.model.AbstractKloBuildAction;
import com.thalesgroup.hudson.plugins.klocwork.model.KloReport;
import com.thalesgroup.hudson.plugins.klocwork.util.KloBuildHealthEvaluator;
import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.Run;
import hudson.util.ChartUtil;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;


public class KloBuildAction extends AbstractKloBuildAction {

    public static final String URL_NAME = "kloResult";
	private String iconFileName = "/plugin/klocwork/icons/klocwork-24.gif";
	private String displayName = "Klocwork Results";


    private KloResult result;
    private KloConfig kloConfig;
    private Map<String, String> matrixBuildVars;

    public KloBuildAction(AbstractBuild<?, ?> owner, KloResult result, KloConfig kloConfig, Map<String, String> matrixBuildVars) {
        super(owner);
        this.result = result;
        this.kloConfig = kloConfig;
        this.matrixBuildVars = matrixBuildVars;
        //Date: 2012-11-22 Author: Andreas Larfors
        //Change: Results now always available due to web API implementation
//		if ((kloConfig != null) && (kloConfig.getNoKwinspectreport() != null)) {
//			// if kwinspectreport has not been used, do not provide
//			// links to results
//			if (kloConfig.getNoKwinspectreport().getKwinspectreportDeprecated()) {
//				iconFileName = null;
//				displayName = null;
//			} else {
//				iconFileName = "/plugin/klocwork/icons/klocwork-24.gif";
//				displayName = "Klocwork Results";
//			}
//		}
                iconFileName = "/plugin/klocwork/icons/klocwork-24.gif";
                displayName = "Klocwork Results";

    }

    public String getIconFileName() {
        return iconFileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrlName() {
        return URL_NAME;
    }

    public KloResult getResult() {
        return this.result;
    }

    public AbstractBuild<?, ?> getBuild() {
        return owner;
    }

    public KloConfig getConfig() {
        return kloConfig;
    }

    public Object getTarget() {
        return this.result;
    }

	public boolean isSummary() {
		//AM : for compatibility with old versions
                //AL : Compatibility no longer required
//		if (kloConfig.getNoKwinspectreport() != null){
//			return !kloConfig.getNoKwinspectreport().getKwinspectreportDeprecated();
//		}
		return true;
	}

    public HealthReport getBuildHealth() {
		if (result == null) {
			return new HealthReport();
		} else {
			try {
				return new KloBuildHealthEvaluator().evaluatBuildHealth(kloConfig, result.getNumberErrorsAccordingConfiguration(kloConfig, false), matrixBuildVars);
			} catch (IOException ioe) {
				return new HealthReport();
			}
		}
    }

    private DataSetBuilder<String, NumberOnlyBuildLabel> getDataSetBuilder() {
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        int interval = Integer.parseInt(kloConfig.getTrendGraph().getInterval());
        int trendNum = Integer.parseInt(kloConfig.getTrendGraph().getTrendNum());

        int count = 0;

        for (KloBuildAction a = this; a != null; a = a.getPreviousResult()) {

            if (checkBuildNumber(interval, trendNum, count)) {
                // jenkins v1.6+ requires NumberOnlyBuildLabel to not be ambiguous
                // owner can be AbstractBuild or hudson.model.Run (super class)
                // NumberOnlyBuildLabel(AbstractBuild) is deprecated, so cast
                // to super class... verify this works!
                ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel((Run) a.owner);
				if (a.getResult() != null) {
					KloReport report = a.getResult().getReport();

					KloConfigTrendGraph configGraph = kloConfig.getTrendGraph();

					if (configGraph.isDisplayHighSeverity()) {
						//Severity higher than 3 --> Warnings and suggestions
						dsb.add(report.getNumberHighSeverities(), "Warnings and\nsuggestions", label);
					}
					if (configGraph.isDisplayLowSeverity()) {
						//Severity lower than 4 (1=Critical, 2=Severe, 3=Error)
						dsb.add(report.getNumberLowSeverities(), "Critical errors", label);
					}

					if (configGraph.isDisplayAllError()) {
						dsb.add(report.getNumberTotal(), "All errors", label);
					}
				}
            }
            count++;
        }
        return dsb;
    }

    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (ChartUtil.awtProblemCause != null) {
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }

        Calendar timestamp = getBuild().getTimestamp();

        if (req.checkIfModified(timestamp, rsp)) {
			return;
		}

        Graph g = new KloTrendGraph(getOwner(), getDataSetBuilder().build(),
                "Number of errors", kloConfig.getTrendGraph().getXSize(), kloConfig.getTrendGraph().getYSize());
        g.doPng(req, rsp);
    }

    public boolean checkBuildNumber(int interval, int trendNum, int count) {
        if ((count % interval) == 0) {
            if (((count / interval) < trendNum) || trendNum == 0) {
                return true;
            }

        }
        return false;
    }

	public String getSearchUrl() {
        return getUrlName();
    }

}

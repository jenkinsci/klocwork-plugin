/*******************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS                             *
 * Author : Loic Quentin                                                        *
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
package com.thalesgroup.hudson.plugins.klocwork;

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
import com.thalesgroup.hudson.plugins.klocwork.config.KloConfigGraph;
import com.thalesgroup.hudson.plugins.klocwork.graph.KloGraph;
import com.thalesgroup.hudson.plugins.klocwork.model.AbstractKloBuildAction;
import com.thalesgroup.hudson.plugins.klocwork.model.KloReport;
import com.thalesgroup.hudson.plugins.klocwork.util.KloBuildHealthEvaluator;
import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.util.ChartUtil;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.Calendar;


public class KloBuildAction extends AbstractKloBuildAction {

    public static final String URL_NAME = "kloResult";

    private KloResult result;
    private KloConfig kloConfig;

    public KloBuildAction(AbstractBuild<?, ?> owner, KloResult result, KloConfig kloConfig) {
        super(owner);
        this.result = result;
        this.kloConfig = kloConfig;
    }

    public String getIconFileName() {
        return "/plugin/klocwork/icons/klocwork-24.gif";
    }

    public String getDisplayName() {
        return "Klocwork Results";
    }

    public String getUrlName() {
        return URL_NAME;
    }

    public KloResult getResult() {
        return this.result;
    }

    AbstractBuild<?, ?> getBuild() {
        return owner;
    }

    public Object getTarget() {
        return this.result;
    }

    public HealthReport getBuildHealth() {

        try {
            return new KloBuildHealthEvaluator().evaluatBuildHealth(kloConfig, result.getNumberErrorsAccordingConfiguration(kloConfig, false));
        } catch (IOException ioe) {
            return new HealthReport();
        }
    }


    private DataSetBuilder<String, NumberOnlyBuildLabel> getDataSetBuilder() {
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        for (KloBuildAction a = this; a != null; a = a.getPreviousResult()) {
            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(a.owner);

            KloReport report = a.getResult().getReport();

            KloConfigGraph configGraph = kloConfig.getConfigGraph();

            if (configGraph.isDisplayHighSeverity()) {
                //Severity higher than 3 --> Warnings and suggestions
                dsb.add(report.getNumberHighSeverities(), "Warnings and suggestions", label);
            }
            if (configGraph.isDisplayLowSeverity()) {
                //Severity lower than 4 (1=Critical, 2=Severe, 3=Error)
                dsb.add(report.getNumberLowSeverities(), "Critical errors", label);
            }

            if (configGraph.isDisplayAllError()) {
                dsb.add(report.getNumberTotal(), "All errors", label);
            }

        }
        return dsb;
    }

    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (ChartUtil.awtProblemCause != null) {
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }

        Calendar timestamp = getBuild().getTimestamp();

        if (req.checkIfModified(timestamp, rsp)) return;

        Graph g = new KloGraph(getOwner(), getDataSetBuilder().build(),
                "Number of error", kloConfig.getConfigGraph().getXSize(), kloConfig.getConfigGraph().getYSize());
        g.doPng(req, rsp);

    }

    public String getSearchUrl() {
        return getUrlName();
    }

}

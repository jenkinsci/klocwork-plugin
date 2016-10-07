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
import com.thalesgroup.hudson.plugins.klocwork.model.AbstractKloProjectAction;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.HashMap;


public class KloProjectAction extends AbstractKloProjectAction {

    private static final long serialVersionUID = 1L;

    public static final String URL_NAME = "kloResult";

    public static final int CHART_WIDTH = 500;
    public static final int CHART_HEIGHT = 200;

    private KloConfig kloConfig;

    public static HashMap<String, KloProjectAction> kloProjectActionHashMap;

    //public AbstractProject<?,?> project;

    public KloProjectAction(final AbstractProject<?, ?> project, KloConfig kloConfig) {
        super(project);
        this.kloConfig = kloConfig;
        
        //Store this instance in hashmap to be accessed by dashboard portlet
        if(kloProjectActionHashMap == null) {
            kloProjectActionHashMap = new HashMap<String, KloProjectAction>();
        }
        kloProjectActionHashMap.put(project.getName(), this);
    }

    public String getIconFileName() {
        //AL : Compatibility no longer required
//		if (kloConfig.getNoKwinspectreport().getKwinspectreportDeprecated()) {
//			return null;
//		} else {
//			return "/plugin/klocwork/icons/klocwork-24.gif";
//		}
        return "/plugin/klocwork/icons/klocwork-24.gif";
    }

    public String getDisplayName() {
        //AL : Compatibility no longer required
//		if (kloConfig.getNoKwinspectreport().getKwinspectreportDeprecated()) {
//			return null;
//		} else {
//			return "Klocwork Results";
//		}
                return "Klocwork Results";
    }

    public String getUrlName() {
		return URL_NAME;
    }

    public boolean getPublishProjectGraph() {
        return kloConfig.getPublishProjectGraph();
    }

    public final boolean isDisplayGraph() {
        // Check that user has ticked to publish project graph
        if (!kloConfig.getPublishProjectGraph()) {
            return false;
        }
        //Latest
        AbstractBuild<?, ?> b = getLastFinishedBuild();
        if (b == null) {
            return false;
        }

        //Affect previous
        //b = b.getPreviousBuild();
        //if (b != null) {

            for (; b != null; b = b.getPreviousBuild()) {
                if (b.getResult().isWorseOrEqualTo(Result.FAILURE)) {
                    continue;
                }
                KloBuildAction action = b.getAction(KloBuildAction.class);
                if (action == null || action.getResult() == null) {
                    continue;
                }
                KloResult result = action.getResult();
                if (result == null)
                    continue;

                return true;
            }
        //}
        return false;
    }

    /**
     *
     * Redirects the index page to the last result.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    /*public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {
        AbstractBuild<?, ?> build = getLastFinishedBuild();
        if (build != null) {
            response.sendRedirect2(String.format("../%d/%s", build.getNumber(), KloBuildAction.URL_NAME));
        }
    }*/

    /**
     * Returns the last finished build.
     *
     * @return the last finished build or <code>null</code> if there is no
     *         such build
     */
    public AbstractBuild<?, ?> getLastFinishedBuild() {
        AbstractBuild<?, ?> lastBuild = project.getLastBuild();
        while (lastBuild != null && (lastBuild.isBuilding() || lastBuild.getAction(KloBuildAction.class) == null)) {
            lastBuild = lastBuild.getPreviousBuild();
        }
        return lastBuild;
    }

    /*public final boolean hasValidResults() {
        AbstractBuild<?, ?> build = getLastFinishedBuild();
        if (build != null) {
        	KloBuildAction resultAction = build.getAction(KloBuildAction.class);
            if (resultAction != null) {
                return resultAction.getPreviousResult() != null;
            }
        }
        return false;
    }*/

    /**
     * Display the trend map. Delegates to the the associated
     * {@link ResultAction}.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    /*public void doTrendMap(final StaplerRequest request, final StaplerResponse response) throws IOException {
        AbstractBuild<?,?> lastBuild = this.getLastFinishedBuild();
        KloBuildAction lastAction = lastBuild.getAction(KloBuildAction.class);

        ChartUtil.generateClickableMap(
                request,
                response,
                KloChartBuilder.buildChart(lastAction),
                CHART_WIDTH,
                CHART_HEIGHT);
    }*/

    /**
     * Display the trend graph. Delegates to the the associated
     * {@link ResultAction}.
     *
     * @param request  Stapler request
     * @param response Stapler response
     * @throws IOException in case of an error in
     *                     {@link ResultAction#doGraph(StaplerRequest, StaplerResponse, int)}
     */
    /*public void doTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {
        AbstractBuild<?,?> lastBuild = this.getLastFinishedBuild();
        KloBuildAction lastAction = lastBuild.getAction(KloBuildAction.class);

        ChartUtil.generateGraph(
                request,
                response,
                KloChartBuilder.buildChart(lastAction),
                CHART_WIDTH,
                CHART_HEIGHT);
        
    }*/

/*
    public void doTrendGraph(StaplerRequest req, StaplerResponse rsp) throws IOException
	{

		DataSetBuilder<String, NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, NumberOnlyBuildLabel>();

		List<Object[]> builds = getKwBuilds();
		int intervalNum = 0;
		int intTrendNum = builds.size();
		int maxBuilds = 0;
		try
        {
			intervalNum = Integer.parseInt(kloConfig.getInterval());
			if (!kloConfig.getTrendNum().equals("ALL"))
            {
				intTrendNum = Integer.parseInt(kloConfig.getTrendNum());
            }
		} catch (NumberFormatException nfe) {
			// Bugger!
			intervalNum = 1;
		}
		
        // Calculate the total number of builds to display
        // on the trend chart
		maxBuilds = intTrendNum * intervalNum;

		int i = 0;

		while (i<builds.size() && i<maxBuilds)
		{
			if ((i%intervalNum)==0)
			{
				Object[] build = builds.get(i);
			
				NumberOnlyBuildLabel label = new NumberOnlyBuildLabel((AbstractBuild) build[0]);
				

				if (kloConfig.getTrendGraph().isDisplayAllError())
					dsb.add((Long) build[4], "All Issues", label);
				if (kloConfig.getTrendGraph().isDisplayHighSeverity())
					dsb.add((Long) build[1], "Low Severity", label);
				if (kloConfig.getTrendGraph().isDisplayMedSeverity())
					dsb.add((Long) build[2], "Med Severity", label);
				if (kloConfig.getTrendGraph().isDisplayLowSeverity())
					dsb.add((Long) build[3], "High Severity", label);
			}
			i++;
		}
		
		KloTrendGraph kwgraph = new KloTrendGraph(dsb.build(),kloConfig,"No. of Issues","Build No.",500,300,
															new Color(200,0,0),new Color(0,0,200),new Color(200,200,0),new Color(0,200,0));
		kwgraph.doPng(req,rsp);
	}


    public List<Object[]> getKwBuilds()
	{
		List<Object[]> builds = new ArrayList<Object[]>();
		for (AbstractBuild build : project.getBuilds())
		{
			KloBuildGraph kwBuild = build.getAction(KloBuildGraph.class);
			KloReport report = null;
			if (kwBuild != null && kwBuild.getKloReport()!=null)
			{
				report = kwBuild.getKloReport();
				builds.add(new Object[] {kwBuild.getOwner(), new Long(report.getNumberHighSeverities()),
							new Long(report.getNumberMedSeverities()), new Long(report.getNumberLowSeverities()),
                            new Long(report.getNumberTotal())});
			}
		}
		return builds;
	}
*/
    @Override
    protected Integer getLastResultBuild() {
        for (AbstractBuild<?, ?> b = (AbstractBuild<?, ?>) project.getLastSuccessfulBuild(); b != null; b = b.getPreviousNotFailedBuild()) {
            if (b.getResult() == Result.FAILURE)
                continue;
            KloBuildAction r = b.getAction(KloBuildAction.class);
            if (r != null)
				if (r.getResult() != null)
					return b.getNumber();
        }
        return null;
    }

    public String getSearchUrl() {
        return getUrlName();
    }

}

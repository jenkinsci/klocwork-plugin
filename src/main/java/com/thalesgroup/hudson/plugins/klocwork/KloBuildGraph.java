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

package com.thalesgroup.hudson.plugins.klocwork;

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
import com.thalesgroup.hudson.plugins.klocwork.config.KloConfigBuildGraph;
import com.thalesgroup.hudson.plugins.klocwork.graph.KloPieChart;
import com.thalesgroup.hudson.plugins.klocwork.model.KloFile;
import com.thalesgroup.hudson.plugins.klocwork.model.KloReport;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.BuildListener;
import hudson.util.ChartUtil;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class KloBuildGraph implements Action {

    public static final String URL_NAME = "klograph";
	
	private AbstractBuild<?, ?> owner;
	
	private KloReport kloReport;

	private KloConfig kloConfig;

    private KloConfigBuildGraph kloConfigBuildGraph;
	
    public KloBuildGraph(AbstractBuild<?, ?> owner, KloConfig kloConfig, KloReport kloReport)
    {
        this.owner = owner;
        this.kloConfig = kloConfig;
        this.kloConfigBuildGraph = kloConfig.getBuildGraph();
		this.kloReport = kloReport;
    }

	public AbstractBuild<?, ?> getOwner()
	{
		return owner;
	}
	
    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return URL_NAME;
    }

    AbstractBuild<?, ?> getBuild()
	{
        return owner;
    }
	
	public boolean isGraphActive()
	{
		return (kloConfig.getPublishBuildGraph() && (kloReport != null));
	}
	
	public KloReport getKloReport()
	{
		return kloReport;
	}

    public void doBuildGraph(StaplerRequest req, StaplerResponse rsp) throws IOException
	{
        if (ChartUtil.awtProblemCause != null)
        {
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }
		
		if (kloReport==null)
		{
			rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
			return;
		}
		
        KloConfigBuildGraph kloConfigBuildGraph  = kloConfig.getBuildGraph();
		 
		//Graph g = new KloBarChart(getDataSetBuilder(kloConfigBuildGraph),
        //                kloConfigBuildGraph.getXSize(), kloConfigBuildGraph.getYSize());
		
		Graph g = new KloPieChart(getDataSetBuilder(kloConfigBuildGraph),
                        kloConfigBuildGraph.getXSize(), kloConfigBuildGraph.getYSize());
		
        g.doPng(req, rsp);

    }


    private DefaultPieDataset getDataSetBuilder(KloConfigBuildGraph kloConfigBuildGraph)
    {		
        DefaultPieDataset dataset = new DefaultPieDataset();
        
   		if (kloConfigBuildGraph.isNeww() && kloReport.getNeww()>0)
	        dataset.setValue("New", kloReport.getNeww());
    	if (kloConfigBuildGraph.isExisting() && kloReport.getExisting()>0)
	        dataset.setValue("Existing", kloReport.getExisting());
   		if (kloConfigBuildGraph.isFixed() && kloReport.getFixed()>0)
    	    dataset.setValue("Fixed", kloReport.getFixed());
        
        return dataset;
    }
    
    public double getNew()
    {
    	return kloReport.getNeww();
    }
    
    public double getExisting()
    {
    	return kloReport.getExisting();
    }
    
    public double getFixed()
    {
    	return kloReport.getFixed();
    }

}

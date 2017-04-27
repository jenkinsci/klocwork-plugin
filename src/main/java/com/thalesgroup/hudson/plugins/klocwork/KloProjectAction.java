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

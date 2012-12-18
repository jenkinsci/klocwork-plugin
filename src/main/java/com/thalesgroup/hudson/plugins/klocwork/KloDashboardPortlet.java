/*******************************************************************************
 * Copyright (c) 2012 Emenda Software Ltd.                                      *
 * Author : Andreas Larfors                                                     *
 *		                                                                *
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

import hudson.plugins.view.dashboard.DashboardPortlet;
import hudson.model.Descriptor;
import hudson.Extension;

import com.thalesgroup.hudson.plugins.klocwork.KloProjectAction;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Project;
import hudson.plugins.view.dashboard.Dashboard;
import hudson.util.ListBoxModel;

import org.kohsuke.stapler.DataBoundConstructor;

/*
 * Class KloDashBoardPortlet extends the Hudson/Jenkins DashboardPortlet class
 * to enable viewing of the Klocwork trend graph in the Hudson/Jenkins 
 * dashboard plug-in view.
 */
public class KloDashboardPortlet extends DashboardPortlet {

    /** Name of the project for which graph is drawn. */
    private String projectName;
    private final String jobPrefix = "job/";
    /**
    * Show standard jobs list at the top of the page
    */
   private boolean showProjectNameInTitle = false;
        
    @DataBoundConstructor
    public KloDashboardPortlet(String name, boolean showProjectNameInTitle, String projectName) {
        super(name);

        this.projectName = projectName; 
        this.showProjectNameInTitle = showProjectNameInTitle;        
    }
    
    /**
     * Fetches the KloProjectAction instance from the static
     * hash map kloProjectActionHashMap in the KloProjectAction class.
     * The KloProjectAction instance is used to determine whether the graph
     * can be shown or not, using the function isDisplayGraph().
     * @param projectNameArg
     * @return The KloProjectAction instance matching the given project name, if any
     */
    public static KloProjectAction getProjectAction(String projectNameArg) {
        return KloProjectAction.kloProjectActionHashMap.get(projectNameArg);
    }
    
    /**
     * Calls the isDisplayGraph(String) function
     * with the projectName member variable
     * @return True if the graph can be displayed, else false
     */
    public final boolean isDisplayGraph() {
        return isDisplayGraph(this.projectName);
    }
    
    /**
     * Attempts to fetch the matching KloProjectAction instance for the given
     * project name, and then calls the isDisplayGraph() function on this instance.
     * @param projectNameArg
     * @return True if the graph can be displayed, else false.
     */
    public static boolean isDisplayGraph(String projectNameArg) {
        KloProjectAction action = getProjectAction(projectNameArg);
        if(action == null) {
                return false;
        }
        return action.isDisplayGraph();
    }
    
    /**
     * Builds and returns the URL string for the Klocwork Trend graph PNG image
     * @return URL string for Klocwork trend graph PNG image
     */
    public String getUrlName() {
        String urlName = Hudson.getInstance().getRootUrlFromRequest();
        urlName = urlName + jobPrefix + this.projectName + "/" + KloProjectAction.URL_NAME;
        return urlName;
    }
    
    /**
     * @return The project name of the instance
     */
    public String getProjectName() {
        return projectName;
    }
    /**
     * Sets the project name of the instance
     * @param projectName 
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    /**
     * @return True if the option to include the project name in the portlet
     * header is ticked, else false
     */
    public boolean isShowProjectNameInTitle() {
        return showProjectNameInTitle;
    }

    /**
     * Builds the display name, including the project name if this option is set
     * @return The portlet display name
     */
    @Override
    public String getDisplayName() {
        if(showProjectNameInTitle) {
            return (super.getName() + " for " + this.getProjectName());
        }
        else {
            return super.getName();
        }
    }
    
    @Extension
    public static class KloDashboardPortletDescriptor extends Descriptor<DashboardPortlet> {
    	
        @Override
            public String getDisplayName() {
                return "Klocwork Trend Graph";
            }
        
        /**
         * Fills the project selection drop-down list box on the "edit view"
         * page.
         * @return ListBoxModel of project names for which the Klocwork trend
         * graph can be shown.
         */
            public ListBoxModel doFillProjectNameItems() {
            ListBoxModel items = new ListBoxModel();
            for (Project project : Hudson.getInstance().getProjects()) {
                //Only add the project to the list if we can display the graph
                if(KloDashboardPortlet.isDisplayGraph(project.getName())) {
                    items.add(project.getDisplayName(), project.getName());    
                }
            }
            return items;
        }
    }
}


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

package com.thalesgroup.hudson.plugins.klocwork.model;

import com.thalesgroup.hudson.plugins.klocwork.KloBuildAction;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

public abstract class AbstractKloProjectAction extends Actionable implements ProminentProjectAction
{

    protected final AbstractProject<?, ?> project;

    public AbstractKloProjectAction(AbstractProject<?, ?> project)
    {
        this.project = project;
    }

    public AbstractProject<?, ?> getProject()
    {
        return project;
    }

    public String getIconFileName()
    {
        return "/plugin/klocwork/icons/klocwork-24.gif";
    }

    public String getSearchUrl()
    {
        return getUrlName();
    }

    protected abstract AbstractBuild<?, ?> getLastFinishedBuild();

    protected abstract Integer getLastResultBuild();

    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException
    {
        AbstractBuild<?, ?> lastBuild = getLastFinishedBuild();
        KloBuildAction klocworkBuildAction = lastBuild.getAction(KloBuildAction.class);
        if (klocworkBuildAction != null)
        {
            klocworkBuildAction.doGraph(req, rsp);
        }
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException
    {
        Integer buildNumber = getLastResultBuild();
        if (buildNumber == null)
        {
            rsp.sendRedirect2("nodata");
        }
        else
        {
            rsp.sendRedirect2("../" + buildNumber + "/" + getUrlName());
        }
    }

}

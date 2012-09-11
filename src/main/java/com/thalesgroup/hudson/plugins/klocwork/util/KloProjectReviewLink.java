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
package com.thalesgroup.hudson.plugins.klocwork.util;

import com.thalesgroup.hudson.plugins.klocwork.model.KloInstallation;
import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;

/*
 * This class sets up a link on the project page to view Klocwork Review
 */
public class KloProjectReviewLink implements ProminentProjectAction {
    public static final String URL_NAME = "reviewlink";

    private final String displayName = "Klocwork Review";

    private final AbstractProject<?, ?> project;

    private String projectName;

    private KloInstallation kloInstall;

    public KloProjectReviewLink(AbstractProject<?, ?> project) {
        this.project = project;
        setKloHostPort();
    }

    private void setKloHostPort() {
        KloBuildInfo kloInfo = project.getLastSuccessfulBuild().getAction(KloBuildInfo.class);
        if (kloInfo != null) {
            kloInstall = kloInfo.getKloInstall();
            projectName = kloInfo.getProject();
        }
    }

    public boolean isDisplayLink() {
        KloBuildInfo kloInfo = project.getLastSuccessfulBuild().getAction(KloBuildInfo.class);
        if (kloInfo != null) {
            return true;
        }
        return false;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }

    public String getReviewHeight() {
        if (project.getNextBuildNumber() > 20)
            return "90%";
        else
            return "800px";
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconFileName() {
        return "/plugin/klocwork/icons/klocwork-48.gif";
    }

    public String getUrlName() {
        if (project.getLastSuccessfulBuild() != null) {
            return project.getLastSuccessfulBuild().number + "/" + URL_NAME;
        } else {
            return URL_NAME;
        }
    }

    public String getKloHost() {
        return kloInstall.getProjectHost();
    }

    public String getKloPort() {
        return kloInstall.getProjectPort();
    }

    public String getProjectName() {
        return projectName;
    }

}

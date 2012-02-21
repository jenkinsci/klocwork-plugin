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

import hudson.model.AbstractBuild;
import hudson.model.Action;

public class KloLinkReview implements Action
{

    public static final String URL_NAME = "reviewlink";
    
    private final String displayName = "Review Link";
	
	private AbstractBuild<?, ?> owner;
	
	private KloInstallation kloInstall;
	
	private String project;
	
    public KloLinkReview(AbstractBuild<?, ?> owner)
    {
    	this.owner = owner;
    	setKloHostPort();
    }
    
    public AbstractBuild<?, ?> getOwner()
    {
    	return owner;
    }
    
    private void setKloHostPort()
    {
		KloBuildInfo kloInfo = owner.getAction(KloBuildInfo.class);
		kloInstall = kloInfo.getKloInstall();
		project = kloInfo.getProject();
    }
    
    public String getKloHost()
    {
    	return kloInstall.getProjectHost();
    }
    
    public String getKloPort()
    {
    	return kloInstall.getProjectPort();
    }
    
    public String getProject()
    {
    	return project;
    }
    
    public String getReviewHeight()
    {
    	return "600px";
    }
    
    public String getIconFileName()
    {
        return "/plugin/klocwork/icons/klocwork-24.gif";
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getUrlName()
    {
        return URL_NAME;
    }

}

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
package com.thalesgroup.hudson.plugins.klocwork.model;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;


public class KloInstallation {

    private final String name;
    private final String executablePath;
    private final String projectHost;
    private final String projectPort;
    private final boolean useSSL; //New in v1.15
    private final String licenseHost;
    private final String licensePort;


    @DataBoundConstructor
    public KloInstallation(String name, String executablePath,
                           String projectHost, String projectPort, 
                           boolean useSSL, //New in v1.15
                           String licenseHost, String licensePort) {

        this.name = name;
        this.executablePath = executablePath;
        this.projectHost = projectHost;
        this.projectPort = projectPort;
        this.useSSL = useSSL; //New in v1.15
        this.licenseHost = licenseHost;
        this.licensePort = licensePort;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public String getName() {
        return name;
    }

    public String getProjectHost() {
        return projectHost;
    }

    public String getProjectPort() {
        return projectPort;
    }
    
    public boolean getUseSSL() {
        return useSSL;
    }

    public String getLicenseHost() {
        return licenseHost;
    }

    public String getLicensePort() {
        return licensePort;
    }

    public File getExecutable() {
        return new File(getExecutablePath());
    }

    public boolean getExists() {
        return getExecutable().exists();
    }

}


/*
 * *****************************************************************************
 * Copyright (c) 2020 Rogue Wave Software, Inc., a Perforce company
 * Author : Klocwork
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * *****************************************************************************
 */

package com.klocwork.kwjenkinsplugin.config;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class KlocworkBuildSpecConfig extends AbstractDescribableImpl<KlocworkBuildSpecConfig> {

    private final String buildCommand;
    private final String tool;
    private final String output;
    private final String additionalOpts;
    private final boolean ignoreErrors;

    @DataBoundConstructor
    public KlocworkBuildSpecConfig(String buildCommand, String tool, String output, String additionalOpts, boolean ignoreErrors) {
        this.buildCommand = buildCommand;
        this.tool = tool;
        this.output = output;
        this.additionalOpts = additionalOpts;
        this.ignoreErrors = ignoreErrors;
    }

    public ArgumentListBuilder getVersionCmd() {
        ArgumentListBuilder versionCmd = new ArgumentListBuilder(getTool());
        versionCmd.add("--version");
        return versionCmd;
    }

    public ArgumentListBuilder getToolCmd(EnvVars envVars) {
        ArgumentListBuilder toolCmd =
            new ArgumentListBuilder();
        if(getTool().equals("kwtrace")){
            toolCmd.add("kwinject", "-T");
            if (!StringUtils.isEmpty(getOutput())) {
                toolCmd.add("--trace-out", getOutput());
            }
        }
        else{
            toolCmd.add(getTool());
            if (!StringUtils.isEmpty(getOutput())) {
                toolCmd.add("--output", getOutput()); //Do not use -o: Not supported by kwmaven
            }
        }
        if (!StringUtils.isEmpty(getAdditionalOpts())) {
            toolCmd.addTokenized(envVars.expand(getAdditionalOpts()));
        }
        if (!StringUtils.isEmpty(getBuildCommand())) {
            toolCmd.addTokenized(envVars.expand(getBuildCommand()));
        }
        return toolCmd;
    }

    public String getTool() {
        return tool;
    }

    public String getOutput() {
        return output;
    }

    public String getAdditionalOpts() {
        return additionalOpts;
    }

    public boolean isIgnoreErrors() {
        return ignoreErrors;
    }

    public String getBuildCommand() {
        return buildCommand;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkBuildSpecConfig> {
        public String getDisplayName() { return null; }
    }

}

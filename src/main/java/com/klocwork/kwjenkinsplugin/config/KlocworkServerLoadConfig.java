
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

import com.klocwork.kwjenkinsplugin.KlocworkConstants;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import org.apache.commons.lang3.StringUtils;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ArgumentListBuilder;

public class KlocworkServerLoadConfig extends AbstractDescribableImpl<KlocworkServerLoadConfig> {

    private String tablesDir;
    private String buildName;
    private String additionalOpts;

    @DataBoundConstructor
    public KlocworkServerLoadConfig(String tablesDir, String buildName, String additionalOpts) {
        this.tablesDir = tablesDir;
        this.buildName = buildName;
        this.additionalOpts = additionalOpts;
    }

    public ArgumentListBuilder getVersionCmd() {
        ArgumentListBuilder versionCmd = new ArgumentListBuilder("kwadmin");
        versionCmd.add("--version");
        return versionCmd;
    }

    public ArgumentListBuilder getKwadminLoadCmd(EnvVars envVars, FilePath workspace) {
        ArgumentListBuilder kwadminCmd =
            new ArgumentListBuilder("kwadmin");
        kwadminCmd.add("--url", envVars.get(KlocworkConstants.KLOCWORK_URL));
        kwadminCmd.add("load");

        // add options such as --name of build
        kwadminCmd.add("--name", KlocworkUtil.getDefaultBuildName(buildName, envVars));

        kwadminCmd.add(envVars.get(KlocworkConstants.KLOCWORK_PROJECT));
        kwadminCmd.add(envVars.expand(KlocworkUtil.getDefaultKwtablesDir(tablesDir)));
        if (!StringUtils.isEmpty(additionalOpts)) {
            kwadminCmd.addTokenized(envVars.expand(additionalOpts));
        }
        return kwadminCmd;
    }

    @DataBoundSetter
    public void setTablesDir(String tablesDir) {
        this.tablesDir = tablesDir;
    }

    @DataBoundSetter
    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    @DataBoundSetter
    public void setAdditionalOpts(String additionalOpts) {
        this.additionalOpts = additionalOpts;
    }

    public String getTablesDir() { return tablesDir; }
    public String getBuildName() { return buildName; }
    public String getAdditionalOpts() { return additionalOpts; }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkServerLoadConfig> {
        public String getDisplayName() { return null; }
    }

}

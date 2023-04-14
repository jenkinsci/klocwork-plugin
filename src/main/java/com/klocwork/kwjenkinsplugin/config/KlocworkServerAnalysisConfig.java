
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
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.ArrayList;
import java.util.List;

public class KlocworkServerAnalysisConfig extends AbstractDescribableImpl<KlocworkServerAnalysisConfig> {

    private final String buildSpec;
    private final String tablesDir;
    private final boolean incrementalAnalysis;
    private final boolean ignoreCompileErrors;
    private final String importConfig;
    private final String additionalOpts;
    private final boolean disableKwdeploy;
    private boolean enabledCreateProject;
    private String duplicateFrom;
    private static final String buildProjectTool = "kwbuildproject";

    @DataBoundSetter
    public void setDuplicateFrom(String duplicateFrom) {
        this.duplicateFrom = duplicateFrom;
    }

    public String getDuplicateFrom() {
        return duplicateFrom;
    }


    @DataBoundConstructor
    public KlocworkServerAnalysisConfig(String buildSpec, String tablesDir,
            boolean incrementalAnalysis, boolean ignoreCompileErrors,
            String importConfig, String additionalOpts, boolean disableKwdeploy) {
        this.buildSpec = buildSpec;
        this.tablesDir = tablesDir;
        this.incrementalAnalysis = incrementalAnalysis;
        this.ignoreCompileErrors = ignoreCompileErrors;
        this.importConfig = importConfig;
        this.additionalOpts = additionalOpts;
        this.disableKwdeploy = disableKwdeploy;
    }

    @DataBoundSetter
    public void setEnabledCreateProject(boolean enabledCreateProject) {
        this.enabledCreateProject = enabledCreateProject;
    }

    public ArgumentListBuilder getVersionCmd() {
        ArgumentListBuilder versionCmd = new ArgumentListBuilder(getBuildProjectTool());
        versionCmd.add("--version");
        return versionCmd;
    }

    public ArgumentListBuilder getKwdeployCmd(EnvVars envVars, FilePath workspace) {
        ArgumentListBuilder kwdeployCmd =
            new ArgumentListBuilder("kwdeploy");
        kwdeployCmd.add("sync");
        kwdeployCmd.add("--url", envVars.get(KlocworkConstants.KLOCWORK_URL));
        return kwdeployCmd;
    }

    public List<ArgumentListBuilder> getKwadminImportConfigCmds(EnvVars envVars) {
        List<ArgumentListBuilder> kwadminCmds = new ArrayList<ArgumentListBuilder>();

        for (String configFile : importConfig.split(",")) {
            ArgumentListBuilder kwadminCmd =
                new ArgumentListBuilder("kwadmin");
            kwadminCmd.add("--url", envVars.get(KlocworkConstants.KLOCWORK_URL));
            kwadminCmd.add("import-config");
            kwadminCmd.add(envVars.get(KlocworkConstants.KLOCWORK_PROJECT));
            kwadminCmd.add(envVars.expand(configFile));
            kwadminCmds.add(kwadminCmd);
        }

        return kwadminCmds;
    }

    public ArgumentListBuilder getKwbuildprojectCmd(EnvVars envVars) throws AbortException {

        ArgumentListBuilder kwbuildprojectCmd =
            new ArgumentListBuilder("kwbuildproject");
        kwbuildprojectCmd.add("--tables-directory", envVars.expand(KlocworkUtil.getDefaultKwtablesDir(tablesDir)));
        kwbuildprojectCmd.add("--url");
        kwbuildprojectCmd.add(KlocworkUtil.getKlocworkProjectUrl(envVars));

        if(!StringUtils.isEmpty(envVars.get(KlocworkConstants.KLOCWORK_LICENSE_HOST))){
            kwbuildprojectCmd.add("--license-host", envVars.get(KlocworkConstants.KLOCWORK_LICENSE_HOST));

            if(!StringUtils.isEmpty(envVars.get(KlocworkConstants.KLOCWORK_LICENSE_PORT))) {
                kwbuildprojectCmd.add("--license-port", envVars.get(KlocworkConstants.KLOCWORK_LICENSE_PORT));
            }
        }

        if (incrementalAnalysis) {
            kwbuildprojectCmd.add("--incremental");
        } else {
            kwbuildprojectCmd.add("--force");
        }
        if (!StringUtils.isEmpty(additionalOpts)) {
            kwbuildprojectCmd.addTokenized(envVars.expand(additionalOpts));
        }
        // Note: this has to be final step, because the build spec always comes
        // last!
        kwbuildprojectCmd.add(envVars.expand(KlocworkUtil.getDefaultBuildSpec(buildSpec)));
        return kwbuildprojectCmd;
    }

    public boolean hasImportConfig() {
        return !StringUtils.isEmpty(importConfig);
    }

    public String getBuildSpec() { return buildSpec; }
    public String getTablesDir() { return tablesDir; }
    public boolean getIncrementalAnalysis() { return incrementalAnalysis; }
    public boolean getIgnoreCompileErrors() { return ignoreCompileErrors; }
    public String getImportConfig() { return importConfig; }
    public String getAdditionalOpts() { return additionalOpts; }
    public boolean getDisableKwdeploy() { return disableKwdeploy; }
    public boolean isEnabledCreateProject() { return enabledCreateProject; }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkServerAnalysisConfig> {
        public String getDisplayName() { return null; }
    }

    public static String getBuildProjectTool() {
        return buildProjectTool;
    }

}

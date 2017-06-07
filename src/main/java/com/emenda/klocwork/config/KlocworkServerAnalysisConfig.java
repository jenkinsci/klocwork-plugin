
package com.emenda.klocwork.config;

import com.emenda.klocwork.KlocworkConstants;
import com.emenda.klocwork.util.KlocworkUtil;

import org.kohsuke.stapler.DataBoundConstructor;

import org.apache.commons.lang3.StringUtils;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.lang.InterruptedException;
import java.util.ArrayList;
import java.util.List;

public class KlocworkServerAnalysisConfig extends AbstractDescribableImpl<KlocworkServerAnalysisConfig> {

    private final String tablesDir;
    private final boolean incrementalAnalysis;
    private final boolean ignoreCompileErrors;
    private final String importConfig;
    private final String additionalOpts;

    @DataBoundConstructor
    public KlocworkServerAnalysisConfig(String tablesDir,
            boolean incrementalAnalysis, boolean ignoreCompileErrors,
            String importConfig, String additionalOpts) {

        this.tablesDir = tablesDir;
        this.incrementalAnalysis = incrementalAnalysis;
        this.ignoreCompileErrors = ignoreCompileErrors;
        this.importConfig = importConfig;
        this.additionalOpts = additionalOpts;
    }

    public ArgumentListBuilder getVersionCmd() {
        ArgumentListBuilder versionCmd = new ArgumentListBuilder("kwbuildproject");
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
        kwbuildprojectCmd.add("--license-host");
        kwbuildprojectCmd.add(envVars.get(KlocworkConstants.KLOCWORK_LICENSE_HOST));
        kwbuildprojectCmd.add("--license-port");
        kwbuildprojectCmd.add(envVars.get(KlocworkConstants.KLOCWORK_LICENSE_PORT));
        kwbuildprojectCmd.add("--url");
        kwbuildprojectCmd.add(KlocworkUtil.getKlocworkProjectUrl(envVars));

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
        kwbuildprojectCmd.add(KlocworkUtil.getBuildSpecFile(envVars));
        return kwbuildprojectCmd;
    }

    public boolean hasImportConfig() {
        return !StringUtils.isEmpty(importConfig);
    }

    public String getTablesDir() { return tablesDir; }
    public boolean getIncrementalAnalysis() { return incrementalAnalysis; }
    public boolean getIgnoreCompileErrors() { return ignoreCompileErrors; }
    public String getImportConfig() { return importConfig; }
    public String getAdditionalOpts() { return additionalOpts; }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkServerAnalysisConfig> {
        public String getDisplayName() { return null; }
    }

}

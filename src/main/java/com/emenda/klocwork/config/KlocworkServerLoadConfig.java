
package com.emenda.klocwork.config;

import com.emenda.klocwork.KlocworkConstants;
import com.emenda.klocwork.util.KlocworkUtil;

import org.kohsuke.stapler.DataBoundConstructor;

import org.apache.commons.lang3.StringUtils;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.lang.InterruptedException;

public class KlocworkServerLoadConfig extends AbstractDescribableImpl<KlocworkServerLoadConfig> {

    private final String tablesDir;
    private final String buildName;
    private final String additionalOpts;

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

    public String getTablesDir() { return tablesDir; }
    public String getBuildName() { return buildName; }
    public String getAdditionalOpts() { return additionalOpts; }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkServerLoadConfig> {
        public String getDisplayName() { return null; }
    }

}

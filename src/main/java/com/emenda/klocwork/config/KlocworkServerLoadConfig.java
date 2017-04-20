
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

    private final String buildName;
    private final String additionalOpts;

    @DataBoundConstructor
    public KlocworkServerLoadConfig(String buildName, String additionalOpts) {

        this.buildName = buildName;
        this.additionalOpts = additionalOpts;
    }

    public ArgumentListBuilder getVersionCmd() {
        ArgumentListBuilder versionCmd = new ArgumentListBuilder("kwadmin");
        versionCmd.add("--version");
        return versionCmd;
    }

    public ArgumentListBuilder getKwadminLoadCmd(EnvVars envVars, FilePath workspace, String tablesDir) {
        ArgumentListBuilder kwadminCmd =
            new ArgumentListBuilder("kwadmin");
        kwadminCmd.add("--url", KlocworkUtil.getAndExpandEnvVar(envVars,
            KlocworkConstants.KLOCWORK_URL));
        kwadminCmd.add("load");

        // add options such as --name of build
        if (!StringUtils.isEmpty(buildName)) {
            kwadminCmd.add("--name", envVars.expand(buildName));
        }

        kwadminCmd.add(KlocworkUtil.getAndExpandEnvVar(envVars,
            KlocworkConstants.KLOCWORK_PROJECT));
        kwadminCmd.add(envVars.expand(KlocworkUtil.getKwtablesDir(tablesDir)));
		kwadminCmd.add(additionalOpts);
        return kwadminCmd;
    }

    public String getBuildName() { return buildName; }
    public String getAdditionalOpts() { return additionalOpts; }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkServerLoadConfig> {
        public String getDisplayName() { return null; }
    }

}

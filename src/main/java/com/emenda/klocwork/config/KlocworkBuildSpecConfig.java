
package com.emenda.klocwork.config;

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
                toolCmd.add("-o", getOutput());
            }
        }
        if (!StringUtils.isEmpty(getAdditionalOpts())) {
            toolCmd.addTokenized(envVars.expand(getAdditionalOpts()));
        }
        if (!StringUtils.isEmpty(getBuildCommand())) {
            toolCmd.add(getBuildCommand());
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

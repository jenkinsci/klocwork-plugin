package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkBuildSpecConfig;
import com.emenda.klocwork.util.KlocworkUtil;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

public class KlocworkBuildSpecBuilder extends Builder implements SimpleBuildStep {

    private KlocworkBuildSpecConfig buildSpecConfig;

    @DataBoundConstructor
    public KlocworkBuildSpecBuilder(KlocworkBuildSpecConfig buildSpecConfig) {
        this.buildSpecConfig = buildSpecConfig;
    }

    public KlocworkBuildSpecConfig getBuildSpecConfig() {
        return buildSpecConfig;
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
        throws AbortException {

        EnvVars envVars = null;
        try {
            envVars = build.getEnvironment(listener);
        } catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }
        perform(build, envVars, workspace, launcher, listener);
    }

    public void perform(Run<?, ?> build, EnvVars envVars, FilePath workspace, Launcher launcher, TaskListener listener)
        throws AbortException {

        KlocworkLogger logger = new KlocworkLogger("BuildSpecBuilder", listener.getLogger());
        logger.logMessage("Starting Klocwork Build Specification Generation Step");

        // validate server settings needed for build-step. AbortException is
        // thrown if URL and server project are not provided as we cannot perform
        // a server analysis without these settings
        KlocworkUtil.validateServerConfigs(envVars);

        KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
                buildSpecConfig.getVersionCmd());

        // ignore return codes with build spec generation as we need to assess them
        // based on which options user provided
        int rc_bstool = KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
                buildSpecConfig.getToolCmd(envVars), true);
        if (rc_bstool != 0) {
            if (buildSpecConfig.isIgnoreErrors()) {
                logger.logMessage("Return code "+rc_bstool+" from build spec generation ignored");
            } else {
                throw new AbortException("Non-zero return code: " + Integer.toString(rc_bstool));
            }
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }


    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_BUILD_SPEC_DISPLAY_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }
}

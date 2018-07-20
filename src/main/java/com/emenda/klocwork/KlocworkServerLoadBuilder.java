package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkServerLoadConfig;
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

public class KlocworkServerLoadBuilder extends Builder implements SimpleBuildStep {
    // TODO - artifact build.log, parse.log, kwloaddb.log if build fails
    private final KlocworkServerLoadConfig serverConfig;

    @DataBoundConstructor
    public KlocworkServerLoadBuilder(KlocworkServerLoadConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public KlocworkServerLoadConfig getServerConfig() {
        return serverConfig;
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
        KlocworkLogger logger = new KlocworkLogger("KlocworkServerLoadConfig", listener.getLogger());
        logger.logMessage("Starting Klocwork Server Analysis Load Step");

        // validate server settings needed for build-step. AbortException is
        // thrown if URL and server project are not provided as we cannot perform
        // a server analysis without these settings
        KlocworkUtil.validateServerConfigs(envVars);

        // cannot check return code of kwadmin --version as it is non-zero
        // for some reason...
        KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
                serverConfig.getVersionCmd(), true);

        KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
                serverConfig.getKwadminLoadCmd(envVars, workspace));

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
            return KlocworkConstants.KLOCWORK_SERVER_LOAD_DISPLAY_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            save();
            return super.configure(req,formData);
        }
    }
}

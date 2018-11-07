package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkXSyncConfig;
import com.emenda.klocwork.util.KlocworkUtil;

import hudson.AbortException;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.lang.InterruptedException;


public class KlocworkXSyncBuilder extends Builder implements SimpleBuildStep {

    private final KlocworkXSyncConfig syncConfig;

    @DataBoundConstructor
    public KlocworkXSyncBuilder(KlocworkXSyncConfig syncConfig) {
        this.syncConfig = syncConfig;
    }

    public KlocworkXSyncConfig getSyncConfig() { return syncConfig; }

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
        KlocworkLogger logger = new KlocworkLogger("XSyncBuilder", listener.getLogger());
        logger.logMessage("Starting Klocwork Cross Synchronisation Step");

        // validate server URL required for accessing Klocwork server
        KlocworkUtil.validateServerURL(envVars);

        KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
                syncConfig.getVersionCmd());
        KlocworkUtil.executeCommand(launcher, listener,
                 workspace, envVars, syncConfig.getxsyncCmd(envVars, launcher));

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
            return KlocworkConstants.KLOCWORK_XSYNC_DISPLAY_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            save();
            return super.configure(req,formData);
        }
    }
}

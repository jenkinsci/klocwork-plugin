package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkXSyncConfig;
import com.emenda.klocwork.util.KlocworkLtokenFetcher;
import com.emenda.klocwork.util.KlocworkUtil;

import hudson.AbortException;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Proc;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;

import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.InterruptedException;
import java.lang.NumberFormatException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class KlocworkXSyncBuilder extends Builder implements SimpleBuildStep {

    private final KlocworkXSyncConfig xsyncConfig;

    @DataBoundConstructor
    public KlocworkXSyncBuilder(KlocworkXSyncConfig xsyncConfig) {
        this.xsyncConfig = xsyncConfig;
    }

    public KlocworkXSyncConfig getXsyncConfig() { return xsyncConfig; }

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
                xsyncConfig.getVersionCmd());
        KlocworkUtil.executeCommand(launcher, listener,
                 workspace, envVars, xsyncConfig.getxsyncCmd(envVars, launcher));

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

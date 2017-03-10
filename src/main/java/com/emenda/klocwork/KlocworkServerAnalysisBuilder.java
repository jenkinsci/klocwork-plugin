package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkServerAnalysisConfig;
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
import java.io.StringReader;
import java.lang.InterruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KlocworkServerAnalysisBuilder extends Builder {

    private final KlocworkServerAnalysisConfig serverConfig;

    @DataBoundConstructor
    public KlocworkServerAnalysisBuilder(KlocworkServerAnalysisConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public KlocworkServerAnalysisConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener)
        throws AbortException {
        KlocworkLogger logger = new KlocworkLogger("ServerBuilder", listener.getLogger());
        logger.logMessage("Starting Klocwork Server Analysis Build Step");
        EnvVars envVars = null;
        FilePath workspace = null;
        try {
            envVars = build.getEnvironment(listener);
            workspace = build.getWorkspace();
        } catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }

        // validate server settings needed for build-step. AbortException is
        // thrown if URL and server project are not provided as we cannot perform
        // a server analysis without these settings
        KlocworkUtil.validateServerConfigs(envVars);

        KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
                serverConfig.getVersionCmd());

        KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
                serverConfig.getKwdeployCmd(envVars, build.getWorkspace()));

        // check if there are config files to import, then for each...
        if (serverConfig.hasImportConfig()) {
            logger.logMessage("Detected config files to import. Running "+
                "kwadmin import-config for each");
            // create kwadmin import-config command for each config file
            for (ArgumentListBuilder cmd : serverConfig.getKwadminImportConfigCmds(envVars)) {
                KlocworkUtil.executeCommand(launcher, listener,
                        build.getWorkspace(), envVars, cmd);
            }
        }

        // ignore return codes with kwbuildproject as we need to assess them
        // based on which options user provided
        int rc_kwbuild = KlocworkUtil.executeCommand(launcher, listener,
                build.getWorkspace(), envVars,
                serverConfig.getKwbuildprojectCmd(envVars), true);
        if (rc_kwbuild != 0) {
            if (serverConfig.getIgnoreCompileErrors() && rc_kwbuild == 2) {
                // this is fine, kwbuildproject returns exit code 2 if
                // the command still ran and generated a tables directory
                // but there was a compile error
                logger.logMessage("Return code 2 indicates compile errors. " +
                    "Check the build.log. Job config says to ignore return code");
            } else {
                throw new AbortException("Non-zero return code: " + Integer.toString(rc_kwbuild));
            }
        }
        return true;
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
            return "Emenda Klocwork Server Build - Step 1 - kwbuildproject";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }
}

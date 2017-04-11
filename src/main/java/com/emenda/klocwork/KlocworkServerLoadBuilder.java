package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkServerLoadConfig;
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
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Project;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KlocworkServerLoadBuilder extends Builder {
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
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener)
        throws AbortException {
        KlocworkLogger logger = new KlocworkLogger("KlocworkServerLoadConfig", listener.getLogger());
        logger.logMessage("Starting Klocwork Server Analysis Load Step");
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

        KlocworkServerAnalysisBuilder analysisBuilder = (KlocworkServerAnalysisBuilder) KlocworkUtil.getInstanceOfBuilder(KlocworkServerAnalysisBuilder.class, build);

        if (analysisBuilder == null) {
            throw new AbortException("Could not find kwbuildproject " +
            "build-step in job config. kwadmin requires a tables directory " +
            "generated by kwbuildproject");
        }

        String tablesDir = analysisBuilder.getServerConfig().getTablesDir();

        // cannot check return code of kwadmin --version as it is non-zero
        // for some reason...
        KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
                serverConfig.getVersionCmd(), true);

        KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
                serverConfig.getKwadminLoadCmd(envVars, build.getWorkspace(), tablesDir));

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
            return "Klocwork - Full Integration Analysis (step 2 - DB load)";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            save();
            return super.configure(req,formData);
        }
    }
}

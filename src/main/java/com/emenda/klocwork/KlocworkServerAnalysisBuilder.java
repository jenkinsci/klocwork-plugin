package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkServerAnalysisConfig;
import com.emenda.klocwork.util.KlocworkUtil;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.util.StringUtil;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KlocworkServerAnalysisBuilder extends Builder implements SimpleBuildStep {

    private KlocworkServerAnalysisConfig serverConfig;

    @DataBoundConstructor
    public KlocworkServerAnalysisBuilder(KlocworkServerAnalysisConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public KlocworkServerAnalysisConfig getServerConfig() {
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

        KlocworkLogger logger = new KlocworkLogger("ServerAnalysisBuilder", listener.getLogger());
        logger.logMessage("Starting Klocwork Server Analysis Build Step");

        // validate server settings needed for build-step. AbortException is
        // thrown if URL and server project are not provided as we cannot perform
        // a server analysis without these settings
        KlocworkUtil.validateServerConfigs(envVars);

        KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
                serverConfig.getVersionCmd());

        if(serverConfig.isEnabledCreateProject()){
            logger.logMessage("Checking if project: "+envVars.get(KlocworkConstants.KLOCWORK_PROJECT)+" exists");
            ByteArrayOutputStream kwadminProjectListOutput = KlocworkUtil.executeCommandParseOutput(launcher,
                    workspace, envVars, KlocworkUtil.getProjectListCmd(envVars.get(KlocworkConstants.KLOCWORK_URL), workspace));
            if(kwadminProjectListOutput != null){
                if(KlocworkUtil.projectExists(kwadminProjectListOutput, launcher, envVars.get(KlocworkConstants.KLOCWORK_PROJECT))){
                    logger.logMessage("\tproject exists, continuing");
                }
                else{
                    logger.logMessage("\tproject does not exist");
                    KlocworkUtil.executeCommandParseOutput(launcher,
                            workspace, envVars,
                            KlocworkUtil.getCreateOrDuplcateCmd(
                                    envVars.get(KlocworkConstants.KLOCWORK_URL),
                                    envVars.get(KlocworkConstants.KLOCWORK_PROJECT),
                                    serverConfig.getDuplicateFrom(),
                                    workspace));
                }
            }
        }

        if(!serverConfig.getDisableKwdeploy()) {
            KlocworkUtil.executeCommand(launcher, listener,
                    workspace, envVars,
                    serverConfig.getKwdeployCmd(envVars, workspace));
        }

        // check if there are config files to import, then for each...
        if (serverConfig.hasImportConfig()) {
            logger.logMessage("Detected config files to import. Running "+
                "kwadmin import-config for each");
            // create kwadmin import-config command for each config file
            for (ArgumentListBuilder cmd : serverConfig.getKwadminImportConfigCmds(envVars)) {
                KlocworkUtil.executeCommand(launcher, listener,
                        workspace, envVars, cmd);
            }
        }

        // ignore return codes with kwbuildproject as we need to assess them
        // based on which options user provided
        int rc_kwbuild = KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
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
            return KlocworkConstants.KLOCWORK_SERVER_ANALYSIS_DISPLAY_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }
}

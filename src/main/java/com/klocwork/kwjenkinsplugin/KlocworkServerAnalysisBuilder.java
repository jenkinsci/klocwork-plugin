/*
 * *****************************************************************************
 * Copyright (c) 2020 Rogue Wave Software, Inc., a Perforce company
 * Author : Klocwork
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * *****************************************************************************
 */

package com.klocwork.kwjenkinsplugin;

import com.klocwork.kwjenkinsplugin.config.KlocworkServerAnalysisConfig;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil.StreamReferences;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class KlocworkServerAnalysisBuilder extends Builder implements SimpleBuildStep {

    private final KlocworkServerAnalysisConfig serverConfig;

    @DataBoundConstructor
    public KlocworkServerAnalysisBuilder(final KlocworkServerAnalysisConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public KlocworkServerAnalysisConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    public void perform(final Run<?, ?> build, @Nonnull final FilePath workspace,
                        @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
        throws AbortException {

        EnvVars envVars = null;
        try {
            envVars = build.getEnvironment(listener);
        } catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }
        perform(build, envVars, workspace, launcher, listener);
    }

    public void perform(final Run<?, ?> build, final EnvVars envVars, final FilePath workspace,
                        final Launcher launcher, final TaskListener listener)
        throws AbortException {

        final KlocworkLogger logger = new KlocworkLogger("ServerAnalysisBuilder", listener.getLogger());
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
            final Map<StreamReferences, ByteArrayOutputStream> kwadminProjectListOutputs =
                    KlocworkUtil.executeCommandParseOutput(launcher, workspace, envVars, KlocworkUtil.getProjectListCmd(envVars.get(KlocworkConstants.KLOCWORK_URL), workspace));

            final ByteArrayOutputStream kwadminProjectListOutput = kwadminProjectListOutputs.get(StreamReferences.OUT_STREAM);
            if(kwadminProjectListOutput != null){
                if(KlocworkUtil.projectExists(kwadminProjectListOutput, launcher, envVars.get(KlocworkConstants.KLOCWORK_PROJECT))){
                    logger.logMessage("\tproject exists, continuing");
                }
                else{
                    logger.logMessage("\tproject does not exist");
                    KlocworkUtil.executeCommandParseOutput(launcher,
                            workspace, envVars,
                            KlocworkUtil.getCreateOrDuplicateCmd(
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
        final int rc_kwbuild = KlocworkUtil.executeCommand(launcher, listener,
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
        public boolean isApplicable(@SuppressWarnings("rawtypes") final Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_SERVER_ANALYSIS_DISPLAY_NAME;
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }
}

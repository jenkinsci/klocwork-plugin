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

import com.klocwork.kwjenkinsplugin.config.KlocworkCiConfig;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil.StreamReferences;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class KlocworkCiBuilder extends Builder implements SimpleBuildStep {

    private transient KlocworkCiConfig desktopConfig;
    private KlocworkCiConfig ciConfig;
    private final boolean analysisSkipped;

    @DataBoundConstructor
    public KlocworkCiBuilder(final KlocworkCiConfig ciConfig) {
        this.ciConfig = ciConfig;
        this.analysisSkipped = false;
    }

    public KlocworkCiConfig getCiConfig() { return ciConfig; }
    public boolean isAnalysisSkipped() { return analysisSkipped; }

    @Override
    public void perform(final Run<?, ?> build, final FilePath workspace,
                        final Launcher launcher, final TaskListener listener)
        throws AbortException {
        final EnvVars envVars;
        try {
            envVars = build.getEnvironment(listener);
        }  catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }

        // call the real perform function passing in envVars
        perform(build, envVars, workspace, launcher, listener);
    }

    public void perform(final Run<?, ?> build, final EnvVars envVars, final FilePath workspace,
                        final Launcher launcher, final TaskListener listener)
        throws AbortException {
        final KlocworkLogger logger = new KlocworkLogger("CiBuilder", listener.getLogger());
        logger.logMessage("Starting Klocwork CI Analysis");
        try {
            boolean foundTool = false;
            if (KlocworkUtil.executeCommand(launcher, listener,
                                            workspace, envVars,
                                            KlocworkCiConfig.getVersionCiAgentCmd(), true) == 0) {
                foundTool = true;
            }

            if (!foundTool) {
                if (KlocworkUtil.executeCommand(launcher, listener,
                                                workspace, envVars,
                                                KlocworkCiConfig.getVersionKwCheckCmd(), true) == 0) {

                    logger.logMessage("");
                    logger.logMessage("*******************************************************************************");
                    logger.logMessage("");
                    logger.logMessage("Note: Differential analysis no longer consumes a user license (kwcheck).");
                    logger.logMessage("Differential analysis requires a CI agent license (kwciagent).");
                    logger.logMessage("Contact your Klocwork representative to ensure you have a sufficient number of CI agent licenses.");
                    logger.logMessage("");
                    logger.logMessage("*******************************************************************************");
                    logger.logMessage("");

                    throw new CiException("Differential Analysis tool, KwCheck, is no longer supported.\n" +
                                          "See log for details.");
                }

                throw new CiException(String.format("Cannot find differential analysis tool: %s", KlocworkCiConfig.getCiTool()));
            }

            if (!ciConfig.hasExistingProject(workspace, envVars)) {
                KlocworkUtil.executeCommand(launcher, listener,
                        workspace, envVars,
                        ciConfig.getCiToolCreateCmd(envVars, workspace));
            } else {
                // update existing project
                KlocworkUtil.executeCommand(launcher, listener,
                        workspace, envVars,
                        ciConfig.getCiToolSetCmd(envVars, workspace));
            }
            String diffListFile = "";
            // should we perform incremental analysis?
            if (ciConfig.getIncrementalAnalysis()) {
                diffListFile = ciConfig.getDiffFileList(envVars);

                if (diffListFile.length() > 0) {
                    logger.logMessage("Performing incremental analysis using " +
                                      "change list specified in " + ciConfig.getDiffFileList(envVars));

                    // check which type of incremental analysis (e.g. git/manual)
                    // check if we need to execute git diff first
                    if (ciConfig.isGitDiffType()) {
                        if(ciConfig.hasPreviousCommitConfig(envVars)) {
                            logger.logMessage("Executing git diff to get change list");

                            try {
                                KlocworkUtil.executeCommand(launcher,
                                                            listener,
                                                            workspace, envVars,
                                                            ciConfig.getGitDiffCmd(envVars));
                            } catch (AbortException e) {
                                logger.logMessage("Unable to run 'git diff' command. Cause: " + e.getMessage());
                                build.setResult(Result.UNSTABLE);
                                diffListFile = "";
                            }
                        } else {
                            logger.logMessage("No previous git commit specified to generate diff file list.");
                            build.setResult(Result.UNSTABLE);

                            diffListFile = "";
                        }
                    }
                }
            }
            if(diffListFile.isEmpty()) {
                logger.logMessage("Performing full CI analysis.");
            }

            // Run kwciagent
            KlocworkUtil.executeCommand(launcher, listener,
                    workspace, envVars,
                    ciConfig.getCiToolRunCmd(envVars, workspace, diffListFile), true);

            // Output any local issues
            final Map<StreamReferences, ByteArrayOutputStream> xmlFormatIssuesStreams =
                    KlocworkUtil.executeCommandParseOutput(launcher, workspace, envVars, ciConfig.getCiToolListCmd(envVars, workspace, diffListFile, "xml"));


            if(xmlFormatIssuesStreams.get(KlocworkUtil.StreamReferences.ERR_STREAM).size() > 0) {
                throw new AbortException(xmlFormatIssuesStreams.get(KlocworkUtil.StreamReferences.ERR_STREAM).toString());
            }

            String xmlFilePath = envVars.expand(KlocworkUtil.getDefaultReportFileName(ciConfig.getReportFile()));

            writeIssuesToFile(workspace, launcher, listener, logger, xmlFilePath, xmlFormatIssuesStreams);

        }  catch (IOException | InterruptedException | CiException ex) {
            throw new AbortException(ex.getMessage());
        }

    }

    private void writeIssuesToFile(final FilePath workspace,
                                   final Launcher launcher,
                                   final TaskListener listener,
                                   final KlocworkLogger logger,
                                   final String outputFilePath,
                                   final Map<StreamReferences, ByteArrayOutputStream> toolListStreams) {

        ByteArrayOutputStream issueListOutputStream = toolListStreams.get(StreamReferences.OUT_STREAM);

        if(issueListOutputStream != null){
            final FilePath reportFile;
            final File isAbs = new File(outputFilePath);
            if(isAbs.isAbsolute()){
                reportFile = new FilePath (launcher.getChannel(), outputFilePath);
            }
            else{
                reportFile = new FilePath (workspace, outputFilePath);
            }
            KlocworkUtil.generateKwListOutput(
                    reportFile,
                    issueListOutputStream,
                    listener,
                    KlocworkCiConfig.getCiTool(),
                    launcher
            );

        }
        else{
            logger.logMessage("Unable to generate differential analysis output");
            final ByteArrayOutputStream errorStream = toolListStreams.get(StreamReferences.ERR_STREAM);
            if (errorStream != null) {
                logger.logMessage(errorStream.toString());
            }
        }
    }

    protected Object readResolve() {
        if (desktopConfig != null) {
            ciConfig = desktopConfig;
        }
        return this;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Initializer(before = InitMilestone.PLUGINS_STARTED)
        public static void addAliases() {
            Items.XSTREAM2.addCompatibilityAlias( "com.klocwork.kwjenkinsplugin.KlocworkDesktopBuilder", KlocworkCiBuilder.class);
            Run.XSTREAM2.addCompatibilityAlias("com.klocwork.kwjenkinsplugin.KlocworkDesktopBuilder", KlocworkCiBuilder.class);
        }

        public DescriptorImpl() {
            load();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_CI_BUILDER_DISPLAY_NAME;
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }
}

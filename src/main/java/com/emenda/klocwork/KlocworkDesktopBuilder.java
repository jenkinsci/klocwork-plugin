package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkDesktopConfig;
import com.emenda.klocwork.util.KlocworkUtil;

import org.apache.commons.lang3.StringUtils;

import hudson.AbortException;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;

import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.*;
import java.lang.InterruptedException;

public class KlocworkDesktopBuilder extends Builder implements SimpleBuildStep {

    private KlocworkDesktopConfig desktopConfig;
    private boolean analysisSkipped;

    @DataBoundConstructor
    public KlocworkDesktopBuilder(KlocworkDesktopConfig desktopConfig) {
        this.desktopConfig = desktopConfig;
        this.analysisSkipped = false;
    }

    public KlocworkDesktopConfig getDesktopConfig() { return desktopConfig; }
    public boolean isAnalysisSkipped() { return analysisSkipped; }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
        throws AbortException {
        EnvVars envVars = new EnvVars();
        try {
            envVars = build.getEnvironment(listener);
        }  catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }

        // call the real perform function passing in envVars
        perform(build, envVars, workspace, launcher, listener);
    }

    public void perform(Run<?, ?> build, EnvVars envVars, FilePath workspace, Launcher launcher, TaskListener listener)
        throws AbortException {
        KlocworkLogger logger = new KlocworkLogger("DesktopBuilder", listener.getLogger());
        logger.logMessage("Starting Klocwork Desktop Analysis");
        try {

            KlocworkUtil.executeCommand(launcher, listener,
                    workspace, envVars,
                    desktopConfig.getVersionCmd());

            if (!desktopConfig.hasExistingProject(workspace, envVars)) {
                KlocworkUtil.executeCommand(launcher, listener,
                        workspace, envVars,
                        desktopConfig.getKwcheckCreateCmd(envVars, workspace));
            } else {
                // update existing project
                KlocworkUtil.executeCommand(launcher, listener,
                        workspace, envVars,
                        desktopConfig.getKwcheckSetCmd(envVars, workspace));
            }
            String diffList = "";
            // should we perform incremental analysis?
            if (desktopConfig.getIncrementalAnalysis()) {
                logger.logMessage("Performing incremental analysis using " +
                "change list specified in " + desktopConfig.getDiffFileList(envVars));
                // check which type of incremental analysis (e.g. git/manual)
                // check if we need to execute git diff first
                if (desktopConfig.isGitDiffType()) {
                    logger.logMessage("Executing git diff to get change list");
                    KlocworkUtil.executeCommand(launcher, listener,
                            workspace, envVars,
                            desktopConfig.getGitDiffCmd(envVars));
                } else {
                    // manual diff just requires reading from diff file, same as git
                    // so do nothing
                }

                // check diff file list and get list of files to analyse, if none,
                // diffList will be empty
                diffList = desktopConfig.getKwcheckDiffList(envVars, workspace, launcher);
                // if there are files to analyse, run kwcheck run
                if (!StringUtils.isEmpty(diffList)) {
                    KlocworkUtil.executeCommand(launcher, listener,
                            workspace, envVars,
                            desktopConfig.getKwcheckRunCmd(envVars, workspace, diffList));
                }
                else{
                    // we do not need to do anything!
                    logger.logMessage("Incremental analysis did not detect any " +
                            "changed files in the build specification. Skipping the analysis");
                }
            }
            else{
                KlocworkUtil.executeCommand(launcher, listener,
                        workspace, envVars,
                        desktopConfig.getKwcheckRunCmd(envVars, workspace, diffList));
            }

            // Output any local issues
            ByteArrayOutputStream kwcheckListOutputStream = KlocworkUtil.executeCommandParseOutput(launcher,
                    workspace, envVars,
                    desktopConfig.getKwcheckListCmd(envVars, workspace, diffList));
            if(kwcheckListOutputStream != null){
                FilePath xmlReport;
                String path = envVars.expand(KlocworkUtil.getDefaultKwcheckReportFile(desktopConfig.getReportFile()));
                File isAbs = new File(path);
                if(isAbs.isAbsolute()){
                    xmlReport = new FilePath (launcher.getChannel(), path);
                }
                else{
                    xmlReport = new FilePath (workspace, path);
                }
                KlocworkUtil.generateKwListOutput(
                        xmlReport,
                        kwcheckListOutputStream,
                        listener
                );
            }
            else{
                logger.logMessage("Unable to generate diff analysis output");
            }
        }  catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }

        // TODO: kwdtagent is a pain!
        // if (desktopConfig.getSetupKwdtagent()) {
        //     try {
        //         // int rc_kwdtagent = KlocworkUtil.executeCommand(launcher, listener,
        //         //         build.getWorkspace(), envVars,
        //         //         desktopConfig.getKwdtagentCmd(envVars, build.getWorkspace()));
        //         ArgumentListBuilder cmds = desktopConfig.getKwdtagentCmd(envVars, build.getWorkspace());
        //         if (launcher.isUnix()) {
        //             cmds = new ArgumentListBuilder("/bin/sh", "-c", cmds.toString());
        //         } else {
        //             cmds = cmds.toWindowsCommand();
        //         }
        //         Proc proc = launcher.launch().
        //             stdout(listener).stderr(listener.getLogger()).
        //             pwd(build.getWorkspace()).envs(envVars).cmds(cmds)
        //             .start();
        //
        //         // proc.joinWithTimeout(15, TimeUnit.SECONDS, listener);
        //         // Thread.sleep(15000);
        //         // proc.kill();
        //         proc.join();
        //
        //         // if (rc_kwdtagent != 0) {
        //         //     logger.logMessage("kwdtagent return code " + Integer.toString(rc_kwdtagent));
        //         //     return false;
        //         // }
        //     } catch (IOException | InterruptedException ex) {
        //         // we expect InterruptedException to kill kwdtagent process
        //         logger.logMessage("Caught Exception from killing " +
        //         "kwdtagent session, as expected. Continuing build...");
        //     }
        // }
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
            return KlocworkConstants.KLOCWORK_DESKTOP_BUILDER_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }
}

package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkCiConfig;
import com.emenda.klocwork.util.KlocworkUtil;
import hudson.*;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractProject;
import hudson.model.Items;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class KlocworkCiBuilder extends Builder implements SimpleBuildStep {

    private transient KlocworkCiConfig desktopConfig;
    private KlocworkCiConfig ciConfig;
    private boolean analysisSkipped;

    @DataBoundConstructor
    public KlocworkCiBuilder(KlocworkCiConfig ciConfig) {
        this.ciConfig = ciConfig;
        this.analysisSkipped = false;
    }

    public KlocworkCiConfig getCiConfig() { return ciConfig; }
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
        KlocworkLogger logger = new KlocworkLogger("CiBuilder", listener.getLogger());
        logger.logMessage("Starting Klocwork Ci Analysis");
        try {

            if (KlocworkUtil.executeCommand(launcher, listener,
                    workspace, envVars,
                    ciConfig.getVersionCiAgentCmd(), true) > 0){
                KlocworkUtil.executeCommand(launcher, listener,
                        workspace, envVars,
                        ciConfig.getVersionKwCheckCmd());
                ciConfig.setCiTool("kwcheck");
                logger.logMessage("");
                logger.logMessage("*******************************************************************************");
                logger.logMessage("");
                logger.logMessage("WARNING USE OF KWCHECK FOR DIFF ANALYSIS HAS BEEN DEPRECATED AND WILL BE ");
                logger.logMessage("REMOVED IN A FUTURE VERSION, SUPPORT FOR KWCIAGENT IS NOW AVAILABLE, PLEASE ");
                logger.logMessage("CONTACT YOUR KLOCWORK REPRESENTATIVE FOR MORE DETAILS");
                logger.logMessage("");
                logger.logMessage("*******************************************************************************");
                logger.logMessage("");
            }
            else{
                ciConfig.setCiTool("kwciagent");
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
            String diffList = "";
            // should we perform incremental analysis?
            if (ciConfig.getIncrementalAnalysis()) {
                logger.logMessage("Performing incremental analysis using " +
                "change list specified in " + ciConfig.getDiffFileList(envVars));
                // check which type of incremental analysis (e.g. git/manual)
                // check if we need to execute git diff first
                if (ciConfig.isGitDiffType()) {
                    logger.logMessage("Executing git diff to get change list");
                    KlocworkUtil.executeCommand(launcher, listener,
                            workspace, envVars,
                            ciConfig.getGitDiffCmd(envVars));
                } else {
                    // manual diff just requires reading from diff file, same as git
                    // so do nothing
                }

                // check diff file list and get list of files to analyse, if none,
                // diffList will be empty
                diffList = ciConfig.getCiToolDiffList(envVars, workspace, launcher);
                // if there are files to analyse, run kwcheck run
                if (!StringUtils.isEmpty(diffList)) {
                    KlocworkUtil.executeCommand(launcher, listener,
                            workspace, envVars,
                            ciConfig.getCiToolRunCmd(envVars, workspace, diffList));
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
                        ciConfig.getCiToolRunCmd(envVars, workspace, diffList));
            }

            // Output any local issues
            ByteArrayOutputStream kwcheckListOutputStream = KlocworkUtil.executeCommandParseOutput(launcher,
                    workspace, envVars,
                    ciConfig.getCiToolListCmd(envVars, workspace, diffList));
            if(kwcheckListOutputStream != null){
                FilePath xmlReport;
                String path = envVars.expand(KlocworkUtil.getDefaultKwcheckReportFile(ciConfig.getReportFile()));
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
                        listener,
                        ciConfig.getCiTool(),
                        launcher
                );
            }
            else{
                logger.logMessage("Unable to generate diff analysis output");
            }
        }  catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
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
            Items.XSTREAM2.addCompatibilityAlias("com.emenda.klocwork.KlocworkDesktopBuilder", KlocworkCiBuilder.class);
            Run.XSTREAM2.addCompatibilityAlias("com.emenda.klocwork.KlocworkDesktopBuilder", KlocworkCiBuilder.class);
        }

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_CI_BUILDER_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }
}

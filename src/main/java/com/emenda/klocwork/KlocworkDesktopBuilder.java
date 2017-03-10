package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkDesktopConfig;
import com.emenda.klocwork.util.KlocworkUtil;

import org.apache.commons.lang3.StringUtils;

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
import java.util.concurrent.TimeUnit;


import java.util.Arrays;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link KlocworkDesktopBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public class KlocworkDesktopBuilder extends Builder {

    private final KlocworkDesktopConfig desktopConfig;

    @DataBoundConstructor
    public KlocworkDesktopBuilder(KlocworkDesktopConfig desktopConfig) {
        this.desktopConfig = desktopConfig;
    }

    public KlocworkDesktopConfig getDesktopConfig() { return desktopConfig; }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener)
        throws AbortException {
        KlocworkLogger logger = new KlocworkLogger("DesktopBuilder", listener.getLogger());
        logger.logMessage("Starting Klocwork Desktop Analysis");
        EnvVars envVars = new EnvVars();
        try {
            envVars = build.getEnvironment(launcher.getListener());

            KlocworkUtil.executeCommand(launcher, listener,
                    build.getWorkspace(), envVars,
                    desktopConfig.getVersionCmd());

            if (!desktopConfig.hasExistingProject(build.getWorkspace(), envVars)) {
                KlocworkUtil.executeCommand(launcher, listener,
                        build.getWorkspace(), envVars,
                        desktopConfig.getKwcheckCreateCmd(envVars, build.getWorkspace()));
            } else {
                // update existing project
                KlocworkUtil.executeCommand(launcher, listener,
                        build.getWorkspace(), envVars,
                        desktopConfig.getKwcheckSetCmd(envVars, build.getWorkspace()));
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
                            build.getWorkspace(), envVars,
                            desktopConfig.getGitDiffCmd(envVars));
                } else {
                    // manual diff just requires reading from diff file, same as git
                    // so do nothing
                }

                // check diff file list and get list of files to analyse, if none,
                // diffList will be empty
                diffList = desktopConfig.getKwcheckDiffList(envVars, build.getWorkspace(), launcher);
                // if there are no files to analyse, do not analyse!
                if (StringUtils.isEmpty(diffList)) {
                    // we do not need to do anything!
                    logger.logMessage("Incremental analysis did not detect any " +
                        "changed files in the build specification. Skipping the analysis");
                    return true;
                }
            }


            KlocworkUtil.executeCommand(launcher, listener,
                    build.getWorkspace(), envVars,
                    desktopConfig.getKwcheckRunCmd(envVars, build.getWorkspace(), diffList));
        }  catch (IOException | InterruptedException ex) {
            // throw new AbortException(KlocworkUtil.exceptionToString(ex));
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
            return "Emenda Klocwork Desktop Analysis (kwcheck)";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }
}

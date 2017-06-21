/**
 * *****************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS * Author : Aravindan
 * Mahendran * * Permission is hereby granted, free of charge, to any person
 * obtaining a copy * of this software and associated documentation files (the
 * "Software"), to deal* in the Software without restriction, including without
 * limitation the rights * to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell * copies of the Software, and to permit persons to
 * whom the Software is * furnished to do so, subject to the following
 * conditions: * * The above copyright notice and this permission notice shall
 * be included in * all copies or substantial portions of the Software. * * THE
 * SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM,* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN * THE SOFTWARE. *
 * *****************************************************************************
 */
package com.thalesgroup.hudson.plugins.klocwork;

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
import com.thalesgroup.hudson.plugins.klocwork.model.KloReport;
import com.thalesgroup.hudson.plugins.klocwork.model.KloSourceContainer;
import com.thalesgroup.hudson.plugins.klocwork.model.KloWorkspaceFile;
import com.thalesgroup.hudson.plugins.klocwork.parser.KloParserResult;
import com.thalesgroup.hudson.plugins.klocwork.util.*;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kohsuke.stapler.DataBoundConstructor;

//AM : KloPublisher now extends Recorder instead of Publisher
//public class KloPublisher extends Publisher implements Serializable {
public class KloPublisher extends Recorder implements Serializable {

    private static final long serialVersionUID = 1L;
    private KloConfig kloConfig;

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new KloProjectAction(project, kloConfig));
        if (kloConfig.getLinkReview()) {
            actions.add(new KloProjectReviewLink(project));
        }
        return actions;
    }

    protected boolean canContinue(final Result result) {
        return result != Result.ABORTED && result != Result.FAILURE;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        String localHost = kloConfig.getHost();
        String localPort = kloConfig.getPort();
        String localProject = kloConfig.getProject();
        String localUser = kloConfig.getUser();
        String ltokenlocation = null;
        
        if (localHost == null) {
            localHost = "";
        }
        if (localPort == null) {
            localPort = "";
        }
        if (localProject == null) {
            localProject = "";
        }
        if (localUser == null) {
            localUser = "";
        }

        EnvVars env = build.getEnvironment(listener);
        if (env != null) {
            ltokenlocation = env.get("KLOCWORK_LTOKEN");
            Iterator it = env.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                //listener.getLogger().println(pairs.getKey() + " = " + pairs.getValue());
                if (localHost.contains("%" + pairs.getKey().toString() + "%")) {
                    localHost = localHost.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (localPort.contains("%" + pairs.getKey().toString() + "%")) {
                    localPort = localPort.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (localProject.contains("%" + pairs.getKey().toString() + "%")) {
                    localProject = localProject.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (localUser.contains("%" + pairs.getKey().toString() + "%")) {
                    localUser = localUser.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }

                if (localHost.contains("${" + pairs.getKey().toString() + "}")) {
                    localHost = localHost.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (localPort.contains("${" + pairs.getKey().toString() + "}")) {
                    localPort = localPort.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (localProject.contains("${" + pairs.getKey().toString() + "}")) {
                    localProject = localProject.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (localUser.contains("${" + pairs.getKey().toString() + "}")) {
                    localUser = localUser.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }

                if (localHost.contains("$" + pairs.getKey().toString())) {
                    localHost = localHost.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                if (localPort.contains("$" + pairs.getKey().toString())) {
                    localPort = localPort.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                if (localProject.contains("$" + pairs.getKey().toString())) {
                    localProject = localProject.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                if (localUser.contains("$" + pairs.getKey().toString())) {
                    localUser = localUser.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        Map<String, String> matrixBuildVars = build.getBuildVariables();
        if (matrixBuildVars != null) {
            Iterator it = matrixBuildVars.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                //listener.getLogger().println(pairs.getKey() + " = " + pairs.getValue());
                if (localHost.contains("%" + pairs.getKey().toString() + "%")) {
                    localHost = localHost.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (localPort.contains("%" + pairs.getKey().toString() + "%")) {
                    localPort = localPort.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (localProject.contains("%" + pairs.getKey().toString() + "%")) {
                    localProject = localProject.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (localUser.contains("%" + pairs.getKey().toString() + "%")) {
                    localUser = localUser.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }

                if (localHost.contains("${" + pairs.getKey().toString() + "}")) {
                    localHost = localHost.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (localPort.contains("${" + pairs.getKey().toString() + "}")) {
                    localPort = localPort.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (localProject.contains("${" + pairs.getKey().toString() + "}")) {
                    localProject = localProject.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (localUser.contains("${" + pairs.getKey().toString() + "}")) {
                    localUser = localUser.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }

                if (localHost.contains("$" + pairs.getKey().toString())) {
                    localHost = localHost.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                if (localPort.contains("$" + pairs.getKey().toString())) {
                    localPort = localPort.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                if (localProject.contains("$" + pairs.getKey().toString())) {
                    localProject = localProject.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                if (localUser.contains("$" + pairs.getKey().toString())) {
                    localUser = localUser.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        int rKwInspectreport = 0;
        String FS;
        if (!launcher.isUnix()) {
            FS = "\\";
        } else {
            FS = "/";
        }

        if (kloConfig.getWebAPI().getUseWebAPI()) {
            String queryEncrypted = kloConfig.getWebAPI().getwebAPIQuery();
            matrixBuildVars = build.getBuildVariables();
            if (matrixBuildVars != null) {
                Iterator it = matrixBuildVars.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    if (queryEncrypted.contains("%" + pairs.getKey().toString() + "%")) {
                        queryEncrypted = queryEncrypted.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                    }
                    if (queryEncrypted.contains("${" + pairs.getKey().toString() + "}")) {
                        queryEncrypted = queryEncrypted.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                    }
                    if (queryEncrypted.contains("$" + pairs.getKey().toString())) {
                        queryEncrypted = queryEncrypted.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                    }
                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
            env = build.getEnvironment(listener);
            if (env != null) {
                Iterator it = env.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    if (queryEncrypted.contains("%" + pairs.getKey().toString() + "%")) {
                        queryEncrypted = queryEncrypted.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                    }
                    if (queryEncrypted.contains("${" + pairs.getKey().toString() + "}")) {
                        queryEncrypted = queryEncrypted.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                    }
                    if (queryEncrypted.contains("$" + pairs.getKey().toString())) {
                        queryEncrypted = queryEncrypted.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                    }
                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
            // IPXX: Use new callable class instance to run on agent
            String rtnGenerateXMLFromIssues = launcher.getChannel().call(
                    new KloXMLGenerator.callGenerateXMLFromIssues(localHost,
                            localPort,
                            kloConfig.getUseSSL(),
                            UserAxisConverter.AxeConverter(build, localProject),
                            build.getWorkspace().getRemote() + FS + "klocwork_result.xml",
                            listener,
                            queryEncrypted,
                            localUser,
                            ltokenlocation));
        }

        if (this.canContinue(build.getResult())) {
            listener.getLogger().println("Starting the klocwork analysis.");
            KloResult result = null;
            KloReport kloReport = null;
            KloSourceContainer kloSourceContainer = null;

            //AL : Results always available due to kwjlib update
            KloParserResult parser = new KloParserResult(listener, kloConfig);
            try {
                kloReport = build.getWorkspace().act(parser);
            } catch (Exception e) {
                listener.getLogger().println("Error on klocwork analysis: " + e);
                e.printStackTrace(listener.getLogger());
                build.setResult(Result.FAILURE);
                return false;
            }

            if (kloReport == null) {
                build.setResult(Result.FAILURE);
                return false;
            }

//            kloSourceContainer = new KloSourceContainer(listener, build.getWorkspace(), kloReport.getAllSeverities());

            result = new KloResult(kloReport, kloSourceContainer, build);

            Result buildResult = new KloBuildResultEvaluator().evaluateBuildResult(
                    listener, result.getNumberErrorsAccordingConfiguration(kloConfig, false),
                    result.getNumberErrorsAccordingConfiguration(kloConfig, true),
                    kloConfig, build.getBuildVariables());

            if (buildResult != Result.SUCCESS) {
                build.setResult(buildResult);
            }

            build.addAction(new KloBuildGraph(build, kloConfig, result.getReport()));

            build.addAction(new KloBuildAction(build, result, kloConfig, build.getBuildVariables()));

            // Check config whether to create links for Klocwork Review, parse_errors.log
            // and build.log
            if (kloConfig.getLinkReview()) {
                boolean ssl = kloConfig.getUseSSL();
                //AL : Results always available due to kwjlib update
                build.addAction(new KloBuildReviewLink(build, localHost, localPort, localProject, ssl));
            }

//            if (kloConfig.getLinkBuildLog()) {
//                //AL : Results always available due to kwjlib update
//                build.addAction(new KloBuildLog(build));
//            }
//
//            if (kloConfig.getLinkParseLog()) {
//                //AL : Results always available due to kwjlib update
//                build.addAction(new KloParseErrorsLog(build));
//            }
            //AL : Results always available due to kwjlib update
//            if (build.getWorkspace().isRemote()) {
//                copyFilesFromSlaveToMaster(build.getRootDir(), launcher.getChannel(), kloSourceContainer.getInternalMap().values());
//            }

            listener.getLogger().println("End of the klocwork analysis.");

            if (kloConfig.getFailNew() != null && kloConfig.getFailNew().getUseFailNew() && kloReport.getNeww() > 0) {
                if (kloConfig.getFailNew().getCritical() && kloReport.getNumCrit() > 0) {
                    listener.getLogger().println("[Error]: Klocwork build contains New issues of severity 'Critical', failed build");
                    listener.getLogger().println("\tNew Critical issues: " + kloReport.getNumCrit());
                    listener.getLogger().println("\tNew Error issues: " + kloReport.getNumErr());
                    listener.getLogger().println("\tNew Warning issues: " + kloReport.getNumWarn());
                    listener.getLogger().println("\tNew Review issues: " + kloReport.getNumRev());
                    build.setResult(Result.FAILURE);
                    return false;
                } else if (kloConfig.getFailNew().getError() && kloReport.getNumErr() > 0) {
                    listener.getLogger().println("[Error]: Klocwork build contains New issues of severity 'Error', failed build");
                    listener.getLogger().println("\tNew Critical issues: " + kloReport.getNumCrit());
                    listener.getLogger().println("\tNew Error issues: " + kloReport.getNumErr());
                    listener.getLogger().println("\tNew Warning issues: " + kloReport.getNumWarn());
                    listener.getLogger().println("\tNew Review issues: " + kloReport.getNumRev());
                    build.setResult(Result.FAILURE);
                    return false;
                } else if (kloConfig.getFailNew().getWarning() && kloReport.getNumWarn() > 0) {
                    listener.getLogger().println("[Error]: Klocwork build contains New issues of severity 'Warning', failed build");
                    listener.getLogger().println("\tNew Critical issues: " + kloReport.getNumCrit());
                    listener.getLogger().println("\tNew Error issues: " + kloReport.getNumErr());
                    listener.getLogger().println("\tNew Warning issues: " + kloReport.getNumWarn());
                    listener.getLogger().println("\tNew Review issues: " + kloReport.getNumRev());
                    build.setResult(Result.FAILURE);
                    return false;
                } else if (kloConfig.getFailNew().getReview() && kloReport.getNumRev() > 0) {
                    listener.getLogger().println("[Error]: Klocwork build contains New issues of severity 'Review', failed build");
                    listener.getLogger().println("\tNew Critical issues: " + kloReport.getNumCrit());
                    listener.getLogger().println("\tNew Error issues: " + kloReport.getNumErr());
                    listener.getLogger().println("\tNew Warning issues: " + kloReport.getNumWarn());
                    listener.getLogger().println("\tNew Review issues: " + kloReport.getNumRev());
                    build.setResult(Result.FAILURE);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Copies all the source files from slave to master for a remote build.
     *
     * @param rootDir directory to store the copied files in
     * @param channel channel to get the files from
     * @param sourcesFiles the sources files to be copied
     * @throws IOException if the files could not be written
     * @throws FileNotFoundException if the files could not be written
     * @throws InterruptedException if the user cancels the processing
     */
    private void copyFilesFromSlaveToMaster(final File rootDir,
            final VirtualChannel channel, final Collection<KloWorkspaceFile> sourcesFiles)
            throws IOException, InterruptedException {

        File directory = new File(rootDir, KloWorkspaceFile.WORKSPACE_FILES);
        if (!directory.exists()) {

            if (!directory.delete()) {
                //do nothing
            }

            if (!directory.mkdir()) {
                throw new IOException("Can't create directory for remote source files: " + directory.getAbsolutePath());
            }
        }

        for (KloWorkspaceFile file : sourcesFiles) {
            if (!file.isSourceIgnored()) {
                File masterFile = new File(directory, file.getTempName());
                if (!masterFile.exists()) {
                    FileOutputStream outputStream = new FileOutputStream(masterFile);
                    new FilePath(channel, file.getFileName()).copyTo(outputStream);
                }
            }
        }
    }

    @Override
    public KloPublisher.KloDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @DataBoundConstructor
    public KloPublisher(KloConfig config) {
        this.kloConfig = config;
    }

    @Extension
    public static final KloPublisher.KloDescriptor DESCRIPTOR = new KloPublisher.KloDescriptor();

    public static final class KloDescriptor extends BuildStepDescriptor<Publisher> {

        public KloDescriptor() {
            super(KloPublisher.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "Klocwork - Publish Klocwork test result report";
        }

        @Override
        public final String getHelpFile() {
            return getPluginRoot() + "help.html";
        }

        /**
         * Returns the root folder of this plug-in.
         *
         * @return the name of the root folder of this plug-in
         */
        public String getPluginRoot() {
            return "/plugin/klocwork/";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public KloConfig getConfig() {
            return new KloConfig();
        }
    }

    public KloConfig getKloConfig() {
        return kloConfig;
    }

    public void setKloConfig(KloConfig kloConfig) {
        this.kloConfig = kloConfig;
    }

}

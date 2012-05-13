/*******************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS                             *
 * Author : Aravindan Mahendran                                                 *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/


package com.thalesgroup.hudson.plugins.klocwork;

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
import com.thalesgroup.hudson.plugins.klocwork.model.KloReport;
import com.thalesgroup.hudson.plugins.klocwork.model.KloSourceContainer;
import com.thalesgroup.hudson.plugins.klocwork.model.KloWorkspaceFile;
import com.thalesgroup.hudson.plugins.klocwork.parser.KloParserResult;
import com.thalesgroup.hudson.plugins.klocwork.util.*;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixProject;
import hudson.model.*;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kohsuke.stapler.DataBoundConstructor;


//AM : KloPublisher now extends Recorder instead of Publisher
//public class KloPublisher extends Publisher implements Serializable {
public class KloPublisher extends Recorder implements Serializable {

    private static final long serialVersionUID = 1L;


    private KloConfig kloConfig;


    /*
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project)
    {
        return new KloProjectAction(project, kloConfig);
    }*/

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
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {


        if (this.canContinue(build.getResult())) {
            listener.getLogger().println("Starting the klocwork analysis.");

            //            final FilePath[] moduleRoots = build.getModuleRoots();
            //            final boolean multipleModuleRoots = moduleRoots != null && moduleRoots.length > 1;
            //            final FilePath moduleRoot = multipleModuleRoots ? build.getWorkspace() : build.getModuleRoot();

            KloParserResult parser = new KloParserResult(listener, kloConfig.getKlocworkReportPattern());
            KloReport kloReport;
            try {
                kloReport = build.getWorkspace().act(parser);
            } catch (Exception e) {
                listener.getLogger().println("Error on klocwork analysis: " + e);
                build.setResult(Result.FAILURE);
                return false;
            }

            if (kloReport == null) {
                build.setResult(Result.FAILURE);
                return false;
            }

            KloSourceContainer kloSourceContainer = new KloSourceContainer(listener, build.getWorkspace(), kloReport.getAllSeverities());

            KloResult result = new KloResult(kloReport, kloSourceContainer, build);

            Result buildResult = new KloBuildResultEvaluator().evaluateBuildResult(
                    listener, result.getNumberErrorsAccordingConfiguration(kloConfig, false),
                    result.getNumberErrorsAccordingConfiguration(kloConfig, true),
                    kloConfig);

            if (buildResult != Result.SUCCESS) {
                build.setResult(buildResult);
            }

            build.addAction(new KloBuildAction(build, result, kloConfig));
            build.addAction(new KloBuildGraph(build, kloConfig, result.getReport()));

            // Check config whether to create links for Klocwork Review, parse_errors.log
            // and build.log
            if (kloConfig.getLinkReview()) {
                String host = null, port = null, project = null;
                if (kloReport.getAllSeverities().get(0) != null) {
                    String url = kloReport.getAllSeverities().get(0).get("url");
                    if (url !=null){
	                    Pattern p = Pattern.compile("^http://(.*?):(\\d*?)/.*?=(.*?),.*$");
	                    Matcher m = p.matcher(url);
	                    if (m.matches()) {
	                        host = m.group(1);
	                        port = m.group(2);
	                        project = m.group(3);
	                    }
                    }
                }
                build.addAction(new KloBuildReviewLink(build, host, port, project));
            }
            if (kloConfig.getLinkBuildLog()) {
                build.addAction(new KloBuildLog(build));
            }
            if (kloConfig.getLinkParseLog()) {
                build.addAction(new KloParseErrorsLog(build));
            }

            if (build.getWorkspace().isRemote()) {
                copyFilesFromSlaveToMaster(build.getRootDir(), launcher.getChannel(), kloSourceContainer.getInternalMap().values());
            }

            listener.getLogger().println("End of the klocwork analysis.");
        }
        return true;
    }

    /**
     * Copies all the source files from slave to master for a remote build.
     *
     * @param rootDir      directory to store the copied files in
     * @param channel      channel to get the files from
     * @param sourcesFiles the sources files to be copied
     * @throws IOException           if the files could not be written
     * @throws FileNotFoundException if the files could not be written
     * @throws InterruptedException  if the user cancels the processing
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
    public KloDescriptor getDescriptor() {
        return DESCRIPTOR;
    }
    
    @DataBoundConstructor
    public KloPublisher(KloConfig config) {
        this.kloConfig = config;
    }

    @Extension
    public static final KloDescriptor DESCRIPTOR = new KloDescriptor();

    public static final class KloDescriptor extends BuildStepDescriptor<Publisher> {

        public KloDescriptor() {
            super(KloPublisher.class);
            load();
        }


        @Override
        public String getDisplayName() {
            return "Publish Klocwork test result report";
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
            return FreeStyleProject.class.isAssignableFrom(jobType) || MatrixProject.class.isAssignableFrom(jobType);
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

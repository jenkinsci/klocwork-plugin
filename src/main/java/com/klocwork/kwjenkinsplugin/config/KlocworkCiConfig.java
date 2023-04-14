
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

package com.klocwork.kwjenkinsplugin.config;

import com.google.common.base.Strings;
import com.klocwork.kwjenkinsplugin.KlocworkConstants;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil;
import hudson.*;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Items;
import hudson.model.Run;
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

public class KlocworkCiConfig extends AbstractDescribableImpl<KlocworkCiConfig> {

    private final String buildSpec;
    private final String projectDir;
    private final boolean cleanupProject;
    private final String reportFile;
    private final String additionalOpts;
    private final boolean incrementalAnalysis;
    private final KlocworkDifferentialAnalysisConfig differentialAnalysisConfig;
    private static final String ciTool = "kwciagent";
    @Deprecated
    private static final String oldTool = "kwcheck";

    private static final String GIT_PREVIOUS_COMMIT = "GIT_PREVIOUS_COMMIT";

    private static final Logger debugLogger = Logger.getLogger(KlocworkCiConfig.class.getName());

    @DataBoundConstructor
    public KlocworkCiConfig(final String buildSpec, final String projectDir, final boolean cleanupProject,
                            final String reportFile, final String additionalOpts, final boolean incrementalAnalysis,
                            final KlocworkDifferentialAnalysisConfig differentialAnalysisConfig) {
        this.buildSpec = buildSpec;
        this.projectDir = projectDir;
        this.cleanupProject = cleanupProject;
        this.reportFile = reportFile;
        this.additionalOpts = additionalOpts;
        this.incrementalAnalysis = incrementalAnalysis;
        this.differentialAnalysisConfig = differentialAnalysisConfig;
    }

    public static ArgumentListBuilder getVersionCiAgentCmd()
            throws IOException, InterruptedException {
        final ArgumentListBuilder command = new ArgumentListBuilder(ciTool);
        command.add("--version");
        return command;
    }

    public static ArgumentListBuilder getVersionKwCheckCmd()
            throws IOException, InterruptedException {
        final ArgumentListBuilder command = new ArgumentListBuilder(oldTool);
        command.add("--version");
        return command;
    }

    public ArgumentListBuilder getCiToolCreateCmd(final EnvVars envVars, final FilePath workspace)
            throws IOException, InterruptedException {

        validateParentProjectDir(getKwlpDir(workspace, envVars).getParent());

        final ArgumentListBuilder command = new ArgumentListBuilder(ciTool, "create");
        final String projectUrl = KlocworkUtil.getKlocworkProjectUrl(envVars);
        if (!StringUtils.isEmpty(projectUrl)) {
            command.add("--url", projectUrl);
        }
        command.add("--project-dir", getKwlpDir(workspace, envVars).getRemote());
        command.add("--settings-dir", getKwpsDir(workspace, envVars).getRemote());
        command.add("--build-spec", envVars.expand(KlocworkUtil.getDefaultBuildSpec(buildSpec)));
        return command;
    }

    public ArgumentListBuilder getCiToolSetCmd(final EnvVars envVars, final FilePath workspace)
            throws IOException, InterruptedException {

        validateParentProjectDir(getKwlpDir(workspace, envVars).getParent());

        final ArgumentListBuilder command = new ArgumentListBuilder(ciTool, "set");
        command.add("--project-dir", getKwlpDir(workspace, envVars).getRemote());
        final String serverUrl = envVars.get(KlocworkConstants.KLOCWORK_URL);
        if (!StringUtils.isEmpty(serverUrl)) {
            URL url = new URL(serverUrl);
            command.add("klocwork.host=" + url.getHost());
            command.add("klocwork.port=" + Integer.toString(url.getPort()));
            command.add("klocwork.project=" + envVars.get(KlocworkConstants.KLOCWORK_PROJECT));
        }
        return command;
    }

    public ArgumentListBuilder getCiToolListCmd(final EnvVars envVars, final FilePath workspace, final String diffList, final String outputFormat)
            throws IOException, InterruptedException {
        final ArgumentListBuilder command =
                new ArgumentListBuilder(ciTool, "list");
        command.add("--project-dir", getKwlpDir(workspace, envVars).getRemote());
        final String licenseHost = envVars.get(KlocworkConstants.KLOCWORK_LICENSE_HOST);
        if (!StringUtils.isEmpty(licenseHost)) {
            command.add("--license-host", licenseHost);
        }

        final String licensePort = envVars.get(KlocworkConstants.KLOCWORK_LICENSE_PORT);
        if (!StringUtils.isEmpty(licensePort)) {
            command.add("--license-port", licensePort);
        }

        command.add("-F", outputFormat);

        if (!StringUtils.isEmpty(additionalOpts)) {
            command.addTokenized(envVars.expand(additionalOpts));
        }

        // add list of changed files to end of tool's run command
        if (!StringUtils.isEmpty(diffList)) {
            command.add("@" + diffList);
        }

        return command;
    }

    public ArgumentListBuilder getCiToolRunCmd(final EnvVars envVars, final FilePath workspace, final String diffList)
            throws IOException, InterruptedException {
        final ArgumentListBuilder command =
                new ArgumentListBuilder(ciTool, "run");
        command.add("--project-dir", getKwlpDir(workspace, envVars).getRemote());

        if (!StringUtils.isEmpty(envVars.get(KlocworkConstants.KLOCWORK_LICENSE_HOST))) {
            command.add("--license-host", envVars.get(KlocworkConstants.KLOCWORK_LICENSE_HOST));
            if (!StringUtils.isEmpty(envVars.get(KlocworkConstants.KLOCWORK_LICENSE_PORT))) {
                command.add("--license-port", envVars.get(KlocworkConstants.KLOCWORK_LICENSE_PORT));
            }
        }

        command.add("-Y", "-L"); // Report nothing

        command.add("--build-spec", envVars.expand(KlocworkUtil.getDefaultBuildSpec(buildSpec)));
        if (!StringUtils.isEmpty(additionalOpts)) {
            command.addTokenized(envVars.expand(additionalOpts));
        }

        // add list of changed files to end of tool's run command
        if (!StringUtils.isEmpty(diffList)) {
            command.add("@" + diffList);
        }

        return command;
    }

    public ArgumentListBuilder getGitDiffCmd(final EnvVars envVars) {
        final ArgumentListBuilder command = new ArgumentListBuilder("git");
        command.add("diff", "--name-only", envVars.expand(differentialAnalysisConfig.getGitPreviousCommit()));
        command.add(">", getDiffFileList(envVars));
        return command;
    }

    public boolean hasPreviousCommitConfig(final EnvVars envVars) {
        String commit = envVars.expand(differentialAnalysisConfig.getGitPreviousCommit());

        if(commit.isEmpty()) {
            debugLogger.warning(String.format("Git previous commit is empty", commit));
            return false;
        }

        //that's right. we are checking if commit varibale have no been parsed by jenkins properly
        // and instead of an actual value contains the variable name
        if(commit.contains(GIT_PREVIOUS_COMMIT)) {
            debugLogger.warning(String.format("Git commit variable [%s] is not set.", commit));
            return false;
        }

        return true;
    }

    /*
    function to check if a local project already exists.
    If the creation of a project went wrong before, there may be some left over .kwlp or .kwps directories
    so we need to make sure to clean these up.
    If both .kwlp and .kwps exist then we reuse them
     */
    public boolean hasExistingProject(final FilePath workspace, final EnvVars envVars)
            throws IOException, InterruptedException {
        final FilePath kwlp = getKwlpDir(workspace, envVars);
        final FilePath kwps = getKwpsDir(workspace, envVars);

        if (cleanupProject) {
            // cleanup is forced
            cleanupExistingProject(kwlp, kwps);
        }
        // else check if a cleanup is needed because the tool's create command
        // failed and left some things lying around that will make it fail
        // next time...
        if (kwlp.exists()) {
            if (kwps.exists()) {
                // both directories exist
                return true;
            } else {
                // clean up directories because something has gone wrong
                cleanupExistingProject(kwlp, kwps);
                return false;
            }
        } else if (kwps.exists()) {
            // clean up directories because something has gone wrong
            cleanupExistingProject(kwlp, kwps);
            return false;
        } else {
            // no existing project
            return false;
        }
    }

    private static void validateParentProjectDir(final FilePath dir) throws IOException, InterruptedException {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private FilePath getKwlpDir(final FilePath workspace, final EnvVars envVars) {
        return new FilePath(
                workspace.child(envVars.expand(projectDir)), ".kwlp");
    }

    private FilePath getKwpsDir(final FilePath workspace, final EnvVars envVars) {
        return new FilePath(
                workspace.child(envVars.expand(projectDir)), ".kwps");
    }

    private static void cleanupExistingProject(final FilePath kwlp, final FilePath kwps)
            throws IOException, InterruptedException {
        if (kwlp.exists()) {
            kwlp.deleteRecursive();
        }
        if (kwps.exists()) {
            kwps.deleteRecursive();
        }
    }

    public String getDiffFileList(final EnvVars envVars) {
        return envVars.expand(differentialAnalysisConfig.getDiffFileList());
    }

    public boolean isGitDiffType() {
        return differentialAnalysisConfig.isGitDiffType();
    }

    public String getBuildSpec() {
        return buildSpec;
    }

    public String getProjectDir() {
        return projectDir;
    }

    public boolean getCleanupProject() {
        return cleanupProject;
    }

    public String getReportFile() {
        return reportFile;
    }

    public String getAdditionalOpts() {
        return additionalOpts;
    }

    public boolean getIncrementalAnalysis() {
        return incrementalAnalysis;
    }

    public KlocworkDifferentialAnalysisConfig getDifferentialAnalysisConfig() {
        return differentialAnalysisConfig;
    }

    public static String getCiTool() {
        return ciTool;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkCiConfig> {
        @Initializer(before = InitMilestone.PLUGINS_STARTED)
        public static void addAliases() {
            Items.XSTREAM2.addCompatibilityAlias("com.klocwork.kwjenkinsplugin.config.KlocworkDesktopConfig", KlocworkCiConfig.class);
            Run.XSTREAM2.addCompatibilityAlias("com.klocwork.kwjenkinsplugin.config.KlocworkDesktopConfig", KlocworkCiConfig.class);
        }

        @Override
        public String getDisplayName() {
            return null;
        }
    }
}

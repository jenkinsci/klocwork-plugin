
package com.emenda.klocwork.config;

import com.emenda.klocwork.KlocworkConstants;
import com.emenda.klocwork.util.KlocworkBuildSpecParser;
import com.emenda.klocwork.util.KlocworkUtil;
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
import java.util.List;

public class KlocworkCiConfig extends AbstractDescribableImpl<KlocworkCiConfig> {

    private final String buildSpec;
    private final String projectDir;
    private final boolean cleanupProject;
    private final String reportFile;
    private final String additionalOpts;
    private final boolean incrementalAnalysis;
    private final KlocworkDiffAnalysisConfig diffAnalysisConfig;
    private String ciTool;

    @DataBoundConstructor
    public KlocworkCiConfig(String buildSpec, String projectDir, boolean cleanupProject, String reportFile, String additionalOpts,
                            boolean incrementalAnalysis, KlocworkDiffAnalysisConfig diffAnalysisConfig, String ciTool) {
        this.buildSpec = buildSpec;
        this.projectDir = projectDir;
        this.cleanupProject = cleanupProject;
        this.reportFile = reportFile;
        this.additionalOpts = additionalOpts;
        this.incrementalAnalysis = incrementalAnalysis;
        this.diffAnalysisConfig = diffAnalysisConfig;
        this.ciTool = ciTool;
    }

    public ArgumentListBuilder getVersionCiAgentCmd()
                                        throws IOException, InterruptedException {
        ArgumentListBuilder versionCmd = new ArgumentListBuilder("kwciagent");
        versionCmd.add("--version");
        return versionCmd;
    }

    public ArgumentListBuilder getVersionKwCheckCmd()
            throws IOException, InterruptedException {
        ArgumentListBuilder versionCmd = new ArgumentListBuilder("kwcheck");
        versionCmd.add("--version");
        return versionCmd;
    }

    public ArgumentListBuilder getCiToolCreateCmd(EnvVars envVars, FilePath workspace)
                                        throws IOException, InterruptedException {

        validateParentProjectDir(getKwlpDir(workspace, envVars).getParent());

        ArgumentListBuilder kwcheckCreateCmd = new ArgumentListBuilder(ciTool, "create");
        String projectUrl = KlocworkUtil.getKlocworkProjectUrl(envVars);
        if (!StringUtils.isEmpty(projectUrl)) {
            kwcheckCreateCmd.add("--url", projectUrl);
        }
        kwcheckCreateCmd.add("--project-dir", getKwlpDir(workspace, envVars).getRemote());
        kwcheckCreateCmd.add("--settings-dir", getKwpsDir(workspace, envVars).getRemote());
        kwcheckCreateCmd.add("--build-spec", envVars.expand(KlocworkUtil.getDefaultBuildSpec(buildSpec)));
        return kwcheckCreateCmd;
    }

    public ArgumentListBuilder getCiToolSetCmd(EnvVars envVars, FilePath workspace)
                                        throws IOException, InterruptedException {

        validateParentProjectDir(getKwlpDir(workspace, envVars).getParent());

        ArgumentListBuilder kwcheckSetCmd = new ArgumentListBuilder(ciTool, "set");
        kwcheckSetCmd.add("--project-dir", getKwlpDir(workspace, envVars).getRemote());
        String serverUrl = envVars.get(KlocworkConstants.KLOCWORK_URL);
        if (!StringUtils.isEmpty(serverUrl)) {
            URL url = new URL(serverUrl);
            kwcheckSetCmd.add("klocwork.host=" + url.getHost());
            kwcheckSetCmd.add("klocwork.port=" + Integer.toString(url.getPort()));
            kwcheckSetCmd.add("klocwork.project=" + envVars.get(KlocworkConstants.KLOCWORK_PROJECT));
        }
        return kwcheckSetCmd;
    }

    public ArgumentListBuilder getCiToolListCmd(EnvVars envVars, FilePath workspace,
                                                String diffList)
                                        throws IOException, InterruptedException {
        ArgumentListBuilder kwcheckRunCmd =
            new ArgumentListBuilder(ciTool, "list");
        kwcheckRunCmd.add("--project-dir", getKwlpDir(workspace, envVars).getRemote());
        String licenseHost = envVars.get(KlocworkConstants.KLOCWORK_LICENSE_HOST);
        if (!StringUtils.isEmpty(licenseHost)) {
            kwcheckRunCmd.add("--license-host", licenseHost);
        }

        String licensePort = envVars.get(KlocworkConstants.KLOCWORK_LICENSE_PORT);
        if (!StringUtils.isEmpty(licensePort)) {
            kwcheckRunCmd.add("--license-port", licensePort);
        }

        kwcheckRunCmd.add("-F", "xml");

        if (!StringUtils.isEmpty(additionalOpts)) {
            kwcheckRunCmd.addTokenized(envVars.expand(additionalOpts));
        }

        // add list of changed files to end of kwcheck run command
        kwcheckRunCmd.addTokenized(diffList);

        return kwcheckRunCmd;
    }

    public ArgumentListBuilder getCiToolRunCmd(EnvVars envVars, FilePath workspace,
                                               String diffList)
                                        throws IOException, InterruptedException {
        ArgumentListBuilder kwcheckRunCmd =
            new ArgumentListBuilder(ciTool, "run");
        kwcheckRunCmd.add("--project-dir", getKwlpDir(workspace, envVars).getRemote());

        if (!StringUtils.isEmpty(envVars.get(KlocworkConstants.KLOCWORK_LICENSE_HOST))) {
            kwcheckRunCmd.add("--license-host", envVars.get(KlocworkConstants.KLOCWORK_LICENSE_HOST));
            if (!StringUtils.isEmpty(envVars.get(KlocworkConstants.KLOCWORK_LICENSE_PORT))) {
                kwcheckRunCmd.add("--license-port", envVars.get(KlocworkConstants.KLOCWORK_LICENSE_PORT));
            }
        }

        kwcheckRunCmd.add("-Y", "-L"); // Report nothing

        kwcheckRunCmd.add("--build-spec", envVars.expand(KlocworkUtil.getDefaultBuildSpec(buildSpec)));
        if (!StringUtils.isEmpty(additionalOpts)) {
            kwcheckRunCmd.addTokenized(envVars.expand(additionalOpts));
        }

        // add list of changed files to end of kwcheck run command
        kwcheckRunCmd.addTokenized(diffList);

        return kwcheckRunCmd;
    }

    public ArgumentListBuilder getGitDiffCmd(EnvVars envVars) {
        ArgumentListBuilder gitDiffCmd = new ArgumentListBuilder("git");
        gitDiffCmd.add("diff", "--name-only", envVars.expand(diffAnalysisConfig.getGitPreviousCommit()));
        gitDiffCmd.add(">", getDiffFileList(envVars));
        return gitDiffCmd;
    }

    /*
    function to check if a local project already exists.
    If the creation of a project went wrong before, there may be some left over .kwlp or .kwps directories
    so we need to make sure to clean these up.
    If both .kwlp and .kwps exist then we reuse them
     */
    public boolean hasExistingProject(FilePath workspace, EnvVars envVars)
        throws IOException, InterruptedException {
        FilePath kwlp = getKwlpDir(workspace, envVars);
        FilePath kwps = getKwpsDir(workspace, envVars);

        if (cleanupProject) {
            // cleanup is forced
            cleanupExistingProject(kwlp, kwps);
        }
        // else check if a cleanup is needed because a kwcheck create command
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

    private void validateParentProjectDir(FilePath dir) throws IOException, InterruptedException {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private FilePath getKwlpDir(FilePath workspace, EnvVars envVars) {
        return new FilePath(
            workspace.child(envVars.expand(projectDir)), ".kwlp");
    }

    private FilePath getKwpsDir(FilePath workspace, EnvVars envVars) {
        return new FilePath(
            workspace.child(envVars.expand(projectDir)), ".kwps");
    }

    private void cleanupExistingProject(FilePath kwlp, FilePath kwps)
        throws IOException, InterruptedException {
        if (kwlp.exists()) {
            kwlp.deleteRecursive();
        }
        if (kwps.exists()) {
            kwps.deleteRecursive();
        }
    }

    public String getCiToolDiffList(EnvVars envVars, FilePath workspace, Launcher launcher) throws AbortException {
        try {
            List<String> fileList = launcher.getChannel().call(
                new KlocworkBuildSpecParser(workspace.getRemote(),
                    envVars.expand(getDiffFileList(envVars)),
                    envVars.expand(KlocworkUtil.getBuildSpecPath(buildSpec, workspace))));
            return String.join(" ", fileList);
        } catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }

    }

    public String getDiffFileList(EnvVars envVars) {
        String diffFileList = envVars.expand(diffAnalysisConfig.getDiffFileList());
        return diffFileList;
    }

    public boolean isGitDiffType() {
        return diffAnalysisConfig.isGitDiffType();
    }

    public String getBuildSpec() { return buildSpec; }
    public String getProjectDir() { return projectDir; }
    public boolean getCleanupProject() { return cleanupProject; }
    public String getReportFile() { return reportFile; }
    public String getAdditionalOpts() { return additionalOpts; }
    public boolean getIncrementalAnalysis() { return incrementalAnalysis; }
    public KlocworkDiffAnalysisConfig getDiffAnalysisConfig() { return diffAnalysisConfig; }
    public String getCiTool() { return ciTool; }

    public void setCiTool(String tool) {
        if(tool.equalsIgnoreCase("kwciagent")){
            ciTool = "kwciagent";
        }
        else if (tool.equalsIgnoreCase("kwcheck")){
            ciTool = "kwcheck";
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkCiConfig> {
        @Initializer(before = InitMilestone.PLUGINS_STARTED)
        public static void addAliases() {
            Items.XSTREAM2.addCompatibilityAlias("com.emenda.klocwork.config.KlocworkDesktopConfig", KlocworkCiConfig.class);
            Run.XSTREAM2.addCompatibilityAlias("com.emenda.klocwork.config.KlocworkDesktopConfig", KlocworkCiConfig.class);
        }
        public String getDisplayName() { return null; }
    }


}

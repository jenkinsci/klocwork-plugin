
package com.emenda.emendaklocwork.config;

import com.emenda.emendaklocwork.KlocworkConstants;
import com.emenda.emendaklocwork.util.KlocworkBuildSpecParser;
import com.emenda.emendaklocwork.util.KlocworkUtil;

import org.apache.commons.lang3.StringUtils;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ArgumentListBuilder;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.InterruptedException;
import java.net.URL;
import java.util.List;

public class KlocworkDiffAnalysisConfig extends AbstractDescribableImpl<KlocworkDiffAnalysisConfig> {


    // private final boolean usingGit;
    private final String diffType;
    private final String gitPreviousCommit;
    // private final boolean usingManual;
    private final String diffFileList;

    @DataBoundConstructor
    public KlocworkDiffAnalysisConfig(String diffType, String gitPreviousCommit, String diffFileList) {

        this.diffType = diffType;
        this.gitPreviousCommit = gitPreviousCommit;
        // this.usingManual = usingManual;
        this.diffFileList = diffFileList;
    }

    public boolean isGitDiffType() {
        return diffType.equals("git");
    }

    public boolean isManualDiffType() {
        return diffType.equals("manual");
    }

    public String getDiffType() { return diffType; }
    public String getGitPreviousCommit() { return gitPreviousCommit; }
    public String getDiffFileList() { return diffFileList; }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkDiffAnalysisConfig> {
        public String getDisplayName() { return null; }

        public FormValidation doCheckDiffFileList(@QueryParameter String value)
            throws IOException, ServletException {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.ok("Default is " + KlocworkConstants.DEFAULT_DIFF_FILE_LIST);
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckGitPreviousCommit(@QueryParameter String value)
            throws IOException, ServletException {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("Previous Git commit is mandatory");
            } else {
                return FormValidation.ok();
            }
        }
    }



}

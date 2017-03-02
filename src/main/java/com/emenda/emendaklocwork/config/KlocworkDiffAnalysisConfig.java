
package com.emenda.emendaklocwork.config;

import com.emenda.emendaklocwork.KlocworkConstants;
import com.emenda.emendaklocwork.util.KlocworkBuildSpecParser;
import com.emenda.emendaklocwork.util.KlocworkUtil;

import org.apache.commons.lang3.StringUtils;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ArgumentListBuilder;

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
    }


}

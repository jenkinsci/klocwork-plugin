
package com.emenda.klocwork.config;

import com.emenda.klocwork.KlocworkConstants;

import org.apache.commons.lang3.StringUtils;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

import javax.servlet.ServletException;
import java.io.IOException;

public class KlocworkDiffAnalysisConfig extends AbstractDescribableImpl<KlocworkDiffAnalysisConfig> {

    private final String diffType;
    private final String gitPreviousCommit;
    private final String diffFileList;

    @DataBoundConstructor
    public KlocworkDiffAnalysisConfig(String diffType, String gitPreviousCommit, String diffFileList) {

        this.diffType = diffType;
        this.gitPreviousCommit = gitPreviousCommit;
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
    public String getDiffFileList() {
		if (StringUtils.isEmpty(diffFileList)) {
            return KlocworkConstants.DEFAULT_DIFF_FILE_LIST;
        }
		return diffFileList; 
	}

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

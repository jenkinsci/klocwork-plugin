
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

import com.klocwork.kwjenkinsplugin.KlocworkConstants;

import org.apache.commons.lang3.StringUtils;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

import javax.servlet.ServletException;
import java.io.IOException;

public class KlocworkDifferentialAnalysisConfig extends AbstractDescribableImpl<KlocworkDifferentialAnalysisConfig> {

    private final String diffType;
    private final String gitPreviousCommit;
    private final String diffFileList;

    @DataBoundConstructor
    public KlocworkDifferentialAnalysisConfig(String diffType, String gitPreviousCommit, String diffFileList) {

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
    public static class DescriptorImpl extends Descriptor<KlocworkDifferentialAnalysisConfig> {
        public String getDisplayName() { return null; }

        public FormValidation doCheckDiffFileList(@QueryParameter String value)
            throws IOException, ServletException {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.ok(Messages.KlocworkDifferentialAnalysisConfig_default_value() + KlocworkConstants.DEFAULT_DIFF_FILE_LIST);
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckGitPreviousCommit(@QueryParameter String value)
            throws IOException, ServletException {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.error(Messages.KlocworkDifferentialAnalysisConfig_previous_commit_mandatory());
            } else {
                return FormValidation.ok();
            }
        }
    }



}

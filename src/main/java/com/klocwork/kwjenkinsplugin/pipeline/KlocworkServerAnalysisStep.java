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

package com.klocwork.kwjenkinsplugin.pipeline;

import com.google.inject.Inject;
import com.klocwork.kwjenkinsplugin.KlocworkConstants;
import com.klocwork.kwjenkinsplugin.KlocworkServerAnalysisBuilder;
import com.klocwork.kwjenkinsplugin.Messages;
import com.klocwork.kwjenkinsplugin.config.KlocworkCiConfig;
import com.klocwork.kwjenkinsplugin.config.KlocworkServerAnalysisConfig;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil;
import hudson.*;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


public class KlocworkServerAnalysisStep extends AbstractStepImpl {

    private KlocworkServerAnalysisConfig serverConfig;

    @DataBoundConstructor
    public KlocworkServerAnalysisStep(KlocworkServerAnalysisConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public KlocworkServerAnalysisConfig getServerConfig() {
        return serverConfig;
    }


    private static class KlocworkServerAnalysisStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        private transient KlocworkServerAnalysisStep step;

        @StepContextParameter
        @SuppressWarnings("unused")
        private transient Run build;

        @StepContextParameter
        @SuppressWarnings("unused")
        private transient FilePath workspace;

        @StepContextParameter
        @SuppressWarnings("unused")
        private transient Launcher launcher;

        @StepContextParameter
        @SuppressWarnings("unused")
        private transient TaskListener listener;

        @StepContextParameter
        private transient EnvVars env;

        @Override
        protected Void run() throws Exception {
            if (!KlocworkUtil.toolSupportsRepriseLicenseProvider(KlocworkCiConfig.getCiTool(), launcher, workspace, env, listener)) {
                throw new AbortException(Messages.KlocworkBuildWrapper_old_ciagent());
            }
            KlocworkServerAnalysisBuilder builder = new KlocworkServerAnalysisBuilder(step.getServerConfig());
            builder.perform(build, env, workspace, launcher, listener);
            return null;
        }
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() {
            super(KlocworkServerAnalysisStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "klocworkIntegrationStep1";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_SERVER_ANALYSIS_DISPLAY_NAME;
        }
    }
}

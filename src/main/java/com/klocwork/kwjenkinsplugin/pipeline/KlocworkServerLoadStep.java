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

import com.klocwork.kwjenkinsplugin.config.KlocworkReportConfig;
import com.klocwork.kwjenkinsplugin.config.KlocworkServerLoadConfig;
import com.klocwork.kwjenkinsplugin.KlocworkConstants;
import com.klocwork.kwjenkinsplugin.KlocworkServerLoadBuilder;
import com.google.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;


public class KlocworkServerLoadStep extends AbstractStepImpl {

    private KlocworkServerLoadConfig serverConfig;
    private KlocworkReportConfig reportConfig;

    @DataBoundConstructor
    public KlocworkServerLoadStep(KlocworkServerLoadConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @DataBoundSetter
    public void setReportConfig(KlocworkReportConfig reportConfig) {
        this.reportConfig = reportConfig;
    }

//    @DataBoundSetter
//    public void setServerConfig(KlocworkServerLoadConfig serverConfig) {
//        this.serverConfig = serverConfig;
//    }

    public KlocworkServerLoadConfig getServerConfig() { return serverConfig; }
    public KlocworkReportConfig getReportConfig() { return reportConfig; }

    private static class KlocworkServerLoadStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        private transient KlocworkServerLoadStep step;

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

            if (step.getServerConfig() == null) {
                throw new AbortException("Klocwork server configuration is null. " +
                    "Please update klocworkIntegrationStep2 pipeline step. " +
                    "This is due to an update to include a Klocwork trend chart. " +
                    "Sorry for any inconvenience caused! Enjoy the chart :)");
            }

            KlocworkServerLoadBuilder builder;
            if (step.getReportConfig() == null) {
                builder = new KlocworkServerLoadBuilder(
                        step.getServerConfig());
            }
            else{
                builder = new KlocworkServerLoadBuilder(
                        step.getServerConfig(), step.getReportConfig());
            }

            builder.perform(build, env, workspace, launcher, listener);
            return null;
        }
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() {
            super(KlocworkServerLoadStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "klocworkIntegrationStep2";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_SERVER_LOAD_DISPLAY_NAME;
        }
    }
}

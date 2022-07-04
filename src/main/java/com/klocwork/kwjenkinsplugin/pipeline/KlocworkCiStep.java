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

import com.google.common.base.Strings;
import com.klocwork.kwjenkinsplugin.KlocworkConstants;
import com.klocwork.kwjenkinsplugin.Messages;
import com.klocwork.kwjenkinsplugin.config.KlocworkCiConfig;
import com.klocwork.kwjenkinsplugin.KlocworkCiBuilder;
import com.google.inject.Inject;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Items;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class KlocworkCiStep extends AbstractStepImpl {

    private transient KlocworkCiConfig desktopConfig;
    private KlocworkCiConfig ciConfig;

    protected Object readResolve() {
        if (desktopConfig != null) {
            ciConfig = desktopConfig;
        }
        return this;
    }

    @DataBoundConstructor
    public KlocworkCiStep(KlocworkCiConfig ciConfig) {
        this.ciConfig = ciConfig;
    }

    // @DataBoundSetter
    // public void setDesktopConfig(KlocworkCiConfig ciConfig) {
    //     this.ciConfig = ciConfig;
    // }

    public KlocworkCiConfig getCiConfig() { return ciConfig; }


    private static class KlocworkCiStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        private transient KlocworkCiStep step;

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
            final String licenseProvider = env.get(KlocworkConstants.KLOCWORK_LICENSE_PROVIDER);

            if (!Strings.isNullOrEmpty(licenseProvider) && !KlocworkUtil.toolSupportsLicenseProvider(KlocworkCiConfig.getCiTool(), launcher, workspace, env, listener)) {
                throw new AbortException(Messages.KlocworkBuildWrapper_old_ciagent());
            }

            KlocworkCiBuilder builder = new KlocworkCiBuilder(step.getCiConfig());
            builder.perform(build, env, workspace, launcher, listener);
            return null;
        }
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() {
            super(KlocworkCiStepExecution.class);
        }

        @Initializer(before = InitMilestone.PLUGINS_STARTED)
        public static void addAliases() {
            Items.XSTREAM2.addCompatibilityAlias("com.klocwork.kwjenkinsplugin.pipeline.KlocworkDesktopStep", KlocworkCiStep.class);
            Run.XSTREAM2.addCompatibilityAlias("com.klocwork.kwjenkinsplugin.pipeline.KlocworkDesktopStep", KlocworkCiStep.class);
        }

        @Override
        public String getFunctionName() {
            return "klocworkIncremental";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_CI_BUILDER_DISPLAY_NAME;
        }
    }
}

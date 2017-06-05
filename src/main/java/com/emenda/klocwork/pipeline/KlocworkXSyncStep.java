package com.emenda.klocwork.pipeline;

import com.emenda.klocwork.KlocworkXSyncBuilder;
import com.emenda.klocwork.KlocworkConstants;
import com.emenda.klocwork.config.KlocworkXSyncConfig;

import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.workflow.structs.DescribableHelper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;


public class KlocworkXSyncStep extends AbstractStepImpl {

    private KlocworkXSyncConfig xsyncConfig;

    @DataBoundConstructor
    public KlocworkXSyncStep() {
    }

    @DataBoundSetter
    public void setXsyncConfig(KlocworkXSyncConfig xsyncConfig) {
        this.xsyncConfig = xsyncConfig;
    }

    public KlocworkXSyncConfig getXsyncConfig() { return xsyncConfig; }


    private static class KlocworkXSyncStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        private transient KlocworkXSyncStep step;

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

            KlocworkXSyncBuilder builder = new KlocworkXSyncBuilder(step.getXsyncConfig());
            builder.perform(build, env, workspace, launcher, listener);
            return null;
        }
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() {
            super(KlocworkXSyncStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "klocworkXSync";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_XSYNC_DISPLAY_NAME;
        }
    }
}

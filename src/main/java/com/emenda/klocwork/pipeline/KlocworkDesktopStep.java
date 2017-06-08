package com.emenda.klocwork.pipeline;

import com.emenda.klocwork.KlocworkDesktopBuilder;
import com.emenda.klocwork.config.KlocworkDesktopConfig;

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


public class KlocworkDesktopStep extends AbstractStepImpl {

    private KlocworkDesktopConfig desktopConfig;

    @DataBoundConstructor
    public KlocworkDesktopStep(KlocworkDesktopConfig desktopConfig) {
        this.desktopConfig = desktopConfig;
    }

    // @DataBoundSetter
    // public void setDesktopConfig(KlocworkDesktopConfig desktopConfig) {
    //     this.desktopConfig = desktopConfig;
    // }

    public KlocworkDesktopConfig getDesktopConfig() { return desktopConfig; }


    private static class KlocworkDesktopStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        private transient KlocworkDesktopStep step;

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

            KlocworkDesktopBuilder builder = new KlocworkDesktopBuilder(step.getDesktopConfig());
            builder.perform(build, env, workspace, launcher, listener);
            return null;
        }
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() {
            super(KlocworkDesktopStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "klocworkIncremental";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Klocwork - Incremental Diff Analysis";
        }
    }
}

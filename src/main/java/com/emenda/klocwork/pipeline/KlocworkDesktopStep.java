package com.emenda.klocwork.pipeline;

import com.emenda.klocwork.KlocworkCiBuilder;
import com.emenda.klocwork.config.KlocworkCiConfig;

import com.google.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;


public class KlocworkDesktopStep extends AbstractStepImpl {

    private KlocworkCiConfig desktopConfig;

    @DataBoundConstructor
    public KlocworkDesktopStep(KlocworkCiConfig desktopConfig) {
        this.desktopConfig = desktopConfig;
    }

    // @DataBoundSetter
    // public void setDesktopConfig(KlocworkCiConfig desktopConfig) {
    //     this.desktopConfig = desktopConfig;
    // }

    public KlocworkCiConfig getDesktopConfig() { return desktopConfig; }


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

            KlocworkCiBuilder builder = new KlocworkCiBuilder(step.getDesktopConfig());
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

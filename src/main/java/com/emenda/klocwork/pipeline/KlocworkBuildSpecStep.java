package com.emenda.klocwork.pipeline;

import com.emenda.klocwork.KlocworkBuildSpecBuilder;
import com.emenda.klocwork.KlocworkConstants;
import com.emenda.klocwork.config.KlocworkBuildSpecConfig;
import com.google.inject.Inject;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


public class KlocworkBuildSpecStep extends AbstractStepImpl {

    private KlocworkBuildSpecConfig buildSpecConfig;

    @DataBoundConstructor
    public KlocworkBuildSpecStep(KlocworkBuildSpecConfig buildSpecConfig) {
        this.buildSpecConfig = buildSpecConfig;
    }

    public KlocworkBuildSpecConfig getBuildSpecConfig() { return buildSpecConfig; }


    private static class KlocworkBuildSpecStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        private transient KlocworkBuildSpecStep step;

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

            KlocworkBuildSpecBuilder builder = new KlocworkBuildSpecBuilder(step.getBuildSpecConfig());
            // builder.setServerConfig();
            builder.perform(build, env, workspace, launcher, listener);
            return null;
        }
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() {
            super(KlocworkBuildSpecStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "klocworkBuildSpecGeneration";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_BUILD_SPEC_DISPLAY_NAME;
        }
    }
}

package com.emenda.klocwork.pipeline;

import com.emenda.klocwork.KlocworkConstants;
import com.emenda.klocwork.KlocworkServerAnalysisBuilder;
import com.emenda.klocwork.config.KlocworkServerAnalysisConfig;
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


public class KlocworkServerAnalysisStep extends AbstractStepImpl {

    private KlocworkServerAnalysisConfig serverConfig;

    @DataBoundConstructor
    public KlocworkServerAnalysisStep(KlocworkServerAnalysisConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    // @DataBoundSetter
    // public void setServerConfig(KlocworkServerAnalysisConfig serverConfig) {
    //     this.serverConfig = serverConfig;
    // }

    public KlocworkServerAnalysisConfig getServerConfig() { return serverConfig; }


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

            KlocworkServerAnalysisBuilder builder = new KlocworkServerAnalysisBuilder(step.getServerConfig());
            // builder.setServerConfig();
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

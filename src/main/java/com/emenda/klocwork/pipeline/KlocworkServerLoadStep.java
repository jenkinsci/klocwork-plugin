package com.emenda.klocwork.pipeline;

import com.emenda.klocwork.KlocworkServerLoadBuilder;
import com.emenda.klocwork.KlocworkConstants;
import com.emenda.klocwork.config.KlocworkReportConfig;
import com.emenda.klocwork.config.KlocworkServerLoadConfig;

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
        this.reportConfig = new KlocworkReportConfig(true);
    }

    @DataBoundSetter
    public void setReportConfig(KlocworkReportConfig reportConfig) {
        this.reportConfig = reportConfig;
    }

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

            KlocworkServerLoadBuilder builder = new KlocworkServerLoadBuilder(
                step.getServerConfig(), step.getReportConfig());
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

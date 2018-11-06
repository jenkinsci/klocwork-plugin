package com.emenda.klocwork.pipeline;

import com.emenda.klocwork.KlocworkConstants;
import com.emenda.klocwork.KlocworkGatewayPublisher;
import com.emenda.klocwork.config.KlocworkGatewayConfig;
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


public class KlocworkGatewayStep extends AbstractStepImpl {

    private final KlocworkGatewayConfig gatewayConfig;

    @DataBoundConstructor
    public KlocworkGatewayStep(KlocworkGatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
    }

    public KlocworkGatewayConfig getGatewayConfig() {
        return gatewayConfig;
    }

    private static class KlocworkGatewayStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        private transient KlocworkGatewayStep step;

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
            KlocworkGatewayPublisher gateway = new KlocworkGatewayPublisher(
                step.getGatewayConfig());
            gateway.perform(build, env, workspace, launcher, listener);
            return null;
        }
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() {
            super(KlocworkGatewayStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "klocworkQualityGateway";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_QUALITY_GATEWAY_DISPLAY_NAME;
        }
    }
}

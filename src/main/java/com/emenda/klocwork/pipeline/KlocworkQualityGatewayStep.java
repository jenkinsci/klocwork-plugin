package com.emenda.klocwork.pipeline;

import com.emenda.klocwork.KlocworkQualityGateway;
import com.emenda.klocwork.KlocworkConstants;
import com.emenda.klocwork.config.KlocworkPassFailConfig;
import com.emenda.klocwork.config.KlocworkDesktopGateway;

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

import java.util.List;


public class KlocworkQualityGatewayStep extends AbstractStepImpl {

    private final boolean enableServerGateway;
    private final List<KlocworkPassFailConfig> passFailConfigs;
    private final boolean enableDesktopGateway;
    private final KlocworkDesktopGateway desktopGateway;

    @DataBoundConstructor
    public KlocworkQualityGatewayStep(boolean enableServerGateway,
        List<KlocworkPassFailConfig> passFailConfigs,
        boolean enableDesktopGateway, KlocworkDesktopGateway desktopGateway) {

            this.enableServerGateway = enableServerGateway;
            this.passFailConfigs = passFailConfigs;
            this.enableDesktopGateway = enableDesktopGateway;
            this.desktopGateway = desktopGateway;
    }

    public boolean getEnableServerGateway() {
        return enableServerGateway;
    }

    public List<KlocworkPassFailConfig> getPassFailConfigs() {
        return passFailConfigs;
    }

    public boolean getEnableDesktopGateway() {
        return enableDesktopGateway;
    }

    public KlocworkDesktopGateway getDesktopGateway() {
        return desktopGateway;
    }


    private static class KlocworkQualityGatewayStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        private transient KlocworkQualityGatewayStep step;

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

            KlocworkQualityGateway gateway = new KlocworkQualityGateway(
                step.getEnableServerGateway(), step.getPassFailConfigs(),
                step.getEnableDesktopGateway(), step.getDesktopGateway());
            gateway.perform(build, env, workspace, launcher, listener);
            return null;
        }
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() {
            super(KlocworkQualityGatewayStepExecution.class);
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

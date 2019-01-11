package com.emenda.klocwork.pipeline;

import com.emenda.klocwork.KlocworkCiBuilder;
import com.emenda.klocwork.config.KlocworkCiConfig;
import com.google.inject.Inject;
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
            Items.XSTREAM2.addCompatibilityAlias("com.emenda.klocwork.pipeline.KlocworkDesktopStep", KlocworkCiStep.class);
            Run.XSTREAM2.addCompatibilityAlias("com.emenda.klocwork.pipeline.KlocworkDesktopStep", KlocworkCiStep.class);
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

package com.emenda.emendaklocwork;

import com.emenda.emendaklocwork.config.KlocworkServerAnalysisConfig;
import com.emenda.emendaklocwork.util.KlocworkUtil;

import hudson.AbortException;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Proc;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;

import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.InterruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link KlocworkServerAnalysisBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public class KlocworkServerAnalysisBuilder extends Builder {

    private final KlocworkServerAnalysisConfig serverConfig;

    @DataBoundConstructor
    public KlocworkServerAnalysisBuilder(KlocworkServerAnalysisConfig serverConfig) {
        this.serverConfig = serverConfig;

    }

    /**
     * We'll use this from the {@code config.jelly}.
     */
    public KlocworkServerAnalysisConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener)
        throws AbortException {
        KlocworkLogger logger = new KlocworkLogger("ServerBuilder", listener.getLogger());
        EnvVars envVars = null;
        try {
            envVars = build.getEnvironment(listener);

            KlocworkUtil.executeCommand(launcher, listener,
                    build.getWorkspace(), envVars,
                    serverConfig.getVersionCmd());


            KlocworkUtil.executeCommand(launcher, listener,
                    build.getWorkspace(), envVars,
                    serverConfig.getKwdeployCmd(envVars, build.getWorkspace()));

            // ignore return codes with kwbuildproject as we need to assess them
            // baed on which options user provided
            int rc_kwbuild = KlocworkUtil.executeCommand(launcher, listener,
                    build.getWorkspace(), envVars,
                    serverConfig.getKwbuildprojectCmd(envVars), true);
            if (!serverConfig.getIgnorereturnCodes() && rc_kwbuild != 0) {
                return false;
            }

        } catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }

        return true;

    }
    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link KlocworkServerAnalysisBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/KlocworkServerAnalysisBuilder/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use {@code transient}.
         */


        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         *      <p>
         *      Note that returning {@link FormValidation#error(String)} does not
         *      prevent the form from being saved. It just means that a message
         *      will be displayed to the user.
         */
        /*
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        */

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }


        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Emenda Klocwork - Step 1 - Run Server Analysis";
        }



        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            save();
            return super.configure(req,formData);
        }
    }
}

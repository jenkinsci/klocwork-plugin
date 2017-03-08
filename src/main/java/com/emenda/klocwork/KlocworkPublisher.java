package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkPassFailConfig;
import com.emenda.klocwork.config.KlocworkDesktopGateway;
import com.emenda.klocwork.services.KlocworkApiConnection;
import com.emenda.klocwork.util.KlocworkUtil;

import org.apache.commons.lang3.StringUtils;

import hudson.AbortException;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Proc;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
// import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;

import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.InterruptedException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Sample {@link Publisher}.
 *
 * <p>
 * When the user configures the project and enables this Publisher,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link KlocworkPublisher} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public class KlocworkPublisher extends Publisher {

    private final List<KlocworkPassFailConfig> passFailConfigs;
    private final boolean enableDesktopGateway;
    private final KlocworkDesktopGateway desktopGateway;
    // private final KlocworkPassFailConfig passFailConfig;

    @DataBoundConstructor
    public KlocworkPublisher(List<KlocworkPassFailConfig> passFailConfigs,
        boolean enableDesktopGateway, KlocworkDesktopGateway desktopGateway) {
    // public KlocworkPublisher(KlocworkPassFailConfig passFailConfig) {
        this.passFailConfigs = passFailConfigs;
        this.enableDesktopGateway = enableDesktopGateway;
        this.desktopGateway = desktopGateway;
    }

    /**
     * We'll use this from the {@code config.jelly}.
     */
    public List<KlocworkPassFailConfig> getPassFailConfigs() {
        return passFailConfigs;
    }

    // public KlocworkPassFailConfig getPassFailConfig() {
    //     return passFailConfig;
    // }

    public boolean getEnableDesktopGateway() {
        return enableDesktopGateway;
    }

    public KlocworkDesktopGateway getDesktopGateway() {
        return desktopGateway;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws AbortException {
        KlocworkLogger logger = new KlocworkLogger("Publisher", listener.getLogger());
        EnvVars envVars = null;
        try {
            envVars = build.getEnvironment(listener);
            // AbstractProject p = build.getProject();
            // List<Builder> builders;
            // if (p instanceof Project) {
            //     builders = ((Project) p).getBuilders();
            // } else if (p instanceof MatrixProject) {
            //     builders = ((MatrixProject) p).getBuilders();
            // } else {
            //     builders = Collections.emptyList();
            // }

            for (KlocworkPassFailConfig kw : passFailConfigs) {
                String request = "action=search&project=" + envVars.get(KlocworkConstants.KLOCWORK_PROJECT);
                if (!StringUtils.isEmpty(kw.getQuery())) {
                    request += "&query=grouping:off " + URLEncoder.encode(kw.getQuery(), "UTF-8");
                }


                logger.logMessage("Using query: " + request);
                JSONArray response;

                try {
                    String[] ltokenLine = KlocworkUtil.getLtokenValues(envVars, launcher);
                    KlocworkApiConnection kwService = new KlocworkApiConnection(
                                    KlocworkUtil.getAndExpandEnvVar(envVars, KlocworkConstants.KLOCWORK_URL),
                                    ltokenLine[KlocworkConstants.LTOKEN_USER_INDEX],
                                    ltokenLine[KlocworkConstants.LTOKEN_HASH_INDEX]);
                    response = kwService.sendRequest(request);
                } catch (IOException ex) {
                    throw new AbortException("Error: failed to connect to the Klocwork" +
                        " web API.\nCause: " + ex.getMessage());
                }

                logger.logMessage("Condition Name : " + kw.getConditionName());
                logger.logMessage("Number of issues returned : " + Integer.toString(response.size()));
                if (response.size() >= Integer.parseInt(kw.getThreshold())) {
                    logger.logMessage("Failing build...");
                    build.setResult(kw.getResultValue());
                }
                for (int i = 0; i < response.size(); i++) {
                      JSONObject jObj = response.getJSONObject(i);
                      logger.logMessage(jObj.toString());
                }
            }

        } catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }

        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }


    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link KlocworkPublisher}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/KlocworkPublisher/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // Indicates that this Publisher can be used with all kinds of project types
            return true;
        }


        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Emenda Klocwork Report";
        }



        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            save();
            return super.configure(req,formData);
        }
    }
}

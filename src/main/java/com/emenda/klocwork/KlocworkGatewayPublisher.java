package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkGatewayConfig;
import com.emenda.klocwork.config.KlocworkGatewayServerConfig;
import com.emenda.klocwork.config.KlocworkGatewayDesktopConfig;
import com.emenda.klocwork.services.KlocworkApiConnection;
import com.emenda.klocwork.util.KlocworkUtil;
import com.emenda.klocwork.util.KlocworkXMLReportParser;

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
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Project;
import hudson.model.Result;
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
import java.io.UnsupportedEncodingException;
import java.lang.InterruptedException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class KlocworkGatewayPublisher extends Publisher implements SimpleBuildStep {

    private final KlocworkGatewayConfig gatewayConfig;

    @DataBoundConstructor
    public KlocworkGatewayPublisher(KlocworkGatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
    }

    public KlocworkGatewayConfig getGatewayConfig() {
        return gatewayConfig;
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
    throws AbortException {
        EnvVars envVars = null;
        try {
            envVars = build.getEnvironment(listener);
        } catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }
        perform(build, envVars, workspace, launcher, listener);
    }


    public void perform(Run<?, ?> build, EnvVars envVars, FilePath workspace, Launcher launcher, TaskListener listener)
    throws AbortException {
        KlocworkLogger logger = new KlocworkLogger("KlocworkGatewayPublisher", listener.getLogger());
        if (gatewayConfig.getEnableServerGateway()) {
            logger.logMessage("Performing Klocwork Server Gateway");
            for (KlocworkGatewayServerConfig pfConfig : gatewayConfig.getGatewayServerConfigs()) {
                String request = "action=search&project=" + envVars.get(KlocworkConstants.KLOCWORK_PROJECT);
                if (!StringUtils.isEmpty(pfConfig.getQuery())) {
                    try {
                        request += "&query=grouping:off " + URLEncoder.encode(pfConfig.getQuery(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        throw new AbortException(ex.getMessage());
                    }

                }
                logger.logMessage("Condition Name : " + pfConfig.getConditionName());
                logger.logMessage("Using query: " + request);
                JSONArray response;

                try {
                    String[] ltokenLine = KlocworkUtil.getLtokenValues(envVars, launcher);
                    KlocworkApiConnection kwService = new KlocworkApiConnection(
                                    envVars.get(KlocworkConstants.KLOCWORK_URL),
                                    ltokenLine[KlocworkConstants.LTOKEN_USER_INDEX],
                                    ltokenLine[KlocworkConstants.LTOKEN_HASH_INDEX]);
                    response = kwService.sendRequest(request);
                } catch (IOException ex) {
                    throw new AbortException("Error: failed to connect to the Klocwork" +
                        " web API.\nCause: " + ex.getMessage());
                }


                logger.logMessage("Number of issues returned : " + Integer.toString(response.size()));
                if (response.size() >= Integer.parseInt(pfConfig.getThreshold())) {
                    logger.logMessage("Threshold exceeded. Marking build as failed.");
                    build.setResult(pfConfig.getResultValue());
                }
                for (int i = 0; i < response.size(); i++) {
                      JSONObject jObj = response.getJSONObject(i);
                      logger.logMessage(jObj.toString());
                }
            }
        }


        if (gatewayConfig.getEnableDesktopGateway()) {
			logger.logMessage("Performing Klocwork Desktop Gateway");

            String xmlReport = envVars.expand(KlocworkUtil.getDefaultKwcheckReportFile(
                gatewayConfig.getGatewayDesktopConfig().getReportFile()));
			logger.logMessage("Working with report file: " + xmlReport);

            try {
                int totalIssueCount = launcher.getChannel().call(
                    new KlocworkXMLReportParser(
                    workspace.getRemote(), xmlReport));
                logger.logMessage("Total Desktop Issues : " +
                    Integer.toString(totalIssueCount));
                logger.logMessage("Configured Threshold : " +
                    gatewayConfig.getGatewayDesktopConfig().getThreshold());
                if (totalIssueCount >= Integer.parseInt(gatewayConfig.getGatewayDesktopConfig().getThreshold())) {
                    logger.logMessage("Threshold exceeded. Marking build as failed.");
                    build.setResult(Result.FAILURE);
                }
            } catch (InterruptedException | IOException ex) {
                throw new AbortException(ex.getMessage());
            }
        }
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_QUALITY_GATEWAY_DISPLAY_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }
}

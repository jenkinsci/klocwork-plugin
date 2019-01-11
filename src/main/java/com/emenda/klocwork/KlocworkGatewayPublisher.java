package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkGatewayCiConfig;
import com.emenda.klocwork.config.KlocworkGatewayConfig;
import com.emenda.klocwork.config.KlocworkGatewayServerConfig;
import com.emenda.klocwork.reporting.KlocworkDashboard;
import com.emenda.klocwork.reporting.KlocworkProjectRedirectLink;
import com.emenda.klocwork.services.KlocworkApiConnection;
import com.emenda.klocwork.definitions.KlocworkIssue;
import com.emenda.klocwork.util.KlocworkUtil;
import com.emenda.klocwork.util.KlocworkXMLReportParser;

import hudson.AbortException;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import com.emenda.klocwork.util.KlocworkXMLReportParserIssueList;
import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;

import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.lang.InterruptedException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class KlocworkGatewayPublisher extends Publisher implements SimpleBuildStep {

    private final KlocworkGatewayConfig gatewayConfig;

    @DataBoundConstructor
    public KlocworkGatewayPublisher(KlocworkGatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        List<Action> actions = new ArrayList<>();
        boolean actionAdded = false;
        if(gatewayConfig.getGatewayCiConfigs() != null) {
            for (KlocworkGatewayCiConfig config : gatewayConfig.getGatewayCiConfigs()) {
                if (config.isEnableHTMLReporting()) {
                    actions.add(new KlocworkProjectRedirectLink());
                    actionAdded = true;
                    break;
                }
            }
        }
        if(gatewayConfig.getGatewayServerConfigs() != null && !actionAdded) {
            for (KlocworkGatewayServerConfig config : gatewayConfig.getGatewayServerConfigs()) {
                if (config.isEnableHTMLReporting()) {
                    actions.add(new KlocworkProjectRedirectLink());
                    break;
                }
            }
        }
        return actions;
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
        boolean stopBuild = false;
        boolean shouldDashboardLocal = false;
        boolean shouldDashboardServer = false;
        ArrayList<KlocworkIssue> localIssues = new ArrayList<>();
        ArrayList<KlocworkIssue> serverIssues = new ArrayList<>();
        if (gatewayConfig.getEnableServerGateway()) {
            logger.logMessage("Performing Klocwork Server Gateway");
            // check env vars are set, otherwise this throws AbortException
            KlocworkUtil.validateServerConfigs(envVars);
            for (KlocworkGatewayServerConfig pfConfig : gatewayConfig.getGatewayServerConfigs()) {
                String request = KlocworkUtil.createKlocworkAPIRequest(
                    "search", pfConfig.getQuery(), envVars);
                logger.logMessage("Condition Name : " + pfConfig.getConditionName());
                logger.logMessage("Using query: " + request);

                JSONArray response = KlocworkUtil.getJSONRespose(request, envVars, launcher);

                logger.logMessage("Number of issues returned : " + Integer.toString(response.size()));
                logger.logMessage("Configured Threshold : " + pfConfig.getThreshold());
                if (response.size() >= Integer.parseInt(pfConfig.getThreshold())) {
                    logger.logMessage("Threshold exceeded. Marking build as failed.");
                    build.setResult(pfConfig.getResultValue());
                    if(pfConfig.getStopBuild()){
                        stopBuild = true;
                    }
                }
                for (int i = 0; i < response.size(); i++) {
                    JSONObject jObj = response.getJSONObject(i);
                    if(pfConfig.isEnableHTMLReporting()) {
                        shouldDashboardServer = true;
                        if (!isIssueInList(jObj.getString("id"), serverIssues)) {
                            String line = "";
                            if (jObj.containsKey("line")) {
                                line = jObj.getString("line");
                            }
                            serverIssues.add(new KlocworkIssue(jObj.getString("id"),
                                    jObj.getString("code"), jObj.getString("message"), jObj.getString("file"),
                                    line, jObj.getString("severity"), jObj.getString("status")
                            ));
                        }
                    }
                    else {
                        logger.logMessage(jObj.toString());
                    }
                }
            }
        }
        if (gatewayConfig.getEnableCiGateway()) {
			logger.logMessage("Performing Klocwork Ci Gateway");
			if (gatewayConfig.getGatewayCiConfigs() != null) {
                for (KlocworkGatewayCiConfig ciConfig : gatewayConfig.getGatewayCiConfigs()) {
                    ArrayList<KlocworkIssue> qgate_issues = new ArrayList<>();
                    logger.logMessage("Checking ci gateway: " + ciConfig.getName());
                    String xmlReport = envVars.expand(KlocworkUtil.getDefaultKwcheckReportFile(
                            ciConfig.getReportFile()));
                    logger.logMessage("Working with report file: " + xmlReport);
                    try {
                        int qualityGateIssues;
                        if (ciConfig.isEnableHTMLReporting()) {
                            shouldDashboardLocal = true;
                            qgate_issues = launcher.getChannel().call(
                                    new KlocworkXMLReportParserIssueList(workspace.getRemote(), xmlReport, ciConfig.getEnabledSeverites(), ciConfig.getEnabledStatuses()));
                            qualityGateIssues = qgate_issues.size();
                        } else {
                            qualityGateIssues = launcher.getChannel().call(
                                    new KlocworkXMLReportParser(workspace.getRemote(), xmlReport, ciConfig.getEnabledSeverites(), ciConfig.getEnabledStatuses()));
                        }
                        for (KlocworkIssue qgate_issue : qgate_issues) {
                            if (!isIssueInList(qgate_issue.getId(), localIssues)) {
                                localIssues.add(qgate_issue);
                            }
                        }
                        logger.logMessage("Total Ci Issues : " +
                                Integer.toString(qualityGateIssues));
                        logger.logMessage("Configured Threshold : " +
                                ciConfig.getThreshold());
                        if (qualityGateIssues >= Integer.parseInt(ciConfig.getThreshold())) {
                            logger.logMessage("Threshold exceeded. Marking build as failed.");
                            if(ciConfig.getFailUnstable()){
                              build.setResult(Result.UNSTABLE);
                            } else {
                              build.setResult(Result.FAILURE);
                            }
                            if (ciConfig.getStopBuild()) {
                                stopBuild = true;
                            }
                        }
                    } catch (InterruptedException | IOException ex) {
                        throw new AbortException(ex.getMessage());
                    }
                }
            }
            else{
			    logger.logMessage("WARNING: Quality gate enabled, but could not find configuration");
                build.setResult(Result.UNSTABLE);
            }
        }

        if(shouldDashboardLocal || shouldDashboardServer){
            build.addAction(new KlocworkDashboard(localIssues, serverIssues, shouldDashboardLocal, shouldDashboardServer));
        }

        if(stopBuild){
            throw new AbortException("Stopping build due to configuration");
        }
    }

    private boolean isIssueInList(String issue_id, ArrayList<KlocworkIssue> issues) {
        for(KlocworkIssue issue : issues) {
            if (issue.getId().equals(issue_id)) {
                return true;
            }
        }
        return false;
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

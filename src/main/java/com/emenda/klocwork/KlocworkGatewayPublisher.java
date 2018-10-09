package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkGatewayConfig;
import com.emenda.klocwork.config.KlocworkGatewayServerConfig;
import com.emenda.klocwork.reporting.KlocworkDashboard;
import com.emenda.klocwork.reporting.KlocworkProjectRedirectLink;
import com.emenda.klocwork.services.KlocworkApiConnection;
import com.emenda.klocwork.util.KlocworkIssue;
import com.emenda.klocwork.util.KlocworkUtil;
import com.emenda.klocwork.util.KlocworkXMLReportParser;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class KlocworkGatewayPublisher extends Publisher implements SimpleBuildStep {

    private final KlocworkGatewayConfig gatewayConfig;
    private transient int totalIssuesDesktop;
    private transient int thresholdDesktop;
    private int totalIssuesCi;
    private int thresholdCi;

    @DataBoundConstructor
    public KlocworkGatewayPublisher(KlocworkGatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
        this.totalIssuesCi = 0;
        this.thresholdCi = 0;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        List<Action> actions = new ArrayList<>();
        if(gatewayConfig.isEnableHTMLReporting()) {
            actions.add(new KlocworkProjectRedirectLink());
        }
        return actions;
    }

    protected Object readResolve() {
        if (totalIssuesDesktop <= 0) {
            totalIssuesCi = totalIssuesDesktop;
        }
        if (thresholdDesktop <= 0) {
            thresholdCi = thresholdDesktop;
        }
        return this;
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
        ArrayList<KlocworkIssue> localIssues = new ArrayList<>();
        if (gatewayConfig.getEnableServerGateway()) {
            logger.logMessage("Performing Klocwork Server Gateway");
            for (KlocworkGatewayServerConfig pfConfig : gatewayConfig.getGatewayServerConfigs()) {
                String request = "action=search&project=" + envVars.get(KlocworkConstants.KLOCWORK_PROJECT);
                if (!StringUtils.isEmpty(pfConfig.getQuery())) {
                    try {
                        request += "&query=";
                        if(!pfConfig.getQuery().toLowerCase().contains("grouping:off")
                                && !pfConfig.getQuery().toLowerCase().contains("grouping:on")){
                            request += "grouping:off ";
                        }
                        request += URLEncoder.encode(pfConfig.getQuery(), "UTF-8");
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
                      logger.logMessage(jObj.toString());
                }
            }
        }


        if (gatewayConfig.getEnableCiGateway()) {
			logger.logMessage("Performing Klocwork Ci Gateway");

            String xmlReport = envVars.expand(KlocworkUtil.getDefaultKwcheckReportFile(
                gatewayConfig.getGatewayCiConfig().getReportFile()));
			logger.logMessage("Working with report file: " + xmlReport);

            try {
                if(gatewayConfig.isEnableHTMLReporting()){
                    localIssues = launcher.getChannel().call(
                            new KlocworkXMLReportParserIssueList(workspace.getRemote(), xmlReport));
                    totalIssuesCi = localIssues.size();
                }
                else {
                    totalIssuesCi = launcher.getChannel().call(
                            new KlocworkXMLReportParser(workspace.getRemote(), xmlReport));
                }
                logger.logMessage("Total Ci Issues : " +
                    Integer.toString(totalIssuesCi));
                logger.logMessage("Configured Threshold : " +
                    gatewayConfig.getGatewayCiConfig().getThreshold());
                thresholdCi = Integer.parseInt(gatewayConfig.getGatewayCiConfig().getThreshold());
                if (totalIssuesCi >= thresholdCi) {
                    logger.logMessage("Threshold exceeded. Marking build as failed.");
                    build.setResult(Result.FAILURE);
                    if(gatewayConfig.getGatewayCiConfig().getStopBuild()){
                        stopBuild = true;
                    }
                }
            } catch (InterruptedException | IOException ex) {
                throw new AbortException(ex.getMessage());
            }
        }

        if(gatewayConfig.isEnableHTMLReporting()){
            build.addAction(new KlocworkDashboard(localIssues));
        }

        if(stopBuild){
            throw new AbortException("Stopping build due to configuration");
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

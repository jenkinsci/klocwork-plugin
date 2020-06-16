/*
 * *****************************************************************************
 * Copyright (c) 2020 Rogue Wave Software, Inc., a Perforce company
 * Author : Klocwork
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * *****************************************************************************
 */

package com.klocwork.kwjenkinsplugin;

import com.klocwork.kwjenkinsplugin.config.KlocworkFailureConditionCiConfig;
import com.klocwork.kwjenkinsplugin.config.KlocworkFailureConditionConfig;
import com.klocwork.kwjenkinsplugin.config.KlocworkFailureConditionServerConfig;
import com.klocwork.kwjenkinsplugin.definitions.KlocworkIssue;
import com.klocwork.kwjenkinsplugin.reporting.KlocworkDashboard;
import com.klocwork.kwjenkinsplugin.reporting.KlocworkResultsAction;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil;

import com.klocwork.kwjenkinsplugin.util.KlocworkXMLReportParserIssueList;
import hudson.AbortException;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;

import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.lang.InterruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * 1. Fails build (or sets to unstable) based on threshold
 * 2. Generates HTML reports from XML and JSON files created at the analysis step
 *
 * For full server analysis - uses old KlocworkDashboard to display issues
 * For local differential analysis - uses issues from JSON generated at the CI analysis step (KlocworkCiBuilder),
 * transforms into format supported by html/jelly files moved from old kwjenkins
 *
 */
public class KlocworkFailureConditionPublisher extends Publisher implements SimpleBuildStep {

    private static final Logger debugLogger = Logger.getLogger(KlocworkFailureConditionPublisher.class.getName());

    private final KlocworkFailureConditionConfig failureConditionConfig;

    @DataBoundConstructor
    public KlocworkFailureConditionPublisher(KlocworkFailureConditionConfig failureConditionConfig) {
        this.failureConditionConfig = failureConditionConfig;
    }

    public KlocworkFailureConditionConfig getFailureConditionConfig() {
        return failureConditionConfig;
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
        KlocworkLogger logger = new KlocworkLogger("KlocworkFailureConditionPublisher", listener.getLogger());
        boolean stopBuild = false;
        boolean shouldDashboardLocal = false;
        boolean shouldDashboardServer = false;

        List<KlocworkIssue> localIssues = new ArrayList<>();
        List<KlocworkIssue> serverIssues = new ArrayList<>();

        debugLogger.fine("[" + this.getClass().getName() + "] - Performing Failure Condition validation");

        if (failureConditionConfig.getEnableServerFailureCondition()) {
            debugLogger.fine("[" + this.getClass().getName() + "] - Entered server Failure Condition validation");
            logger.logMessage("Performing Klocwork Server Build Failure Condition validation");
            // check env vars are set, otherwise this throws AbortException
            KlocworkUtil.validateServerConfigs(envVars);
            for (KlocworkFailureConditionServerConfig pfConfig : failureConditionConfig.getFailureConditionServerConfigs()) {
                debugLogger.fine("[" + this.getClass().getName() + "] - " + pfConfig.toString());
                String request = KlocworkUtil.createKlocworkAPIRequestOld(
                    "search", pfConfig.getQuery(), envVars);
                logger.logMessage("Condition Name : " + pfConfig.getConditionName());
                logger.logMessage("Using query: " + request);

                JSONArray response = KlocworkUtil.getJSONResponse(request, envVars, launcher);

                logger.logMessage("Number of issues returned : " + Integer.toString(response.size()));
                debugLogger.fine("[" + this.getClass().getName() + "] - returns "+Integer.toString(response.size())+" issues");
                logger.logMessage("Configured Threshold : " + pfConfig.getThreshold());
                if (response.size() >= Integer.parseInt(pfConfig.getThreshold())) {
                    debugLogger.fine("[" + this.getClass().getName() + "] - Build Failure Condition triggered");
                    logger.logMessage("Threshold exceeded. Marking build as failed.");
                    build.setResult(pfConfig.getResultValue());
                    if(pfConfig.getStopBuild()){
                        stopBuild = true;
                    }
                }
                else{
                    debugLogger.fine("[" + this.getClass().getName() + "] - Build Failure Condition passed");
                }
                if(pfConfig.isEnableHTMLReporting()) {
                    if (!shouldDashboardServer) {
                        debugLogger.fine("[" + this.getClass().getName() + "] - setting shouldDashboardServer to true");
                        shouldDashboardServer = true;
                    }
                }
                for (int i = 0; i < response.size(); i++) {
                    JSONObject jObj = response.getJSONObject(i);
                    if(pfConfig.isEnableHTMLReporting()) {
                        if (!isIssueInList(jObj.getString("id"), serverIssues)) {
                            String line = "";
                            if (jObj.containsKey("line")) {
                                line = jObj.getString("line");
                            }
                            serverIssues.add(new KlocworkIssue(jObj.getString("id"),
                                    jObj.getString("code"), jObj.getString("message"), jObj.getString("file"),
                                    line, jObj.getString("severity"), jObj.getString("severityCode"), jObj.getString("status")
                            ));
                        }
                    }
                    else {
                        debugLogger.fine("[" + this.getClass().getName() + "] - not setting shouldDashboardServer");
                        logger.logMessage(jObj.toString());
                    }
                }
            }
        }
        if (failureConditionConfig.getEnableCiFailureCondition()) {
            logger.logMessage("Performing Klocwork Ci Build Failure Condition validation");
            if (failureConditionConfig.getFailureConditionCiConfigs() != null) {
                debugLogger.fine("[" + this.getClass().getName() + "] - Entered ci Failure Condition validation");

                for (KlocworkFailureConditionCiConfig ciConfig : failureConditionConfig.getFailureConditionCiConfigs()) {
                    debugLogger.fine("[" + this.getClass().getName() + "] - " + ciConfig.toString());
                    ArrayList<KlocworkIssue> failureConditionIssuesList = new ArrayList<>();
                    logger.logMessage("Checking ci Failure Condition validation: " + ciConfig.getName());
                    String xmlReport = envVars.expand(KlocworkUtil.getDefaultReportFileName(
                            ciConfig.getReportFile()));

                    logger.logMessage("Working with report file: " + xmlReport);
                    try {
                        int failureConditionIssuesCount;

                        if (ciConfig.isEnableHTMLReporting()) {
                            shouldDashboardLocal = true;
                        }

                        failureConditionIssuesList = launcher.getChannel().call(
                                new KlocworkXMLReportParserIssueList(workspace.getRemote(), xmlReport, ciConfig.getEnabledSeverites(), ciConfig.getEnabledStatuses()));

                        failureConditionIssuesCount = failureConditionIssuesList.size();

                        for (KlocworkIssue issue : failureConditionIssuesList) {
                            if (!isIssueInList(issue.getId(), localIssues)) {
                                localIssues.add(issue);
                            }
                        }

                        logger.logMessage("Total Ci Issues : " +
                                          failureConditionIssuesCount);
                        debugLogger.fine("[" + this.getClass().getName() + "] - returns " + failureConditionIssuesCount + " issues");
                        logger.logMessage("Configured Threshold : " +
                                ciConfig.getThreshold());
                        if (failureConditionIssuesCount >= Integer.parseInt(ciConfig.getThreshold())) {
                            debugLogger.fine("[" + this.getClass().getName() + "] - Build Failure Condition triggered");
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
                        else{
                            debugLogger.fine("[" + this.getClass().getName() + "] - Build Failure Condition passed");
                        }
                    } catch (InterruptedException | IOException ex) {
                        debugLogger.fine("[" + this.getClass().getName() + "] - exception thrown: "+ ex.getMessage());
                        throw new AbortException(ex.getMessage());
                    }
                }
            }
            else{
                debugLogger.fine("[" + this.getClass().getName() + "] - Build Failure Conditions enabled, but could not find configuration");
                logger.logMessage("WARNING: Build Failure Conditions enabled, but could not find configuration");
                build.setResult(Result.UNSTABLE);
            }
        }

        if(shouldDashboardLocal){
            debugLogger.fine("[" + this.getClass().getName() + "] - Entered addAction [ shouldDashboardLocal:"+shouldDashboardLocal+", shouldDashboardServer:"+shouldDashboardServer+" ]");
            build.addAction(new KlocworkResultsAction(build, workspace.getRemote(), workspace.getChannel(), launcher, envVars, listener, failureConditionConfig));

        } else if (shouldDashboardServer) {
            build.addAction(new KlocworkDashboard(localIssues, serverIssues, shouldDashboardLocal, shouldDashboardServer));
        } else{
            debugLogger.fine("[" + this.getClass().getName() + "] - Not entered addAction [ shouldDashboardLocal:"+shouldDashboardLocal+", shouldDashboardServer:"+shouldDashboardServer+" ]");
        }

        if(stopBuild){
            debugLogger.fine("[" + this.getClass().getName() + "] - stopped build");
            throw new AbortException("Stopping build due to configuration");
        }
    }

    private boolean isIssueInList(String issue_id, List<? extends KlocworkIssue> issues) {
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
            return KlocworkConstants.KLOCWORK_BUILD_FAILURE_CONDITION_DISPLAY_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }
}

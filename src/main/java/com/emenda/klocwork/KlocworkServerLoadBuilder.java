package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkReportConfig;
import com.emenda.klocwork.config.KlocworkServerLoadConfig;
import com.emenda.klocwork.util.KlocworkUtil;

import jenkins.tasks.SimpleBuildStep;

import hudson.AbortException;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.lang.InterruptedException;
import java.util.HashMap;
import java.util.Map;

public class KlocworkServerLoadBuilder extends Builder implements SimpleBuildStep {
    // TODO - artifact build.log, parse.log, kwloaddb.log if build fails
    private KlocworkServerLoadConfig serverConfig;
    private KlocworkReportConfig reportConfig;

    protected Object readResolve() {
        if(this.reportConfig == null){
            this.reportConfig = new KlocworkReportConfig(false);
        }
        return this;
    }

    @DataBoundConstructor
    public KlocworkServerLoadBuilder(KlocworkServerLoadConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.reportConfig = new KlocworkReportConfig(false);
    }

    public KlocworkServerLoadBuilder(KlocworkServerLoadConfig serverConfig,
    KlocworkReportConfig reportConfig) {
        this.serverConfig = serverConfig;
        this.reportConfig = reportConfig;
    }

    @DataBoundSetter
    public void setReportConfig(KlocworkReportConfig reportConfig) {
        this.reportConfig = reportConfig;
    }

    @DataBoundSetter
    public void setServerConfig(KlocworkServerLoadConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public KlocworkServerLoadConfig getServerConfig() {
        return serverConfig;
    }

    public KlocworkReportConfig getReportConfig() {
        return reportConfig;
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
        KlocworkLogger logger = new KlocworkLogger("KlocworkServerLoadConfig", listener.getLogger());
        logger.logMessage("Starting Klocwork Server Analysis Load Step");

        // validate server settings needed for build-step. AbortException is
        // thrown if URL and server project are not provided as we cannot perform
        // a server analysis without these settings
        KlocworkUtil.validateServerConfigs(envVars);

        // cannot check return code of kwadmin --version as it is non-zero
        // for some reason...
        KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
                serverConfig.getVersionCmd(), true);

        KlocworkUtil.executeCommand(launcher, listener,
                workspace, envVars,
                serverConfig.getKwadminLoadCmd(envVars, workspace));

        if(reportConfig != null && reportConfig.isDisplayChart()) {
            createBuildAction(logger, build, envVars, launcher);
        }

    }

    private void createBuildAction(KlocworkLogger logger, Run<?, ?> build, EnvVars envVars,
    Launcher launcher) throws AbortException {
        String request = KlocworkUtil.createKlocworkAPIRequestOld("search", reportConfig.getQuery(), envVars);
        logger.logMessage("Using query: " + request);
        JSONArray response = KlocworkUtil.getJSONRespose(request, envVars, launcher);
        logger.logMessage("Number of issues returned : " + Integer.toString(response.size()));

        Map<String, Integer> severityMap = new HashMap<String,Integer>();
        for (int i = 0; i < response.size(); i++) {
            String severity = response.getJSONObject(i).getString("severityCode");
            if (StringUtils.isEmpty(severity) || StringUtils.isEmpty(getSeverity_en(severity))) {
                logger.logMessage(String.format("WARNING: found empty severity %s", severity));
            } else {
                // increment count
                severityMap.put(getSeverity_en(severity), severityMap.getOrDefault(getSeverity_en(severity), 0) + 1);
            }
        }

        //get project ID for server url
        request = KlocworkUtil.createKlocworkAPIRequest("projects", new HashMap<>());
        logger.logMessage("Using query: " + request);
        response = KlocworkUtil.getJSONRespose(request, envVars, launcher);
        String projectId = "";
        for (int i = 0; i < response.size(); i++) {
            String projectName = response.getJSONObject(i).getString("name");
            if (!StringUtils.isEmpty(projectName) && projectName.equals(envVars.get(KlocworkConstants.KLOCWORK_PROJECT))){
                projectId = response.getJSONObject(i).getString("id");
                break;
            }
        }
        build.addAction(new KlocworkBuildAction(build, severityMap, envVars, serverConfig.getBuildName(), reportConfig, projectId));
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_SERVER_LOAD_DISPLAY_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            save();
            return super.configure(req,formData);
        }
    }
    
    private String getSeverity_en(String severityCode) {
    	String severity_en = "";
    	int severityLevel = 0;
		try {
			severityLevel = Integer.parseInt(severityCode);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
    	switch(severityLevel) {
    	case 1:
    		severity_en = KlocworkConstants.KLOCWORK_ISSUE_CRITICAL;
    		break;
    	case 2:
    		severity_en = KlocworkConstants.KLOCWORK_ISSUE_ERROR;
    		break;
    	case 3:
    		severity_en = KlocworkConstants.KLOCWORK_ISSUE_WARNING;
    		break;
    	case 4:
    		severity_en = KlocworkConstants.KLOCWORK_ISSUE_REVIEW;
    		break;
    	}
    	return severity_en;
    }
}

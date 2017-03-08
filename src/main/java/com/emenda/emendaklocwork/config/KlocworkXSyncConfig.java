
package com.emenda.emendaklocwork.config;

import com.emenda.emendaklocwork.KlocworkConstants;
import com.emenda.emendaklocwork.services.KlocworkApiConnection;
import com.emenda.emendaklocwork.util.KlocworkUtil;


import org.apache.commons.lang3.StringUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.StringBuilder;
import java.lang.InterruptedException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KlocworkXSyncConfig extends AbstractDescribableImpl<KlocworkXSyncConfig> {

    private final boolean dryRun;
    private final String lastSync;
    private final String projectRegexp;
    private final boolean statusAnalyze;
    private final boolean statusIgnore;
    private final boolean statusNotAProblem;
    private final boolean statusFix;
    private final boolean statusFixInNextRelease;
    private final boolean statusFixInLaterRelease;
    private final boolean statusDefer;
    private final boolean statusFilter;
    private final String additionalOpts;

    @DataBoundConstructor
    public KlocworkXSyncConfig(boolean dryRun, String lastSync, String projectRegexp,
                boolean statusAnalyze, boolean statusIgnore,
                boolean statusNotAProblem, boolean statusFix,
                boolean statusFixInNextRelease, boolean statusFixInLaterRelease,
                boolean statusDefer, boolean statusFilter,
                String additionalOpts) {

        this.dryRun = dryRun;
        this.lastSync = lastSync;
        this.projectRegexp = projectRegexp;
        this.statusAnalyze = statusAnalyze;
        this.statusIgnore = statusIgnore;
        this.statusNotAProblem = statusNotAProblem;
        this.statusFix = statusFix;
        this.statusFixInNextRelease = statusFixInNextRelease;
        this.statusFixInLaterRelease = statusFixInLaterRelease;
        this.statusDefer = statusDefer;
        this.statusFilter = statusFilter;
        this.additionalOpts = additionalOpts;
    }

    public ArgumentListBuilder getVersionCmd()
                                        throws IOException, InterruptedException {
        ArgumentListBuilder versionCmd = new ArgumentListBuilder("kwxsync");
        versionCmd.add("--version");
        return versionCmd;
    }

    public ArgumentListBuilder getxsyncCmd(EnvVars envVars, Launcher launcher)
                                            throws AbortException {

        ArgumentListBuilder xsyncCmd = new ArgumentListBuilder("kwxsync");
        String projectList = getProjectList(envVars, launcher);
        String lastSyncArg = getLastSyncDateDiff();

        xsyncCmd.add("--url", KlocworkUtil.getAndExpandEnvVar(envVars, KlocworkConstants.KLOCWORK_URL));
        xsyncCmd.add("--last-sync", lastSyncArg);

        if (dryRun) {
            xsyncCmd.add("--dry");
        }

        List<String> statuses = new ArrayList<String>();
        if (statusAnalyze) {
            statuses.add("Analyze");
        }
        if (statusIgnore) {
            statuses.add("Ignore");
        }
        if (statusNotAProblem) {
            statuses.add("Not a Problem");
        }
        if (statusFix) {
            statuses.add("Fix");
        }
        if (statusFixInNextRelease) {
            statuses.add("Fix in Next Release");
        }
        if (statusFixInLaterRelease) {
            statuses.add("Fix in Later Release");
        }
        if (statusDefer) {
            statuses.add("Defer");
        }
        if (statusFilter) {
            statuses.add("Filter");
        }

        if (statuses.size() > 0) {
            xsyncCmd.add("--statuses");
            xsyncCmd.add(StringUtils.join(statuses,"\",\""));
        }

        if (!StringUtils.isEmpty(additionalOpts)) {
            xsyncCmd.addTokenized(envVars.expand(additionalOpts));
        }

        xsyncCmd.addTokenized(projectList);
        return xsyncCmd;
    }

    private String getProjectList(EnvVars envVars, Launcher launcher)
        throws AbortException {
        StringBuilder projectList = new StringBuilder();
        String request = "action=projects";
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
                " web API.\nMessage: " + ex.getMessage());
                //  + "\nStacktrace:\n" +
                // KlocworkUtil.exceptionToString(ex));
        }

        Pattern p = Pattern.compile(projectRegexp);
        for (int i = 0; i < response.size(); i++) {
              JSONObject jObj = response.getJSONObject(i);
              Matcher m = p.matcher(jObj.getString("name"));
              if (m.find()) {
                  projectList.append("\"" + jObj.getString("name") + "\"");
                  projectList.append(" ");
              }
        }
        if (StringUtils.isEmpty(projectList)) {
            throw new AbortException("Could not match any projects on server " +
                KlocworkUtil.getAndExpandEnvVar(envVars, KlocworkConstants.KLOCWORK_URL) +
                " with regular expression \"" + projectRegexp + "\"");
        }

        return projectList.toString();
    }

    private String getLastSyncDateDiff() throws NumberFormatException, AbortException {
        Pattern p = Pattern.compile(KlocworkConstants.REGEXP_LASTSYNC);
        Matcher m = p.matcher(lastSync);
        if (!m.find()) {
            throw new AbortException("Error: Could not match Last Sync value " +
                lastSync + " using regular expression. " +
                "Please check date/time format on job config.");
        }

        // get current date/time
        DateTime date = new DateTime(new Date());
        DateTimeFormatter dtf = DateTimeFormat.forPattern(KlocworkConstants.LASTSYNC_FORMAT);

        if (!isStringNumZero(m.group(KlocworkConstants.REGEXP_GROUP_DAY))) {
            date = date.minusDays(Integer.valueOf(m.group(KlocworkConstants.REGEXP_GROUP_DAY)));
        }
        if (!isStringNumZero(m.group(KlocworkConstants.REGEXP_GROUP_MONTH))) {
            date = date.minusMonths(Integer.valueOf(m.group(KlocworkConstants.REGEXP_GROUP_MONTH)));
        }
        if (!isStringNumZero(m.group(KlocworkConstants.REGEXP_GROUP_YEAR))) {
            date = date.minusYears(Integer.valueOf(m.group(KlocworkConstants.REGEXP_GROUP_YEAR)));
        }
        if (!isStringNumZero(m.group(KlocworkConstants.REGEXP_GROUP_HOUR))) {
            date = date.minusHours(Integer.valueOf(m.group(KlocworkConstants.REGEXP_GROUP_HOUR)));
        }
        if (!isStringNumZero(m.group(KlocworkConstants.REGEXP_GROUP_MINUTE))) {
            date = date.minusMinutes(Integer.valueOf(m.group(KlocworkConstants.REGEXP_GROUP_MINUTE)));
        }
        if (!isStringNumZero(m.group(KlocworkConstants.REGEXP_GROUP_SECOND))) {
            date = date.minusSeconds(Integer.valueOf(m.group(KlocworkConstants.REGEXP_GROUP_SECOND)));
        }

        return dtf.print(date);
    }

    private boolean isStringNumZero(String num) {
        if (num.trim().matches("0+")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean getDryRun() { return dryRun; }
    public String getLastSync() { return lastSync; }
    public String getProjectRegexp() { return projectRegexp; }
    public boolean isKlocworkStatusAnalyze() { return statusAnalyze; }
    public boolean isKlocworkStatusIgnore() { return statusIgnore; }
    public boolean isKlocworkStatusNotAProblem() { return statusNotAProblem; }
    public boolean isKlocworkStatusFix() { return statusFix; }
    public boolean isKlocworkStatusFixInNextRelease() { return statusFixInNextRelease; }
    public boolean isKlocworkStatusFixInLaterRelease() { return statusFixInLaterRelease; }
    public boolean isKlocworkStatusDefer() { return statusDefer; }
    public boolean isKlocworkStatusFilter() { return statusFilter; }
    public String getAdditionalOpts() { return additionalOpts; }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkXSyncConfig> {
        public String getDisplayName() { return null; }

        public FormValidation doCheckLastSync(@QueryParameter String value)
            throws IOException, ServletException {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("Last Sync is mandatory");
            } else {
                Pattern p = Pattern.compile(KlocworkConstants.REGEXP_LASTSYNC);
                Matcher m = p.matcher(value);
                if (!m.find()) {
                    return FormValidation.error("Error: Could not match Last Sync value " +
                        value + " using regular expression. " +
                        "Please check date/time format on job config.");
                } else {
                    return FormValidation.ok();
                }
            }
        }
    }

}

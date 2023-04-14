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

package com.klocwork.kwjenkinsplugin.reporting;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.klocwork.kwjenkinsplugin.KlocworkConstants;
import com.klocwork.kwjenkinsplugin.config.KlocworkFailureConditionCiConfig;
import com.klocwork.kwjenkinsplugin.config.KlocworkFailureConditionConfig;
import com.klocwork.kwjenkinsplugin.definitions.*;
import com.klocwork.kwjenkinsplugin.dto.HtmlDetailedIssueData;
import com.klocwork.kwjenkinsplugin.dto.HtmlReportIssues;
import com.klocwork.kwjenkinsplugin.dto.KlocworkJsonIssue;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil.StreamReferences;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.ArgumentListBuilder;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.klocwork.kwjenkinsplugin.KlocworkConstants.KLOCWORK_URL;
import static java.util.stream.Collectors.groupingBy;

public class KlocworkResultsAction implements Action, LastBuildAction {
    private static final String KLOCWORK_LTOKEN = "KLOCWORK_LTOKEN"; //NON-NLS
    private static final String LTOKEN = "ltoken"; //NON-NLS
    private static final String KLOCWORK_USER_OVERRIDE = "KLOCWORK_USER_OVERRIDE"; //NON-NLS

    private final Run<?, ?> build;
    private final String url;
    private final String text;
    private final String icon;

    private final transient Launcher launcher;
    private final transient EnvVars envVars;
    private final transient TaskListener listener;
    private final transient String remotePath;
    private final transient VirtualChannel channel;
    private final transient KlocworkFailureConditionConfig failureConditionConfig;

    private List<HtmlDetailedIssueData> htmlIssues = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(KlocworkResultsAction.class.getName());

    public KlocworkResultsAction(final Run<?, ?> build,
                                 final String remote, final VirtualChannel channel, final Launcher launcher,
                                 final EnvVars envVars,
                                 final TaskListener listener, final KlocworkFailureConditionConfig failureConditionConfig) {
        this.build = build;
        this.launcher = launcher;
        this.envVars = envVars;
        this.failureConditionConfig = failureConditionConfig;
        this.listener = listener;
        this.channel = channel;
        remotePath = remote;

        url = "KlocworkResultsAction";
        text = Messages.KlocworkDashboard_klocwork_results();
        icon = KlocworkConstants.ICON_URL;

        try {
            updateIssues();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    private List<HtmlDetailedIssueData> initHtmlIssues(final List<KlocworkJsonIssue> jsonIssues) {
        return jsonIssues
                .stream()
                .map(HtmlDetailedIssueData::fromJsonIssue)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        final List<KlocworkResultsAction> projectActions = new ArrayList<>();
        projectActions.add(this);

        return projectActions;
    }

    @Override
    public String getIconFileName() {
        return icon;
    }

    @Override
    public String getDisplayName() {
        return text;
    }

    @Override
    public String getUrlName() {
        return url;
    }

    public HtmlReportIssues getResults() {
        final HtmlReportIssues report = new HtmlReportIssues();

        if (htmlIssues == null || htmlIssues.isEmpty()) {
            return report;
        }

        //transform and group issues by state
        final Map<String, List<HtmlDetailedIssueData>> sortedIssues = htmlIssues
                .stream()
                .collect(groupingBy(HtmlDetailedIssueData::getState));

        if (sortedIssues.get(State.NEW.toString()) != null) {
            report.setNewIssues(sortedIssues.get(State.NEW.toString()).size());
            report.setNewIssuesData(sortedIssues.get(State.NEW.toString()));
        }

        if (sortedIssues.get(State.FIXED.text()) != null) {
            report.setFixedIssues(sortedIssues.get(State.FIXED.toString()).size());
            report.setFixedIssuesData(sortedIssues.get(State.FIXED.toString()));
        }

        final int filesCount = htmlIssues
                .stream()
                .collect(groupingBy(HtmlDetailedIssueData::getFile))
                .size();

        report.setNewIssuesFiles(filesCount);

        return report;
    }

    /**
     * Updates issue list by calling 'kwciagent list'
     *
     * @throws IOException*
     */
    private void updateIssues() throws IOException {
        Set<KlocworkJsonIssue> allIssues = new LinkedHashSet<>();

        if (failureConditionConfig == null) {
            return;
        }

        if (failureConditionConfig.getFailureConditionCiConfigs() != null) {
            for (KlocworkFailureConditionCiConfig failureConfig : failureConditionConfig.getFailureConditionCiConfigs()) {

                final FilePath workspace = new FilePath(channel, remotePath);
                final ArgumentListBuilder listCommand = getListCmd(failureConfig.getEnabledSeverites(),
                                                                   failureConfig.getEnabledStatuses(),
                                                                   workspace,
                                                                   "json",
                                                                   failureConfig.getDiffFileList());
                final Map<StreamReferences, ByteArrayOutputStream> response = KlocworkUtil.executeCommandParseOutput(launcher, workspace, envVars, listCommand);

                if (response.get(StreamReferences.ERR_STREAM).size() > 0) {
                    LOGGER.log(Level.WARNING, response.get(StreamReferences.ERR_STREAM).toString());
                }

                final List<KlocworkJsonIssue> jsonIssues = parseIssues(response.get(StreamReferences.OUT_STREAM));
                allIssues.addAll(jsonIssues);
            }
        }

        htmlIssues = initHtmlIssues(Lists.newArrayList(allIssues.iterator()));
    }

    private List<KlocworkJsonIssue> parseIssues(final ByteArrayOutputStream jsonStream) {
        InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(jsonStream.toByteArray()));
        JsonReader jsonReader = new JsonReader(reader);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        List<KlocworkJsonIssue> result = gson.fromJson(jsonReader, new TypeToken<List<KlocworkJsonIssue>>() {
        }.getType());

        return result != null ? result : new ArrayList<>();
    }

    @JavaScriptMethod
    public JSONObject citeIssue(final int issueId, final int status, final String comment, final String ltokenString, final String userName) {
        LOGGER.log(Level.INFO, Messages.KlocworkResultAction_logger_cite_start(issueId));
        final JSONObject result = new JSONObject();
        result.put("result", false);
        try {
            if (remotePath == null) {
                LOGGER.log(Level.WARNING, Messages.KlocworkResultAction_logger_cite_fails(issueId, Messages.KlocworkResultAction_oldbuild_error()));
                result.put("error", Messages.KlocworkResultAction_oldbuild_error());
                return result;
            }
            if (comment.length() > 150) {
                LOGGER.log(Level.WARNING, Messages.KlocworkResultAction_logger_cite_fails(issueId, Messages.KlocworkResultAction_comment_error()));
                result.put("error", Messages.KlocworkResultAction_comment_error());
                return result;
            }
            final Map<StreamReferences, ByteArrayOutputStream> response;
            final FilePath workspace = new FilePath(channel, remotePath);
            final FilePath tmpLtokenFile = workspace.createTextTempFile(LTOKEN, "", ltokenString);
            try {
                envVars.put(KLOCWORK_LTOKEN, tmpLtokenFile.getRemote());
                envVars.put(KLOCWORK_USER_OVERRIDE, userName);
                final ArgumentListBuilder setStatusCommand = getSetStatusCmd(issueId, Status.getValue(status), comment, workspace);
                LOGGER.info("Running: " + setStatusCommand.toString());
                response = KlocworkUtil.executeCommandParseOutput(launcher, workspace, envVars, setStatusCommand);
                if (response.get(StreamReferences.ERR_STREAM).size() > 0) {
                    LOGGER.log(Level.WARNING, Messages.KlocworkResultAction_logger_cite_fails(issueId, response.get(StreamReferences.ERR_STREAM)));
                    result.put("error", response.get(StreamReferences.ERR_STREAM).toString());
                    return result;
                } else {
                    LOGGER.log(Level.INFO, Messages.KlocworkResultAction_logger_cite_success(issueId));
                    result.put("result", true);
                }
            } finally {
                deleteTokenFile(tmpLtokenFile);
            }
        } catch (Exception e) {
            final String errorMessage = getErrorMessage(e);
            LOGGER.log(Level.WARNING, Messages.KlocworkResultAction_logger_cite_fails(issueId, errorMessage));
            result.put("error", errorMessage);
            return result;
        }

        try {
            updateIssues();
        } catch (IOException e) {
            result.put("error", e.getMessage());
        }

        return result;
    }

    private static void deleteTokenFile(final FilePath tokenFile) {
        try {
            tokenFile.delete();
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.WARNING, Messages.KlocworkResultAction_non_deleted_token(tokenFile.getRemote()), e);
        }
    }

    private static String getErrorMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : Messages.KlocworkResultAction_unknown_error();
    }

    @JavaScriptMethod
    public JSONObject doAuth(final String username, final String password) {
        final JSONObject result = new JSONObject();
        result.put("result", false);
        if (remotePath == null || launcher == null) {
            LOGGER.log(Level.WARNING, Messages.KlocworkResultAction_authentication_oldbuild_error());
            result.put("error", Messages.KlocworkResultAction_authentication_oldbuild_error());
            return result;
        }
        final FilePath workspace = new FilePath(channel, remotePath);
        final FilePath userLtokenPath = new FilePath(workspace, username + LTOKEN);
        final String userLtokenLocation = userLtokenPath.getRemote();
        envVars.put(KLOCWORK_LTOKEN, userLtokenLocation);
        try {
            KlocworkAuthenticator.authenticate(launcher, workspace, username, password, envVars.get(KLOCWORK_URL), envVars);
            final String[] ltokenArray = getTokenEntry(launcher, workspace, envVars, userLtokenLocation);
            if (ltokenArray.length != 4) {
                LOGGER.log(Level.WARNING, new StringBuilder()
                        .append("Could not authenticate user ")
                        .append(username)
                        .append(". Token returned by Klocwork Server could not be read or is empty")
                        .toString());
                result.put("error", Messages.KlocworkResultAction_authentication_failed(username));
            } else {
                LOGGER.log(Level.INFO, Messages.KlocworkResultAction_authentication_success(username));
                result.put("data", tokenInfoToJSON(ltokenArray).toString());
                result.put("result", true);
                deleteTokenFile(userLtokenPath);
            }
        } catch (ConsoleErrorException | AbortException e) {
            final String errorMessage = Messages.KlocworkResultAction_authentication_failed_cause(username, getErrorMessage(e));
            LOGGER.log(Level.WARNING, errorMessage, e);
            result.put("error", errorMessage);
        }
        return result;
    }

    private static JSONObject tokenInfoToJSON(final String[] ltokenArray) {
        return new JSONObject()
                .accumulate("kwTokenServer", ltokenArray[0])
                .accumulate("kwTokenPort", ltokenArray[1])
                .accumulate("kwTokenUsername", ltokenArray[2])
                .accumulate("kwTokenValue", ltokenArray[3]);
    }

    /**
     * Reads an ltoken file and returns an array of the first entry in it
     *
     * @param ltokenLocation Location of the ltoken file
     * @return An array of the first entry in the ltoken, or empty array if ltoken is empty or an error occurs
     */
    private static String[] getTokenEntry(final Launcher launcher, final FilePath workspace, final EnvVars envVars, String ltokenLocation) {
        String tool;
        if(launcher.isUnix()){
            tool = "cat";
        } else{
            tool = "type";
        }
        final ArgumentListBuilder outputGrabCommand = new ArgumentListBuilder(tool, ltokenLocation);
        try {
            final Map<StreamReferences, ByteArrayOutputStream> results = KlocworkUtil.executeCommandParseOutput(launcher,
                    workspace,
                    envVars,
                    outputGrabCommand);
            InputStream inputStream = new ByteArrayInputStream(results.get(StreamReferences.OUT_STREAM).toByteArray());
            BufferedReader bufferedReader = null;
            if (launcher.isUnix()) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            }
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.trim().split(";").length == 4){
                    return line.trim().split(";");
                }
            }
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, Messages.KlocworkResultAction_unknown_error(), e);
        }
        return new String[0];
    }

    public ArgumentListBuilder getSetStatusCmd(final int issueId, final Status status, final String comment, final FilePath workspace) {
        final String ciTool = "kwciagent";

        final ArgumentListBuilder command =
                new ArgumentListBuilder(ciTool, "set-status");

        command.add(issueId);
        command.add("-s", status.getName());

        if (!comment.isEmpty()) {
            command.add("-c", comment);
        }

        command.add("-pd", getKwlpDir(workspace, envVars).getRemote());

        return command;
    }

    public ArgumentListBuilder getListCmd(final KlocworkSeverities severities, KlocworkStatuses statuses, final FilePath workspace, String outputFormat, String diffFileList) {
        final String ciTool = "kwciagent";

        final ArgumentListBuilder command =
                new ArgumentListBuilder(ciTool, "list");

        String statusString = statuses.getEnabled()
                                      .keySet()
                                      .stream()
                                      .filter(status -> statuses.getEnabled().getOrDefault(status, false))
                                      .collect(Collectors.joining(","));

        if (!statusString.isEmpty()) {
            command.add("--status", statusString);
        }

        String severityString = severities.getEnabled().keySet()
                                          .stream()
                                          .filter(severity -> severities.getEnabled().getOrDefault(severity, false))
                                          .map(severity -> getSeveritiesString(severity))
                                          .collect(Collectors.joining(","));
        if (!severityString.isEmpty()) {
            command.add("--severity", severityString);
        }

        command.add("-pd", getKwlpDir(workspace, envVars).getRemote());

        if (!outputFormat.isEmpty()) {
            command.add("-F", outputFormat);
        }

        if (!StringUtils.isEmpty(diffFileList)) {
            command.add("@" + diffFileList);
        }

        return command;
    }

    private String getSeveritiesString(final String severityKey) {
        final String FIVE_TO_TEN_SEVERITY = "fiveToTen";

        StringBuilder result = new StringBuilder();

        if (severityKey.equalsIgnoreCase(FIVE_TO_TEN_SEVERITY)) {
            for (int i = 5; i <= 10; i++) {
                result.append(i);
                if (i < 10) {
                    result.append(",");
                }
            }
        } else {
            result.append(Severity.valueOf(severityKey.toUpperCase()).getId());
        }

        return result.toString();
    }

    private FilePath getKwlpDir(final FilePath workspace, final EnvVars envVars) {
        return new FilePath(
                workspace.child(envVars.expand(workspace.getRemote())), ".kwlp");
    }

    /**
     * Used in jelly template to iterate over Statuses
     */
    public JSONObject getKlocworkStatusValues() {
        final JSONObject statusValues = new JSONObject();

        for (final Status status : Status.values()) {
            statusValues.put(status.getName(), status.getId());
        }

        return statusValues;
    }
}

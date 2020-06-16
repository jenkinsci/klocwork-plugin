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
import com.klocwork.kwjenkinsplugin.dto.KlocworkJsonIssue;
import com.klocwork.kwjenkinsplugin.dto.HtmlDetailedIssueData;
import com.klocwork.kwjenkinsplugin.dto.HtmlReportIssues;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil.StreamReferences;
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
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class KlocworkResultsAction implements Action, LastBuildAction {

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

        if(htmlIssues == null || htmlIssues.isEmpty()) {
            return report;
        }

        //transform and group issues by state
        final Map<String, List<HtmlDetailedIssueData>> sortedIssues = htmlIssues
                .stream()
                .collect(groupingBy(HtmlDetailedIssueData::getState));

        if(sortedIssues.get(State.NEW.toString()) != null) {
            report.setNewIssues(sortedIssues.get(State.NEW.toString()).size());
            report.setNewIssuesData(sortedIssues.get(State.NEW.toString()));
        }

        if(sortedIssues.get(State.FIXED.text()) != null) {
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

        if(failureConditionConfig == null) {
            return;
        }

        if(failureConditionConfig.getFailureConditionCiConfigs() != null) {
            for (KlocworkFailureConditionCiConfig failureConfig : failureConditionConfig.getFailureConditionCiConfigs()) {

                final FilePath workspace = new FilePath(channel, remotePath);
                final ArgumentListBuilder listCommand = getListCmd(failureConfig.getEnabledSeverites(), failureConfig.getEnabledStatuses(), workspace, "json");
                final Map<StreamReferences, ByteArrayOutputStream> response = KlocworkUtil.executeCommandParseOutput(launcher, workspace, envVars, listCommand);

                if(response.get(StreamReferences.ERR_STREAM).size() > 0) {
                    throw new IOException(response.get(StreamReferences.ERR_STREAM).toString());
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

        return gson.fromJson(jsonReader, new TypeToken<List<KlocworkJsonIssue>>(){}.getType());
    }

    @JavaScriptMethod
    public JSONObject citeIssue(final int issueId, final int status, final String comment) {
        LOGGER.log(Level.INFO, Messages.KlocworkResultAction_logger_cite_start(issueId));

        final JSONObject result = new JSONObject();
        result.put("result", false);

        try {

            if(remotePath == null) {
                LOGGER.log(Level.WARNING, Messages.KlocworkResultAction_logger_cite_fails(issueId, Messages.KlocworkResultAction_oldbuild_error()));
                result.put("error", Messages.KlocworkResultAction_oldbuild_error());
                return result;
            }

            if(comment.length() > 150) {
                LOGGER.log(Level.WARNING, Messages.KlocworkResultAction_logger_cite_fails(issueId, Messages.KlocworkResultAction_comment_error()));
                result.put("error", Messages.KlocworkResultAction_comment_error());
                return result;
            }

            final Map<StreamReferences, ByteArrayOutputStream> response;
            final FilePath workspace = new FilePath(channel, remotePath);

            final ArgumentListBuilder setStatusCommand = getSetStatusCmd(issueId, Status.getValue(status), comment, workspace);
            LOGGER.info("Running: " + setStatusCommand.toString());
            response = KlocworkUtil.executeCommandParseOutput(launcher, workspace, envVars, setStatusCommand);

            if(response.get(StreamReferences.ERR_STREAM).size() > 0) {
                LOGGER.log(Level.WARNING, Messages.KlocworkResultAction_logger_cite_fails(issueId, response.get(StreamReferences.ERR_STREAM)));
                result.put("error", response.get(StreamReferences.ERR_STREAM).toString());
                return result;

            } else {

                LOGGER.log(Level.INFO, Messages.KlocworkResultAction_logger_cite_success(issueId));
                result.put("result", true);
            }

        } catch (Exception e) {
            final String errorMessage;

            if(e.getMessage() != null) {
                errorMessage = e.getMessage();
            } else {
                errorMessage = Messages.KlocworkResultAction_unknown_error();
            }

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

    public ArgumentListBuilder getSetStatusCmd(final int issueId, final Status status, final String comment, final FilePath workspace) {
        final String ciTool = "kwciagent";

        final ArgumentListBuilder command =
                new ArgumentListBuilder(ciTool, "set-status");

        command.add(issueId);
        command.add("-s", status.getName());

        if(!comment.isEmpty()) {
            command.add("-c", comment);
        }

        command.add("-pd", getKwlpDir(workspace, envVars).getRemote());

        return command;
    }

    public ArgumentListBuilder getListCmd(final KlocworkSeverities severities, KlocworkStatuses statuses, final FilePath workspace, String outputFormat) {
        final String ciTool = "kwciagent";

        final ArgumentListBuilder command =
                new ArgumentListBuilder(ciTool, "list");

        String statusString = statuses.getEnabled()
                .keySet()
                .stream()
                .filter(status -> statuses.getEnabled().getOrDefault(status, false))
                .collect(Collectors.joining(","));

        if(!statusString.isEmpty()) {
            command.add("--status", statusString);
        }

        String severityString = severities.getEnabled().keySet()
                                               .stream()
                                               .filter(severity -> severities.getEnabled().getOrDefault(severity, false))
                                               .map(severity -> getSeveritiesString(severity))
                                               .collect(Collectors.joining(","));
        if(!severityString.isEmpty()) {
            command.add("--severity", severityString);
        }

        command.add("-pd", getKwlpDir(workspace, envVars).getRemote());

        if(!outputFormat.isEmpty()) {
            command.add("-F", outputFormat);
        }

        return command;
    }

    private String getSeveritiesString(final String severityKey) {
        final String FIVE_TO_TEN_SEVERITY = "fiveToTen";

         StringBuilder result = new StringBuilder();

        if(severityKey.equalsIgnoreCase(FIVE_TO_TEN_SEVERITY)) {
            for(int i = 5; i <= 10; i++) {
                result.append(i);
                if(i < 10) {
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

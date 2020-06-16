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

package com.klocwork.kwjenkinsplugin.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.klocwork.kwjenkinsplugin.dto.KlocworkJsonIssue;
import com.klocwork.kwjenkinsplugin.definitions.KlocworkSeverities;
import com.klocwork.kwjenkinsplugin.definitions.KlocworkStatuses;
import jenkins.security.MasterToSlaveCallable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KlocworkJsonReportParserIssueList extends MasterToSlaveCallable<List<KlocworkJsonIssue>, IOException> implements Serializable {

    private final String workspace;
    private final String jsonReportFile;
    private final KlocworkSeverities enabledSeverities;
    private final KlocworkStatuses enabledStatuses;
    private final String FIVE_TO_TEN_SEVERITY = "fiveToTen";

    public KlocworkJsonReportParserIssueList(final String workspace,
                                             final String jsonReportFile,
                                             final KlocworkSeverities enabledSeverities,
                                             final KlocworkStatuses enabledStatuses) {
        this.workspace = workspace;
        this.jsonReportFile = jsonReportFile;
        this.enabledSeverities = enabledSeverities;
        this.enabledStatuses = enabledStatuses;
    }



    @Override
    public List<KlocworkJsonIssue> call() throws IOException {

        List<KlocworkJsonIssue> issues;
        File reportFile;

        //We must handle both relative and absolute paths
        if (Paths.get(jsonReportFile).isAbsolute()) {
            reportFile = new File(jsonReportFile);
        } else {
            reportFile = new File(workspace, jsonReportFile);
        }

        try (InputStream jsonInput = new FileInputStream(reportFile)) {
            InputStreamReader reader = new InputStreamReader(jsonInput);
            JsonReader jsonReader = new JsonReader(reader);
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();

            issues = gson.fromJson(jsonReader, new TypeToken<List<KlocworkJsonIssue>>(){}.getType());
        }

        if(issues == null) {
            return new ArrayList<KlocworkJsonIssue>();
        }

        //filter by statuses and severities and return
        return issues
                .stream()
                .filter(issue -> enabledStatuses
                                         .getEnabled()
                                         .getOrDefault(issue.getCitingStatus().toLowerCase(), false))
                .filter(issue -> isSeverityInEnabled(
                        Integer.parseInt(issue.getSeverityCode()),
                        issue.getSeverity()))
                .collect(Collectors.toList());

    }

    private boolean isSeverityInEnabled(int severityCode, String severityName) {
        if(enabledSeverities.getEnabled().get(FIVE_TO_TEN_SEVERITY) && (severityCode >= 5 && severityCode <= 10)) {
            return true;
        }

        if(severityName == null) {
            return false;
        }

        if(enabledSeverities.getEnabled().getOrDefault(severityName.toLowerCase(), false)) {
            return true;
        }

        return false;
    }
}

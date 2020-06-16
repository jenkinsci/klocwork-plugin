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

package com.klocwork.kwjenkinsplugin.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HtmlReportIssues {
    private Integer newIssues = 0;
    private Integer fixedIssues = 0;
    private Integer newIssuesFiles = 0;
    private List<HtmlDetailedIssueData> newIssuesData = new ArrayList<>();
    private List<HtmlDetailedIssueData> fixedIssuesData = new ArrayList<>();

    public HtmlReportIssues() {
    }

    public Integer getNewIssues() {
        return this.newIssues;
    }

    public Integer getFixedIssues() {
        return this.fixedIssues;
    }

    public Integer getNewIssuesFiles() {
        return this.newIssuesFiles;
    }

    public List<HtmlDetailedIssueData> getNewIssuesData() {
        return this.newIssuesData;
    }

    public List<HtmlDetailedIssueData> getFixedIssuesData() {
        return this.fixedIssuesData;
    }

    public Map<String, List<HtmlDetailedIssueData>> getNewIssuesDataGroupedByFile() {
        return this.newIssuesData == null ? new HashMap() : this.getIssuesSortedByFile(this.newIssuesData);
    }

    public Map<String, List<HtmlDetailedIssueData>> getFixedIssuesDataGroupedByFile() {
        return this.fixedIssuesData == null ? new HashMap() : this.getIssuesSortedByFile(this.fixedIssuesData);
    }

    private Map<String, List<HtmlDetailedIssueData>> getIssuesSortedByFile(List<HtmlDetailedIssueData> issueList) {
        return issueList.stream().collect(Collectors.groupingBy(HtmlDetailedIssueData::getFile));
    }

    public void setNewIssues(final Integer newIssues) {
        this.newIssues = newIssues;
    }

    public void setFixedIssues(final Integer fixedIssues) {
        this.fixedIssues = fixedIssues;
    }

    public void setNewIssuesFiles(final Integer newIssuesFiles) {
        this.newIssuesFiles = newIssuesFiles;
    }

    public void setNewIssuesData(final List<HtmlDetailedIssueData> newIssuesData) {
        this.newIssuesData = newIssuesData;
    }

    public void setFixedIssuesData(final List<HtmlDetailedIssueData> fixedIssuesData) {
        this.fixedIssuesData = fixedIssuesData;
    }
}

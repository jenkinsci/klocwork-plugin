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
import java.util.List;

public class HtmlDetailedIssueData {
    Integer id;
    String code;
    String file;
    String message;
    Boolean system;
    String status;
    String severity;
    String supportLevel;
    Integer severityCode;
    Integer supportLevelCode;
    String state;
    String method;
    Integer line;
    List<HtmlIssueDataTrace> trace = new ArrayList<>();

    public HtmlDetailedIssueData() {
    }

    public static HtmlDetailedIssueData fromJsonIssue(KlocworkJsonIssue jsonIssue) {
        HtmlDetailedIssueData result = new HtmlDetailedIssueData();

        result.id = jsonIssue.getId() == null ? null : Integer.parseInt(jsonIssue.getId());
        result.code = jsonIssue.getCode();
        result.file = jsonIssue.getFile();
        result.message = jsonIssue.getMessage();
        result.system = jsonIssue.getSystem();
        result.status = jsonIssue.getCitingStatus();
        result.severityCode = jsonIssue.getSeverityCode() != null ? Integer.parseInt(jsonIssue.getSeverityCode()) : 0;
        result.severity = jsonIssue.getSeverity();
        result.supportLevelCode = jsonIssue.getSupportLevelCode() != null ? Integer.parseInt(jsonIssue.getSupportLevelCode()) : 0;
        result.supportLevel = jsonIssue.getSupportLevel();
        result.state = jsonIssue.getState();
        result.method = jsonIssue.getParent();
        result.line = jsonIssue.getLine() == null ? null : Integer.parseInt(jsonIssue.getLine());

        ArrayList<HtmlIssueDataTrace> traceResult = new ArrayList<>();

        if(jsonIssue.getTrace() != null) {
            for(JsonIssueTraceBlock jsonTraceBlock : jsonIssue.getTrace().getTraceBlocks()) {
                HtmlIssueDataTrace resultTraceBlock = new HtmlIssueDataTrace();
                resultTraceBlock.file = jsonTraceBlock.getFile();
                resultTraceBlock.entity = jsonTraceBlock.getMethod();

                for(JsonIssueTraceLine jsonTraceLine: jsonTraceBlock.lines) {
                    HtmlIssueDataTraceLine resultTraceLine = new HtmlIssueDataTraceLine();
                    resultTraceLine.line = jsonTraceLine.getLine();
                    resultTraceLine.text = jsonTraceLine.getText();

                    resultTraceBlock.lines.add(resultTraceLine);
                }

                traceResult.add(resultTraceBlock);
            }

            result.trace = traceResult;
        }

        return result;
    }

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getFile() {
        return file;
    }

    public String getMessage() {
        return message;
    }

    public Boolean isSystem() {
        return this.system;
    }

    public String getStatus() {
        return this.status;
    }

    public String getSeverity() {
        return this.severity;
    }

    public String getSupportLevel() {
        return this.supportLevel;
    }

    public Integer getSeverityCode() {
        return this.severityCode;
    }

    public Integer getSupportLevelCode() {
        return this.supportLevelCode;
    }

    public String getState() {
        return this.state;
    }

    public String getMethod() {
        return this.method;
    }

    public Integer getLine() {
        return this.line;
    }

    public List<HtmlIssueDataTrace> getTrace() {
        return this.trace;
    }

    public String toString() {
        return "DetailedIssueData{system=" + this.system + ", status='" + this.status + '\'' + ", severity='" + this.severity + '\'' + ", severityCode=" + this.severityCode + ", supportLevel='" + this.supportLevel + '\'' + ", supportLevelCode=" + this.supportLevelCode + ", state='" + this.state + '\'' + ", code='" + this.code + '\'' + ", method='" + this.method + '\'' + ", line=" + this.line + ", trace=" + this.trace + '}';
    }
}

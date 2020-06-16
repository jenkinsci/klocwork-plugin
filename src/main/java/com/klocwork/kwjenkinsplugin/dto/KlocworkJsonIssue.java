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

import com.klocwork.kwjenkinsplugin.definitions.KlocworkIssue;
import java.util.Objects;

public class KlocworkJsonIssue extends KlocworkIssue {

    private Boolean isSystem;
    private String fileId;
    private Integer column;
    private String parent;
    private Long anchor;
    private JsonIssueTrace trace;
    private String prefix;
    private String postfix;
    private Boolean alive;
    private String state;
    private String citingStatus;
    private Boolean statusChanged;
    private Integer creationTime;

    private Integer groupingCode;

    private Integer starScore;

    private String supportLevel;
    private String supportLevelCode;


    public KlocworkJsonIssue() {
    }

    public Boolean getSystem() {
        return isSystem;
    }

    public void setSystem(final Boolean system) {
        isSystem = system;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(final String fileId) {
        this.fileId = fileId;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(final Integer column) {
        this.column = column;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public Long getAnchor() {
        return anchor;
    }

    public void setAnchor(final Long anchor) {
        this.anchor = anchor;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String getPostfix() {
        return postfix;
    }

    public void setPostfix(final String postfix) {
        this.postfix = postfix;
    }

    public Boolean getAlive() {
        return alive;
    }

    public void setAlive(final Boolean alive) {
        this.alive = alive;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getCitingStatus() {
        return citingStatus;
    }

    public void setCitingStatus(final String citingStatus) {
        this.citingStatus = citingStatus;
    }

    public Boolean getStatusChanged() {
        return statusChanged;
    }

    public void setStatusChanged(final Boolean statusChanged) {
        this.statusChanged = statusChanged;
    }

    public Integer getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(final Integer creationTime) {
        this.creationTime = creationTime;
    }

    public Integer getGroupingCode() {
        return groupingCode;
    }

    public void setGroupingCode(final Integer groupingCode) {
        this.groupingCode = groupingCode;
    }

    public Integer getStarScore() {
        return starScore;
    }

    public void setStarScore(final Integer starScore) {
        this.starScore = starScore;
    }

    public JsonIssueTrace getTrace() {
        return trace;
    }

    public void setTrace(final JsonIssueTrace trace) {
        this.trace = trace;
    }

    public String getSupportLevel() {
        return supportLevel;
    }

    public void setSupportLevel(final String supportLevel) {
        this.supportLevel = supportLevel;
    }

    public String getSupportLevelCode() {
        return supportLevelCode;
    }

    public void setSupportLevelCode(final String supportLevelCode) {
        this.supportLevelCode = supportLevelCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KlocworkIssue)) {
            return false;
        }
        final KlocworkIssue issue = (KlocworkIssue) o;

        return Objects.equals(getId(), issue.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

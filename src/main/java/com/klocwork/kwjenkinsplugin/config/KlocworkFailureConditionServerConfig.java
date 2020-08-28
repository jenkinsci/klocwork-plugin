
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

package com.klocwork.kwjenkinsplugin.config;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Result;


public class KlocworkFailureConditionServerConfig extends AbstractDescribableImpl<KlocworkFailureConditionServerConfig> {

    private final String jobResult;
    private final String stageResult;
    private final String query;
    private final String threshold;
    private final String conditionName;
    private final boolean stopBuild;

    public boolean isEnableHTMLReporting() {
        return enableHTMLReporting;
    }

    private final boolean enableHTMLReporting;


    @DataBoundConstructor
    public KlocworkFailureConditionServerConfig(String jobResult, String stageResult, String query,
                                                String threshold, String conditionName, boolean stopBuild, boolean enableHTMLReporting) {

        this.stageResult = stageResult;
        this.jobResult = jobResult;
        this.query = query;
        this.threshold = threshold;
        this.conditionName = conditionName;
        this.stopBuild = stopBuild;
        this.enableHTMLReporting = enableHTMLReporting;
    }

    public String toString(){
        return new StringBuilder().append("[ jobResult:").append(this.jobResult)
                                  .append(", stageResult:").append(this.stageResult)
                                  .append(", query:").append(this.query)
                                  .append(", threshold:").append(this.threshold)
                                  .append(", conditionName:").append(this.conditionName)
                                  .append(", stopBuild:").append(this.stopBuild)
                                  .append(", enableHTMLReporting:").append(this.enableHTMLReporting)
                                  .append(" ]").toString();
    }

    public String getJobResult() {
        return jobResult;
    }

    public String getStageResult() {
        return this.stageResult;
    }

    public String getQuery() {
        return query;
    }

    public String getThreshold() {
        return threshold;
    }

    public String getConditionName() {
        return conditionName;
    }

    public Result getResultValue() {
        return getResultValue(jobResult);
    }

    private static Result getResultValue(String result) {
        switch (result) {
            case "failure": {
                return Result.FAILURE;
            }
            case "unstable": {
                return Result.UNSTABLE;
            }
            case "pass": {
                return Result.SUCCESS;
            }
            default: {
                // this should never happen
                return Result.FAILURE;
            }
        }
    }

    public Result getStageResultValue() {
        if (this.stageResult != null)
        {
            return getResultValue(this.stageResult);
        }
        else
        {
            return getResultValue(this.jobResult);
        }
    }

    public boolean getStopBuild() { return stopBuild; }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkFailureConditionServerConfig> {
        @Override
        public String getDisplayName() { return null; }

    }

}

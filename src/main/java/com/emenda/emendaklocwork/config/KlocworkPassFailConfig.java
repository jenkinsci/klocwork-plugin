
package com.emenda.emendaklocwork.config;

import org.apache.commons.lang3.StringUtils;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.util.FormValidation;

import javax.servlet.ServletException;

import java.io.IOException;
import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;


public class KlocworkPassFailConfig extends AbstractDescribableImpl<KlocworkPassFailConfig> {

    private final String jobResult;
    private final String query;
    private final String threshold;
    private final String conditionName;


    @DataBoundConstructor
    public KlocworkPassFailConfig(String jobResult, String query,
                           String threshold, String conditionName) {

        this.jobResult = jobResult;
        this.query = query;
        this.threshold = threshold;
        this.conditionName = conditionName;
    }

    public String getJobResult() {
        return jobResult;
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
        switch (jobResult) {
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

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkPassFailConfig> {
        public String getDisplayName() { return null; }

    }

}

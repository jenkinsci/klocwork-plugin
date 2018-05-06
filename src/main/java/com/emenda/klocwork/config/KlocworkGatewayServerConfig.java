
package com.emenda.klocwork.config;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Result;

public class KlocworkGatewayServerConfig extends AbstractDescribableImpl<KlocworkGatewayServerConfig> {

    private final String jobResult;
    private final String query;
    private final String threshold;
    private final String conditionName;


    @DataBoundConstructor
    public KlocworkGatewayServerConfig(String jobResult, String query,
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
    public static class DescriptorImpl extends Descriptor<KlocworkGatewayServerConfig> {
        public String getDisplayName() { return null; }

    }

}

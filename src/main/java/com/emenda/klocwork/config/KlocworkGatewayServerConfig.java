
package com.emenda.klocwork.config;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Result;
import org.kohsuke.stapler.DataBoundConstructor;


public class KlocworkGatewayServerConfig extends AbstractDescribableImpl<KlocworkGatewayServerConfig> {

    private final String jobResult;
    private final String query;
    private final String threshold;
    private final String conditionName;
    private final boolean stopBuild;

    public boolean isEnableHTMLReporting() {
        return enableHTMLReporting;
    }

    private final boolean enableHTMLReporting;


    @DataBoundConstructor
    public KlocworkGatewayServerConfig(String jobResult, String query,
                                       String threshold, String conditionName, boolean stopBuild, boolean enableHTMLReporting) {

        this.jobResult = jobResult;
        this.query = query;
        this.threshold = threshold;
        this.conditionName = conditionName;
        this.stopBuild = stopBuild;
        this.enableHTMLReporting = enableHTMLReporting;
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

    public boolean getStopBuild() { return stopBuild; }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkGatewayServerConfig> {
        public String getDisplayName() { return null; }

    }

}

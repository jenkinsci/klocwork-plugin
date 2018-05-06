
package com.emenda.klocwork.config;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class KlocworkReportConfig extends AbstractDescribableImpl<KlocworkReportConfig> {

    private final String query;
    private final String chartHeight;
    private final String chartWidth;

    @DataBoundConstructor
    public KlocworkReportConfig(String query, String chartHeight, String chartWidth) {
        this.query = query;
        this.chartHeight = chartHeight;
        this.chartWidth = chartWidth;
    }

    public String getQuery() {
        return query;
    }

    public String getChartHeight() {
        return chartHeight;
    }

    public String getChartWidth() {
        return chartWidth;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkReportConfig> {
        public String getDisplayName() { return null; }

    }

}


package com.emenda.klocwork.config;

import com.emenda.klocwork.KlocworkConstants;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class KlocworkReportConfig extends AbstractDescribableImpl<KlocworkReportConfig> {

    private static final String DEFAULT_CHART_QUERY = "";
    private static final String DEFAULT_CHART_HEIGHT = "450px";
    private static final String DEFAULT_CHART_WIDTH = "800px";

    private boolean displayChart;
    private String query;
    private String chartHeight;
    private String chartWidth;

    @DataBoundConstructor
    public KlocworkReportConfig(boolean displayChart) {
        this.displayChart = displayChart;
        this.query = DEFAULT_CHART_QUERY;
        this.chartHeight = DEFAULT_CHART_HEIGHT;
        this.chartWidth = DEFAULT_CHART_WIDTH;
    }

    public boolean isDisplayChart() {
        return displayChart;
    }

    @DataBoundSetter
    public void setDisplayChart(boolean displayChart) {
        this.displayChart = displayChart;
    }

    public String getQuery() {
        return query;
    }

    @DataBoundSetter
    public void setQuery(String query) {
        this.query = query;
    }

    public String getChartHeight() {
        return chartHeight;
    }

    @DataBoundSetter
    public void setChartHeight(String chartHeight) {
        this.chartHeight = chartHeight;
    }

    public String getChartWidth() {
        return chartWidth;
    }

    @DataBoundSetter
    public void setChartWidth(String chartWidth) {
        this.chartWidth = chartWidth;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkReportConfig> {
        public String getDisplayName() { return null; }
        public static String getDefaultChartQuery() {
            return DEFAULT_CHART_QUERY;
        }
    
        public static String getDefaultChartHeight() {
            return DEFAULT_CHART_HEIGHT;
        }
    
        public static String getDefaultChartWidth() {
            return DEFAULT_CHART_WIDTH;
        }
    }

}

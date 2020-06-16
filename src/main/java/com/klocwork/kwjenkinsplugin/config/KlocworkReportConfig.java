
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

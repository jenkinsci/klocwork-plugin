
package com.emenda.klocwork.config;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class KlocworkGatewayDesktopConfig extends AbstractDescribableImpl<KlocworkGatewayDesktopConfig> {

    private final String threshold;
    private final String reportFile;

    @DataBoundConstructor
    public KlocworkGatewayDesktopConfig(String threshold, String reportFile) {
        this.threshold = threshold;
        this.reportFile = reportFile;
    }
    public String getThreshold() {
        return threshold;
    }

    public String getReportFile() {
        return reportFile;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkGatewayDesktopConfig> {
        public String getDisplayName() { return null; }

    }

}

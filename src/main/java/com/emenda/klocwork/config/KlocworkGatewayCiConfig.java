
package com.emenda.klocwork.config;

import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Items;
import hudson.model.Run;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;


public class KlocworkGatewayCiConfig extends AbstractDescribableImpl<KlocworkGatewayCiConfig> {

    private final String threshold;
    private final String reportFile;

    @DataBoundConstructor
    public KlocworkGatewayCiConfig(String threshold, String reportFile) {
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
    public static class DescriptorImpl extends Descriptor<KlocworkGatewayCiConfig> {
        public String getDisplayName() { return null; }

        @Initializer(before = InitMilestone.PLUGINS_STARTED)
        public static void addAliases() {
            Items.XSTREAM2.addCompatibilityAlias("com.emenda.klocwork.config.KlocworkGatewayDesktopConfig", KlocworkGatewayCiConfig.class);
            Run.XSTREAM2.addCompatibilityAlias("com.emenda.klocwork.config.KlocworkGatewayDesktopConfig", KlocworkGatewayCiConfig.class);
        }

    }

}

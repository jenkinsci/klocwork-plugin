
package com.emenda.klocwork.config;

import com.emenda.klocwork.definitions.KlocworkSeverities;
import com.emenda.klocwork.definitions.KlocworkStatuses;
import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Items;
import hudson.model.Run;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;


public class KlocworkGatewayCiConfig extends AbstractDescribableImpl<KlocworkGatewayCiConfig> {

    private String name;
    private String threshold;
    private String reportFile;
    private boolean stopBuild;
    private KlocworkSeverities enabledSeverites;
    private KlocworkStatuses enabledStatuses;
    private boolean enableHTMLReporting;

    protected Object readResolve() {
        if(this.name == null){
            this.name = "Default";
        }
        if(this.enabledSeverites == null){
            this.enabledSeverites = new KlocworkSeverities();
        }
        if(this.enabledStatuses == null){
            this.enabledStatuses = new KlocworkStatuses();
        }
        return this;
    }

    @DataBoundConstructor
    public KlocworkGatewayCiConfig(String threshold,
                                   String reportFile) {
        this.threshold = threshold;
        this.reportFile = reportFile;
    }

    @Deprecated
    public KlocworkGatewayCiConfig(String name,
                                   String threshold,
                                   String reportFile,
                                   boolean stopBuild,
                                   KlocworkSeverities enabledSeverites,
                                   KlocworkStatuses enabledStatuses,
                                   boolean enableHTMLReporting) {
        this.name = name;
        this.threshold = threshold;
        this.reportFile = reportFile;
        this.stopBuild = stopBuild;
        this.enabledSeverites = enabledSeverites;
        this.enabledStatuses = enabledStatuses;
        this.enableHTMLReporting = enableHTMLReporting;
    }

    @DataBoundSetter
    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    @DataBoundSetter
    public void setReportFile(String reportFile) {
        this.reportFile = reportFile;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    @DataBoundSetter
    public void setStopBuild(boolean stopBuild) {
        this.stopBuild = stopBuild;
    }

    @DataBoundSetter
    public void setEnabledSeverites(KlocworkSeverities enabledSeverites) {
        this.enabledSeverites = enabledSeverites;
    }

    @DataBoundSetter
    public void setEnabledStatuses(KlocworkStatuses enabledStatuses) {
        this.enabledStatuses = enabledStatuses;
    }

    @DataBoundSetter
    public void setEnableHTMLReporting(boolean enableHTMLReporting) {
        this.enableHTMLReporting = enableHTMLReporting;
    }

    public String getName() {
        return name;
    }

    public boolean isStopBuild() {
        return stopBuild;
    }

    public String getThreshold() {
        return threshold;
    }

    public String getReportFile() {
        return reportFile;
    }

    public boolean getStopBuild() {
        return stopBuild;
    }

    public KlocworkSeverities getEnabledSeverites() {
        return enabledSeverites;
    }

    public KlocworkStatuses getEnabledStatuses() {
        return enabledStatuses;
    }

    public boolean isEnableHTMLReporting() {
        return enableHTMLReporting;
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

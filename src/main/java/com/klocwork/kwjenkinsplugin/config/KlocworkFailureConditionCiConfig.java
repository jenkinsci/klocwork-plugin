
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

import com.klocwork.kwjenkinsplugin.definitions.KlocworkSeverities;
import com.klocwork.kwjenkinsplugin.definitions.KlocworkStatuses;
import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Items;
import hudson.model.Run;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;


public class KlocworkFailureConditionCiConfig extends AbstractDescribableImpl<KlocworkFailureConditionCiConfig> {

    private String name;
    private String threshold;
    private String reportFile;
    private boolean stopBuild;
    private KlocworkSeverities enabledSeverites;
    private KlocworkStatuses enabledStatuses;
    private boolean enableHTMLReporting;
    private boolean failUnstable;

    protected Object readResolve() {
        if(this.name == null){
            this.name = Messages.KlocworkFailureConditionCiConfig_default_name();
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
    public KlocworkFailureConditionCiConfig(String threshold,
                                            String reportFile) {
        this.threshold = threshold;
        this.reportFile = reportFile;
        if(this.enabledSeverites == null){
            this.enabledSeverites = new KlocworkSeverities();
        }
        if(this.enabledStatuses == null){
            this.enabledStatuses = new KlocworkStatuses();
        }
    }

    @Deprecated
    public KlocworkFailureConditionCiConfig(String name,
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

    public String toString(){
        return new StringBuilder().append("[ name:").append(this.name)
                                  .append(", threshold:").append(this.threshold)
                                  .append(", reportFile:").append(this.reportFile)
                                  .append(", stopBuild:").append(this.stopBuild)
                                  .append(", enabledSeverities:").append(this.enabledSeverites)
                                  .append(", enabledStatuses:").append(this.enabledStatuses)
                                  .append(", enableHTMLReporting:").append(this.enableHTMLReporting)
                                  .append(" ]").toString();
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
    public void setFailUnstable(boolean failUnstable) {
        this.failUnstable = failUnstable;
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
    
    public boolean getFailUnstable()
    {
    	return failUnstable;
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
    public static class DescriptorImpl extends Descriptor<KlocworkFailureConditionCiConfig> {
        public String getDisplayName() { return null; }

        @Initializer(before = InitMilestone.PLUGINS_STARTED)
        public static void addAliases() {
            Items.XSTREAM2.addCompatibilityAlias("com.klocwork.kwjenkinsplugin.config.KlocworkFailureConditionDesktopConfig", KlocworkFailureConditionCiConfig.class);
            Run.XSTREAM2.addCompatibilityAlias("com.klocwork.kwjenkinsplugin.config.KlocworkFailureConditionDesktopConfig", KlocworkFailureConditionCiConfig.class);
        }

    }

}


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

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import org.kohsuke.stapler.DataBoundSetter;

import java.util.ArrayList;
import java.util.List;

public class KlocworkFailureConditionConfig extends AbstractDescribableImpl<KlocworkFailureConditionConfig> {

    private boolean enableServerFailureCondition;
    private boolean enableCiFailureCondition;
    private List<KlocworkFailureConditionCiConfig> failureConditionCiConfigs;
    private List<KlocworkFailureConditionServerConfig> failureConditionServerConfigs;

    /* These are old member vars maintained for backwards compatibility */
    private transient boolean enableDesktopFailureCondition;
    private transient KlocworkFailureConditionCiConfig failureConditionDesktopConfig;
    private transient KlocworkFailureConditionCiConfig failureConditionCiConfig;

    protected Object readResolve() {
        if (enableDesktopFailureCondition) {
            enableCiFailureCondition = true;
        }
        if (failureConditionDesktopConfig != null) {
            failureConditionCiConfigs.add(failureConditionDesktopConfig);
        }
        if(failureConditionCiConfig != null){
            if(failureConditionCiConfigs != null) {
                failureConditionCiConfigs.add(failureConditionCiConfig);
            }
            else{
                failureConditionCiConfigs = new ArrayList<>();
                failureConditionCiConfigs.add(failureConditionCiConfig);
            }
        }
        return this;
    }

    @DataBoundConstructor
    public KlocworkFailureConditionConfig(boolean enableServerFailureCondition,
                                          List<KlocworkFailureConditionServerConfig> failureConditionServerConfigs,
                                          boolean enableCiFailureCondition) {
        this.enableServerFailureCondition = enableServerFailureCondition;
        this.failureConditionServerConfigs = failureConditionServerConfigs;
        this.enableCiFailureCondition = enableCiFailureCondition;
    }

    @Deprecated
    public KlocworkFailureConditionConfig(boolean enableServerFailureCondition,
                                          List<KlocworkFailureConditionServerConfig> failureConditionServerConfigs,
                                          boolean enableCiFailureCondition,
                                          List<KlocworkFailureConditionCiConfig> failureConditionCiConfigs) {
        this.enableServerFailureCondition = enableServerFailureCondition;
        this.failureConditionServerConfigs = failureConditionServerConfigs;
        this.enableCiFailureCondition = enableCiFailureCondition;
        this.failureConditionCiConfigs = failureConditionCiConfigs;
    }

    @DataBoundSetter
    public void setEnableServerFailureCondition(boolean enableServerFailureCondition) {
        this.enableServerFailureCondition = enableServerFailureCondition;
    }

    @DataBoundSetter
    public void setEnableCiFailureCondition(boolean enableCiFailureCondition) {
        this.enableCiFailureCondition = enableCiFailureCondition;
    }

    @DataBoundSetter
    public void setFailureConditionCiConfigs(List<KlocworkFailureConditionCiConfig> failureConditionCiConfigs) {
        this.failureConditionCiConfigs = failureConditionCiConfigs;
    }

    @DataBoundSetter
    public void setFailureConditionServerConfigs(List<KlocworkFailureConditionServerConfig> failureConditionServerConfigs) {
        this.failureConditionServerConfigs = failureConditionServerConfigs;
    }

    @DataBoundSetter
    public void setEnableDesktopFailureCondition(boolean enableDesktopFailureCondition) {
        this.enableCiFailureCondition = enableDesktopFailureCondition;
    }

    @DataBoundSetter
    public void setFailureConditionDesktopConfig(KlocworkFailureConditionCiConfig failureConditionDesktopConfig) {
        if(this.getFailureConditionCiConfigs() == null){
            this.failureConditionCiConfigs = new ArrayList<>();
            this.failureConditionCiConfigs.add(failureConditionDesktopConfig);
        }
    }

    @DataBoundSetter
    public void setFailureConditionCiConfig(KlocworkFailureConditionCiConfig failureConditionCiConfig) {
        if(this.getFailureConditionCiConfigs() == null){
            this.failureConditionCiConfigs = new ArrayList<>();
            this.failureConditionCiConfigs.add(failureConditionCiConfig);
        }
    }

    public boolean isEnableServerFailureCondition() {
        return getEnableServerFailureCondition();
    }

    public boolean isEnableCiFailureCondition() {
        return getEnableCiFailureCondition();
    }

    public boolean getEnableServerFailureCondition() {
        return enableServerFailureCondition;
    }

    public List<KlocworkFailureConditionServerConfig> getFailureConditionServerConfigs() {
        return failureConditionServerConfigs;
    }

    public boolean getEnableCiFailureCondition() {
        return enableCiFailureCondition;
    }

    public List<KlocworkFailureConditionCiConfig> getFailureConditionCiConfigs() {
        return failureConditionCiConfigs;
    }

    /* Should not be used but is here to ensure the pipeline snippet generator works whilst the member var exists */
    public Boolean getEnableDesktopFailureCondition() {
        return null;
    }

    /* Should not be used but is here to ensure the pipeline snippet generator works whilst the member var exists */
    public Boolean isEnableDesktopFailureCondition() {
        return null;
    }

    /* Should not be used but is here to ensure the pipeline snippet generator works whilst the member var exists */
    public KlocworkFailureConditionCiConfig getFailureConditionCiConfig() {
        return null;
    }

    /* Should not be used but is here to ensure the pipeline snippet generator works whilst the member var exists */
    public KlocworkFailureConditionCiConfig getFailureConditionDesktopConfig() {
        return null;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkFailureConditionConfig> {
        public String getDisplayName() { return null; }

    }

}

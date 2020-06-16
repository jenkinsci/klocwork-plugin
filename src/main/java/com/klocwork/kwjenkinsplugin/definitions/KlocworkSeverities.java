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

package com.klocwork.kwjenkinsplugin.definitions;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class KlocworkSeverities extends AbstractDescribableImpl<KlocworkSeverities> implements Serializable {
    private Map<String, Boolean> enabled;

    public KlocworkSeverities(){
        this.enabled = new HashMap<>();
        this.enabled.put("critical", true);
        this.enabled.put("error", true);
        this.enabled.put("warning", true);
        this.enabled.put("review", true);
        this.enabled.put("fiveToTen", true);
    }

    @DataBoundConstructor
    public KlocworkSeverities(boolean critical, boolean error, boolean warning, boolean review, boolean fiveToTen) {
        this.enabled = new HashMap<>();
        this.enabled.put("critical", critical);
        this.enabled.put("error", error);
        this.enabled.put("warning", warning);
        this.enabled.put("review", review);
        this.enabled.put("fiveToTen", fiveToTen);
    }

    public boolean isCritical() {
        return enabled.get("critical");
    }

    public void setCritical(boolean critical) {
        this.enabled.put("critical", critical);
    }

    public boolean isError() {
        return enabled.get("error");
    }

    public void setError(boolean error) {
        this.enabled.put("error", error);
    }

    public boolean isWarning() {
        return enabled.get("warning");
    }

    public void setWarning(boolean warning) {
        this.enabled.put("warning", warning);
    }

    public boolean isReview() {
        return enabled.get("review");
    }

    public void setReview(boolean review) {
        this.enabled.put("review", review);
    }

    public boolean isFiveToTen() {
        return enabled.get("fiveToTen");
    }

    public void setFiveToTen(boolean fiveToTen) {
        this.enabled.put("fiveToTen", fiveToTen);
    }

    public Map<String, Boolean> getEnabled() {
        return enabled;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkSeverities> {
        public String getDisplayName() { return null; }
    }
}

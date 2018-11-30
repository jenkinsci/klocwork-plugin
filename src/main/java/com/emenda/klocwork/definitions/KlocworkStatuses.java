package com.emenda.klocwork.definitions;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.HashMap;

public class KlocworkStatuses extends AbstractDescribableImpl<KlocworkStatuses> implements Serializable {
    private HashMap<String, Boolean> enabled;

    public KlocworkStatuses(){
        this.enabled = new HashMap<>();
        this.enabled.put("analyze", true);
        this.enabled.put("ignore", false);
        this.enabled.put("not a problem", false);
        this.enabled.put("fix", true);
        this.enabled.put("fix in next release", false);
        this.enabled.put("fix in later release", false);
        this.enabled.put("defer", false);
        this.enabled.put("filter", false);
    }

    @DataBoundConstructor
    public KlocworkStatuses(boolean analyze, boolean ignore, boolean notAProblem, boolean fix, boolean fixInNextRelease, boolean fixInLaterRelease, boolean defer, boolean filter) {
        this.enabled = new HashMap<>();
        this.enabled.put("analyze", analyze);
        this.enabled.put("ignore", ignore);
        this.enabled.put("not a problem", notAProblem);
        this.enabled.put("fix", fix);
        this.enabled.put("fix in next release", fixInNextRelease);
        this.enabled.put("fix in later release", fixInLaterRelease);
        this.enabled.put("defer", defer);
        this.enabled.put("filter", filter);
    }

    public boolean isAnalyze() {
        return enabled.get("analyze");
    }

    public void setAnalyze(boolean analyze) {
        this.enabled.put("analyze", analyze);
    }

    public boolean isIgnore() {
        return enabled.get("ignore");
    }

    public void setIgnore(boolean ignore) {
        this.enabled.put("ignore", ignore);
    }

    public boolean isNotAProblem() {
        return enabled.get("not a problem");
    }

    public void setNotAProblem(boolean notAProblem) {
        this.enabled.put("not a problem", notAProblem);
    }

    public boolean isFix() {
        return enabled.get("fix");
    }

    public void setFix(boolean fix) {
        this.enabled.put("fix", fix);
    }

    public boolean isFixInNextRelease() {
        return enabled.get("fix in next release");
    }

    public void setFixInNextRelease(boolean fixInNextRelease) {
        this.enabled.put("fix in next release", fixInNextRelease);
    }

    public boolean isFixInLaterRelease() {
        return enabled.get("fix in later release");
    }

    public void setFixInLaterRelease(boolean fixInLaterRelease) {
        this.enabled.put("fix in later release", fixInLaterRelease);
    }

    public boolean isDefer() {
        return enabled.get("defer");
    }

    public void setDefer(boolean defer) {
        this.enabled.put("defer", defer);
    }

    public boolean isFilter() {
        return enabled.get("filter");
    }

    public void setFilter(boolean filter) {
        this.enabled.put("filter", filter);
    }

    public HashMap<String, Boolean> getEnabled() {
        return enabled;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkStatuses> {
        public String getDisplayName() { return null; }
    }
}

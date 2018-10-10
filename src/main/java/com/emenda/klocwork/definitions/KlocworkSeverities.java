package com.emenda.klocwork.definitions;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.Map;

public class KlocworkSeverities extends AbstractDescribableImpl<KlocworkSeverities> {
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

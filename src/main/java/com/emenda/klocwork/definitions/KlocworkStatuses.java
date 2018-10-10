package com.emenda.klocwork.definitions;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;

public class KlocworkStatuses extends AbstractDescribableImpl<KlocworkStatuses> {
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

//    public boolean isAnalyze() {
//        return enabled.get("analyze;
//    }
//
//    public void setAnalyze(boolean analyze) {
//        this.analyze = analyze;
//    }
//
//    public boolean isIgnore() {
//        return ignore;
//    }
//
//    public void setIgnore(boolean ignore) {
//        this.ignore = ignore;
//    }
//
//    public boolean isNotAProblem() {
//        return notAProblem;
//    }
//
//    public void setNotAProblem(boolean notAProblem) {
//        this.notAProblem = notAProblem;
//    }
//
//    public boolean isFix() {
//        return fix;
//    }
//
//    public void setFix(boolean fix) {
//        this.fix = fix;
//    }
//
//    public boolean isFixInNextRelease() {
//        return fixInNextRelease;
//    }
//
//    public void setFixInNextRelease(boolean fixInNextRelease) {
//        this.fixInNextRelease = fixInNextRelease;
//    }
//
//    public boolean isFixInLaterRelease() {
//        return fixInLaterRelease;
//    }
//
//    public void setFixInLaterRelease(boolean fixInLaterRelease) {
//        this.fixInLaterRelease = fixInLaterRelease;
//    }
//
//    public boolean isDefer() {
//        return defer;
//    }
//
//    public void setDefer(boolean defer) {
//        this.defer = defer;
//    }
//
//    public boolean isFilter() {
//        return filter;
//    }
//
//    public void setFilter(boolean filter) {
//        this.filter = filter;
//    }


    public HashMap<String, Boolean> getEnabled() {
        return enabled;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkStatuses> {
        public String getDisplayName() { return null; }
    }
}

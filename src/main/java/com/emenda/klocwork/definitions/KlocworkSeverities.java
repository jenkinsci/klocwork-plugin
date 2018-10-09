package com.emenda.klocwork.definitions;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class KlocworkSeverities extends AbstractDescribableImpl<KlocworkSeverities> {
    private boolean critical;
    private boolean error;
    private boolean warning;
    private boolean review;
    private boolean fiveToTen;

    @DataBoundConstructor
    public KlocworkSeverities(boolean critical, boolean error, boolean warning, boolean review, boolean fiveToTen) {
        this.critical = critical;
        this.error = error;
        this.warning = warning;
        this.review = review;
        this.fiveToTen = fiveToTen;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    public boolean isReview() {
        return review;
    }

    public void setReview(boolean review) {
        this.review = review;
    }

    public boolean isFiveToTen() {
        return fiveToTen;
    }

    public void setFiveToTen(boolean fiveToTen) {
        this.fiveToTen = fiveToTen;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkSeverities> {
        public String getDisplayName() { return null; }
    }
}


package com.emenda.klocwork.config;

import org.apache.commons.lang3.StringUtils;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

import javax.servlet.ServletException;

import java.io.IOException;
import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;


public class KlocworkDesktopGateway extends AbstractDescribableImpl<KlocworkDesktopGateway> {

    private final String threshold;
    private final String reportFile;


    @DataBoundConstructor
    public KlocworkDesktopGateway(String reportFile, String threshold) {
        this.reportFile = reportFile;
        this.threshold = threshold;
    }
    public String getThreshold() {
        return threshold;
    }

    public String getReportFile() {
        return reportFile;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkDesktopGateway> {
        public String getDisplayName() { return null; }

    }

}


package com.emenda.klocwork.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class KlocworkServerConfig extends AbstractDescribableImpl<KlocworkServerConfig> {

    private final String name;
    private final String url;
    private final boolean specificLicense;
    private final String licenseHost;
    private final String licensePort;


    @DataBoundConstructor
    public KlocworkServerConfig(String name, String url,
                           boolean specificLicense,
                           String licenseHost, String licensePort) {

        this.name = name;
        this.url = url;
        this.specificLicense = specificLicense;
        this.licenseHost = licenseHost;
        this.licensePort = licensePort;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isSpecificLicense() {
        return specificLicense;
    }

    public String getLicenseHost() {
        return licenseHost;
    }

    public String getLicensePort() {
        return licensePort;
    }
    @Symbol("serverConfigs")
    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkServerConfig> {
        public String getDisplayName() { return null; }

        public FormValidation doCheckUrl(@QueryParameter String value)
            throws IOException, ServletException {

            try {
                URL klocworkUrl = new URL(value);
                if (klocworkUrl.getPort() == -1) {
                    return FormValidation.error("Please specify a port");
                }
                if (!(klocworkUrl.getProtocol().equals("http") || klocworkUrl.getProtocol().equals("https"))) {
                    return FormValidation.error("Protocol must be http or https");
                }
            } catch (MalformedURLException ex) {
                return FormValidation.error("Invalid URL format");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckLicensePort(@QueryParameter String value)
            throws IOException, ServletException {

            if (StringUtils.isNumeric(value)) {
                return FormValidation.ok();
            } else {
                return FormValidation.error("Port must be a number");
            }
        }
    }

}

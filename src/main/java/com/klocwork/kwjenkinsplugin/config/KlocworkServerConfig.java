
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
        public String getDisplayName() {
            return null;
        }

        public FormValidation doCheckUrl(@QueryParameter String value)
                throws IOException, ServletException {

            try {
                URL klocworkUrl = new URL(value);
                if (klocworkUrl.getPort() == -1) {
                    return FormValidation.error(Messages.KlocworkServerConfig_port_needed());
                }
                if (!(klocworkUrl.getProtocol().equals("http") || klocworkUrl.getProtocol().equals("https"))) {
                    return FormValidation.error(Messages.KlocworkServerConfig_http_protocol_needed());
                }
            } catch (MalformedURLException ex) {
                return FormValidation.error(Messages.KlocworkServerConfig_invalid_url_format());
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckLicensePort(@QueryParameter String value)
                throws IOException, ServletException {

            if (StringUtils.isNumeric(value)) {
                return FormValidation.ok();
            } else {
                return FormValidation.error(Messages.KlocworkServerConfig_port_must_be_a_number());
            }
        }
    }

}

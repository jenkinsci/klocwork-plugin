
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

import java.util.List;


public class KlocworkGatewayConfig extends AbstractDescribableImpl<KlocworkGatewayConfig> {

    private final boolean enableServerGateway;
    private final List<KlocworkGatewayServerConfig> passFailConfigs;
    private final boolean enableDesktopGateway;
    private final KlocworkGatewayDesktopConfig desktopGateway;

    @DataBoundConstructor
    public KlocworkGatewayConfig(boolean enableServerGateway,
        List<KlocworkGatewayServerConfig> passFailConfigs,
        boolean enableDesktopGateway, KlocworkGatewayDesktopConfig desktopGateway) {
        this.enableServerGateway = enableServerGateway;
        this.passFailConfigs = passFailConfigs;
        this.enableDesktopGateway = enableDesktopGateway;
        this.desktopGateway = desktopGateway;
    }

    public boolean getEnableServerGateway() {
        return enableServerGateway;
    }

    public List<KlocworkGatewayServerConfig> getPassFailConfigs() {
        return passFailConfigs;
    }

    public boolean getEnableDesktopGateway() {
        return enableDesktopGateway;
    }

    public KlocworkGatewayDesktopConfig getDesktopGateway() {
        return desktopGateway;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkGatewayConfig> {
        public String getDisplayName() { return null; }

    }

}


package com.emenda.klocwork.config;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import java.util.List;

public class KlocworkGatewayConfig extends AbstractDescribableImpl<KlocworkGatewayConfig> {

    private final boolean enableServerGateway;
    private final List<KlocworkGatewayServerConfig> gatewayServerConfigs;
    private final boolean enableDesktopGateway;
    private final KlocworkGatewayDesktopConfig gatewayDesktopConfig;

    @DataBoundConstructor
    public KlocworkGatewayConfig(boolean enableServerGateway,
        List<KlocworkGatewayServerConfig> gatewayServerConfigs,
        boolean enableDesktopGateway, KlocworkGatewayDesktopConfig gatewayDesktopConfig) {
        this.enableServerGateway = enableServerGateway;
        this.gatewayServerConfigs = gatewayServerConfigs;
        this.enableDesktopGateway = enableDesktopGateway;
        this.gatewayDesktopConfig = gatewayDesktopConfig;
    }

    public boolean getEnableServerGateway() {
        return enableServerGateway;
    }

    public List<KlocworkGatewayServerConfig> getGatewayServerConfigs() {
        return gatewayServerConfigs;
    }

    public boolean getEnableDesktopGateway() {
        return enableDesktopGateway;
    }

    public KlocworkGatewayDesktopConfig getGatewayDesktopConfig() {
        return gatewayDesktopConfig;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkGatewayConfig> {
        public String getDisplayName() { return null; }

    }

}

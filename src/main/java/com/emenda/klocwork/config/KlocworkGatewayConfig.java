
package com.emenda.klocwork.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;


public class KlocworkGatewayConfig extends AbstractDescribableImpl<KlocworkGatewayConfig> {

    private final boolean enableServerGateway;
    private final List<KlocworkGatewayServerConfig> gatewayServerConfigs;
    private transient boolean enableDesktopGateway;
    private boolean enableCiGateway;
    private transient KlocworkGatewayCiConfig gatewayDesktopConfig;
    private KlocworkGatewayCiConfig gatewayCiConfig;

    protected Object readResolve() {
        if (gatewayDesktopConfig != null) {
            gatewayCiConfig = gatewayDesktopConfig;
        }
        if (enableDesktopGateway) {
            enableCiGateway = true;
        }
        return this;
    }

    @DataBoundConstructor
    public KlocworkGatewayConfig(boolean enableServerGateway,
                                 List<KlocworkGatewayServerConfig> gatewayServerConfigs,
                                 boolean enableCiGateway, KlocworkGatewayCiConfig gatewayCiConfig) {
        this.enableServerGateway = enableServerGateway;
        this.gatewayServerConfigs = gatewayServerConfigs;
        this.enableCiGateway = enableCiGateway;
        this.gatewayCiConfig = gatewayCiConfig;
    }

    public boolean getEnableServerGateway() {
        return enableServerGateway;
    }

    public List<KlocworkGatewayServerConfig> getGatewayServerConfigs() {
        return gatewayServerConfigs;
    }

    public boolean getEnableCiGateway() {
        return enableCiGateway;
    }

    public KlocworkGatewayCiConfig getGatewayCiConfig() {
        return gatewayCiConfig;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkGatewayConfig> {
        public String getDisplayName() { return null; }

    }

}

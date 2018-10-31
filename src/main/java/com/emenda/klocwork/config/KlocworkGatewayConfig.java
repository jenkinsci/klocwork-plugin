
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
    private List<KlocworkGatewayCiConfig> gatewayCiConfigs;

    public boolean isEnableHTMLReporting() {
        return enableHTMLReporting;
    }

    private final boolean enableHTMLReporting;

    protected Object readResolve() {
        if (gatewayDesktopConfig != null) {
            gatewayCiConfigs.add(gatewayDesktopConfig);
        }
        if (enableDesktopGateway) {
            enableCiGateway = true;
        }
        return this;
    }

    @DataBoundConstructor
    public KlocworkGatewayConfig(boolean enableServerGateway,
                                 List<KlocworkGatewayServerConfig> gatewayServerConfigs,
                                 boolean enableCiGateway, List<KlocworkGatewayCiConfig> gatewayCiConfigs, boolean enableDesktopGateway, KlocworkGatewayCiConfig gatewayDesktopConfig, boolean enableHTMLReporting) {
        this.enableServerGateway = enableServerGateway;
        this.gatewayServerConfigs = gatewayServerConfigs;
        this.enableCiGateway = enableCiGateway;
        this.gatewayCiConfigs = gatewayCiConfigs;
        if(!enableCiGateway && enableDesktopGateway ){
            this.enableCiGateway = true;
        }
        if(gatewayCiConfigs == null && gatewayDesktopConfig != null){
            this.gatewayCiConfigs.add(gatewayDesktopConfig);
        }
        this.enableHTMLReporting = enableHTMLReporting;
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

    public boolean getEnableDesktopGateway() {
        return getEnableCiGateway();
    }

    public List<KlocworkGatewayCiConfig> getGatewayCiConfigs() {
        return gatewayCiConfigs;
    }

    public KlocworkGatewayCiConfig getGatewayDesktopConfig() {
        return getGatewayCiConfigs().get(0);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkGatewayConfig> {
        public String getDisplayName() { return null; }

    }

}

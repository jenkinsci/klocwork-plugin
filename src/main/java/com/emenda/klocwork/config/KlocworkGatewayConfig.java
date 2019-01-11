
package com.emenda.klocwork.config;

import org.kohsuke.stapler.DataBoundConstructor;

import com.emenda.klocwork.definitions.KlocworkSeverities;
import com.emenda.klocwork.definitions.KlocworkStatuses;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.ArrayList;
import java.util.List;

public class KlocworkGatewayConfig extends AbstractDescribableImpl<KlocworkGatewayConfig> {

    private boolean enableServerGateway;
    private boolean enableCiGateway;
    private List<KlocworkGatewayCiConfig> gatewayCiConfigs;
    private List<KlocworkGatewayServerConfig> gatewayServerConfigs;

    /* These are old member vars maintained for backwards compatibility */
    private transient boolean enableDesktopGateway;
    private transient KlocworkGatewayCiConfig gatewayDesktopConfig;
    private transient KlocworkGatewayCiConfig gatewayCiConfig;

    protected Object readResolve() {
        if (enableDesktopGateway) {
            enableCiGateway = true;
        }
        if (gatewayDesktopConfig != null) {
            gatewayCiConfigs.add(gatewayDesktopConfig);
        }
        if(gatewayCiConfig != null){
            if(gatewayCiConfigs != null) {
                gatewayCiConfigs.add(gatewayCiConfig);
            }
            else{
                gatewayCiConfigs = new ArrayList<>();
                gatewayCiConfigs.add(gatewayCiConfig);
            }
        }
        return this;
    }

    @DataBoundConstructor
    public KlocworkGatewayConfig(boolean enableServerGateway,
                                 List<KlocworkGatewayServerConfig> gatewayServerConfigs,
                                 boolean enableCiGateway) {
        this.enableServerGateway = enableServerGateway;
        this.gatewayServerConfigs = gatewayServerConfigs;
        this.enableCiGateway = enableCiGateway;
    }

    @Deprecated
    public KlocworkGatewayConfig(boolean enableServerGateway,
                                 List<KlocworkGatewayServerConfig> gatewayServerConfigs,
                                 boolean enableCiGateway,
                                 List<KlocworkGatewayCiConfig> gatewayCiConfigs) {
        this.enableServerGateway = enableServerGateway;
        this.gatewayServerConfigs = gatewayServerConfigs;
        this.enableCiGateway = enableCiGateway;
        this.gatewayCiConfigs = gatewayCiConfigs;
    }

    @DataBoundSetter
    public void setEnableServerGateway(boolean enableServerGateway) {
        this.enableServerGateway = enableServerGateway;
    }

    @DataBoundSetter
    public void setEnableCiGateway(boolean enableCiGateway) {
        this.enableCiGateway = enableCiGateway;
    }

    @DataBoundSetter
    public void setGatewayCiConfigs(List<KlocworkGatewayCiConfig> gatewayCiConfigs) {
        this.gatewayCiConfigs = gatewayCiConfigs;
    }

    @DataBoundSetter
    public void setGatewayServerConfigs(List<KlocworkGatewayServerConfig> gatewayServerConfigs) {
        this.gatewayServerConfigs = gatewayServerConfigs;
    }

    @DataBoundSetter
    public void setEnableDesktopGateway(boolean enableDesktopGateway) {
        this.enableCiGateway = enableDesktopGateway;
    }

    @DataBoundSetter
    public void setGatewayDesktopConfig(KlocworkGatewayCiConfig gatewayDesktopConfig) {
        if(this.getGatewayCiConfigs() == null){
            this.gatewayCiConfigs = new ArrayList<>();
            this.gatewayCiConfigs.add(gatewayDesktopConfig);
        }
    }

    @DataBoundSetter
    public void setGatewayCiConfig(KlocworkGatewayCiConfig gatewayCiConfig) {
        if(this.getGatewayCiConfigs() == null){
            this.gatewayCiConfigs = new ArrayList<>();
            this.gatewayCiConfigs.add(gatewayCiConfig);
        }
    }

    public boolean isEnableServerGateway() {
        return getEnableServerGateway();
    }

    public boolean isEnableCiGateway() {
        return getEnableCiGateway();
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

    public List<KlocworkGatewayCiConfig> getGatewayCiConfigs() {
        return gatewayCiConfigs;
    }

    /* Should not be used but is here to ensure the pipeline snippet generator works whilst the member var exists */
    public Boolean getEnableDesktopGateway() {
        return null;
    }

    /* Should not be used but is here to ensure the pipeline snippet generator works whilst the member var exists */
    public Boolean isEnableDesktopGateway() {
        return null;
    }

    /* Should not be used but is here to ensure the pipeline snippet generator works whilst the member var exists */
    public KlocworkGatewayCiConfig getGatewayCiConfig() {
        return null;
    }

    /* Should not be used but is here to ensure the pipeline snippet generator works whilst the member var exists */
    public KlocworkGatewayCiConfig getGatewayDesktopConfig() {
        return null;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkGatewayConfig> {
        public String getDisplayName() { return null; }

    }

}

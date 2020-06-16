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

package com.klocwork.kwjenkinsplugin;

import com.klocwork.kwjenkinsplugin.config.KlocworkInstallConfig;
import com.klocwork.kwjenkinsplugin.config.KlocworkServerConfig;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KlocworkBuildWrapper extends SimpleBuildWrapper {

    private final String serverConfig;
    private final String installConfig;
    private final String serverProject;
    private final String ltoken;

    @DataBoundConstructor
    public KlocworkBuildWrapper(String serverConfig, String installConfig,
                    String serverProject, String ltoken) {
        this.serverConfig = serverConfig;
        this.installConfig = installConfig;
        this.serverProject = serverProject;
        this.ltoken = ltoken;
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {

            final KlocworkLogger logger = new KlocworkLogger("BuildWrapper", listener.getLogger());
            logger.logMessage(Messages.KlocworkBuildWrapper_setup_environment_variables());
            final KlocworkServerConfig server = getDescriptor().getServerConfig(serverConfig);
            final KlocworkInstallConfig install = getDescriptor().getInstallConfig(installConfig);


            if (server != null) {
                if (StringUtils.isEmpty(server.getUrl())) {
                    logger.logMessage("WARNING: Server URL for configuration \"" +
                        server.getName() + "\" is empty");
                } else {
                    logger.logMessage("Adding the Klocwork Server URL " + server.getUrl());
                    context.env(KlocworkConstants.KLOCWORK_URL, server.getUrl());
                }
                // if specific license details, else use the global ones
                if (server.isSpecificLicense()) {
                    logger.logMessage("Using specific License for given server " +
                        server.getLicensePort() + "@" + server.getLicenseHost());
                    context.env(KlocworkConstants.KLOCWORK_LICENSE_HOST,
                                server.getLicenseHost());
                    context.env(KlocworkConstants.KLOCWORK_LICENSE_PORT,
                                server.getLicensePort());
                } else {
                    logger.logMessage("Using Global License Settings " +
                        getDescriptor().getGlobalLicensePort() + "@" +
                        getDescriptor().getGlobalLicenseHost());
                    context.env(KlocworkConstants.KLOCWORK_LICENSE_HOST,
                                    getDescriptor().getGlobalLicenseHost());
                    context.env(KlocworkConstants.KLOCWORK_LICENSE_PORT,
                                    getDescriptor().getGlobalLicensePort());
                }
            } else {
                logger.logMessage("WARNING: No Klocwork server selected. " +
                    "Klocwork cannot perform server builds or synchronisations " +
                    "without a server.");
                logger.logMessage("Using Global License Settings " +
                    getDescriptor().getGlobalLicensePort() + "@" +
                    getDescriptor().getGlobalLicenseHost());
                context.env(KlocworkConstants.KLOCWORK_LICENSE_HOST,
                                getDescriptor().getGlobalLicenseHost());
                context.env(KlocworkConstants.KLOCWORK_LICENSE_PORT,
                                getDescriptor().getGlobalLicensePort());
            }
            if (StringUtils.isEmpty(serverProject)) {
                logger.logMessage("WARNING: No Klocwork project provided. " +
                    "Klocwork cannot perform server builds or synchronisations " +
                    "without a project.");
            } else {
                context.env(KlocworkConstants.KLOCWORK_PROJECT, serverProject);
            }

            if (install != null) {
                logger.logMessage("Adding Klocwork paths. Using install \""
                + install.getName() + "\"");
                String separator = (launcher.isUnix()) ? ":" : ";";
                String paths = install.getPaths()+separator;
                String path = initialEnvironment.get("PATH");
                context.env("PATH", paths+path);
            }

            if (StringUtils.isEmpty(ltoken)) {
                logger.logMessage("No ltoken file specified. KLOCWORK_LTOKEN will not be set.");
            } else {
                logger.logMessage("Detected ltoken file. Setting KLOCWORK_LTOKEN to \"" +
                    ltoken + "\"");
                // expand ltoken as this is referenced directly by the Klocwork tools.
                // Other environment variables the plugin expands and passes to the Klocwork
                // command line tools
                context.env(KlocworkConstants.KLOCWORK_LTOKEN, initialEnvironment.expand(ltoken));
            }
    }

    public String getServerConfig() { return serverConfig; }
    public String getInstallConfig() { return installConfig; }
    public String getServerProject() { return serverProject; }
    public String getLtoken() { return ltoken; }

    public final static String getNoneValue() { return Messages.KlocworkBuildWrapper_none_value(); }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Symbol("klocworkWrapper")
    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

         private String globalLicenseHost;
         private String globalLicensePort;
         private List<KlocworkServerConfig> serverConfigs = new ArrayList<KlocworkServerConfig>();
         private List<KlocworkInstallConfig> installConfigs = new ArrayList<KlocworkInstallConfig>();

        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        public String getDisplayName() {
            return KlocworkConstants.KLOCWORK_BUILD_WRAPPER_DISPLAY_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            serverConfigs.clear();
            serverConfigs.addAll(req.bindJSONToList(KlocworkServerConfig.class,
                    formData.get("serverConfigs")));
            installConfigs.clear();
            installConfigs.addAll(req.bindJSONToList(KlocworkInstallConfig.class, 
                    formData.get("installConfigs")));
            req.bindJSON(this, formData);
            save();
            return super.configure(req,formData);
        }

        public String getGlobalLicenseHost() { return globalLicenseHost; }

        @DataBoundSetter
        public void setGlobalLicenseHost(String globalLicenseHost) {
            this.globalLicenseHost = globalLicenseHost;
        }

        public String getGlobalLicensePort() { return globalLicensePort; }

        @DataBoundSetter
        public void setGlobalLicensePort(String globalLicensePort) {
            this.globalLicensePort = globalLicensePort;
        }

        public KlocworkServerConfig[] getServerConfigs() {
            return serverConfigs.toArray(new KlocworkServerConfig[0]);
        }

        @DataBoundSetter
        public void setServerConfigs(ArrayList<KlocworkServerConfig> serverConfigs) {
            this.serverConfigs = serverConfigs;
        }

        public KlocworkInstallConfig[] getInstallConfigs() {
            return installConfigs.toArray(new KlocworkInstallConfig[0]);
        }

        @DataBoundSetter
        public void setInstallConfigs(ArrayList<KlocworkInstallConfig> installConfigs) {
            this.installConfigs = installConfigs;
        }

        public KlocworkServerConfig getServerConfig(String name) {
            for (KlocworkServerConfig config : serverConfigs) {
                if (config.getName().equals(name))
                    return config;
            }
            return null;
        }

        public KlocworkInstallConfig getInstallConfig(String name) {
            for (KlocworkInstallConfig config : installConfigs) {
                if (config.getName().equals(name))
                    return config;
            }
            return null;
        }

        public FormValidation doCheckGlobalLicensePort(@QueryParameter String value)
            throws IOException, ServletException {

            if (StringUtils.isNumeric(value)) {
                return FormValidation.ok();
            } else {
                return FormValidation.error(Messages.KlocworkBuildWrapper_port_must_be_a_number());
            }
        }

        public ListBoxModel doFillServerConfigItems() {
            ListBoxModel items = new ListBoxModel();
            items.add(getNoneValue());
            for (KlocworkServerConfig config : serverConfigs) {
                items.add(config.getName());
            }
            return items;
        }

        public FormValidation doCheckServerConfig(@QueryParameter String value)
            throws IOException, ServletException {

            if (value.equals(getNoneValue())) {
                return FormValidation.warning(Messages.KlocworkBuildWrapper_server_configuration_required());
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckServerProject(@QueryParameter String value)
            throws IOException, ServletException {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.warning(Messages.KlocworkBuildWrapper_server_project_required());
            } else {
                return FormValidation.ok();
            }
        }

        public ListBoxModel doFillInstallConfigItems() {
            ListBoxModel items = new ListBoxModel();
            items.add(getNoneValue());
            for (KlocworkInstallConfig config : installConfigs) {
                items.add(config.getName());
            }
            return items;
        }
    }
}

package com.emenda.klocwork;

import com.emenda.klocwork.config.KlocworkInstallConfig;
import com.emenda.klocwork.config.KlocworkServerConfig;

import org.apache.commons.lang3.StringUtils;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.DecoratedLauncher;
import hudson.Proc;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Item;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.Run.RunnerAbortedException;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.Builder;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.CopyOnWriteList;

import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public class KlocworkBuildWrapper extends BuildWrapper {

    private final String serverConfig;
    private final String installConfig;
    private final String serverProject;
    private final String buildSpec;

    @DataBoundConstructor
    public KlocworkBuildWrapper(String serverConfig, String installConfig,
                    String serverProject, String buildSpec) {
        this.serverConfig = serverConfig;
        this.installConfig = installConfig;
        this.serverProject = serverProject;
        this.buildSpec = buildSpec;
    }

    @Override
    public Launcher decorateLauncher(AbstractBuild build, final Launcher launcher,
                             BuildListener listener) throws IOException,
                             RunnerAbortedException {
        final KlocworkLogger logger = new KlocworkLogger("BuildWrapper", listener.getLogger());
        logger.logMessage("Setting up PATH env var for Klocwork jobs...");
        final KlocworkInstallConfig install = getDescriptor().getInstallConfig(installConfig);

        final Node node =  Computer.currentComputer().getNode();
        if (node == null) {
            throw new AbortException("Cannot add variables to deleted node");
        }

        return new DecoratedLauncher(launcher) {
            @Override
            public Proc launch(ProcStarter starter) throws IOException {
                EnvVars vars;
                // taken from CustomToolsPlugin
                try { // Dirty hack, which allows to avoid NPEs in Launcher::envs()
                    vars = toEnvVars(starter.envs());
                } catch (NullPointerException npe) {
                    vars = new EnvVars();
                } catch (InterruptedException x) {
                    throw new IOException(x);
                }

                if (install != null) {
                    logger.logMessage("Adding Klocwork paths. Using install \""
                    + install.getName() + "\"");
                    String paths = vars.get("PATH");
                    String separator = (launcher.isUnix()) ? ":" : ";";
                    paths += separator + install.getPaths();
                    vars.remove("PATH");
                    vars.put("PATH+", paths);
                }

                return getInner().launch(starter.envs(vars));
            }

            private EnvVars toEnvVars(String[] envs) throws IOException, InterruptedException {
                Computer computer = node.toComputer();
                EnvVars vars = computer != null ? computer.getEnvironment() : new EnvVars();
                for (String line : envs) {
                    vars.addLine(line);
                }
                return vars;
            }
        };
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher,
            BuildListener listener) throws IOException, InterruptedException {

            final KlocworkLogger logger = new KlocworkLogger("BuildWrapper", listener.getLogger());
            logger.logMessage("Setting up environment variables for Klocwork jobs...");
            final KlocworkServerConfig server = getDescriptor().getServerConfig(serverConfig);
            return new Environment() {
                @Override
                public void buildEnvVars(Map<String, String> env) {

                    if (server != null) {
                        if (StringUtils.isEmpty(server.getUrl())) {
                            logger.logMessage("WARNING: Server URL for configuration \"" +
                                server.getName() + "\" is empty");
                        } else {
                            logger.logMessage("Adding the Klocwork Server URL " + server.getUrl());
                            env.put(KlocworkConstants.KLOCWORK_URL, server.getUrl());
                        }
                        // if specific license details, else use the global ones
                        if (server.isSpecificLicense()) {
                            logger.logMessage("Using specific License for given server " +
                                server.getLicensePort() + "@" + server.getLicenseHost());
                            env.put(KlocworkConstants.KLOCWORK_LICENSE_HOST,
                                        server.getLicenseHost());
                            env.put(KlocworkConstants.KLOCWORK_LICENSE_PORT,
                                        server.getLicensePort());
                        } else {
                            logger.logMessage("Using Global License Settings " +
                                getDescriptor().getGlobalLicensePort() + "@" +
                                getDescriptor().getGlobalLicenseHost());
                            env.put(KlocworkConstants.KLOCWORK_LICENSE_HOST,
                                            getDescriptor().getGlobalLicenseHost());
                            env.put(KlocworkConstants.KLOCWORK_LICENSE_PORT,
                                            getDescriptor().getGlobalLicensePort());
                        }
                    } else {
                        logger.logMessage("WARNING: No Klocwork server selected. " +
                            "Klocwork cannot perform server builds or synchronisations " +
                            "without a server.");
                        logger.logMessage("Using Global License Settings " +
                            getDescriptor().getGlobalLicensePort() + "@" +
                            getDescriptor().getGlobalLicenseHost());
                        env.put(KlocworkConstants.KLOCWORK_LICENSE_HOST,
                                        getDescriptor().getGlobalLicenseHost());
                        env.put(KlocworkConstants.KLOCWORK_LICENSE_PORT,
                                        getDescriptor().getGlobalLicensePort());
                    }
                    if (StringUtils.isEmpty(serverProject)) {
                        logger.logMessage("WARNING: No Klocwork project provided. " +
                            "Klocwork cannot perform server builds or synchronisations " +
                            "without a project.");
                    } else {
                        env.put(KlocworkConstants.KLOCWORK_PROJECT, serverProject);
                    }
                    if (StringUtils.isEmpty(buildSpec)) {
                        env.put(KlocworkConstants.KLOCWORK_BUILD_SPEC,
                            KlocworkConstants.DEFAULT_BUILD_SPEC);
                    } else {
                        env.put(KlocworkConstants.KLOCWORK_BUILD_SPEC, buildSpec);
                    }


                }
            };
    }

    public String getServerConfig() { return serverConfig; }
    public String getInstallConfig() { return installConfig; }
    public String getServerProject() { return serverProject; }
    public String getBuildSpec() { return buildSpec; }

    public final static String getNoneValue() { return "-- none --"; }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

         private String globalLicenseHost;
         private String globalLicensePort;
         private CopyOnWriteList<KlocworkServerConfig> serverConfigs = new CopyOnWriteList<KlocworkServerConfig>();
         private CopyOnWriteList<KlocworkInstallConfig> installConfigs = new CopyOnWriteList<KlocworkInstallConfig>();

        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        public String getDisplayName() {
            return "Klocwork - Build Capture Settings";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            serverConfigs.replaceBy(req.bindJSONToList(KlocworkServerConfig.class, formData.get("serverConfigs")));
            installConfigs.replaceBy(req.bindJSONToList(KlocworkInstallConfig.class, formData.get("installConfigs")));
            globalLicenseHost = formData.getString("globalLicenseHost");
            globalLicensePort = formData.getString("globalLicensePort");
            save();
            return super.configure(req,formData);
        }

        public String getGlobalLicenseHost() { return globalLicenseHost; }
        public String getGlobalLicensePort() { return globalLicensePort; }

        public KlocworkServerConfig[] getServerConfigs() {
            return serverConfigs.toArray(new KlocworkServerConfig[0]);
        }

        public KlocworkInstallConfig[] getInstallConfigs() {
            return installConfigs.toArray(new KlocworkInstallConfig[0]);
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
                return FormValidation.error("Port must be a number");
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
                return FormValidation.warning("Server Configuration is required for server builds, cross synchronisation and desktop analysis synchronisation");
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckServerProject(@QueryParameter String value)
            throws IOException, ServletException {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.warning("Server Project is required for server builds, cross synchronisation and desktop analysis synchronisation");
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckBuildSpec(@QueryParameter String value)
            throws IOException, ServletException {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.ok("Default is " + KlocworkConstants.DEFAULT_BUILD_SPEC);
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

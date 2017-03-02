
package com.emenda.emendaklocwork.config;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

import javax.servlet.ServletException;
import java.io.File;


public class KlocworkInstallConfig extends AbstractDescribableImpl<KlocworkInstallConfig> {

    private final String name;
    private final String paths;


    @DataBoundConstructor
    public KlocworkInstallConfig(String name, String paths) {
        this.name = name;
        this.paths = paths;
    }

    public String getName() { return name; }
    public String getPaths() { return paths; }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkInstallConfig> {
        public String getDisplayName() { return null; }
    }
}

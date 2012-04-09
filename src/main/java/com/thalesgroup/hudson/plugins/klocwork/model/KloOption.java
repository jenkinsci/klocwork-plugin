package com.thalesgroup.hudson.plugins.klocwork.model;

import org.kohsuke.stapler.DataBoundConstructor;

public class KloOption {

    private String cmdOption = null;

    private String cmdValue = null;

    @DataBoundConstructor
    public KloOption(String cmdOption, String cmdValue) {
        this.cmdOption = cmdOption;
        this.cmdValue = cmdValue;
    }

    public String getCmdOption() {
        return cmdOption;
    }

    public String getCmdValue() {
        return cmdValue;
    }
}
package com.emenda.klocwork.reporting;

import hudson.model.Action;

public class KlocworkProjectRedirectLink implements Action {

    public final String url;
    public final String text;
    public final String icon;

    public KlocworkProjectRedirectLink() {
        this.url = "lastBuild/KlocworkDashboard";
        this.text = "Klocwork Dashboard";
        this.icon = "/plugin/klocwork/icons/klocwork-24.gif";
    }

    @Override
    public String getUrlName() {
        return url;
    }

    @Override
    public String getDisplayName() {
        return text;
    }

    @Override
    public String getIconFileName() {
        return icon;
    }
}

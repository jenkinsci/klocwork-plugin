package com.emenda.klocwork;

import hudson.model.Action;

public class KlocworkQualityGateBadge implements Action {

    public final String text;
    public final String icon;
    public final String url;

    public KlocworkQualityGateBadge(String url, String text, String icon) {
        this.url = url;
        this.text = text;
        this.icon = icon;
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

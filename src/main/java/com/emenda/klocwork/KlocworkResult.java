package com.emenda.klocwork;

import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.model.Item;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class KlocworkResult implements Serializable {

    private static final long serialVersionUID = 3300652780649767750L;

    /**
     * The build owner
     */
    private AbstractBuild<?, ?> owner;

    public KlocworkResult(AbstractBuild<?, ?> owner) {
        this.owner = owner;
    }

    public AbstractBuild<?, ?> getOwner() {
        return owner;
    }
}

package com.emenda.klocwork;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Result;
import org.kohsuke.stapler.StaplerProxy;

public class KlocworkBuildAction extends Actionable implements Action, StaplerProxy {

    public static final String URL_NAME = "klocworkResult";
    private String iconFileName = "/plugin/emenda-klocwork/icons/klocwork-48.gif";
    private String displayName = "Klocwork Results";

    protected AbstractBuild<?, ?> owner;
    private KlocworkResult klocworkResult;

    protected KlocworkBuildAction(AbstractBuild<?, ?> owner) {
        this.owner = owner;
        this.klocworkResult = new KlocworkResult(owner);
    }

    // public <T extends KlocworkBuildAction> T getPreviousResult() {
    //     AbstractBuild<?, ?> b = owner;
    //     while (true) {
    //         b = b.getPreviousBuild();
    //         if (b == null)
    //             return null;
    //         if (b.getResult() == Result.FAILURE)
    //             continue;
    //         KlocworkBuildAction r = b.getAction(this.getClass());
    //         if (r != null)
    //             return (T) r;
    //     }
    // }

    public AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    public String getIconFileName() {
        return iconFileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrlName() {
        return URL_NAME;
    }

    public String getSearchUrl() {
        return getUrlName();
    }

    public Object getTarget() {
        return klocworkResult;
    }

}

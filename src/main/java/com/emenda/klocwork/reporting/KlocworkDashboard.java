package com.emenda.klocwork.reporting;

import com.emenda.klocwork.util.KlocworkIssue;
import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.util.ChartUtil;
import hudson.util.Graph;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Map;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class KlocworkDashboard implements Action {

    public final String url;
    public final String text;
    public final String icon;
    public final ArrayList<KlocworkIssue> localIssues;

    public KlocworkDashboard(ArrayList<KlocworkIssue> localIssues) {
        this.url = "KlocworkDashboard";
        this.text = "Klocwork Dashboard";
        this.icon = "/plugin/klocwork/icons/klocwork-24.gif";
        this.localIssues = localIssues;
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

    public double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public ArrayList<KlocworkIssue> getLocalIssues() {
        return localIssues;
    }

    public String getLocalIssuesSize() {
        return String.valueOf(localIssues.size());
    }
}

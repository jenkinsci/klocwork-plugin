package com.emenda.klocwork;

import hudson.model.*;
import org.json.*;

import com.emenda.klocwork.util.KlocworkUtil;

import java.util.List;
import java.util.ArrayList;

public class KlocworkProjectAction implements Action {

    private final Job<?, ?> job;

    public KlocworkProjectAction(Job<?, ?> job) {
        this.job = job;
    }

    public String getIconFileName() {
        return KlocworkConstants.ICON_URL;
    }

    public String getDisplayName() {
        return KlocworkConstants.DISPLAY_NAME;
    }

    public String getUrlName() {
        KlocworkBuildAction buildAction = getLatestBuildAction();
        if (buildAction != null) {
            if(buildAction.getProjectId() == null || buildAction.getProjectId().equals("")){
                return KlocworkUtil.getIssueListUrl(buildAction.getKlocworkURL(), buildAction.getKlocworkProject());
            }
            else{
                return KlocworkUtil.getIssueListUrl(buildAction.getKlocworkURL(), buildAction.getProjectId());
            }
        }
        return "";
    }

    public String getChartWidth() {
        return getLatestBuildAction().getChartWidth();
    }

    public String getChartHeight() {
        return getLatestBuildAction().getChartHeight();
    }

    public Job<?, ?> getJob() {
        return job;
    }

    public String getChartData() {
        JSONObject data = new JSONObject();

        // create JSON object to contain data...

        // labels are builds
        // each dataset is critical, error, etc issues for each build...
        List<String> labels = new ArrayList<String>();
        JSONObject criticalIssuedata = new JSONObject();
        JSONObject errorIssuedata = new JSONObject();
        JSONObject warningIssuedata = new JSONObject();
        JSONObject reviewIssuedata = new JSONObject();
        for (Run<?, ?> build = getJob().getFirstBuild(); build != null; build = build.getNextBuild()) {
            KlocworkBuildAction kwBuildAction = build.getAction(KlocworkBuildAction.class);
            if (kwBuildAction != null) {
                labels.add(kwBuildAction.getBuildName());
                criticalIssuedata.append("data", kwBuildAction.getCriticalCount());
                errorIssuedata.append("data", kwBuildAction.getErrorCount());
                warningIssuedata.append("data", kwBuildAction.getWarningCount());
                reviewIssuedata.append("data", kwBuildAction.getReviewCount());
            }
        }

        criticalIssuedata.put("label", KlocworkConstants.KLOCWORK_ISSUE_CRITICAL);
        criticalIssuedata.put("backgroundColor", "#dc0d0e");
        criticalIssuedata.put("borderColor", "#dc0d0e");
        criticalIssuedata.put("fill", "true");
        errorIssuedata.put("label", KlocworkConstants.KLOCWORK_ISSUE_ERROR);
        errorIssuedata.put("backgroundColor", "#de890d");
        errorIssuedata.put("borderColor", "#de890d");
        errorIssuedata.put("fill", "true");
        warningIssuedata.put("label", KlocworkConstants.KLOCWORK_ISSUE_WARNING);
        warningIssuedata.put("backgroundColor", "#3fa45b");
        warningIssuedata.put("borderColor", "#3fa45b");
        warningIssuedata.put("fill", "true");
        reviewIssuedata.put("label", KlocworkConstants.KLOCWORK_ISSUE_REVIEW);
        reviewIssuedata.put("backgroundColor", "#848f94");
        reviewIssuedata.put("borderColor", "#848f94");
        reviewIssuedata.put("fill", "true");

        data.put("labels", labels);
        data.append("datasets", criticalIssuedata);
        data.append("datasets", errorIssuedata);
        data.append("datasets", warningIssuedata);
        data.append("datasets", reviewIssuedata);

        return data.toString();
    }

    public boolean isVisible() {
        KlocworkBuildAction kwBuildAction = getLatestBuildAction();
        if (kwBuildAction != null) {
            return kwBuildAction.isDisplayChart();
        }
        return false;
    }

    public KlocworkBuildAction getLatestBuildAction() {
        for (Run<?, ?> build : getJob().getBuilds()) {
            KlocworkBuildAction kwBuildAction = build.getAction(KlocworkBuildAction.class);
            if (kwBuildAction != null) {
                return kwBuildAction;
            }
        }
        return null;
    }
}

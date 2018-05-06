package com.emenda.klocwork;

import hudson.model.*;
import org.json.*;

import java.util.LinkedHashMap;
import java.util.Map;

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
      return KlocworkUtil.getIssueListUrl(
        buildAction.getKlocworkURL(), buildAction.getKlocworkProject());
    }
    return "";
  }

  public String getChartWidth() {
    return "500";
  }

  public String getChartHeight() {
    return "200";
  }

  public Job<?, ?> getJob() {
    return job;
  }

  public String getChartData() {
    JSONObject data = new JSONObject();
  
    // create JSON object to contain data...

    // labels are builds
    // each dataset  is critical, error, etc issues for each build...
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

  //   data: {
  //     labels: [1500,1600,1700,1750,1800,1850,1900,1950,1999,2050],
  //     datasets: [{ 
  //         data: [86,114,106,106,107,111,133,221,783,2478],
  //         label: "Africa",
  //         borderColor: "#3e95cd",
  //         fill: true
  //     }, { 
  //         data: [282,350,411,502,635,809,947,1402,3700,5267],
  //         label: "Asia",
  //         borderColor: "#8e5ea2",
  //         fill: true
  //     }]
  // },
    return data.toString();
  }

  // private void putCommonProperties(JSONObject obj) {
  //   obj.put("fill", "true");
  // }

  public boolean isVisible() {
    for (Run<?, ?> build : getJob().getBuilds()) {
      KlocworkBuildAction kwBuildAction = build.getAction(KlocworkBuildAction.class);
      if (kwBuildAction != null) {
        return true;
      }
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

package com.emenda.klocwork;

import com.emenda.klocwork.util.KlocworkUtil;

import hudson.EnvVars;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.tasks.SimpleBuildStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.List;

public class KlocworkBuildAction implements Action, SimpleBuildStep.LastBuildAction {

  private final Run<?, ?> run;
  private final int criticalCount;
  private final int errorCount;
  private final int warningCount;
  private final int reviewCount;
  private final String buildName;
  private final String klocworkURL;
  private final String klocworkProject;

  public KlocworkBuildAction(Run<?, ?> run, Map<String, Integer> severityMap,
  EnvVars envVars, String buildName) {
    this.run = run;
    this.criticalCount = severityMap.getOrDefault(KlocworkConstants.KLOCWORK_ISSUE_CRITICAL, 0);
    this.errorCount = severityMap.getOrDefault(KlocworkConstants.KLOCWORK_ISSUE_ERROR, 0);
    this.warningCount = severityMap.getOrDefault(KlocworkConstants.KLOCWORK_ISSUE_WARNING, 0);
    this.reviewCount = severityMap.getOrDefault(KlocworkConstants.KLOCWORK_ISSUE_REVIEW, 0);
    this.buildName = KlocworkUtil.getDefaultBuildName(buildName, envVars);
    this.klocworkURL = KlocworkUtil.getNormalizedKlocworkUrl(envVars);
    this.klocworkProject = envVars.get(KlocworkConstants.KLOCWORK_PROJECT);
  }

  public Run<?, ?> getRun() {
    return run;
  }

  public String getIconFileName() {
    return KlocworkConstants.ICON_URL;
  }

  public String getDisplayName() {
    return KlocworkConstants.DISPLAY_NAME;
  }

  public String getUrlName() {
    return KlocworkUtil.getBuildIssueListUrl(klocworkURL, klocworkProject, buildName);
  }

  public int getCriticalCount() {
    return criticalCount;
  }

  public int getErrorCount() {
    return errorCount;
  }

  public int getReviewCount() {
    return reviewCount;
  }

  public int getWarningCount() {
    return warningCount;
  }

  public String getBuildName() {
    return buildName;
  }

  public String getKlocworkURL() {
    return klocworkURL;
  }

  public String getKlocworkProject() {
    return klocworkProject;
  }

  @Override
  public Collection<? extends Action> getProjectActions() {
    List<KlocworkProjectAction> projectActions = new ArrayList<>();
    projectActions.add(new KlocworkProjectAction(run.getParent()));
    return projectActions;
  }
}

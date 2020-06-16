/*
 * *****************************************************************************
 * Copyright (c) 2020 Rogue Wave Software, Inc., a Perforce company
 * Author : Klocwork
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * *****************************************************************************
 */

package com.klocwork.kwjenkinsplugin.reporting;

import com.klocwork.kwjenkinsplugin.KlocworkConstants;
import com.klocwork.kwjenkinsplugin.definitions.KlocworkIssue;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class KlocworkDashboard implements Action, LastBuildAction {

    private static final Logger debugLogger = Logger.getLogger(KlocworkDashboard.class.getName());

    public final String url;
    public final String text;
    public final String icon;
    public final List<? extends KlocworkIssue> localIssues;
    public final List<KlocworkIssue> serverIssues;
    public final boolean shouldDashboardLocal;
    public final boolean shouldDashboardServer;

    public KlocworkDashboard(List<? extends KlocworkIssue> localIssues, List<KlocworkIssue> serverIssues, boolean shouldDashboardLocal, boolean shouldDashboardServer) {
        debugLogger.fine("[" + this.getClass().getName() + "] - Constructing the dashboard");
        this.shouldDashboardLocal = shouldDashboardLocal;
        this.shouldDashboardServer = shouldDashboardServer;
        this.url = "KlocworkDashboard";
        this.text = Messages.KlocworkDashboard_klocwork_dashboard();
        this.icon = KlocworkConstants.ICON_URL;
        this.localIssues = localIssues;
        this.serverIssues = serverIssues;
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

    public List<? extends KlocworkIssue> getLocalIssues() {
        return localIssues;
    }

    public String getLocalIssuesSize() {
        return String.valueOf(localIssues.size());
    }

    public List<KlocworkIssue> getServerIssues() {
        return serverIssues;
    }

    public String getServerIssuesSize() {
        return String.valueOf(serverIssues.size());
    }

    public boolean isShouldDashboardLocal() {
        return shouldDashboardLocal;
    }

    public boolean isShouldDashboardServer() {
        return shouldDashboardServer;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        final List<KlocworkDashboard> projectActions = new ArrayList<>();
        projectActions.add(new KlocworkDashboard(localIssues, serverIssues, shouldDashboardLocal, shouldDashboardServer));

        return projectActions;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thalesgroup.hudson.plugins.klocwork;

import com.thalesgroup.hudson.plugins.klocwork.model.KloReport;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.view.dashboard.DashboardPortlet;
import org.kohsuke.stapler.DataBoundConstructor;

public class KloPortlet extends DashboardPortlet {

    @DataBoundConstructor
    public KloPortlet(String name) {
        super(name);
    }

    public KloReport getStatistics(Job<?, ?> job) {
        Run<?, ?> build = job.getLastBuild();
        while (build != null) {
            KloBuildAction action = build.getAction(KloBuildAction.class);
            if (action != null) {
                KloResult result = action.getResult();
                if (result != null) {
                    return result.getReport();
                }
            }
            build = build.getPreviousBuild();
        }
        return new KloReport();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DashboardPortlet> {

        @Override
        public String getDisplayName() {
            return "Klocwork Dashboard";
        }
    }
}

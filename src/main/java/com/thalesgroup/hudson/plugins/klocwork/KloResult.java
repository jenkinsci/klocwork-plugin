/*******************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS                             *
 * Author : Loic Quentin                                                        *
 *		                                                                        *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *                                                                              *
 *******************************************************************************/
package com.thalesgroup.hudson.plugins.klocwork;

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
import com.thalesgroup.hudson.plugins.klocwork.model.KloReport;
import com.thalesgroup.hudson.plugins.klocwork.model.KloSourceContainer;
import com.thalesgroup.hudson.plugins.klocwork.model.KloWorkspaceFile;
import com.thalesgroup.hudson.plugins.klocwork.util.KloSummary;
import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.model.Item;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class KloResult implements Serializable {


    private static final long serialVersionUID = 3300652780649767750L;

    /**
     * The Klocwork report
     */
    private KloReport report;

    /**
     * The build owner
     */
    private AbstractBuild<?, ?> owner;

    /**
     * The Klocwork container with all source files
     */
    private KloSourceContainer kloSourceContainer;
    
    private boolean kwinspectreportDeprecated;

    public KloResult(KloReport report, KloSourceContainer kloSourceContainer, AbstractBuild<?, ?> owner) {
        this.report = report;
        this.owner = owner;
        this.kloSourceContainer = kloSourceContainer;
    }

    /**
     * Gets the remote API for the build result.
     *
     * @return the remote API
     */
    public Api getApi() {
        return new Api(report);
    }

    public KloReport getReport() {
        return report;
    }


    public AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    /**
     * Renders the summary Klocwork report for the build result.
     *
     * @return the HTML fragment of the summary Klocwork report
     */
    public String getSummary() {
        return KloSummary.createReportSummary(this);
    }

    /**
     * Renders the detailed summary Klocwork report for the build result.
     *
     * @return the HTML fragment of the summary Klocwork report
     */
    public String getDetails() {
        return KloSummary.createReportSummaryDetails(this);
    }

    /**
     * Returns the number of new errors from the previous build result.
     *
     * @return the number of new errors
     */
    public int getNumberNewErrorsFromPreviousBuild() {
        if(this.report != null){
            Double d = this.report.getNeww();
            return d.intValue();
        }
        else{
            return 0;
        }
    }

    /**
     * Gets the number of errors according the selected severitiies form the configuration user object.
     *
     * @param kloConfig     the Klocwork configuration object
     * @param checkNewError true, if the request is for the number of new errors
     * @return the number of errors or new errors (if checkNewEroor is set to true) for the current configuration object
     * @throws IOException klocwork configuration file is missing
     */
    public int getNumberErrorsAccordingConfiguration(KloConfig kloConfig,
                                                     boolean checkNewError) throws IOException {
        if (kloConfig == null) {
            throw new IOException("[ERROR] - The klocwork configuration file is missing. Could you save again your job configuration.");
        }

        int nbErrors = 0;
        int nbPreviousError = 0;
        KloResult previousResult = this.getPreviousResult();
        
        if (kloConfig.getConfigSeverityEvaluation().isHighSeverity()) {
            nbErrors = this.getReport().getHighSeverities();
            if (previousResult != null) {
                nbPreviousError = previousResult.getReport().getHighSeverities();
            }
        }

        if (kloConfig.getConfigSeverityEvaluation().isLowSeverity()) {
            nbErrors = nbErrors + this.getReport().getLowSeverities();
            if (previousResult != null) {
                nbPreviousError = nbPreviousError + previousResult.getReport().getLowSeverities();
            }
        }

        if (checkNewError) {
            //if (previousResult != null) {
                return nbErrors - nbPreviousError;
            //} else {
            //    return 0;
            //}
        } else {
            return nbErrors;
        }
    }

    /**
     * Gets the previous Klocwork result for the build result.
     *
     * @return the previous Klocwork result
     */
    public KloResult getPreviousResult() {
        KloBuildAction previousAction = getPreviousAction();
        KloResult previousResult = null;
        if (previousAction != null) {
            previousResult = previousAction.getResult();
        }

        return previousResult;
    }

    /**
     * Gets the previous Action for the build result.
     *
     * @return the previous Klocwork Build Action
     */
    KloBuildAction getPreviousAction() {
        AbstractBuild<?, ?> previousBuild = owner.getPreviousBuild();
        if (previousBuild != null) {
            return previousBuild.getAction(KloBuildAction.class);
        }
        return null;
    }

    public KloSourceContainer getKloSourceContainer() {
        return kloSourceContainer;
    }
    
    public boolean isKwinspectreportDeprecated() {
        // TODO: Improve maintainability of code by removing version-dependency here
        return report.getKloVersion().startsWith("9.6");
    }
}

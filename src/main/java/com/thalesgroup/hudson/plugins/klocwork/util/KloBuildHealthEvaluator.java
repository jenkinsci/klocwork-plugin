/*******************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS                             *
 * Author : Aravindan Mahendran                                                 *
 *                                                                              *
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
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.klocwork.util;

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
import hudson.model.HealthReport;

public class KloBuildHealthEvaluator {

    public HealthReport evaluatBuildHealth(KloConfig kloConfig, int nbErrorForSeverity) {

        if (kloConfig == null) {
            // no thresholds => no report
            return null;
        }

        if (isHealthyReportEnabled(kloConfig)) {
            int percentage;
            int counter = nbErrorForSeverity;

            if (counter < KloMetricUtil.convert(kloConfig.getConfigSeverityEvaluation().getHealthy())) {
                percentage = 100;
            } else if (counter > KloMetricUtil.convert(kloConfig.getConfigSeverityEvaluation().getUnHealthy())) {
                percentage = 0;
            } else {
                percentage = 100 - ((counter - KloMetricUtil.convert(kloConfig.getConfigSeverityEvaluation().getHealthy())) * 100
                        / (KloMetricUtil.convert(kloConfig.getConfigSeverityEvaluation().getUnHealthy()) - KloMetricUtil.convert(kloConfig.getConfigSeverityEvaluation().getHealthy())));
            }

            return new HealthReport(percentage, Messages._KlocworkBuildHealthEvaluator_Description(KloMetricUtil.getMessageSelectedSeverties(kloConfig)));
        }
        return null;
    }


    private boolean isHealthyReportEnabled(KloConfig kloConfig) {
        if (KloMetricUtil.isValid(kloConfig.getConfigSeverityEvaluation().getHealthy()) && KloMetricUtil.isValid(kloConfig.getConfigSeverityEvaluation().getUnHealthy())) {
            int healthyNumber = KloMetricUtil.convert(kloConfig.getConfigSeverityEvaluation().getHealthy());
            int unHealthyNumber = KloMetricUtil.convert(kloConfig.getConfigSeverityEvaluation().getUnHealthy());
            return unHealthyNumber > healthyNumber;
        }
        return false;
    }
}

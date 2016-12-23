/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package com.thalesgroup.hudson.plugins.klocwork.util;

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
import hudson.model.HealthReport;
import java.util.Iterator;
import java.util.Map;

public class KloBuildHealthEvaluator {

    public HealthReport evaluatBuildHealth(KloConfig kloConfig, int nbErrorForSeverity, Map<String, String> matrixBuildVars) {

        String healthy = kloConfig.getConfigSeverityEvaluation().getHealthy();
        String unhealthy = kloConfig.getConfigSeverityEvaluation().getUnHealthy();
        
        if (matrixBuildVars != null) {
            Iterator it = matrixBuildVars.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                if (healthy.contains("%" + pairs.getKey().toString() + "%")) {
                    healthy = healthy.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (healthy.contains("${" + pairs.getKey().toString() + "}")) {
                    healthy = healthy.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (healthy.contains("$" + pairs.getKey().toString())) {
                    healthy = healthy.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                
                if (unhealthy.contains("%" + pairs.getKey().toString() + "%")) {
                    unhealthy = unhealthy.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (unhealthy.contains("${" + pairs.getKey().toString() + "}")) {
                    unhealthy = unhealthy.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (unhealthy.contains("$" + pairs.getKey().toString())) {
                    unhealthy = unhealthy.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        if (kloConfig == null) {
            // no thresholds => no report
            return null;
        }

        if (isHealthyReportEnabled(healthy, unhealthy)) {
            int percentage;
            int counter = nbErrorForSeverity;

            if (counter < KloMetricUtil.convert(healthy)) {
                percentage = 100;
            } else if (counter > KloMetricUtil.convert(unhealthy)) {
                percentage = 0;
            } else {
                percentage = 100 - ((counter - KloMetricUtil.convert(healthy)) * 100
                        / (KloMetricUtil.convert(unhealthy) - KloMetricUtil.convert(healthy)));
            }

            return new HealthReport(percentage, Messages._KlocworkBuildHealthEvaluator_Description(KloMetricUtil.getMessageSelectedSeverties(kloConfig)));
        }
        return null;
    }

    private boolean isHealthyReportEnabled(String healthy, String unhealthy) {
        if (KloMetricUtil.isValid(healthy) && KloMetricUtil.isValid(unhealthy)) {
            int healthyNumber = KloMetricUtil.convert(healthy);
            int unHealthyNumber = KloMetricUtil.convert(unhealthy);
            return unHealthyNumber > healthyNumber;
        }
        return false;
    }
}

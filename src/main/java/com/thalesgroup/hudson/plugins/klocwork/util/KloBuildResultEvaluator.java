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
package com.thalesgroup.hudson.plugins.klocwork.util;

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
import hudson.model.BuildListener;
import hudson.model.Result;
import java.util.Iterator;
import java.util.Map;

public class KloBuildResultEvaluator {

    private boolean isErrorCountExceeded(final int errorCount, final String errorThreshold) {
        if (errorCount > 0 && KloMetricUtil.isValid(errorThreshold)) {
            return errorCount > KloMetricUtil.convert(errorThreshold);
        }
        return false;
    }

    public Result evaluateBuildResult(
            final BuildListener listener,
            int errorsCount,
            int newErrorsCount,
            KloConfig kloConfig,
            Map<String, String> matrixBuildVars) {
        
        String failureThreshold = kloConfig.getConfigSeverityEvaluation().getFailureThreshold();
        String newFailureThreshold = kloConfig.getConfigSeverityEvaluation().getNewFailureThreshold();
        String unstableThreshold = kloConfig.getConfigSeverityEvaluation().getThreshold();
        String newUnstableThreshold = kloConfig.getConfigSeverityEvaluation().getNewThreshold();
        
        if (matrixBuildVars != null) {
            Iterator it = matrixBuildVars.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                if (failureThreshold.contains("%" + pairs.getKey().toString() + "%")) {
                    failureThreshold = failureThreshold.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (failureThreshold.contains("${" + pairs.getKey().toString() + "}")) {
                    failureThreshold = failureThreshold.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (failureThreshold.contains("$" + pairs.getKey().toString())) {
                    failureThreshold = failureThreshold.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                
                if (newFailureThreshold.contains("%" + pairs.getKey().toString() + "%")) {
                    newFailureThreshold = newFailureThreshold.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (newFailureThreshold.contains("${" + pairs.getKey().toString() + "}")) {
                    newFailureThreshold = newFailureThreshold.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (newFailureThreshold.contains("$" + pairs.getKey().toString())) {
                    newFailureThreshold = newFailureThreshold.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                
                if (unstableThreshold.contains("%" + pairs.getKey().toString() + "%")) {
                    unstableThreshold = unstableThreshold.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (unstableThreshold.contains("${" + pairs.getKey().toString() + "}")) {
                    unstableThreshold = unstableThreshold.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (unstableThreshold.contains("$" + pairs.getKey().toString())) {
                    unstableThreshold = unstableThreshold.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                
                if (newUnstableThreshold.contains("%" + pairs.getKey().toString() + "%")) {
                    newUnstableThreshold = newUnstableThreshold.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (newUnstableThreshold.contains("${" + pairs.getKey().toString() + "}")) {
                    newUnstableThreshold = newUnstableThreshold.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (newUnstableThreshold.contains("$" + pairs.getKey().toString())) {
                    newUnstableThreshold = newUnstableThreshold.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        if (isErrorCountExceeded(errorsCount, failureThreshold)) {
            listener.getLogger().println("Setting build status to FAILURE since total number of errors ("
                    + KloMetricUtil.getMessageSelectedSeverties(kloConfig)
                    + ") exceeds the threshold value ;" + failureThreshold + "'.");
            return Result.FAILURE;
        }
        if (isErrorCountExceeded(newErrorsCount, newFailureThreshold)) {
            listener.getLogger().println("Setting build status to FAILURE since total number of new errors ("
                    + KloMetricUtil.getMessageSelectedSeverties(kloConfig)
                    + ") exceeds the threshold value '" + newFailureThreshold + "'.");
            return Result.FAILURE;
        }
        if (isErrorCountExceeded(errorsCount, unstableThreshold)) {
            listener.getLogger().println("Setting build status to UNSTABLE since total number of errors ("
                    + KloMetricUtil.getMessageSelectedSeverties(kloConfig)
                    + ") exceeds the threshold value '" + unstableThreshold + "'.");
            return Result.UNSTABLE;
        }
        if (isErrorCountExceeded(newErrorsCount, newUnstableThreshold)) {
            listener.getLogger().println("Setting build status to UNSTABLE since total number of new errors ("
                    + KloMetricUtil.getMessageSelectedSeverties(kloConfig)
                    + ") exceeds the threshold value '" + newUnstableThreshold + "'.");
            return Result.UNSTABLE;
        }

        listener.getLogger().println("Not changing build status, since no threshold has been exceeded");
        return Result.SUCCESS;
    }

}

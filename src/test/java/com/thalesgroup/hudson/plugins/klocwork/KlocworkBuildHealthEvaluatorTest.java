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


package com.thalesgroup.hudson.plugins.klocwork;

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
import com.thalesgroup.hudson.plugins.klocwork.config.KloConfigSeverityEvaluation;
import com.thalesgroup.hudson.plugins.klocwork.util.KloBuildHealthEvaluator;
import hudson.model.HealthReport;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KlocworkBuildHealthEvaluatorTest {
    private KloBuildHealthEvaluator klocworkBuildHealthEvaluator;
    KloConfig klocworkConfig;

    @Before
    public void initialize() {
        klocworkConfig = mock(KloConfig.class);
        klocworkBuildHealthEvaluator = new KloBuildHealthEvaluator();
    }

    private int processSetThreshold(int healthy, int unHealthy, int errorsForSevrity) {
        KloConfigSeverityEvaluation configSeverityEvaluation = mock(KloConfigSeverityEvaluation.class);
        when(klocworkConfig.getConfigSeverityEvaluation()).thenReturn(configSeverityEvaluation);
        when(klocworkConfig.getConfigSeverityEvaluation().getHealthy()).thenReturn(String.valueOf(healthy));
        when(klocworkConfig.getConfigSeverityEvaluation().getUnHealthy()).thenReturn(String.valueOf(unHealthy));
        HealthReport healthReport = klocworkBuildHealthEvaluator.evaluatBuildHealth(klocworkConfig, errorsForSevrity);
        return healthReport.getScore();
    }

    @Test
    public void testScore() {
        Assert.assertEquals(0, processSetThreshold(0, 10, 11));
        Assert.assertEquals(0, processSetThreshold(0, 10, 10));
        Assert.assertEquals(10, processSetThreshold(0, 10, 9));
        Assert.assertEquals(20, processSetThreshold(0, 10, 8));
        Assert.assertEquals(30, processSetThreshold(0, 10, 7));
        Assert.assertEquals(40, processSetThreshold(0, 10, 6));
        Assert.assertEquals(50, processSetThreshold(0, 10, 5));
        Assert.assertEquals(60, processSetThreshold(0, 10, 4));
        Assert.assertEquals(70, processSetThreshold(0, 10, 3));
        Assert.assertEquals(80, processSetThreshold(0, 10, 2));
        Assert.assertEquals(90, processSetThreshold(0, 10, 1));
        Assert.assertEquals(100, processSetThreshold(0, 10, 0));
    }
}

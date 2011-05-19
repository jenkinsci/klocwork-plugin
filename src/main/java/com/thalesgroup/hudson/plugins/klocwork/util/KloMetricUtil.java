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
import org.apache.commons.lang.StringUtils;

public class KloMetricUtil {

    public static int convert(String threshold) {
        if (isValid(threshold)) {
            if (StringUtils.isNotBlank(threshold)) {
                try {
                    return Integer.valueOf(threshold);
                } catch (NumberFormatException exception) {
                    // not valid
                }
            }
        }
        throw new IllegalArgumentException("Not a parsable integer value >= 0: " + threshold);
    }

    public static boolean isValid(final String threshold) {
        if (StringUtils.isNotBlank(threshold)) {
            try {
                return Integer.valueOf(threshold) >= 0;
            } catch (NumberFormatException exception) {
                // not valid
            }
        }
        return false;
    }

    public static String getMessageSelectedSeverties(KloConfig kloConfig) {
        StringBuffer sb = new StringBuffer();

        if (kloConfig.getConfigSeverityEvaluation().isAllSeverities()) {
            sb.append("with all severities");
            return sb.toString();
        }

        if (kloConfig.getConfigSeverityEvaluation().isHighSeverity()) {
            sb.append(" and ");
            sb.append("severity 'high severity'");
        }

        if (kloConfig.getConfigSeverityEvaluation().isLowSeverity()) {
            sb.append(" and ");
            sb.append("severity 'low severity'");
        }

        if (sb.length() != 0)
            sb.delete(0, 5);

        return sb.toString();
    }

}

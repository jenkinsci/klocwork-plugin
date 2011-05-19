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

import com.thalesgroup.hudson.plugins.klocwork.KloBuildAction;
import com.thalesgroup.hudson.plugins.klocwork.KloResult;
import com.thalesgroup.hudson.plugins.klocwork.Messages;


public class KloSummary {

    private KloSummary() {
        super();
    }

    public static String createReportSummary(KloResult kloResult) {
        StringBuilder summary = new StringBuilder();
        int nbErrors = kloResult.getReport().getNumberTotal();

        summary.append(Messages.klocwork_Errors_ProjectAction_Name());
        summary.append(": ");
        if (nbErrors == 0) {
            summary.append(Messages.klocwork_ResultAction_NoError());
        } else {
            summary.append("<a href=\"" + KloBuildAction.URL_NAME + "\">");

            if (nbErrors == 1) {
                summary.append(Messages.klocwork_ResultAction_OneError());
            } else {
                summary.append(Messages.klocwork_ResultAction_MultipleErrors(nbErrors));
            }
            summary.append("</a>");
        }
        summary.append(".");

        return summary.toString();
    }

    public static String createReportSummaryDetails(KloResult kloResult) {
        StringBuilder builder = new StringBuilder();
        int nbNewErrors = kloResult.getNumberNewErrorsFromPreviousBuild();

        builder.append("<li>");

        if (nbNewErrors == 0) {
            builder.append(Messages.klocwork_ResultAction_Detail_NoNewError());
        } else if (nbNewErrors == 1) {
            builder.append(Messages.klocwork_ResultAction_Detail_NewOneError());
        } else {
            builder.append(Messages.klocwork_ResultAction_Detail_NewMultipleErrors());
            builder.append(": ");
            builder.append(nbNewErrors);
        }
        builder.append("</li>");

        return builder.toString();
    }
}

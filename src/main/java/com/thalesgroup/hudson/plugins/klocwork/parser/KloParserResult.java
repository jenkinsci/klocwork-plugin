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

package com.thalesgroup.hudson.plugins.klocwork.parser;

import com.thalesgroup.hudson.plugins.klocwork.model.KloReport;
import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;

public class KloParserResult implements FilePath.FileCallable<KloReport> {

    private static final long serialVersionUID = 1L;

    private final BuildListener listener;

    private final String klocworkReportPattern;

    public static final String DELAULT_REPORT_MAVEN = "klocwork_result.xml";

    public KloParserResult(final BuildListener listener, String klocworkReportPattern) {

        if (klocworkReportPattern == null) {
            klocworkReportPattern = DELAULT_REPORT_MAVEN;
        }

        if (klocworkReportPattern.trim().length() == 0) {
            klocworkReportPattern = DELAULT_REPORT_MAVEN;
        }

        this.listener = listener;
        this.klocworkReportPattern = klocworkReportPattern;
    }

    public KloReport invoke(java.io.File basedir, VirtualChannel channel) throws IOException {

        KloReport kloReportResult = new KloReport();
        try {
            String[] kloReportFiles = findKlocworkReports(basedir);
            if (kloReportFiles.length == 0) {
                String msg = "No klocwork test report file(s) were found with the pattern '"
                        + klocworkReportPattern + "' relative to '"
                        + basedir + "'."
                        + "  Did you enter a pattern relative to the correct directory?"
                        + "  Did you generate the XML report(s) for Klocwork?";
                throw new IllegalArgumentException(msg);
            }

            listener.getLogger().println("Processing " + kloReportFiles.length + " files with the pattern '" + klocworkReportPattern + "'.");

            for (String kloReportFileName : kloReportFiles) {
                KloReport kloReport = new KloParser().parse(new File(basedir, kloReportFileName));

                mergeReport(kloReportResult, kloReport);
            }
        } catch (Exception e) {
            listener.getLogger().println("Parsing has been canceled. " + e.getMessage() + " " + e.getLocalizedMessage());
            return null;
        }
        return kloReportResult;
    }


    private static void mergeReport(KloReport kloReportResult, KloReport kloReport) {

        kloReportResult.getHighSeverities().addAll(kloReport.getHighSeverities());
        kloReportResult.getLowSeverities().addAll(kloReport.getLowSeverities());
        kloReportResult.getAllSeverities().addAll(kloReport.getAllSeverities());
        kloReportResult.setNeww(kloReportResult.getNeww() + kloReport.getNeww());
        kloReportResult.setFixed(kloReportResult.getFixed() + kloReport.getFixed());
        kloReportResult.setExisting(kloReportResult.getExisting() + kloReport.getExisting());

    }

    /**
     * Return all klocwork report files
     *
     * @param parentPath parent
     * @return an array of strings
     */
    private String[] findKlocworkReports(File parentPath) {
        FileSet fs = Util.createFileSet(parentPath, this.klocworkReportPattern);
        DirectoryScanner ds = fs.getDirectoryScanner();
        String[] kloFiles = ds.getIncludedFiles();
        return kloFiles;
    }

    public String getKlocworkReportPattern() {
        return klocworkReportPattern;
    }

}

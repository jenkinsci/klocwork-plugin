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

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
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
    private double newIssues;
    private int numCrit = 0;
    private int numErr = 0;
    private int numWarn = 0;
    private int numRev = 0;
    private int totalNumCrit = 0;
    private int totalNumErr = 0;
    private int totalNumWarn = 0;
    private int totalNumRev = 0;
    private boolean kw96up = true;

    public static final String DELAULT_REPORT_MAVEN = "klocwork_result.xml";

    public KloParserResult(final BuildListener listener, KloConfig kloConfig) {

        String kwRepPattern = kloConfig.getKlocworkReportPattern();
        
        if (kwRepPattern == null) {
            kwRepPattern = DELAULT_REPORT_MAVEN;
        }

        if (kwRepPattern.trim().length() == 0) {
            kwRepPattern = DELAULT_REPORT_MAVEN;
        }

        this.kw96up = kloConfig.getWebAPI().getUseWebAPI();
        this.listener = listener;
        this.klocworkReportPattern = kwRepPattern;
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
                KloReport kloReport = new KloParser().parse(new File(basedir, kloReportFileName), listener, kw96up);
                mergeReport(kloReportResult, kloReport);
            }
            
        } catch (Exception e) {
            listener.getLogger().println("Parsing has been canceled. " + e.getMessage() + " " + e.getLocalizedMessage());
            return null;
        }
        newIssues = kloReportResult.getNeww();
        numCrit = kloReportResult.getNumCrit();
        numErr = kloReportResult.getNumErr();
        numWarn = kloReportResult.getNumWarn();
        numRev = kloReportResult.getNumRev();
        
        totalNumCrit = kloReportResult.getTotalNumCrit();
        totalNumErr = kloReportResult.getTotalNumErr();
        totalNumWarn = kloReportResult.getTotalNumWarn();
        totalNumRev = kloReportResult.getTotalNumRev();
        
        return kloReportResult;
    }


    private static void mergeReport(KloReport kloReportResult, KloReport kloReport) {
        kloReportResult.setHighSeverities(kloReport.getHighSeverities()+kloReportResult.getHighSeverities());
        kloReportResult.setLowSeverities(kloReport.getLowSeverities()+kloReportResult.getLowSeverities());
        kloReportResult.setErrors(kloReport.getAllSeverities() + kloReportResult.getAllSeverities());
        kloReportResult.setNeww(kloReportResult.getNeww() + kloReport.getNeww());
        kloReportResult.setNumCrit(kloReportResult.getNumCrit() + kloReport.getNumCrit());
        kloReportResult.setNumErr(kloReportResult.getNumErr() + kloReport.getNumErr());
        kloReportResult.setNumWarn(kloReportResult.getNumWarn() + kloReport.getNumWarn());
        kloReportResult.setNumRev(kloReportResult.getNumRev() + kloReport.getNumRev());
        kloReportResult.setFixed(kloReportResult.getFixed() + kloReport.getFixed());
        kloReportResult.setExisting(kloReportResult.getExisting() + kloReport.getExisting());
        kloReportResult.setKloVersion(kloReport.getKloVersion());
        kloReportResult.setTotalNumCrit(kloReportResult.getTotalNumCrit() + kloReport.getTotalNumCrit());
        kloReportResult.setTotalNumErr(kloReportResult.getTotalNumErr() + kloReport.getTotalNumErr());
        kloReportResult.setTotalNumWarn(kloReportResult.getTotalNumWarn() + kloReport.getTotalNumWarn());
        kloReportResult.setTotalNumRev(kloReportResult.getTotalNumRev() + kloReport.getTotalNumRev());
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

    public double getNewIssues() {
        return newIssues;
    }
    public String getKlocworkReportPattern() {
        return klocworkReportPattern;
    }
    public int getNumCrit() {
        return numCrit;
    }
    public int getNumErr() {
        return numErr;
    }
    public int getNumWarn() {
        return numWarn;
    }
    public int getNumRev() {
        return numRev;
    }
}

/**
 * *****************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS * Author : Loic Quentin * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * * of this software and associated documentation files (the "Software"), to
 * deal* in the Software without restriction, including without limitation the
 * rights * to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell * copies of the Software, and to permit persons to whom the Software is
 * * furnished to do so, subject to the following conditions: * * The above
 * copyright notice and this permission notice shall be included in * all copies
 * or substantial portions of the Software. * * THE SOFTWARE IS PROVIDED "AS
 * IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR * IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, * FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE * AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER * LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,* OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN * THE SOFTWARE. * *
 ******************************************************************************
 */
package com.thalesgroup.hudson.plugins.klocwork.model;

import org.kohsuke.stapler.export.Exported;

import java.io.Serializable;

public class KloReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private int highSeverities = 0; // High severities = low errors
    private int lowSeverities = 0; // Low severities = high errors
    private int all = 0;

    private double fixed = 0.0;
    private double existing = 0.0;
    private double neww = 0.0;
    private int numCrit = 0;
    private int numErr = 0;
    private int numWarn = 0;
    private int numRev = 0;
    private int totalNumCrit = 0;
    private int totalNumErr = 0;
    private int totalNumWarn = 0;
    private int totalNumRev = 0;

    private String kloVersion = "";

    public int getHighSeverities() {
        return highSeverities;
    }

    public void setHighSeverities(int highSeverities) {
        this.highSeverities = highSeverities;
    }

    public int getLowSeverities() {
        return lowSeverities;
    }

    public void setLowSeverities(int lowSeverities) {
        this.lowSeverities = lowSeverities;
    }

    @Exported
    public int getAllSeverities() {
        return all;
    }

    public void setErrors(int errors) {
        this.all = errors;
    }

    public void setKloVersion(String kloVersion) {
        if (kloVersion != null) {
            this.kloVersion = kloVersion;
        }
    }

    public String getKloVersion() {
        if (kloVersion != null) {
            return kloVersion;
        }
        return "";
    }

    @Exported
    public int getNumberTotal() {
        return all;
    }

    @Exported
    public int getNumberHighSeverities() {
        return highSeverities;
    }

    @Exported
    public int getNumberLowSeverities() {
        return lowSeverities;
    }

    public void setFixed(double fixed) {
        this.fixed = fixed;
    }

    public double getFixed() {
        return fixed;
    }
    
    public int getFixedInt() {
        return (int)fixed;
    }

    public void setExisting(double existing) {
        this.existing = existing;
    }

    public double getExisting() {
        return existing;
    }
    
    public int getExistingInt() {
        return (int)existing;
    }

    public void setNeww(double neww) {
        this.neww = neww;
    }

    public double getNeww() {
        return neww;
    }
    
    public int getNewwInt() {
        return (int)neww;
    }

    public void setNumCrit(int numCrit) {
        this.numCrit = numCrit;
    }

    public int getNumCrit() {
        return numCrit;
    }

    public void setNumErr(int numErr) {
        this.numErr = numErr;
    }

    public int getNumErr() {
        return numErr;
    }

    public void setNumWarn(int numWarn) {
        this.numWarn = numWarn;
    }

    public int getNumWarn() {
        return numWarn;
    }

    public void setNumRev(int numRev) {
        this.numRev = numRev;
    }

    public int getNumRev() {
        return numRev;
    }

    public void setTotalNumCrit(int totalNumCrit) {
        this.totalNumCrit = totalNumCrit;
    }

    public int getTotalNumCrit() {
        return totalNumCrit;
    }

    public void setTotalNumErr(int totalNumErr) {
        this.totalNumErr = totalNumErr;
    }

    public int getTotalNumErr() {
        return totalNumErr;
    }

    public void setTotalNumWarn(int totalNumWarn) {
        this.totalNumWarn = totalNumWarn;
    }

    public int getTotalNumWarn() {
        return totalNumWarn;
    }

    public void setTotalNumRev(int totalNumRev) {
        this.totalNumRev = totalNumRev;
    }

    public int getTotalNumRev() {
        return totalNumRev;
    }

}

/*******************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS                             *
 * Author : Loic Quentin                                                        *
 *		                                                                       *
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
package com.thalesgroup.hudson.plugins.klocwork.model;

import org.kohsuke.stapler.export.Exported;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class KloReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<KloFile> highSeverities = new ArrayList<KloFile>(); // High severities = low errors
    private List<KloFile> lowSeverities = new ArrayList<KloFile>(); // Low severities = high errors
    private List<KloFile> all = new ArrayList<KloFile>();

    private double fixed = 0.0;
	private double existing = 0.0;
	private double neww = 0.0;

    public List<KloFile> getHighSeverities() {
        return highSeverities;
    }

    public void setHighSeverities(List<KloFile> highSeverities) {
        this.highSeverities = highSeverities;
    }

    public List<KloFile> getLowSeverities() {
        return lowSeverities;
    }

    public void setLowSeverities(List<KloFile> lowSeverities) {
        this.lowSeverities = lowSeverities;
    }

    @Exported
    public List<KloFile> getAllSeverities() {
        return all;
    }

    public void setErrors(List<KloFile> errors) {
        this.all = errors;
    }

    @Exported
    public int getNumberTotal() {
        return (all == null) ? 0 : all.size();
    }

    @Exported
    public int getNumberHighSeverities() {
        return (highSeverities == null) ? 0 : highSeverities.size();
    }

    @Exported
    public int getNumberLowSeverities() {
        return (lowSeverities == null) ? 0 : lowSeverities.size();
    }

    public void setFixed(double fixed)
    {
        this.fixed = fixed;
    }
    
    public double getFixed()
    {
        return fixed;
    }

    public void setExisting(double existing)
    {
        this.existing = existing;
    }

    public double getExisting()
    {
        return existing;
    }

    public void setNeww(double neww)
    {
        this.neww = neww;
    }

    public double getNeww()
    {
        return neww;
    }

}

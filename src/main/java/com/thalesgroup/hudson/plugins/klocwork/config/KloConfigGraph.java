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

package com.thalesgroup.hudson.plugins.klocwork.config;

import com.thalesgroup.hudson.plugins.klocwork.graph.KloGraph;

import java.io.Serializable;

public class KloConfigGraph implements Serializable {

    private int xSize = KloGraph.DEFAULT_CHART_WIDTH;
    private int ySize = KloGraph.DEFAULT_CHART_HEIGHT;

    private boolean displayAllError = true;
    private boolean displayHighSeverity = true;
    private boolean displayLowSeverity = true;

    public KloConfigGraph() {
    }

    public KloConfigGraph(int xSize, int ySize, boolean diplayAllError,
                          boolean displayHighSeverity, boolean displayLowSeverity) {
        super();
        this.xSize = xSize;
        this.ySize = ySize;
        this.displayAllError = diplayAllError;
        this.displayHighSeverity = displayHighSeverity;
        this.displayLowSeverity = displayLowSeverity;
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public boolean isDisplayAllError() {
        return displayAllError;
    }

    public boolean isDisplayHighSeverity() {
        return displayHighSeverity;
    }

    public boolean isDisplayLowSeverity() {
        return displayLowSeverity;
    }

}

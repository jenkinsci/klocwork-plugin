/*******************************************************************************
 * Copyright (c) 2011 Emenda Software Ltd.                                      *
 * Author : Jacob Larfors                                                       *
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

package com.thalesgroup.hudson.plugins.klocwork.graph;

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
import com.thalesgroup.hudson.plugins.klocwork.model.KloReport;
import hudson.util.Graph;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;

import java.awt.*;


public class KloPieChart extends Graph {

    private PieDataset dataset;
    private KloConfig kloConfig;
    private KloReport kloReport;

    public KloPieChart(PieDataset dataset, KloConfig kloConfig, KloReport kloReport,
                       int chartWidth, int chartHeight) {
        super(-1, chartWidth, chartHeight);
        this.dataset = dataset;
        this.kloConfig = kloConfig;
        this.kloReport = kloReport;
    }

    protected JFreeChart createGraph() {

        JFreeChart chart = ChartFactory.createPieChart(null, dataset, true, true, false);
        chart.setBackgroundPaint(Color.white);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setDataset(dataset);
        plot.setOutlinePaint(null);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("No Klocwork data found.");
        plot.setCircular(false);
        plot.setLabelGap(0.02);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{1}"));

        // Set colours
        //plot.setOutlinePaint("New", new Color(200, 0, 0));
        int i = 0;
        if (kloConfig.getBuildGraph().isNeww() && kloReport.getNeww() > 0) {
            plot.setSectionPaint(plot.getDataset().getKey(i), new Color(200, 0, 0));
            i++;
        }
        if (kloConfig.getBuildGraph().isExisting() && kloReport.getExisting() > 0) {
            plot.setSectionPaint(plot.getDataset().getKey(i), new Color(0, 0, 200));
            i++;
        }
        if (kloConfig.getBuildGraph().isFixed() && kloReport.getFixed() > 0) {
            plot.setSectionPaint(plot.getDataset().getKey(i), new Color(0, 200, 0));
        }

        //plot.setOutlinePaint("Existing", new Color(0, 0, 200));
        //plot.setOutlinePaint("Fixed", new Color(0, 200, 0));

        return chart;

    }

}
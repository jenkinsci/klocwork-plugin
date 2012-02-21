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

import hudson.model.AbstractBuild;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

public class KloBarChart extends Graph
{

	private final CategoryDataset categoryDataset;
	
	public KloBarChart(CategoryDataset categoryDataset, int chartWidth, int chartHeight)
	{
		super(-1, chartWidth, chartHeight);
		this.categoryDataset = categoryDataset;
	}

	/**
	 * Creates a Klocwork trend graph
	 * @return the JFreeChart graph object
	 */
	protected JFreeChart createGraph()
	{
		// Create BarChart
		final JFreeChart chart = ChartFactory.createBarChart(null,"Issue State","No. of Issues",categoryDataset,PlotOrientation.VERTICAL, true, false, false);

		final LegendTitle legendTitle = chart.getLegend();
		legendTitle.setPosition(RectangleEdge.RIGHT);
		// Set background of chart
		chart.setBackgroundPaint(Color.white);
		
		CategoryPlot plot = chart.getCategoryPlot();
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		// Set the color of each bar
		renderer.setSeriesPaint(0,gp0());
		renderer.setSeriesPaint(1,gp1());
		renderer.setSeriesPaint(2,gp2());
		renderer.setSeriesPaint(3,gp3());
		renderer.setSeriesPaint(4,gp4());
		renderer.setSeriesPaint(5,gp5());
		
		return chart;
	}
	
	// Generates the different GradientPaints
	GradientPaint gp0()
	{
		return new GradientPaint(
            0.0f, 0.0f, Color.red, 
            0.0f, 0.0f, new Color(100, 0, 0)
        );
	}
	GradientPaint gp1()
	{
		return new GradientPaint(
            0.0f, 0.0f, Color.green, 
            0.0f, 0.0f, new Color(0, 100, 0)
        );
	}
	GradientPaint gp2()
	{
		return new GradientPaint(
            0.0f, 0.0f, Color.blue, 
            0.0f, 0.0f, new Color(0, 0, 100)
        );
	}
	GradientPaint gp3()
	{
		return new GradientPaint(
            0.0f, 0.0f, Color.cyan, 
            0.0f, 0.0f, new Color(0, 200, 200)
        );
	}
	GradientPaint gp4()
	{
		return new GradientPaint(
            0.0f, 0.0f, Color.darkGray, 
            0.0f, 0.0f, new Color(100, 100, 100)
        );
	}
	GradientPaint gp5()
	{
		return new GradientPaint(
            0.0f, 0.0f, Color.yellow, 
            0.0f, 0.0f, new Color(200, 200, 0)
        );
	}
}

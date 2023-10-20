/*
 * Copyright 2023 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mapton.butterfly_topo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.mapton.api.ui.forms.ChartBuilder;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoChartBuilder extends ChartBuilder<BTopoControlPoint> {

    private ChartHelper mChartHelper = new ChartHelper();

    public TopoChartBuilder() {
    }

    @Override
    public Callable<ChartPanel> build(BTopoControlPoint p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            XYDataset dataset = createDataset2(p);
            JFreeChart chart = createChart(dataset);
            ChartPanel panel = new ChartPanel(chart);
            panel.setMouseZoomable(true, false);
            return panel;
        };

        return callable;
    }

    /**
     * Creates a sample chart.
     *
     * @param dataset a dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart(CategoryDataset dataset) {
// create the chart...
        JFreeChart chart = ChartFactory.createLineChart(
                "Java Standard Class Library", // chart title
                null, // domain axis label
                "Class Count", // range axis label
                dataset);
        chart.removeLegend();
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setRangePannable(true);
        plot.setRangeGridlinesVisible(false);
// customise the range axis...
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        ChartUtils.applyCurrentTheme(chart);
        chart.addSubtitle(new TextTitle("Number of Classes By Release"));
        TextTitle source = new TextTitle(
                "Sources: https://stackoverflow.com/q/3112882 "
                + "and Java In A Nutshell (5th Edition) by David Flanagan (O'Reilly)");
        source.setFont(new Font("SansSerif", Font.PLAIN, 10));
        source.setPosition(RectangleEdge.BOTTOM);
        source.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        chart.addSubtitle(source);
// customise the renderer...
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDrawOutlines(true);
        renderer.setUseFillPaint(true);
        renderer.setDefaultFillPaint(Color.WHITE);
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShape(0, new Ellipse2D.Double(-5.0, -5.0, 10.0, 10.0));
        return chart;
    }

    private JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Legal & General Unit Trust Prices", // title
                "Date", // x-axis label
                "Price Per Unit", // y-axis label
                dataset, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
        );
        chart.setBackgroundPaint(Color.white);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
        }
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));
        return chart;
    }

    private XYDataset createDataset2(BTopoControlPoint p) {
        TimeSeries s1 = new TimeSeries("L&G European Index Trust");
        p.ext().getObservationsFiltered().forEach(o -> {
            ZonedDateTime zdt = o.getDate().atZone(ZoneId.of("America/Los_Angeles"));

            long millis = zdt.toInstant().toEpochMilli();
//            DateHelper.toDateString(LocalDate.MAX)
            s1.add(mChartHelper.convertToMinute(o.getDate()), o.ext().getDeltaZ());
        });
//        s1.add(new Month(2, 2001), 181.8);
//        s1.add(new Month(3, 2001), 167.3);
//        s1.add(new Month(4, 2001), 153.8);
//        s1.add(new Month(5, 2001), 167.6);
//        s1.add(new Month(6, 2001), 158.8);
//        s1.add(new Month(7, 2001), 148.3);
//        s1.add(new Month(8, 2001), 153.9);
//        s1.add(new Month(9, 2001), 142.7);
//        s1.add(new Month(10, 2001), 123.2);
//        s1.add(new Month(11, 2001), 131.8);
//        s1.add(new Month(12, 2001), 139.6);
//        s1.add(new Month(1, 2002), 142.9);
//        s1.add(new Month(2, 2002), 138.7);
//        s1.add(new Month(3, 2002), 137.3);
//        s1.add(new Month(4, 2002), 143.9);
//        s1.add(new Month(5, 2002), 139.8);
//        s1.add(new Month(6, 2002), 137.0);
//        s1.add(new Month(7, 2002), 132.8);
//        TimeSeries s2 = new TimeSeries("L&G UK Index Trust");
//        s2.add(new Month(2, 2001), 129.6);
//        s2.add(new Month(3, 2001), 123.2);
//        s2.add(new Month(4, 2001), 117.2);
//        s2.add(new Month(5, 2001), 124.1);
//        s2.add(new Month(6, 2001), 122.6);
//        s2.add(new Month(7, 2001), 119.2);
//        s2.add(new Month(8, 2001), 116.5);
//        s2.add(new Month(9, 2001), 112.7);
//        s2.add(new Month(10, 2001), 101.5);
//        s2.add(new Month(11, 2001), 106.1);
//        s2.add(new Month(12, 2001), 110.3);
//        s2.add(new Month(1, 2002), 111.7);
//        s2.add(new Month(2, 2002), 111.0);
//        s2.add(new Month(3, 2002), 109.6);
//        s2.add(new Month(4, 2002), 113.2);
//        s2.add(new Month(5, 2002), 111.6);
//        s2.add(new Month(6, 2002), 108.8);
//        s2.add(new Month(7, 2002), 101.6);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);
//        dataset.addSeries(s2);
//        dataset.setDomainIsPointsInTime(true);
        return dataset;
    }
}

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
package org.mapton.butterfly_geo.inclinometer.chart;

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.EmptyBlock;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class InclinoChartBuilder extends XyzChartBuilder<BGeoInclinometerPoint> {

    private ChartPanel mChartPanel;
    private TextTitle mDateSubTextTitle;
    private TextTitle mDeltaSubTextTitle;

    public InclinoChartBuilder() {
        initChart();
    }

    private void initChart() {
        var plot = new CombinedDomainXYPlot(new DateAxis());
        plot.setGap(10.0);
        plot.setOrientation(PlotOrientation.VERTICAL);

        mChart = new JFreeChart("", plot);
        mChart.setBackgroundPaint(Color.white);
        mChart.getTitle().setBackgroundPaint(Color.LIGHT_GRAY);
        mChart.getTitle().setExpandToFitSpace(true);
        mChart.removeLegend();

        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        var itemRenderer = plot.getRenderer();
        if (itemRenderer instanceof XYLineAndShapeRenderer renderer) {
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
        }

        var dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
        dateAxis.setAutoRange(true);

        mChartPanel = new ChartPanel(mChart);
        mChartPanel.setMouseZoomable(true, false);
        mChartPanel.setDisplayToolTips(true);
        //        mChartPanel.setDomainZoomable(true);
        mChartPanel.setMouseWheelEnabled(false);

        var font = new Font("monospaced", Font.BOLD, SwingHelper.getUIScaled(12));
        mDateSubTextTitle = new TextTitle("", font, Color.BLACK, RectangleEdge.TOP, HorizontalAlignment.LEFT, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);
        mDeltaSubTextTitle = new TextTitle("", font, Color.BLACK, RectangleEdge.TOP, HorizontalAlignment.RIGHT, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);

        var blockContainer = new BlockContainer(new BorderArrangement());
        blockContainer.add(mDateSubTextTitle, RectangleEdge.LEFT);
        blockContainer.add(mDeltaSubTextTitle, RectangleEdge.RIGHT);
        blockContainer.add(new EmptyBlock(2000, 0));

        var compositeTitle = new CompositeTitle(blockContainer);
        compositeTitle.setPadding(new RectangleInsets(0, 20, 0, 20));
        mChart.addSubtitle(compositeTitle);
    }

    @Override
    public synchronized Callable<ChartPanel> build(BGeoInclinometerPoint p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);
            var plot = (CombinedDomainXYPlot) mChart.getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            //dateAxis.setRange(DateHelper.convertToDate(mTemporalManager.getLowDate()), DateHelper.convertToDate(mTemporalManager.getHighDate()));
            dateAxis.setAutoRange(true);
            plot.clearRangeMarkers();
            plotAlarmIndicators(p);

//            var rangeAxis = (NumberAxis) plot.getRangeAxis();
//            rangeAxis.setAutoRange(true);
//            rangeAxis.setRange(-0.050, +0.050);
            return mChartPanel;
        };

        return callable;
    }

    @Override
    public void setTitle(BGeoInclinometerPoint p) {
        mChart.setTitle(p.getName());
//        setTitle(p, InclinoHelper.getAlarmColorAwt(p));
//
//        var dateFirst = Objects.toString(DateHelper.toDateString(p.getDateZero()), "");
//        var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
//        var date = "(%s) → %s".formatted(dateFirst, dateLast);
//        getLeftSubTextTitle().setText(date);

//        var rightTitle = "%s: %s".formatted(p.getAlarm1Id(), p.ext().getDeltaZero());
//        getRightSubTextTitle().setText(rightTitle);
    }

    @Override
    public synchronized void updateDataset(BGeoInclinometerPoint ppp) {
        var plot = (CombinedDomainXYPlot) mChart.getPlot();
        plot.clearDomainMarkers();
        new ArrayList<>(plot.getSubplots()).stream().forEach(p -> plot.remove(p));

        var rangeMin = Double.MAX_VALUE;
        var rangeMax = Double.MIN_VALUE;
//        for (var p : ppp.getPoints()) {
//            double pMin = p.ext().getObservationsTimeFiltered().stream().mapToDouble(o -> o.ext().getDelta()).min().getAsDouble();
//            double pMax = p.ext().getObservationsTimeFiltered().stream().mapToDouble(o -> o.ext().getDelta()).max().getAsDouble();
//            rangeMin = Math.min(rangeMin, pMin);
//            rangeMax = Math.max(rangeMax, pMax);
//        }
        rangeMin = Math.min(rangeMin, 0);
        rangeMax = Math.max(rangeMax, 0.030);

        var depthToSeries = new LinkedHashMap<Double, TimeSeries>();

        for (var o : ppp.ext().getObservationsTimeFiltered()) {
            var minute = ChartHelper.convertToMinute(o.getDate());
            for (var oi : o.getObservationItems()) {
                var timeSeries = depthToSeries.computeIfAbsent(oi.getDown(), k -> new TimeSeries("%.1f".formatted(k)));
                timeSeries.add(minute, oi.getDistance());
            }
        }

        for (var entry : depthToSeries.entrySet()) {
//            var series=entry.getValue();
            var name = "%.1f".formatted(entry.getKey());
            var series = entry.getValue();
//        }
//        for (var depth : ppp.ext().getDepths()) {
//            for (var o : ppp.ext().getObservationsTimeFiltered()) {
//                o.getObservationItems();
//            }
//        }
//
//        for (var p : ppp.getPoints()) {
//            var name = p.getName();
//            name = StringUtils.removeStartIgnoreCase(name, ppp.getName());
//            name = StringUtils.removeStartIgnoreCase(name, "-");
//            var series = new TimeSeries(name);
//            for (var o : p.ext().getObservationsTimeFiltered()) {
//                var minute = ChartHelper.convertToMinute(o.getDate());
//                series.add(minute, o.ext().getDelta());
//            }

            var timeSeriesCollection = new TimeSeriesCollection(series);
            var renderer = new StandardXYItemRenderer();
            var rangeAxis = new NumberAxis(name);
            var subplot = new XYPlot(timeSeriesCollection, null, rangeAxis, renderer);
            subplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
            subplot.setBackgroundPaint(Color.lightGray);
            subplot.setDomainGridlinePaint(Color.white);
            subplot.setRangeGridlinePaint(Color.white);
//            subplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
            subplot.setDomainCrosshairVisible(true);
            subplot.setRangeCrosshairVisible(true);
            rangeAxis.setRange(rangeMin, rangeMax);
            subplot.getRangeAxis().setLabelFont(new Font(Font.SANS_SERIF, Font.BOLD, SwingHelper.getUIScaled(12)));
            renderer.setSeriesPaint(timeSeriesCollection.getSeriesIndex(series.getKey()), Color.RED);

            plot.add(subplot, 1);
        }
    }

}

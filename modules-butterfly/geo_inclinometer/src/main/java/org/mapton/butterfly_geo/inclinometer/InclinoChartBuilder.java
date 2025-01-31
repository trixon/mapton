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
package org.mapton.butterfly_geo.inclinometer;

import java.awt.BasicStroke;
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
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.LengthAdjustmentType;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.api.ui.forms.ChartBuilder;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.CircularInt;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class InclinoChartBuilder extends ChartBuilder<BGeoInclinometerPoint> {

    private final ChartHelper mChartHelper = new ChartHelper();
    private final CircularInt mColorCircularInt = new CircularInt(0, 5);
    private final XYLineAndShapeRenderer mSecondaryRenderer = new XYLineAndShapeRenderer();
    private final NumberAxis mTemperatureAxis = new NumberAxis("°C");
    private final TimeSeriesCollection mTemperatureDataset = new TimeSeriesCollection();
    private final TimeSeries mTimeSeriesTemperature = new TimeSeries("°C");
    private final TimeSeries mTimeSeriesZ = new TimeSeries("Δ µε");
    private JFreeChart mChart;
    private ChartPanel mChartPanel;
    private TextTitle mDateSubTextTitle;
    private TextTitle mDeltaSubTextTitle;

    public InclinoChartBuilder() {
        initChart();
//        initChart("mm", null);
//
//        var plot = (CombinedDomainXYPlot) mChart.getPlot();
//        plot.setRangeAxis(2, mTemperatureAxis);
//        plot.setDataset(2, mTemperatureDataset);
//        plot.mapDatasetToRangeAxis(2, 2);
//        plot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_RIGHT);
//        plot.setRenderer(2, mSecondaryRenderer);
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
            var minute = mChartHelper.convertToMinute(o.getDate());
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
//                var minute = mChartHelper.convertToMinute(o.getDate());
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

    public synchronized void updateDatasetXX(BGeoInclinometerPoint p) {
//        getDataset().removeAllSeries();
        mTimeSeriesZ.clear();

        mTemperatureDataset.removeAllSeries();
        mTimeSeriesTemperature.clear();

        var plot = (XYPlot) mChart.getPlot();
        plot.clearDomainMarkers();

//        updateDatasetTemperature(p);
        var depthToSeries = new LinkedHashMap<Double, TimeSeries>();

        updateDatasetXX(p, Color.RED, true);
        mColorCircularInt.set(0);

        for (var o : p.ext().getObservationsTimeFiltered()) {
            var minute = mChartHelper.convertToMinute(o.getDate());
            for (var oi : o.getObservationItems()) {
                var timeSeries = depthToSeries.computeIfAbsent(oi.getDown(), k -> new TimeSeries("%.1f".formatted(k)));
                double value = oi.getDown() + oi.getDistance() * 100;
                timeSeries.addOrUpdate(minute, value);
            }
        }

        for (var timeSeries : depthToSeries.values()) {
//            getDataset().addSeries(timeSeries);
        }

//        mManager.getTimeFilteredItems().stream()
//                .filter(pp -> {
//                    return Math.hypot(pp.getZeroX() - p.getZeroX(), pp.getZeroY() - p.getZeroY()) < 1.0;
//                })
//                .filter(pp -> pp != p)
//                .forEach(pp -> {
//                    updateDataset(pp, getColor(), false);
//                });
    }

    private Color getColor() {
        var colors = new Color[]{
            Color.BLUE,
            Color.CYAN,
            Color.MAGENTA,
            Color.YELLOW,
            Color.GREEN,
            Color.ORANGE};

        return colors[mColorCircularInt.inc()];
    }

    private void plotAlarmIndicator(BComponent component, double value, Color color) {
        var marker = new ValueMarker(value);
        float width = 1.0f;
        float dash[] = {5.0f, 5.0f};
        var dashedStroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.5f, dash, 0);
        var stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.5f, null, 0);
        if (component == BComponent.HEIGHT) {
            marker.setStroke(dashedStroke);
        } else {
            marker.setStroke(stroke);
        }
        marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        marker.setPaint(color);

        var plot = (XYPlot) mChart.getPlot();
        plot.addRangeMarker(marker);
    }

    private void plotAlarmIndicators(BGeoInclinometerPoint p) {
        var ha = p.ext().getAlarm(BComponent.HEIGHT);
        if (ha != null) {
            var range0 = ha.ext().getRange0();
            if (range0 != null) {
                plotAlarmIndicator(BComponent.HEIGHT, range0.getMinimum(), Color.YELLOW);
                plotAlarmIndicator(BComponent.HEIGHT, range0.getMaximum(), Color.YELLOW);
            }

            var range1 = ha.ext().getRange1();
            if (range1 != null) {
                plotAlarmIndicator(BComponent.HEIGHT, range1.getMinimum(), Color.RED);
                plotAlarmIndicator(BComponent.HEIGHT, range1.getMaximum(), Color.RED);
            }
        }
    }

    private void updateDatasetXX(BGeoInclinometerPoint p, Color color, boolean plotZeroAndReplacement) {
        var plot = (XYPlot) mChart.getPlot();
        var timeSeries = new TimeSeries(p.getName());

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            var minute = mChartHelper.convertToMinute(o.getDate());
//            if (plotZeroAndReplacement) {
//                if (o.isReplacementMeasurement()) {
//                    addMarker(plot, minute, "E", Color.RED);
//                } else if (o.isZeroMeasurement()) {
//                    addMarker(plot, minute, "N", Color.BLUE);
//                }
//            }

            if (o.ext().getDeltaZ() != null) {
                timeSeries.addOrUpdate(minute, o.ext().getDeltaZ() * 1000);
            }
        });

        var renderer = plot.getRenderer();

//        getDataset().addSeries(timeSeries);
//        renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries.getKey()), color);
    }

    private void updateDatasetTemperature(BGeoInclinometerPoint p) {
        p.ext().getObservationsTimeFiltered().forEach(o -> {
            var minute = mChartHelper.convertToMinute(o.getDate());
//            if (MathHelper.isBetween(-40d, +40d, o.getA())) {
//                mTimeSeriesTemperature.addOrUpdate(minute, o.getA());
//            }
        });

        if (!mTimeSeriesTemperature.isEmpty()) {
            mTemperatureDataset.addSeries(mTimeSeriesTemperature);
            mSecondaryRenderer.setSeriesPaint(mTemperatureDataset.getSeriesIndex(mTimeSeriesTemperature.getKey()), Color.GRAY);
        }
    }
}

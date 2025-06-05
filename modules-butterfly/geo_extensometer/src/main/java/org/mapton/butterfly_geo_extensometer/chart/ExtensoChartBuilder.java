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
package org.mapton.butterfly_geo_extensometer.chart;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
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
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.api.ui.forms.ChartBuilder;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ExtensoChartBuilder extends ChartBuilder<BGeoExtensometer> {

    private JFreeChart mChart;
    private ChartPanel mChartPanel;
    private final boolean mCompleteView;
    private TextTitle mDateSubTextTitle;
    private TextTitle mDeltaSubTextTitle;
    private final Integer mRecentDays;

    public ExtensoChartBuilder(Integer recentDays) {
        mCompleteView = recentDays == null;
        mRecentDays = recentDays;

        initChart();
    }

    @Override
    public synchronized Callable<ChartPanel> build(BGeoExtensometer p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);
            var plot = (CombinedDomainXYPlot) mChart.getPlot();

            plot.clearRangeMarkers();

            return mChartPanel;
        };

        return callable;
    }

    @Override
    public void setTitle(BGeoExtensometer p) {
        mChart.setTitle(mCompleteView ? p.getName() : "Senaste %d dygnen".formatted(mRecentDays));
    }

    @Override
    public void updateDataset(BGeoExtensometer extenso) {
        var plot = (CombinedDomainXYPlot) mChart.getPlot();
        plot.clearDomainMarkers();
        new ArrayList<>(plot.getSubplots()).stream().forEach(p -> plot.remove(p));
        var startDate = mCompleteView ? LocalDateTime.MIN : LocalDateTime.now().minusDays(mRecentDays);

        if (extenso.ext().getReferencePoint() != null) {
            var p = extenso.ext().getReferencePoint();
            var name = p.getName();
            name = StringUtils.removeStartIgnoreCase(name, extenso.getName());
            name = StringUtils.removeStartIgnoreCase(name, "-");
            var series = new TimeSeries(name);
            for (var o : p.ext().getObservationsTimeFiltered()) {
                if (o.getDate().isAfter(startDate)) {
                    var minute = ChartHelper.convertToMinute(o.getDate());
                    series.addOrUpdate(minute, o.ext().getDelta() * 1000);
                }
            }

            var timeSeriesCollection = new TimeSeriesCollection(series);
            var renderer = new XYLineAndShapeRenderer(true, true);
            var rangeAxis = new NumberAxis(mCompleteView ? name : "");
            var subplot = new XYPlot(timeSeriesCollection, null, rangeAxis, renderer);
            subplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
            subplot.setBackgroundPaint(Color.lightGray);
            subplot.setDomainGridlinePaint(Color.white);
            subplot.setRangeGridlinePaint(Color.white);
            subplot.setDomainCrosshairVisible(true);
            subplot.setRangeCrosshairVisible(true);
            rangeAxis.setRange(-5, 5);

            subplot.getRangeAxis().setLabelFont(new Font(Font.DIALOG, Font.BOLD, SwingHelper.getUIScaled(10)));
            var seriesIndex = timeSeriesCollection.getSeriesIndex(series.getKey());
            renderer.setSeriesPaint(seriesIndex, Color.RED);
            renderer.setDefaultShape(new java.awt.Rectangle(50, 50));
            var shapeSize = SwingHelper.getUIScaled(6);
            var shape = new Ellipse2D.Double(-shapeSize / 2, -shapeSize / 2, shapeSize, shapeSize);
            renderer.setSeriesShape(seriesIndex, shape);
            renderer.setSeriesShapesVisible(seriesIndex, true);

            for (var o : p.ext().getObservationsTimeFiltered()) {
                XyzChartBuilder.addNEMarkers2(plot, o, true);
            }

            plot.add(subplot, 1);
        }

        var rangeMin = Double.MAX_VALUE;
        var rangeMax = Double.MIN_VALUE;
        for (var p : extenso.getPoints()) {
            double pMin = p.ext().getObservationsTimeFiltered().stream()
                    .filter(o -> o.getDate().isAfter(startDate))
                    .mapToDouble(o -> o.ext().getDelta())
                    .min()
                    .orElse(0.0);
            double pMax = p.ext().getObservationsTimeFiltered().stream()
                    .filter(o -> o.getDate().isAfter(startDate))
                    .filter(o -> o.ext().getDelta() != null)
                    .mapToDouble(o -> o.ext().getDelta())
                    .max()
                    .orElse(0.0);
            rangeMin = Math.min(rangeMin, pMin);
            rangeMax = Math.max(rangeMax, pMax);
        }

        for (var p : extenso.getPoints()) {
            var name = p.getName();
            name = StringUtils.removeStartIgnoreCase(name, extenso.getName());
            name = StringUtils.removeStartIgnoreCase(name, "-");
            var series = new TimeSeries(name);
            p.ext().getObservationsTimeFiltered().stream()
                    .filter(o -> o.getDate().isAfter(startDate))
                    .forEachOrdered(o -> {
                        var minute = ChartHelper.convertToMinute(o.getDate());
                        series.addOrUpdate(minute, o.ext().getDelta());
                    });

            var timeSeriesCollection = new TimeSeriesCollection(series);
            var renderer = new StandardXYItemRenderer();
            var rangeAxis = new NumberAxis(mCompleteView ? name : "");
            var subplot = new XYPlot(timeSeriesCollection, null, rangeAxis, renderer);
            subplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
            subplot.setBackgroundPaint(Color.lightGray);
            subplot.setDomainGridlinePaint(Color.white);
            subplot.setRangeGridlinePaint(Color.white);
            subplot.setDomainCrosshairVisible(true);
            subplot.setRangeCrosshairVisible(true);

            var dateAxis = (DateAxis) plot.getDomainAxis();
            var now = LocalDate.now();
            var nowAsDate = DateHelper.convertToDate(now);

            if (mCompleteView) {
                dateAxis.setRange(DateHelper.convertToDate(p.ext().getDateFirst()), nowAsDate);
                rangeAxis.setRange(rangeMin, rangeMax);
            } else {
                dateAxis.setRange(DateHelper.convertToDate(now.minusDays(mRecentDays)), nowAsDate);
                rangeAxis.setAutoRange(true);
                rangeAxis.setAutoRangeIncludesZero(false);
                if (!series.getItems().isEmpty()) {
                    var span = "  %.2f  ".formatted(rangeAxis.getRange().getLength());
                    var minute = ChartHelper.convertToMinute(startDate);
                    var marker = new ValueMarker(minute.getFirstMillisecond());
                    marker.setLabel(span);
                    marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                    marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
                    marker.setLabelFont(new Font(Font.DIALOG, Font.BOLD, SwingHelper.getUIScaled(10)));
                    marker.setLabelBackgroundColor(Color.BLACK);
                    marker.setLabelPaint(Color.YELLOW);
                    marker.setPaint(plot.getBackgroundPaint());
                    subplot.addDomainMarker(marker);
                }
            }
            subplot.getRangeAxis().setLabelFont(new Font(Font.DIALOG, Font.BOLD, SwingHelper.getUIScaled(12)));
            renderer.setSeriesPaint(timeSeriesCollection.getSeriesIndex(series.getKey()), Color.RED);
            var plotBlastLabels = p == extenso.getPoints().getFirst();// && extenso.ext().getReferencePoint() == null;
            XyzChartBuilder.plotBlasts(subplot, extenso, p.ext().getObservationFilteredFirstDate(), p.ext().getObservationFilteredLastDate(), plotBlastLabels);
            XyzChartBuilder.plotMeasNeed(subplot, p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));

            for (var o : p.ext().getObservationsTimeFiltered()) {
                XyzChartBuilder.addNEMarkers2(plot, o, true);
            }

            plot.add(subplot, 1);
        }
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

        mChartPanel = new ChartPanel(mChart);
        mChartPanel.setMouseZoomable(true, false);
        mChartPanel.setDisplayToolTips(true);
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
}

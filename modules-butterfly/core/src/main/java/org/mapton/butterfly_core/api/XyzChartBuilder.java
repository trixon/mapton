/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.EmptyBlock;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.time.Minute;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.api.MLatLon;
import org.mapton.api.ui.forms.ChartBuilder;
import org.mapton.butterfly_format.types.BBaseControlPoint;
import org.mapton.butterfly_format.types.BBaseControlPointObservation;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class XyzChartBuilder<T extends BBaseControlPoint> extends ChartBuilder<T> {

    protected JFreeChart mChart;
    protected Date mDateEnd;
    protected Date mDateNull;
    private ChartPanel mChartPanel;
    private final TimeSeriesCollection mDataset = new TimeSeriesCollection();
    private TextTitle mLeftSubTextTitle;
    private TextTitle mRightSubTextTitle;

    public static void addMarker(XYPlot plot, Minute minute, String string, Color color) {
        var marker = new ValueMarker(minute.getFirstMillisecond());
        marker.setPaint(color);
        marker.setLabel(string);
        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        plot.addDomainMarker(marker);
    }

    public static void addNEMarkers2(XYPlot plot, BBaseControlPointObservation o, boolean doPlot) {
        if (!doPlot) {
            return;
        }
        var minute = ChartHelper.convertToMinute(o.getDate());
        if (o.isReplacementMeasurement()) {
            addMarker(plot, minute, "E", Color.RED);
        } else if (o.isZeroMeasurement()) {
//            mDateNull = DateHelper.convertToDate(o.getDate());
            addMarker(plot, minute, "N", Color.BLUE);
        }
    }

    public static void plotBlasts(XYPlot plot, BBasePoint p, LocalDate firstDate, LocalDate lastDate) {
        plotBlasts(plot, p, firstDate, lastDate, true);
    }

    public static void plotBlasts(XYPlot plot, BBasePoint p, LocalDate firstDate, LocalDate lastDate, boolean plotLabel) {
        var distanceLimitDefault = 40.0;
        if (p instanceof BXyzPoint xyz && xyz.ext() instanceof BXyzPoint.Ext<? extends BXyzPointObservation> ext && ext.getFrequenceIntenseBuffer() != null) {
            distanceLimitDefault = ext.getFrequenceIntenseBuffer();
        }
        var distanceLimit = distanceLimitDefault;
        var currentStroke = new BasicStroke(4f);
        var otherStroke = new BasicStroke(1.2f);
        var pointLatLon = new MLatLon(p.getLat(), p.getLon());

        ButterflyManager.getInstance().getButterfly().noise().getBlasts().stream()
                .filter(b -> {
                    return DateHelper.isBetween(
                            firstDate,
                            lastDate,
                            b.getDateLatest().toLocalDate());
                })
                .forEachOrdered(b -> {
                    var blastLatLon = new MLatLon(b.getLat(), b.getLon());
                    var distance = blastLatLon.distance(pointLatLon);

                    if (distance <= distanceLimit) {
                        var minute = ChartHelper.convertToMinute(b.getDateLatest());
                        var marker = new ValueMarker(minute.getFirstMillisecond());
                        Color color;

                        if (b == p) {
                            color = Color.RED;
                            marker.setStroke(currentStroke);
                        } else {
                            var distanceQuota = (distanceLimit - distance) / (distanceLimit - 10.0);
                            marker.setStroke(otherStroke);
                            distanceQuota = Math.min(1, distanceQuota);
                            int alpha = (int) (Math.max(distanceQuota, 0.25) * 255d);
                            color = new Color(0, 0, 255, alpha);
                            if (plotLabel) {
                                marker.setLabel("%.0f".formatted(distance));
                                marker.setLabelFont(new Font("Dialog", Font.PLAIN, SwingHelper.getUIScaled(10)));
                                marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                                marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
                            }
                        }
                        marker.setPaint(color);
                        plot.addDomainMarker(marker);
                    }
                });
    }

    public static void plotMeasNeed(XYPlot plot, BBaseControlPoint p, long days) {
        if (p.getFrequency() > 0 && days < 0) {
            var ldt = LocalDateTime.now().plusDays(days);
            var minute = ChartHelper.convertToMinute(ldt);
            var marker = new ValueMarker(minute.getFirstMillisecond());
            marker.setPaint(Color.ORANGE);
            marker.setLabelPaint(Color.BLACK);
            marker.setLabelBackgroundColor(Color.ORANGE);
            marker.setLabel(ldt.toLocalDate().toString());
            float[] dashPattern = {15f, 15f};
            marker.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dashPattern, 0f));
            marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
            marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
            marker.setLabelFont(new Font(Font.DIALOG, Font.BOLD, SwingHelper.getUIScaled(11)));

            plot.addDomainMarker(marker);
        }
    }

    public void addNEMarkers(XYPlot plot, BBaseControlPointObservation o, boolean doPlot) {
        if (!doPlot) {
            return;
        }
        var minute = ChartHelper.convertToMinute(o.getDate());
        if (o.isReplacementMeasurement()) {
            addMarker(plot, minute, "E", Color.RED);
        } else if (o.isZeroMeasurement()) {
            mDateNull = DateHelper.convertToDate(o.getDate());
            addMarker(plot, minute, "N", Color.BLUE);
        }
    }

    public void clear(TimeSeries... series) {
        for (var timeSerie : series) {
            timeSerie.clear();
        }
    }

    public TimeSeries createSubSetMovingAverage(TimeSeries timeSeries, Minute start, Minute end, String name, int periodCount, int skip) {
        try {
            return MovingAverage.createMovingAverage(timeSeries.createCopy(start, end), name, periodCount, skip);
        } catch (CloneNotSupportedException ex) {
//            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public ChartPanel getChartPanel() {
        return mChartPanel;
    }

    public TimeSeriesCollection getDataset() {
        return mDataset;
    }

    public TextTitle getLeftSubTextTitle() {
        return mLeftSubTextTitle;
    }

    public TextTitle getRightSubTextTitle() {
        return mRightSubTextTitle;
    }

    public void resetPlot(XYPlot plot) {
        getDataset().removeAllSeries();
        plot.clearDomainMarkers();
        plot.clearAnnotations();
    }

    public void setDateRangeNullLast(XYPlot plot, BBaseControlPoint p, Date dateNull, Date dateEnd) {
        try {
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setAutoRange(true);
            dateAxis.setRange(dateNull, dateEnd);
        } catch (IllegalArgumentException e) {
            System.out.println("%s: Bad chart plot range".formatted(p.getName()));
        }
    }

    public void setDateRangeNullNow(XYPlot plot, BBaseControlPoint p, Date dateNull) {
        try {
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setAutoRange(true);
            dateAxis.setRange(dateNull, DateHelper.convertToDate(LocalDate.now()));
        } catch (IllegalArgumentException e) {
            System.out.println("%s: Bad chart plot range".formatted(p.getName()));
        }
    }

    @Override
    public void setTitle(T p) {
        mChart.setTitle(p.getName());
    }

    public void setTitle(T p, Color color) {
        mChart.setTitle(p.getName());
        if (color == Color.RED || color == Color.GREEN) {
            color = color.darker();
        }
        mChart.getTitle().setPaint(color);
    }

    protected void initChart(String valueAxisLabel, String decimalPattern) {
        mChart = ChartFactory.createTimeSeriesChart(
                "",
                Dict.DATE.toString(),
                Objects.toString(valueAxisLabel, "m"),
                mDataset,
                true,
                true,
                false
        );

        mChart.setBackgroundPaint(Color.white);
        mChart.getTitle().setBackgroundPaint(Color.LIGHT_GRAY);
        mChart.getTitle().setExpandToFitSpace(true);
        mChart.getTitle().setFont(mChart.getTitle().getFont().deriveFont((float) SwingHelper.getUIScaled(13.0)));
        var plot = (XYPlot) mChart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        var yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setNumberFormatOverride(new DecimalFormat(Objects.toString(decimalPattern, "0.000")));

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
        mLeftSubTextTitle = new TextTitle("", font, Color.BLACK, RectangleEdge.TOP, HorizontalAlignment.LEFT, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);
        mRightSubTextTitle = new TextTitle("", font, Color.BLACK, RectangleEdge.TOP, HorizontalAlignment.RIGHT, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);

        var blockContainer = new BlockContainer(new BorderArrangement());
        blockContainer.add(mLeftSubTextTitle, RectangleEdge.LEFT);
        blockContainer.add(mRightSubTextTitle, RectangleEdge.RIGHT);
        blockContainer.add(new EmptyBlock(2000, 0));

        var compositeTitle = new CompositeTitle(blockContainer);
        compositeTitle.setPadding(new RectangleInsets(0, 20, 0, 20));
        mChart.addSubtitle(compositeTitle);
    }

}

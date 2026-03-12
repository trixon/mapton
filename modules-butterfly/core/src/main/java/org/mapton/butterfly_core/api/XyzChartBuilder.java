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
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.numbers.core.Precision;
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
import org.jfree.chart.ui.LengthAdjustmentType;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.time.Minute;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.api.MChartOverlay;
import org.mapton.api.ui.forms.ChartBuilder;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BBaseControlPoint;
import org.mapton.butterfly_format.types.BBaseControlPointObservation;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.MinMaxCollection;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class XyzChartBuilder<T extends BBaseControlPoint> extends ChartBuilder<T> {

    private static final double sMinDefaultValue = 0.00001d;

    protected JFreeChart mChart;
    protected Date mDateEnd;
    protected Date mDateNull;
    protected final MinMaxCollection mMinMaxCollection = new MinMaxCollection();
    private ChartPanel mChartPanel;
    private final TimeSeriesCollection mDataset = new TimeSeriesCollection();
    private Date mDefaultDate;
    private TextTitle mLeftSubTextTitle;
    private Integer mRecentDays;
    private Integer mRecentDaysDefault;
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

    public static void plotOverlays(XYPlot plot, BBasePoint p, LocalDate aStartDate) {
//    public static void plotOverlays(XYPlot plot, BBasePoint p, LocalDate aStartDate, Class<? extends BChartOverlay>... excludedOverlays) {
//        var excludedOverlaysSet = new HashSet();
//        if (excludedOverlays != null) {
//            Collections.addAll(excludedOverlaysSet, excludedOverlays);
//        }

        Lookup.getDefault().lookupAll(BChartOverlay.class).stream()
                //                .filter(o -> !excludedOverlaysSet.contains(o.getClass()))
                .sorted(Comparator.comparingInt(MChartOverlay::getPosition))
                .forEach(chartOverlay -> {
                    try {
                        chartOverlay.plot(plot, p, aStartDate);
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                });
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
        } catch (CloneNotSupportedException | IllegalArgumentException ex) {
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

    public Integer getRecentDays() {
        return mRecentDays;
    }

    public Integer getRecentDaysDefault() {
        return mRecentDaysDefault;
    }

    public TextTitle getRightSubTextTitle() {
        return mRightSubTextTitle;
    }

    public boolean isCompleteView() {
        return mRecentDaysDefault == null;
    }

    public void plotAlarmIndicator(BComponent component, double value, Color color) {
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

    public void plotAlarmIndicators(BXyzPoint p) {
        plotAlarmIndicators(p, 1);
    }

    public void plotAlarmIndicators(BXyzPoint p, int factor) {
        if (p.ext() instanceof BXyzPoint.Ext<? extends BXyzPointObservation> ext) {
            var ha = ext.getAlarm(BComponent.HEIGHT);
            var color1 = Color.YELLOW;
            var color2 = Color.RED;
            var color3 = Color.decode("#800000");

            if (ha != null) {
                var range0 = ha.ext().getRange0();
                if (range0 != null) {
                    plotAlarmIndicator(BComponent.HEIGHT, range0.getMinimum() * factor, color1);
                    plotAlarmIndicator(BComponent.HEIGHT, range0.getMaximum() * factor, color1);
                }

                var range1 = ha.ext().getRange1();
                if (range1 != null) {
                    plotAlarmIndicator(BComponent.HEIGHT, range1.getMinimum() * factor, color2);
                    plotAlarmIndicator(BComponent.HEIGHT, range1.getMaximum() * factor, color2);
                }

                var range2 = ha.ext().getRange2();
                if (range2 != null) {
                    plotAlarmIndicator(BComponent.HEIGHT, range2.getMinimum() * factor, color3);
                    plotAlarmIndicator(BComponent.HEIGHT, range2.getMaximum() * factor, color3);
                }
            }

            var pa = ext.getAlarm(BComponent.PLANE);
            if (pa != null) {
                var range0 = pa.ext().getRange0();
                if (range0 != null) {
                    if (!Precision.equals(range0.getMinimum(), 0.0)) {
                        plotAlarmIndicator(BComponent.PLANE, range0.getMinimum() * factor, color1);
                    }
                    plotAlarmIndicator(BComponent.PLANE, range0.getMaximum() * factor, color1);
                }

                var range1 = pa.ext().getRange1();
                if (range1 != null) {
                    if (!Precision.equals(range1.getMinimum(), 0.0)) {
                        plotAlarmIndicator(BComponent.PLANE, range1.getMinimum() * factor, color2);
                    }
                    plotAlarmIndicator(BComponent.PLANE, range1.getMaximum() * factor, color2);
                }

                var range2 = pa.ext().getRange2();
                if (range2 != null) {
                    plotAlarmIndicator(BComponent.PLANE, range2.getMinimum() * factor, color3);
                    plotAlarmIndicator(BComponent.PLANE, range2.getMaximum() * factor, color3);
                }
            }
        }
    }

    public void resetPlot(XYPlot plot) {
        getDataset().removeAllSeries();
        plot.clearDomainMarkers();
        plot.clearAnnotations();
        mMinMaxCollection.reset();
    }

    public void setDateRangeNullLast(XYPlot plot, BBaseControlPoint p, Date dateNull, Date dateEnd) {
        try {
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setAutoRange(true);
            dateAxis.setRange(getNullSafeDate(dateNull), dateEnd);
        } catch (IllegalArgumentException e) {
            System.out.println("%s: Bad chart plot range".formatted(p.getName()));
        }
    }

    public void setDateRangeNullNow(XYPlot plot, BBaseControlPoint p, Date dateNull) {
        try {
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setAutoRange(true);
            dateAxis.setRange(getNullSafeDate(dateNull), DateHelper.convertToDate(LocalDate.now().plusDays(1)));
        } catch (IllegalArgumentException e) {
            System.out.println("%s: Bad chart plot range".formatted(p.getName()));
        }
    }

    public void setRange(double margin, BAlarm... alarms) {
        setRange(margin, 1.0, alarms);
    }

    public void setRange(double margin, double alarmFactor, BAlarm... alarms) {
        var alarmMinMax = AlarmHelper.getInstance().getMinMax(alarms);
        mMinMaxCollection.add(alarmMinMax.getX() * alarmFactor, alarmMinMax.getY() * alarmFactor);
        var plot = (XYPlot) mChart.getPlot();
        var rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRange(false);
        rangeAxis.setRange(getMinMaxMin() * margin, getMinMaxMax() * margin);
    }

    public void setRange() {
        setRange(1.0);
    }

    public void setRange(double margin) {
        var plot = (XYPlot) mChart.getPlot();
        var rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRange(false);
        rangeAxis.setRange(mMinMaxCollection.getMin() * margin, mMinMaxCollection.getMax() * margin);
    }

    public void setRecentDays(Integer recentDays) {
        mRecentDays = recentDays;
    }

    public void setRecentDaysDefault(Integer recentDaysDefault) {
        mRecentDaysDefault = recentDaysDefault;
    }

    @Override
    public void setTitle(T p) {
        mChart.setTitle(p.getName());
    }

    public void setTitle(T p, Color color) {
        setTitle(p.getName(), color);
    }

    public void setTitle(String title, Color color) {
        mChart.setTitle(isCompleteView() ? title : "Senaste %d dygnen".formatted(mRecentDays));
        if (color == Color.RED || color == Color.GREEN) {
            color = color.darker();
        }
        mChart.getTitle().setPaint(color);
    }

    protected void initChart(String valueAxisLabel, String decimalPattern) {
        mChart = ChartFactory.createTimeSeriesChart(
                "",
                "",
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

    private double getMinMaxMax() {
        return Math.max(sMinDefaultValue, mMinMaxCollection.getMax());
    }

    private double getMinMaxMin() {
        if (mMinMaxCollection.getMax() == sMinDefaultValue && mMinMaxCollection.getMin() == 0d) {
            return -sMinDefaultValue;
        } else {
            return mMinMaxCollection.getMin();
        }

    }

    private Date getNullSafeDate(Date date) {
        if (date == null) {
            if (mDefaultDate == null) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(2000, Calendar.JANUARY, 1);
                mDefaultDate = calendar.getTime();
            }
            return mDefaultDate;
        } else {
            return date;
        }
    }

}

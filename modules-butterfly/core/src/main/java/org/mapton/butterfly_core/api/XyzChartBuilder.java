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

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
import org.mapton.butterfly_format.types.BBasePoint;
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
    protected final ChartHelper mChartHelper = new ChartHelper();
    private ChartPanel mChartPanel;
    private final TimeSeriesCollection mDataset = new TimeSeriesCollection();
    private TextTitle mLeftSubTextTitle;
    private TextTitle mRightSubTextTitle;

    public void addMarker(XYPlot plot, Minute minute, String string, Color color) {
        var marker = new ValueMarker(minute.getFirstMillisecond());
        marker.setPaint(color);
        marker.setLabel(string);
        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        plot.addDomainMarker(marker);
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

    public void plotBlasts(XYPlot plot, BBasePoint p, LocalDate firstDate, LocalDate lastDate) {
        var extensometer = new MLatLon(p.getLat(), p.getLon());
        ButterflyManager.getInstance().getButterfly().noise().getBlasts().stream()
                .filter(b -> {
                    return DateHelper.isBetween(
                            firstDate,
                            lastDate,
                            b.getDateLatest().toLocalDate());
                })
                .forEachOrdered(b -> {
                    var blast = new MLatLon(b.getLat(), b.getLon());
                    var distance = blast.distance(extensometer);
                    if (distance <= 40.0) {
                        int alpha = (int) ((100d - distance) / 200d * 255d);
                        var color = new Color(0, 0, 255, alpha);
                        var minute = mChartHelper.convertToMinute(b.getDateLatest());
                        var marker = new ValueMarker(minute.getFirstMillisecond());
                        marker.setPaint(color);
                        plot.addDomainMarker(marker);
                    }
                });
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

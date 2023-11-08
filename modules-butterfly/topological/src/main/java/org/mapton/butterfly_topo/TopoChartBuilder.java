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
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
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
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.api.MTemporalManager;
import org.mapton.api.ui.forms.ChartBuilder;
import org.mapton.butterfly_alarm.api.AlarmHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoChartBuilder extends ChartBuilder<BTopoControlPoint> {

    private JFreeChart mChart;
    private final ChartHelper mChartHelper = new ChartHelper();
    private ChartPanel mChartPanel;
    private final TimeSeriesCollection mDataset = new TimeSeriesCollection();
    private TextTitle mDateSubTextTitle;
    private TextTitle mDeltaSubTextTitle;
    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private final TimeSeries mTimeSeries2d = new TimeSeries("2d");
    private final TimeSeries mTimeSeries3d = new TimeSeries("3d");
    private final TimeSeries mTimeSeriesE = new TimeSeries("E");
    private final TimeSeries mTimeSeriesH = new TimeSeries("H");
    private final TimeSeries mTimeSeriesN = new TimeSeries("N");

    public TopoChartBuilder() {
        initChart();
    }

    @Override
    public synchronized Callable<ChartPanel> build(BTopoControlPoint p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);
            var plot = (XYPlot) mChart.getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setRange(DateHelper.convertToDate(mTemporalManager.getLowDate()), DateHelper.convertToDate(mTemporalManager.getHighDate()));

            plot.clearRangeMarkers();
            plotAlarmIndicators(p);

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(false);
            rangeAxis.setRange(-0.050, +0.050);

            return mChartPanel;
        };

        return callable;
    }

    private void initChart() {
        mChart = ChartFactory.createTimeSeriesChart(
                "",
                Dict.DATE.toString(),
                "m",
                mDataset,
                true,
                true,
                false
        );

        mChart.setBackgroundPaint(Color.white);
        mChart.getTitle().setBackgroundPaint(Color.GRAY);

        var plot = (XYPlot) mChart.getPlot();
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
        dateAxis.setAutoRange(false);

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

    private void plotAlarmIndicators(BTopoControlPoint p) {
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

        var pa = p.ext().getAlarm(BComponent.PLANE);
        if (pa != null) {
            var range0 = pa.ext().getRange0();
            if (range0 != null) {
                plotAlarmIndicator(BComponent.PLANE, range0.getMinimum(), Color.YELLOW);
                plotAlarmIndicator(BComponent.PLANE, range0.getMaximum(), Color.YELLOW);
            }

            var range1 = pa.ext().getRange1();
            if (range1 != null) {
                plotAlarmIndicator(BComponent.PLANE, range1.getMinimum(), Color.RED);
                plotAlarmIndicator(BComponent.PLANE, range1.getMaximum(), Color.RED);
            }
        }
    }

    private void setTitle(BTopoControlPoint p) {
        mChart.setTitle(p.getName());
        mChart.getTitle().setPaint(TopoHelper.getAlarmColorAwt(p));
        var dateFirst = Objects.toString(DateHelper.toDateString(p.getDateZero()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        mDateSubTextTitle.setText(date);

        var sb = new StringBuilder();
        if (!StringUtils.isBlank(p.getNameOfAlarmHeight())) {
            sb.append("H ").append(p.getNameOfAlarmHeight());
            if (!StringUtils.isBlank(p.getNameOfAlarmPlane())) {
                sb.append(", ");
            }
        }

        if (!StringUtils.isBlank(p.getNameOfAlarmPlane())) {
            sb.append("P ").append(p.getNameOfAlarmPlane());
        }

        var alarmNames = sb.toString();

        String hAlarm = "";
        if (p.getDimension() != BDimension._2d) {
            hAlarm = "H " + AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p);
            if (p.getDimension() == BDimension._3d) {
                hAlarm = hAlarm + ", ";
            }
        }

        String pAlarm = "";
        if (p.getDimension() != BDimension._1d) {
            pAlarm = "P " + AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p);
        }

        String delta = p.ext().deltaZero().getDelta(3);

        var rightTitle = "%s%s: %s".formatted(hAlarm, pAlarm, delta);
        mDeltaSubTextTitle.setText(rightTitle);
    }

    private void updateDataset(BTopoControlPoint p) {
        mDataset.removeAllSeries();
        mTimeSeriesN.clear();
        mTimeSeriesE.clear();
        mTimeSeriesH.clear();
        mTimeSeries2d.clear();
        mTimeSeries3d.clear();

        var plot = (XYPlot) mChart.getPlot();
        plot.clearDomainMarkers();

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            var minute = mChartHelper.convertToMinute(o.getDate());
            if (o.isReplacementMeasurement()) {
                var marker = new ValueMarker(minute.getFirstMillisecond());
                marker.setPaint(Color.RED);
                marker.setLabel("E");
                marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
                marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
                plot.addDomainMarker(marker);
            } else if (o.isZeroMeasurement()) {
                var marker = new ValueMarker(minute.getFirstMillisecond());
                marker.setPaint(Color.BLUE);
                marker.setLabel("N");
                marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
                marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
                plot.addDomainMarker(marker);
            }

            if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
                mTimeSeriesH.add(minute, o.ext().getDeltaZ());
            }
            if (p.getDimension() == BDimension._2d || p.getDimension() == BDimension._3d) {
                mTimeSeriesN.add(minute, o.ext().getDeltaY());
                mTimeSeriesE.add(minute, o.ext().getDeltaX());
                mTimeSeries2d.add(minute, o.ext().getDelta2d());
            }
            if (p.getDimension() == BDimension._3d) {
                mTimeSeries3d.add(minute, o.ext().getDelta3d());
            }
        });

        if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
            mDataset.addSeries(mTimeSeriesH);
        }

        if (p.getDimension() == BDimension._2d || p.getDimension() == BDimension._3d) {
            //mDataset.addSeries(mTimeSeriesN);
            //mDataset.addSeries(mTimeSeriesE);
            mDataset.addSeries(mTimeSeries2d);
        }

        if (p.getDimension() == BDimension._3d) {
            mDataset.addSeries(mTimeSeries3d);
        }
    }
}

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
package org.mapton.butterfly_topo.grade;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.concurrent.Callable;
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
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.api.MTemporalManager;
import org.mapton.api.ui.forms.ChartBuilder;
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GradeChartBuilder extends ChartBuilder<BTopoGrade> {

    private JFreeChart mChart;
    private final ChartHelper mChartHelper = new ChartHelper();
    private ChartPanel mChartPanel;
    private final TimeSeriesCollection mDataset = new TimeSeriesCollection();
    private TextTitle mDateSubTextTitle;
    private TextTitle mDeltaSubTextTitle;
    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private final TimeSeries mTimeSeriesV = new TimeSeries(Dict.Geometry.VERTICAL.toString());
    private final TimeSeries mTimeSeriesH = new TimeSeries(Dict.Geometry.HORIZONTAL.toString());

    public GradeChartBuilder() {
        initChart();
    }

    @Override
    public synchronized Callable<ChartPanel> build(BTopoGrade p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);
            var plot = (XYPlot) mChart.getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setAutoRange(true);

            plot.clearRangeMarkers();
            //plotAlarmIndicators(p);

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);

            return mChartPanel;
        };

        return callable;
    }

    private void initChart() {
        mChart = ChartFactory.createTimeSeriesChart(
                "",
                Dict.DATE.toString(),
                "mm/m",
                mDataset,
                true,
                true,
                false
        );

        mChart.setBackgroundPaint(Color.white);
        mChart.getTitle().setBackgroundPaint(Color.LIGHT_GRAY);
        mChart.getTitle().setExpandToFitSpace(true);

        var plot = (XYPlot) mChart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        var yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setNumberFormatOverride(new DecimalFormat("0.0"));

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

//    private void plotAlarmIndicators(BTopoGrade p) {
//        var ha = p.ext().getAlarm(BComponent.HEIGHT);
//        if (ha != null) {
//            var range0 = ha.ext().getRange0();
//            if (range0 != null) {
//                plotAlarmIndicator(BComponent.HEIGHT, range0.getMinimum(), Color.YELLOW);
//                plotAlarmIndicator(BComponent.HEIGHT, range0.getMaximum(), Color.YELLOW);
//            }
//
//            var range1 = ha.ext().getRange1();
//            if (range1 != null) {
//                plotAlarmIndicator(BComponent.HEIGHT, range1.getMinimum(), Color.RED);
//                plotAlarmIndicator(BComponent.HEIGHT, range1.getMaximum(), Color.RED);
//            }
//        }
//
//        var pa = p.ext().getAlarm(BComponent.PLANE);
//        if (pa != null) {
//            var range0 = pa.ext().getRange0();
//            if (range0 != null) {
//                if (!Precision.equals(range0.getMinimum(), 0.0)) {
//                    plotAlarmIndicator(BComponent.PLANE, range0.getMinimum(), Color.YELLOW);
//                }
//                plotAlarmIndicator(BComponent.PLANE, range0.getMaximum(), Color.YELLOW);
//            }
//
//            var range1 = pa.ext().getRange1();
//            if (range1 != null) {
//                if (!Precision.equals(range1.getMinimum(), 0.0)) {
//                    plotAlarmIndicator(BComponent.PLANE, range1.getMinimum(), Color.RED);
//                }
//                plotAlarmIndicator(BComponent.PLANE, range1.getMaximum(), Color.RED);
//            }
//        }
//    }
    @Override
    public void setTitle(BTopoGrade p) {
        mChart.setTitle(p.getName());
        var color = Color.BLUE;
//        Color color = TopoHelper.getAlarmColorAwt(p);
//        if (color == Color.RED || color == Color.GREEN) {
//            color = color.darker();
//        }
        mChart.getTitle().setPaint(color);
        var dateFirst = Objects.toString(DateHelper.toDateString(p.getFirstDate()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.getLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        mDateSubTextTitle.setText(date);

//        var sb = new StringBuilder();
//        if (!StringUtils.isBlank(p.getNameOfAlarmHeight())) {
//            sb.append("H ").append(p.getNameOfAlarmHeight());
//            if (!StringUtils.isBlank(p.getNameOfAlarmPlane())) {
//                sb.append(", ");
//            }
//        }
//
//        if (!StringUtils.isBlank(p.getNameOfAlarmPlane())) {
//            sb.append("P ").append(p.getNameOfAlarmPlane());
//        }
//
//        var alarmNames = sb.toString();
//
//        String hAlarm = "";
//        if (p.getDimension() != BDimension._2d) {
//            hAlarm = "H " + AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p);
//            if (p.getDimension() == BDimension._3d) {
//                hAlarm = hAlarm + ", ";
//            }
//        }
//
//        String pAlarm = "";
//        if (p.getDimension() != BDimension._1d) {
//            pAlarm = "P " + AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p);
//        }
//
//        String delta = p.ext().deltaZero().getDelta(3);
//
//        var rightTitle = "%s%s: %s".formatted(hAlarm, pAlarm, delta);
//        mDeltaSubTextTitle.setText(rightTitle);
    }

    @Override
    public void updateDataset(BTopoGrade p) {
        mDataset.removeAllSeries();
        mTimeSeriesH.clear();
        mTimeSeriesV.clear();

        var plot = (XYPlot) mChart.getPlot();
        plot.clearDomainMarkers();
        p.getCommonObservations().entrySet().forEach(entry -> {
            var date = entry.getKey();
            var p1 = entry.getValue();
            var p2 = entry.getValue();
            //TODO Handle replacement & zero measurements

            var minute = mChartHelper.convertToMinute(date.atStartOfDay());
            var gradeDiff = p.ext().getDiff(p.getFirstObservation(), p2);

            if (p.getAxis() == BAxis.HORIZONTAL) {
                mTimeSeriesH.add(minute, gradeDiff.getZPerMille());
            }

            if (p.getAxis() == BAxis.VERTICAL) {
                mTimeSeriesV.add(minute, gradeDiff.getRPerMille());
            }
        });

        var renderer = plot.getRenderer();

        if (!mTimeSeriesH.isEmpty()) {
            mDataset.addSeries(mTimeSeriesH);
            renderer.setSeriesPaint(mDataset.getSeriesIndex(mTimeSeriesH.getKey()), Color.RED);
        }

        if (!mTimeSeriesV.isEmpty()) {
            mDataset.addSeries(mTimeSeriesV);
            renderer.setSeriesPaint(mDataset.getSeriesIndex(mTimeSeriesV.getKey()), Color.BLUE);
        }

    }
}

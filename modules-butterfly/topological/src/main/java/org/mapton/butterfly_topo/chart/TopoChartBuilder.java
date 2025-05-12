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
package org.mapton.butterfly_topo.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.numbers.core.Precision;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.LengthAdjustmentType;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.mapton.butterfly_alarm.api.AlarmHelper;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.TopoHelper;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.openide.util.Exceptions;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class TopoChartBuilder extends XyzChartBuilder<BTopoControlPoint> {

    private Minute mSubSetLastMinute;
    private Minute mSubSetZeroMinute;
    private final TimeSeries mTimeSeries2d = new TimeSeries(Dict.Geometry.PLANE);
    private final TimeSeries mTimeSeries3d = new TimeSeries("3d");
    private final TimeSeries mTimeSeriesH = new TimeSeries(Dict.Geometry.HEIGHT);

    public TopoChartBuilder() {
        initChart(null, null);
    }

    @Override
    public synchronized Callable<ChartPanel> build(BTopoControlPoint p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            var plot = (XYPlot) mChart.getPlot();
            updateDataset(p);

            setDateRangeNullNow(plot, p, mDateNull);
            plot.clearRangeMarkers();
            plotAlarmIndicators(p);

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);
//            rangeAxis.setRange(-0.050, +0.050);

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public void setTitle(BTopoControlPoint p) {
        setTitle(p, TopoHelper.getAlarmColorAwt(p));

        var dateFirst = Objects.toString(DateHelper.toDateString(p.getDateZero()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        getLeftSubTextTitle().setText(date);

        var sb = new StringBuilder();
        if (!StringUtils.isBlank(p.getAlarm1Id())) {
            sb.append("H ").append(p.getAlarm1Id());
            if (!StringUtils.isBlank(p.getAlarm2Id())) {
                sb.append(", ");
            }
        }

        if (!StringUtils.isBlank(p.getAlarm2Id())) {
            sb.append("P ").append(p.getAlarm2Id());
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
        getRightSubTextTitle().setText(rightTitle);
    }

    @Override
    public void updateDataset(BTopoControlPoint p) {
        mTimeSeriesH.clear();
        mTimeSeries2d.clear();
        mTimeSeries3d.clear();

        var plot = (XYPlot) mChart.getPlot();
        resetPlot(plot);
        plotBlasts(plot, p, p.ext().getObservationFilteredFirstDate(), p.ext().getObservationFilteredLastDate());
        plotMeasNeed(plot, p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            addNEMarkers(plot, o, true);

            var minute = ChartHelper.convertToMinute(o.getDate());
            mSubSetLastMinute = minute;
            if (o.isZeroMeasurement()) {
                mSubSetZeroMinute = minute;
            }

            if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
                mTimeSeriesH.add(minute, o.ext().getDeltaZ());
            }

            if (p.getDimension() == BDimension._2d || p.getDimension() == BDimension._3d) {
                mTimeSeries2d.add(minute, o.ext().getDelta2d());
            }

            if (p.getDimension() == BDimension._3d) {
                try {
                    mTimeSeries3d.add(minute, Math.abs(o.ext().getDelta3d()));
                } catch (NullPointerException e) {
                    System.err.println("Failed to add observation to chart %s %s".formatted(p.getName(), o.getDate()));
                }
            }

            mDateEnd = DateHelper.convertToDate(o.getDate());
        });

        var renderer = plot.getRenderer();
        var avgStroke = new BasicStroke(5.0f);
        int avdDays = 90 * 60 * 24;
        int avgSkipMeasurements = 0;
        boolean plotAvg = true;

        if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
            getDataset().addSeries(mTimeSeriesH);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeriesH.getKey()), Color.RED);
            if (plotAvg) {
                var mavg = createSubSetMovingAverage(mTimeSeriesH, mSubSetZeroMinute, mSubSetLastMinute, "%s (avg)".formatted(mTimeSeriesH.getKey()), avdDays, avgSkipMeasurements);
                if (mavg != null) {
                    getDataset().addSeries(mavg);
                    int index = getDataset().getSeriesIndex(mavg.getKey());
                    renderer.setSeriesPaint(index, Color.RED);
                    renderer.setSeriesStroke(index, avgStroke);
                }
            }
        }

        if (p.getDimension() == BDimension._2d || p.getDimension() == BDimension._3d) {
            getDataset().addSeries(mTimeSeries2d);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeries2d.getKey()), Color.GREEN);
            if (plotAvg) {
                var mavg = createSubSetMovingAverage(mTimeSeries2d, mSubSetZeroMinute, mSubSetLastMinute, "%s (avg)".formatted(mTimeSeries2d.getKey()), avdDays, avgSkipMeasurements);
                if (mavg != null) {
                    getDataset().addSeries(mavg);
                    int index = getDataset().getSeriesIndex(mavg.getKey());
                    renderer.setSeriesPaint(index, Color.GREEN);
                    renderer.setSeriesStroke(index, avgStroke);
                }
            }
        }

        if (p.getDimension() == BDimension._3d) {
            getDataset().addSeries(mTimeSeries3d);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeries3d.getKey()), Color.BLUE);
            if (plotAvg) {
                var mavg = createSubSetMovingAverage(mTimeSeries3d, mSubSetZeroMinute, mSubSetLastMinute, "%s (avg)".formatted(mTimeSeries3d.getKey()), avdDays, avgSkipMeasurements);
                if (mavg != null) {
                    try {
                        getDataset().addSeries(mavg);
                        int index = getDataset().getSeriesIndex(mavg.getKey());
                        renderer.setSeriesPaint(index, Color.BLUE);
                        renderer.setSeriesStroke(index, avgStroke);
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
        }
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
                if (!Precision.equals(range0.getMinimum(), 0.0)) {
                    plotAlarmIndicator(BComponent.PLANE, range0.getMinimum(), Color.YELLOW);
                }
                plotAlarmIndicator(BComponent.PLANE, range0.getMaximum(), Color.YELLOW);
            }

            var range1 = pa.ext().getRange1();
            if (range1 != null) {
                if (!Precision.equals(range1.getMinimum(), 0.0)) {
                    plotAlarmIndicator(BComponent.PLANE, range1.getMinimum(), Color.RED);
                }
                plotAlarmIndicator(BComponent.PLANE, range1.getMaximum(), Color.RED);
            }
        }
    }
}

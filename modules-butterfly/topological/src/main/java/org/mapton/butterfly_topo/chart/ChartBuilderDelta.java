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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.openide.util.Exceptions;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ChartBuilderDelta extends ChartBuilderBase {

    private boolean mPlotAvg = true;

    public ChartBuilderDelta(boolean plotAvg, Integer recentDaysDefault) {
        setRecentDaysDefault(recentDaysDefault);
        setRecentDays(recentDaysDefault);
        mPlotAvg = plotAvg;
        initChart("mm", "0");
    }

    @Override
    public void updateDataset(BTopoControlPoint p) {
        mTimeSeries1d.clear();
        mTimeSeries2d.clear();

        var plot = (XYPlot) mChart.getPlot();
        var rangeAxis = plot.getRangeAxis();
        resetPlot(plot);
        plotMarkers(p);
        var delta1d = 0.0;
        var delta2d = 0.0;
        if (p.getDimension() != BDimension._2d) {
            delta1d = plot(p, mTimeSeries1d, Color.RED, (BXyzPointObservation o) -> o.ext().getDelta1d());
        }
        if (p.getDimension() != BDimension._1d) {
            delta2d = plot(p, mTimeSeries2d, Color.GREEN, (BXyzPointObservation o) -> o.ext().getDelta2d());
        }

        var dateAxis = (DateAxis) plot.getDomainAxis();
        var now = LocalDate.now();
        var nowAsDate = DateHelper.convertToDate(now);
        if (isCompleteView()) {
            dateAxis.setRange(DateHelper.convertToDate(p.ext().getDateFirst()), nowAsDate);
            setRange(1.05, 1000, p.ext().getAlarm(BComponent.PLANE), p.ext().getAlarm(BComponent.HEIGHT));
        } else {
            var sb = new StringBuilder();
            if (p.getDimension() != BDimension._2d) {
                sb.append("Δ1d %+.0f".formatted(delta1d));
                if (p.getDimension() == BDimension._3d) {
                    sb.append(", ");
                }
            }
            if (p.getDimension() != BDimension._1d) {
                sb.append("Δ2d %+.0f".formatted(delta2d));
            }
            getRightSubTextTitle().setText(sb.toString());
            dateAxis.setRange(DateHelper.convertToDate(now.minusDays(getRecentDays())), nowAsDate);
            rangeAxis.setLabel("");
            rangeAxis.setAutoRange(true);
        }
    }

    private double plot(BTopoControlPoint p, TimeSeries timeSeries, Color color, Function<BXyzPointObservation, Double> function) {
        var plot = (XYPlot) mChart.getPlot();
        var renderer = plot.getRenderer();
        var startDate = isCompleteView() ? LocalDateTime.MIN : LocalDateTime.now().minusDays(getRecentDays());
        Double firstDelta = null;
        Double lastDelta = null;
        for (var o : p.ext().getObservationsTimeFiltered()) {
            if (o.getDate().isAfter(startDate)) {
                var delta = function.apply(o) * 1000;
                if (firstDelta == null) {
                    firstDelta = delta;
                }
                lastDelta = delta;
                timeSeries.add(ChartHelper.convertToMinute(o.getDate()), delta);
                if (DateHelper.isAfterOrEqual(o.getDate().toLocalDate(), p.getDateZero())) {
                    mMinMaxCollection.add(delta);
                }
            }
        }

        getDataset().addSeries(timeSeries);
        renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries.getKey()), color);
        plotAvg(timeSeries, color);
        try {
            return lastDelta - firstDelta;
        } catch (Exception e) {
            return 0;
        }
    }

    private void plotAvg(TimeSeries timeSeries, Color color) {
        if (!mPlotAvg) {
            return;
        }
        var plot = (XYPlot) mChart.getPlot();
        int avdDays = 90 * 60 * 24;
        int avgSkipMeasurements = 0;
        var mavg = createSubSetMovingAverage(timeSeries, mSubSetZeroMinute, mSubSetLastMinute, "%s (avg)".formatted(timeSeries.getKey()), avdDays, avgSkipMeasurements);
        if (mavg != null) {
            try {
                getDataset().addSeries(mavg);
                var renderer = (XYLineAndShapeRenderer) plot.getRenderer();
                var avgStroke = new BasicStroke(2.0f);
                int index = getDataset().getSeriesIndex(mavg.getKey());
                renderer.setSeriesPaint(index, color.brighter().brighter());
                renderer.setSeriesStroke(index, avgStroke);
//                renderer.setDefaultShapesVisible(false);
                renderer.setSeriesShapesVisible(index, false);

            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    private void plotMarkers(BTopoControlPoint p) {
        var plot = (XYPlot) mChart.getPlot();
        plotBlasts(plot, p, p.ext().getObservationFilteredFirstDate(), p.ext().getObservationFilteredLastDate());
        plotMeasNeed(plot, p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            addNEMarkers(plot, o, true);

            var minute = ChartHelper.convertToMinute(o.getDate());
            mSubSetLastMinute = minute;
            if (o.isZeroMeasurement()) {
                mSubSetZeroMinute = minute;
            }

            mDateEnd = DateHelper.convertToDate(o.getDate());
        });

    }

}

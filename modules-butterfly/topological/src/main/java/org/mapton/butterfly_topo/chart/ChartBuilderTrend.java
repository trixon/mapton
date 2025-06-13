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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.mapton.butterfly_core.api.TrendHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class ChartBuilderTrend extends ChartBuilderBase {

    private final BDimension mDimension;
    private final Function<BXyzPointObservation, Double> mFunction;
    private final TimeSeries mTimeSeries;

    public ChartBuilderTrend(BDimension dimension, Function<BXyzPointObservation, Double> function) {
        mDimension = dimension;
        mFunction = function;
        mTimeSeries = new TimeSeries(dimension == BDimension._1d ? Dict.Geometry.HEIGHT : Dict.Geometry.PLANE);
        initChart(null, null);
    }

    @Override
    public void updateDataset(BTopoControlPoint p) {
        mTimeSeries.clear();

        var plot = (XYPlot) mChart.getPlot();
        resetPlot(plot);

        for (var entry : plot.getDatasets().entrySet()) {
            var key = entry.getKey();
            if (entry.getValue() instanceof XYSeriesCollection dataset) {
                if (key != 0 && dataset != null) {
                    dataset.removeAllSeries();
                    plot.setDataset(key, null);
                }
            }

        }

        plotBlasts(plot, p, p.ext().getObservationFilteredFirstDate(), p.ext().getObservationFilteredLastDate());
        plotMeasNeed(plot, p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            addNEMarkers(plot, o, true);

            var minute = ChartHelper.convertToMinute(o.getDate());
            mSubSetLastMinute = minute;
            if (o.isZeroMeasurement()) {
                mSubSetZeroMinute = minute;
            }

            mTimeSeries.add(minute, mFunction.apply(o));

            mDateEnd = DateHelper.convertToDate(o.getDate());
        });

        if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
            getDataset().addSeries(mTimeSeries);
            var renderer = new XYLineAndShapeRenderer(true, false);
            plot.setRenderer(0, renderer);

            renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeries.getKey()), mDimension == BDimension._1d ? Color.RED : Color.BLUE);
        }

        var startDateFirst = p.ext().getDateFirst();
        var startDateZero = p.getDateZero().atStartOfDay();
        var endDateLast = p.getDateLatest();
        var startDateMinus6m = endDateLast.minusMonths(6);
        var startDateMinus3m = endDateLast.minusMonths(3);
        var startDateMinus1m = endDateLast.minusMonths(1);
        var startDateMinus1w = endDateLast.minusWeeks(1);

        var index = 1;
        plot(p, "▼", startDateFirst, LocalDateTime.MIN, Color.GREEN, index++, -50);
        plot(p, "▲", startDateFirst, LocalDateTime.MIN, Color.GREEN, index++, 50);
        if (startDateFirst.isBefore(startDateZero)) {
            plot(p, "Första", startDateFirst, LocalDateTime.MIN, Color.BLACK, index++, null);
        }

        plot(p, "Nolla", startDateZero, LocalDateTime.MIN, Color.MAGENTA, index++, null);
        plot(p, "6m", startDateMinus6m, startDateZero, Color.CYAN, index++, null);
        plot(p, "3m", startDateMinus3m, startDateZero, Color.YELLOW, index++, null);
        plot(p, "1m", startDateMinus1m, startDateZero, Color.ORANGE, index++, null);
        plot(p, "1w", startDateMinus1w, startDateZero, Color.RED, index++, null);

        setRange(1.05, p.ext().getAlarm(BComponent.PLANE), p.ext().getAlarm(BComponent.HEIGHT));
    }

    private void plot(BTopoControlPoint p, String title, LocalDateTime startDate, LocalDateTime limitDate, Color color, int index, Integer percentile) {
        if (startDate.isBefore(limitDate)) {
            return;
        }

        var endDate = LocalDateTime.now();
        TrendHelper.Trend trend;
        try {
            if (percentile == null) {
                trend = TrendHelper.createTrend(p, startDate, endDate, mFunction);
            } else {
                trend = TrendHelper.createTrend(p, startDate, endDate, mFunction, percentile);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Trend: Not enough data.");
            return;
        }
        var dataset = DatasetUtils.sampleFunction2D(
                trend.function(),
                trend.startMinute().getFirstMillisecond(),
                trend.endMinute().getFirstMillisecond(),
                100,
                title);

        var plot = (XYPlot) mChart.getPlot();
        plot.setDataset(index, dataset);

        var renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, color);
        renderer.setSeriesStroke(0, new BasicStroke(percentile == null ? 4f : 2f));
        renderer.setDefaultToolTipGenerator((xyDataset, series, item) -> {
            var now = LocalDateTime.now();
            var val1 = trend.function().getValue(ChartHelper.convertToMinute(now.plusYears(1)).getFirstMillisecond());
            var val2 = trend.function().getValue(ChartHelper.convertToMinute(now).getFirstMillisecond());
            return "%.1f mm/år".formatted((val1 - val2) * 1000);
        });

        plot.setRenderer(index, renderer);
    }
}

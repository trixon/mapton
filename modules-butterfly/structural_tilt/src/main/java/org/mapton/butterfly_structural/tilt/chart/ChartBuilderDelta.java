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
package org.mapton.butterfly_structural.tilt.chart;

import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;
import org.jfree.chart.axis.DateAxis;
import org.jfree.data.time.TimeSeries;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.mapton.core.api.ChartMiscLineMode;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ChartBuilderDelta extends ChartBuilderBase {

    private final Function<BXyzPointObservation, Double> mFunction;
    private final boolean mPlotAvg;

    public ChartBuilderDelta(Function<BXyzPointObservation, Double> function, boolean plotAvg, Integer recentDaysDefault) {
        mFunction = function;
        setRecentDaysDefault(recentDaysDefault);
        setRecentDays(recentDaysDefault);
        mPlotAvg = plotAvg;
        initChart("mm/m", "0.0");
    }

    @Override
    public void updateDataset(BStructuralTiltPoint p) {
        mTimeSeriesX.clear();
        mTimeSeriesY.clear();
        mTimeSeriesZ.clear();

        var plot = getPlot();
        var rangeAxis = plot.getRangeAxis();
        resetPlot(plot);
        var plotMarkerDates = plotMarkers(p);
//        mSubSetFirstMinute = plotMarkerDates.first();
        mSubSetZeroMinute = plotMarkerDates.zero();
        mSubSetLastMinute = plotMarkerDates.last();

        var deltaX = 0.0;
        var deltaY = 0.0;
        var deltaR = 0.0;
        if (mFunction == null) {
            deltaX = plot(p, mTimeSeriesX, Color.RED, (BXyzPointObservation o) -> o.ext().getDeltaX());
            deltaY = plot(p, mTimeSeriesY, Color.GREEN, (BXyzPointObservation o) -> o.ext().getDeltaY());
            deltaR = plot(p, mTimeSeriesZ, Color.BLUE, (BXyzPointObservation o) -> o.ext().getDelta2d());
        } else {
            plot(p, mTimeSeriesZ, Color.BLACK, mFunction);
        }

        var dateAxis = (DateAxis) plot.getDomainAxis();
        var now = LocalDate.now();
        var nowAsDate = DateHelper.convertToDate(now.plusDays(1));
        if (isCompleteView()) {
            if (mPlotAvg) {
                rangeAxis.setAutoRange(true);
            } else {
                setRange(1.05, p.ext().getAlarm(BComponent.PLANE), p.ext().getAlarm(BComponent.HEIGHT));
            }
        } else {
            var title = "T=%+.1f, L=%+.1f, R=%+.1f".formatted(deltaX, deltaY, deltaR);
            getRightSubTextTitle().setText(title);
            dateAxis.setRange(DateHelper.convertToDate(now.minusDays(getRecentDays())), nowAsDate);
            rangeAxis.setLabel("");
            rangeAxis.setAutoRange(true);
        }
    }

    private double plot(BStructuralTiltPoint p, TimeSeries timeSeries, Color color, Function<BXyzPointObservation, Double> function) {
        var plot = getPlot();
        var renderer = plot.getRenderer();
        var startDate = isCompleteView() ? LocalDateTime.MIN : LocalDateTime.now().minusDays(getRecentDays());
        Double firstDelta = null;
        Double lastDelta = null;
        for (var o : p.ext().getObservationsTimeFiltered()) {
            if (o.getDate().isAfter(startDate)) {
                var delta = function.apply(o);
                if (firstDelta == null) {
                    firstDelta = delta;
                }
                if (mChartOptionsManager.getMiscLineModeProperty() == ChartMiscLineMode.EXTRAPOLATE) {
                    if (lastDelta != null) {
                        timeSeries.addOrUpdate(ChartHelper.convertToMinute(o.getDate()).previous(), lastDelta);
                    }
                }
                lastDelta = delta;
                timeSeries.addOrUpdate(ChartHelper.convertToMinute(o.getDate()), delta);
                if (DateHelper.isAfterOrEqual(o.getDate().toLocalDate(), p.getDateZero())) {
                    mMinMaxCollection.add(delta);
                }
            }
        }

//        if (mFunction == null) {
//            getDataset().addSeries(timeSeries);
//            renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries.getKey()), color);
//        } else {
//        }
        plot(p, mPlotAvg, timeSeries, renderer, color);

        try {
            return lastDelta - firstDelta;
        } catch (Exception e) {
            return 0;
        }
    }

}

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
import org.apache.commons.lang3.ObjectUtils;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.mapton.core.api.ChartMiscLineMode;
import org.openide.util.Exceptions;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.GraphicsHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ChartBuilderDelta extends ChartBuilderBase {

    private final BComponent mComponent;
    private final boolean mPlotAvg;

    public ChartBuilderDelta(BComponent component, boolean plotAvg, Integer recentDaysDefault) {
        mComponent = component;
        setRecentDaysDefault(recentDaysDefault);
        setRecentDays(recentDaysDefault);
        mPlotAvg = plotAvg;
        initChart("mm", "0");
    }

    @Override
    public void updateDataset(BTopoControlPoint p) {
        var plot = getPlot();
        mTimeSeries1d.clear();
        mTimeSeries2d.clear();
        var rangeAxis = plot.getRangeAxis();
        resetPlot(plot);
        plotMarkers(p);
        var delta1d = 0.0;
        var delta2d = 0.0;
        if (p.getDimension() != BDimension._2d && mComponent == null || mComponent == BComponent.HEIGHT) {
            delta1d = plot(p, mTimeSeries1d, Color.RED, (BXyzPointObservation o) -> o.ext().getDelta1d());
        }
        if (p.getDimension() != BDimension._1d && mComponent == null || mComponent == BComponent.PLANE) {
            delta2d = plot(p, mTimeSeries2d, Color.GREEN, (BXyzPointObservation o) -> o.ext().getDelta2d());
        }

        var dateAxis = (DateAxis) plot.getDomainAxis();
        var now = LocalDate.now();
        var nowAsDate = DateHelper.convertToDate(now.plusDays(1));

        if (isCompleteView()) {
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

    private synchronized double plot(BTopoControlPoint p, TimeSeries timeSeries, Color color, Function<BXyzPointObservation, Double> function) {
        var plot = getPlot();
        var renderer = plot.getRenderer();
        var startDate = isCompleteView() ? LocalDateTime.MIN : LocalDateTime.now().minusDays(getRecentDays());
        Double firstDelta = null;
        Double lastDelta = null;
        var originalTimeSeries = new TimeSeries("original");

        for (var o : p.ext().getObservationsTimeFiltered()) {
            if (o.getDate().isAfter(startDate)) {
                var delta = function.apply(o) * 1000;
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
                originalTimeSeries.addOrUpdate(ChartHelper.convertToMinute(o.getDate()), delta);
                if (DateHelper.isAfterOrEqual(o.getDate().toLocalDate(), p.getDateZero())) {
                    mMinMaxCollection.add(delta);
                }
            }
        }

        if (mPlotAvg) {
            if (mChartOptionsManager.isAvgPlotRaw()) {
                getDataset().addSeries(timeSeries);
                renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries.getKey()), GraphicsHelper.colorAddAlpha(Color.RED, 128));
            }
            var ewma1 = createEWMA(p.getDateZero(), timeSeries, mChartOptionsManager.getAvgPeriod1());
            var ewma2 = createEWMA(p.getDateZero(), timeSeries, mChartOptionsManager.getAvgPeriod2());
            if (mChartOptionsManager.isAvgPlotPeriod1()) {
                plotAvg(ewma1, Color.ORANGE);
            }
            if (mChartOptionsManager.isAvgPlotPeriod2()) {
                plotAvg(ewma2, Color.BLUE);
            }
            if (mChartOptionsManager.isAvgPlotDiff() && ObjectUtils.allNotNull(ewma1, ewma2)) {
                var diff = createDifference(ewma1, ewma2, "Aktivitet");
                plotAvg(diff, Color.MAGENTA);
            }
        } else {
            getDataset().addSeries(timeSeries);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries.getKey()), color);
        }

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
        var plot = getPlot();
        if (timeSeries != null) {
            try {
                getDataset().addSeries(timeSeries);
                var renderer = (XYLineAndShapeRenderer) plot.getRenderer();
                var avgStroke = new BasicStroke(2.0f);
                int index = getDataset().getSeriesIndex(timeSeries.getKey());
                renderer.setSeriesPaint(index, color);
                renderer.setSeriesStroke(index, avgStroke);
                renderer.setSeriesShapesVisible(index, false);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    private void plotMarkers(BTopoControlPoint p) {
        SwingHelper.runLater(() -> {
            var plot = getPlot();
            plotOverlays(plot, p, p.ext().getObservationFilteredFirstDate());
            plotMeasNeed(plot, p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));

            try {
                var firstDate = p.ext().getObservationsTimeFiltered().getFirst().getDate();
                mSubSetFirstMinute = ChartHelper.convertToMinute(firstDate);
            } catch (Exception e) {
            }

            p.ext().getObservationsTimeFiltered().forEach(o -> {
                addNEMarkers(plot, o, true);

                var minute = ChartHelper.convertToMinute(o.getDate());
                mSubSetLastMinute = minute;
                if (o.isZeroMeasurement()) {
                    mSubSetZeroMinute = minute;
                }

                mDateEnd = DateHelper.convertToDate(o.getDate());
            });
            mChart.fireChartChanged();
        });
    }

}

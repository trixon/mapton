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
package org.mapton.butterfly_topo_convergence.group.chart;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TreeMap;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import static org.mapton.butterfly_format.types.BDimension._1d;
import static org.mapton.butterfly_format.types.BDimension._2d;
import static org.mapton.butterfly_format.types.BDimension._3d;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceObservation;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ChartBuilderDelta extends ChartBuilderBase {

    private TreeMap<Double, BTopoConvergencePair> mLastWeekDeltaToPair = new TreeMap<>();

    public ChartBuilderDelta(Integer recentDaysDefault, BDimension dimension) {
        mDimension = dimension;
        setRecentDaysDefault(recentDaysDefault);
        setRecentDays(recentDaysDefault);

        switch (dimension) {
            case _1d:
                mFunction = BTopoConvergenceObservation.FUNCTION_1D;
                break;
            case _2d:
                mFunction = BTopoConvergenceObservation.FUNCTION_2D;

                break;
            case _3d:
                mFunction = BTopoConvergenceObservation.FUNCTION_3D;

                break;
            default:
                throw new AssertionError();
        }

    }

    @Override
    public void updateDataset(BTopoConvergenceGroup p) {
        mLastWeekDeltaToPair.clear();
        var plot = (XYPlot) mChart.getPlot();
        var rangeAxis = plot.getRangeAxis();
        resetPlot(plot);
        plotMarkers(p);

        for (var pair : p.ext().getPairs()) {
            updateDataset(pair);
        }

        setRange(1.05, 1000, p.ext().getAlarm(BComponent.HEIGHT));
        mDateNull = DateHelper.convertToDate(p.getDateZero());
        var dateAxis = (DateAxis) plot.getDomainAxis();
        var now = LocalDate.now();
        var nowAsDate = DateHelper.convertToDate(now.plusDays(1));
        if (isCompleteView()) {
            dateAxis.setRange(DateHelper.convertToDate(p.getDateZero()), nowAsDate);
            setRange(1.05, 1000, p.ext().getAlarm(BComponent.PLANE), p.ext().getAlarm(BComponent.HEIGHT));
        } else {
            var subTitle = "";
            var entry = mLastWeekDeltaToPair.lastEntry();
            if (entry != null) {
                subTitle = "%+.1f %s".formatted(entry.getKey(), entry.getValue().getSimpleName());
            }
            getRightSubTextTitle().setText(subTitle);
            dateAxis.setRange(DateHelper.convertToDate(now.minusDays(getRecentDays())), nowAsDate);
            rangeAxis.setLabel("");
            rangeAxis.setAutoRange(true);
        }
    }

    private void plotMarkers(BTopoConvergenceGroup p) {
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

    private void updateDataset(BTopoConvergencePair pair) {
        var timeSeries = new TimeSeries(pair.getSimpleName());
        var startDate = isCompleteView() ? LocalDateTime.MIN : LocalDateTime.now().minusDays(getRecentDays());
        Double firstDelta = null;
        Double lastDelta = null;

        for (var o : pair.ext().getObservationsTimeFiltered()) {
            if (o.getDate().isAfter(startDate)) {
                var delta = mFunction.apply(o);
                if (firstDelta == null) {
                    firstDelta = delta;
                }
                lastDelta = delta;
                timeSeries.addOrUpdate(ChartHelper.convertToMinute(o.getDate()), delta);
                mMinMaxCollection.add(delta);
            }
        }

        try {
            mLastWeekDeltaToPair.put(Math.abs(lastDelta - firstDelta), pair);
        } catch (Exception e) {
            //nvm
        }

        getDataset().addSeries(timeSeries);
    }
}

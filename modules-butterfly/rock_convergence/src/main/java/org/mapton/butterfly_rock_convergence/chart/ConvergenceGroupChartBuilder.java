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
package org.mapton.butterfly_rock_convergence.chart;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;
import org.mapton.butterfly_topo.TopoHelper;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergenceGroupChartBuilder extends XyzChartBuilder<BTopoConvergenceGroup> {

    public ConvergenceGroupChartBuilder() {
        initChart("mm", "0.0");
    }

    @Override
    public synchronized Callable<ChartPanel> build(BTopoConvergenceGroup p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);
            var plot = (XYPlot) mChart.getPlot();
            if (mDateNull != null) {
                setDateRangeNullNow(plot, p, mDateNull);
            }

            plot.clearRangeMarkers();
            plotAlarmIndicators(p, 1000);
            return getChartPanel();
        };

        return callable;
    }

    @Override
    public void setTitle(BTopoConvergenceGroup p) {
        setTitle(p, TopoHelper.getAlarmColorAwt(p));
        var dateFirst = Objects.toString(DateHelper.toDateString(p.getDateZero()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        getLeftSubTextTitle().setText(date);
    }

    @Override
    public synchronized void updateDataset(BTopoConvergenceGroup p) {
        var plot = (XYPlot) mChart.getPlot();
        resetPlot(plot);
        plotBlasts(plot, p, p.ext().getObservationFilteredFirstDate(), p.ext().getObservationFilteredLastDate());
        plotMeasNeed(plot, p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));

        for (var pair : p.ext().getPairs()) {
            updateDataset(pair);
        }
        setRange(1.05, 1000, p.ext().getAlarm(BComponent.HEIGHT));
    }

    private void updateDataset(BTopoConvergencePair pair) {
        var timeSeries = new TimeSeries(pair.getSimpleName());

        var plot = (XYPlot) mChart.getPlot();

        pair.ext().getObservationsTimeFiltered().forEach(o -> {
            addNEMarkers(plot, o, true);
            if (o.isZeroMeasurement()) {
//                mDateNull;
            }
            var minute = ChartHelper.convertToMinute(o.getDate());
            Double delta = o.getMeasuredX();
            timeSeries.addOrUpdate(minute, delta);
//            if (DateHelper.isAfterOrEqual(o.getDate().toLocalDate(), pair.getDateZero())) {
            mMinMaxCollection.add(delta);
//            }
        });
        var renderer = plot.getRenderer();

        getDataset().addSeries(timeSeries);
//        renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries.getKey()), Color.RED);
    }

}

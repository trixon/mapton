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
package org.mapton.butterfly_structural.load.chart;

import java.awt.Color;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.structural.BStructuralLoadCellPoint;
import org.mapton.butterfly_structural.load.LoadHelper;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LoadChartBuilder extends XyzChartBuilder<BStructuralLoadCellPoint> {

    private final TimeSeries mTimeSeriesZ = new TimeSeries("kN");

    public LoadChartBuilder() {
        initChart("kN", "0");
    }

    @Override
    public synchronized Callable<ChartPanel> build(BStructuralLoadCellPoint p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);
            var plot = (XYPlot) mChart.getPlot();
            setDateRangeNullNow(plot, p, mDateNull);

            plot.clearRangeMarkers();
            plotAlarmIndicators(p);

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public void setTitle(BStructuralLoadCellPoint p) {
        setTitle(p, LoadHelper.getAlarmColorAwt(p));

        var dateFirst = Objects.toString(DateHelper.toDateString(p.getDateZero()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        getLeftSubTextTitle().setText(date);

        var rightTitle = "%s: %s".formatted(p.getAlarm1Id(), p.ext().getDeltaZero());
        getRightSubTextTitle().setText(rightTitle);
    }

    @Override
    public synchronized void updateDataset(BStructuralLoadCellPoint p) {
        clear(mTimeSeriesZ);
        var plot = (XYPlot) mChart.getPlot();
        resetPlot(plot);

        plotBlasts(plot, p, p.ext().getObservationFilteredFirstDate(), p.ext().getObservationFilteredLastDate());
        plotMeasNeed(plot, p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));

        var timeSeries = new TimeSeries(p.getName());

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            addNEMarkers(plot, o, true);

            if (o.ext().getDeltaZ() != null) {
                var minute = ChartHelper.convertToMinute(o.getDate());
                timeSeries.addOrUpdate(minute, o.ext().getDeltaZ());
                if (DateHelper.isAfterOrEqual(o.getDate().toLocalDate(), p.getDateZero())) {
                    mMinMaxCollection.add(o.ext().getDeltaZ());
                }
            }
        });

        var renderer = plot.getRenderer();

        getDataset().addSeries(timeSeries);
        renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries.getKey()), Color.RED);
        setRange(1.05, p.ext().getAlarm(BComponent.HEIGHT));
    }

}

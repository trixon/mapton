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

import java.awt.BasicStroke;
import java.awt.Color;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergenceGroupChartBuilder extends XyzChartBuilder<BTopoConvergenceGroup> {

    private final TimeSeries mTimeSeriesX = new TimeSeries("Mätning");

    public ConvergenceGroupChartBuilder() {
        initChart("mm/m", "0.0");

        var plot = (XYPlot) mChart.getPlot();
        plot.getRangeAxis().setVisible(false);
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
            setDateRangeNullNow(plot, p, mDateNull);

            plot.clearRangeMarkers();

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public void setTitle(BTopoConvergenceGroup p) {
        super.setTitle(p);
//        Color color = TopoHelper.getAlarmColorAwt(p);
        Color color = Color.BLUE;
        if (color == Color.RED || color == Color.GREEN) {
            color = color.darker();
        }
        mChart.getTitle().setPaint(color);
        var dateFirst = Objects.toString(DateHelper.toDateString(p.getDateZero()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        getLeftSubTextTitle().setText(date);

//        var rightTitle = "%s: %s".formatted(p.getAlarm1Id(), p.ext().getDeltaZero());
//        getRightSubTextTitle().setText(rightTitle);
    }

    @Override
    public synchronized void updateDataset(BTopoConvergenceGroup p) {
        mTimeSeriesX.clear();

        var plot = (XYPlot) mChart.getPlot();
        resetPlot(plot);
        plotBlasts(plot, p, p.ext().getObservationFilteredFirstDate(), p.ext().getObservationFilteredLastDate());
        plotMeasNeed(plot, p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            addNEMarkers(plot, o, true);

            var minute = ChartHelper.convertToMinute(o.getDate());
            mTimeSeriesX.add(minute, 0.0);
        });

        var renderer = plot.getRenderer();
        var avgStroke = new BasicStroke(5.0f);
        int avdDays = 90 * 60 * 24;
        int avgSkipMeasurements = 0;
        boolean plotAvg = false;

        getDataset().addSeries(mTimeSeriesX);
        renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeriesX.getKey()), Color.RED);
        if (plotAvg) {
            var mavg = MovingAverage.createMovingAverage(mTimeSeriesX, "%s (avg)".formatted(mTimeSeriesX.getKey()), avdDays, avgSkipMeasurements);
            getDataset().addSeries(mavg);
            int index = getDataset().getSeriesIndex(mavg.getKey());
            renderer.setSeriesPaint(index, Color.RED);
            renderer.setSeriesStroke(index, avgStroke);
        }
    }

}

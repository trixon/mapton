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
import java.util.function.Function;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
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

    private final boolean mPlotAvg = true;

    public ChartBuilderDelta() {
        initChart(null, null);
    }

    @Override
    public void updateDataset(BTopoControlPoint p) {
        mTimeSeries1d.clear();
        mTimeSeries2d.clear();
        mTimeSeries3d.clear();

        var plot = (XYPlot) mChart.getPlot();
        resetPlot(plot);
        plotMarkers(p);

        if (p.getDimension() != BDimension._2d) {
            plot(p, mTimeSeries1d, Color.RED, (BXyzPointObservation o) -> o.ext().getDelta1d());
        }
        if (p.getDimension() != BDimension._1d) {
            plot(p, mTimeSeries2d, Color.GREEN, (BXyzPointObservation o) -> o.ext().getDelta2d());
        }
        if (p.getDimension() == BDimension._3d) {
            plot(p, mTimeSeries3d, Color.BLUE, (BXyzPointObservation o) -> o.ext().getDelta3d());
        }
    }

    private void plot(BTopoControlPoint p, TimeSeries timeSeries, Color color, Function<BXyzPointObservation, Double> function) {
        var plot = (XYPlot) mChart.getPlot();
        var renderer = plot.getRenderer();

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            timeSeries.add(ChartHelper.convertToMinute(o.getDate()), function.apply(o));
        });

        getDataset().addSeries(timeSeries);
        renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries.getKey()), color);
        plotAvg(timeSeries, color);
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

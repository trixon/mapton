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
package org.mapton.butterfly_meteo.chart;

import java.awt.Color;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.time.TimeSeries;
import org.mapton.api.MTemporalManager;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import static org.mapton.butterfly_core.api.XyzChartBuilder.plotOverlays;
import org.mapton.butterfly_format.types.BMeteoPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MeteoChartBuilder extends XyzChartBuilder<BMeteoPoint> {

    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private final TimeSeries mTimeSeriesH = new TimeSeries("Temperatur");

    public MeteoChartBuilder() {
        initChart("°C", "0.0");
    }

    @Override
    public synchronized Callable<ChartPanel> build(BMeteoPoint p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);
            var plot = getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setAutoRange(true);

            plot.clearRangeMarkers();
            //plotAlarmIndicators(p);

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public void setTitle(BMeteoPoint p) {
        setTitle(p, Color.BLUE);
    }

    @Override
    public void updateDataset(BMeteoPoint p) {
        mTimeSeriesH.clear();

        var plot = getPlot();
        resetPlot(plot);

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            var minute = ChartHelper.convertToMinute(o.getDate());

            mTimeSeriesH.addOrUpdate(minute, o.getAirTemperature());
        });
        getDataset().addSeries(mTimeSeriesH);
        plotOverlays(plot, p, p.ext().getObservationFilteredFirstDate());
    }

}

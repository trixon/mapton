/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_meteo.chart.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.time.LocalDate;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.api.MChartOverlay;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MChartOverlay.class)
public class MeteoPrecipitationChartOverlay extends BaseMeteoChartOverlay {

    public static final Color COLOR = Color.WHITE;

    public MeteoPrecipitationChartOverlay() {
        super("Nederbörd");
        mAxis.setAutoRange(false);
        mAxis.setRange(0, 50);
    }

    @Override
    public synchronized void plot(XYPlot plot, BBasePoint p, LocalDate aStartDate) {
        resetDatasetIfExisting(plot, mIndex);

        if (mObjectStorageManager.getBoolean(MeteoPrecipitationChartSOSB.class, MeteoPrecipitationChartSOSB.DEFAULT_VALUE)) {
            var startDate = aStartDate == null ? LocalDate.now() : aStartDate;
            var points = ButterflyHelper.getLimitedPoints(p, p.getButterfly().meteo().getMeteoPoints(), true, MAX_DISTANCE, MAX_COUNT, startDate, true);
            if (!points.isEmpty()) {
                var renderer = new XYLineAndShapeRenderer(true, false);
                var dataset = new TimeSeriesCollection();
                init(plot, dataset, renderer);

                var color = COLOR;

                for (int i = 0; i < points.size(); i++) {
                    if (i > 0) {
                        color = GraphicsHelper.brighten(color, 0.25);
                    }
                    var meteoPoint = points.get(i);
                    var distance = meteoPoint.<Double>getValue(ButterflyHelper.KEY_DISTANCE);
                    var timeSeries = new TimeSeries("%c.%.0f".formatted('A' + i, distance));

                    for (var o : meteoPoint.ext().getObservationsTimeFiltered()) {
                        if (o.getDate().isAfter(startDate.atStartOfDay())) {
                            timeSeries.addOrUpdate(ChartHelper.convertToMinute(o.getDate()), o.getPrecipitation());
                        }
                    }

                    dataset.addSeries(timeSeries);
                    var seriesIndex = dataset.getSeriesIndex(timeSeries.getKey());
                    renderer.setSeriesToolTipGenerator(seriesIndex, (xyDataset, series, item) -> {
                        return "%.0fm  %s".formatted(distance, meteoPoint.getName());
                    });

                    renderer.setSeriesVisibleInLegend(seriesIndex, true);
                    renderer.setSeriesPaint(seriesIndex, color, true);
                    var width = i == 0 ? 3f : 1.5f;
                    renderer.setSeriesStroke(seriesIndex, new BasicStroke(width));
                }
            }
        }
    }
}

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
package org.mapton.butterfly_structural.strain.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.time.LocalDate;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.api.MChartOverlay;
import org.mapton.butterfly_core.api.BChartOverlay;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.butterfly_structural.strain.StrainChartSOSB;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.GraphicsHelper;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MChartOverlay.class)
public class StrainChartOverlay extends BChartOverlay {

    public static final Color COLOR = Color.decode("#800080");
    public static final int MAX_COUNT = 3;
    public static final int MAX_DISTANCE = 10;
    private final NumberAxis mAxis = new NumberAxis(SDict.STRAIN_GAUGES.toString());
    private final int mIndex = 108;

    public StrainChartOverlay() {
        mAxis.setAutoRangeIncludesZero(false);
        mAxis.setAutoRange(true);
    }

    @Override
    public synchronized void plot(XYPlot plot, BBasePoint p, LocalDate aStartDate) {
        if (!mObjectStorageManager.getBoolean(StrainChartSOSB.class, StrainChartSOSB.DEFAULT_VALUE)) {
            plot.setDataset(mIndex, null);
            plot.setRangeAxis(mIndex, null);

            return;
        }

        var startDate = aStartDate == null ? LocalDate.now() : aStartDate;
        var points = ButterflyHelper.getLimitedPoints(p, p.getButterfly().structural().getStrainPoints(), MAX_DISTANCE, MAX_COUNT, startDate);
        var renderer = new XYLineAndShapeRenderer(true, false);
        var dataset = new TimeSeriesCollection();

        plot.setRangeAxis(mIndex, mAxis);
        plot.setDataset(mIndex, dataset);
        plot.mapDatasetToRangeAxis(mIndex, mIndex);
        plot.setRangeAxisLocation(mIndex, AxisLocation.BOTTOM_OR_RIGHT);
        plot.setRenderer(mIndex, renderer);

        var color = GraphicsHelper.colorAddAlpha(COLOR, 80);

        for (int i = 0; i < points.size(); i++) {
            if (i > 0) {
                color = GraphicsHelper.brighten(color, 0.25);
            }
            var loadcellPoint = points.get(i);
            var distance = loadcellPoint.<Double>getValue(ButterflyHelper.KEY_DISTANCE);
            var timeSeries = new TimeSeries("%c.%.0f".formatted('A' + i, distance));

            for (var o : loadcellPoint.ext().getObservationsTimeFiltered()) {
                if (o.getDate().isAfter(startDate.atStartOfDay())) {
                    timeSeries.addOrUpdate(ChartHelper.convertToMinute(o.getDate()), o.getMeasuredZ());
                }
            }

            dataset.addSeries(timeSeries);
            var seriesIndex = dataset.getSeriesIndex(timeSeries.getKey());
            renderer.setSeriesToolTipGenerator(seriesIndex, (xyDataset, series, item) -> {
                return "%.0fm  %s".formatted(distance, loadcellPoint.getName());
            });

            renderer.setSeriesVisibleInLegend(seriesIndex, true);
            renderer.setSeriesPaint(seriesIndex, color, true);
            var width = i == 0 ? 3f : 1.5f;
            renderer.setSeriesStroke(seriesIndex, new BasicStroke(width));
        }
    }

}

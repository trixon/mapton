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
package org.mapton.butterfly_hydro.waterlevel.chart;

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
import org.mapton.butterfly_hydro.waterlevel.WaterLevelChartSOSB;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MChartOverlay.class)
public class WaterLevelChartOverlay extends BChartOverlay {

    public static final Color COLOR = Color.decode("#4B0082");
    public static final int MAX_COUNT = 5;
    public static final int MAX_DISTANCE = 3000;
    private final NumberAxis mAxis = new NumberAxis("Vattenstånd");
    private final int mIndex = 300;

    public WaterLevelChartOverlay() {
        mAxis.setAutoRangeIncludesZero(false);
        mAxis.setAutoRange(true);
    }

    @Override
    public synchronized void plot(XYPlot plot, BBasePoint p, LocalDate aStartDate) {
        if (!mObjectStorageManager.getBoolean(WaterLevelChartSOSB.class, WaterLevelChartSOSB.DEFAULT_VALUE)) {
            plot.setDataset(mIndex, null);
            plot.setRangeAxis(mIndex, null);

            return;
        }

        var startDate = aStartDate == null ? LocalDate.now() : aStartDate;
        var waterLevelPoints = ButterflyHelper.getLimitedPoints(p, p.getButterfly().hydro().getWaterLevelPoints(), MAX_DISTANCE, MAX_COUNT, startDate);
        var renderer = new XYLineAndShapeRenderer(true, false);
        var dataset = new TimeSeriesCollection();

        plot.setRangeAxis(mIndex, mAxis);
        plot.setDataset(mIndex, dataset);
        plot.mapDatasetToRangeAxis(mIndex, mIndex);
        plot.setRangeAxisLocation(mIndex, AxisLocation.BOTTOM_OR_RIGHT);
        plot.setRenderer(mIndex, renderer);

        var color = GraphicsHelper.colorAddAlpha(COLOR, 80);

        for (int i = 0; i < waterLevelPoints.size(); i++) {
            if (i > 0) {
                color = GraphicsHelper.brighten(color, 0.25);
            }
            var groundwaterPoint = waterLevelPoints.get(i);
            var distance = groundwaterPoint.<Double>getValue(ButterflyHelper.KEY_DISTANCE);
            var timeSeries = new TimeSeries("%c.%.0f".formatted('A' + i, distance));

            for (var o : groundwaterPoint.ext().getObservationsTimeFiltered()) {
                if (o.getDate().isAfter(startDate.atStartOfDay())) {
                    timeSeries.addOrUpdate(ChartHelper.convertToMinute(o.getDate()), o.getGroundwaterLevel());
                }
            }

            dataset.addSeries(timeSeries);
            var seriesIndex = dataset.getSeriesIndex(timeSeries.getKey());
            renderer.setSeriesToolTipGenerator(seriesIndex, (xyDataset, series, item) -> {
                return "%.0fm  %s".formatted(distance, groundwaterPoint.getName());
            });

            renderer.setSeriesVisibleInLegend(seriesIndex, true);
            renderer.setSeriesPaint(seriesIndex, color, false);
            var width = i == 0 ? 3f : 1.5f;
            renderer.setSeriesStroke(seriesIndex, new BasicStroke(width));
        }
    }

    /*
        public synchronized void plot(XYPlot plot, BBasePoint p, LocalDate aStartDate) {
        if (!mObjectStorageManager.getBoolean(GroundwaterChartSOSB.class, false)) {
            return;
        }
        var startDate = aStartDate == null ? LocalDate.now().minusYears(5) : aStartDate;
        var groundwaterPoints = ButterflyHelper.getGroundwaterPoints(p, 100, 5, startDate);
        var gwRenderer = new XYLineAndShapeRenderer(true, false);
        var gwDataset = new TimeSeriesCollection();
        var gwAxis = new NumberAxis(SDict.GROUNDWATER.toString());
        gwAxis.setAutoRangeIncludesZero(false);
        gwAxis.setAutoRange(true);
        plot.setRangeAxis(2, gwAxis);
        plot.setDataset(2, gwDataset);
        plot.mapDatasetToRangeAxis(2, 2);
        plot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_RIGHT);
        plot.setRenderer(2, gwRenderer);
        var color = GraphicsHelper.colorAddAlpha(Color.BLUE, 80);

        for (int i = 0; i < groundwaterPoints.size(); i++) {
            if (i > 0) {
                color = GraphicsHelper.brighten(color, 0.25);
            }
            var groundwaterPoint = groundwaterPoints.get(i);
            var distance = groundwaterPoint.<Double>getValue(ButterflyHelper.KEY_DISTANCE);
            var timeSeries = new TimeSeries("%c.%.0f".formatted('A' + i, distance));

            for (var o : groundwaterPoint.ext().getObservationsTimeFiltered()) {
                if (o.getDate().isAfter(startDate.atStartOfDay())) {
                    timeSeries.addOrUpdate(ChartHelper.convertToMinute(o.getDate()), o.getGroundwaterLevel());
                }
            }

            gwDataset.addSeries(timeSeries);
            int series = gwDataset.getSeriesIndex(timeSeries.getKey());
            gwRenderer.setSeriesToolTipGenerator(series, (xyDataset, seriesx, item) -> {
                return "%.0fm  %s".formatted(distance, groundwaterPoint.getName());
            });

            gwRenderer.setSeriesVisibleInLegend(series, true);
            gwRenderer.setSeriesPaint(series, color, true);
            var width = i == 0 ? 3f : 1.5f;
            gwRenderer.setSeriesStroke(series, new BasicStroke(width));
        }
    }

    public synchronized void plot2(XYPlot plot, BBasePoint p, LocalDate aStartDate) {
        mDataset.removeAllSeries();
        plot.setRangeAxis(index, null);
        plot.setDataset(index, mDataset);
        //        plot.mapDatasetToRangeAxis(index, null);
//        plot.setRangeAxisLocation(index, AxisLocation.);
        plot.setRenderer(index, null);
        if (!mObjectStorageManager.getBoolean(GroundwaterChartSOSB.class, false)) {
            return;
        }

        var startDate = aStartDate == null ? LocalDate.now().minusYears(5) : aStartDate;
        var groundwaterPoints = ButterflyHelper.getGroundwaterPoints(p, 100, 5, startDate);
        mAxis.setAutoRangeIncludesZero(false);
//        gwAxis.setAutoRange(true);
        plot.setRangeAxis(index, mAxis);
        plot.setDataset(index, mDataset);
        plot.mapDatasetToRangeAxis(index, index);
        plot.setRangeAxisLocation(index, AxisLocation.BOTTOM_OR_RIGHT);
        plot.setRenderer(index, mRenderer);
        var color = GraphicsHelper.colorAddAlpha(Color.BLUE, 80);

        for (int i = 0; i < groundwaterPoints.size(); i++) {
            if (i > 0) {
                color = GraphicsHelper.brighten(color, 0.25);
            }
            var groundwaterPoint = groundwaterPoints.get(i);
            var distance = groundwaterPoint.<Double>getValue(ButterflyHelper.KEY_DISTANCE);
            var timeSeries = new TimeSeries("%c.%.0f".formatted('A' + i, distance));

            for (var o : groundwaterPoint.ext().getObservationsTimeFiltered()) {
                if (o.getDate().isAfter(startDate.atStartOfDay())) {
                    timeSeries.addOrUpdate(ChartHelper.convertToMinute(o.getDate()), o.getGroundwaterLevel());
                }
            }

            mDataset.addSeries(timeSeries);

            int series = mDataset.getSeriesIndex(timeSeries.getKey());
            mRenderer.setSeriesToolTipGenerator(series, (xyDataset, seriesx, item) -> {
                return "%.0fm  %s".formatted(distance, groundwaterPoint.getName());
            });

            mRenderer.setSeriesVisibleInLegend(series, true);
            mRenderer.setSeriesPaint(series, color, true);
            var width = i == 0 ? 3f : 1.5f;
            mRenderer.setSeriesStroke(series, new BasicStroke(width));
        }
    }
     */
}

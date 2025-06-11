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
package org.mapton.butterfly_structural.crack.chart;

import java.awt.Color;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.structural.BStructuralCrackPoint;
import org.mapton.butterfly_structural.crack.CrackHelper;
import org.mapton.butterfly_structural.crack.CrackManager;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.CircularInt;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class CrackChartBuilder extends XyzChartBuilder<BStructuralCrackPoint> {

    private final CircularInt mColorCircularInt = new CircularInt(0, 5);
    private final XYLineAndShapeRenderer mSecondaryRenderer = new XYLineAndShapeRenderer();
    private final NumberAxis mTemperatureAxis = new NumberAxis("°C");
    private final TimeSeriesCollection mTemperatureDataset = new TimeSeriesCollection();
    private final TimeSeries mTimeSeriesTemperature = new TimeSeries("°C");
    private final TimeSeries mTimeSeriesZ = new TimeSeries("Δ µε");

    public CrackChartBuilder() {
        initChart("mm", null);

        var plot = (XYPlot) mChart.getPlot();
        plot.setRangeAxis(2, mTemperatureAxis);
        plot.setDataset(2, mTemperatureDataset);
        plot.mapDatasetToRangeAxis(2, 2);
        plot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_RIGHT);
        plot.setRenderer(2, mSecondaryRenderer);
    }

    @Override
    public synchronized Callable<ChartPanel> build(BStructuralCrackPoint p) {
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

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);
//            rangeAxis.setRange(-0.050, +0.050);

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public void setTitle(BStructuralCrackPoint p) {
        setTitle(p, CrackHelper.getAlarmColorAwt(p));

        var dateFirst = Objects.toString(DateHelper.toDateString(p.getDateZero()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        getLeftSubTextTitle().setText(date);

        var rightTitle = "%s: %s".formatted(p.getAlarm1Id(), p.ext().getDeltaZero());
        getRightSubTextTitle().setText(rightTitle);
    }

    @Override
    public synchronized void updateDataset(BStructuralCrackPoint p) {
        mTimeSeriesZ.clear();

        mTemperatureDataset.removeAllSeries();
        mTimeSeriesTemperature.clear();

        var plot = (XYPlot) mChart.getPlot();
        resetPlot(plot);

        plotBlasts(plot, p, p.ext().getObservationFilteredFirstDate(), p.ext().getObservationFilteredLastDate());
        plotMeasNeed(plot, p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));

        updateDatasetTemperature(p);

        var single = true;
        if (single) {
            updateDataset(p, Color.RED, true);
        } else {
            updateDataset(p, Color.RED, true);
            mColorCircularInt.set(0);
            CrackManager.getInstance().getTimeFilteredItems().stream()
                    .filter(pp -> {
                        return Math.hypot(pp.getZeroX() - p.getZeroX(), pp.getZeroY() - p.getZeroY()) < 1.0;
                    })
                    .filter(pp -> pp != p)
                    .forEach(pp -> {
                        updateDataset(pp, getColor(), false);
                    });
        }
    }

    private Color getColor() {
        var colors = new Color[]{
            Color.BLUE,
            Color.CYAN,
            Color.MAGENTA,
            Color.YELLOW,
            Color.GREEN,
            Color.ORANGE};

        return colors[mColorCircularInt.inc()];
    }

    private void updateDataset(BStructuralCrackPoint p, Color color, boolean plotZeroAndReplacement) {
        var plot = (XYPlot) mChart.getPlot();
        var timeSeries = new TimeSeries(p.getName());

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            addNEMarkers(plot, o, plotZeroAndReplacement);

            if (o.ext().getDeltaZ() != null) {
                var minute = ChartHelper.convertToMinute(o.getDate());
                timeSeries.addOrUpdate(minute, o.ext().getDeltaZ() * 1000);
            }
        });

        var renderer = plot.getRenderer();

        getDataset().addSeries(timeSeries);
        renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries.getKey()), color);
    }

    private void updateDatasetTemperature(BStructuralCrackPoint p) {
        p.ext().getObservationsTimeFiltered().forEach(o -> {
            var minute = ChartHelper.convertToMinute(o.getDate());
            if (MathHelper.isBetween(-40d, +40d, o.getTemperature())) {
                mTimeSeriesTemperature.addOrUpdate(minute, o.getTemperature());
            }
        });

        if (!mTimeSeriesTemperature.isEmpty()) {
            mTemperatureDataset.addSeries(mTimeSeriesTemperature);
            mSecondaryRenderer.setSeriesPaint(mTemperatureDataset.getSeriesIndex(mTimeSeriesTemperature.getKey()), Color.GRAY);
        }
    }
}

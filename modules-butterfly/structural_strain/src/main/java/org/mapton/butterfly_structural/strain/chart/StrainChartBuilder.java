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
package org.mapton.butterfly_structural.strain.chart;

import java.awt.Color;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.data.time.TimeSeries;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePoint;
import org.mapton.butterfly_structural.strain.StrainHelper;
import org.mapton.butterfly_structural.strain.api.StrainManager;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.CircularInt;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class StrainChartBuilder extends XyzChartBuilder<BStructuralStrainGaugePoint> {

    private final CircularInt mColorCircularInt = new CircularInt(0, 5);
    private final TimeSeries mTimeSeriesZ = new TimeSeries("Δ µε");

    public StrainChartBuilder() {
        initChart("Δ µε", "0");
    }

    @Override
    public synchronized Callable<ChartPanel> build(BStructuralStrainGaugePoint p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);
            var plot = getPlot();
            setDateRangeBySettings(plot, p);

            plot.clearRangeMarkers();
            plotAlarmIndicators(p);

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public void setTitle(BStructuralStrainGaugePoint p) {
        setTitle(p, StrainHelper.getAlarmColorAwt(p));

        var dateFirst = Objects.toString(DateHelper.toDateString(p.getDateZero()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        getLeftSubTextTitle().setText(date);

        var rightTitle = "%s: %s".formatted(p.getAlarm1Id(), p.ext().getDeltaZero());
        getRightSubTextTitle().setText(rightTitle);
    }

    @Override
    public synchronized void updateDataset(BStructuralStrainGaugePoint p) {
        mTimeSeriesZ.clear();

        var plot = getPlot();
        resetPlot(plot);

        plotOverlays(plot, p, p.ext().getObservationFilteredFirstDate());
        plotMeasNeed(plot, p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));

        var single = true;
        if (single) {
            updateDataset(p, Color.RED, true);
        } else {
            updateDataset(p, Color.RED, true);
            mColorCircularInt.set(0);
            StrainManager.getInstance().getTimeFilteredItems().stream()
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

    private void updateDataset(BStructuralStrainGaugePoint p, Color color, boolean plotZeroAndReplacement) {
        var plot = getPlot();
        var timeSeries = new TimeSeries(p.getName());

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            addNEMarkers(plot, o, plotZeroAndReplacement);

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
        renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries.getKey()), color);
        setRange(1.05, p.ext().getAlarm(BComponent.HEIGHT));
    }
}

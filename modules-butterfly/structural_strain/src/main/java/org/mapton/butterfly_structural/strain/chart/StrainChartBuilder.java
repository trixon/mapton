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

import java.awt.BasicStroke;
import java.awt.Color;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.LengthAdjustmentType;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePoint;
import org.mapton.butterfly_structural.strain.StrainHelper;
import org.mapton.butterfly_structural.strain.StrainManager;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.CircularInt;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class StrainChartBuilder extends XyzChartBuilder<BStructuralStrainGaugePoint> {

    private final CircularInt mColorCircularInt = new CircularInt(0, 5);
    private final XYLineAndShapeRenderer mSecondaryRenderer = new XYLineAndShapeRenderer();
    private final NumberAxis mTemperatureAxis = new NumberAxis("°C");
    private final TimeSeriesCollection mTemperatureDataset = new TimeSeriesCollection();
    private final TimeSeries mTimeSeriesTemperature = new TimeSeries("°C");
    private final TimeSeries mTimeSeriesZ = new TimeSeries("Δ µε");

    public StrainChartBuilder() {
        initChart("Δ µε", "0");

        var plot = (XYPlot) mChart.getPlot();
        plot.setRangeAxis(2, mTemperatureAxis);
        plot.setDataset(2, mTemperatureDataset);
        plot.mapDatasetToRangeAxis(2, 2);
        plot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_RIGHT);
        plot.setRenderer(2, mSecondaryRenderer);
    }

    @Override
    public synchronized Callable<ChartPanel> build(BStructuralStrainGaugePoint p) {
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

        mTemperatureDataset.removeAllSeries();
        mTimeSeriesTemperature.clear();

        var plot = (XYPlot) mChart.getPlot();
        resetPlot(plot);

        plotBlasts(plot, p, p.ext().getObservationFilteredFirstDate(), p.ext().getObservationFilteredLastDate());
        plotMeasNeed(plot, p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));

        updateDatasetTemperature(p);

        var single = false;
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

    private void plotAlarmIndicator(BComponent component, double value, Color color) {
        var marker = new ValueMarker(value);
        float width = 1.0f;
        float dash[] = {5.0f, 5.0f};
        var dashedStroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.5f, dash, 0);
        var stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.5f, null, 0);
        if (component == BComponent.HEIGHT) {
            marker.setStroke(dashedStroke);
        } else {
            marker.setStroke(stroke);
        }
        marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        marker.setPaint(color);

        var plot = (XYPlot) mChart.getPlot();
        plot.addRangeMarker(marker);
    }

    private void plotAlarmIndicators(BStructuralStrainGaugePoint p) {
        var ha = p.ext().getAlarm(BComponent.HEIGHT);
        if (ha != null) {
            var range0 = ha.ext().getRange0();
            if (range0 != null) {
                plotAlarmIndicator(BComponent.HEIGHT, range0.getMinimum(), Color.YELLOW);
                plotAlarmIndicator(BComponent.HEIGHT, range0.getMaximum(), Color.YELLOW);
            }

            var range1 = ha.ext().getRange1();
            if (range1 != null) {
                plotAlarmIndicator(BComponent.HEIGHT, range1.getMinimum(), Color.RED);
                plotAlarmIndicator(BComponent.HEIGHT, range1.getMaximum(), Color.RED);
            }
        }
    }

    private void updateDataset(BStructuralStrainGaugePoint p, Color color, boolean plotZeroAndReplacement) {
        var plot = (XYPlot) mChart.getPlot();
        var timeSeries = new TimeSeries(p.getName());

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            addNEMarkers(plot, o, plotZeroAndReplacement);

            if (o.ext().getDeltaZ() != null) {
                var minute = ChartHelper.convertToMinute(o.getDate());
                timeSeries.addOrUpdate(minute, o.ext().getDeltaZ());
            }
        });

        var renderer = plot.getRenderer();

        getDataset().addSeries(timeSeries);
        renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries.getKey()), color);
    }

    private void updateDatasetTemperature(BStructuralStrainGaugePoint p) {
        p.ext().getObservationsTimeFiltered().forEach(o -> {
            var minute = ChartHelper.convertToMinute(o.getDate());
            if (MathHelper.isBetween(-40d, +40d, o.getTemperature())) {
                mTimeSeriesTemperature.addOrUpdate(minute, o.getTemperature());
            }
        });

        mTemperatureDataset.addSeries(mTimeSeriesTemperature);
        mSecondaryRenderer.setSeriesPaint(mTemperatureDataset.getSeriesIndex(mTimeSeriesTemperature.getKey()), Color.GRAY);
    }
}

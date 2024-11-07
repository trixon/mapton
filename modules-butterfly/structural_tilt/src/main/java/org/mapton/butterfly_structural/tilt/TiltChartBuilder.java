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
package org.mapton.butterfly_structural.tilt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.LengthAdjustmentType;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.openide.util.Exceptions;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TiltChartBuilder extends XyzChartBuilder<BStructuralTiltPoint> {

    private final ChartHelper mChartHelper = new ChartHelper();
    private final XYLineAndShapeRenderer mSecondaryRenderer = new XYLineAndShapeRenderer();
    private final NumberAxis mTemperatureAxis = new NumberAxis("°C");
    private final TimeSeriesCollection mTemperatureDataset = new TimeSeriesCollection();
    private final TimeSeries mTimeSeriesTemperature = new TimeSeries("°C");
    private final TimeSeries mTimeSeriesX = new TimeSeries("Transversal");
    private final TimeSeries mTimeSeriesY = new TimeSeries("Longitudinell");
    private final TimeSeries mTimeSeriesZ = new TimeSeries("Resultant");

    public TiltChartBuilder() {
        initChart("mm/m", "0.0");

        var plot = (XYPlot) mChart.getPlot();
        plot.setRangeAxis(2, mTemperatureAxis);
        plot.setDataset(2, mTemperatureDataset);
        plot.mapDatasetToRangeAxis(2, 2);
        plot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_RIGHT);
        plot.setRenderer(2, mSecondaryRenderer);
    }

    @Override
    public synchronized Callable<ChartPanel> build(BStructuralTiltPoint p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);
            var plot = (XYPlot) mChart.getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            //dateAxis.setRange(DateHelper.convertToDate(mTemporalManager.getLowDate()), DateHelper.convertToDate(mTemporalManager.getHighDate()));
            dateAxis.setAutoRange(true);
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
    public void setTitle(BStructuralTiltPoint p) {
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

        var rightTitle = "%s: %s".formatted(p.getAlarm1Id(), p.ext().getDeltaZero());
        getRightSubTextTitle().setText(rightTitle);
    }

    @Override
    public synchronized void updateDataset(BStructuralTiltPoint p) {
        getDataset().removeAllSeries();
        mTimeSeriesX.clear();
        mTimeSeriesY.clear();
        mTimeSeriesZ.clear();

        mTemperatureDataset.removeAllSeries();
        mTimeSeriesTemperature.clear();

        var plot = (XYPlot) mChart.getPlot();
        plot.clearDomainMarkers();

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            var minute = mChartHelper.convertToMinute(o.getDate());
            if (o.isReplacementMeasurement()) {
                addMarker(plot, minute, "E", Color.RED);
            } else if (o.isZeroMeasurement()) {
                addMarker(plot, minute, "N", Color.BLUE);
            }

            mTimeSeriesX.add(minute, o.ext().getDeltaX());
            mTimeSeriesY.add(minute, o.ext().getDeltaY());
            mTimeSeriesZ.add(minute, o.ext().getDelta2d());
            mTimeSeriesTemperature.add(minute, o.getTemperature());
        });

        var renderer = plot.getRenderer();
        var avgStroke = new BasicStroke(5.0f);
        int avdDays = 90 * 60 * 24;
        int avgSkipMeasurements = 0;
        boolean plotAvg = true;

        getDataset().addSeries(mTimeSeriesX);
        renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeriesX.getKey()), Color.RED);
        if (plotAvg) {
            var mavg = MovingAverage.createMovingAverage(mTimeSeriesX, "%s (avg)".formatted(mTimeSeriesX.getKey()), avdDays, avgSkipMeasurements);
            getDataset().addSeries(mavg);
            int index = getDataset().getSeriesIndex(mavg.getKey());
            renderer.setSeriesPaint(index, Color.RED);
            renderer.setSeriesStroke(index, avgStroke);
        }

        getDataset().addSeries(mTimeSeriesY);
        renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeriesY.getKey()), Color.GREEN);
        if (plotAvg) {
            var mavg = MovingAverage.createMovingAverage(mTimeSeriesY, "%s (avg)".formatted(mTimeSeriesY.getKey()), avdDays, avgSkipMeasurements);
            try {
                getDataset().addSeries(mavg);
                int index = getDataset().getSeriesIndex(mavg.getKey());
                renderer.setSeriesPaint(index, Color.GREEN);
                renderer.setSeriesStroke(index, avgStroke);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }

        getDataset().addSeries(mTimeSeriesZ);
        renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeriesZ.getKey()), Color.BLUE);
        if (plotAvg) {
            var mavg = MovingAverage.createMovingAverage(mTimeSeriesZ, "%s (avg)".formatted(mTimeSeriesZ.getKey()), avdDays, avgSkipMeasurements);
            getDataset().addSeries(mavg);
            int index = getDataset().getSeriesIndex(mavg.getKey());
            renderer.setSeriesPaint(index, Color.BLUE);
            renderer.setSeriesStroke(index, avgStroke);
        }

        mTemperatureDataset.addSeries(mTimeSeriesTemperature);
        mSecondaryRenderer.setSeriesPaint(mTemperatureDataset.getSeriesIndex(mTimeSeriesTemperature.getKey()), Color.GRAY);
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

    private void plotAlarmIndicators(BStructuralTiltPoint p) {
        var ha = p.ext().getAlarm(BComponent.HEIGHT);
        if (ha != null) {
            var range0 = ha.ext().getRange0();
            if (range0 != null) {
                var min = TiltHelper.toRadianBased(range0.getMinimum());
                var max = TiltHelper.toRadianBased(range0.getMaximum());
                plotAlarmIndicator(BComponent.HEIGHT, min, Color.YELLOW);
                plotAlarmIndicator(BComponent.HEIGHT, max, Color.YELLOW);
            }

            var range1 = ha.ext().getRange1();
            if (range1 != null) {
                var min = TiltHelper.toRadianBased(range1.getMinimum());
                var max = TiltHelper.toRadianBased(range1.getMaximum());
                plotAlarmIndicator(BComponent.HEIGHT, min, Color.RED);
                plotAlarmIndicator(BComponent.HEIGHT, max, Color.RED);
            }
        }
    }
}

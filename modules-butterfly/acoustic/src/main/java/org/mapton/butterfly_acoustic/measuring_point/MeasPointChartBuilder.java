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
package org.mapton.butterfly_acoustic.measuring_point;

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
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.acoustic.BAcousticMeasuringPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class MeasPointChartBuilder extends XyzChartBuilder<BAcousticMeasuringPoint> {

    private final ChartHelper mChartHelper = new ChartHelper();
    private final NumberAxis mFreqAxis = new NumberAxis("Hz");
    private final TimeSeriesCollection mFreqDataset = new TimeSeriesCollection();
    private final XYLineAndShapeRenderer mSecondaryRenderer = new XYLineAndShapeRenderer();
    private final TimeSeries mTimeSeries2d = new TimeSeries(Dict.Geometry.PLANE);
    private final TimeSeries mTimeSeries3d = new TimeSeries("3d");
    private final TimeSeries mTimeSeriesFreqX = new TimeSeries("fX");
    private final TimeSeries mTimeSeriesFreqY = new TimeSeries("fY");
    private final TimeSeries mTimeSeriesFreqZ = new TimeSeries("fZ");
    private final TimeSeries mTimeSeriesH = new TimeSeries(Dict.Geometry.HEIGHT);

    public MeasPointChartBuilder() {
        initChart("mm/s", "0.00");

        var plot = (XYPlot) mChart.getPlot();
        plot.setRangeAxis(2, mFreqAxis);
        plot.setDataset(2, mFreqDataset);
        plot.mapDatasetToRangeAxis(2, 2);
        plot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_RIGHT);
        plot.setRenderer(2, mSecondaryRenderer);
    }

    @Override
    public synchronized Callable<ChartPanel> build(BAcousticMeasuringPoint p) {
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
    public void setTitle(BAcousticMeasuringPoint p) {
        setTitle(p, Color.BLUE);
//        setTitle(p, StrainHelper.getAlarmColorAwt(p));

        var dateFirst = Objects.toString(DateHelper.toDateString(p.getDateZero()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        getLeftSubTextTitle().setText(date);

//        var rightTitle = "%s: %s".formatted(p.getAlarm1Id(), p.ext().getDeltaZero());
//        getRightSubTextTitle().setText(rightTitle);
    }

    @Override
    public void updateDataset(BAcousticMeasuringPoint p) {
        getDataset().removeAllSeries();
        mTimeSeriesH.clear();
        mTimeSeries2d.clear();
        mTimeSeries3d.clear();

        mFreqDataset.removeAllSeries();
        mTimeSeriesFreqX.clear();
        mTimeSeriesFreqY.clear();
        mTimeSeriesFreqZ.clear();

        var plot = (XYPlot) mChart.getPlot();
        plot.clearDomainMarkers();
        p.ext().getObservationsTimeFiltered().forEach(o -> {
            var minute = mChartHelper.convertToMinute(o.getDate());

            if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
                mTimeSeriesH.add(minute, o.ext().getDeltaZ());
            }

            if (p.getDimension() == BDimension._2d || p.getDimension() == BDimension._3d) {
                mTimeSeries2d.add(minute, o.ext().getDelta2d());
            }

            if (p.getDimension() == BDimension._3d) {
                try {
                    mTimeSeries3d.add(minute, Math.abs(o.ext().getDelta3d()));
                } catch (NullPointerException e) {
                    System.err.println("Failed to add observation to chart %s %s".formatted(p.getName(), o.getDate()));
                }
            }

            mTimeSeriesFreqX.add(minute, o.getFrequencyX());
            mTimeSeriesFreqY.add(minute, o.getFrequencyY());
            mTimeSeriesFreqZ.add(minute, o.getFrequencyZ());

        });

        mFreqDataset.addSeries(mTimeSeriesFreqX);
        mFreqDataset.addSeries(mTimeSeriesFreqY);
        mFreqDataset.addSeries(mTimeSeriesFreqZ);

        mSecondaryRenderer.setSeriesPaint(mFreqDataset.getSeriesIndex(mTimeSeriesFreqX.getKey()), Color.YELLOW);
        mSecondaryRenderer.setSeriesPaint(mFreqDataset.getSeriesIndex(mTimeSeriesFreqY.getKey()), Color.CYAN);
        mSecondaryRenderer.setSeriesPaint(mFreqDataset.getSeriesIndex(mTimeSeriesFreqZ.getKey()), Color.MAGENTA);

        var renderer = plot.getRenderer();

        if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
            getDataset().addSeries(mTimeSeriesH);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeriesH.getKey()), Color.RED);
        }

        if (p.getDimension() == BDimension._2d || p.getDimension() == BDimension._3d) {
            getDataset().addSeries(mTimeSeries2d);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeries2d.getKey()), Color.GREEN);
        }

        if (p.getDimension() == BDimension._3d) {
            getDataset().addSeries(mTimeSeries3d);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeries3d.getKey()), Color.BLUE);
        }
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

    private void plotAlarmIndicators(BAcousticMeasuringPoint p) {
        BAlarm ha = null;
//        var ha = p.ext().getAlarm(BComponent.HEIGHT);
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
}

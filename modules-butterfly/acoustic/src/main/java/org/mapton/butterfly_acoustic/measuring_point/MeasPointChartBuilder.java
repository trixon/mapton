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

import java.awt.Color;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.acoustic.BAcousticMeasuringPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MeasPointChartBuilder extends XyzChartBuilder<BAcousticMeasuringPoint> {

    private final ChartHelper mChartHelper = new ChartHelper();
    private final NumberAxis mFreqAxis = new NumberAxis("Hz");
    private final TimeSeriesCollection mFreqDataset = new TimeSeriesCollection();
    private final XYLineAndShapeRenderer mSecondaryRenderer = new XYLineAndShapeRenderer();
    private final TimeSeries mTimeSeriesFreqZ = new TimeSeries("Frekvens");
    private final TimeSeries mTimeSeriesLimit = new TimeSeries("Riktvärde");
    private final TimeSeries mTimeSeriesZ = new TimeSeries("Mark Z");

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
        mFreqDataset.removeAllSeries();
        clear(
                mTimeSeriesZ,
                mTimeSeriesLimit,
                mTimeSeriesFreqZ
        );

        var plot = (XYPlot) mChart.getPlot();
        plot.clearDomainMarkers();
        p.ext().getObservationsTimeFiltered().forEach(o -> {
            var minute = mChartHelper.convertToMinute(o.getDate());

            if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
                mTimeSeriesZ.add(minute, o.getMeasuredZ());
            }

            mTimeSeriesLimit.add(minute, o.getLimit());
            mTimeSeriesFreqZ.add(minute, o.getFrequencyZ());

        });

        mFreqDataset.addSeries(mTimeSeriesFreqZ);

        mSecondaryRenderer.setSeriesPaint(mFreqDataset.getSeriesIndex(mTimeSeriesFreqZ.getKey()), Color.YELLOW.darker());

        var renderer = plot.getRenderer();
        getDataset().addSeries(mTimeSeriesLimit);
        renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeriesLimit.getKey()), Color.GRAY);

        if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
            getDataset().addSeries(mTimeSeriesZ);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeriesZ.getKey()), Color.PINK);
        }
    }
}

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
package org.mapton.butterfly_topo;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.api.MTemporalManager;
import org.mapton.api.ui.forms.ChartBuilder;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class TopoChartBuilder extends ChartBuilder<BTopoControlPoint> {

    private JFreeChart mChart;
    private final ChartHelper mChartHelper = new ChartHelper();
    private ChartPanel mChartPanel;
    private final TimeSeriesCollection mDataset = new TimeSeriesCollection();
    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private final TimeSeries mTimeSeries2d = new TimeSeries("2d");
    private final TimeSeries mTimeSeries3d = new TimeSeries("3d");
    private final TimeSeries mTimeSeriesE = new TimeSeries("E");
    private final TimeSeries mTimeSeriesH = new TimeSeries("H");
    private final TimeSeries mTimeSeriesN = new TimeSeries("N");

    public TopoChartBuilder() {
        initChart();
    }

    @Override
    public synchronized Callable<ChartPanel> build(BTopoControlPoint p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            mChart.setTitle(p.getName());
            updateDataset(p);
            var plot = (XYPlot) mChart.getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setRange(DateHelper.convertToDate(mTemporalManager.getLowDate()), DateHelper.convertToDate(mTemporalManager.getHighDate()));

            return mChartPanel;
        };

        return callable;
    }

    private void initChart() {
        mChart = ChartFactory.createTimeSeriesChart(
                "",
                Dict.DATE.toString(),
                "m",
                mDataset,
                true,
                true,
                false
        );

        mChart.setBackgroundPaint(Color.white);

        var plot = (XYPlot) mChart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        var itemRenderer = plot.getRenderer();
        if (itemRenderer instanceof XYLineAndShapeRenderer renderer) {
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
        }

        var dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
        dateAxis.setAutoRange(false);

        var rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRange(false);
        rangeAxis.setRange(-0.050, +0.050);

        mChartPanel = new ChartPanel(mChart);
        mChartPanel.setMouseZoomable(true, false);
        mChartPanel.setDisplayToolTips(true);
//        mChartPanel.setDomainZoomable(true);
        mChartPanel.setMouseWheelEnabled(true);
    }

    private void updateDataset(BTopoControlPoint p) {
        mDataset.removeAllSeries();
        mTimeSeriesN.clear();
        mTimeSeriesE.clear();
        mTimeSeriesH.clear();
        mTimeSeries2d.clear();
        mTimeSeries3d.clear();

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            var minute = mChartHelper.convertToMinute(o.getDate());
            if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
                mTimeSeriesH.add(minute, o.ext().getDeltaZ());
            }
            if (p.getDimension() == BDimension._2d || p.getDimension() == BDimension._3d) {
                mTimeSeriesN.add(minute, o.ext().getDeltaY());
                mTimeSeriesE.add(minute, o.ext().getDeltaX());
                mTimeSeries2d.add(minute, o.ext().getDelta2d());
            }
            if (p.getDimension() == BDimension._3d) {
                mTimeSeries3d.add(minute, o.ext().getDelta3d());
            }
        });

        if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
            mDataset.addSeries(mTimeSeriesH);
        }

        if (p.getDimension() == BDimension._2d || p.getDimension() == BDimension._3d) {
            //mDataset.addSeries(mTimeSeriesN);
            //mDataset.addSeries(mTimeSeriesE);
            mDataset.addSeries(mTimeSeries2d);
        }

        if (p.getDimension() == BDimension._3d) {
            mDataset.addSeries(mTimeSeries3d);
        }
    }
}

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
package org.mapton.butterfly_topo_convergence.pair;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.EmptyBlock;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.api.MKey;
import org.mapton.api.MLatLon;
import org.mapton.api.MTemporalManager;
import org.mapton.api.Mapton;
import org.mapton.api.ui.forms.ChartBuilder;
import org.mapton.butterfly_core.api.ButterflyManager;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePairObservation;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergencePairChartBuilder extends ChartBuilder<BTopoConvergencePair> {

    private JFreeChart mChart;
    private final ChartHelper mChartHelper = new ChartHelper();
    private ChartPanel mChartPanel;
    private final TimeSeriesCollection mDataset = new TimeSeriesCollection();
    private TextTitle mDateSubTextTitle;
    private TextTitle mDeltaSubTextTitle;
    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private final TimeSeries mTimeSeriesBlast = new TimeSeries("Salvor inom 100 m");
    private final TimeSeries mTimeSeriesDeltaL = new TimeSeries("Längdförändring");
    private final TimeSeries mTimeSeriesDeltaV = new TimeSeries("Hastighet");

    public ConvergencePairChartBuilder() {
        initChart();

        Mapton.getGlobalState().addListener(gsce -> {
            plotChartForNode(gsce.getValue());
        }, ConvergencePairChartBuilder.class.getName() + "node");
    }

    @Override
    public synchronized Callable<ChartPanel> build(BTopoConvergencePair p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);
            var plot = (XYPlot) mChart.getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setAutoRange(true);

            plot.clearRangeMarkers();

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);

            return mChartPanel;
        };

        return callable;
    }

    @Override
    public void setTitle(BTopoConvergencePair p) {
        mChart.setTitle(p.getName());
    }

    @Override
    public void updateDataset(BTopoConvergencePair p) {
        mDataset.removeAllSeries();
        mTimeSeriesDeltaL.clear();
        mTimeSeriesDeltaV.clear();
        mTimeSeriesBlast.clear();

        var plot = (XYPlot) mChart.getPlot();
        plot.clearDomainMarkers();

        BTopoConvergencePairObservation prevO = null;
        for (var o : p.getObservations()) {
            var minute = mChartHelper.convertToMinute(o.getDate());
            mTimeSeriesDeltaL.add(minute, o.getDeltaDeltaDistanceComparedToFirst() * 1000);

            var velocity = 0.0;
            if (prevO != null) {
                var delta = o.getDeltaDistanceInPairForSameDate() - prevO.getDeltaDistanceInPairForSameDate();
                var timeSpan = prevO.getDate().until(o.getDate(), ChronoUnit.HOURS);
                velocity = delta / (timeSpan / 168.0) * 1000;
            }

            mTimeSeriesDeltaV.add(minute, Math.abs(velocity));

            prevO = o;
        }
        if (!p.getObservations().isEmpty()) {
            plotBlasts(p);
        }
        mDataset.addSeries(mTimeSeriesDeltaL);
        mDataset.addSeries(mTimeSeriesDeltaV);
        if (!mTimeSeriesBlast.isEmpty()) {
            mDataset.addSeries(mTimeSeriesBlast);
        }
    }

    private void initChart() {
        mChart = ChartFactory.createTimeSeriesChart(
                "",
                Dict.DATE.toString(),
                "mm",
                mDataset,
                true,
                true,
                false
        );

        mChart.setBackgroundPaint(Color.white);
        mChart.getTitle().setBackgroundPaint(Color.LIGHT_GRAY);
        mChart.getTitle().setExpandToFitSpace(true);

        var plot = (XYPlot) mChart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        var yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setNumberFormatOverride(new DecimalFormat("0.00"));

        var itemRenderer = plot.getRenderer();
        if (itemRenderer instanceof XYLineAndShapeRenderer renderer) {
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
        }

        var dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
        dateAxis.setAutoRange(true);

        mChartPanel = new ChartPanel(mChart);
        mChartPanel.setMouseZoomable(true, false);
        mChartPanel.setDisplayToolTips(true);
//        mChartPanel.setDomainZoomable(true);
        mChartPanel.setMouseWheelEnabled(false);

        var font = new Font("monospaced", Font.BOLD, SwingHelper.getUIScaled(12));
        mDateSubTextTitle = new TextTitle("", font, Color.BLACK, RectangleEdge.TOP, HorizontalAlignment.LEFT, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);
        mDeltaSubTextTitle = new TextTitle("", font, Color.BLACK, RectangleEdge.TOP, HorizontalAlignment.RIGHT, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);

        var blockContainer = new BlockContainer(new BorderArrangement());
        blockContainer.add(mDateSubTextTitle, RectangleEdge.LEFT);
        blockContainer.add(mDeltaSubTextTitle, RectangleEdge.RIGHT);
        blockContainer.add(new EmptyBlock(2000, 0));

        var compositeTitle = new CompositeTitle(blockContainer);
        compositeTitle.setPadding(new RectangleInsets(0, 20, 0, 20));
        mChart.addSubtitle(compositeTitle);
    }

    private void plotBlasts(BTopoConvergencePair p) {
        ButterflyManager.getInstance().getButterfly().acoustic().getBlasts().forEach(b -> {
            var ll1 = new MLatLon(b.getLat(), b.getLon());
            var ll2 = new MLatLon(p.getP1().getLat(), p.getP1().getLon());

            if (ll1.distance(ll2) <= 100 && DateHelper.isBetween(p.getObservations().getFirst().getDate().toLocalDate(),
                    p.getObservations().getLast().getDate().toLocalDate(),
                    b.getDateTime().toLocalDate())) {
                try {
                    var minute = mChartHelper.convertToMinute(b.getDateTime());
                    mTimeSeriesBlast.add(minute, -20.0);
                } catch (SeriesException e) {
                    //
                }
            }
        });
    }

    private void plotChartForNode(String node) {
        var callable = (Callable<ChartPanel>) () -> {
            mChart.setTitle(node);
            updateDataset(node);
            var plot = (XYPlot) mChart.getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setAutoRange(true);

            plot.clearRangeMarkers();

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);

            return mChartPanel;
        };

        Mapton.getGlobalState().put(MKey.CHART, callable);
    }

    private void updateDataset(String node) {
        mDataset.removeAllSeries();

        var plot = (XYPlot) mChart.getPlot();
        plot.clearDomainMarkers();
        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();

        propertyMap.put(cat1 + "#Origin", node);

        var pairs = ConvergencePairManager.getInstance().getFilteredItems().filtered(p -> StringUtils.equalsAnyIgnoreCase(node, p.getP1().getName(), p.getP2().getName()));
        int i = 1;
        for (var pair : pairs) {
            var theOtherOne = StringHelper.getTheOtherOne(node, pair.getP1().getName(), pair.getP2().getName());
            var series = new TimeSeries(theOtherOne);
            pair.getObservations().forEach(o -> {
                var minute = mChartHelper.convertToMinute(o.getDate());
                series.add(minute, o.getDeltaDeltaDistanceComparedToFirst() * 1000);
            });

            propertyMap.put(cat1 + "#" + theOtherOne, "%.1f mm".formatted(pair.getDeltaDistanceOverTime() * 1000));
            Mapton.getGlobalState().put(MKey.OBJECT_PROPERTIES, propertyMap);
            mDataset.addSeries(series);
        }
    }
}

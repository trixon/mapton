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
package org.mapton.butterfly_topo.heightpair.chart;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
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
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.api.MTemporalManager;
import org.mapton.api.ui.forms.ChartBuilder;
import org.mapton.butterfly_format.types.topo.BTopoHeightPair;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoHeightPairChartBuilder extends ChartBuilder<BTopoHeightPair> {

    private JFreeChart mChart;
    private ChartPanel mChartPanel;
    private final TimeSeriesCollection mDataset = new TimeSeriesCollection();
    private TextTitle mDateSubTextTitle;
    private TextTitle mDeltaSubTextTitle;
    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private final TimeSeries mTimeSeriesH = new TimeSeries("Höjdskillnadsförändring");

    public TopoHeightPairChartBuilder() {
        initChart();
    }

    @Override
    public synchronized Callable<ChartPanel> build(BTopoHeightPair p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);
            var plot = (XYPlot) mChart.getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setRange(DateHelper.convertToDate(mTemporalManager.getLowDate()), DateHelper.convertToDate(mTemporalManager.getHighDate()));

            plot.clearRangeMarkers();

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);

            return mChartPanel;
        };

        return callable;
    }

    @Override
    public void setTitle(BTopoHeightPair p) {
        mChart.setTitle(p.getName());
    }

    @Override
    public void updateDataset(BTopoHeightPair p) {
        mDataset.removeAllSeries();
        mTimeSeriesH.clear();

        var plot = (XYPlot) mChart.getPlot();
        plot.clearDomainMarkers();
        for (var entry : p.getCommonObservations().entrySet()) {
            var date = entry.getKey();
            var o = entry.getValue();
            var minute = ChartHelper.convertToMinute(date.atTime(12, 0));
            var pairDiff = p.ext().getDiff(p.getFirstObservation(), o);

            mTimeSeriesH.addOrUpdate(minute, pairDiff.getPartialDiffZ());
//            mTimeSeriesH.addOrUpdate(minute, new Random().nextDouble(-3d, +3d));
            System.out.println(entry.getKey());
        }

        mDataset.addSeries(mTimeSeriesH);
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
        yAxis.setNumberFormatOverride(new DecimalFormat("0.000"));

        var itemRenderer = plot.getRenderer();
        if (itemRenderer instanceof XYLineAndShapeRenderer renderer) {
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
        }

        var dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
        dateAxis.setAutoRange(false);

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
}

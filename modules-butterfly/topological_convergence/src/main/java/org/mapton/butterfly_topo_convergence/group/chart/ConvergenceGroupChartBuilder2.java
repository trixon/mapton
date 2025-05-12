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
package org.mapton.butterfly_topo_convergence.group.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.mapton.api.MTemporalManager;
import org.mapton.api.ui.forms.ChartBuilder;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergenceGroupChartBuilder2 extends ChartBuilder<BTopoConvergenceGroup> {

    private JFreeChart mChart;
    private ChartPanel mChartPanel;
    private final XYSeriesCollection mDataset = new XYSeriesCollection();
    private TextTitle mDateSubTextTitle;
    private TextTitle mDeltaSubTextTitle;
    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();

    public ConvergenceGroupChartBuilder2() {
        initChart();
    }

    @Override
    public synchronized Callable<ChartPanel> build(BTopoConvergenceGroup p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);

            return mChartPanel;
        };

        return callable;
    }

    @Override
    public void setTitle(BTopoConvergenceGroup p) {

        mChart.setTitle(p.getName());
    }

    @Override
    public void updateDataset(BTopoConvergenceGroup p) {
        mDataset.removeAllSeries();
        var title = p.ext2().getControlPointsWithoutAnchor().stream()
                .map(pp -> pp.getName())
                .collect(Collectors.joining(", "));
        var series = new XYSeries(title);
        p.ext2().getProjected2dCoordinates().forEach(pp -> series.add(pp.getX(), pp.getY()));
        mDataset.addSeries(series);
    }

    private void initChart() {
        mChart = ChartFactory.createScatterPlot("",
                "", "", mDataset, PlotOrientation.VERTICAL, true, false, false);
        mChartPanel = new ChartPanel(mChart);
        mChartPanel.setMouseZoomable(true, false);
//        mChartPanel.setDisplayToolTips(true);
//        mChartPanel.setDomainZoomable(true);
        mChartPanel.setMouseWheelEnabled(false);

        var plot = (XYPlot) mChart.getPlot();
        plot.setNoDataMessage("NO DATA");

        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);

        plot.setDomainGridlineStroke(new BasicStroke(0.0f));
        plot.setDomainMinorGridlineStroke(new BasicStroke(0.0f));
        plot.setDomainGridlinePaint(Color.BLUE);
        plot.setRangeGridlineStroke(new BasicStroke(0.0f));
        plot.setRangeMinorGridlineStroke(new BasicStroke(0.0f));
        plot.setRangeGridlinePaint(Color.BLUE);

        plot.setDomainMinorGridlinesVisible(true);
        plot.setRangeMinorGridlinesVisible(true);

        var renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesOutlinePaint(0, Color.black);
        renderer.setUseOutlinePaint(true);

        var domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);

        domainAxis.setTickMarkInsideLength(2.0f);
        domainAxis.setTickMarkOutsideLength(2.0f);

        domainAxis.setMinorTickCount(2);
        domainAxis.setMinorTickMarksVisible(true);
//        domainAxis.setVisible(false);

        var rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickMarkInsideLength(2.0f);
        rangeAxis.setTickMarkOutsideLength(2.0f);
        rangeAxis.setMinorTickCount(2);
        rangeAxis.setMinorTickMarksVisible(true);
//        rangeAxis.setVisible(false);
    }
}

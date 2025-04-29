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
package org.mapton.butterfly_acoustic.blast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BMultiChartComponent;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.acoustic.BAcousticBlast;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BlastMultiChartPartBuilder extends XyzChartBuilder<BAcousticBlast> {

    private LocalDate mDateFirst;
    private LocalDate mDateLast;
    private BMultiChartComponent mDisruptorInfluent;
    private final String mTitlePrefix;
    private int mPointSize;

    public BlastMultiChartPartBuilder(String titlePrefix, String axisLabel, String decimalPattern) {
        mTitlePrefix = titlePrefix;
        initChart(axisLabel, decimalPattern);
    }

    public synchronized Callable<ChartPanel> build(BAcousticBlast p, BMultiChartComponent disruptorInfluent) {
        if (p == null) {
            return null;
        }
        mDisruptorInfluent = disruptorInfluent;
        var callable = (Callable<ChartPanel>) () -> {
            mDateFirst = p.ext().getDateFirst().toLocalDate().minusMonths(2);
            mDateLast = p.ext().getDateFirst().toLocalDate().plusMonths(2);
            setTitle(p);
            updateDataset(p);
            var plot = (XYPlot) mChart.getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setRange(DateHelper.convertToDate(mDateFirst), DateHelper.convertToDate(mDateLast));
            plot.clearRangeMarkers();

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public Object build(BAcousticBlast selectedObject) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getPointSize() {
        return mPointSize;
    }

    @Override
    public void setTitle(BAcousticBlast p) {
        mChart.setTitle("%s: %s".formatted(mTitlePrefix, p.getName()));

//        setTitle(p, Color.BLUE);
        var date = "%s ← (%s) → %s".formatted(mDateFirst, p.ext().getDateFirst().toLocalDate(), mDateLast);
        getLeftSubTextTitle().setText(date);

        var rightTitle = "Z = %.1f".formatted(p.getZeroZ());
        getRightSubTextTitle().setText(rightTitle);
    }

    @Override
    public void updateDataset(BAcousticBlast b) {
        getDataset().removeAllSeries();
        var plot = (XYPlot) mChart.getPlot();
        plot.clearDomainMarkers();
        var latLon = new MLatLon(b.getLat(), b.getLon());
        var points = mDisruptorInfluent.getPointsAndSeries(latLon, mDateFirst, mDateLast);
        mPointSize = points.size();
        for (var p : points) {
            var timeSeries = new TimeSeries(p.getName());
            TreeMap<LocalDateTime, Double> map = p.getValue(BMultiChartComponent.class);
            if (map != null) {
                for (var entry : map.entrySet()) {
                    var date = entry.getKey();
                    var z = entry.getValue();
                    var minute = mChartHelper.convertToMinute(date);
                    timeSeries.addOrUpdate(minute, z);
                }
            }
            getDataset().addSeries(timeSeries);
        }

        plotBlasts(plot, b, mDateFirst, mDateLast);
    }
}

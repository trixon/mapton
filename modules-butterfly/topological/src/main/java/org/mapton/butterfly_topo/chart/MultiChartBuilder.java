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
package org.mapton.butterfly_topo.chart;

import com.sun.jna.platform.KeyboardUtils;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BMultiChartPart;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MultiChartBuilder extends XyzChartBuilder<BTopoControlPoint> {

    private LocalDate mDateFirst;
    private LocalDate mDateLast;
    private BMultiChartPart mMultiChartComponent;
    private final String mTitlePrefix;
    private int mPointSize;

    public MultiChartBuilder(String titlePrefix, String axisLabel, String decimalPattern) {
        mTitlePrefix = titlePrefix;
        initChart(axisLabel, decimalPattern);
    }

    public synchronized Callable<ChartPanel> build(BTopoControlPoint p, BMultiChartPart multiChartComponent) {
        if (p == null) {
            return null;
        }

        mMultiChartComponent = multiChartComponent;
        var callable = (Callable<ChartPanel>) () -> {
            mDateFirst = LocalDate.now().minusMonths(3);
            mDateLast = LocalDate.now();
            setTitle(p);
            updateDataset(p);
            var plot = (XYPlot) mChart.getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setRange(DateHelper.convertToDate(mDateFirst), DateHelper.convertToDate(mDateLast));
            plot.clearRangeMarkers();

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);

            getChartPanel().addChartMouseListener(new ChartMouseListener() {
                @Override
                public void chartMouseClicked(ChartMouseEvent event) {
                    var e = event.getEntity();
                    if (e != null) {
                        var name = "";
                        if (event.getEntity() instanceof XYItemEntity entity) {
                            name = getDataset().getSeriesKey(entity.getSeriesIndex()).toString();
                        } else if (e instanceof LegendItemEntity entity) {
                            name = entity.getSeriesKey().toString();
                        }

                        if (!name.isBlank()) {
                            var isKeyPressed = KeyboardUtils.isPressed(KeyEvent.VK_SHIFT);
                            mMultiChartComponent.panTo(name);
                            if (isKeyPressed) {
                                mMultiChartComponent.select(name);
                            }
                        }
                    }
                }

                @Override
                public void chartMouseMoved(ChartMouseEvent event) {
                    //nvm
                }
            });

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public Object build(BTopoControlPoint selectedObject) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getPointSize() {
        return mPointSize;
    }

    @Override
    public void setTitle(BTopoControlPoint b) {
        mChart.setTitle("%s: %s".formatted(mTitlePrefix, b.getName()));

//        setTitle(p, Color.BLUE);
        var date = "%s ← (%s) → %s".formatted(mDateFirst, b.ext().getDateLatest().toLocalDate(), mDateLast);
        getLeftSubTextTitle().setText(date);

        var rightTitle = "Z = %.1f".formatted(b.getZeroZ());
        getRightSubTextTitle().setText(rightTitle);
    }

    @Override
    public void updateDataset(BTopoControlPoint p) {
        var plot = (XYPlot) mChart.getPlot();
        resetPlot(plot);

        var latLon = new MLatLon(p.getLat(), p.getLon());
        var points = mMultiChartComponent.getPoints(latLon, mDateFirst, p.ext().getDateFirst().toLocalDate(), mDateLast);
        mPointSize = points.size();

        updateDataset2(p);
        points.stream().filter(pp -> pp != p).forEachOrdered(pp -> updateDataset2(pp));
    }

    public void updateDataset2(BXyzPoint p) {
        var timeSeries = new TimeSeries(p.getName());
        TreeMap<LocalDateTime, Double> map = p.getValue(BMultiChartPart.class);
        if (map != null) {
            for (var entry : map.entrySet()) {
                var date = entry.getKey();
                var z = entry.getValue();
                var minute = ChartHelper.convertToMinute(date);
                timeSeries.addOrUpdate(minute, z);
            }
        }
        getDataset().addSeries(timeSeries);
    }
}

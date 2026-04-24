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
package org.mapton.butterfly_core.chart.cluster;

import com.sun.jna.platform.KeyboardUtils;
import java.awt.BasicStroke;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.function.Function;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.BMultiChartPart;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.butterfly_format.types.rock.BRockBlast;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class DynamicClusterMultiChartBuilder extends XyzChartBuilder<BRockBlast> {

    private LocalDate mDateFirst;
    private LocalDate mDateLast;
    private BMultiChartPart mMultiChartComponent;
    private int mPointSize;
    private final String mTitlePrefix;

    public DynamicClusterMultiChartBuilder(String titlePrefix, String axisLabel, String decimalPattern) {
        mTitlePrefix = titlePrefix;
        initChart(axisLabel, decimalPattern);
    }

    public synchronized Callable<ChartPanel> build(BRockBlast p, BMultiChartPart multiChartComponent) {
        if (p == null) {
            return null;
        }
        mMultiChartComponent = multiChartComponent;
        var callable = (Callable<ChartPanel>) () -> {
            mDateFirst = LocalDate.MAX;
            mDateLast = LocalDate.MIN;
            updateDataset(p);
            setTitle(p);
            var plot = getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();

            if (getPointSize() > 0) {
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
            }

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public Object build(BRockBlast selectedObject) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getPointSize() {
        return mPointSize;
    }

    @Override
    public void setTitle(BRockBlast b) {
        mChart.setTitle("%s: %s".formatted(b.getName(), mTitlePrefix));
        var date = "%s → %s".formatted(mDateFirst, mDateLast);
        getLeftSubTextTitle().setText(date);
    }

    @Override
    public void updateDataset(BRockBlast b) {
        var plot = getPlot();
        resetPlot(plot);
        var renderer = new XYLineAndShapeRenderer(true, true);
        var stroke = new BasicStroke(2.0f);
        renderer.setDefaultStroke(stroke);
        plot.setRenderer(renderer);
        var latLon = new MLatLon(b.getLat(), b.getLon());
        var points = mMultiChartComponent.getPoints(latLon, mDateFirst, b.ext().getDateFirst().toLocalDate(), mDateLast);
        var seriesList = new ArrayList<TimeSeries>();

        for (var p : points) {
            var timeSeries = new TimeSeries(p.getName());
            Function<BXyzPointObservation, Double> function = p.getValue(BKey.CLUSTER_CHART_FUNCTION);
            if (function != null) {
                for (var o : p.extOrNull().getObservationsTimeFiltered()) {
                    var date = o.getDate();
                    var minute = ChartHelper.convertToMinute(date);
                    timeSeries.addOrUpdate(minute, function.apply(o));
                    if (DateHelper.isBeforeOrEqual(date, mDateFirst.atStartOfDay())) {
                        mDateFirst = date.toLocalDate();
                    }
                    if (DateHelper.isAfterOrEqual(date, mDateLast.atStartOfDay())) {
                        mDateLast = date.toLocalDate();
                    }
                }
                seriesList.add(timeSeries);
            }
        }

        var orderedList = seriesList.stream()
                .sorted(Comparator.comparingDouble(ts -> {
                    var count = ts.getItemCount();
                    if (count == 0) {
                        return Double.NEGATIVE_INFINITY;
                    }
                    var lastItem = ts.getDataItem(count - 1);
                    var value = lastItem.getValue();
                    return (value == null) ? Double.NaN : value.doubleValue();
                }))
                .limit(10)
                .toList();

        orderedList.forEach(timeSeries -> getDataset().addSeries(timeSeries));
        for (int i = 0; i < getDataset().getSeriesCount(); i++) {
            renderer.setSeriesStroke(i, stroke);
        }
        mPointSize = orderedList.size();

        plotOverlays(plot, b, mDateFirst);
    }
}

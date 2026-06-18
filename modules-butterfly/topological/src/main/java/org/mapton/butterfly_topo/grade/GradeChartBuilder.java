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
package org.mapton.butterfly_topo.grade;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.time.TimeSeries;
import org.mapton.api.MTemporalManager;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.TopoHelper;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GradeChartBuilder extends XyzChartBuilder<BTopoGrade> {

    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();

    public GradeChartBuilder() {
        initChart("mm/m", "0.0");
    }

    @Override
    public synchronized Callable<ChartPanel> build(BTopoGrade p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            updateDataset(p);
            var plot = getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setAutoRange(true);

            mDateNull = DateHelper.convertToDate(p.getFirstDate());

            setDateRangeBySettings(plot, p);

            plot.clearRangeMarkers();
            plotAlarmIndicators(p);

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);
            setRange(1.05);

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public void setTitle(BTopoGrade p) {
        var color = TopoHelper.getAlarmColorAwt(p);
        if (color == Color.RED || color == Color.GREEN) {
            color = color.darker();
        }

        var value = p.getAxis() == BAxis.HORIZONTAL ? p.getDistancePlane()
                : p.getAxis() == BAxis.VERTICAL ? p.getDistanceHeight()
                : p.getDistance3d();
        var label = p.getAxis() == BAxis.HORIZONTAL ? "P"
                : p.getAxis() == BAxis.VERTICAL ? "H"
                : "3d";
        var title = "%s :: Δ%s=%.1fm".formatted(p.getName(), label, value);
        setTitle(title, color);

        var dateFirst = Objects.toString(DateHelper.toDateString(p.getFirstDate()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.getLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        getLeftSubTextTitle().setText(date);

        var alarmText = "?";
        if (p.getAxis() == BAxis.HORIZONTAL) {
            if (!StringUtils.isBlank(p.getP1().getAlarm1Id())) {
                var ratio = p.getP1().ext().getAlarm(BComponent.HEIGHT).getRatio2s();
                ratio = StringUtils.defaultIfBlank(ratio, "?");
                alarmText = "%s, %+.1f".formatted(ratio, p.ext().getDiff().getZPerMille());
            }
        } else if (p.getAxis() == BAxis.VERTICAL) {
            if (!StringUtils.isBlank(p.getP1().getAlarm2Id())) {
                var ratio = p.getP1().ext().getAlarm(BComponent.PLANE).getRatio2s();
                ratio = StringUtils.defaultIfBlank(ratio, "?");
                alarmText = "%s, %+.1f".formatted(ratio, p.ext().getDiff().getRPerMille() * 1000);
            }
        }

        getRightSubTextTitle().setText(alarmText);
    }

    @Override
    public void updateDataset(BTopoGrade p) {
        TimeSeries timeSeries1Tmp = null;
        TimeSeries timeSeries2Tmp = null;
        TimeSeries timeSeries3Tmp = null;

        switch (p.getAxis()) {
            case HORIZONTAL:
                timeSeries1Tmp = new TimeSeries("Differentialsättning");
                break;
            case RESULTANT:
                timeSeries1Tmp = new TimeSeries("Höjd");
                timeSeries2Tmp = new TimeSeries("Plan");
                timeSeries3Tmp = new TimeSeries("Avstånd");

                break;
            case VERTICAL:
                timeSeries2Tmp = new TimeSeries("Vertikallutning");

                break;
            default:
                throw new AssertionError();
        }

        var timeSeries1 = timeSeries1Tmp;
        var timeSeries2 = timeSeries2Tmp;
        var timeSeries3 = timeSeries3Tmp;

        var plot = getPlot();
        resetPlot(plot);
        var label = "mm/m";
        if (p.getAxis() == BAxis.RESULTANT) {
            label = "mm";
        }
        plot.getRangeAxis().setLabel(label);
        plotOverlays(plot, p, p.getCommonObservations().firstKey());
        p.getCommonObservations().entrySet().forEach(entry -> {
            var date = entry.getKey();
            var p2 = entry.getValue();

            var minute = ChartHelper.convertToMinute(date.atStartOfDay());
            var gradeDiff = p.ext().getDiff(p.getFirstObservation(), p2);

            if (p.getAxis() == BAxis.HORIZONTAL) {
                timeSeries1.add(minute, gradeDiff.getZPerMille());
                mMinMaxCollection.add(gradeDiff.getZPerMille());
            }

            if (p.getAxis() == BAxis.VERTICAL) {
                timeSeries2.add(minute, gradeDiff.getRPerMille());
                mMinMaxCollection.add(gradeDiff.getRPerMille());
            }

            if (p.getAxis() == BAxis.RESULTANT) {
                BDimension distanceMode = p.getValue("distanceMode");
                double value;
                if (distanceMode == BDimension._1d) {
                    value = gradeDiff.getPartialDiffZ() * 1000;
                } else {
                    value = gradeDiff.getPartialDiffDistance();
                }
                timeSeries3.add(minute, value);
                mMinMaxCollection.add(value);

                var dz = gradeDiff.getPartialDiffZ() * 1000;
                var dr = gradeDiff.getPartialDiffR() * 1000;

                timeSeries1.add(minute, dz);
                timeSeries2.add(minute, dr);
                mMinMaxCollection.add(dz);
                mMinMaxCollection.add(dr);
            }
        });

        var renderer = plot.getRenderer();

        if (isValidTimeSeries(timeSeries1)) {
            getDataset().addSeries(timeSeries1);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries1.getKey()), Color.RED);
        }

        if (isValidTimeSeries(timeSeries2)) {
            getDataset().addSeries(timeSeries2);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries2.getKey()), Color.GREEN.darker());
        }

        if (isValidTimeSeries(timeSeries3)) {
            getDataset().addSeries(timeSeries3);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries3.getKey()), Color.BLUE);
        }
    }

    private boolean isValidTimeSeries(TimeSeries timeSeries) {
        return timeSeries != null && !timeSeries.isEmpty();
    }

    private void plotAlarmIndicators(BTopoGrade p) {
        try {
            var alarm = p.ext().getAlarmP1(p.getAxis() == BAxis.HORIZONTAL ? BComponent.HEIGHT : BComponent.PLANE);
            var l1 = 1000 * p.ext().getAlarmLevelForRangeByIndex(alarm, 0);
            var l2 = 1000 * p.ext().getAlarmLevelForRangeByIndex(alarm, 1);
            for (var level : List.of(-l1, l1)) {
                plotAlarmIndicator(BComponent.HEIGHT, level, Color.YELLOW);
                mMinMaxCollection.add(level);
            }
            for (var level : List.of(-l2, l2)) {
                plotAlarmIndicator(BComponent.HEIGHT, level, Color.RED);
                mMinMaxCollection.add(level);
            }
        } catch (Exception e) {
        }
    }
}

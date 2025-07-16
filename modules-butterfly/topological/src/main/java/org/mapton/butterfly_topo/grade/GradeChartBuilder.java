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
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.mapton.api.MTemporalManager;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import static org.mapton.butterfly_core.api.XyzChartBuilder.plotBlasts;
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.TopoHelper;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class GradeChartBuilder extends XyzChartBuilder<BTopoGrade> {

    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private final TimeSeries mTimeSeriesH = new TimeSeries(Dict.Geometry.HORIZONTAL.toString());
    private final TimeSeries mTimeSeriesV = new TimeSeries(Dict.Geometry.VERTICAL.toString());

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
            var plot = (XYPlot) mChart.getPlot();
            var dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setAutoRange(true);

            mDateNull = DateHelper.convertToDate(p.getFirstDate());

            setDateRangeNullNow(plot, p, mDateNull);

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
        setTitle(p, color);

        var dateFirst = Objects.toString(DateHelper.toDateString(p.getFirstDate()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.getLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        getLeftSubTextTitle().setText(date);

        var alarmText = "?";
        if (!StringUtils.isBlank(p.getP1().getAlarm1Id())) {
            var ratio = p.getP1().ext().getAlarm(BComponent.HEIGHT).getRatio2s();
            ratio = StringUtils.defaultIfBlank(ratio, "?");
            alarmText = "%s, %+.1f".formatted(ratio, p.ext().getDiff().getZPerMille());
        }

        getRightSubTextTitle().setText(alarmText);
    }

    @Override
    public void updateDataset(BTopoGrade p) {
        mTimeSeriesH.clear();
        mTimeSeriesV.clear();

        var plot = (XYPlot) mChart.getPlot();
        resetPlot(plot);

        plotBlasts(plot, p, p.ext().getObservationFilteredFirstDate(), p.ext().getObservationFilteredLastDate());
        p.getCommonObservations().entrySet().forEach(entry -> {
            var date = entry.getKey();
            var p1 = entry.getValue();
            var p2 = entry.getValue();
            //TODO Handle replacement & zero measurements

            var minute = ChartHelper.convertToMinute(date.atStartOfDay());
            var gradeDiff = p.ext().getDiff(p.getFirstObservation(), p2);

            if (p.getAxis() == BAxis.HORIZONTAL) {
                mTimeSeriesH.add(minute, gradeDiff.getZPerMille());
                mMinMaxCollection.add(gradeDiff.getZPerMille());
            }

            if (p.getAxis() == BAxis.VERTICAL) {
                mTimeSeriesV.add(minute, gradeDiff.getRPerMille());
                mMinMaxCollection.add(gradeDiff.getRPerMille());
            }
        });

        var renderer = plot.getRenderer();

        if (!mTimeSeriesH.isEmpty()) {
            getDataset().addSeries(mTimeSeriesH);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeriesH.getKey()), Color.RED);
        }

        if (!mTimeSeriesV.isEmpty()) {
            getDataset().addSeries(mTimeSeriesV);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(mTimeSeriesV.getKey()), Color.BLUE);
        }

    }

    private void plotAlarmIndicators(BTopoGrade p) {
        try {
            var alarm = p.ext().getAlarmP1(BComponent.HEIGHT);
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

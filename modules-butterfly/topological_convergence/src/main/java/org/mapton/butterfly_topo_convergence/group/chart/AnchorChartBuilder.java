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

import java.awt.Color;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.TimeSeries;
import org.mapton.api.MTemporalManager;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class AnchorChartBuilder extends XyzChartBuilder<BTopoConvergencePair> {

    private TextTitle mDateSubTextTitle;
    private TextTitle mDeltaSubTextTitle;
    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private final TimeSeries mTimeSeriesDeltaZ = new TimeSeries("ΔZ");
    private final TimeSeries mTimeSeriesAnchor = new TimeSeries("Ankare");
    private final TimeSeries mTimeSeriesPoint = new TimeSeries("Punkt");

    public AnchorChartBuilder() {
        initChart("m", "0.000");
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
            //dateAxis.setRange(DateHelper.convertToDate(mTemporalManager.getLowDate()), DateHelper.convertToDate(mTemporalManager.getHighDate()));
            dateAxis.setAutoRange(true);
            plot.clearRangeMarkers();
//            plotAlarmIndicators(p);

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);
//            rangeAxis.setRange(-0.050, +0.050);

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public void setTitle(BTopoConvergencePair p) {
        super.setTitle(p);
//        Color color = TopoHelper.getAlarmColorAwt(p);
        Color color = Color.BLUE;
        if (color == Color.RED || color == Color.GREEN) {
            color = color.darker();
        }
        mChart.getTitle().setPaint(color);
        var dateFirst = Objects.toString(DateHelper.toDateString(p.getDateZero()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        getLeftSubTextTitle().setText(date);
    }

    @Override
    public void updateDataset(BTopoConvergencePair p) {
        mTimeSeriesAnchor.clear();
        mTimeSeriesPoint.clear();
        mTimeSeriesDeltaZ.clear();

        var plot = (XYPlot) mChart.getPlot();
        resetPlot(plot);

        if (p.getObservations().isEmpty()) {
            return;
        }

        for (var o : p.getObservations()) {
            var minute = ChartHelper.convertToMinute(o.getDate());
            mTimeSeriesDeltaZ.add(minute, o.getDeltaDeltaZComparedToFirst());
        }

        getDataset().addSeries(mTimeSeriesDeltaZ);
        plotZ(p, p.getP1());
        plotZ(p, p.getP2());

        plotBlasts(plot, p, p.ext().getFirstDate().toLocalDate(), p.ext().getLastDate().toLocalDate());
    }

    private void plotZ(BTopoConvergencePair pair, BTopoControlPoint p) {
        var plot = (XYPlot) mChart.getPlot();
        var firstDate = pair.getObservations().getFirst().getDate().toLocalDate();
        var lastDate = pair.getObservations().getLast().getDate().toLocalDate();
        var series = pair.getConvergenceGroup().ext2().getAnchorPoint() == p ? mTimeSeriesAnchor : mTimeSeriesPoint;
        p.ext().getObservationsAllRaw().stream()
                .filter(o -> DateHelper.isBetween(
                firstDate,
                lastDate,
                o.getDate().toLocalDate()))
                .forEachOrdered(o -> {
                    var minute = ChartHelper.convertToMinute(o.getDate());
                    series.add(minute, o.ext().getDeltaZ());
                });
        getDataset().addSeries(series);
    }

}

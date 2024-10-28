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
package org.mapton.butterfly_acoustic.measuring_point;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.LengthAdjustmentType;
import org.jfree.data.time.TimeSeries;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.acoustic.BAcousticMeasuringPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.CircularInt;

/**
 *
 * @author Patrik Karlström
 */
public class MeasPointChartBuilder extends XyzChartBuilder<BAcousticMeasuringPoint> {

    private final ChartHelper mChartHelper = new ChartHelper();
    private final CircularInt mColorCircularInt = new CircularInt(0, 5);
    private final TimeSeries mTimeSeriesZ = new TimeSeries("Δ µε");

    public MeasPointChartBuilder() {
        initChart("mm/s", "0.00");
    }

    @Override
    public synchronized Callable<ChartPanel> build(BAcousticMeasuringPoint p) {
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
            plotAlarmIndicators(p);

            var rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);
//            rangeAxis.setRange(-0.050, +0.050);

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public void setTitle(BAcousticMeasuringPoint p) {
        super.setTitle(p);
//        setTitle(p, StrainHelper.getAlarmColorAwt(p));

//        var dateFirst = Objects.toString(DateHelper.toDateString(p.getDateZero()), "");
//        var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
//        var date = "(%s) → %s".formatted(dateFirst, dateLast);
//        getLeftSubTextTitle().setText(date);
//
//        var rightTitle = "%s: %s".formatted(p.getAlarm1Id(), p.ext().getDeltaZero());
//        getRightSubTextTitle().setText(rightTitle);
    }

    @Override
    public synchronized void updateDataset(BAcousticMeasuringPoint p) {
        getDataset().removeAllSeries();
        mTimeSeriesZ.clear();
        mColorCircularInt.set(0);

        var plot = (XYPlot) mChart.getPlot();
        plot.clearDomainMarkers();

        updateDataset2(p);
    }

    private Color getColor() {
        var colors = new Color[]{
            Color.BLUE,
            Color.CYAN,
            Color.MAGENTA,
            Color.YELLOW,
            Color.GREEN,
            Color.ORANGE};

        return colors[mColorCircularInt.inc()];
    }

    private void plotAlarmIndicator(BComponent component, double value, Color color) {
        var marker = new ValueMarker(value);
        float width = 1.0f;
        float dash[] = {5.0f, 5.0f};
        var dashedStroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.5f, dash, 0);
        var stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.5f, null, 0);
        if (component == BComponent.HEIGHT) {
            marker.setStroke(dashedStroke);
        } else {
            marker.setStroke(stroke);
        }
        marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        marker.setPaint(color);

        var plot = (XYPlot) mChart.getPlot();
        plot.addRangeMarker(marker);
    }

    private void plotAlarmIndicators(BAcousticMeasuringPoint p) {
        BAlarm ha = null;
//        var ha = p.ext().getAlarm(BComponent.HEIGHT);
        if (ha != null) {
            var range0 = ha.ext().getRange0();
            if (range0 != null) {
                plotAlarmIndicator(BComponent.HEIGHT, range0.getMinimum(), Color.YELLOW);
                plotAlarmIndicator(BComponent.HEIGHT, range0.getMaximum(), Color.YELLOW);
            }

            var range1 = ha.ext().getRange1();
            if (range1 != null) {
                plotAlarmIndicator(BComponent.HEIGHT, range1.getMinimum(), Color.RED);
                plotAlarmIndicator(BComponent.HEIGHT, range1.getMaximum(), Color.RED);
            }
        }
    }

    private void updateDataset2(BAcousticMeasuringPoint p) {
        var plot = (XYPlot) mChart.getPlot();
        var renderer = plot.getRenderer();
        p.ext().getChannels().forEach(c -> {
            var timeSeries = new TimeSeries(c.getType());
            for (var o : c.ext().getObservations()) {
                var minute = mChartHelper.convertToMinute(o.getDate());
                if (o.getMeasuredZ() != null) {
                    timeSeries.add(minute, o.getMeasuredZ());
                }
            }
            getDataset().addSeries(timeSeries);
            renderer.setSeriesPaint(getDataset().getSeriesIndex(timeSeries.getKey()), getColor());
        });

    }
}

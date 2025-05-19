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

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.numbers.core.Precision;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.LengthAdjustmentType;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.mapton.butterfly_alarm.api.AlarmHelper;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.TopoHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class ChartBuilderBase extends XyzChartBuilder<BTopoControlPoint> {

    protected Minute mSubSetLastMinute;
    protected Minute mSubSetZeroMinute;
    protected final TimeSeries mTimeSeries1d = new TimeSeries(Dict.Geometry.HEIGHT);
    protected final TimeSeries mTimeSeries2d = new TimeSeries(Dict.Geometry.PLANE);
    protected final TimeSeries mTimeSeries3d = new TimeSeries("3d");

    public ChartBuilderBase() {
        initChart(null, null);
    }

    @Override
    public synchronized Callable<ChartPanel> build(BTopoControlPoint p) {
        if (p == null) {
            return null;
        }
        var callable = (Callable<ChartPanel>) () -> {
            setTitle(p);
            var plot = (XYPlot) mChart.getPlot();
            updateDataset(p);

            setDateRangeNullNow(plot, p, mDateNull);
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
    public void setTitle(BTopoControlPoint p) {
        setTitle(p, TopoHelper.getAlarmColorAwt(p));

        var dateFirst = Objects.toString(DateHelper.toDateString(p.getDateZero()), "");
        var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
        var date = "(%s) → %s".formatted(dateFirst, dateLast);
        getLeftSubTextTitle().setText(date);

        var sb = new StringBuilder();
        if (!StringUtils.isBlank(p.getAlarm1Id())) {
            sb.append("H ").append(p.getAlarm1Id());
            if (!StringUtils.isBlank(p.getAlarm2Id())) {
                sb.append(", ");
            }
        }

        if (!StringUtils.isBlank(p.getAlarm2Id())) {
            sb.append("P ").append(p.getAlarm2Id());
        }

        var alarmNames = sb.toString();

        String hAlarm = "";
        if (p.getDimension() != BDimension._2d) {
            hAlarm = "H " + AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p);
            if (p.getDimension() == BDimension._3d) {
                hAlarm = hAlarm + ", ";
            }
        }

        String pAlarm = "";
        if (p.getDimension() != BDimension._1d) {
            pAlarm = "P " + AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p);
        }

        String delta = p.ext().deltaZero().getDelta(3);

        var rightTitle = "%s%s: %s".formatted(hAlarm, pAlarm, delta);
        getRightSubTextTitle().setText(rightTitle);
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

    private void plotAlarmIndicators(BTopoControlPoint p) {
        var ha = p.ext().getAlarm(BComponent.HEIGHT);
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

        var pa = p.ext().getAlarm(BComponent.PLANE);
        if (pa != null) {
            var range0 = pa.ext().getRange0();
            if (range0 != null) {
                if (!Precision.equals(range0.getMinimum(), 0.0)) {
                    plotAlarmIndicator(BComponent.PLANE, range0.getMinimum(), Color.YELLOW);
                }
                plotAlarmIndicator(BComponent.PLANE, range0.getMaximum(), Color.YELLOW);
            }

            var range1 = pa.ext().getRange1();
            if (range1 != null) {
                if (!Precision.equals(range1.getMinimum(), 0.0)) {
                    plotAlarmIndicator(BComponent.PLANE, range1.getMinimum(), Color.RED);
                }
                plotAlarmIndicator(BComponent.PLANE, range1.getMaximum(), Color.RED);
            }
        }
    }
}

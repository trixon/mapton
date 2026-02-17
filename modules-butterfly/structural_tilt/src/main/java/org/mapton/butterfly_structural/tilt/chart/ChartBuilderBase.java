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
package org.mapton.butterfly_structural.tilt.chart;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.mapton.butterfly_core.api.XyzChartBuilder;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
import org.mapton.butterfly_structural.tilt.TiltHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class ChartBuilderBase extends XyzChartBuilder<BStructuralTiltPoint> {

    protected Minute mSubSetLastMinute;
    protected Minute mSubSetZeroMinute;
    protected final TimeSeries mTimeSeriesX = new TimeSeries("Transversal");
    protected final TimeSeries mTimeSeriesY = new TimeSeries("Longitudinell");
    protected final TimeSeries mTimeSeriesZ = new TimeSeries("Resultant");

    public ChartBuilderBase() {
        initChart("mm/m", "0.0");
    }

    @Override
    public synchronized Callable<ChartPanel> build(BStructuralTiltPoint p) {
        if (p == null) {
            return null;
        }
        var callable = (Callable<ChartPanel>) () -> {
            var frequency = p.getFrequency();
            if (!isCompleteView() && frequency != null) {
                if (frequency > 2) {
                    setRecentDays(getRecentDaysDefault() * 4);
                } else {
                    setRecentDays(getRecentDaysDefault());
                }
            }

            setTitle(p);
            var plot = (XYPlot) mChart.getPlot();
            updateDataset(p);
            var date = isCompleteView() ? mDateNull : Date.from(Instant.now().minus(getRecentDays(), ChronoUnit.DAYS));
            setDateRangeNullNow(plot, p, date);
            plot.clearRangeMarkers();
            plotAlarmIndicators(p);

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public void setTitle(BStructuralTiltPoint p) {
        setTitle(p, TiltHelper.getAlarmColorHeightAwt(p));

        if (isCompleteView()) {
            var dateFirst = Objects.toString(DateHelper.toDateString(p.ext().getObservationFilteredFirstDate()), "");
            var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
            var date = "(%s) → %s".formatted(dateFirst, dateLast);
            getLeftSubTextTitle().setText(date);
        }

        var rightTitle = "%s".formatted(p.ext().getDeltaZero());
        getRightSubTextTitle().setText(rightTitle);
    }
}

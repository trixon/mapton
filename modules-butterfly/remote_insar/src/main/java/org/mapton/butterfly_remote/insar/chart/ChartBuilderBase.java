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
package org.mapton.butterfly_remote.insar.chart;

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
import org.mapton.butterfly_format.types.remote.BRemoteInsarPoint;
import org.mapton.butterfly_remote.insar.InsarHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class ChartBuilderBase extends XyzChartBuilder<BRemoteInsarPoint> {

    protected Minute mSubSetLastMinute;
    protected Minute mSubSetZeroMinute;
    protected final TimeSeries mTimeSeries1d = new TimeSeries(Dict.Geometry.HEIGHT.toString());

    public ChartBuilderBase() {
        initChart("mm", "0.0");
    }

    @Override
    public synchronized Callable<ChartPanel> build(BRemoteInsarPoint p) {
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
            plotAlarmIndicators(p, InsarHelper.getScaleFactor(p));

            return getChartPanel();
        };

        return callable;
    }

    @Override
    public void setTitle(BRemoteInsarPoint p) {
        setTitle(p, InsarHelper.getAlarmColorAwt(p));

        if (isCompleteView()) {
            var dateFirst = Objects.toString(DateHelper.toDateString(p.ext().getObservationFilteredFirstDate()), "");
            var dateLast = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
            var date = "(%s) → %s".formatted(dateFirst, dateLast);
            getLeftSubTextTitle().setText(date);
        }

        String delta = p.ext().deltaZero().getDelta1(3, InsarHelper.getScaleFactor(p), false);
        getRightSubTextTitle().setText("Δ " + delta);
    }
}

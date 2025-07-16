/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import java.time.LocalDateTime;
import java.util.function.Function;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.statistics.Regression;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TrendHelper {

    public static Trend createTrend(BXyzPoint p, LocalDateTime startDate, LocalDateTime endDate, Function<BXyzPointObservation, Double> function, int percentile) throws IllegalArgumentException {
        if (percentile < -100 || percentile > 100) {
            throw new IllegalArgumentException("percentile must be between -100 and +100.");
        }

        if (p.ext() instanceof BXyzPoint.Ext<? extends BXyzPointObservation> ext) {
            var timeFilteredValues = ext.getObservationsTimeFiltered().stream()
                    .filter(o -> DateHelper.isBetween(startDate.toLocalDate(), endDate.toLocalDate().plusDays(1), o.getDate().toLocalDate()))
                    .toList();

            var values = timeFilteredValues.stream()
                    .mapToDouble(o -> function.apply(o))
                    .sorted()
                    .boxed()
                    .toList();

            int index = (int) Math.ceil(Math.abs(percentile) / 100.0 * values.size()) - 1;
            var limit = values.get(index);
            var timeSeries = new TimeSeries("-");

            timeFilteredValues.stream()
                    .filter(o -> {
                        if (percentile < 0) {
                            return function.apply(o) < limit;
                        } else {
                            return function.apply(o) > limit;
                        }
                    })
                    .forEachOrdered(o -> {
                        timeSeries.addOrUpdate(ChartHelper.convertToMinute(o.getDate()), function.apply(o));
                    });

            var dataset = new TimeSeriesCollection();
            dataset.addSeries(timeSeries);
            var coefficients = Regression.getOLSRegression(dataset, 0);

            return new Trend(
                    new LineFunction2D(coefficients[0], coefficients[1]),
                    ChartHelper.convertToMinute(startDate),
                    ChartHelper.convertToMinute(endDate),
                    timeSeries.getItemCount()
            );
        }

        return null;
    }

    public static Trend createTrend(BXyzPoint p, LocalDateTime startDate, LocalDateTime endDate, Function<BXyzPointObservation, Double> function) throws IllegalArgumentException {
        var timeSeries = new TimeSeries("-");

        if (p.ext() instanceof BXyzPoint.Ext<? extends BXyzPointObservation> ext) {
            ext.getObservationsTimeFiltered().stream()
                    .filter(o -> DateHelper.isBetween(startDate.toLocalDate(), endDate.toLocalDate().plusDays(1), o.getDate().toLocalDate()))
                    .forEachOrdered(o -> {
                        timeSeries.addOrUpdate(ChartHelper.convertToMinute(o.getDate()), function.apply(o));
                    });
        }

        var dataset = new TimeSeriesCollection();
        dataset.addSeries(timeSeries);
        var coefficients = Regression.getOLSRegression(dataset, 0);

        return new Trend(
                new LineFunction2D(coefficients[0], coefficients[1]),
                ChartHelper.convertToMinute(startDate),
                ChartHelper.convertToMinute(endDate),
                timeSeries.getItemCount()
        );
    }

    public static Double getMmPerYear(Trend trend) {
        var now = LocalDateTime.now();
        var startMinute = new Minute(0, new Hour());
        if (trend != null && !trend.startMinute().getDay().equals(startMinute.getDay())) {
            var val1 = trend.function().getValue(ChartHelper.convertToMinute(now.plusYears(1)).getFirstMillisecond());
            var val2 = trend.function().getValue(ChartHelper.convertToMinute(now).getFirstMillisecond());
            return (val1 - val2) * 1000;
        }

        return null;
    }

    private TrendHelper() {
    }

    public record Trend(LineFunction2D function, Minute startMinute, Minute endMinute, int numOfMeas) {

    }
}

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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.statistics.Regression;
import org.jfree.data.time.Day;
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

    public static Trend createTrend(BXyzPoint p, boolean dailyMean, LocalDateTime startDate, LocalDateTime endDate, Function<BXyzPointObservation, Double> function) throws IllegalArgumentException {
        var startLocalDate = startDate.toLocalDate();
        var endLocalDatePlusOne = endDate.toLocalDate().plusDays(1);
        var filteredObservations = new ArrayList<BXyzPointObservation>();

        if (p.ext() instanceof BXyzPoint.Ext<? extends BXyzPointObservation> ext) {
            for (var o : ext.getObservationsTimeFiltered()) {
                if (DateHelper.isBetween(startLocalDate, endLocalDatePlusOne, o.getDate().toLocalDate())) {
                    filteredObservations.add(o);
                }
            }
        }

        if (filteredObservations.isEmpty()) {
            throw new IllegalArgumentException("Not enough data");
        }

        var dataset = new TimeSeriesCollection();

        if (dailyMean) {
            var dailyValues = new HashMap<Day, ArrayList<Double>>();

            for (var o : filteredObservations) {
                var day = new Day(Date.from(o.getDate().atZone(ZoneId.systemDefault()).toInstant()));
                double val = function.apply(o);
                dailyValues.computeIfAbsent(day, k -> new ArrayList<>()).add(val);
            }

            var dailyMedians = new TimeSeries("-");
            var median = new Median();

            for (var entry : dailyValues.entrySet()) {
                var values = entry.getValue();
                var targetArray = values.stream().mapToDouble(Double::doubleValue).toArray();
                var medianValue = median.evaluate(targetArray);
                dailyMedians.add(entry.getKey(), medianValue);
            }
            dataset.addSeries(dailyMedians);
        } else {
            var timeSeries = new TimeSeries("-");
            for (var o : filteredObservations) {
                timeSeries.addOrUpdate(ChartHelper.convertToMinute(o.getDate()), function.apply(o));
            }
            dataset.addSeries(timeSeries);
        }

        var coefficients = Regression.getOLSRegression(dataset, 0);

        return new Trend(
                new LineFunction2D(coefficients[0], coefficients[1]),
                ChartHelper.convertToMinute(startDate),
                ChartHelper.convertToMinute(endDate),
                dataset.getSeries(0).getItemCount()
        );
    }

    public static Double getVelocity(Trend trend) {
        var now = LocalDateTime.now();
        var startMinute = new Minute(0, new Hour());
        if (trend != null && !trend.startMinute().getDay().equals(startMinute.getDay())) {
            var val1 = trend.function().getValue(ChartHelper.convertToMinute(now.plusYears(1)).getFirstMillisecond());
            var val2 = trend.function().getValue(ChartHelper.convertToMinute(now).getFirstMillisecond());
            return (val1 - val2) * 1000;
        } else {
            return null;
        }
    }

    public static Double getVelocityDiff(Trend trend1, Trend trend2) {
        var velocity1 = getVelocity(trend1);
        var velocity2 = getVelocity(trend2);
        if (ObjectUtils.allNotNull(velocity1, velocity2)) {
            return velocity1 - velocity2;
        } else {
            return null;
        }
    }

    private TrendHelper() {
    }

    public record Trend(LineFunction2D function, Minute startMinute, Minute endMinute, int numOfMeas) {

    }
}

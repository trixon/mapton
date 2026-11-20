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
package org.mapton.ce_jfreechart.api;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jfree.data.time.Day;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;

/**
 *
 * @author Patrik Karlström
 */
public class ChartHelper {

    private ChartHelper() {
    }

    public static Day convertToDay(LocalDate ld) {
        return new Day(
                ld.getDayOfMonth(),
                ld.getMonthValue(),
                ld.getYear()
        );
    }

    public static LocalDateTime convertToLocalDateTime(Minute minute) {
        return Instant.ofEpochMilli(minute.getFirstMillisecond())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static Minute convertToMinute(LocalDateTime ldt) {
        return new Minute(
                ldt.getMinute(),
                ldt.getHour(),
                ldt.getDayOfMonth(),
                ldt.getMonthValue(),
                ldt.getYear()
        );
    }

    public static TimeSeries dailyMedian(TimeSeries source) {
        Map<LocalDate, List<Double>> byDay = new TreeMap<>();

        for (int i = 0; i < source.getItemCount(); i++) {
            var item = source.getDataItem(i);
            var day = Instant.ofEpochMilli(
                    item.getPeriod().getStart().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            byDay.computeIfAbsent(day, d -> new ArrayList<>()).add(item.getValue().doubleValue());
        }

        var result = new TimeSeries(source.getKey() + " (daily median)");

        byDay.forEach((day, values) -> {
            values.sort(Double::compare);

            double median;
            int n = values.size();

            if ((n & 1) == 0) {
                median = (values.get(n / 2 - 1) + values.get(n / 2)) / 2.0;
            } else {
                median = values.get(n / 2);
            }

            result.add(new Day(
                    day.getDayOfMonth(),
                    day.getMonthValue(),
                    day.getYear()),
                    median);
        });

        return result;
    }
}

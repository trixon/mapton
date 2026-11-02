/*
 * Copyright 2026 Patrik Karlström.
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

/**
 *
 * @author Patrik Karlström
 */
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Geodetisk analysmotor för att detektera årliga säsongsvariationer i
 * mätserier.
 */
public class Cyclic {

    public static double calculateYearlyCyclicAmplitude(List<EwmaObservation> obsList) {
        if (obsList == null || obsList.size() < 6) {
            return Double.NaN;
        }

        double trendPerDay = calculateLinearTrendPerDay(obsList);
        if (Double.isNaN(trendPerDay)) {
            return Double.NaN;
        }

        long epochDay = obsList.getFirst().getLocalDateTime().toLocalDate().toEpochDay();

        double[] monthSums = new double[12];
        int[] monthCounts = new int[12];

        for (int i = 0; i < obsList.size(); i++) {
            var o = obsList.get(i);
            double z = o.deltaZ();

            if (Double.isNaN(z)) {
                continue;
            }

            int monthIndex = o.getLocalDateTime().getMonthValue() - 1;
            long daysSinceStart = o.getLocalDateTime().toLocalDate().toEpochDay() - epochDay;

            double detrendedZ = z - (trendPerDay * daysSinceStart);

            monthSums[monthIndex] += detrendedZ;
            monthCounts[monthIndex]++;
        }

        double maxMonthAvg = -Double.MAX_VALUE;
        double minMonthAvg = Double.MAX_VALUE;
        int validMonthsCount = 0;

        for (int m = 0; m < 12; m++) {
            if (monthCounts[m] > 0) {
                double avg = monthSums[m] / monthCounts[m];
                validMonthsCount++;

                if (avg > maxMonthAvg) {
                    maxMonthAvg = avg;
                }
                if (avg < minMonthAvg) {
                    minMonthAvg = avg;
                }
            }
        }

        if (validMonthsCount < 3 || maxMonthAvg == -Double.MAX_VALUE || minMonthAvg == Double.MAX_VALUE) {
            return Double.NaN;
        }

        return maxMonthAvg - minMonthAvg;
    }

    public static double calculateLinearTrendPerDay(List<EwmaObservation> obsList) {
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int count = 0;

        long epochDay = obsList.getFirst().getLocalDateTime().toLocalDate().toEpochDay();

        for (int i = 0; i < obsList.size(); i++) {
            var o = obsList.get(i);
            double y = o.deltaZ();

            if (Double.isNaN(y)) {
                continue;
            }

            double x = o.getLocalDateTime().toLocalDate().toEpochDay() - epochDay;

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
            count++;
        }

        if (count < 4) {
            return Double.NaN;
        }

        double denominator = (count * sumX2 - sumX * sumX);
        return denominator == 0 ? Double.NaN : (count * sumXY - sumX * sumY) / denominator;
    }

    public static record EwmaObservation(long epochSecond, double deltaZ) {

        public LocalDateTime getLocalDateTime() {
            return LocalDateTime.ofEpochSecond(epochSecond, 0, ZoneOffset.UTC);
        }
    }

}

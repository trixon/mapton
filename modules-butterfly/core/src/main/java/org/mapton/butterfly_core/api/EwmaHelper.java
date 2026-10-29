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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.jfree.data.time.TimeSeries;
import org.mapton.api.MAvgPeriod;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;

/**
 *
 * @author Patrik Karlström
 */
public class EwmaHelper {

    public static BPrimitiveSeries calculateEwma(BPrimitiveSeries series, int days) {
        var ewmaSeries = new BPrimitiveSeries(series.size());
        if (series.size() == 0) {
            return ewmaSeries;
        }

        double lambda = 2.0 / (days + 1.0);
        double currentEwma = Double.NaN;

        for (int i = 0; i < series.size(); i++) {
            double raw = series.getValues()[i];
            long seconds = series.getEpochSeconds()[i];
            LocalDateTime dt = LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC);

            if (Double.isNaN(raw)) {
                ewmaSeries.add(dt, currentEwma);
                continue;
            }

            if (Double.isNaN(currentEwma)) {
                currentEwma = raw;
            } else {
                currentEwma = (lambda * raw) + ((1.0 - lambda) * currentEwma);
            }

            ewmaSeries.add(dt, currentEwma);
        }

        return ewmaSeries;
    }

    public static Ewma createEwma(BXyzPoint p, String key, MAvgPeriod period) {
        BPrimitiveSeries rawSeries = p.getValue(key + "raw");

        if (rawSeries == null || rawSeries.isEmpty()) {
            return new Ewma(rawSeries, Double.NaN);
        }

        var ewmaSeries = calculateEwma(rawSeries, period.getDays());

        double[] ewmaValues = ewmaSeries.getValues();
        double lastValue = ewmaValues[ewmaValues.length - 1];

        return new Ewma(ewmaSeries, lastValue);
    }

    public static class Ewma {

        private TimeSeries mCachedTimeSeries = null;

        private final double mLastValue;
        private final BPrimitiveSeries mPrimitiveSeries;

        public Ewma(BPrimitiveSeries ewmaSeries, double lastValue) {
            mPrimitiveSeries = ewmaSeries;
            mLastValue = lastValue;
        }

        public double getLastValue() {
            return mLastValue;
        }

        public BPrimitiveSeries getPrimitiveSeries() {
            return mPrimitiveSeries;
        }

        public synchronized TimeSeries getTimeSeries() {
            if (mCachedTimeSeries == null && mPrimitiveSeries != null && !mPrimitiveSeries.isEmpty()) {
                mCachedTimeSeries = new TimeSeries("EWMA");

                long[] seconds = mPrimitiveSeries.getEpochSeconds();
                double[] values = mPrimitiveSeries.getValues();

                for (int i = 0; i < mPrimitiveSeries.size(); i++) {
                    if (!Double.isNaN(values[i])) {
                        var date = LocalDateTime.ofEpochSecond(seconds[i], 0, ZoneOffset.UTC);
                        mCachedTimeSeries.addOrUpdate(ChartHelper.convertToMinute(date), values[i]);
                    }
                }
            }

            return mCachedTimeSeries;
        }
    }
}

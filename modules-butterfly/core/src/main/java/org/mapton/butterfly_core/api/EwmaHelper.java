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

import org.jfree.data.time.TimeSeries;
import org.mapton.api.MAvgPeriod;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public class EwmaHelper {

    public static Ewma createEwma(BXyzPoint p, String key, MAvgPeriod period) {
        TimeSeries rawSeries = p.getValue(key + "raw");
        var timeSeries = XyzChartBuilder.createEWMA(null, p.getDateZero(), rawSeries, period);
        var ewma = new Ewma(timeSeries);

        return ewma;
    }

    public static class Ewma {

        private double mLastValue;
        private final TimeSeries mTimeSeries;

        public Ewma(TimeSeries timeSeries) {
            mTimeSeries = timeSeries;

            if (!mTimeSeries.isEmpty()) {
                var lastItem = mTimeSeries.getDataItem(mTimeSeries.getItemCount() - 1);
                mLastValue = lastItem.getValue().doubleValue();
            }
        }

        public double getLastValue() {
            return mLastValue;
        }

        public TimeSeries getTimeSeries() {
            return mTimeSeries;
        }

    }
}

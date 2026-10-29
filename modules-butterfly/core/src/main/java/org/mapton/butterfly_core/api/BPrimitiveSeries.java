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
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BPrimitiveSeries {

    private int mCount = 0;
    private long[] mEpochSeconds;
    private double[] mValues;

    public static BPrimitiveSeries createPrimitiveDifference(BPrimitiveSeries series1, BPrimitiveSeries series2) {
        int count = Math.min(series1.size(), series2.size());
        var result = new BPrimitiveSeries(count);

        long[] seconds1 = series1.getEpochSeconds();
        double[] values1 = series1.getValues();
        double[] values2 = series2.getValues();

        for (int i = 0; i < count; i++) {
            double diff = values1[i] - values2[i];

            var date = LocalDateTime.ofEpochSecond(seconds1[i], 0, ZoneOffset.UTC);
            result.add(date, diff);
        }

        return result;
    }

    public static double getInterpolatedValue(BPrimitiveSeries series, long targetTime) {
        if (series == null || series.isEmpty()) {
            return Double.NaN;
        }

        int count = series.size();
        long[] epochTimes = series.getEpochSeconds();
        double[] values = series.getValues();

        long firstTime = epochTimes[0];
        if (targetTime <= firstTime) {
            return values[0];
        }

        long lastTime = epochTimes[count - 1];
        if (targetTime >= lastTime) {
            return values[count - 1];
        }

        for (int i = 1; i < count; i++) {
            long t1 = epochTimes[i - 1];
            long t2 = epochTimes[i];

            if (targetTime >= t1 && targetTime <= t2) {
                double y1 = values[i - 1];
                double y2 = values[i];

                if (t1 == t2) {
                    return y1;
                }

                double fraction = (double) (targetTime - t1) / (t2 - t1);

                return y1 + fraction * (y2 - y1);
            }
        }

        return Double.NaN;
    }

    public BPrimitiveSeries(int initialCapacity) {
        mEpochSeconds = new long[initialCapacity];
        mValues = new double[initialCapacity];
    }

    public void add(LocalDateTime dateTime, double value) {
        if (mCount >= mValues.length) {
            int newLength = mValues.length * 2;
            mEpochSeconds = Arrays.copyOf(mEpochSeconds, newLength);
            mValues = Arrays.copyOf(mValues, newLength);
        }
        mEpochSeconds[mCount] = dateTime.toEpochSecond(ZoneOffset.UTC);
        mValues[mCount] = Double.isFinite(value) ? value : Double.NaN;
        mCount++;
    }

    public LocalDateTime getDateTime(int index) {
        return LocalDateTime.ofEpochSecond(mEpochSeconds[index], 0, ZoneOffset.UTC);
    }

    public Stream<LocalDateTime> getDateTimeStream() {
        return IntStream.range(0, mCount)
                .mapToObj(i -> LocalDateTime.ofEpochSecond(mEpochSeconds[i], 0, ZoneOffset.UTC));
    }

    public long[] getEpochSeconds() {
        return mEpochSeconds;
    }

    public double[] getValues() {
        return mValues;
    }

    public boolean isEmpty() {
        return mCount == 0;
    }

    public int size() {
        return mCount;
    }
}

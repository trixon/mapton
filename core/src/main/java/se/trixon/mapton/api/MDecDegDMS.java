/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.api;

import org.apache.commons.lang3.Range;

/**
 *
 * @author Patrik Karlström
 */
public class MDecDegDMS {

    private static final Range<Integer> LAT_RANGE = Range.between(-90, 90);
    private static final Range<Integer> LON_RANGE = Range.between(-180, 180);
    private static final Range<Integer> MIN_RANGE = Range.between(0, 59);
    private static final Range<Double> SEC_RANGE = Range.between(0.0, 59.999999);

    private boolean mAbsolute;
    private double mDecDeg;
    private int mLatDeg;
    private int mLatMin;
    private double mLatSec;
    private int mLonDeg;
    private int mLonMin;
    private double mLonSec;

    public MDecDegDMS(double decdeg, boolean absolute) {
        mDecDeg = decdeg;
        mAbsolute = absolute;
    }

    public MDecDegDMS(double decdeg) {
        this(decdeg, false);
    }

    public MDecDegDMS(int latDeg, int latMin, double latSec, int lonDeg, int lonMin, double lonSec) {
        mLatDeg = latDeg;
        mLatMin = latMin;
        mLatSec = latSec;
        mLonDeg = lonDeg;
        mLonMin = lonMin;
        mLonSec = lonSec;

        if (!(LAT_RANGE.contains(latDeg)
                && MIN_RANGE.contains(latMin)
                && SEC_RANGE.contains(latSec)
                && LON_RANGE.contains(lonDeg)
                && MIN_RANGE.contains(lonMin)
                && SEC_RANGE.contains(lonSec))) {
            throw new IllegalArgumentException();
        }
    }

    public String format(String format, String pos, String neg) {
        return String.format(format,
                getDeg(),
                getMin(),
                getSec(),
                mAbsolute ? mDecDeg < 0 ? neg : pos : pos
        );
    }

    public double getDecimalDegrees() {
        return mAbsolute ? Math.abs(mDecDeg) : mDecDeg;
    }

    public int getDeg() {
        return (int) getDecimalDegrees();
    }

    public double getLatitude() {
        double lat = Math.abs(mLatDeg) + mLatMin / 60f + mLatSec / 3600f;
        return mLatDeg < 0 ? -1 * lat : lat;
    }

    public double getLongitude() {
        double lon = Math.abs(mLonDeg) + mLonMin / 60f + mLonSec / 3600f;
        return mLonDeg < 0 ? -1 * lon : lon;
    }

    public int getMin() {
        return Math.abs((int) ((getDecimalDegrees() - getDeg()) * 60));
    }

    public double getSec() {
        return (Math.abs(getDecimalDegrees() - getDeg()) - getMin() / 60.0) * 3600;
    }
}

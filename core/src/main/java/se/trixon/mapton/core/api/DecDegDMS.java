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
package se.trixon.mapton.core.api;

/**
 *
 * @author Patrik Karlström
 */
public class DecDegDMS {

    private boolean mAbsolute;
    private double mDecDeg;

    public DecDegDMS(double decdeg, boolean absolute) {
        mDecDeg = decdeg;
        mAbsolute = absolute;
    }

    public DecDegDMS(double decdeg) {
        this(decdeg, false);
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

    public int getMin() {
        return Math.abs((int) ((getDecimalDegrees() - getDeg()) * 60));
    }

    public double getSec() {
        return (Math.abs(getDecimalDegrees() - getDeg()) - getMin() / 60.0) * 3600;
    }
}

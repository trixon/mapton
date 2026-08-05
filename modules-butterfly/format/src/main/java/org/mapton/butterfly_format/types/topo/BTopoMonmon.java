/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_format.types.topo;

import java.time.LocalDate;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoMonmon extends BTopoControlPoint {

    private Ext mExt;
    private final int[] mMeasCount = new int[365];
    private int mMeasPerDay;
    private String mStationName;
    private BTopoControlPoint mStationPoint;
    private BTopoControlPoint mControlPoint;

    public BTopoMonmon(int measPerDay, String stationName, BTopoControlPoint controlPoint) {
        mMeasPerDay = measPerDay;
        mStationName = stationName;
        mControlPoint = controlPoint;
    }

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public BTopoControlPoint getControlPoint() {
        return mControlPoint;
    }

    public int[] getMeasCount() {
        return mMeasCount;
    }

    public int getMeasPerDay() {
        return mMeasPerDay;
    }

    public double getQuota(int index) {
        int sum = mMeasCount[index];
        int max = getMeasPerDay() * index;

        return sum * 1.0 / max * 1.0;
    }

    public String getStationName() {
        return mStationName;
    }

    public BTopoControlPoint getStationPoint() {
        return mStationPoint;
    }

    public String getString(int index) {
        int sum = mMeasCount[index];
        int max = getMeasPerDay() * index;

        return "%.0f%% %d/%d".formatted(getQuota(index) * 100.0, sum, max);
    }

    public boolean isChild() {
        return !isParent();
    }

    public boolean isParent() {
        return StringUtils.isBlank(mStationName);
    }

    public void setStationName(String stationName) {
        mStationName = stationName;
    }

    public void setStationPoint(BTopoControlPoint stationPoint) {
        mStationPoint = stationPoint;
    }

    public class Ext extends BTopoControlPoint.Ext {

        @Override
        public LocalDate getObservationRawFirstDate() {
            try {
                var p = BTopoMonmon.this.getButterfly().topo().getControlPointByName(BTopoMonmon.this.getName());
                return p.ext().getObservationRawFirst().getDate().toLocalDate();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public LocalDate getObservationRawLastDate() {
            try {
                return getTcp().ext().getObservationRawLast().getDate().toLocalDate();
            } catch (Exception e) {
                return null;
            }
        }

        public double getDelta1d() {
            return Math.abs(getZeroZ() - mStationPoint.getZeroZ());
        }

        public double getDelta2d() {
            double dx = mStationPoint.getZeroX() - getZeroX();
            double dy = mStationPoint.getZeroY() - getZeroY();
            return Math.hypot(dx, dy);
        }

        public double getDelta3d() {
            return Math.hypot(getDelta1d(), getDelta2d());
        }

        @Override
        public ArrayList<BTopoControlPointObservation> getObservationsAllRaw() {
            return getTcp().ext().getObservationsAllRaw();
        }

        private BTopoControlPoint getTcp() {
            return BTopoMonmon.this.getButterfly().topo().getControlPointByName(BTopoMonmon.this.getName());

        }
    }

}

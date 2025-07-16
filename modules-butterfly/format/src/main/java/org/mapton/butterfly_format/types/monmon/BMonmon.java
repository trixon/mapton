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
package org.mapton.butterfly_format.types.monmon;

import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.butterfly_format.types.structural.BStructuralCrackPointObservation;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class BMonmon extends BBasePoint {

    private final BTopoControlPoint mControlPoint;
    private Ext mExt;
    private final int[] mMeasCount = new int[365];
    private final int mMeasPerDay;
    private String mStationName;
    private BTopoControlPoint mStationPoint;

    public BMonmon(BTopoControlPoint controlPoint, int measPerDay, String stationName) {
        mControlPoint = controlPoint;
        mMeasPerDay = measPerDay;
        mStationName = stationName;
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public BTopoControlPoint getControlPoint() {
        return mControlPoint;
    }

    @Override
    public String getGroup() {
        return mControlPoint.getGroup();
    }

    @Override
    public Double getLat() {
        return mControlPoint.getLat();
    }

    @Override
    public Double getLon() {
        return mControlPoint.getLon();
    }

    public int[] getMeasCount() {
        return mMeasCount;
    }

    public int getMeasPerDay() {
        return mMeasPerDay;
    }

    @Override
    public String getName() {
        return mControlPoint.getName();
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
        this.mStationPoint = stationPoint;
    }

    public class Ext extends BBasePoint.Ext<BStructuralCrackPointObservation> {

        public double getDelta1d() {
            return Math.abs(mControlPoint.getZeroZ() - mStationPoint.getZeroZ());
        }

        public double getDelta2d() {
            double dx = mStationPoint.getZeroX() - mControlPoint.getZeroX();
            double dy = mStationPoint.getZeroY() - mControlPoint.getZeroY();
            return Math.hypot(dx, dy);
        }

        public double getDelta3d() {
            return Math.hypot(getDelta1d(), getDelta2d());
        }
    }

}

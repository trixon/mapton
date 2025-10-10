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

import java.time.LocalDate;
import java.util.List;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.BXyzPointObservation;

/**
 *
 * @author Patrik Karlström
 */
public class BMeasurementReport {

    private LocalDate mFirstDate;
    private LocalDate mLastDate;
    private int mNumOfMeasurements;
    private int mNumOfReplacements;
    private final BXyzPoint mPoint;
    private final List<BMeasurementTab> mTabs;

    public BMeasurementReport(BXyzPoint point, List<BMeasurementTab> tabs) {
        mPoint = point;
        mTabs = tabs;

        BXyzPoint.Ext<? extends BXyzPointObservation> ext = point.extOrNull();
        if (ext != null) {
            mFirstDate = ext.getDateFirst().toLocalDate();
            mLastDate = ext.getDateLatest().toLocalDate();
            mNumOfReplacements = ext.getNumOfReplacements();
            mNumOfMeasurements = ext.getNumOfObservations();
        }
    }

    public LocalDate getFirstDate() {
        return mFirstDate;
    }

    public LocalDate getLastDate() {
        return mLastDate;
    }

    public int getNumOfMeasurements() {
        return mNumOfMeasurements;
    }

    public int getNumOfReplacements() {
        return mNumOfReplacements;
    }

    public BXyzPoint getPoint() {
        return mPoint;
    }

    public List<BMeasurementTab> getTabs() {
        return mTabs;
    }

    public void setFirstDate(LocalDate firstDate) {
        this.mFirstDate = firstDate;
    }

    public void setLastDate(LocalDate lastDate) {
        this.mLastDate = lastDate;
    }

    public void setNumOfMeasurements(int numOfMeasurements) {
        this.mNumOfMeasurements = numOfMeasurements;
    }

    public void setNumOfReplacements(int numOfReplacements) {
        this.mNumOfReplacements = numOfReplacements;
    }

}

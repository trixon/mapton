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
package org.mapton.butterfly_core.api;

import java.awt.Color;
import java.time.LocalDate;
import org.mapton.butterfly_format.types.BAlarm;

/**
 *
 * @author Patrik Karlström
 */
public class ChartConfig {

    private Color mAlarmColor;
    private BAlarm mAlarmHeight;
    private BAlarm mAlarmPlane;
    private String mDelta;
    private LocalDate mObservationRawLastDate;

    public Color getAlarmColor() {
        return mAlarmColor;
    }

    public BAlarm getAlarmHeight() {
        return mAlarmHeight;
    }

    public BAlarm getAlarmPlane() {
        return mAlarmPlane;
    }

    public String getDelta() {
        return mDelta;
    }

    public String getNameOfAlarmHeight() {
        if (mAlarmHeight != null) {
            return mAlarmHeight.getName();
        } else {
            return null;
        }
    }

    public String getNameOfAlarmPlane() {
        if (mAlarmPlane != null) {
            return mAlarmPlane.getName();
        } else {
            return null;
        }
    }

    public LocalDate getObservationRawLastDate() {
        return mObservationRawLastDate;
    }

    public void setAlarmColor(Color alarmColor) {
        this.mAlarmColor = alarmColor;
    }

    public void setAlarmHeight(BAlarm alarmHeight) {
        this.mAlarmHeight = alarmHeight;
    }

    public void setAlarmPlane(BAlarm alarmPlane) {
        this.mAlarmPlane = alarmPlane;
    }

    public void setDelta(String delta) {
        this.mDelta = delta;
    }

    public void setObservationRawLastDate(LocalDate observationRawLastDate) {
        this.mObservationRawLastDate = observationRawLastDate;
    }

}

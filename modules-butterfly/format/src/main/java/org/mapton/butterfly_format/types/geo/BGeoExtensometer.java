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
package org.mapton.butterfly_format.types.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "name",
    "group",
    "category",
    "status",
    "frequency",
    "operator",
    "origin",
    "numOfDecXY",
    "numOfDecZ",
    "limit1",
    "limit2",
    "limit3",
    "dateZero",
    //    "sensors",
    "zeroX",
    "zeroY",
    "zeroZ",
    "dimension",
    "comment",
    "meta"
})
@JsonIgnoreProperties(value = {
    "values",
    "offsetX",
    "offsetY",
    "offsetZ",
    "rollingX",
    "rollingY",
    "rollingZ",
    "dateRolling",
    "tag",
    "nameOfAlarmHeight",
    "nameOfAlarmPlane",
    "dateValidFrom",
    "dateValidTo",
    "lat",
    "lon",
    "dateLatest"
})
public class BGeoExtensometer extends BXyzPoint {

    private transient Ext mExt;
    private transient ArrayList<BGeoExtensometerPoint> mPoints = new ArrayList<>();
    private String sensors;

    public BGeoExtensometer() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public ArrayList<BGeoExtensometerPoint> getPoints() {
        return mPoints;
    }

    public String getSensors() {
        return sensors;
    }

    public void setPoints(ArrayList<BGeoExtensometerPoint> points) {
        this.mPoints = points;
    }

    public void setPoints(String points) {
        this.sensors = points;
    }

    public class Ext {

        public Ext() {
        }

        public LocalDateTime getDateFirst() {
            var first = getPoints().stream().map(p -> p.ext().getDateFirst()).min(LocalDateTime::compareTo).orElse(LocalDateTime.MAX);

            return first;
        }

        public long getMeasurementUntilNext(ChronoUnit chronoUnit) {
            var latest = getPoints().stream().map(p -> p.ext().getDateLatest()).max(LocalDateTime::compareTo).orElse(LocalDateTime.MIN);
            var nextMeas = latest.plusDays(getFrequency());

            return chronoUnit.between(LocalDate.now(), nextMeas);
        }

        public LocalDate getObservationFilteredFirstDate() {
            return getPoints().stream()
                    .map(p -> p.ext().getObservationFilteredFirstDate())
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.MAX);
        }

        public LocalDate getObservationFilteredLastDate() {
            return getPoints().stream()
                    .map(p -> p.ext().getObservationFilteredLastDate())
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.MIN);
        }

        public LocalDate getObservationRawFirstDate() {
            return getPoints().stream()
                    .map(p -> p.ext().getObservationRawFirstDate())
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.MAX);
        }

        public LocalDate getObservationRawLastDate() {
            return getPoints().stream()
                    .map(p -> p.ext().getObservationRawLastDate())
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.MIN);
        }

        public LocalDate getObservationRawNextDate() {
            if (ObjectUtils.allNotNull(getObservationRawLastDate(), getFrequency()) && getFrequency() != 0) {
                return getObservationRawLastDate().plusDays(getFrequency());
            } else {
                return null;
            }
        }

        public boolean hasNoObservations() {
            return mPoints.stream()
                    .noneMatch(point -> !point.ext().getObservationsAllRaw().isEmpty());
        }

    }
}

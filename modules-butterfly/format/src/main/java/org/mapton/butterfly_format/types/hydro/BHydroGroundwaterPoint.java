/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.butterfly_format.types.hydro;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
@JsonPropertyOrder({
    "name",
    "group",
    "category",
    "status",
    "frequency",
    "operator",
    "nameOfAlarmHeight",
    "nameOfAlarmPlane",
    "tag",
    "offsetX",
    "offsetY",
    "offsetZ",
    "dateLatest",
    "dateRolling",
    "dateZero",
    "zeroX",
    "zeroY",
    "zeroZ",
    "comment",
    "meta"
})
public class BHydroGroundwaterPoint extends BXyzPoint {

    @JsonIgnore
    private BDimension dimension;
    @JsonIgnore
    private Ext mExt;
    private Double offsetX;
    private Double offsetY;
    private Double offsetZ;

    public BHydroGroundwaterPoint() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getOffsetX() {
        return offsetX;
    }

    public Double getOffsetY() {
        return offsetY;
    }

    public Double getOffsetZ() {
        return offsetZ;
    }

    public void setOffsetX(Double offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(Double offsetY) {
        this.offsetY = offsetY;
    }

    public void setOffsetZ(Double offsetZ) {
        this.offsetZ = offsetZ;
    }

    public class Ext extends BBasePoint.Ext<BHydroGroundwaterPointObservation> {

        public BHydroGroundwaterPointObservation getMaxObservation() {
            return getObservationsAllRaw().stream()
                    .filter(o -> o.getGroundwaterLevel() != null)
                    .max(Comparator.comparing(BHydroGroundwaterPointObservation::getGroundwaterLevel))
                    .orElse(null);
        }

        public long getMeasurementUntilNext(ChronoUnit chronoUnit) {
            var latest = getDateLatest() != null ? getDateLatest().toLocalDate() : LocalDate.MIN;
            var nextMeas = latest.plusDays(getFrequency());

            return chronoUnit.between(LocalDate.now(), nextMeas);
        }

        public BHydroGroundwaterPointObservation getMinObservation() {
            return getObservationsAllRaw().stream()
                    .filter(o -> o.getGroundwaterLevel() != null)
                    .min(Comparator.comparing(BHydroGroundwaterPointObservation::getGroundwaterLevel))
                    .orElse(null);
        }

        public Double getGroundwaterLevelDiff(int daysBeforeNow) {
            var now = LocalDate.now();
            return getGroundwaterLevelDiff(now.minusDays(daysBeforeNow), now);
        }

        public Double getGroundwaterLevelDiff(LocalDate firstDate, LocalDate lastDate) {
            var obs = getObservationsTimeFiltered().stream()
                    .filter(o -> DateHelper.isBetween(firstDate, lastDate, o.getDate().toLocalDate()))
                    .toList();

            if (obs.size() < 2) {
                return null;
            } else {
                var o1 = obs.getFirst();
                var o2 = obs.getLast();

                if (ObjectUtils.anyNull(o1.getGroundwaterLevel(), o2.getGroundwaterLevel())) {
                    return null;
                }
                return o2.getGroundwaterLevel() - o1.getGroundwaterLevel();
            }
        }
    }
}

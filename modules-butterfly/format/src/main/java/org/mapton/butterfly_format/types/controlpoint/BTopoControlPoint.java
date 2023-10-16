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
package org.mapton.butterfly_format.types.controlpoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_format.types.BDimension;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
@JsonPropertyOrder({
    "name",
    "group",
    "category",
    "dimension",
    "status",
    "frequency",
    "operator",
    "nameOfAlarmHeight",
    "nameOfAlarmPlane",
    "tag",
    "numOfDecXY",
    "numOfDecZ",
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
@JsonIgnoreProperties(value = {"values"})
public class BTopoControlPoint extends BBaseControlPoint {

    private BDimension dimension;
    @JsonIgnore
    private Ext mExt;
    private String nameOfAlarmHeight;
    private String nameOfAlarmPlane;
    private Double offsetX;
    private Double offsetY;
    private Double offsetZ;

    public BTopoControlPoint() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public BDimension getDimension() {
        return dimension;
    }

    public String getNameOfAlarmHeight() {
        return nameOfAlarmHeight;
    }

    public String getNameOfAlarmPlane() {
        return nameOfAlarmPlane;
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

    public void setDimension(BDimension dimension) {
        this.dimension = dimension;
    }

    public void setNameOfAlarmHeight(String nameOfAlarmHeight) {
        this.nameOfAlarmHeight = nameOfAlarmHeight;
    }

    public void setNameOfAlarmPlane(String nameOfAlarmPlane) {
        this.nameOfAlarmPlane = nameOfAlarmPlane;
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

    public class Ext extends BBaseControlPoint.Ext {

        private transient LinkedHashMap<String, Integer> measuremenCountStats = new LinkedHashMap<>();
        private transient ArrayList<BTopoControlPointObservation> observationsCalculated;
        private transient ArrayList<BTopoControlPointObservation> observationsRaw;

        public String getDelta(int decimals) {
            return StringHelper.joinNonNulls(", ",
                    getDelta1(decimals),
                    getDelta2(decimals),
                    getDelta3(decimals)
            );
        }

        public Double getDelta1() {
            return getDeltaZ();
        }

        public String getDelta1(int decimals) {
            var delta = getDelta1();
            return delta == null ? null : StringHelper.round(delta, decimals, "Δ1d=", "");
        }

        public String getDelta2(int decimals) {
            var delta = getDelta2();
            return delta == null ? null : StringHelper.round(delta, decimals, "Δ2d=", "");
        }

        public Double getDelta2() {
            if (ObjectUtils.allNotNull(getDeltaX(), getDeltaY())) {
                return Math.hypot(getDeltaX(), getDeltaY());
            } else {
                return null;
            }
        }

        public String getDelta3(int decimals) {
            var delta = getDelta3();
            return delta == null ? null : StringHelper.round(delta, decimals, "Δ3d=", "");
        }

        public Double getDelta3() {
            if (ObjectUtils.allNotNull(getDelta1(), getDelta2())) {
                return Math.hypot(getDelta1(), getDelta2());
            } else {
                return null;
            }
        }

        public String getDeltaX(int decimals) {
            var delta = getDeltaX();
            return delta == null ? null : StringHelper.round(delta, decimals, "ΔX=", "");
        }

        public Double getDeltaX() {
            return getObservationsCalculated().isEmpty() ? null : getObservationsCalculated().getLast().ext().getDeltaX();
        }

        public String getDeltaY(int decimals) {
            var delta = getDeltaY();
            return delta == null ? null : StringHelper.round(delta, decimals, "ΔY=", "");
        }

        public Double getDeltaY() {
            return getObservationsCalculated().isEmpty() ? null : getObservationsCalculated().getLast().ext().getDeltaY();
        }

        public String getDeltaZ(int decimals) {
            var delta = getDeltaZ();
            return delta == null ? null : StringHelper.round(delta, decimals, "ΔZ=", "");
        }

        public Double getDeltaZ() {
            return getObservationsCalculated().isEmpty() ? null : getObservationsCalculated().getLast().ext().getDeltaZ();
        }

        public long getMeasurementAge(ChronoUnit chronoUnit) {
            var latest = getDateLatest() != null ? getDateLatest().toLocalDate() : LocalDate.MIN;

            return chronoUnit.between(latest, LocalDate.now());
        }

        public LinkedHashMap<String, Integer> getMeasurementCountStats() {
            return measuremenCountStats;
        }

        public long getMeasurementUntilNext(ChronoUnit chronoUnit) {
            var latest = getDateLatest() != null ? getDateLatest().toLocalDate() : LocalDate.MIN;
            var nextMeas = latest.plusDays(getFrequency());

            return chronoUnit.between(LocalDate.now(), nextMeas);
        }

        public int getNumOfObservations() {
            return getObservationsRaw().size();
        }

        public int getNumOfObservationsTimeFiltered() {
            return getObservationsCalculated().size();
        }

        public ArrayList<BTopoControlPointObservation> getObservationsCalculated() {
            if (observationsCalculated == null) {
                observationsCalculated = new ArrayList<>();
            }

            return observationsCalculated;
        }

        public ArrayList<BTopoControlPointObservation> getObservationsRaw() {
            if (observationsRaw == null) {
                observationsRaw = new ArrayList<>();
            }

            return observationsRaw;
        }

        public void setMeasurementCountStats(LinkedHashMap<String, Integer> measuremenCountStats) {
            this.measuremenCountStats = measuremenCountStats;
        }

        public void setObservationsCalculated(ArrayList<BTopoControlPointObservation> observationsCalculated) {
            this.observationsCalculated = observationsCalculated;
        }

        public void setObservationsRaw(ArrayList<BTopoControlPointObservation> observationsRaw) {
            this.observationsRaw = observationsRaw;
        }

    }
}

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
package org.mapton.butterfly_format.types.topo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BBase;
import org.mapton.butterfly_format.types.BBaseControlPoint;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import static org.mapton.butterfly_format.types.BDimension._1d;
import static org.mapton.butterfly_format.types.BDimension._2d;
import static org.mapton.butterfly_format.types.BDimension._3d;
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
    "rollingX",
    "rollingY",
    "rollingZ",
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

    public class Ext extends BBase.Ext<BTopoControlPointObservation> {

        private transient final DeltaRolling deltaRolling = new DeltaRolling();
        private transient final DeltaZero deltaZero = new DeltaZero();

        public Ext() {
            Ext.this.getObservationFilteredFirst();
        }

        public void calculateObservations(List<BTopoControlPointObservation> observations) {
            if (observations.isEmpty()) {
                return;
            }

            observations.getFirst().setZeroMeasurement(true);
            var latestZero = observations.reversed().stream()
                    .filter(o -> o.isZeroMeasurement())
                    .findFirst().orElse(observations.getFirst());

            Double zX = latestZero.getMeasuredX();
            Double zY = latestZero.getMeasuredY();
            Double zZ = latestZero.getMeasuredZ();
            var rX = 0.0;
            var rY = 0.0;
            var rZ = 0.0;

            for (int i = 0; i < observations.size(); i++) {
                var o = observations.get(i);
                BTopoControlPointObservation prev = null;
                if (i > 0) {
                    prev = observations.get(i - 1);
                }
                Double x = o.getMeasuredX();
                Double y = o.getMeasuredY();
                Double z = o.getMeasuredZ();

                if (ObjectUtils.allNotNull(x, zX)) {
                    o.ext().setDeltaX(x - zX);
                }
                if (ObjectUtils.allNotNull(y, zY)) {
                    o.ext().setDeltaY(y - zY);
                }
                if (ObjectUtils.allNotNull(z, zZ)) {
                    o.ext().setDeltaZ(z - zZ);
                }

                if (o.isReplacementMeasurement() && prev != null) {
                    var mX = o.getMeasuredX();
                    var pX = prev.getMeasuredX();
                    if (ObjectUtils.allNotNull(mX, pX)) {
                        rX = rX + mX - pX;
                        o.ext().setDeltaX(o.ext().getDeltaX() + rX);
                    }

                    var mY = o.getMeasuredY();
                    var pY = prev.getMeasuredY();
                    if (ObjectUtils.allNotNull(mY, pY)) {
                        rY = rY + mY - pY;
                        o.ext().setDeltaY(o.ext().getDeltaY() + rY);
                    }

                    var mZ = o.getMeasuredZ();
                    var pZ = prev.getMeasuredZ();
                    if (ObjectUtils.allNotNull(mZ, pZ, o.ext().getDeltaZ())) {
                        rZ = rZ + mZ - pZ;
                        o.ext().setDeltaZ(o.ext().getDeltaZ() + rZ);
                    }
                }
            }
        }

        public DeltaRolling deltaRolling() {
            return deltaRolling;
        }

        public DeltaZero deltaZero() {
            return deltaZero;
        }

        public BAlarm getAlarm(BComponent component) {
            var alarm = getButterfly().getAlarms().stream()
                    .filter(a -> {
                        if (component == BComponent.HEIGHT) {
                            return StringUtils.equals(a.getId(), nameOfAlarmHeight);
                        } else {
                            return StringUtils.equals(a.getId(), nameOfAlarmPlane);
                        }
                    }).findAny().orElse(null);

            return alarm;
        }

        public int getAlarmLevel(BComponent component, BTopoControlPointObservation o) {
            var alarm = getAlarm(component);

            if (ObjectUtils.anyNull(alarm, o)) {
                return -1;
            } else {
                return alarm.ext().getLevel(component == BComponent.HEIGHT ? o.ext().getDeltaZ() : o.ext().getDelta2d());
            }
        }

        public int getAlarmLevel(BTopoControlPointObservation o) {
            return Math.max(getAlarmLevelHeight(o), getAlarmLevelPlane(o));
        }

        public int getAlarmLevelHeight(BTopoControlPointObservation o) {
            return getAlarmLevel(BComponent.HEIGHT, o);
        }

        public int getAlarmLevelPlane(BTopoControlPointObservation o) {
            return getAlarmLevel(BComponent.PLANE, o);
        }

        public long getMeasurementUntilNext(ChronoUnit chronoUnit) {
            var latest = getDateLatest() != null ? getDateLatest().toLocalDate() : LocalDate.MIN;
            var nextMeas = latest.plusDays(getFrequency());

            return chronoUnit.between(LocalDate.now(), nextMeas);
        }

        public abstract class Delta {

            public String getDelta(int decimals) {
                return StringHelper.joinNonNulls(", ",
                        getDelta1(decimals),
                        getDelta2(decimals),
                        getDelta3(decimals)
                );
            }

            public Double getDelta() {
                switch (getDimension()) {
                    case _1d -> {
                        return getDelta1();
                    }
                    case _2d -> {
                        return getDelta2();
                    }
                    case _3d -> {
                        return getDelta3();
                    }
                }

                return null;
            }

            public Double getDelta1() {
                return getDeltaZ();
            }

            public String getDelta1(int decimals) {
                var delta = getDelta1();
                return delta == null ? null : StringHelper.round(delta, decimals, "Δ1d=", "", true);
            }

            public String getDelta2(int decimals) {
                var delta = getDelta2();
                return delta == null ? null : StringHelper.round(delta, decimals, "Δ2d=", "", false);
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
                return delta == null ? null : StringHelper.round(delta, decimals, "Δ3d=", "", false);
            }

            public Double getDelta3() {
                if (ObjectUtils.allNotNull(getDelta1(), getDelta2())) {
                    return Math.hypot(getDelta1(), getDelta2());
                } else {
                    return null;
                }
            }

            public abstract Double getDeltaX();

            public String getDeltaX(int decimals) {
                var delta = getDeltaX();
                return delta == null ? null : StringHelper.round(delta, decimals, "ΔX=", "", false);
            }

            public abstract Double getDeltaY();

            public String getDeltaY(int decimals) {
                var delta = getDeltaY();
                return delta == null ? null : StringHelper.round(delta, decimals, "ΔY=", "", false);
            }

            public abstract Double getDeltaZ();

            public String getDeltaZ(int decimals) {
                var delta = getDeltaZ();
                return delta == null ? null : StringHelper.round(delta, decimals, "ΔZ=", "", true);
            }
        }

        public class DeltaRolling extends Delta {

            @Override
            public Double getDeltaX() {
                var observations = ext().getObservationsTimeFiltered();
                if (observations == null || observations.isEmpty()) {
                    return null;
                }

                if (ObjectUtils.allNotNull(getRollingX(), observations.getLast().getMeasuredX())) {
                    return observations.getLast().getMeasuredX() - getRollingX();
                } else {
                    return null;
                }
            }

            @Override
            public Double getDeltaY() {
                var observations = ext().getObservationsTimeFiltered();
                if (observations == null || observations.isEmpty()) {
                    return null;
                }

                if (ObjectUtils.allNotNull(getRollingY(), observations.getLast().getMeasuredY())) {
                    return observations.getLast().getMeasuredY() - getRollingY();
                } else {
                    return null;
                }
            }

            @Override
            public Double getDeltaZ() {
                var observations = ext().getObservationsTimeFiltered();
                if (observations == null || observations.isEmpty()) {
                    return null;
                }

                if (ObjectUtils.allNotNull(getRollingZ(), observations.getLast().getMeasuredZ())) {
                    return observations.getLast().getMeasuredZ() - getRollingZ();
                } else {
                    return null;
                }
            }
        }

        public class DeltaZero extends Delta {

            @Override
            public Double getDeltaX() {
                return getObservationsTimeFiltered().isEmpty() ? null : getObservationsTimeFiltered().getLast().ext().getDeltaX();
            }

            @Override
            public Double getDeltaY() {
                return getObservationsTimeFiltered().isEmpty() ? null : getObservationsTimeFiltered().getLast().ext().getDeltaY();
            }

            @Override
            public Double getDeltaZ() {
                return getObservationsTimeFiltered().isEmpty() ? null : getObservationsTimeFiltered().getLast().ext().getDeltaZ();
            }
        }

    }
}

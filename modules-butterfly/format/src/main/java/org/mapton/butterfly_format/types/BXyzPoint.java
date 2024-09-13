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
package org.mapton.butterfly_format.types;

import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import static org.mapton.butterfly_format.types.BDimension._1d;
import static org.mapton.butterfly_format.types.BDimension._2d;
import static org.mapton.butterfly_format.types.BDimension._3d;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BXyzPoint extends BBaseControlPoint {

    private BDimension dimension;
    private Integer numOfDecXY;
    private Integer numOfDecZ;
    private Double rollingX;
    private Double rollingY;
    private Double rollingZ;
    private Double zeroX;
    private Double zeroY;
    private Double zeroZ;

    public BDimension getDimension() {
        return dimension;
    }

    public Integer getNumOfDecXY() {
        return numOfDecXY;
    }

    public Integer getNumOfDecZ() {
        return numOfDecZ;
    }

    public Double getRollingX() {
        return rollingX;
    }

    public Double getRollingY() {
        return rollingY;
    }

    public Double getRollingZ() {
        return rollingZ;
    }

    public Double getZeroX() {
        return zeroX;
    }

    public Double getZeroY() {
        return zeroY;
    }

    public Double getZeroZ() {
        return zeroZ;
    }

    public void setDimension(BDimension dimension) {
        this.dimension = dimension;
    }

    public void setNumOfDecXY(Integer numOfDecXY) {
        this.numOfDecXY = numOfDecXY;
    }

    public void setNumOfDecZ(Integer numOfDecZ) {
        this.numOfDecZ = numOfDecZ;
    }

    public void setRollingX(Double rollingX) {
        this.rollingX = rollingX;
    }

    public void setRollingY(Double rollingY) {
        this.rollingY = rollingY;
    }

    public void setRollingZ(Double rollingZ) {
        this.rollingZ = rollingZ;
    }

    public void setZeroX(Double zeroX) {
        this.zeroX = zeroX;
    }

    public void setZeroY(Double zeroY) {
        this.zeroY = zeroY;
    }

    public void setZeroZ(Double zeroZ) {
        this.zeroZ = zeroZ;
    }

    public abstract class Ext<T extends BXyzPointObservation> extends BBasePoint.Ext<T> {

        private transient final DeltaRolling deltaRolling = new DeltaRolling();
        private transient final DeltaZero deltaZero = new DeltaZero();

        public DeltaRolling deltaRolling() {
            return deltaRolling;
        }

        public DeltaZero deltaZero() {
            return deltaZero;
        }

        public void calculateObservations(List<T> observations) {
            if (observations.isEmpty()) {
                return;
            }

            observations.forEach(o -> {
                var dateMatch = getStoredZeroDateTime() == o.getDate();
                if (dateMatch) {
                    setZeroUnset(false);
                }
                o.setZeroMeasurement(dateMatch);
            });

            if (isZeroUnset()) {
                observations.getFirst().setZeroMeasurement(true);
            }

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
                BXyzPointObservation prev = null;
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
                    if (ObjectUtils.allNotNull(mX, pX, o.ext().getDeltaX())) {
                        rX = rX + mX - pX;
                        o.ext().setDeltaX(o.ext().getDeltaX() + rX);
                    }

                    var mY = o.getMeasuredY();
                    var pY = prev.getMeasuredY();
                    if (ObjectUtils.allNotNull(mY, pY, o.ext().getDeltaY())) {
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
                    return Math.hypot(getDelta1(), getDelta2()) * MathHelper.sign(getDelta1());
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
                var observations = getObservationsTimeFiltered();
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
                var observations = getObservationsTimeFiltered();
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
                var observations = getObservationsTimeFiltered();
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

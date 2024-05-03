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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_format.types.BBase;
import org.mapton.butterfly_format.types.BBaseControlPoint;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "name",
    "extensometer",
    "status",
    "frequency",
    "tag",
    "numOfDecXY",
    "numOfDecZ",
    "limit1",
    "limit2",
    "limit3",
    "offsetX",
    "offsetY",
    "offsetZ",
    "dateRolling",
    "dateZero",
    "zeroX",
    "zeroY",
    "zeroZ",
    "comment",
    "meta"
})
@JsonIgnoreProperties(value = {
    "rollingX",
    "rollingY",
    "rollingZ",
    "group",
    "category",
    "operator",
    "values",
    "dimension",
    "nameOfAlarmHeight",
    "nameOfAlarmPlane",
    "dateLatest"
})
public class BGeoExtensometerPoint extends BBaseControlPoint {

    @JsonIgnore
    private transient String category;
    private String extensometer;
    @JsonIgnore
    private transient String group;
    private double limit1;
    private double limit2;
    private double limit3;
    @JsonIgnore
    private Ext mExt;
    @JsonIgnore
    private transient String operator;

    public BGeoExtensometerPoint() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }
        return mExt;
    }

    public String getExtensometer() {
        return extensometer;
    }

    public double getLimit1() {
        return limit1;
    }

    public double getLimit2() {
        return limit2;
    }

    public double getLimit3() {
        return limit3;
    }

    public void setExtensometer(String extensometer) {
        this.extensometer = extensometer;
    }

    public void setLimit1(double limit1) {
        this.limit1 = limit1;
    }

    public void setLimit2(double limit2) {
        this.limit2 = limit2;
    }

    public void setLimit3(double limit3) {
        this.limit3 = limit3;
    }

    public class Ext extends BBase.Ext<BGeoExtensometerPointObservation> {

        public Ext() {
        }

        public void calculateObservations(ArrayList<BGeoExtensometerPointObservation> observations) {
            if (observations.isEmpty()) {
                return;
            }

            var p = BGeoExtensometerPoint.this;

            observations.forEach(o -> {
                var dateMatch = p.ext().getStoredZeroDateTime() == o.getDate();
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

//            Double zX = latestZero.getMeasuredX();
//            Double zY = latestZero.getMeasuredY();
            Double zZ = latestZero.getMeasuredZ();
//            var rX = 0.0;
//            var rY = 0.0;
            var rZ = 0.0;

            for (int i = 0; i < observations.size(); i++) {
                var o = observations.get(i);
                BGeoExtensometerPointObservation prev = null;
                if (i > 0) {
                    prev = observations.get(i - 1);
                }
//                Double x = o.getMeasuredX();
//                Double y = o.getMeasuredY();
                Double z = o.getMeasuredZ();

//                if (ObjectUtils.allNotNull(x, zX)) {
//                    o.ext().setDeltaX(x - zX);
//                }
//                if (ObjectUtils.allNotNull(y, zY)) {
//                    o.ext().setDeltaY(y - zY);
//                }
                if (ObjectUtils.allNotNull(z, zZ)) {
                    o.ext().setDeltaZ(z - zZ);
                }

                if (o.isReplacementMeasurement() && prev != null) {
//                    var mX = o.getMeasuredX();
//                    var pX = prev.getMeasuredX();
//                    if (ObjectUtils.allNotNull(mX, pX, o.ext().getDeltaX())) {
//                        rX = rX + mX - pX;
//                        o.ext().setDeltaX(o.ext().getDeltaX() + rX);
//                    }

//                    var mY = o.getMeasuredY();
//                    var pY = prev.getMeasuredY();
//                    if (ObjectUtils.allNotNull(mY, pY, o.ext().getDeltaY())) {
//                        rY = rY + mY - pY;
//                        o.ext().setDeltaY(o.ext().getDeltaY() + rY);
//                    }
                    var mZ = o.getMeasuredZ();
                    var pZ = prev.getMeasuredZ();
                    if (ObjectUtils.allNotNull(mZ, pZ, o.ext().getDeltaZ())) {
                        rZ = rZ + mZ - pZ;
                        o.ext().setDeltaZ(o.ext().getDeltaZ() + rZ);
                    }
                }
            }
        }

    }

}

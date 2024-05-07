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
package org.mapton.butterfly_format.types.geo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.mapton.butterfly_format.types.BBaseControlPointObservation;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
@JsonPropertyOrder({
    "name",
    "date",
    "value",
    "status",
    "replacementMeasurement",
    "zeroMeasurement"
})
@JsonIgnoreProperties(ignoreUnknown = true, value = {
    "comment",
    "operator",
    "instrument",
    "measuredX",
    "measuredY",
    "values",
    "offsetX",
    "offsetY",
    "offsetZ",
    "rollingX",
    "rollingY",
    "rollingZ",
    "dateRolling",
    "tag",
    "dimension",
    "nameOfAlarmHeight",
    "nameOfAlarmPlane",
    "dateValidFrom", "dateValidTo", "lat", "lon", "origin",
    "dateLatest"
})
public class BGeoExtensometerPointObservation extends BBaseControlPointObservation {

    @JsonIgnore
    private transient String comment;
    @JsonIgnore
    private transient String instrument;
    @JsonIgnore
    private BGeoExtensometerPointObservation.Ext mExt;
    @JsonIgnore
    private transient String operator;
    private Double value;

    public BGeoExtensometerPointObservation() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double measuredZ) {
        this.value = measuredZ;
    }

    public class Ext {

        private Double mDelta;
        private BGeoExtensometerPoint mParent;

        public Ext() {
        }

        public Double getDelta() {
            return mDelta;
        }

        public BGeoExtensometerPoint getParent() {
            return mParent;
        }

        public void setDelta(Double delta) {
            this.mDelta = delta;
        }

        public void setParent(BGeoExtensometerPoint point) {
            this.mParent = point;
        }
    }
}

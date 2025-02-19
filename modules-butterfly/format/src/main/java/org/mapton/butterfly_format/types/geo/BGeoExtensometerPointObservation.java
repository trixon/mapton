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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.mapton.butterfly_format.types.BXyzPointObservation;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
@JsonPropertyOrder({
    "name",
    "date",
    "measuredZ",
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
public class BGeoExtensometerPointObservation extends BXyzPointObservation {

    private transient String comment;
    private transient String instrument;
    private transient BGeoExtensometerPointObservation.Ext mExt;
    private transient String operator;

    public BGeoExtensometerPointObservation() {
    }

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public class Ext extends BXyzPointObservation.Ext<BGeoExtensometerPoint> {

    }
}

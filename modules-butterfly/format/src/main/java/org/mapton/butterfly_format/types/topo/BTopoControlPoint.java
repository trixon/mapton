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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.mapton.butterfly_format.types.BXyzPoint;

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
    "alarm1Id",
    "alarm2Id",
    "tag",
    "numOfDecXY",
    "numOfDecZ",
    "offsetX",
    "offsetY",
    "offsetZ",
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
@JsonIgnoreProperties(value = {"values", "dateLatest"})
public class BTopoControlPoint extends BXyzPoint {

    private transient Ext mExt;

    public BTopoControlPoint() {
    }

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public class Ext extends BXyzPoint.Ext<BTopoControlPointObservation> {

        public Ext() {
            Ext.this.getObservationFilteredFirst();
        }

        public BTopoControlPointObservation getReferenceObservation() {
            return getObservationsTimeFiltered().stream()
                    .filter(o -> o.isZeroMeasurement())
                    .findFirst().orElse(getObservationsTimeFiltered().getFirst());
        }
    }
}

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
import org.mapton.butterfly_format.types.BBaseControlPoint;

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
    "numOfDecXY",
    "numOfDecZ",
    "limit1",
    "limit2",
    "limit3",
    "dateZero",
    "zeroX",
    "zeroY",
    "zeroZ",
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
    "dimension",
    "nameOfAlarmHeight",
    "nameOfAlarmPlane",
    "dateValidFrom", "dateValidTo", "lat", "lon", "origin",
    "dateLatest"
})
public class BGeoExtensometerGroup extends BBaseControlPoint {

    private String points;

    public BGeoExtensometerGroup() {
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

}

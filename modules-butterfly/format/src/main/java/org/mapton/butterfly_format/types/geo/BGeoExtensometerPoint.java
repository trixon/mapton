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
    "rollingX",
    "rollingY",
    "rollingZ",
    "comment",
    "meta"
})
@JsonIgnoreProperties(value = {
    "values",
    "dimension",
    "nameOfAlarmHeight",
    "nameOfAlarmPlane",
    "dateLatest"
})
public class BGeoExtensometerPoint extends BBaseControlPoint {

    private double limit1;
    private double limit2;
    private double limit3;

    public BGeoExtensometerPoint() {
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

    public void setLimit1(double limit1) {
        this.limit1 = limit1;
    }

    public void setLimit2(double limit2) {
        this.limit2 = limit2;
    }

    public void setLimit3(double limit3) {
        this.limit3 = limit3;
    }

}

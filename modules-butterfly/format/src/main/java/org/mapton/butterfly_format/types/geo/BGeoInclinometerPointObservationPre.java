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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.mapton.butterfly_format.types.BXyzPointObservation;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "name",
    "date",
    "down",
    "a",
    "b",
    "replacementMeasurement",
    "zeroMeasurement"
})
public class BGeoInclinometerPointObservationPre extends BXyzPointObservation {

    private Double a;
    private Double b;
    private Double down;
    private transient Ext mExt;

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getA() {
        return a;
    }

    public Double getB() {
        return b;
    }

    public Double getDown() {
        return down;
    }

    public void setA(Double a) {
        this.a = a;
    }

    public void setB(Double b) {
        this.b = b;
    }

    public void setDown(Double down) {
        this.down = down;
    }

    public class Ext extends BXyzPointObservation.Ext<BGeoInclinometerPoint> {

        public Ext() {
        }

    }

}

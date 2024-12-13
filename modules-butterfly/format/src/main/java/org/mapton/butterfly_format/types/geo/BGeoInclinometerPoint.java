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

import org.mapton.butterfly_format.types.BMeasurementMode;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public class BGeoInclinometerPoint extends BXyzPoint {

    private Ext mExt;
    private BMeasurementMode mMeasurementMode;

    public BGeoInclinometerPoint() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public BMeasurementMode getMeasurementMode() {
        return mMeasurementMode;
    }

    public void setMeasurementMode(BMeasurementMode measurementMode) {
        mMeasurementMode = measurementMode;
    }

    public class Ext extends BXyzPoint.Ext<BGeoInclinometerPointObservation> {

        public String getDeltaRolling() {
            return getDelta(deltaRolling());
        }

        public String getDeltaZero() {
            return getDelta(deltaZero());
        }

        private String getDelta(BXyzPoint.Ext.Delta delta) {
            var s = "Δ=%.3fmm".formatted(delta.getDeltaZ());

            return s;
        }
    }

}

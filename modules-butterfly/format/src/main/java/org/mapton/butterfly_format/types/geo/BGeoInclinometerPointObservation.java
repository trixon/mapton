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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "name",
    "date",
    "values",
    "down",
    "a",
    "b",
    "replacementMeasurement",
    "zeroMeasurement"
})
public class BGeoInclinometerPointObservation extends BXyzPointObservation {

    @JsonIgnore
    private Ext mExt;
    private final ArrayList<ObservationItem> mObservationItems = new ArrayList<>();

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public ArrayList<ObservationItem> getObservationItems() {
        return mObservationItems;
    }

    public static class ObservationItem {

        private Double mA;
        private Double mAzimuth = 0.0;
        private Double mB;
        private Double mDistance = 0.0;
        private Double mDown;

        public ObservationItem() {
        }

        public Double getA() {
            return mA;
        }

        public Double getAzimuth() {
            return mAzimuth;
        }

        public Double getB() {
            return mB;
        }

        public Double getDistance() {
            return mDistance;
        }

        public Double getDown() {
            return mDown;
        }

        public void recalc() {
            if (mA != 0 && mB != 0) {
                setAzimuth(MathHelper.azimuthToDegrees(mA, mB));
                setDistance(Math.hypot(mA, mB));
            }
        }

        public void setA(Double a) {
            mA = a;
        }

        public void setAzimuth(Double azimuth) {
            mAzimuth = azimuth;
        }

        public void setB(Double b) {
            mB = b;
        }

        public void setDistance(Double distance) {
            mDistance = distance;
        }

        public void setDown(Double down) {
            mDown = down;
        }

    }

    public class Ext extends BXyzPointObservation.Ext<BGeoInclinometerPoint> {

        public Ext() {
        }

    }
}

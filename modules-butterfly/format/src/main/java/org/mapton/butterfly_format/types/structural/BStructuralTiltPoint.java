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
package org.mapton.butterfly_format.types.structural;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public class BStructuralTiltPoint extends BXyzPoint {

    @JsonIgnore
    private Ext mExt;
    private Double zeroTiltX;
    private Double zeroTiltY;
    private Double zeroTiltZ;
    private Double directionX;

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getDirectionX() {
        return directionX;
    }

    public Double getZeroTiltX() {
        return zeroTiltX;
    }

    public Double getZeroTiltY() {
        return zeroTiltY;
    }

    public Double getZeroTiltZ() {
        return zeroTiltZ;
    }

    public void setDirectionX(Double directionX) {
        this.directionX = directionX;
    }

    public void setZeroTiltX(Double zeroTiltX) {
        this.zeroTiltX = zeroTiltX;
    }

    public void setZeroTiltY(Double zeroTiltY) {
        this.zeroTiltY = zeroTiltY;
    }

    public void setZeroTiltZ(Double zeroTiltZ) {
        this.zeroTiltZ = zeroTiltZ;
    }

    public class Ext extends BXyzPoint.Ext<BStructuralTiltPointObservation> {

        public String getDeltaRolling() {
            return getDelta(deltaRolling());
        }

        public String getDeltaZero() {
            return getDelta(deltaZero());
        }

        public long getMeasurementUntilNext(ChronoUnit chronoUnit) {
            var latest = getDateLatest() != null ? getDateLatest().toLocalDate() : LocalDate.MIN;
            var nextMeas = latest.plusDays(getFrequency());

            return chronoUnit.between(LocalDate.now(), nextMeas);
        }

        private String getDelta(Delta delta) {
            var dR = "";
            if (delta.getDeltaZ() != null) {
                dR = ", R=%.1f".formatted(Math.abs(delta.getDeltaZ()));
            }

            var s = "X=%.1f, Y=%.1f%s".formatted(delta.getDeltaX(), delta.getDeltaY(), dR);

            return s;
        }
    }

}

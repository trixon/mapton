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
import org.apache.commons.lang3.Range;
import org.mapton.butterfly_format.types.BComponent;
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

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
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

        public String getAlarmPercentString() {
            var percent = getAlarmPercent();
            if (percent == null) {
                return "";
            } else {
                return "%d%%".formatted(percent);
            }
        }

        @Override
        public Integer getAlarmPercent() {
            var alarm = getAlarm(BComponent.HEIGHT);
            if (alarm == null) {
                return null;
            }

            try {
                var delta = deltaZero().getDelta2();
                delta = Math.toDegrees(delta) / 1000.0;
                double limit;
                Range<Double> range;
                if (alarm.ext().getRange2() != null) {
                    range = alarm.ext().getRange2();
                } else if (alarm.ext().getRange1() != null) {
                    range = alarm.ext().getRange1();
                } else {
                    range = alarm.ext().getRange0();
                }

                limit = range.getMaximum();
                return (int) Math.round((delta / limit) * 100);
            } catch (Exception e) {
                return null;
            }
        }

        public String getDeltaRolling() {
            return getDelta(deltaRolling());
        }

        public String getDeltaZero() {
            return getDelta(deltaZero());
        }

        private String getDelta(Delta delta) {
            var s = "X=%.1f, Y=%.1f, R=%.1f".formatted(delta.getDeltaX(), delta.getDeltaY(), delta.getDelta2());

            return s;
        }
    }

}

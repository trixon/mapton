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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.mapton.butterfly_format.types.BComponent;
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

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public class Ext extends BXyzPoint.Ext<BTopoControlPointObservation> {

        private transient final LimitValuePredictor limitValuePredictor = new LimitValuePredictor();

        public Ext() {
            Ext.this.getObservationFilteredFirst();
        }

        public BTopoControlPointObservation getReferenceObservation() {
            return getObservationsTimeFiltered().stream()
                    .filter(o -> o.isZeroMeasurement())
                    .findFirst().orElse(getObservationsTimeFiltered().getFirst());
        }

//        public boolean firstIsZero() {
//            if (getObservationsAllRaw().isEmpty()) {
//                return false;
//            } else {
//                return getObservationRawFirstDate().equals(getDateZero());
//            }
//        }
        public LimitValuePredictor limitValuePredictor() {
            return limitValuePredictor;
        }

        public class LimitValuePredictor {
            //TODO Make from trend, not only last two measurements

            public Double getRemainingUntilLimit() {
                if (getObservationsTimeFiltered().size() < 2) {
                    return null;
                }
                try {
                    var alarm = getAlarm(BComponent.HEIGHT);
                    var targetValue = isRisingByTrend() ? alarm.ext().getRange1().getMaximum() : alarm.ext().getRange1().getMinimum();
                    var remaining = targetValue - deltaZero().getDelta1();

                    return Math.abs(remaining);
                } catch (Exception e) {
                    return null;
                }
            }

            public Boolean isRisingByTrend() {
                if (getObservationsTimeFiltered().size() < 2) {
                    return null;
                }

                var lastObservation = getObservationFilteredLast();
                var secondLastObservation = getObservationsTimeFiltered().get(getObservationsTimeFiltered().size() - 2);
                var rising = lastObservation.ext().getDeltaZ() - secondLastObservation.ext().getDeltaZ() >= 0;

                return rising;
            }

            public String getExtrapolatedLimitDate() {
                var remainingDays = getExtrapolatedLimitDays();
                if (remainingDays == null) {
                    return "-";
                } else if (remainingDays == Long.MAX_VALUE) {
                    return "E";
                } else if (remainingDays >= 5 * 365) {
                    return ">=5 år";
                } else {
                    var lastObservation = getObservationFilteredLast();
                    var targetDate = lastObservation.getDate().plusDays(Math.round(remainingDays));
                    var tooLateIndicator = targetDate.isBefore(LocalDateTime.now()) ? "*" : "";

                    return "%s%s".formatted(targetDate.toLocalDate().toString(), tooLateIndicator);
                }
            }

            public Long getExtrapolatedLimitDaysFromNow() {
                try {
                    return getExtrapolatedLimitDays() - getMeasurementAge(ChronoUnit.DAYS);
                } catch (Exception e) {
                    return null;
                }
            }

            public Long getExtrapolatedLimitDays() {
                var lastObservation = getObservationFilteredLast();
                if (lastObservation == null || getAlarmLevel(BComponent.HEIGHT, lastObservation) == 2) {
                    return null;
                }

                try {
                    var measurementAge = getMeasurementAge(ChronoUnit.DAYS);
                    var speed = getSpeed()[0];
                    var remaining = getRemainingUntilLimit();
                    var remainingDays = Math.abs(remaining / speed) * 365;
                    remainingDays = (remainingDays - measurementAge);

                    return Math.round(remainingDays);
                } catch (Exception e) {
                    System.out.println(e);
                    return Long.MAX_VALUE;
                }
            }
        }
    }
}

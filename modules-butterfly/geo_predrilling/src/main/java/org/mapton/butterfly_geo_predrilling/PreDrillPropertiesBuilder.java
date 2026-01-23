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
package org.mapton.butterfly_geo_predrilling;

import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import org.mapton.butterfly_core.api.AlarmHelper;
import org.mapton.butterfly_core.api.BPropertiesBuilder;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BMeasurementMode;
import org.mapton.butterfly_format.types.geo.BGeoPreDrillPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class PreDrillPropertiesBuilder extends BPropertiesBuilder<BGeoPreDrillPoint> {

    @Override
    public Object build(BGeoPreDrillPoint p) {
        if (p == null) {
            return p;
        }
        p.setMeasurementMode(BMeasurementMode.MANUAL);
        /*
         */
        var propertyMap = new LinkedHashMap<String, Object>();
//******************************************************************************
        var basicParams = new BPropertiesBuilder.BasicParams();
        propertyMap.putAll(populateBasics(p, basicParams));
//******************************************************************************
        Double azimuth = null;
        try {
            var o = p.ext().getObservationsTimeFiltered().getLast();
            azimuth = o.ext().getBearing();
        } catch (Exception e) {
        }
        var measParams = new BPropertiesBuilder.MeasParams<BTopoControlPoint>(
                azimuth,
                p.ext().getMeasurementUntilNext(ChronoUnit.DAYS),
                p.ext().getMeasurementAge(ChronoUnit.DAYS),
                p.ext().getNumOfObservationsFiltered(),
                p.ext().getNumOfObservations(),
                p.ext().firstIsZero(),
                p.ext().getObservationsAllRaw().stream().filter(obs -> obs.isReplacementMeasurement()).count(),
                AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p),
                AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p),
                p.ext().getAlarmPercentString(p.ext()),
                p.ext().getAlarmLevelAge(),
                p.ext().deltaRolling().getDelta1d2d(3),
                p.ext().deltaZero().getDelta1d2d(3),
                p.ext().deltaFirst().getDelta1d2d(3)
        );
        propertyMap.putAll(populateMeas(p, measParams));
//******************************************************************************
        var dateParams = new BPropertiesBuilder.DateParams(
                p.ext().getObservationRawFirstDate(),
                p.ext().getObservationFilteredFirstDate(),
                p.ext().getObservationRawLastDate(),
                p.ext().getObservationFilteredLastDate(),
                p.ext().getObservationRawNextDate()
        );
        propertyMap.putAll(populateDates(p, dateParams));
//******************************************************************************

//TODO Replace with trend based analysis
//
//        var category = "Analys (VARNING)";
//        try {
//            var trend = p.ext().getHeightDirectionTrendDaysMeas();
//            propertyMap.put(getCatKeyNum(category, "Trend (dagar::antal)"), "%s::%d::%d".formatted(trend[0], trend[1], trend[2]));
//
//        } catch (NullPointerException e) {
//        }
//        var speed = p.ext().getSpeed();
//        var ageIndicator = p.ext().getMeasurementAge(ChronoUnit.DAYS) > 365 ? "*" : "";
//        var speedString = "%.1f mm/%s (%.1f)%s".formatted(speed[0] * 1000.0, Dict.Time.YEAR.toLower(), speed[1], ageIndicator);
//
//        propertyMap.put(getCatKeyNum(category, Dict.SPEED.toString()), speedString);
//
//        var limitValuePredictor = p.ext().limitValuePredictor();
//        if (limitValuePredictor.getRemainingUntilLimit() != null) {
//            propertyMap.put(getCatKeyNum(category, Dict.REMAINING.toString()), StringHelper.round(limitValuePredictor.getRemainingUntilLimit() * 1000, 1, "", " mm", false));
//            var limitDate = limitValuePredictor.getExtrapolatedLimitDate();
//            if (!StringUtils.equalsAny(limitDate, "-", "E")) {
//                limitDate = "%s (%d)".formatted(limitDate, limitValuePredictor.getExtrapolatedLimitDaysFromNow());
//            }
//            propertyMap.put(getCatKeyNum(category, Dict.Time.END_DATE.toString()), limitDate);
//            var direction = limitValuePredictor.isRisingByTrend() ? Dict.INCREASEING.toString() : Dict.DECREASING.toString();
//            propertyMap.put(getCatKeyNum(category, Dict.Geometry.DIRECTION.toString()), direction);
//        }
//******************************************************************************
        propertyMap.putAll(populateDatabase(p));

        return propertyMap;
    }

}

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

import java.util.LinkedHashMap;
import java.util.Set;
import org.mapton.butterfly_core.api.BPropertiesBuilder;
import org.mapton.butterfly_format.types.BMeasurementMode;
import org.mapton.butterfly_format.types.geo.BGeoPreDrillPoint;

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
        var basicExclusions = Set.of(
                ExcludeBasic.COMMENT,
                ExcludeBasic.MEAS_MODE,
                ExcludeBasic.TAG
        );
        var basicParams = new BPropertiesBuilder.BasicParams();
        propertyMap.putAll(populateBasics(p, basicParams, basicExclusions));
//******************************************************************************
        propertyMap.put("DJUP", p.getDepth());
        propertyMap.put("DIAMETER", p.getDiameter());

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

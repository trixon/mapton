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
package org.mapton.butterfly_geo_extensometer;

import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.BPropertiesBuilder;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public class ExtensoPropertiesBuilder extends BPropertiesBuilder<BGeoExtensometer> {

    @Override
    public Object build(BGeoExtensometer p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
//******************************************************************************
        var basicParams = new BasicParams();
        propertyMap.putAll(populateBasics(p, basicParams));
//******************************************************************************
        var dateParams = new DateParams(
                p.ext().getObservationRawFirstDate(),
                p.ext().getObservationFilteredFirstDate(),
                p.ext().getObservationRawLastDate(),
                p.ext().getObservationFilteredLastDate(),
                p.ext().getObservationRawNextDate()
        );
        propertyMap.putAll(populateDates(p, dateParams));
//******************************************************************************
//        Double azimuth = null;
//        try {
//            var o = p.ext().getObservationsTimeFiltered().getLast();
//            azimuth = o.ext().getBearing();
//        } catch (Exception e) {
//        }
//        var cat = "CUSTOM";
//        propertyMap.put(getCatKeyNum(cat, Dict.BEARING.toString()), StringHelper.round(p.getAzimuth(), 0));
//        var lastObservation = p.ext().getObservationFilteredLast();
//
//        if (lastObservation != null) {
//            for (var o : lastObservation.getObservationItems()) {
//                var azimuth = Angle.normalizedDegrees(o.getAzimuth() + p.getAzimuth());
//                if (azimuth < 0) {
//                    azimuth += 360;
//                }
//                var value = "%smm :: %.0f°".formatted(StringHelper.round(o.getDistance() * 1000, 1), azimuth);
//                propertyMap.put(getCatKeyNum(cat, "%.1f".formatted(o.getDown())), value);
//            }
//        }
        var dayUntilNext = p.getPoints().stream()
                .mapToLong(point -> point.ext().getMeasurementUntilNext(ChronoUnit.DAYS))
                .min().getAsLong();

        var age = p.getPoints().stream()
                .mapToLong(point -> point.ext().getMeasurementAge(ChronoUnit.DAYS))
                .max().getAsLong();

        var numOfFilteredObs = p.getPoints().stream()
                .mapToInt(point -> point.ext().getNumOfObservationsFiltered())
                .sum();

        var numOfObs = p.getPoints().stream()
                .mapToInt(point -> point.ext().getNumOfObservations())
                .sum();

        var measParams = new MeasParams<BXyzPoint>(
                0.0,
                dayUntilNext,
                age,
                numOfFilteredObs,
                numOfObs,
                p.ext().firstIsZero(),
                p.ext().getObservationsAllRaw().stream().filter(obs -> obs.isReplacementMeasurement()).count(),
                NA,
                NA,
                NA,
                NA,
                NA,
                NA
        );

        var measMap = populateMeas(p, measParams);
        measMap.put(getCatKeyNum(CAT_MEAS, SDict.MEASUREMENTS_FIRST_IS_ZERO.toString()), NA);
        measMap.put(getCatKeyNum(CAT_MEAS, SDict.MEASUREMENTS_NUM_OF_REPLACEMENTS.toString()), NA);

        propertyMap.putAll(measMap);
//******************************************************************************
        var cat1 = "Värden";
        propertyMap.put(getCatKey(cat1, "Referenspunkt"), p.getReferencePointName());
        for (var point : p.getPoints()) {
            var d = point.ext().getDelta();
            var s = "%.2f (%.1f, %.1f, %.1f)".formatted(d,
                    point.getLimit1() * 1000, point.getLimit2() * 1000, point.getLimit3() * 1000);
            var depth = "-%s".formatted(StringUtils.substringAfter(point.getName(), "-"));
            propertyMap.put(getCatKey(cat1, depth), s);
        }
//******************************************************************************
        propertyMap.putAll(populateDatabase(p));

        removeByValues(propertyMap, NA, " :: ");

        return propertyMap;
    }

}

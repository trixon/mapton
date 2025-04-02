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
package org.mapton.butterfly_acoustic.vibration;

import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import static org.mapton.api.ui.forms.PropertiesBuilder.NA;
import org.mapton.butterfly_core.api.BPropertiesBuilder;
import static org.mapton.butterfly_core.api.BPropertiesBuilder.CAT_MEAS;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationPoint;
import org.openide.util.NbBundle;

/**
 *
 * @author Patrik Karlström
 */
public class VibrationPropertiesBuilder extends BPropertiesBuilder<BAcousticVibrationPoint> {

    private final ResourceBundle mBundle = NbBundle.getBundle(VibrationPropertiesBuilder.class);

    @Override
    public Object build(BAcousticVibrationPoint p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
//******************************************************************************
        var basicParams = new BPropertiesBuilder.BasicParams();
        propertyMap.putAll(populateBasics(p, basicParams));
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

        var measParams = new BPropertiesBuilder.MeasParams<BXyzPoint>(
                0.0,
                -1L,
                p.ext().getMeasurementAge(ChronoUnit.DAYS),
                p.ext().getNumOfObservationsFiltered(),
                p.ext().getNumOfObservations(),
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
        measMap.put(getCatKeyNum(CAT_MEAS, "FIRST_IS_ZERO"), NA);
        measMap.put(getCatKeyNum(CAT_MEAS, "NUM_OF_REPLACEMENTS"), NA);

        propertyMap.putAll(measMap);
//******************************************************************************
        var cat1 = "Värden";
//        for (var point : p.getPoints()) {
//            var d = point.ext().getDelta();
//            var s = "%.2f (%.1f, %.1f, %.1f)".formatted(d,
//                    point.getLimit1() * 1000, point.getLimit2() * 1000, point.getLimit3() * 1000);
//            var depth = "-%s".formatted(StringUtils.substringAfter(point.getName(), "-"));
//            propertyMap.put(getCatKey(cat1, depth), s);
//        }
//******************************************************************************
        propertyMap.putAll(populateDatabase(p));

        removeByValues(propertyMap, NA, " :: ");

        return propertyMap;
    }

}

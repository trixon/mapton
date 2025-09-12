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
package org.mapton.butterfly_geo.inclinometer;

import gov.nasa.worldwind.geom.Angle;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import org.mapton.butterfly_core.api.AlarmHelper;
import org.mapton.butterfly_core.api.BPropertiesBuilder;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class InclinoPropertiesBuilder extends BPropertiesBuilder<BGeoInclinometerPoint> {

    @Override
    public Object build(BGeoInclinometerPoint p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
//******************************************************************************
        var basicParams = new BPropertiesBuilder.BasicParams();
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
        var cat = "CUSTOM";
        propertyMap.put(getCatKeyNum(cat, Dict.BEARING.toString()), StringHelper.round(p.getAzimuth(), 0));
        var lastObservation = p.ext().getObservationFilteredLast();

        if (lastObservation != null) {
            for (var o : lastObservation.getObservationItems()) {
                var azimuth = Angle.normalizedDegrees(o.getAzimuth() + p.getAzimuth());
                if (azimuth < 0) {
                    azimuth += 360;
                }
                var value = "%smm :: %.0f°".formatted(StringHelper.round(o.getDistance() * 1000, 1), azimuth);
                propertyMap.put(getCatKeyNum(cat, "%.1f".formatted(o.getDown())), value);
            }
        }

        var measParams = new MeasParams<BXyzPoint>(
                0.0,
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
                p.ext().deltaRolling().getDelta(3),
                p.ext().deltaZero().getDelta(3),
                p.ext().deltaFirst().getDelta(3)
        );
        propertyMap.putAll(populateMeas(p, measParams));

//******************************************************************************
//******************************************************************************
//******************************************************************************
        propertyMap.putAll(populateDatabase(p));

        return propertyMap;
    }

}

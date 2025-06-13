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
package org.mapton.butterfly_structural.tilt;

import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import org.mapton.butterfly_core.api.AlarmHelper;
import org.mapton.butterfly_core.api.BPropertiesBuilder;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;

/**
 *
 * @author Patrik Karlström
 */
public class TiltPropertiesBuilder extends BPropertiesBuilder<BStructuralTiltPoint> {

    @Override
    public Object build(BStructuralTiltPoint p) {
        if (p == null) {
            return p;
        }

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
        var measParams = new MeasParams<BXyzPoint>(
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
                p.ext().deltaRolling().getDelta(3),
                p.ext().deltaZero().getDelta(3)
        );
        propertyMap.putAll(populateMeas(p, measParams));
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
        propertyMap.putAll(populateDatabase(p));

        return propertyMap;
    }
}

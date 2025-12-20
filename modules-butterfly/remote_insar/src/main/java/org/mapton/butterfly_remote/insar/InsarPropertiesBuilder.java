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
package org.mapton.butterfly_remote.insar;

import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Set;
import org.mapton.butterfly_core.api.AlarmHelper;
import org.mapton.butterfly_core.api.BPropertiesBuilder;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class InsarPropertiesBuilder extends BPropertiesBuilder<BRemoteInsarPoint> {

    @Override
    public Object build(BRemoteInsarPoint p) {
        if (p == null) {
            return p;
        }

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
        propertyMap.putAll(populate(p));
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
                p.ext().deltaRolling().getDelta(3),
                p.ext().deltaZero().getDelta(3),
                p.ext().deltaFirst().getDelta(3)
        );
        var measExclusions = Set.of(
                ExcludeMeas.PLANE,
                ExcludeMeas.FREQ,
                ExcludeMeas.FREQ_CONDITION,
                ExcludeMeas.MEAS_FIRST_ZERO,
                ExcludeMeas.MEAS_REPLACEMENT,
                ExcludeMeas.BEARING,
                ExcludeMeas.SPARSE,
                ExcludeMeas.ROLLING_FORMULA,
                ExcludeMeas.ROLLING_VALUE
        );

        propertyMap.putAll(populateMeas(p, measParams, measExclusions));
//******************************************************************************
        var dateExclusions = Set.of(
                ExcludeDate.VALID
        );
        var dateParams = new BPropertiesBuilder.DateParams(
                p.ext().getObservationRawFirstDate(),
                p.ext().getObservationFilteredFirstDate(),
                p.ext().getObservationRawLastDate(),
                p.ext().getObservationFilteredLastDate(),
                p.ext().getObservationRawNextDate()
        );
        propertyMap.putAll(populateDates(p, dateParams, dateExclusions));
//******************************************************************************
        var databaseExclusions = Set.of(
                ExcludeDatabase.CHANGED,
                ExcludeDatabase.CREATED
        );
        propertyMap.putAll(populateDatabase(p, databaseExclusions));

        return propertyMap;
    }

    public LinkedHashMap<String, Object> populate(BRemoteInsarPoint p) {
        var map = new LinkedHashMap<String, Object>();
        var category = "InSAR";
        map.put(getCatKeyNum(category, "ChangeDetected"), p.getChangeDetected());
        map.put(getCatKeyNum(category, "Acceleration"), p.getAcceleration());
        map.put(getCatKeyNum(category, "CumulativeDisplacement"), p.getCumulativeDisplacement());
        map.put(getCatKeyNum(category, "EffArea"), p.getEffArea());
        map.put(getCatKeyNum(category, "SeasonAmp"), p.getSeasonAmp());
        map.put(getCatKeyNum(category, "Velocity"), p.getVelocity());
        map.put(getCatKeyNum(category, "Velocity3m"), p.getVelocity3m());
        map.put(getCatKeyNum(category, "Velocity6m"), p.getVelocity6m());
        map.put(getCatKeyNum(category, "StDef"), p.getStDef());
        map.put(getCatKeyNum(category, "StDevAcceleration"), p.getStDevAcceleration());
        map.put(getCatKeyNum(category, "StDevHeight"), p.getStDevHeight());
        map.put(getCatKeyNum(category, "StDevSeasonAmp"), p.getStDevSeasonAmp());
        map.put(getCatKeyNum(category, "StDevVelocity"), p.getStDevVelocity());
        map.put(getCatKeyNum(category, "StDevVelocity3m"), p.getStDevVelocity3m());
        map.put(getCatKeyNum(category, "StDevVelocity6m"), p.getStDevVelocity6m());

        return map;
    }
}

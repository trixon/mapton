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
package org.mapton.butterfly_rock_convergence;

import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.Strings;
import org.mapton.butterfly_core.api.AlarmHelper;
import org.mapton.butterfly_core.api.BPropertiesBuilder;
import org.mapton.butterfly_core.api.ButterflyManager;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.rock.BRockConvergence;
import org.mapton.butterfly_format.types.rock.BRockConvergenceObservation;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergencePropertiesBuilder extends BPropertiesBuilder<BRockConvergence> {

    @Override
    public Object build(BRockConvergence p) {
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
                null,
                p.ext().getAlarmLevelAge(),
                p.ext().deltaRolling().getDelta(3),
                p.ext().deltaZero().getDelta(3),
                p.ext().deltaFirst().getDelta(3)
        );
        var measMap = populateMeas(p, measParams);
        removeByIndices(measMap, 3, 4, 6, 9, 10, 11, 12, 13);
        propertyMap.putAll(measMap);
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
        propertyMap.putAll(populateDatabase(p));

        removeByKeyContains(propertyMap,
                Dict.Geometry.PLANE.toString(),
                SDict.ROLLING.toString(),
                Dict.FIRST.toString(),
                Dict.REFERENCE.toString()
        );

        propertyMap.put("Antal punkter", p.ext().getControlPointsWithoutAnchor().size());
        propertyMap.put("Antal linjer", p.ext().getPairs().size());
        propertyMap.put("Punkter", Strings.CI.remove(p.getRef(), p.getName()));

        var function = BRockConvergenceObservation.FUNCTION_3D;

        p.ext().getPairsOrderedByDeltaDesc(function, 5)
                .forEachOrdered(pair -> {
                    var observations = pair.ext().getObservationsTimeFiltered();
                    if (observations.size() > 1) {
                        var o0 = observations.get(observations.size() - 2);
                        var o1 = observations.getLast();
                        var delta0 = pair.ext().getDelta(function, o0);
                        var delta1 = pair.ext().getDelta(function, o1);
                        var days = ChronoUnit.DAYS.between(o0.getDate(), o1.getDate());
                        var blasts = ButterflyManager.getInstance().util().getBlasts(p, 40, o0.getDate(), o1.getDate()).size();
                        propertyMap.put(pair.getSimpleName(), "%+.1f (%+.1f @ %dd/%ds)".formatted(
                                pair.ext().getDelta(function),
                                delta1 - delta0,
                                days,
                                blasts
                        ));
                    } else {
                        propertyMap.put(pair.getSimpleName(), "%+.1f".formatted(pair.ext().getDelta(function)));
                    }
                });

        return propertyMap;
    }
}

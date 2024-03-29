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
package org.mapton.butterfly_topo;

import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_alarm.api.AlarmHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoPropertiesBuilder extends PropertiesBuilder<BTopoControlPoint> {

    @Override
    public Object build(BTopoControlPoint p) {
        if (p == null) {
            return p;
        }
        //TODO Add some gauges at the top: Alarm, mätbehov...
        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();
        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        propertyMap.put(getCatKey(cat1,
                StringHelper.join(SEPARATOR, "", Dict.STATUS.toString(), SDict.DIMENSION.toString())),
                StringHelper.join(SEPARATOR, "", p.getStatus(), p.getDimension().getName()));
        propertyMap.put(getCatKey(cat1,
                StringHelper.join(SEPARATOR, "", Dict.GROUP.toString(), Dict.CATEGORY.toString())),
                StringHelper.join(SEPARATOR, "", p.getGroup(), p.getCategory()));
        propertyMap.put(getCatKey(cat1, SDict.OPERATOR.toString()), p.getOperator());
        propertyMap.put(getCatKey(cat1, Dict.COMMENT.toString()), p.getComment());
        propertyMap.put(getCatKey(cat1, SDict.ALARM.toString()), StringHelper.join(SEPARATOR, "", p.getNameOfAlarmHeight(), p.getNameOfAlarmPlane()));
        propertyMap.put(getCatKey(cat1, Dict.Geometry.HEIGHT.toString()), AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p));
        propertyMap.put(getCatKey(cat1, Dict.Geometry.PLANE.toString()), AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p));
        var measurements = "%d / %d    (%d - %d)".formatted(
                p.ext().getNumOfObservationsFiltered(),
                p.ext().getNumOfObservations(),
                p.ext().getObservationsAllRaw().stream().filter(obs -> obs.isZeroMeasurement()).count(),
                p.ext().getObservationsAllRaw().stream().filter(obs -> obs.isReplacementMeasurement()).count()
        );
        String validFromTo = null;
        if (ObjectUtils.anyNotNull(p.getDateValidFrom(), p.getDateValidTo())) {
            var fromDat = Objects.toString(DateHelper.toDateString(p.getDateValidFrom()), "1970-01-01");
            var toDat = Objects.toString(DateHelper.toDateString(p.getDateValidTo()), "2099-12-31");
            validFromTo = StringHelper.joinNonNulls(" // ", fromDat, toDat);
        }
        propertyMap.put(getCatKey(cat1, SDict.MEASUREMENTS.toString()), measurements);
        propertyMap.put(getCatKey(cat1, "%s %s - %s".formatted(Dict.VALID.toString(), Dict.FROM.toLower(), Dict.TO.toLower())), validFromTo);

        var firstRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawFirstDate()), "-");
        var firstFiltered = Objects.toString(DateHelper.toDateString(p.ext().getObservationFilteredFirstDate()), "-");
        var lastRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "-");
        var lastFiltered = Objects.toString(DateHelper.toDateString(p.ext().getObservationFilteredLastDate()), "-");
        var nextRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawNextDate()), "-");

        propertyMap.put(getCatKey(cat1, SDict.FREQUENCY.toString()), p.getFrequency());
        var need = p.getFrequency() == 0 ? "-" : Long.toString(p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));
        propertyMap.put(getCatKey(cat1, Dict.NEED.toString()), need);
        propertyMap.put(getCatKey(cat1, Dict.AGE.toString()), p.ext().getMeasurementAge(ChronoUnit.DAYS));
        propertyMap.put(getCatKey(cat1, "%s, %s".formatted(Dict.AGE.toString(), SDict.ALARM_LEVEL.toLower())), p.ext().getAlarmLevelAge());
        propertyMap.put(getCatKey(cat1, Dict.LATEST.toString()),
                "%s (%s)".formatted(lastRaw, lastFiltered)
        );
        propertyMap.put(getCatKey(cat1, Dict.NEXT.toString()), nextRaw);
        propertyMap.put(getCatKey(cat1, Dict.REFERENCE.toString()),
                "%s (%s)".formatted(
                        Objects.toString(DateHelper.toDateString(p.getDateZero()), "-"),
                        Objects.toString(DateHelper.toDateString(p.getDateRolling()), "-"))
        );
        propertyMap.put(getCatKey(cat1, Dict.FIRST.toString()),
                "%s (%s)".formatted(firstRaw, firstFiltered)
        );
        var delta = "Δ ";
        propertyMap.put(getCatKey(cat1, delta + SDict.ROLLING.toString()), p.ext().deltaRolling().getDelta(3));
        propertyMap.put(getCatKey(cat1, delta + Dict.REFERENCE.toString()), p.ext().deltaZero().getDelta(3));
        propertyMap.put(getCatKey(cat1, "N"), StringHelper.round(p.getZeroY(), 3));
        propertyMap.put(getCatKey(cat1, "E"), StringHelper.round(p.getZeroX(), 3));
        propertyMap.put(getCatKey(cat1, "H"), StringHelper.round(p.getZeroZ(), 3));

        Double bearing = null;
        try {
            var o = p.ext().getObservationsTimeFiltered().getLast();
            bearing = o.ext().getBearing();
        } catch (Exception e) {
        }

        propertyMap.put(getCatKey(cat1, Dict.BEARING.toString()), StringHelper.round(bearing, 0));

        return propertyMap;
    }

}

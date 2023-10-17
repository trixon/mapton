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

import java.util.LinkedHashMap;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
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
                StringHelper.join(SEPARATOR, "", SDict.DIMENSION.toString(), Dict.STATUS.toString())),
                StringHelper.join(SEPARATOR, "", String.valueOf(p.getDimension().ordinal() + 1), p.getStatus()));
        propertyMap.put(getCatKey(cat1,
                StringHelper.join(SEPARATOR, "", Dict.GROUP.toString(), Dict.CATEGORY.toString())),
                StringHelper.join(SEPARATOR, "", p.getGroup(), p.getCategory()));
        propertyMap.put(getCatKey(cat1,
                StringHelper.join(SEPARATOR, "", SDict.ALARM.toString() + " " + Dict.Geometry.HEIGHT.toString(), Dict.Geometry.PLANE.toString())),
                StringHelper.join(SEPARATOR, "", p.getNameOfAlarmHeight(), p.getNameOfAlarmPlane()));
        propertyMap.put(getCatKey(cat1, SDict.OPERATOR.toString()), p.getOperator());
        propertyMap.put(getCatKey(cat1, Dict.TAG.toString()), p.getTag());
        propertyMap.put(getCatKey(cat1, Dict.COMMENT.toString()), p.getComment());
        propertyMap.put(getCatKey(cat1, SDict.FREQUENCY.toString()), p.getFrequency());// frekevens/sedan/kvar  även label by
        var measurements = "%d / %d    (%d - %d)".formatted(
                p.ext().getNumOfObservationsTimeFiltered(),
                p.ext().getNumOfObservations(),
                p.ext().getObservationsRaw().stream().filter(obs -> obs.isZeroMeasurement()).count(),
                p.ext().getObservationsRaw().stream().filter(obs -> obs.isReplacementMeasurement()).count()
        );
        var delta = ", Δ";
        propertyMap.put(getCatKey(cat1, SDict.MEASUREMENTS.toString()), measurements);
        propertyMap.put(getCatKey(cat1, SDict.LATEST.toString()), DateHelper.toDateString(p.getDateLatest()));
        propertyMap.put(getCatKey(cat1, SDict.ROLLING.toString()), DateHelper.toDateString(p.getDateRolling()));
        propertyMap.put(getCatKey(cat1, SDict.ROLLING.toString()), DateHelper.toDateString(p.getDateRolling()));
        propertyMap.put(getCatKey(cat1, Dict.FIRST.toString()), DateHelper.toDateString(p.getDateZero()));
        propertyMap.put(getCatKey(cat1, SDict.ROLLING.toString() + delta), p.ext().deltaRolling().getDelta(3));
        propertyMap.put(getCatKey(cat1, Dict.FIRST.toString() + delta), p.ext().deltaZero().getDelta(3));
        propertyMap.put(getCatKey(cat1, "N"), StringHelper.round(p.getZeroY(), 3));
        propertyMap.put(getCatKey(cat1, "E"), StringHelper.round(p.getZeroX(), 3));
        propertyMap.put(getCatKey(cat1, "H"), StringHelper.round(p.getZeroZ(), 3));

        /* TODO
        dateValidFrom=
        dateValidTo=

        offsetX=
        offsetY=
        offsetZ=
        origin=
        zeroX=
        zeroY=
        zeroZ=

         */
        return propertyMap;
    }

}

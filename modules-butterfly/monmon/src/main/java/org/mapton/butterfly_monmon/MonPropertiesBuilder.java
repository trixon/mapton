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
package org.mapton.butterfly_monmon;

import java.util.LinkedHashMap;
import java.util.Objects;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.monmon.BMonmon;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class MonPropertiesBuilder extends PropertiesBuilder<BMonmon> {

    @Override
    public Object build(BMonmon mon) {
        if (mon == null) {
            return mon;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();

        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), mon.getName());
        propertyMap.put(getCatKey(cat1, "Station"), mon.getStationName());
        var firstRaw = Objects.toString(DateHelper.toDateString(mon.getControlPoint().ext().getObservationRawFirstDate()), "");
        var lastRaw = Objects.toString(DateHelper.toDateString(mon.getControlPoint().ext().getObservationRawLastDate()), "");
        propertyMap.put(getCatKey(cat1, Dict.DATE.toString()), "%s — %s".formatted(firstRaw, lastRaw));
        propertyMap.put(getCatKey(cat1, "Mätningar/dag"), mon.getMeasPerDay());
        propertyMap.put(getCatKey(cat1, "Idag"), mon.getString(1));
        propertyMap.put(getCatKey(cat1, "Senaste veckan"), mon.getString(7));
        propertyMap.put(getCatKey(cat1, "Senaste två veckorna"), mon.getString(14));

        return propertyMap;
    }

}

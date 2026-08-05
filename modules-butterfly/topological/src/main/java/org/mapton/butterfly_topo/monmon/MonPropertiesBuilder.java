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
package org.mapton.butterfly_topo.monmon;

import java.util.LinkedHashMap;
import java.util.Objects;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.topo.BTopoMonmon;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MonPropertiesBuilder extends PropertiesBuilder<BTopoMonmon> {

    @Override
    public Object build(BTopoMonmon p) {
        if (p == null) {
            return p;
        }
        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();

        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        propertyMap.put(getCatKey(cat1, "Station"), p.getStationName());
        var firstRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawFirstDate()), "");
        var lastRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
        propertyMap.put(getCatKey(cat1, Dict.DATE.toString()), "%s — %s".formatted(firstRaw, lastRaw));
        propertyMap.put(getCatKey(cat1, "Mätningar/dag"), p.getMeasPerDay());
        propertyMap.put(getCatKey(cat1, "Senaste dygnet"), p.getString(1));
        propertyMap.put(getCatKey(cat1, "Senaste veckan"), p.getString(7));
        propertyMap.put(getCatKey(cat1, "Senaste två veckorna"), p.getString(14));
        propertyMap.put(getCatKey(cat1, "Lutande längd"), MathHelper.convertDoubleToString(p.ext().getDelta3d(), 1));
        propertyMap.put(getCatKey(cat1, "Planavstånd"), MathHelper.convertDoubleToString(p.ext().getDelta2d(), 1));
        propertyMap.put(getCatKey(cat1, "Höjdavstånd"), MathHelper.convertDoubleToString(p.ext().getDelta1d(), 1));

        return propertyMap;
    }

}

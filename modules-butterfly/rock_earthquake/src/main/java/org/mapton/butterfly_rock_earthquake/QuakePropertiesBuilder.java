/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_rock_earthquake;

import java.util.LinkedHashMap;
import org.mapton.butterfly_core.api.BPropertiesBuilder;
import org.mapton.butterfly_format.types.rock.BRockEarthquake;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public class QuakePropertiesBuilder extends BPropertiesBuilder<BRockEarthquake> {

    @Override
    public Object build(BRockEarthquake p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();
        propertyMap.put(getCatKey(cat1, "Ext.id"), p.getExternalId());
        propertyMap.put(getCatKey(cat1, Dict.DATE.toString()), QuakeListCell.DATE_TIME_FORMATTER.format(p.getDateLatest()));
        propertyMap.put(getCatKey(cat1, Dict.PLACE.toString()), p.getName());
        propertyMap.put(getCatKey(cat1, "Magnitude"), "%.1f %s".formatted(p.getMag(), p.getMagType()));
        propertyMap.put(getCatKey(cat1, "Alert"), p.getClassification());
        propertyMap.put(getCatKey(cat1, SDict.ALARM.toString()), p.getAlarm1Id());
        propertyMap.put(getCatKey(cat1, Dict.CATEGORY.toString()), p.getCategory());
//        propertyMap.put(getCatKey(cat1, Dict.GROUP.toString()), p.getGroup());
        propertyMap.put(getCatKey(cat1, Dict.STATUS.toString()), p.getStatus());
        propertyMap.put(getCatKey(cat1, Dict.TAGS.toString()), p.getTag());
        propertyMap.put(getCatKey(cat1, Dict.ORIGIN.toString()), p.getOrigin());
        propertyMap.put(getCatKey(cat1, SDict.OPERATOR.toString()), p.getOperator());

        return propertyMap;
    }

}

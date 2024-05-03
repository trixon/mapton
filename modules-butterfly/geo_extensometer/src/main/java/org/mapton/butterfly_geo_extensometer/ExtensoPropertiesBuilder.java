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
package org.mapton.butterfly_geo_extensometer;

import java.util.LinkedHashMap;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class ExtensoPropertiesBuilder extends PropertiesBuilder<BGeoExtensometer> {

    @Override
    public Object build(BGeoExtensometer extenso) {
        if (extenso == null) {
            return extenso;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();

        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), extenso.getName());
        propertyMap.put(getCatKey(cat1, "LATEST"), extenso.getDateLatest());
        propertyMap.put(getCatKey(cat1, "POINTS"), extenso.getSensors());

        for (var point : extenso.getPoints()) {
            propertyMap.put(getCatKey(cat1, point.getName()), point.ext().getObservationsAllCalculated().size());
        }

        return propertyMap;
    }

}

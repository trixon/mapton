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
package org.mapton.butterfly_topo.tilt;

import java.util.LinkedHashMap;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.topo.BTopoTiltH;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public class TiltHPropertiesBuilder extends PropertiesBuilder<BTopoTiltH> {

    @Override
    public Object build(BTopoTiltH p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();
        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        propertyMap.put(getCatKey(cat1, Dict.DATE.toString()), "%s - %s".formatted(p.getDateFirst(), p.getDateLast()));
        propertyMap.put(getCatKey(cat1, Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower())), p.getCommonObservations().size());
        propertyMap.put(getCatKey(cat1, "Höjdavstånd"), p.getDistanceHeight());
        propertyMap.put(getCatKey(cat1, "Planavstånd"), p.getDistancePlane());
        propertyMap.put(getCatKey(cat1, "Delta"), p.getDelta());
        propertyMap.put(getCatKey(cat1, "Lutning"), p.getTilt());

        return propertyMap;
    }

}

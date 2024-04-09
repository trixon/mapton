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
package org.mapton.worldwind;

import java.util.LinkedHashMap;
import org.mapton.api.MDict;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.worldwind.api.MapStyle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class LayerMapStylePropertiesBuilder extends PropertiesBuilder<MapStyle> {

    @Override
    public Object build(MapStyle s) {
        if (s == null) {
            return s;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();
        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), s.getName());
        propertyMap.put(getCatKey(cat1, Dict.CATEGORY.toString()), s.getCategory());
        propertyMap.put(getCatKey(cat1, Dict.DESCRIPTION.toString()), s.getDescription());
        propertyMap.put(getCatKey(cat1, MDict.ORIGIN.toString()), s.getSuppliers());
        propertyMap.put(getCatKey(cat1, Dict.LAYERS.toString()), String.join(", ", s.getLayers()));
        propertyMap.put(getCatKey(cat1, "Id"), s.getId());

        return propertyMap;
    }

}

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
package org.mapton.butterfly_activities;

import java.util.LinkedHashMap;
import java.util.Objects;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.BAreaActivity;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class ActPropertiesBuilder extends PropertiesBuilder<BAreaActivity> {

    @Override
    public Object build(BAreaActivity a) {
        if (a == null) {
            return a;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();

        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), a.getName());
        propertyMap.put(getCatKey(cat1, Dict.DESCRIPTION.toString()), a.getDescription());
        var datFrom = Objects.toString(DateHelper.toDateString(a.getDatFrom()), "-");
        propertyMap.put(getCatKey(cat1, Dict.FROM.toString()), datFrom);
        var datTo = Objects.toString(DateHelper.toDateString(a.getDatTo()), "-");
        propertyMap.put(getCatKey(cat1, Dict.TO.toString()), datTo);
        propertyMap.put(getCatKey(cat1, Dict.STATUS.toString()), ActHelper.getStatusAsString(a.getStatus()));

        return propertyMap;
    }

}

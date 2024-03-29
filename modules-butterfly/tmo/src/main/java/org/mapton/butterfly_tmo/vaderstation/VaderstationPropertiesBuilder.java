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
package org.mapton.butterfly_tmo.vaderstation;

import java.util.LinkedHashMap;
import java.util.Objects;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.tmo.BVaderstation;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class VaderstationPropertiesBuilder extends PropertiesBuilder<BVaderstation> {

    @Override
    public Object build(BVaderstation p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();
        var date = Objects.toString(DateHelper.toDateString(p.getInstallationsdatum()), "-");

        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        propertyMap.put(getCatKey(cat1, Dict.GROUP.toString()), p.getGroup());
        propertyMap.put(getCatKey(cat1, Dict.COMMENT.toString()), p.getComment());
        propertyMap.put(getCatKey(cat1, Dict.DATE.toString()), date);
//        propertyMap.put(getCatKey(cat1, Dict.AGE.toString()), p.ext().getAge(ChronoUnit.DAYS));
//        propertyMap.put(getCatKey(cat1, "Z"), MathHelper.convertDoubleToString(p.getZ(), 1));

        return propertyMap;
    }

}

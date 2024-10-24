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
package org.mapton.butterfly_acoustic.measuring_point;

import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.acoustic.BAcousticMeasuringPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MeasPointPropertiesBuilder extends PropertiesBuilder<BAcousticMeasuringPoint> {

    private final ResourceBundle mBundle = NbBundle.getBundle(MeasPointPropertiesBuilder.class);

    @Override
    public Object build(BAcousticMeasuringPoint p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();

        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        propertyMap.put(getCatKey(cat1, Dict.TYPE.toString()), p.getTypeOfWork());
        propertyMap.put(getCatKey(cat1, mBundle.getString("soilMaterial")), p.getSoilMaterial());
        propertyMap.put(getCatKey(cat1, "Adress"), p.getAddress());
        propertyMap.put(getCatKey(cat1, Dict.COMMENT.toString()), p.getComment());
        propertyMap.put(getCatKey(cat1, "Z"), MathHelper.convertDoubleToString(p.getZ(), 1));

        return propertyMap;
    }

}

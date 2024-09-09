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
package org.mapton.butterfly_structural.tilt;

import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public class TiltPropertiesBuilder extends PropertiesBuilder<BStructuralTiltPoint> {

    private final ResourceBundle mBundle = NbBundle.getBundle(TiltPropertiesBuilder.class);

    @Override
    public Object build(BStructuralTiltPoint p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();

        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        propertyMap.put(getCatKey(cat1, Dict.COMMENT.toString()), p.getComment());
        propertyMap.put(getCatKey(cat1, Dict.CATEGORY.toString()), p.getCategory());
        propertyMap.put(getCatKey(cat1, "date changed"), p.getDateChanged());
        propertyMap.put(getCatKey(cat1, "date created"), p.getDateCreated());
        propertyMap.put(getCatKey(cat1, "date latest"), p.getDateLatest());
        propertyMap.put(getCatKey(cat1, "date rolling"), p.getDateRolling());
        propertyMap.put(getCatKey(cat1, "date valid from"), p.getDateValidFrom());
        propertyMap.put(getCatKey(cat1, "date valid to"), p.getDateValidTo());
        propertyMap.put(getCatKey(cat1, "date zero"), p.getDateZero());
        propertyMap.put(getCatKey(cat1, SDict.FREQUENCY.toString()), p.getFrequency());
        propertyMap.put(getCatKey(cat1, Dict.GROUP.toString()), p.getGroup());
        propertyMap.put(getCatKey(cat1, SDict.OPERATOR.toString()), p.getOperator());
        propertyMap.put(getCatKey(cat1, Dict.ORIGIN.toString()), p.getOrigin());
        propertyMap.put(getCatKey(cat1, "r x"), p.getRollingX());
        propertyMap.put(getCatKey(cat1, "r y"), p.getRollingY());
        propertyMap.put(getCatKey(cat1, "r z"), p.getRollingZ());
        propertyMap.put(getCatKey(cat1, Dict.STATUS.toString()), p.getStatus());
        propertyMap.put(getCatKey(cat1, Dict.TAG.toString()), p.getTag());
        propertyMap.put(getCatKey(cat1, "zero x"), p.getZeroX());
        propertyMap.put(getCatKey(cat1, "zero x2"), p.getZeroX2());
        propertyMap.put(getCatKey(cat1, "zero y"), p.getZeroY());
        propertyMap.put(getCatKey(cat1, "zero y2"), p.getZeroY2());
        propertyMap.put(getCatKey(cat1, "zero z"), p.getZeroZ());
        propertyMap.put(getCatKey(cat1, "zero z2"), p.getZeroZ2());

        return propertyMap;
    }

}

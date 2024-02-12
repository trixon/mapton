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
package org.mapton.butterfly_topo.pair.vertical;

import java.util.LinkedHashMap;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.pair.horizontal.Pair1PropertiesBuilder;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class Pair3PropertiesBuilder extends Pair1PropertiesBuilder {

    @Override
    public Object build(BTopoGrade p) {
        if (p == null) {
            return p;
        }

        var propertyMap = (LinkedHashMap<String, Object>) super.build(p);
        var cat1 = Dict.BASIC.toString();
        propertyMap.put(getCatKey(cat1, "R"), null);
        propertyMap.put(getCatKey(cat1, "R Grader"), MathHelper.convertDoubleToString(p.ext().getDiff().getRAngleDeg(), 0));
        propertyMap.put(getCatKey(cat1, "R Gon"), MathHelper.convertDoubleToString(p.ext().getDiff().getRAngleGon(), 0));
        propertyMap.put(getCatKey(cat1, "R Radianer"), MathHelper.convertDoubleToString(p.ext().getDiff().getRAngleRad(), 4));
        propertyMap.put(getCatKey(cat1, "R Lutningskvot"), MathHelper.convertDoubleToString(p.ext().getDiff().getRQuota(), 6));
        propertyMap.put(getCatKey(cat1, "R Procent"), MathHelper.convertDoubleToString(p.ext().getDiff().getRPercentage(), 1));
        propertyMap.put(getCatKey(cat1, "R Promille"), MathHelper.convertDoubleToString(p.ext().getDiff().getRPerMille(), 1));

        return propertyMap;
    }

}

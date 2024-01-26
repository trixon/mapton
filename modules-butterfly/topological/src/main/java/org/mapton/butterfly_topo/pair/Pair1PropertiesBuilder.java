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
package org.mapton.butterfly_topo.pair;

import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.topo.BTopoPointPair;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public class Pair1PropertiesBuilder extends PropertiesBuilder<BTopoPointPair> {

    private final ResourceBundle mBundle = NbBundle.getBundle(Pair1PropertiesBuilder.class);

    @Override
    public Object build(BTopoPointPair p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();
        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        propertyMap.put(getCatKey(cat1, Dict.DATE.toString()), "%s - %s".formatted(p.getDateFirst(), p.getDateLast()));
        propertyMap.put(getCatKey(cat1, Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower())), p.getCommonObservations().size());
        propertyMap.put(getCatKey(cat1, mBundle.getString("deltaH")), MathHelper.convertDoubleToString(p.getDistanceHeight(), 2));
        propertyMap.put(getCatKey(cat1, mBundle.getString("deltaR")), MathHelper.convertDoubleToString(p.getDistancePlane(), 2));
        propertyMap.put(getCatKey(cat1, mBundle.getString("dabbaH")), "%.1f mm".formatted(p.getPartialDiffZ() * 1000));
        propertyMap.put(getCatKey(cat1, mBundle.getString("dabbaR")), "%.1f mm".formatted(p.getPartialDiffR() * 1000));
        propertyMap.put(getCatKey(cat1, "H Grader"), MathHelper.convertDoubleToString(p.getZAngleDeg(), 0));
        propertyMap.put(getCatKey(cat1, "H Gon"), MathHelper.convertDoubleToString(p.getZAngleGon(), 0));
        propertyMap.put(getCatKey(cat1, "H Radianer"), MathHelper.convertDoubleToString(p.getZAngleRad(), 4));
        propertyMap.put(getCatKey(cat1, "H Lutningskvot"), MathHelper.convertDoubleToString(p.getZQuota(), 6));
        propertyMap.put(getCatKey(cat1, "H Procent"), MathHelper.convertDoubleToString(p.getZPercentage(), 1));
        propertyMap.put(getCatKey(cat1, "H Promille"), MathHelper.convertDoubleToString(p.getZPerMille(), 1));

        return propertyMap;
    }

}

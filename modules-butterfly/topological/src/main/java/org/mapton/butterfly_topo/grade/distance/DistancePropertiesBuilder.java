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
package org.mapton.butterfly_topo.grade.distance;

import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.grade.GradeManagerBase;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public class DistancePropertiesBuilder extends PropertiesBuilder<BTopoGrade> {

    protected final ResourceBundle mBundle = NbBundle.getBundle(GradeManagerBase.class);

    @Override
    public Object build(BTopoGrade p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();
        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        propertyMap.put(getCatKey(cat1, Dict.DATE.toString()), p.getPeriod());
        propertyMap.put(getCatKey(cat1, Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower())), p.ext().getNumOfCommonObservations());
        propertyMap.put(getCatKey(cat1, Dict.NUM_OF_S.toString().formatted(Dict.Time.DAYS.toLower())), p.ext().getNumOfCommonDays());
        propertyMap.put(getCatKey(cat1, Dict.AGE.toString()), p.ext().getNumOfDaysSinceLast());
        propertyMap.put(getCatKey(cat1, mBundle.getString("filterDeltaH")), MathHelper.convertDoubleToString(p.getDistanceHeight(), 2));
        propertyMap.put(getCatKey(cat1, mBundle.getString("filterDeltaR")), MathHelper.convertDoubleToString(p.getDistancePlane(), 2));
        propertyMap.put(getCatKey(cat1, mBundle.getString("filterDabbaH")), "%.1f".formatted(p.ext().getDiff().getPartialDiffZ() * 1000));
        propertyMap.put(getCatKey(cat1, mBundle.getString("filterDabbaR")), "%.1f".formatted(p.ext().getDiff().getPartialDiffR() * 1000));
//        propertyMap.put(getCatKey(cat1, mBundle.getString("gradeHDeg")), MathHelper.convertDoubleToString(p.ext().getDiff().getZAngleDeg(), 0));
//        propertyMap.put(getCatKey(cat1, mBundle.getString("gradeHGon")), MathHelper.convertDoubleToString(p.ext().getDiff().getZAngleGon(), 0));
//        propertyMap.put(getCatKey(cat1, mBundle.getString("gradeHRad")), MathHelper.convertDoubleToString(p.ext().getDiff().getZAngleRad(), 4));
        propertyMap.put(getCatKey(cat1, mBundle.getString("gradeHPerMille")), MathHelper.convertDoubleToString(p.ext().getDiff().getZPerMille(), 1));

        var component = p.getAxis() == BAxis.HORIZONTAL ? BComponent.HEIGHT : BComponent.PLANE;
        var alarm = p.getP1().ext().getAlarm(component);
        try {
            propertyMap.put(getCatKey(cat1, "%s".formatted(SDict.ALARM.toString())), alarm.getId());
            propertyMap.put(getCatKey(cat1, "%s %d".formatted(SDict.ALARM.toString(), 1)), alarm.getRatio1s());
            propertyMap.put(getCatKey(cat1, "%s %d".formatted(SDict.ALARM.toString(), 2)), alarm.getRatio2s());

        } catch (Exception e) {
            //nvm
        }

        return propertyMap;
    }

}

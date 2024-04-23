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
package org.mapton.butterfly_topo_convergence.pair;

import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Objects;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergencePairPropertiesBuilder extends PropertiesBuilder<BTopoConvergencePair> {

    @Override
    public Object build(BTopoConvergencePair p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();
        var firstDate = Objects.toString(DateHelper.toDateString(p.ext().getFirstDate()), "-");
        var lastDate = Objects.toString(DateHelper.toDateString(p.ext().getLastDate()), "-");

        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        propertyMap.put(getCatKey(cat1, Dict.GROUP.toString()), p.getConvergenceGroup().getName());
        propertyMap.put(getCatKey(cat1, "P1"), p.getP1().getName());
        propertyMap.put(getCatKey(cat1, "P2"), p.getP2().getName());
        propertyMap.put(getCatKey(cat1, Dict.FIRST.toString()), firstDate);
        propertyMap.put(getCatKey(cat1, Dict.LATEST.toString()), lastDate);
        propertyMap.put(getCatKey(cat1, Dict.AGE.toString()), p.ext().getAge(ChronoUnit.DAYS));
        propertyMap.put(getCatKey(cat1, "Count"), p.getObservations().size());

        var desc2 = "ΔΔL=%.1fmm  ΔΔR=%.1fmm  ΔΔH=%.1fmm".formatted(
                p.getDeltaDistanceOverTime() * 1000,
                p.getDeltaROverTime() * 1000,
                p.getDeltaZOverTime() * 1000
        );

        var desc3 = "ΔL=%.3f  ΔR=%.3f  ΔH=%.3f  ".formatted(
                p.getDistance(),
                p.getDeltaR(),
                p.getDeltaZ()
        );
        propertyMap.put(getCatKey(cat1, "Delta 1"), desc2);
        propertyMap.put(getCatKey(cat1, "Delta 2"), desc3);

        return propertyMap;
    }

}

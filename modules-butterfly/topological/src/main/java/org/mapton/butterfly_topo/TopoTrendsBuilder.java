/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_topo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.mapton.butterfly_core.api.TrendHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import static org.mapton.butterfly_topo.api.TopoManager.KEY_TRENDS_H;
import static org.mapton.butterfly_topo.api.TopoManager.KEY_TRENDS_P;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class TopoTrendsBuilder extends BTrendsBuilder<BTopoControlPoint> {

    @Override
    public Object build(BTopoControlPoint p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();
        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        if (p.getDimension() != BDimension._2d) {
            populate(p, BComponent.HEIGHT, cat1, propertyMap);
        }
        if (p.getDimension() != BDimension._1d) {
            populate(p, BComponent.PLANE, cat1, propertyMap);
        }

        return propertyMap;
    }

    private LinkedHashMap<String, String> populate(HashMap<String, TrendHelper.Trend> map) {
        var resultMap = new LinkedHashMap<String, String>();
        var titleMap = Map.of(
                "1w", "1 vecka",
                "1m", "1 månad",
                "3m", "3 månader",
                "6m", "6 månader",
                "z", "nollmätning",
                "f", "första"
        );

        var now = LocalDateTime.now();
        if (map != null) {
            var startMinute = new Minute(0, new Hour());
            for (var key : List.of("1w", "1m", "3m", "6m", "z", "f")) {
                var trend = map.get(key);
                if (trend != null && !trend.startMinute().getDay().equals(startMinute.getDay())) {
                    var val1 = trend.function().getValue(ChartHelper.convertToMinute(now.plusYears(1)).getFirstMillisecond());
                    var val2 = trend.function().getValue(ChartHelper.convertToMinute(now).getFirstMillisecond());
                    var speed = "%+.1f mm/år (%d)".formatted((val1 - val2) * 1000, trend.numOfMeas());
                    resultMap.put(titleMap.get(key), speed);
                    startMinute = trend.startMinute();
                }
            }
        }

        return resultMap;
    }

    private void populate(BTopoControlPoint p, BComponent component, String cat1, LinkedHashMap<String, Object> propertyMap) {
        var trendKey = component == BComponent.HEIGHT ? KEY_TRENDS_H : KEY_TRENDS_P;
        for (var entry : populate(p.getValue(trendKey)).entrySet()) {
            var key = entry.getKey();
            var val = entry.getValue();
            propertyMap.put(getCatKey(cat1, "%dd, %s".formatted(component.getDimension().getIndex(), key)), val);
        }
    }

}

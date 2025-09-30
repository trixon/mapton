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
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.TrendHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BTrendPeriod;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import se.trixon.almond.util.DateHelper;
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
        propertyMap.put(getCatKey(cat1, "REFERENSDATUM"), DateHelper.toDateString(p.getDateLatest().toLocalDate()));
        if (p.getDimension() != BDimension._2d) {
            populate(p, BComponent.HEIGHT, cat1, propertyMap);
        }
        if (p.getDimension() != BDimension._1d) {
            populate(p, BComponent.PLANE, cat1, propertyMap);
        }

        return propertyMap;
    }

    private LinkedHashMap<String, String> populate(HashMap<BTrendPeriod, TrendHelper.Trend> map) {
        var resultMap = new LinkedHashMap<String, String>();
        var now = LocalDateTime.now();
        if (map != null) {
            var startMinute = new Minute(0, new Hour());
            for (var key : BTrendPeriod.values()) {
                var trend = map.get(key);
                if (trend != null && !trend.startMinute().getDay().equals(startMinute.getDay())) {
                    var val1 = trend.function().getValue(ChartHelper.convertToMinute(now.plusYears(1)).getFirstMillisecond());
                    var val2 = trend.function().getValue(ChartHelper.convertToMinute(now).getFirstMillisecond());
                    var speed = "%+.1f mm/år (%d)".formatted((val1 - val2) * 1000, trend.numOfMeas());
                    resultMap.put(key.getTitle(), speed);
                    startMinute = trend.startMinute();
                }
            }
        }

        return resultMap;
    }

    private void populate(BTopoControlPoint p, BComponent component, String cat1, LinkedHashMap<String, Object> propertyMap) {
        var trendKey = component == BComponent.HEIGHT ? BKey.TRENDS_H : BKey.TRENDS_P;
        for (var entry : populate(p.getValue(trendKey)).entrySet()) {
            var key = entry.getKey();
            var val = entry.getValue();
            propertyMap.put(getCatKey(cat1, "%dd, %s".formatted(component.getDimension().getIndex(), key)), val);
        }
    }

}

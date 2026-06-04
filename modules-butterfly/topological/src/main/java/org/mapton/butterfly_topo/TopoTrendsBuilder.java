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
import org.mapton.butterfly_core.api.ButterflyHelper;
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

    private void populate(BTopoControlPoint p, BComponent component, String cat1, LinkedHashMap<String, Object> propertyMap) {
        var trendKey = component == BComponent.HEIGHT ? BKey.TRENDS_H : BKey.TRENDS_P;
        var trendKeyPrev = component == BComponent.HEIGHT ? BKey.TRENDS_PREV_H : BKey.TRENDS_PREV_P;
        HashMap<BTrendPeriod, TrendHelper.Trend> trendMap = p.getValue(trendKey);
        HashMap<BTrendPeriod, TrendHelper.Trend> trendMapPrev = p.getValue(trendKeyPrev);
        var startMinute = new Minute(0, new Hour());
        var now = LocalDateTime.now();
        for (var key : BTrendPeriod.values()) {
            var trend1 = trendMap.get(key);
            TrendHelper.Trend trend2 = null;
            if (trendMapPrev != null) {
                trend2 = trendMapPrev.get(key);
            }

            if (trend1 != null && !trend1.startMinute().getDay().equals(startMinute.getDay())) {
                var trend1val1 = trend1.function().getValue(ChartHelper.convertToMinute(now.plusYears(1)).getFirstMillisecond());
                var trend1val2 = trend1.function().getValue(ChartHelper.convertToMinute(now).getFirstMillisecond());
                var val1 = (trend1val1 - trend1val2) * 1000;
                startMinute = trend1.startMinute();
                var value = "";
                if (trend2 != null) {
                    var trend2val1 = trend2.function().getValue(ChartHelper.convertToMinute(now.plusYears(1)).getFirstMillisecond());
                    var trend2val2 = trend2.function().getValue(ChartHelper.convertToMinute(now).getFirstMillisecond());
                    var val2 = (trend2val1 - trend2val2) * 1000;
                    value = "(%+.1f • %+.1f • %+.1f) mm/år (%d • %d)".formatted(val1, val2, val1 - val2, trend1.numOfMeas(), trend2.numOfMeas());
                } else {
                    value = "%+.1f mm/år (%d)".formatted(val1, trend1.numOfMeas());
                }
                value = ButterflyHelper.replacePlusMinus(value);
                propertyMap.put(getCatKey(cat1, "%dd, %s".formatted(component.getDimension().getIndex(), key)), value);
            }
        }
    }

}

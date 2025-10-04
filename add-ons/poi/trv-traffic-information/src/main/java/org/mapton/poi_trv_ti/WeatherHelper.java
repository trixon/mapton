/*
 * Copyright 2025 Patrik Karlström <patrik@trixon.se>.
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
package org.mapton.poi_trv_ti;

import se.trixon.trv_traffic_information.road.weathermeasurepoint.v2_1.PrecipitationConditionAggregated;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class WeatherHelper {

    public static String getPrecipType(PrecipitationConditionAggregated precipitation) {
        var sb = new StringBuilder();
        if (precipitation.getRain().isValue()) {
            sb.append("Regn");
            if (precipitation.getSnow().isValue()) {
                sb.append(" & ");
            }
        }
        if (precipitation.getSnow().isValue()) {
            sb.append("Snö");
        }

        return sb.toString();
    }
}

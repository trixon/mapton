/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.se_poi;

import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MLatLon;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiManager;
import org.mapton.api.Mapton;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/**
 *
 * @author Patrik Karlström
 */
public class PoiQuickSearchProvider implements SearchProvider {

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        for (MPoi poi : MPoiManager.getInstance().getAllItems()) {
            if (StringUtils.containsIgnoreCase(String.join("/", poi.getCategory(), poi.getName(), poi.getProvider()), request.getText())) {
                if (!response.addResult(() -> {
                    Mapton.getEngine().panTo(new MLatLon(poi.getLatitude(), poi.getLongitude()), poi.getZoom());
                }, poi.getName())) {
                    break;
                }
            }
        }
    }
}

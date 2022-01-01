/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.geonames;

import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MLatLon;
import org.mapton.api.Mapton;
import org.mapton.geonames.api.CountryManager;
import org.mapton.geonames.api.Geoname;
import org.mapton.geonames.api.GeonamesManager;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/**
 *
 * @author Patrik Karlström
 */
public class GeoNamesQuickSearchProvider implements SearchProvider {

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        for (Geoname g : GeonamesManager.getInstance().getGeonames()) {
            if (StringUtils.containsIgnoreCase(String.join("/", g.getAsciiName(), g.getName(), g.getAlternateNames(), CountryManager.getInstance().getCodeNameMap().getOrDefault(g.getCountryCode(), "")), request.getText())) {
                if (!response.addResult(() -> {
                    Mapton.getEngine().panTo(new MLatLon(g.getLatitude(), g.getLongitude()), 0.5);
                }, g.getName())) {
                    break;
                }
            }
        }
    }

}

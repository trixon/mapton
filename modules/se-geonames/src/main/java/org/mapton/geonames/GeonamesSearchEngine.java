/*
 * Copyright 2019 Patrik Karlström.
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

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MBookmark;
import org.mapton.api.MSearchEngine;
import org.mapton.geonames.api.CountryManager;
import org.mapton.geonames.api.GeonamesManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MSearchEngine.class)
public class GeonamesSearchEngine implements MSearchEngine {

    public GeonamesSearchEngine() {
    }

    @Override
    public String getName() {
        return "GeoNames";
    }

    @Override
    public ArrayList<MBookmark> getResults(String searchString) {
        ArrayList<MBookmark> bookmarks = new ArrayList<>();
        int limit = StringUtils.isBlank(searchString) ? Integer.MAX_VALUE : 20;
        GeonamesManager.getInstance().getGeonames().stream()
                .filter((g) -> (StringUtils.containsIgnoreCase(String.join("/", g.getAsciiName(), g.getName(), g.getAlternateNames(), CountryManager.getInstance().getCountries().getOrDefault(g.getCountryCode(), "")), searchString)))
                //                .limit(limit)
                .forEachOrdered((g) -> {
                    MBookmark b = new MBookmark();
                    b.setName(g.getName());
                    b.setCategory(g.getCountryCode());
                    b.setLatitude(g.getLatitude());
                    b.setLongitude(g.getLongitude());
                    b.setZoom(0.5);
                    bookmarks.add(b);
                });

        return bookmarks;
    }
}

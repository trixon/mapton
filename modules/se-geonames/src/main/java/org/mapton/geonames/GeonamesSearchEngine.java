/*
 * Copyright 2018 Patrik Karlström.
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;
import org.mapton.api.MBookmark;
import org.mapton.api.MSearchEngine;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MSearchEngine.class)
public class GeonamesSearchEngine implements MSearchEngine {

    private static final TreeMap<String, String> sCountries = new TreeMap<>();
    private static ArrayList<Geoname> sGeonames;

    static {
        init();
    }

    private static void init() {
        Gson gson = new GsonBuilder().create();
        String json;

        json = SystemHelper.getResourceAsString(GeonamesSearchEngine.class, "geonames.json");
        sGeonames = gson.fromJson(json, new TypeToken<ArrayList<Geoname>>() {
        }.getType());

        sGeonames.sort((Geoname o1, Geoname o2) -> o1.getName().compareTo(o2.getName()));
        json = SystemHelper.getResourceAsString(GeonamesSearchEngine.class, "country_codes.json");
        ArrayList<Country> countries = gson.fromJson(json, new TypeToken<ArrayList<Country>>() {
        }.getType());

        countries.forEach((country) -> {
            sCountries.put(country.mCode, country.mName);
        });
    }

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
        sGeonames.stream()
                .filter((g) -> (StringUtils.containsIgnoreCase(String.join("/", g.getAsciiName(), g.getName(), g.getAlternateNames(), sCountries.getOrDefault(g.getCountryCode(), "")), searchString)))
                //                .limit(limit)
                .forEachOrdered((g) -> {
                    MBookmark b = new MBookmark();
                    b.setName(g.getName());
                    b.setCategory(g.getCountryCode());
                    b.setLatitude(g.getLatitude());
                    b.setLongitude(g.getLongitude());
                    bookmarks.add(b);
                });

        return bookmarks;
    }

    private class Country {

        @SerializedName("Code")
        private String mCode;
        @SerializedName("Name")
        private String mName;
    }
}

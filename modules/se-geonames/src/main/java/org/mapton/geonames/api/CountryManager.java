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
package org.mapton.geonames.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.TreeMap;
import org.mapton.geonames.GeonamesSearchEngine;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class CountryManager {

    private final TreeMap<String, Country> mCodeCountryMap = new TreeMap<>();
    private final TreeMap<String, String> mCodeNameMap = new TreeMap<>();
    private ArrayList<Country> mCountryList;

    public static CountryManager getInstance() {
        return Holder.INSTANCE;
    }

    private CountryManager() {
        Gson gson = new GsonBuilder().create();
        String json = SystemHelper.getResourceAsString(GeonamesSearchEngine.class, "country_codes.json");
        mCountryList = gson.fromJson(json, new TypeToken<ArrayList<Country>>() {
        }.getType());

        mCountryList.sort((Country o1, Country o2) -> o1.getName().compareTo(o2.getName()));

        mCountryList.forEach((country) -> {
            mCodeCountryMap.put(country.getCode(), country);
            mCodeNameMap.put(country.getCode(), country.getName());
        });
    }

    public TreeMap<String, Country> getCodeCountryMap() {
        return mCodeCountryMap;
    }

    public TreeMap<String, String> getCodeNameMap() {
        return mCodeNameMap;
    }

    public ArrayList<Country> getCountryList() {
        return mCountryList;
    }

    private static class Holder {

        private static final CountryManager INSTANCE = new CountryManager();
    }
}

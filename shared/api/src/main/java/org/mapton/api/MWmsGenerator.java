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
package org.mapton.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MWmsGenerator {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    protected final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .setVersion(1.0)
            .setPrettyPrinting()
            .setDateFormat(DATE_FORMAT)
            .create();
    protected final ArrayList<MWmsSource> mSources = new ArrayList<>();
    protected final ArrayList<MWmsStyle> mStyles = new ArrayList<>();
    protected final TreeMap<String, String> mCategoriesEarth = new TreeMap<>();
    protected final TreeMap<String, String> mCategoriesSwe = new TreeMap<>();

    public MWmsGenerator() {
        mCategoriesEarth.put("", "Earth");
        mCategoriesEarth.put("sv", "Jorden");
        mCategoriesSwe.put("", "Sweden");
        mCategoriesSwe.put("sv", "Sverige");
    }

    public MWmsSource createSource(String name, String url, TreeMap<String, String> layers, TreeMap<String, MAttribution> attributions, boolean enabled) {
        MWmsSource source = new MWmsSource();
        source.setName(name);
        source.setEnabled(enabled);
        source.setUrl(url);
        source.setLayers(layers);
        source.setAttributions(attributions);

        return source;
    }

    public MWmsStyle createStyle(TreeMap<String, String> categories, TreeMap<String, String> names, TreeMap<String, String> descriptions, String supplier, boolean enabled, String id, String... layers) {
        MWmsStyle style = new MWmsStyle();
        style.setCategories(categories);
        style.setNames(names);
        style.setEnabled(enabled);
        style.setSupplier(supplier);
        style.setDescriptions(descriptions);
        style.setId(id);
        style.setLayers(new ArrayList<>(Arrays.asList(layers)));

        return style;
    }

    public String getSourceJson() {
        return gson.toJson(mSources);
    }

    public String getStyleJson() {
        return gson.toJson(mStyles);
    }
}

/*
 * Copyright 2021 Patrik Karlström.
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

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public class MWmsStyle {

    @SerializedName("category")
    private TreeMap<String, String> mCategories = new TreeMap<>();
    @SerializedName("description")
    private TreeMap<String, String> mDescriptions = new TreeMap<>();
    @SerializedName("enabled")
    private boolean mEnabled;
    @SerializedName("id")
    private String mId;
    @SerializedName("layers")
    private ArrayList<String> mLayers;
    @SerializedName("name")
    private TreeMap<String, String> mNames = new TreeMap<>();
    @SerializedName("supplier")
    private String mSupplier;

    public TreeMap<String, String> getCategories() {
        return mCategories;
    }

    public String getCategory() {
        return getLocalized(mCategories, "");
    }

    public String getDescription() {
        return getLocalized(mDescriptions, "");
    }

    public TreeMap<String, String> getDescriptions() {
        return mDescriptions;
    }

    public String getId() {
        return mId;
    }

    public ArrayList<String> getLayers() {
        return mLayers;
    }

    public String getName() {
        return getLocalized(mNames, "UNKNOWN");
    }

    public TreeMap<String, String> getNames() {
        return mNames;
    }

    public String getSupplier() {
        return mSupplier;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setCategories(TreeMap<String, String> categories) {
        mCategories = categories;
    }

    public void setDescriptions(TreeMap<String, String> descriptions) {
        mDescriptions = descriptions;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setLayers(ArrayList<String> layers) {
        mLayers = layers;
    }

    public void setNames(TreeMap<String, String> names) {
        mNames = names;
    }

    public void setSupplier(String supplier) {
        mSupplier = supplier;
    }

    private String getLocalized(TreeMap<String, String> treeMap, String defaultValue) {
        if (treeMap == null) {
            return null;
        } else {
            return treeMap.getOrDefault(Locale.getDefault().getLanguage(), StringUtils.defaultString(treeMap.get(""), defaultValue));
        }
    }
}

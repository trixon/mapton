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
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public class MWmsSource {

    @SerializedName("attributions")
    private TreeMap<String, MAttribution> mAttributions = new TreeMap<>();
    @SerializedName("enabled")
    private boolean mEnabled;
    @SerializedName("layers")
    private TreeMap<String, String> mLayers = new TreeMap<>();
    @SerializedName("name")
    private String mName;
    @SerializedName("url")
    private String mUrl;

    public MWmsSource() {
    }

    public TreeMap<String, MAttribution> getAttributions() {
        return mAttributions;
    }

    public String getLayerName(String layer) {
        return StringUtils.defaultIfBlank(mLayers.get(layer), layer);
    }

    public TreeMap<String, String> getLayers() {
        return mLayers;
    }

    public String getName() {
        return mName;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setAttributions(TreeMap<String, MAttribution> attributions) {
        mAttributions = attributions;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void setLayers(TreeMap<String, String> layers) {
        mLayers = layers;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}

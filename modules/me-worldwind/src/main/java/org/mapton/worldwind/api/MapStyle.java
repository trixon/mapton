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
package org.mapton.worldwind.api;

import java.util.ArrayList;
import org.mapton.api.MKey;
import org.mapton.api.MWmsStyle;
import org.mapton.api.Mapton;
import org.openide.util.Lookup;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MapStyle {

    private String mCategory;
    private String mDescription;
    private String[] mLayers;
    private String mName;
    private String mSuppliers;

    public static MapStyle createFromWmsStyle(MWmsStyle wmsStyle) {
        MapStyle mapStyle = new MapStyle() {
        };

        mapStyle.setName(wmsStyle.getName());
        mapStyle.setSuppliers(wmsStyle.getSupplier());
        mapStyle.setLayers(wmsStyle.getLayers().toArray(new String[0]));
        mapStyle.setCategory(wmsStyle.getCategory());
        mapStyle.setDescription(wmsStyle.getDescription());

        return mapStyle;
    }

    public static String[] getLayers(String name) {
        String[] layers = null;
        ArrayList<MapStyle> styles = new ArrayList<>(Lookup.getDefault().lookupAll(MapStyle.class));
        ArrayList<MWmsStyle> wmsStyles = Mapton.getGlobalState().get(MKey.DATA_SOURCES_WMS_STYLES);
        wmsStyles.forEach((wmsStyle) -> {
            styles.add(MapStyle.createFromWmsStyle(wmsStyle));
        });

        for (MapStyle mapStyle : styles) {
            if (mapStyle.getName().equalsIgnoreCase(name)) {
                layers = mapStyle.getLayers();
                break;
            }
        }

        return layers;
    }

    public MapStyle() {
    }

    public String getCategory() {
        return mCategory;
    }

    public String getDescription() {
        return mDescription;
    }

    public String[] getLayers() {
        return mLayers;
    }

    public String getName() {
        return mName;
    }

    public String getSuppliers() {
        return mSuppliers;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setLayers(String[] value) {
        mLayers = value.clone();
    }

    public void setName(String value) {
        mName = value;
    }

    public void setSuppliers(String suppliers) {
        mSuppliers = suppliers;
    }

    @Override
    public String toString() {
        return mName;
    }
}

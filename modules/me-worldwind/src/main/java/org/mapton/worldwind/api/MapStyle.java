/*
 * Copyright 2023 Patrik Karlström.
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
import org.apache.commons.lang3.StringUtils;
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
    private String mId;
    private String[] mLayers;
    private String mName;
    private String mSuppliers;

    public static MapStyle createFromWmsStyle(MWmsStyle wmsStyle) {
        MapStyle mapStyle = new MapStyle() {
        };

        mapStyle.setId(wmsStyle.getId());
        mapStyle.setName(wmsStyle.getName());
        mapStyle.setSuppliers(wmsStyle.getSupplier());
        mapStyle.setLayers(wmsStyle.getLayers().toArray(new String[0]));
        mapStyle.setCategory(wmsStyle.getCategory());
        mapStyle.setDescription(wmsStyle.getDescription());

        return mapStyle;
    }

    public static String[] getLayers(String id) {
        String[] layers = null;
        ArrayList<MapStyle> styles = new ArrayList<>(Lookup.getDefault().lookupAll(MapStyle.class));
        ArrayList<MWmsStyle> wmsStyles = Mapton.getGlobalState().get(MKey.DATA_SOURCES_WMS_STYLES);

        try {
            wmsStyles.forEach((wmsStyle) -> {
                styles.add(MapStyle.createFromWmsStyle(wmsStyle));
            });
        } catch (Exception e) {
            //nvm
        }

        for (MapStyle mapStyle : styles) {
            if (mapStyle.getId().equalsIgnoreCase(id)) {
                layers = mapStyle.getLayers();
                break;
            }
        }

        return layers;
    }

    public static MapStyle getStyle(String id) {
        ArrayList<MapStyle> styles = new ArrayList<>(Lookup.getDefault().lookupAll(MapStyle.class));
        ArrayList<MWmsStyle> wmsStyles = Mapton.getGlobalState().get(MKey.DATA_SOURCES_WMS_STYLES);
        if (wmsStyles != null) {
            wmsStyles.forEach((wmsStyle) -> {
                styles.add(MapStyle.createFromWmsStyle(wmsStyle));
            });
        }

        for (MapStyle mapStyle : styles) {
            if (StringUtils.equals(mapStyle.getId(), id)) {
                return mapStyle;
            }
        }

        return new MapStyle() {
            @Override
            public String getId() {
                return "unknown.id";
            }
        };
    }

    public MapStyle() {
    }

    public String getCategory() {
        return mCategory;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getId() {
        return mId;
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

    public void setId(String id) {
        mId = id;
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

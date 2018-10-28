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
package org.mapton.worldwind.api;

import org.openide.util.Lookup;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MapStyle {

    private String[] mLayers;
    private String mName;
    private String mSuppliers;

    public static String[] getLayers(String name) {
        String[] layers = null;

        for (MapStyle mapStyle : Lookup.getDefault().lookupAll(MapStyle.class)) {
            if (mapStyle.getName().equalsIgnoreCase(name)) {
                layers = mapStyle.getLayers();
                break;
            }
        }

        return layers;
    }

    public MapStyle() {
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

    public void setLayers(String[] value) {
        mLayers = value.clone();
    }

    public void setName(String value) {
        mName = value;
    }

    public void setSuppliers(String suppliers) {
        mSuppliers = suppliers;
    }

}

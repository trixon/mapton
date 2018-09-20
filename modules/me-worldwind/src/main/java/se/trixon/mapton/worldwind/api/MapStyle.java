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
package se.trixon.mapton.worldwind.api;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.openide.util.Lookup;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MapStyle {

    private String[] mLayers;
    private final StringProperty mName = new SimpleStringProperty();

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

    public final String[] getLayers() {
        return mLayers;
    }

    public final String getName() {
        return mName.get();
    }

    public final StringProperty nameProperty() {
        return mName;
    }

    public final void setLayers(String[] value) {
        mLayers = value.clone();
    }

    public final void setName(String value) {
        mName.set(value);
    }

}

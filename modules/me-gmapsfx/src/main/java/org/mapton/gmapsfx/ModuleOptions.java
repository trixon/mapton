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
package org.mapton.gmapsfx;

import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class ModuleOptions {

    public static final String KEY_MAP_STYLE = "map_style";
    public static final String KEY_MAP_TYPE = "map_type";
    private static final String DEFAULT_MAP_STYLE = "Retro";
    private static final String DEFAULT_MAP_TYPE = "TERRAIN";
    private static final int DEFAULT_MAP_ZOOM = 12;
    private static final String KEY_MAP_KEY = "map_key";
    private static final String KEY_MAP_ZOOM = "map_zoom";
    private final Preferences mPreferences = NbPreferences.forModule(ModuleOptions.class);

    public static ModuleOptions getInstance() {
        return Holder.INSTANCE;
    }

    private ModuleOptions() {
    }

    public String getMapKey() {
        return mPreferences.get(KEY_MAP_KEY, "AIzaSyCdVPck8GWP2piXLjl7XTf4QOaydWWYzFE");
    }

    public String getMapStyle() {
        return mPreferences.get(KEY_MAP_STYLE, DEFAULT_MAP_STYLE);
    }

    public MapTypeIdEnum getMapType() {
        switch (mPreferences.get(KEY_MAP_TYPE, DEFAULT_MAP_TYPE)) {
            case "HYBRID":
                return MapTypeIdEnum.HYBRID;

            case "SATELLITE":
                return MapTypeIdEnum.SATELLITE;

            case "TERRAIN":
                return MapTypeIdEnum.TERRAIN;

            default:
                return MapTypeIdEnum.ROADMAP;
        }
    }

    public int getMapZoom() {
        return mPreferences.getInt(KEY_MAP_ZOOM, DEFAULT_MAP_ZOOM);
    }

    public Preferences getPreferences() {
        return mPreferences;
    }

    public void setMapKey(String value) {
        mPreferences.put(KEY_MAP_KEY, value);
    }

    public void setMapStyle(String value) {
        mPreferences.put(KEY_MAP_STYLE, value);
    }

    public void setMapType(MapTypeIdEnum mapType) {
        mPreferences.put(KEY_MAP_TYPE, mapType.getName());
    }

    public void setMapZoom(int value) {
        mPreferences.putInt(KEY_MAP_ZOOM, value);
    }

    private static class Holder {

        private static final ModuleOptions INSTANCE = new ModuleOptions();
    }
}

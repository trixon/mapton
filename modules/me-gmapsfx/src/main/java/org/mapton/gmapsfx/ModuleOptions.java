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
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class ModuleOptions extends OptionsBase {

    public static final String KEY_MAP_STYLE = "map_style";
    public static final String KEY_MAP_TYPE = "map_type";
    static final String DEFAULT_MAP_STYLE = "Retro";
    static final String DEFAULT_MAP_TYPE = "TERRAIN";
    static final int DEFAULT_MAP_ZOOM = 12;
    static final String KEY_MAP_ZOOM = "map_zoom";

    public static ModuleOptions getInstance() {
        return Holder.INSTANCE;
    }

    private ModuleOptions() {
        mPreferences = NbPreferences.forModule(ModuleOptions.class);
    }

    public MapTypeIdEnum getMapType() {
        switch (get(KEY_MAP_TYPE, DEFAULT_MAP_TYPE)) {
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

    public void setMapType(MapTypeIdEnum mapType) {
        put(KEY_MAP_TYPE, mapType.getName());
    }

    private static class Holder {

        private static final ModuleOptions INSTANCE = new ModuleOptions();
    }
}

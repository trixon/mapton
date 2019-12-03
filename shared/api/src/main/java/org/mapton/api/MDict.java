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

import java.util.Locale;
import java.util.ResourceBundle;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public enum MDict {
    ATMOSPHERE,
    COMPASS,
    COORDINATE_SYSTEM,
    COPY_LOCATION,
    DISPLAY_PLACEMARK,
    ELEVATION,
    FLAT,
    GLOBE,
    GRID,
    GRIDS,
    HOLLOW,
    HOLLOW_DESCRIPTION,
    LINE_WIDTH,
    MAP_ENGINE,
    MAP_TOOLS,
    OPEN_LOCATION,
    ORIGIN,
    PROJECTION,
    PROJ_LAT_LON,
    PROJ_MERCATOR,
    PROJ_POLAR_NORTH,
    PROJ_POLAR_SOUTH,
    PROJ_SINUSOIDAL,
    PROJ_SINUSOIDAL_MODIFIED,
    PROJ_TRANSVERSE_MERCATOR,
    PROJ_UPS_NORTH,
    PROJ_UPS_SOUTH,
    SCALE_BAR,
    SEARCH_PROMPT,
    SET_HOME,
    SPHERE,
    STARS,
    VIEW_CONTROLS,
    WORLD_MAP;

    private final ResourceBundle mResourceBundle = ResourceBundle.getBundle(SystemHelper.getPackageAsPath(MDict.class) + "MDict", Locale.getDefault());

    private static String getString(ResourceBundle bundle, String key) {
        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        } else {
            return "Key not found: " + key;
        }
    }

    @Override
    public String toString() {
        return getString(mResourceBundle, name().toLowerCase());
    }
}

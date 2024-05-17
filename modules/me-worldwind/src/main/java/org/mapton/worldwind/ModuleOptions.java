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
package org.mapton.worldwind;

import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class ModuleOptions extends OptionsBase {

    public static final String KEY_BACKGROUND_IMAGE_OPACITY = "background_image.opacity";
    public static final String KEY_DISPLAY_ATMOSPHERE = "display_atmosphere";
    public static final String KEY_DISPLAY_COMPASS = "display_compass";
    public static final String KEY_DISPLAY_CONTROLS = "display_controls2";
    public static final String KEY_DISPLAY_MASK = "display_mask";
    public static final String KEY_DISPLAY_PLACE_NAMES = "display_place_names2";
    public static final String KEY_DISPLAY_SCALE_BAR = "display_scale_bar";
    public static final String KEY_DISPLAY_STARS = "display_stars";
    public static final String KEY_DISPLAY_WORLD_MAP = "display_world_map";
    public static final String KEY_MAP_ELEVATION = "map_elevation";
    public static final String KEY_MAP_GLOBE = "map_mode";
    public static final String KEY_MAP_OPACITY = "map_opacity";
    public static final String KEY_MAP_OVERLAYS = "map_overlays";
    public static final String KEY_MAP_PROJECTION = "map_projection";
    public static final String KEY_MAP_STYLE = "map_style";
    public static final String KEY_MAP_STYLE_PREV = "map_style_prev";
    public static final String KEY_MASK_COLOR = "mask_color";
    public static final String KEY_MASK_OPACITY = "mask_opacity";
    public static final String KEY_RULER_ANNOTATION = "ruler.annotation";
    public static final String KEY_RULER_COLOR_ANNOTATION = "ruler.color.annotation";
    public static final String KEY_RULER_COLOR_BACKGROUND = "ruler.color.background";
    public static final String KEY_RULER_COLOR_LINE = "ruler.color.line";
    public static final String KEY_RULER_COLOR_POINT = "ruler.color.point";
    public static final String KEY_RULER_CONTROL_POINTS = "ruler.control_points";
    public static final String KEY_RULER_FOLLOW_TERRAIN = "ruler.follow_terrain";
    public static final String KEY_RULER_FREE_HAND = "ruler.free_hand";
    public static final String KEY_RULER_LINE_WIDTH = "ruler.line_width";
    public static final String KEY_RULER_OPACITY = "ruler.opacity";
    public static final String KEY_RULER_PATH_TYPE = "ruler.path_type";
    public static final String KEY_RULER_POINT_LIST = "ruler.point_list";
    public static final String KEY_RULER_RUBBER_BAND = "ruler.rubber_band";
    public static final String KEY_RULER_SHAPE = "ruler.shape";
    public static final String KEY_VIEW_ALTITUDE = "view_altitude";
    public static final String KEY_VIEW_HEADING = "view_heading";
    public static final String KEY_VIEW_PITCH = "view_pitch";
    public static final String KEY_VIEW_ROLL = "view_roll";

    static final double DEFAULT_BACKGROUND_IMAGE_OPACITY = 1.0;
    static final boolean DEFAULT_DISPLAY_ATMOSPHERE = true;
    static final boolean DEFAULT_DISPLAY_COMPASS = true;
    static final boolean DEFAULT_DISPLAY_CONTROLS = false;
    static final boolean DEFAULT_DISPLAY_MASK = false;
    static final boolean DEFAULT_DISPLAY_PLACE_NAMES = false;
    static final boolean DEFAULT_DISPLAY_SCALE_BAR = true;
    static final boolean DEFAULT_DISPLAY_STARS = true;
    static final boolean DEFAULT_DISPLAY_WORLD_MAP = false;
    static final boolean DEFAULT_MAP_ELEVATION = false;
    static final boolean DEFAULT_MAP_GLOBE = true;
    static final double DEFAULT_MAP_OPACITY = 1.0;
    static final int DEFAULT_MAP_PROJECTION = 1;
    static final String DEFAULT_MAP_STYLE = "at.eox.osm";
    static final String DEFAULT_MASK_COLOR = "ffffff";
    static final float DEFAULT_MASK_OPACITY = 0.5f;

    public static ModuleOptions getInstance() {
        return Holder.INSTANCE;
    }

    private ModuleOptions() {
        mPreferences = NbPreferences.forModule(ModuleOptions.class);
    }

    private static class Holder {

        private static final ModuleOptions INSTANCE = new ModuleOptions();
    }
}

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
package se.trixon.mapton.worldwind;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class ModuleOptions {

    public static final String KEY_DISPLAY_COMPASS = "display_compass";
    public static final String KEY_DISPLAY_CONTROLS = "display_controls";
    public static final String KEY_DISPLAY_SCALE_BAR = "display_scale_bar";
    public static final String KEY_DISPLAY_WORLD_MAP = "display_world_map";
    public static final String KEY_MAP_GLOBE = "map_mode";
    public static final String KEY_MAP_PROJECTION = "map_projection";
    private static final boolean DEFAULT_DISPLAY_COMPASS = true;
    private static final boolean DEFAULT_DISPLAY_CONTROLS = true;
    private static final boolean DEFAULT_DISPLAY_SCALE_BAR = true;
    private static final boolean DEFAULT_DISPLAY_WORLD_MAP = true;
    private static final boolean DEFAULT_MAP_GLOBE = true;
    private static final int DEFAULT_MAP_PROJECTION = 1;
    private final Preferences mPreferences = NbPreferences.forModule(ModuleOptions.class);

    public static ModuleOptions getInstance() {
        return Holder.INSTANCE;
    }

    private ModuleOptions() {
    }

    public int getMapProjection() {
        return mPreferences.getInt(KEY_MAP_PROJECTION, DEFAULT_MAP_PROJECTION);
    }

    public Preferences getPreferences() {
        return mPreferences;
    }

    public boolean isDisplayCompass() {
        return mPreferences.getBoolean(KEY_DISPLAY_COMPASS, DEFAULT_DISPLAY_COMPASS);
    }

    public boolean isDisplayControls() {
        return mPreferences.getBoolean(KEY_DISPLAY_CONTROLS, DEFAULT_DISPLAY_CONTROLS);
    }

    public boolean isDisplayScaleBar() {
        return mPreferences.getBoolean(KEY_DISPLAY_SCALE_BAR, DEFAULT_DISPLAY_SCALE_BAR);
    }

    public boolean isDisplayWorldMap() {
        return mPreferences.getBoolean(KEY_DISPLAY_WORLD_MAP, DEFAULT_DISPLAY_WORLD_MAP);
    }

    public boolean isMapGlobe() {
        return mPreferences.getBoolean(KEY_MAP_GLOBE, DEFAULT_MAP_GLOBE);
    }

    public void setDisplayCompass(boolean value) {
        mPreferences.putBoolean(KEY_DISPLAY_COMPASS, value);
    }

    public void setDisplayControls(boolean value) {
        mPreferences.putBoolean(KEY_DISPLAY_CONTROLS, value);
    }

    public void setDisplayScaleBar(boolean value) {
        mPreferences.putBoolean(KEY_DISPLAY_SCALE_BAR, value);
    }

    public void setDisplayWorldMap(boolean value) {
        mPreferences.putBoolean(KEY_DISPLAY_WORLD_MAP, value);
    }

    public void setMapGlobe(boolean value) {
        mPreferences.putBoolean(KEY_MAP_GLOBE, value);
    }

    public void setMapProjection(int value) {
        mPreferences.putInt(KEY_MAP_PROJECTION, value);
    }

    private static class Holder {

        private static final ModuleOptions INSTANCE = new ModuleOptions();
    }
}

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

import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class MOptions extends OptionsBase {

    public static final boolean DEFAULT_DARK_THEME = false;
    public static final String KEY_DARK_THEME = "ui.dark_theme";
    public static final String KEY_DISPLAY_CROSSHAIR = "display_crosshair";
    public static final String KEY_GRID_GLOBAL_CLAMP_TO_GROUND = "global_clamp_to_ground";
    public static final String KEY_GRID_GLOBAL_EQUATOR = "grid.global.equator";
    public static final String KEY_GRID_GLOBAL_LATITUDES = "grid.global.latitudes";
    public static final String KEY_GRID_GLOBAL_LONGITUDES = "grid.global.longitudes";
    public static final String KEY_GRID_GLOBAL_PLOT = "grid.global.plot";
    public static final String KEY_GRID_GLOBAL_POLAR_ANTARCTIC = "grid.global.polar_antarctic";
    public static final String KEY_GRID_GLOBAL_POLAR_ARCTIC = "grid.global.polar_arctic";
    public static final String KEY_GRID_GLOBAL_TROPIC_CANCER = "grid.global.tropic_cancer";
    public static final String KEY_GRID_GLOBAL_TROPIC_CAPRICORN = "grid.global.tropic_capricorn";
    public static final String KEY_GRID_LOCAL_PLOT = "grid.local.plot";
    public static final String KEY_LOCAL_GRIDS = "local_grids";
    public static final String KEY_MAP_ENGINE = "map_engine";
    public static final String KEY_MAP_HOME_LAT = "map_home_lat";
    public static final String KEY_MAP_HOME_LON = "map_home_lon";
    public static final String KEY_MAP_ONLY = "map_only";
    public static final String KEY_PREFER_POPOVER = "prefer_popover";

    private static final boolean DEFAULT_FULL_SCREEN = false;
    private static final String DEFAULT_MAP_ENGINE = "org.mapton.worldwind.WorldWindMapEngine";
    private static final double DEFAULT_MAP_LAT = 57.661509;
    private static final double DEFAULT_MAP_LON = 11.999312;
    private static final boolean DEFAULT_MAP_ONLY = false;
    private static final double DEFAULT_MAP_ZOOM = 0.8f;
    private static final boolean DEFAULT_PREFER_POPOVER = false;
    private static final String KEY_DISPLAY_BOOKMARK = "display_bookmark";
    private static final String KEY_FULL_SCREEN = "fullscreen";
    private static final String KEY_MAP_CENTER_LAT = "map_center_lat";
    private static final String KEY_MAP_CENTER_LON = "map_center_lon";
    private static final String KEY_MAP_COO_TRANS = "map_coo_trans";
    private static final String KEY_MAP_HOME_ZOOM = "map_home_zoom";
    private static final String KEY_MAP_ZOOM = "map_zoom";

    public static MOptions getInstance() {
        return Holder.INSTANCE;
    }

    private MOptions() {
        setPreferences(NbPreferences.forModule(MOptions.class));
    }

    public String getEngine() {
        return mPreferences.get(KEY_MAP_ENGINE, DEFAULT_MAP_ENGINE);
    }

    public MLatLon getMapCenter() {
        return new MLatLon(
                mPreferences.getDouble(KEY_MAP_CENTER_LAT, DEFAULT_MAP_LAT),
                mPreferences.getDouble(KEY_MAP_CENTER_LON, DEFAULT_MAP_LON));
    }

    public MCooTrans getMapCooTrans() {
        for (MCooTrans cooTrans : MCooTrans.getCooTrans()) {
            if (cooTrans.getName().equalsIgnoreCase(getMapCooTransName())) {
                return cooTrans;
            }
        }

        return null;
    }

    public String getMapCooTransName() {
        return mPreferences.get(KEY_MAP_COO_TRANS, "WGS 84 DMS");
    }

    public MLatLon getMapHome() {
        return new MLatLon(
                mPreferences.getDouble(KEY_MAP_HOME_LAT, DEFAULT_MAP_LAT),
                mPreferences.getDouble(KEY_MAP_HOME_LON, DEFAULT_MAP_LON));
    }

    public double getMapHomeZoom() {
        return mPreferences.getDouble(KEY_MAP_HOME_ZOOM, DEFAULT_MAP_ZOOM);
    }

    public double getMapZoom() {
        return mPreferences.getDouble(KEY_MAP_ZOOM, DEFAULT_MAP_ZOOM);
    }

    public boolean isBookmarkVisible() {
        return mPreferences.getBoolean(KEY_DISPLAY_BOOKMARK, true);
    }

    public boolean isDarkThemeEnabled() {
        return is(MOptions.KEY_DARK_THEME, MOptions.DEFAULT_DARK_THEME);
    }

    public boolean isFullscreen() {
        return mPreferences.getBoolean(KEY_FULL_SCREEN, DEFAULT_FULL_SCREEN);
    }

    public boolean isMapOnly() {
        return mPreferences.getBoolean(KEY_MAP_ONLY, DEFAULT_MAP_ONLY);
    }

    public boolean isPreferPopover() {
        return mPreferences.getBoolean(KEY_PREFER_POPOVER, DEFAULT_PREFER_POPOVER);
    }

    public void setBookmarkVisible(boolean value) {
        mPreferences.putBoolean(KEY_DISPLAY_BOOKMARK, value);
    }

    public void setEngine(String value) {
        mPreferences.put(KEY_MAP_ENGINE, value);
    }

    public void setFullscreen(boolean value) {
        mPreferences.putBoolean(KEY_FULL_SCREEN, value);
    }

    public void setMapCenter(MLatLon value) {
        mPreferences.putDouble(KEY_MAP_CENTER_LAT, value.getLatitude());
        mPreferences.putDouble(KEY_MAP_CENTER_LON, value.getLongitude());
    }

    public void setMapCooTrans(String value) {
        mPreferences.put(KEY_MAP_COO_TRANS, value);
    }

    public void setMapHome(MLatLon value) {
        mPreferences.putDouble(KEY_MAP_HOME_LAT, value.getLatitude());
        mPreferences.putDouble(KEY_MAP_HOME_LON, value.getLongitude());
    }

    public void setMapHomeZoom(double value) {
        mPreferences.putDouble(KEY_MAP_HOME_ZOOM, value);
    }

    public void setMapOnly(boolean value) {
        mPreferences.putBoolean(KEY_MAP_ONLY, value);
    }

    public void setMapZoom(double value) {
        mPreferences.putDouble(KEY_MAP_ZOOM, value);
    }

    public void setPreferPopover(boolean value) {
        mPreferences.putBoolean(KEY_PREFER_POPOVER, value);
    }

    private static class Holder {

        private static final MOptions INSTANCE = new MOptions();
    }
}

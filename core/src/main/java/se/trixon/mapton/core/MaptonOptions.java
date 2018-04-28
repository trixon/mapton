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
package se.trixon.mapton.core;

import com.lynden.gmapsfx.javascript.object.LatLong;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class MaptonOptions {

    private static final double DEFAULT_MAP_LAT = 57.661509;
    private static final double DEFAULT_MAP_LON = 11.999312;

    private static final int DEFAULT_MAP_ZOOM = 5;

    private static final String KEY_DISPLAY_BOOKMARK = "display_bookmark";
    private static final String KEY_MAP_CENTER_LAT = "map_center_lat";
    private static final String KEY_MAP_CENTER_LON = "map_center_lon";
    private static final String KEY_MAP_HOME_LAT = "map_home_lat";
    private static final String KEY_MAP_HOME_LON = "map_home_lon";
    private static final String KEY_MAP_HOME_ZOOM = "map_home_zoom";
    private static final String KEY_MAP_KEY = "map_key";
    private static final String KEY_MAP_ZOOM = "map_zoom";
    private final Preferences mPreferences = NbPreferences.forModule(MaptonOptions.class);

    public static MaptonOptions getInstance() {
        return MaptonOptionsHolder.INSTANCE;
    }

    private MaptonOptions() {
    }

    public LatLong getMapCenter() {
        return new LatLong(
                mPreferences.getDouble(KEY_MAP_CENTER_LAT, DEFAULT_MAP_LAT),
                mPreferences.getDouble(KEY_MAP_CENTER_LON, DEFAULT_MAP_LON));
    }

    public LatLong getMapHome() {
        return new LatLong(
                mPreferences.getDouble(KEY_MAP_HOME_LAT, DEFAULT_MAP_LAT),
                mPreferences.getDouble(KEY_MAP_HOME_LON, DEFAULT_MAP_LON));
    }

    public int getMapHomeZoom() {
        return mPreferences.getInt(KEY_MAP_HOME_ZOOM, DEFAULT_MAP_ZOOM);
    }

    public int getMapZoom() {
        return mPreferences.getInt(KEY_MAP_ZOOM, DEFAULT_MAP_ZOOM);
    }

    public boolean isBookmarkVisible() {
        return mPreferences.getBoolean(KEY_DISPLAY_BOOKMARK, true);
    }

    public void setBookamarkVisible(boolean value) {
        mPreferences.putBoolean(KEY_DISPLAY_BOOKMARK, value);
    }

    public void setMapCenter(LatLong value) {
        mPreferences.putDouble(KEY_MAP_CENTER_LAT, value.getLatitude());
        mPreferences.putDouble(KEY_MAP_CENTER_LON, value.getLongitude());
    }

    public void setMapHome(LatLong value) {
        mPreferences.putDouble(KEY_MAP_HOME_LAT, value.getLatitude());
        mPreferences.putDouble(KEY_MAP_HOME_LON, value.getLongitude());
    }

    public void setMapHomeZoom(int value) {
        mPreferences.putInt(KEY_MAP_HOME_ZOOM, value);
    }

    public void setMapKey(String value) {
        mPreferences.put(KEY_MAP_KEY, value);
    }

    public void setMapZoom(int value) {
        mPreferences.putInt(KEY_MAP_ZOOM, value);
    }

    private static class MaptonOptionsHolder {

        private static final MaptonOptions INSTANCE = new MaptonOptions();
    }
}

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

import gov.nasa.worldwind.geom.Angle;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class ModuleOptions {

    public static final String KEY_DISPLAY_ATMOSPHERE = "display_atmosphere";
    public static final String KEY_DISPLAY_COMPASS = "display_compass";
    public static final String KEY_DISPLAY_CONTROLS = "display_controls";
    public static final String KEY_DISPLAY_SCALE_BAR = "display_scale_bar";
    public static final String KEY_DISPLAY_STARS = "display_stars";
    public static final String KEY_DISPLAY_WORLD_MAP = "display_world_map";
    public static final String KEY_MAP_ELEVATION = "map_elevation";
    public static final String KEY_MAP_GLOBE = "map_mode";
    public static final String KEY_MAP_OPACITY = "map_opacity";
    public static final String KEY_MAP_PROJECTION = "map_projection";
    public static final String KEY_MAP_STYLE = "map_style";
    public static final String KEY_VIEW_HEADING = "view_heading";
    public static final String KEY_VIEW_PITCH = "view_pitch";
    public static final String KEY_VIEW_ROLL = "view_roll";

    private static final boolean DEFAULT_DISPLAY_ATMOSPHERE = true;
    private static final boolean DEFAULT_DISPLAY_COMPASS = true;
    private static final boolean DEFAULT_DISPLAY_CONTROLS = true;
    private static final boolean DEFAULT_DISPLAY_SCALE_BAR = true;
    private static final boolean DEFAULT_DISPLAY_STARS = true;
    private static final boolean DEFAULT_DISPLAY_WORLD_MAP = false;
    private static final boolean DEFAULT_MAP_ELEVATION = true;
    private static final boolean DEFAULT_MAP_GLOBE = true;
    private static final double DEFAULT_MAP_OPACITY = 1.0;
    private static final int DEFAULT_MAP_PROJECTION = 1;
    private static final String DEFAULT_MAP_STYLE = Dict.MAP_TYPE_ROADMAP.toString();
    private final Preferences mPreferences = NbPreferences.forModule(ModuleOptions.class);

    public static ModuleOptions getInstance() {
        return Holder.INSTANCE;
    }

    private ModuleOptions() {
    }

    public double getMapOpacity() {
        return mPreferences.getDouble(KEY_MAP_OPACITY, DEFAULT_MAP_OPACITY);
    }

    public int getMapProjection() {
        return mPreferences.getInt(KEY_MAP_PROJECTION, DEFAULT_MAP_PROJECTION);
    }

    public String getMapStyle() {
        return mPreferences.get(KEY_MAP_STYLE, DEFAULT_MAP_STYLE);
    }

    public Preferences getPreferences() {
        return mPreferences;
    }

    public Angle getViewHeading() {
        return Angle.fromDegrees(mPreferences.getDouble(KEY_VIEW_HEADING, 0));
    }

    public Angle getViewPitch() {
        return Angle.fromDegrees(mPreferences.getDouble(KEY_VIEW_PITCH, 0));
    }

    public Angle getViewRoll() {
        return Angle.fromDegrees(mPreferences.getDouble(KEY_VIEW_ROLL, 0));
    }

    public boolean isDisplayAtmosphere() {
        return mPreferences.getBoolean(KEY_DISPLAY_ATMOSPHERE, DEFAULT_DISPLAY_ATMOSPHERE);
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

    public boolean isDisplayStar() {
        return mPreferences.getBoolean(KEY_DISPLAY_STARS, DEFAULT_DISPLAY_STARS);
    }

    public boolean isDisplayWorldMap() {
        return mPreferences.getBoolean(KEY_DISPLAY_WORLD_MAP, DEFAULT_DISPLAY_WORLD_MAP);
    }

    public boolean isMapElevation() {
        return mPreferences.getBoolean(KEY_MAP_ELEVATION, DEFAULT_MAP_ELEVATION);
    }

    public boolean isMapGlobe() {
        return mPreferences.getBoolean(KEY_MAP_GLOBE, DEFAULT_MAP_GLOBE);
    }

    public void setDisplayAtmosphere(boolean value) {
        mPreferences.putBoolean(KEY_DISPLAY_ATMOSPHERE, value);
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

    public void setDisplayStars(boolean value) {
        mPreferences.putBoolean(KEY_DISPLAY_STARS, value);
    }

    public void setDisplayWorldMap(boolean value) {
        mPreferences.putBoolean(KEY_DISPLAY_WORLD_MAP, value);
    }

    public void setMapElevation(boolean value) {
        mPreferences.putBoolean(KEY_MAP_ELEVATION, value);
    }

    public void setMapGlobe(boolean value) {
        mPreferences.putBoolean(KEY_MAP_GLOBE, value);
    }

    public void setMapOpacity(double value) {
        mPreferences.putDouble(KEY_MAP_OPACITY, value);
    }

    public void setMapProjection(int value) {
        mPreferences.putInt(KEY_MAP_PROJECTION, value);
    }

    public void setMapStyle(String value) {
        mPreferences.put(KEY_MAP_STYLE, value);
    }

    public void setViewHeading(Angle value) {
        mPreferences.putDouble(KEY_VIEW_HEADING, value.getDegrees());
    }

    public void setViewPitch(Angle value) {
        mPreferences.putDouble(KEY_VIEW_PITCH, value.getDegrees());
    }

    public void setViewRoll(Angle value) {
        mPreferences.putDouble(KEY_VIEW_ROLL, value.getDegrees());
    }

    private static class Holder {

        private static final ModuleOptions INSTANCE = new ModuleOptions();
    }
}

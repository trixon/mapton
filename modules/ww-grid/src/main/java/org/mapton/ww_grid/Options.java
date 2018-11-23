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
package org.mapton.ww_grid;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class Options {

    private static final String KEY_CLAMP_TO_GROUND = "clamp_to_ground";
    private static final String KEY_EQUATOR = "equator";
    private static final String KEY_LATITUDES = "latitudes";
    private static final String KEY_LONGITUDES = "longitudes";
    private static final String KEY_POLAR_ANTARCTIC = "polar_antarctic";
    private static final String KEY_POLAR_ARCTIC = "polar_arctic";
    private static final String KEY_TROPIC_CANCER = "tropic_cancer";
    private static final String KEY_TROPIC_CAPRICORN = "tropic_capricorn";

    private final Preferences mPreferences = NbPreferences.forModule(Options.class);

    public static Options getInstance() {
        return Holder.INSTANCE;
    }

    private Options() {
    }

    public Preferences getPreferences() {
        return mPreferences;
    }

    public boolean isClampToGround() {
        return mPreferences.getBoolean(KEY_CLAMP_TO_GROUND, true);
    }

    public boolean isEquator() {
        return mPreferences.getBoolean(KEY_EQUATOR, true);
    }

    public boolean isLatitudes() {
        return mPreferences.getBoolean(KEY_LATITUDES, true);
    }

    public boolean isLongitudes() {
        return mPreferences.getBoolean(KEY_LONGITUDES, true);
    }

    public boolean isPolarAntarctic() {
        return mPreferences.getBoolean(KEY_POLAR_ANTARCTIC, true);
    }

    public boolean isPolarArctic() {
        return mPreferences.getBoolean(KEY_POLAR_ARCTIC, true);
    }

    public boolean isTropicCancer() {
        return mPreferences.getBoolean(KEY_TROPIC_CANCER, true);
    }

    public boolean isTropicCapricorn() {
        return mPreferences.getBoolean(KEY_TROPIC_CAPRICORN, true);
    }

    public void setClampToGround(boolean value) {
        mPreferences.putBoolean(KEY_CLAMP_TO_GROUND, value);
    }

    public void setEquator(boolean value) {
        mPreferences.putBoolean(KEY_EQUATOR, value);
    }

    public void setLatitudes(boolean value) {
        mPreferences.putBoolean(KEY_LATITUDES, value);
    }

    public void setLongitudes(boolean value) {
        mPreferences.putBoolean(KEY_LONGITUDES, value);
    }

    public void setPolarAntarctic(boolean value) {
        mPreferences.putBoolean(KEY_POLAR_ANTARCTIC, value);
    }

    public void setPolarArctic(boolean value) {
        mPreferences.putBoolean(KEY_POLAR_ARCTIC, value);
    }

    public void setTropicCancer(boolean value) {
        mPreferences.putBoolean(KEY_TROPIC_CANCER, value);
    }

    public void setTropicCapricorn(boolean value) {
        mPreferences.putBoolean(KEY_TROPIC_CAPRICORN, value);
    }

    private static class Holder {

        private static final Options INSTANCE = new Options();
    }
}

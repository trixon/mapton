/*
 * Copyright 2020 Patrik Karlström.
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

import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MOptions extends OptionsBase {

    public static final String DEFAULT_UI_LAF_ICON_COLOR_BRIGHT = "D3D3D3";
    public static final String DEFAULT_UI_LAF_ICON_COLOR_DARK = "1A1A1A";
    public static final String KEY_APP_FIRST_RUN = "app.first_run";

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
    public static final String KEY_MAP_DISPLAY_CROSSHAIR = "map.display.crosshair";
    public static final String KEY_MAP_DISPLAY_HOME_ICON = "map.display.homeicon";
    public static final String KEY_MAP_ENGINE = "map.engine";
    public static final String KEY_MAP_HOME_LAT = "map.home_lat";
    public static final String KEY_MAP_HOME_LON = "map.home_lon";
    public static final String KEY_MAP_ONLY = "map_only";
    public static final String KEY_UI_LAF_DARK = "ui.laf.dark";
    public static final String KEY_UI_LAF_ICON_COLOR_BRIGHT = "ui.laf.icon_color_bright";
    public static final String KEY_UI_LAF_ICON_COLOR_DARK = "ui.laf.icon_color_dark";
    public static final String KEY_UI_POPOVER = "ui.popover";

    private static final boolean DEFAULT_FULL_SCREEN = false;
    private static final boolean DEFAULT_MAP_DISPLAY_CROSSHAIR = true;
    private static final boolean DEFAULT_MAP_DISPLAY_HOME_ICON = false;
    private static final String DEFAULT_MAP_ENGINE = "WorldWind";
    private static final double DEFAULT_MAP_LAT = 57.661509;
    private static final double DEFAULT_MAP_LON = 11.999312;
    private static final boolean DEFAULT_MAP_ONLY = false;
    private static final double DEFAULT_MAP_ZOOM = 0.8f;
    private static final boolean DEFAULT_UI_LAF_NIGHT_MODE = true;
    private static final boolean DEFAULT_UI_POPOVER = false;
    private static final String KEY_FULL_SCREEN = "fullscreen";
    private static final String KEY_MAP_CENTER_LAT = "map.center_lat";
    private static final String KEY_MAP_CENTER_LON = "map.center_lon";
    private static final String KEY_MAP_COO_TRANS = "map.coo_trans";
    private static final String KEY_MAP_HOME_ZOOM = "map.home_zoom";
    private static final String KEY_MAP_ZOOM = "map.zoom";

    private final ResourceBundle mBundle = NbBundle.getBundle(MOptions.class);
    private final BooleanProperty mDisplayCrosshairProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mDisplayHomeIconProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<String> mEngineProperty = new SimpleObjectProperty<>("WorldWind");
    private final ObjectProperty<Color> mIconColorBrightProperty = new SimpleObjectProperty<>(Color.valueOf(DEFAULT_UI_LAF_ICON_COLOR_BRIGHT));
    private final ObjectProperty<Color> mIconColorDarkProperty = new SimpleObjectProperty<>(Color.valueOf(DEFAULT_UI_LAF_ICON_COLOR_DARK));
    private final BooleanProperty mMaximizedMapProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mNightModeProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mPreferPopoverProperty = new SimpleBooleanProperty(true);

    public static MOptions getInstance() {
        return Holder.INSTANCE;
    }

    private MOptions() {
        setPreferences(NbPreferences.forModule(MOptions.class));

        initListeners();
        load();
    }

    public BooleanProperty displayCrosshairProperty() {
        return mDisplayCrosshairProperty;
    }

    public BooleanProperty displayHomeIconProperty() {
        return mDisplayHomeIconProperty;
    }

    public ObjectProperty<String> engineProperty() {
        return mEngineProperty;
    }

    public String getEngine() {
        return mEngineProperty.get();
    }

    public Color getIconColor() {
        if (is(KEY_UI_LAF_DARK)) {
            return getIconColorBright();
        } else {
            return getIconColorDark();
        }
    }

    public Color getIconColorBright() {
        return mIconColorBrightProperty.getValue();
    }

    public Color getIconColorDark() {
        return mIconColorDarkProperty.getValue();
    }

    public Color getIconColorForBackground() {
        return mNightModeProperty.get() ? mIconColorBrightProperty.getValue() : mIconColorDarkProperty.getValue();
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

    public ObjectProperty<Color> iconColorBrightProperty() {
        return mIconColorBrightProperty;
    }

    public ObjectProperty<Color> iconColorDarkProperty() {
        return mIconColorDarkProperty;
    }

    public boolean isDisplayCrosshair() {
        return mDisplayCrosshairProperty.get();
    }

    public boolean isDisplayHomeIcon() {
        return mDisplayHomeIconProperty.get();
    }

    public boolean isFirstRun() {
        return mPreferences.getBoolean(KEY_APP_FIRST_RUN, true);
    }

    public boolean isFullscreen() {
        return mPreferences.getBoolean(KEY_FULL_SCREEN, DEFAULT_FULL_SCREEN);
    }

    public boolean isMapOnly() {
        return mPreferences.getBoolean(KEY_MAP_ONLY, DEFAULT_MAP_ONLY);
    }

    public boolean isMaximizedMap() {
        return mMaximizedMapProperty.get();
    }

    public boolean isNightMode() {
        return mNightModeProperty.get();
    }

    public boolean isPreferPopover() {
        return mPreferPopoverProperty.get();
    }

    public BooleanProperty maximizedMapProperty() {
        return mMaximizedMapProperty;
    }

    public BooleanProperty nightModeProperty() {
        return mNightModeProperty;
    }

    public BooleanProperty preferPopoverProperty() {
        return mPreferPopoverProperty;
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

    private void initListeners() {
        mPreferences.addPreferenceChangeListener(pcl -> {
            switch (pcl.getKey()) {
                case KEY_UI_POPOVER:
                    //Allow setting default value on startup
                    mPreferPopoverProperty.set(is(KEY_UI_POPOVER));
                    break;
            }
        });
        mNightModeProperty.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            put(KEY_UI_LAF_DARK, t1);
        });

        mPreferPopoverProperty.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            put(KEY_UI_POPOVER, t1);
        });

        mDisplayCrosshairProperty.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            put(KEY_MAP_DISPLAY_CROSSHAIR, t1);
        });

        mDisplayHomeIconProperty.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            put(KEY_MAP_DISPLAY_HOME_ICON, t1);
        });

        mEngineProperty.addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            final MEngine oldEngine = Mapton.getEngine();
            try {
                oldEngine.onDeactivate();
                setMapZoom(oldEngine.getZoom());
                setMapCenter(oldEngine.getCenter());
            } catch (NullPointerException e) {
            }
            put(KEY_MAP_ENGINE, t1);
        });

        mIconColorBrightProperty.addListener((ObservableValue<? extends Color> ov, Color t, Color t1) -> {
            put(KEY_UI_LAF_ICON_COLOR_BRIGHT, FxHelper.colorToHexRGB(t1));
        });

        mIconColorDarkProperty.addListener((ObservableValue<? extends Color> ov, Color t, Color t1) -> {
            put(KEY_UI_LAF_ICON_COLOR_DARK, FxHelper.colorToHexRGB(t1));
        });

    }

    private void load() {
        mPreferPopoverProperty.setValue(is(KEY_UI_POPOVER, DEFAULT_UI_POPOVER));
        mNightModeProperty.setValue(is(KEY_UI_LAF_DARK, DEFAULT_UI_LAF_NIGHT_MODE));
        mDisplayCrosshairProperty.setValue(is(KEY_MAP_DISPLAY_CROSSHAIR, DEFAULT_MAP_DISPLAY_CROSSHAIR));
        mDisplayHomeIconProperty.setValue(is(KEY_MAP_DISPLAY_HOME_ICON, DEFAULT_MAP_DISPLAY_HOME_ICON));

        mEngineProperty.set(get(KEY_MAP_ENGINE, DEFAULT_MAP_ENGINE));

        mIconColorDarkProperty.set(FxHelper.colorFromHexRGBA(get(KEY_UI_LAF_ICON_COLOR_DARK, DEFAULT_UI_LAF_ICON_COLOR_DARK)));
        mIconColorBrightProperty.set(FxHelper.colorFromHexRGBA(get(KEY_UI_LAF_ICON_COLOR_BRIGHT, DEFAULT_UI_LAF_ICON_COLOR_BRIGHT)));
    }

    private static class Holder {

        private static final MOptions INSTANCE = new MOptions();
    }
}

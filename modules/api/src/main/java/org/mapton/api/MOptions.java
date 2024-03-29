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
package org.mapton.api;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
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
    public static final String KEY_COPY_LOCATION_COORDINATE_SEPARATOR = "copyLocation.coordinateSeparator";
    public static final String KEY_COPY_LOCATION_DECIMAL_SYMBOL = "copyLocation.decimalSymbol";
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

    private static final String DEFAULT_COPY_LOCATION_COORDINATE_SEPARATOR = ",";
    private static final String DEFAULT_COPY_LOCATION_DECIMAL_SYMBOL = ".";
    private static final boolean DEFAULT_MAP_DISPLAY_CROSSHAIR = true;
    private static final boolean DEFAULT_MAP_DISPLAY_HOME_ICON = false;
    private static final String DEFAULT_MAP_ENGINE = "WorldWind";
    private static final double DEFAULT_MAP_LAT = 57.557360;
    private static final double DEFAULT_MAP_LON = 12.539189;
    private static final boolean DEFAULT_MAP_ONLY = false;
    private static final double DEFAULT_MAP_ZOOM = 0.7f;
    private static final boolean DEFAULT_UI_LAF_NIGHT_MODE = true;
    private static final boolean DEFAULT_UI_POPOVER = false;
    private static final String KEY_MAP_CENTER_LAT = "map.center_lat";
    private static final String KEY_MAP_CENTER_LON = "map.center_lon";
    private static final String KEY_MAP_COO_TRANS = "map.coo_trans";
    private static final String KEY_MAP_HOME_ZOOM = "map.home_zoom";
    private static final String KEY_MAP_ZOOM = "map.zoom";
    private final StringProperty mCoordinateSeparatorProperty = new SimpleStringProperty(DEFAULT_COPY_LOCATION_COORDINATE_SEPARATOR);
    private final StringProperty mDecimalSymbolProperty = new SimpleStringProperty(DEFAULT_COPY_LOCATION_DECIMAL_SYMBOL);

    private final BooleanProperty mDisplayCrosshairProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mDisplayHomeIconProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<String> mEngineInternalProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<String> mEngineProperty = new SimpleObjectProperty<>("WorldWind");
    private final ObjectProperty<Color> mIconColorBrightProperty = new SimpleObjectProperty<>(Color.valueOf(DEFAULT_UI_LAF_ICON_COLOR_BRIGHT));
    private final ObjectProperty<Color> mIconColorDarkProperty = new SimpleObjectProperty<>(Color.valueOf(DEFAULT_UI_LAF_ICON_COLOR_DARK));
    private final BooleanProperty mMapOnlyProperty = new SimpleBooleanProperty(false);
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

    public StringProperty coordinateSeparatorProperty() {
        return mCoordinateSeparatorProperty;
    }

    public StringProperty decimalSymbolProperty() {
        return mDecimalSymbolProperty;
    }

    public BooleanProperty displayCrosshairProperty() {
        return mDisplayCrosshairProperty;
    }

    public BooleanProperty displayHomeIconProperty() {
        return mDisplayHomeIconProperty;
    }

    public ObjectProperty<String> engineInternalProperty() {
        return mEngineInternalProperty;
    }

    public ObjectProperty<String> engineProperty() {
        return mEngineProperty;
    }

    public String getCoordinateSeparator() {
        return mCoordinateSeparatorProperty.get();
    }

    public String getDecimalSymbol() {
        return mDecimalSymbolProperty.get();
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
        var items = MCrsManager.getInstance().getItems();
        for (var cooTrans : items) {
            if (cooTrans.getName().equalsIgnoreCase(getMapCooTransName())) {
                return cooTrans;
            }
        }

        var cooTrans = items.get(0);
        setMapCooTrans(cooTrans.getName());

        return cooTrans;
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

    public BooleanProperty mapOnlyProperty() {
        return mMapOnlyProperty;
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
                case KEY_MAP_ONLY ->
                    mMapOnlyProperty.set(is(KEY_MAP_ONLY));

                case KEY_UI_POPOVER -> //Allow setting default value on startup
                    mPreferPopoverProperty.set(is(KEY_UI_POPOVER));
            }
        });

        mNightModeProperty.addListener((p, o, n) -> {
            put(KEY_UI_LAF_DARK, n);
        });

        mPreferPopoverProperty.addListener((p, o, n) -> {
            put(KEY_UI_POPOVER, n);
        });

        mDisplayCrosshairProperty.addListener((p, o, n) -> {
            put(KEY_MAP_DISPLAY_CROSSHAIR, n);
        });

        mDisplayHomeIconProperty.addListener((p, o, n) -> {
            put(KEY_MAP_DISPLAY_HOME_ICON, n);
        });

        mEngineInternalProperty.addListener((p, o, n) -> {
            var oldEngine = Mapton.getEngine();

            try {
                oldEngine.onDeactivate();
                setMapZoom(oldEngine.getZoom());
                setMapCenter(oldEngine.getCenter());
            } catch (NullPointerException e) {
                //
            }

            put(KEY_MAP_ENGINE, n);
            mEngineProperty.set(n);
        });

        mIconColorBrightProperty.addListener((p, o, n) -> {
            put(KEY_UI_LAF_ICON_COLOR_BRIGHT, FxHelper.colorToHexRGB(n));
        });

        mIconColorDarkProperty.addListener((p, o, n) -> {
            put(KEY_UI_LAF_ICON_COLOR_DARK, FxHelper.colorToHexRGB(n));
        });

        mCoordinateSeparatorProperty.addListener((p, o, n) -> {
            put(KEY_COPY_LOCATION_COORDINATE_SEPARATOR, n);
        });

        mDecimalSymbolProperty.addListener((p, o, n) -> {
            put(KEY_COPY_LOCATION_DECIMAL_SYMBOL, n);
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

        mCoordinateSeparatorProperty.set(get(KEY_COPY_LOCATION_COORDINATE_SEPARATOR, DEFAULT_COPY_LOCATION_COORDINATE_SEPARATOR));
        mDecimalSymbolProperty.set(get(KEY_COPY_LOCATION_DECIMAL_SYMBOL, DEFAULT_COPY_LOCATION_DECIMAL_SYMBOL));
    }

    private static class Holder {

        private static final MOptions INSTANCE = new MOptions();
    }
}

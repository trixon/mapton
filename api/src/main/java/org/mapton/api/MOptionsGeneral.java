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

import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import static org.mapton.api.MOptions.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class MOptionsGeneral {

    public static final String DEFAULT_UI_LAF_ICON_COLOR_BRIGHT = "D3D3D3";
    public static final String DEFAULT_UI_LAF_ICON_COLOR_DARK = "1A1A1A";

    private final ResourceBundle mBundle = NbBundle.getBundle(MOptions.class);
    private final Category mCategory;
    private final BooleanProperty mDisplayCrosshairProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mDisplayHomeIconProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<String> mEngineProperty = new SimpleObjectProperty<>("WorldWind");
    private final ObjectProperty<Color> mIconColorBrightProperty = new SimpleObjectProperty<>(Color.valueOf(DEFAULT_UI_LAF_ICON_COLOR_BRIGHT));
    private final ObjectProperty<Color> mIconColorDarkProperty = new SimpleObjectProperty<>(Color.valueOf(DEFAULT_UI_LAF_ICON_COLOR_DARK));
    private final BooleanProperty mMaximizedMapProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty mNightModeProperty = new SimpleBooleanProperty(true);
    private final MOptions mOptions = MOptions.getInstance();
    private final BooleanProperty mPreferPopoverProperty = new SimpleBooleanProperty(false);

    public MOptionsGeneral() {
        ArrayList<String> engineNames = new ArrayList<>();
        Lookup.getDefault().lookupAll(MEngine.class).stream()
                .sorted((MEngine o1, MEngine o2) -> o1.getName().compareTo(o2.getName()))
                .forEach((engine) -> {
                    engineNames.add(engine.getName());
                });

        ObservableList<String> engineItems = FXCollections.observableList(engineNames);

        mCategory = Category.of(Dict.GENERAL.toString(),
                Group.of(
                        Dict.LOOK_AND_FEEL.toString(),
                        //                        Setting.of(mBundle.getString("popover"), mPreferPopoverProperty).customKey("general.map.popover"),
                        Setting.of(Dict.NIGHT_MODE.toString(), mNightModeProperty).customKey("general.map.nightMode")
                //                        Setting.of(mBundle.getString("iconColor"), mIconColorDarkProperty).customKey("general.map.iconColor"),
                //                        Setting.of(mBundle.getString("iconColorNightMode"), mIconColorBrightProperty).customKey("general.map.iconColorNightMode")
                ),
                Group.of(Dict.MAP.toString(),
                        Setting.of(MDict.MAP_ENGINE.toString(), engineItems, mEngineProperty).customKey("general.map.engine"),
                        Setting.of(mBundle.getString("maximize_map"), mMaximizedMapProperty).customKey("general.map.maximized"),
                        Setting.of(mBundle.getString("crosshair"), mDisplayCrosshairProperty).customKey("general.map.crosshair"),
                        Setting.of(mBundle.getString("homeIcon"), mDisplayHomeIconProperty).customKey("general.map.homeIcon")
                )
        );

        initListeners();
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

    public Category getCategory() {
        return mCategory;
    }

    public String getEngine() {
        return mEngineProperty.get();
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

    public ObjectProperty iconColorBrightProperty() {
        return mIconColorBrightProperty;
    }

    public ObjectProperty iconColorDarkProperty() {
        return mIconColorDarkProperty;
    }

    public boolean isDisplayCrosshair() {
        return mDisplayCrosshairProperty.get();
    }

    public boolean isDisplayHomeIcon() {
        return mDisplayHomeIconProperty.get();
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

    private void initListeners() {
        mNightModeProperty.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            mOptions.put(KEY_UI_LAF_DARK, t1);
        });

        mIconColorBrightProperty.addListener((ObservableValue<? extends Color> ov, Color t, Color t1) -> {
            mOptions.setIconColorBright(t1);
        });

        mIconColorDarkProperty.addListener((ObservableValue<? extends Color> ov, Color t, Color t1) -> {
            mOptions.setIconColorDark(t1);
        });

        mEngineProperty.addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            final MEngine oldEngine = Mapton.getEngine();
            try {
                oldEngine.onDeactivate();
                mOptions.setMapZoom(oldEngine.getZoom());
                mOptions.setMapCenter(oldEngine.getCenter());
            } catch (NullPointerException e) {
            }

            mOptions.setEngine(t1);
        });
    }
}

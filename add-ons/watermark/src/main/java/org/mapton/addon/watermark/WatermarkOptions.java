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
package org.mapton.addon.watermark;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import org.mapton.api.ui.MPresetActions;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class WatermarkOptions extends OptionsBase implements MPresetActions {

    public static final double DEFAULT_BORDER_SIZE = FxHelper.getUIScaled(0);
    public static final Color DEFAULT_COLOR_BACKGROUND = Color.BLACK;
    public static final Color DEFAULT_COLOR_BORDER = Color.WHITE;
    public static final Color DEFAULT_COLOR_FONT = Color.GRAY;
    public static final double DEFAULT_FONT_SIZE = FxHelper.getUIScaled(14);
    public static final double DEFAULT_OPACITY = 0.5;
    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH.mm.ss";
    private final ObjectProperty<Color> mBackgroundColorProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BACKGROUND);
    private final ObjectProperty<Color> mBorderColorProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BORDER);
    private final DoubleProperty mBorderSizeProperty = new SimpleDoubleProperty(DEFAULT_BORDER_SIZE);
    private final ObjectProperty<Color> mFontColorProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_FONT);
    private final DoubleProperty mFontSizeProperty = new SimpleDoubleProperty(DEFAULT_FONT_SIZE);
    private final DoubleProperty mOpacityProperty = new SimpleDoubleProperty(DEFAULT_OPACITY);
    private final StringProperty mPatternProperty = new SimpleStringProperty(DEFAULT_PATTERN);

    public static WatermarkOptions getInstance() {
        return Holder.INSTANCE;
    }

    private WatermarkOptions() {
        setPreferences(NbPreferences.forModule(WatermarkOptions.class));
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return mBackgroundColorProperty;
    }

    public ObjectProperty<Color> borderColorProperty() {
        return mBorderColorProperty;
    }

    public DoubleProperty borderSizeProperty() {
        return mBorderSizeProperty;
    }

    public ObjectProperty<Color> fontColorProperty() {
        return mFontColorProperty;
    }

    public DoubleProperty fontSizeProperty() {
        return mFontSizeProperty;
    }

    public java.awt.Color getBackgroundColor() {
        return FxHelper.colorToColor(mBackgroundColorProperty.get());
    }

    public java.awt.Color getBorderColor() {
        return FxHelper.colorToColor(mBorderColorProperty.get());
    }

    public double getBorderSize() {
        return mBorderSizeProperty.get();
    }

    public java.awt.Color getFontColor() {
        return FxHelper.colorToColor(mFontColorProperty.get());
    }

    public int getFontSize() {
        return (int) mFontSizeProperty.get();
    }

    public double getOpacity() {
        return mOpacityProperty.get();
    }

    public String getPattern() {
        return mPatternProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register("pattern", mPatternProperty);
        sessionManager.register("opacity", mOpacityProperty);
        sessionManager.register("fontSize", mFontSizeProperty);
        sessionManager.register("fontColor", mFontColorProperty);
        sessionManager.register("backgroundColor", mBackgroundColorProperty);
        sessionManager.register("borderColor", mBorderColorProperty);
        sessionManager.register("borderSize", mBorderSizeProperty);
    }

    public DoubleProperty opacityProperty() {
        return mOpacityProperty;
    }

    public StringProperty patternProperty() {
        return mPatternProperty;
    }

    @Override
    public void presetRestore(Preferences preferences) {
        presetStore(preferences);
    }

    @Override
    public void presetStore(Preferences preferences) {
        var sessionManager = initSession(preferences);
        sessionManager.unregisterAll();
    }

    public void reset() {
        mPatternProperty.set(DEFAULT_PATTERN);
        mOpacityProperty.set(DEFAULT_OPACITY);
        mFontSizeProperty.set(DEFAULT_FONT_SIZE);
        mFontColorProperty.set(DEFAULT_COLOR_FONT);
        mBackgroundColorProperty.set(DEFAULT_COLOR_BACKGROUND);
        mBorderColorProperty.set(DEFAULT_COLOR_BORDER);
        mBorderSizeProperty.set(DEFAULT_BORDER_SIZE);
    }

    private static class Holder {

        private static final WatermarkOptions INSTANCE = new WatermarkOptions();
    }
}

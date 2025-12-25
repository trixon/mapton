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
import java.awt.Color;
import java.util.prefs.Preferences;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.mapton.api.ui.MPresetActions;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ModuleOptions extends OptionsBase implements MPresetActions {

    public static final double DEFAULT_BORDER_SIZE = FxHelper.getUIScaled(0);
    public static final String DEFAULT_COLOR_BACKGROUND = "000000FF";
    public static final String DEFAULT_COLOR_BORDER = "FFFFFFFF";
    public static final String DEFAULT_COLOR_FONT = "808080FF";
    public static final double DEFAULT_FONT_SIZE = FxHelper.getUIScaled(14);
    public static final double DEFAULT_OPACITY = 0.5;
    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH.mm.ss";
    private final StringProperty mBackgroundColorProperty = new SimpleStringProperty(DEFAULT_COLOR_BACKGROUND);
    private final StringProperty mBorderColorProperty = new SimpleStringProperty(DEFAULT_COLOR_BORDER);
    private final DoubleProperty mBorderSizeProperty = new SimpleDoubleProperty(DEFAULT_BORDER_SIZE);
    private final StringProperty mFontColorProperty = new SimpleStringProperty(DEFAULT_COLOR_FONT);
    private final DoubleProperty mFontSizeProperty = new SimpleDoubleProperty(DEFAULT_FONT_SIZE);
    private final DoubleProperty mOpacityProperty = new SimpleDoubleProperty(DEFAULT_OPACITY);
    private final StringProperty mPatternProperty = new SimpleStringProperty(DEFAULT_PATTERN);

    public static ModuleOptions getInstance() {
        return Holder.INSTANCE;
    }

    private ModuleOptions() {
        setPreferences(NbPreferences.forModule(ModuleOptions.class));
    }

    public StringProperty backgroundColorProperty() {
        return mBackgroundColorProperty;
    }

    public StringProperty borderColorProperty() {
        return mBorderColorProperty;
    }

    public DoubleProperty borderSizeProperty() {
        return mBorderSizeProperty;
    }

    public StringProperty fontColorProperty() {
        return mFontColorProperty;
    }

    public DoubleProperty fontSizeProperty() {
        return mFontSizeProperty;
    }

    public Color getBackgroundColor() {
        return FxHelper.colorToColor(FxHelper.colorFromHexRGBA(mBackgroundColorProperty.get()));
    }

    public Color getBorderColor() {
        return FxHelper.colorToColor(FxHelper.colorFromHexRGBA(mBorderColorProperty.get()));
    }

    public double getBorderSize() {
        return mBorderSizeProperty.get();
    }

    public Color getFontColor() {
        return FxHelper.colorToColor(FxHelper.colorFromHexRGBA(mFontColorProperty.get()));
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
        //clear();
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

    private SessionManager initSession(Preferences preferences) {
        var sessionManager = new SessionManager(preferences);
        initSession(sessionManager);

        return sessionManager;
    }

    private static class Holder {

        private static final ModuleOptions INSTANCE = new ModuleOptions();
    }
}

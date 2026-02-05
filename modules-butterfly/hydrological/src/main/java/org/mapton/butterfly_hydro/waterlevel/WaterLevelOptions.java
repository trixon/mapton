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
package org.mapton.butterfly_hydro.waterlevel;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.mapton.api.ui.MPresetActions;
import org.mapton.butterfly_core.api.BOptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class WaterLevelOptions extends BOptionsBase implements MPresetActions {

    public static final WaterLevelColorBy DEFAULT_COLOR_BY = WaterLevelColorBy.DEFAULT;
    public static final WaterLevelLabelBy DEFAULT_LABEL_BY = WaterLevelLabelBy.NAME;
    public static final WaterLevelPointBy DEFAULT_POINT_BY = WaterLevelPointBy.PIN;
    private final ObjectProperty<WaterLevelColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<WaterLevelLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<WaterLevelPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static WaterLevelOptions getInstance() {
        return Holder.INSTANCE;
    }

    private WaterLevelOptions() {
        initColorProxyProperty(mColorByProperty, WaterLevelColorBy.class);
        initLabelProxyProperty(mLabelByProperty, WaterLevelLabelBy.class);
        initPointProxyProperty(mPointByProperty, WaterLevelPointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
    }

    public ObjectProperty<WaterLevelColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public WaterLevelColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public WaterLevelLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public WaterLevelPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<WaterLevelLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<WaterLevelPointBy> pointProperty() {
        return mPointByProperty;
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

    @Override
    public void reset() {
        super.reset();
        mPointByProperty.set(DEFAULT_POINT_BY);
        mColorByProperty.set(DEFAULT_COLOR_BY);
        mLabelByProperty.set(DEFAULT_LABEL_BY);
    }

    private static class Holder {

        private static final WaterLevelOptions INSTANCE = new WaterLevelOptions();
    }
}

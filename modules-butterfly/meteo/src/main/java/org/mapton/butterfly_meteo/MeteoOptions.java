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
package org.mapton.butterfly_meteo;

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
public class MeteoOptions extends BOptionsBase implements MPresetActions {

    public static final MeteoColorBy DEFAULT_COLOR_BY = MeteoColorBy.DEFAULT;
    public static final MeteoLabelBy DEFAULT_LABEL_BY = MeteoLabelBy.NAME;
    public static final MeteoPointBy DEFAULT_POINT_BY = MeteoPointBy.PIN;
    private final ObjectProperty<MeteoColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<MeteoLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<MeteoPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static MeteoOptions getInstance() {
        return Holder.INSTANCE;
    }

    private MeteoOptions() {
        initColorProxyProperty(mColorByProperty, MeteoColorBy.class);
        initLabelProxyProperty(mLabelByProperty, MeteoLabelBy.class);
        initPointProxyProperty(mPointByProperty, MeteoPointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
        disablePlotAlarm();
        disablePlotDebt();
    }

    public ObjectProperty<MeteoColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public MeteoColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public MeteoLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public MeteoPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<MeteoLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<MeteoPointBy> pointProperty() {
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

        private static final MeteoOptions INSTANCE = new MeteoOptions();
    }
}

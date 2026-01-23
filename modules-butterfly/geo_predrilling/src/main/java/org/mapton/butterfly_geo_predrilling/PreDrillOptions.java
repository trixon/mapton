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
package org.mapton.butterfly_geo_predrilling;

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
public class PreDrillOptions extends BOptionsBase implements MPresetActions {

    public static final PreDrillColorBy DEFAULT_COLOR_BY = PreDrillColorBy.DEFAULT;
    public static final PreDrillLabelBy DEFAULT_LABEL_BY = PreDrillLabelBy.MISC_DATE;
    public static final PreDrillPointBy DEFAULT_POINT_BY = PreDrillPointBy.PIN;
    private final ObjectProperty<PreDrillColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<PreDrillLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<PreDrillPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static PreDrillOptions getInstance() {
        return Holder.INSTANCE;
    }

    private PreDrillOptions() {
        initColorProxyProperty(mColorByProperty, PreDrillColorBy.class);
        initLabelProxyProperty(mLabelByProperty, PreDrillLabelBy.class);
        initPointProxyProperty(mPointByProperty, PreDrillPointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
        disablePlotDebt();
    }

    public ObjectProperty<PreDrillColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public PreDrillColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public PreDrillLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public PreDrillPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<PreDrillLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<PreDrillPointBy> pointProperty() {
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

        private static final PreDrillOptions INSTANCE = new PreDrillOptions();
    }
}

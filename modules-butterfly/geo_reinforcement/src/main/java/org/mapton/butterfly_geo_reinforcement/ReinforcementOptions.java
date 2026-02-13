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
package org.mapton.butterfly_geo_reinforcement;

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
public class ReinforcementOptions extends BOptionsBase implements MPresetActions {

    public static final ReinforcementColorBy DEFAULT_COLOR_BY = ReinforcementColorBy.DEFAULT;
    public static final ReinforcementLabelBy DEFAULT_LABEL_BY = ReinforcementLabelBy.MISC_DATE;
    public static final ReinforcementPointBy DEFAULT_POINT_BY = ReinforcementPointBy.PIN;
    private final ObjectProperty<ReinforcementColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<ReinforcementLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<ReinforcementPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static ReinforcementOptions getInstance() {
        return Holder.INSTANCE;
    }

    private ReinforcementOptions() {
        initColorProxyProperty(mColorByProperty, ReinforcementColorBy.class);
        initLabelProxyProperty(mLabelByProperty, ReinforcementLabelBy.class);
        initPointProxyProperty(mPointByProperty, ReinforcementPointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
        disablePlotDebt();
    }

    public ObjectProperty<ReinforcementColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public ReinforcementColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public ReinforcementLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public ReinforcementPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<ReinforcementLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<ReinforcementPointBy> pointProperty() {
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

        private static final ReinforcementOptions INSTANCE = new ReinforcementOptions();
    }
}

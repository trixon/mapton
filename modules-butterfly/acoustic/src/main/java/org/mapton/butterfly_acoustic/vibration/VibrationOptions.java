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
package org.mapton.butterfly_acoustic.vibration;

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
public class VibrationOptions extends BOptionsBase implements MPresetActions {

    public static final VibrationColorBy DEFAULT_COLOR_BY = VibrationColorBy.DEFAULT;
    public static final VibrationLabelBy DEFAULT_LABEL_BY = VibrationLabelBy.NAME;
    public static final VibrationPointBy DEFAULT_POINT_BY = VibrationPointBy.PIN;
    private final ObjectProperty<VibrationColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<VibrationLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<VibrationPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static VibrationOptions getInstance() {
        return Holder.INSTANCE;
    }

    private VibrationOptions() {
        initColorProxyProperty(mColorByProperty, VibrationColorBy.class);
        initLabelProxyProperty(mLabelByProperty, VibrationLabelBy.class);
        initPointProxyProperty(mPointByProperty, VibrationPointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
        disablePlotDebt();
    }

    public ObjectProperty<VibrationColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public VibrationColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public VibrationLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public VibrationPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<VibrationLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<VibrationPointBy> pointProperty() {
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

        private static final VibrationOptions INSTANCE = new VibrationOptions();
    }
}
